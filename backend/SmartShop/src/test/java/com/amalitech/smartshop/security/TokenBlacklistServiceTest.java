package com.amalitech.smartshop.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenBlacklistServiceTest {

    private TokenBlacklistService tokenBlacklistService;

    @BeforeEach
    void setUp() {
        tokenBlacklistService = new TokenBlacklistService();
    }

    @Test
    void revokeToken_makesTokenRevoked() {
        String token = "test-token";
        long futureExpiry = System.currentTimeMillis() + 60000;

        tokenBlacklistService.revokeToken(token, futureExpiry);

        assertTrue(tokenBlacklistService.isRevoked(token));
    }

    @Test
    void isRevoked_returnsFalseForNonRevokedToken() {
        assertFalse(tokenBlacklistService.isRevoked("unknown-token"));
    }

    @Test
    void isRevoked_returnsFalseForExpiredRevokedToken() {
        String token = "expired-token";
        long pastExpiry = System.currentTimeMillis() - 1000;

        tokenBlacklistService.revokeToken(token, pastExpiry);

        assertFalse(tokenBlacklistService.isRevoked(token));
    }

    @Test
    void getBlacklistSize_returnsCorrectCount() {
        tokenBlacklistService.revokeToken("token1", System.currentTimeMillis() + 60000);
        tokenBlacklistService.revokeToken("token2", System.currentTimeMillis() + 60000);

        assertEquals(2, tokenBlacklistService.getBlacklistSize());
    }

    @Test
    void cleanupExpiredTokens_removesExpiredEntries() {
        tokenBlacklistService.revokeToken("expired", System.currentTimeMillis() - 1000);
        tokenBlacklistService.revokeToken("valid", System.currentTimeMillis() + 60000);

        tokenBlacklistService.cleanupExpiredTokens();

        assertEquals(1, tokenBlacklistService.getBlacklistSize());
        assertFalse(tokenBlacklistService.isRevoked("expired"));
        assertTrue(tokenBlacklistService.isRevoked("valid"));
    }
}
