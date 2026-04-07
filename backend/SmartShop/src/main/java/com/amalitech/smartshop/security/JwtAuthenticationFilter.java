package com.amalitech.smartshop.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    // Cache for user details with simple TTL (5 minutes)
    private final ConcurrentHashMap<String, CachedUserDetails> userDetailsCache = new ConcurrentHashMap<>();

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        // Fast path: No auth header
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(BEARER_PREFIX.length());

        // Fast path: Quick format validation
        if (!jwtService.isTokenFormatValid(jwt)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Extract username (now with caching)
            String username = jwtService.extractUsernameIfValid(jwt);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Get user details from cache or load from DB
                UserDetails userDetails = getUserDetailsWithCache(username);

                // Validate token (now with caching)
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    if (log.isDebugEnabled()) {
                        log.debug("Authenticated user: {}", username);
                    }
                }
            }
        } catch (Exception exception) {
            // Use debug logging to reduce overhead
            if (log.isDebugEnabled()) {
                log.debug("JWT authentication failed: {}", exception.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        String method = request.getMethod();

        // Skip OPTIONS requests entirely
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        // Fast path for public GET endpoints
        if ("GET".equalsIgnoreCase(method) &&
                (path.startsWith("/api/products") || path.startsWith("/api/categories"))) {
            return true;
        }

        // Public endpoints
        return path.startsWith("/api/auth/login")
                || path.startsWith("/api/auth/register")
                || path.startsWith("/oauth2/")
                || path.startsWith("/login/oauth2/")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-resources")
                || path.startsWith("/graphiql")
                || path.startsWith("/actuator/health");
    }

    /**
     * Get user details with caching (5-minute TTL)
     */
    private UserDetails getUserDetailsWithCache(String username) {
        CachedUserDetails cached = userDetailsCache.get(username);

        // Check if cache is valid (within 5 minutes)
        if (cached != null && System.currentTimeMillis() - cached.cachedAt < 300_000) {
            if (log.isDebugEnabled()) {
                log.debug("Cache hit for user: {}", username);
            }
            return cached.userDetails;
        }

        // Cache miss or expired - load from DB
        if (log.isDebugEnabled()) {
            log.debug("Cache miss for user: {}, loading from DB", username);
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        userDetailsCache.put(username, new CachedUserDetails(userDetails));
        return userDetails;
    }

    /**
     * Invalidate cache for a user (call on logout or role change)
     */
    public void invalidateUserCache(String username) {
        userDetailsCache.remove(username);
        if (log.isDebugEnabled()) {
            log.debug("Invalidated cache for user: {}", username);
        }
    }

    /**
     * Helper class for cached user details with timestamp
     */
    private static class CachedUserDetails {
        final UserDetails userDetails;
        final long cachedAt;

        CachedUserDetails(UserDetails userDetails) {
            this.userDetails = userDetails;
            this.cachedAt = System.currentTimeMillis();
        }
    }
}