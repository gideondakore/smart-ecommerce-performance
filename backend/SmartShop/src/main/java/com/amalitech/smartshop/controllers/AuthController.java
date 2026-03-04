package com.amalitech.smartshop.controllers;

import com.amalitech.smartshop.dtos.requests.AuthLoginRequest;
import com.amalitech.smartshop.dtos.requests.AuthRegisterRequest;
import com.amalitech.smartshop.dtos.responses.ApiResponse;
import com.amalitech.smartshop.dtos.responses.AuthResponse;
import com.amalitech.smartshop.security.AuthService;
import com.amalitech.smartshop.security.JwtService;
import com.amalitech.smartshop.security.TokenBlacklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for JWT-based authentication operations.
 * Provides register, login, logout, token refresh, and validation endpoints.
 */
@Tag(name = "Auth", description = "JWT Authentication APIs")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    @Operation(summary = "Register a new user")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody AuthRegisterRequest request) {
        AuthResponse authResponse = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED.value(), "User registered successfully", authResponse));
    }

    @Operation(summary = "Login with email and password")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody AuthLoginRequest request,
            HttpServletRequest httpRequest) {
        String clientIp = httpRequest.getRemoteAddr();
        AuthResponse authResponse = authService.login(request, clientIp);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Login successful", authResponse));
    }

    @Operation(summary = "Refresh access token", security = @SecurityRequirement(name = "BearerAuth"))
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@RequestHeader("Authorization") String authHeader) {
        String refreshToken = extractToken(authHeader);
        AuthResponse authResponse = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Token refreshed successfully", authResponse));
    }

    @Operation(summary = "Logout user (revoke current token)", security = @SecurityRequirement(name = "BearerAuth"))
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        if (token != null) {
            authService.logout(token);
        }
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Logged out successfully", null));
    }

    @Operation(summary = "Validate a JWT token", security = @SecurityRequirement(name = "BearerAuth"))
    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<Boolean>> validateToken(HttpServletRequest request) {
        String token = extractTokenFromRequest(request);
        boolean valid = token != null && jwtService.isTokenStructurallyValid(token);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Token validation completed", valid));
    }

    @Operation(summary = "Get blacklist size (admin only)", security = @SecurityRequirement(name = "BearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/blacklist/size")
    public ResponseEntity<ApiResponse<Integer>> getBlacklistSize() {
        int size = tokenBlacklistService.getBlacklistSize();
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Blacklist size fetched", size));
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        return extractToken(authHeader);
    }

    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
