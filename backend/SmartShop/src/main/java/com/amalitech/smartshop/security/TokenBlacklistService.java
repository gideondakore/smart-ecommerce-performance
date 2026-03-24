package com.amalitech.smartshop.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory token blacklist using ConcurrentHashMap for O(1) lookups.
 * Stores revoked JWT tokens mapped to their expiry timestamps.
 * Periodically cleans up expired entries to prevent memory leaks.
 */
@Service
@Slf4j
public class TokenBlacklistService {

    private final Map<String, Long> revokedTokens = new ConcurrentHashMap<>();

    /**
     * Revokes a JWT token by adding it to the blacklist.
     *
     * @param token   the JWT token to revoke
     * @param expiryMs the token's original expiry time in milliseconds since epoch
     */
    public void revokeToken(String token, long expiryMs) {
        revokedTokens.put(token, expiryMs);
        log.info("Token revoked. Active blacklist size: {}", revokedTokens.size());
    }

    /**
     * Checks whether a token has been revoked.
     * Also performs lazy cleanup if the token has expired.
     *
     * @param token the JWT token to check
     * @return true if the token is revoked and not yet expired
     */
    public boolean isRevoked(String token) {
        Long expiry = revokedTokens.get(token);
        if (expiry == null) {
            return false;
        }
        if (System.currentTimeMillis() > expiry) {
            revokedTokens.remove(token);
            return false;
        } // Not necessary

        return true;
    }

    /**
     * Scheduled cleanup of expired tokens from the blacklist.
     * Runs every 30 minutes to prevent unbounded memory growth.
     */
    @Scheduled(fixedRate = 1800000)
    public void cleanupExpiredTokens() {
        long now = System.currentTimeMillis();
        int sizeBefore = revokedTokens.size();
        revokedTokens.entrySet().removeIf(entry -> now > entry.getValue());
        int removed = sizeBefore - revokedTokens.size();
        if (removed > 0) {
            log.info("Cleaned up {} expired tokens from blacklist. Remaining: {}", removed, revokedTokens.size());
        }
    }

    public int getBlacklistSize() {
        return revokedTokens.size();
    }
}
