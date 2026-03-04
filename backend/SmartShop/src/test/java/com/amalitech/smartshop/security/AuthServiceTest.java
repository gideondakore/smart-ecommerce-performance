package com.amalitech.smartshop.security;

import com.amalitech.smartshop.dtos.requests.AuthLoginRequest;
import com.amalitech.smartshop.dtos.requests.AuthRegisterRequest;
import com.amalitech.smartshop.dtos.responses.AuthResponse;
import com.amalitech.smartshop.entities.User;
import com.amalitech.smartshop.enums.UserRole;
import com.amalitech.smartshop.exceptions.ResourceAlreadyExistsException;
import com.amalitech.smartshop.repositories.jpa.UserJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private AuthService authService;

    @Mock
    private UserJpaRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtService,
                authenticationManager, tokenBlacklistService);
    }

    private User createTestUser() {
        return User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .password("$2a$10$encoded")
                .role(UserRole.CUSTOMER)
                .build();
    }

    @Test
    void register_createsUserAndReturnsTokens() {
        AuthRegisterRequest request = new AuthRegisterRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john@example.com");
        request.setPassword("password123");
        request.setRole(UserRole.CUSTOMER);

        User savedUser = createTestUser();

        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$encoded");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateAccessToken(savedUser)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(savedUser)).thenReturn("refresh-token");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals("john@example.com", response.getEmail());
        verify(passwordEncoder).encode("password123");
    }

    @Test
    void register_throwsWhenEmailExists() {
        AuthRegisterRequest request = new AuthRegisterRequest();
        request.setEmail("existing@example.com");

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class, () -> authService.register(request));
    }

    @Test
    void login_returnsTokensOnSuccess() {
        AuthLoginRequest request = new AuthLoginRequest();
        request.setEmail("john@example.com");
        request.setPassword("password123");

        User user = createTestUser();

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(user)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(user)).thenReturn("refresh-token");

        AuthResponse response = authService.login(request, "127.0.0.1");

        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_throwsOnBadCredentials() {
        AuthLoginRequest request = new AuthLoginRequest();
        request.setEmail("john@example.com");
        request.setPassword("wrong");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> authService.login(request, "127.0.0.1"));
    }

    @Test
    void refreshToken_returnsNewAccessToken() {
        String refreshToken = "valid-refresh-token";
        User user = createTestUser();

        when(jwtService.extractUsername(refreshToken)).thenReturn("john@example.com");
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(jwtService.isTokenValid(refreshToken, user)).thenReturn(true);
        when(jwtService.generateAccessToken(user)).thenReturn("new-access-token");

        AuthResponse response = authService.refreshToken(refreshToken);

        assertNotNull(response);
        assertEquals("new-access-token", response.getAccessToken());
    }

    @Test
    void refreshToken_throwsOnInvalidToken() {
        String refreshToken = "invalid-token";
        User user = createTestUser();

        when(jwtService.extractUsername(refreshToken)).thenReturn("john@example.com");
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(jwtService.isTokenValid(refreshToken, user)).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.refreshToken(refreshToken));
    }

    @Test
    void logout_revokesToken() {
        String token = "token-to-revoke";
        Date expiry = new Date(System.currentTimeMillis() + 60000);

        when(jwtService.extractExpiration(token)).thenReturn(expiry);

        authService.logout(token);

        verify(tokenBlacklistService).revokeToken(token, expiry.getTime());
    }
}
