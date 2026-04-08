package com.amalitech.smartshop.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Service
@Slf4j
public class JwtService {

    private final SecretKey signingKey;
    @Getter
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;
    private final TokenBlacklistService tokenBlacklistService;

    // Cache for parsed claims to avoid repeated JWT parsing
    private final Cache<String, Claims> claimsCache;

    // Cache for token validation results
    private final Cache<String, Boolean> validationCache;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration-ms:3600000}") long accessTokenExpirationMs,
            @Value("${jwt.refresh-token-expiration-ms:86400000}") long refreshTokenExpirationMs,
            TokenBlacklistService tokenBlacklistService) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
        this.tokenBlacklistService = tokenBlacklistService;

        // Cache claims for 5 minutes or until token expires
        this.claimsCache = Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .maximumSize(1000)
                .recordStats()
                .build();

        // Cache validation results for 1 minute
        this.validationCache = Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .maximumSize(2000)
                .recordStats()
                .build();
    }

    public String generateAccessToken(UserDetails userDetails) {
        List<String> roles = userDetails.getAuthorities().stream()
                .map(Object::toString)
                .toList();

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("roles", roles)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpirationMs))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpirationMs))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * FAST PATH: Quick format validation without full parsing
     */
    public boolean isTokenFormatInvalid(String token) {
        if (token == null || token.trim().isEmpty()) {
            return true;
        }
        // Quick JWT structure check: header.payload.signature
        int dotCount = 0;
        for (int i = 0; i < token.length(); i++) {
            if (token.charAt(i) == '.') {
                dotCount++;
            }
        }
        return dotCount != 2;
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaimsWithCache(token);
        return claimsResolver.apply(claims);
    }

    /**
     * OPTIMIZED: Validate token with caching
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        if (token == null || userDetails == null) {
            return false;
        }

        String cacheKey = token + ":" + userDetails.getUsername();

        return validationCache.get(cacheKey, key -> {
            try {
                // Quick format validation first
                if (isTokenFormatInvalid(token)) {
                    return false;
                }

                // Check blacklist
                if (tokenBlacklistService.isRevoked(token)) {
                    return false;
                }

                Claims claims = extractAllClaimsWithCache(token);

                // Check expiration
                Date expiration = claims.getExpiration();
                if (expiration == null || expiration.before(new Date())) {
                    return false;
                }

                // Verify username matches
                String username = claims.getSubject();
                return username != null && username.equals(userDetails.getUsername());
            } catch (JwtException | IllegalArgumentException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Token validation failed: {}", e.getMessage());
                }
                return false;
            }
        });
    }

    /**
     * OPTIMIZED: Extract username with caching
     */
    public String extractUsernameIfValid(String token) {
        if (token == null || isTokenFormatInvalid(token)) {
            return null;
        }

        try {
            Claims claims = extractAllClaimsWithCache(token);

            // Check expiration
            Date expiration = claims.getExpiration();
            if (expiration != null && expiration.before(new Date())) {
                return null;
            }

            // Check blacklist
            if (tokenBlacklistService.isRevoked(token)) {
                return null;
            }

            return claims.getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            if (log.isDebugEnabled()) {
                log.debug("Failed to extract username: {}", e.getMessage());
            }
            return null;
        }
    }

    public boolean isTokenStructurallyValid(String token) {
        return extractUsernameIfValid(token) != null;
    }

    @SuppressWarnings("unchecked")
    public String extractRole(String token) {
        List<String> roles = extractClaim(token, claims -> claims.get("roles", List.class));
        if (roles == null || roles.isEmpty()) {
            return null;
        }
        String firstRole = roles.getFirst();
        return firstRole.startsWith("ROLE_") ? firstRole.substring(5) : firstRole;
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * CORE OPTIMIZATION: Extract claims with caching to avoid repeated parsing
     */
    private Claims extractAllClaimsWithCache(String token) {
        return claimsCache.get(token, this::parseClaims);
    }

    /**
     * Parse claims from token (expensive operation)
     */
    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            log.warn("Failed to parse JWT token: {}", e.getMessage());
            throw e;
        }
    }

}