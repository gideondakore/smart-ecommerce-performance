package com.amalitech.smartshop.controllers;

import com.amalitech.smartshop.config.RequiresRole;
import com.amalitech.smartshop.dtos.responses.ApiResponse;
import com.amalitech.smartshop.enums.UserRole;
import com.amalitech.smartshop.interfaces.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication", description = "APIs for authentication operations")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SessionService sessionService;

    @Operation(summary = "Logout user")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        String token = (String) request.getAttribute("sessionToken");
        if (token != null) {
            sessionService.deleteSession(token);
        }
        ApiResponse<Void> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Logged out successfully", null);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Logout from all devices")
    @PostMapping("/logout-all")
    public ResponseEntity<ApiResponse<Void>> logoutAll(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("authUserId");
        sessionService.deleteAllUserSessions(userId);
        ApiResponse<Void> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Logged out from all devices", null);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Validate a session token")
    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<Boolean>> validateSession(HttpServletRequest request) {
        String token = (String) request.getAttribute("sessionToken");
        boolean valid = token != null && sessionService.isSessionValid(token);
        ApiResponse<Boolean> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Session validation completed", valid);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Clean expired sessions")
    @RequiresRole(UserRole.ADMIN)
    @DeleteMapping("/sessions/expired")
    public ResponseEntity<ApiResponse<Integer>> cleanExpiredSessions() {
        int deleted = sessionService.cleanExpiredSessions();
        ApiResponse<Integer> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), deleted + " expired sessions cleaned", deleted);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Get active session count")
    @RequiresRole(UserRole.ADMIN)
    @GetMapping("/sessions/active-count")
    public ResponseEntity<ApiResponse<Long>> getActiveSessionCount() {
        Long count = sessionService.countActiveSessions();
        ApiResponse<Long> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Active session count fetched", count);
        return ResponseEntity.ok(apiResponse);
    }
}
