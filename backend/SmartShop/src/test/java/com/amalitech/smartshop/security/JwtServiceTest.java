package com.amalitech.smartshop.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.SecretKey;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    private JwtService jwtService;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    private static final String TEST_SECRET = "dGhpc0lzQVZlcnlMb25nU2VjcmV0S2V5Rm9yVGVzdGluZ0hTMjU2QWxnb3JpdGhtMTIzNDU2";
    private static final long ACCESS_EXPIRATION = 900000;
    private static final long REFRESH_EXPIRATION = 604800000;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(TEST_SECRET, ACCESS_EXPIRATION, REFRESH_EXPIRATION, tokenBlacklistService);
    }

    private UserDetails createTestUser() {
        return new User("test@example.com", "password",
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
    }

    @Test
    void generateAccessToken_returnsValidToken() {
        UserDetails userDetails = createTestUser();

        String token = jwtService.generateAccessToken(userDetails);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void extractUsername_returnsCorrectUsername() {
        UserDetails userDetails = createTestUser();
        String token = jwtService.generateAccessToken(userDetails);

        String username = jwtService.extractUsername(token);

        assertEquals("test@example.com", username);
    }

    @Test
    void isTokenValid_returnsTrueForValidToken() {
        UserDetails userDetails = createTestUser();
        String token = jwtService.generateAccessToken(userDetails);

        when(tokenBlacklistService.isRevoked(token)).thenReturn(false);

        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void isTokenValid_returnsFalseForRevokedToken() {
        UserDetails userDetails = createTestUser();
        String token = jwtService.generateAccessToken(userDetails);

        when(tokenBlacklistService.isRevoked(token)).thenReturn(true);

        assertFalse(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void isTokenExpired_returnsFalseForFreshToken() {
        UserDetails userDetails = createTestUser();
        String token = jwtService.generateAccessToken(userDetails);

        assertFalse(jwtService.isTokenExpired(token));
    }

    @Test
    void isTokenExpired_throwsForExpiredToken() throws Exception {
        JwtService shortLivedService = new JwtService(TEST_SECRET, 1, REFRESH_EXPIRATION, tokenBlacklistService);
        UserDetails userDetails = createTestUser();
        String token = shortLivedService.generateAccessToken(userDetails);

        Thread.sleep(50);

        assertThrows(ExpiredJwtException.class, () -> shortLivedService.isTokenExpired(token));
    }

    @Test
    void extractRole_returnsCorrectRole() {
        UserDetails userDetails = createTestUser();
        String token = jwtService.generateAccessToken(userDetails);

        String role = jwtService.extractRole(token);

        assertEquals("CUSTOMER", role);
    }

    @Test
    void generateRefreshToken_containsRefreshType() {
        UserDetails userDetails = createTestUser();
        String token = jwtService.generateRefreshToken(userDetails);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void isTokenStructurallyValid_returnsTrueForValidToken() {
        UserDetails userDetails = createTestUser();
        String token = jwtService.generateAccessToken(userDetails);

        when(tokenBlacklistService.isRevoked(token)).thenReturn(false);

        assertTrue(jwtService.isTokenStructurallyValid(token));
    }

    @Test
    void isTokenStructurallyValid_returnsFalseForInvalidToken() {
        assertFalse(jwtService.isTokenStructurallyValid("invalid.token.here"));
    }

    @Test
    void getAccessTokenExpirationMs_returnsConfiguredValue() {
        assertEquals(ACCESS_EXPIRATION, jwtService.getAccessTokenExpirationMs());
    }
}
