package com.amalitech.smartshop.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    // Cache for user details with simple TTL (5 minutes)
    private final Cache<String, UserDetails> userDetailsCache = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .maximumSize(1000)
            .build();



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
        if (jwtService.isTokenFormatInvalid(jwt)) {
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
                    // Creating new SecurityContext instance to avoid race condition
                    SecurityContext context = SecurityContextHolder.createEmptyContext();
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    context.setAuthentication(authToken);

                    SecurityContextHolder.setContext(context);

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
        String method = request.getMethod();

        // Skip OPTIONS requests entirely
        return "OPTIONS".equalsIgnoreCase(method);
    }

    /**
     * Get user details with caching (5-minute TTL)
     */
    private UserDetails getUserDetailsWithCache(String username) {
        return userDetailsCache.get(username, key -> {

            if(log.isDebugEnabled()) {
                log.info("Cache miss for user {}, loading from DB", key);
            }
            return userDetailsService.loadUserByUsername(username);
        });
    }



}