package com.amalitech.smartshop.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

/**
 * Handles JWT token generation, validation, and claim extraction.
 * Uses HMAC SHA-256 (HS256) signing algorithm with a configurable secret key.
 */
@Service
@Slf4j
public class JwtService {

    private final SecretKey signingKey;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;
    private final TokenBlacklistService tokenBlacklistService;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration-ms:3600000}") long accessTokenExpirationMs,
            @Value("${jwt.refresh-token-expiration-ms:86400000}") long refreshTokenExpirationMs,
            TokenBlacklistService tokenBlacklistService) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
        this.tokenBlacklistService = tokenBlacklistService;
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

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsernameIfValid(token);
        return username != null && username.equals(userDetails.getUsername());
    }

    public boolean isTokenStructurallyValid(String token) {
        try {
            return extractUsernameIfValid(token) != null;
        } catch (JwtException | IllegalArgumentException exception) {
            log.warn("Invalid JWT token: {}", exception.getMessage());
            return false;
        }
    }

    public String extractUsernameIfValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            if (claims.getExpiration() == null || claims.getExpiration().before(new Date())) {
                return null;
            }
            if (tokenBlacklistService.isRevoked(token)) {
                return null;
            }
            return claims.getSubject();
        } catch (JwtException | IllegalArgumentException exception) {
            return null;
        }
    }

    public long getAccessTokenExpirationMs() {
        return accessTokenExpirationMs;
    }

    /**
     * Extracts the primary role from a JWT token.
     * Roles are stored as a list with ROLE_ prefix; this returns the role name without the prefix.
     */
    @SuppressWarnings("unchecked")
    public String extractRole(String token) {
        List<String> roles = extractClaim(token, claims -> claims.get("roles", List.class));
        if (roles == null || roles.isEmpty()) {
            return null;
        }
        String firstRole = roles.getFirst();
        return firstRole.startsWith("ROLE_") ? firstRole.substring(5) : firstRole;
    }

    /**
     * Checks whether the given JWT token is expired.
     */
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
