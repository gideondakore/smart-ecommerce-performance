package com.amalitech.smartshop.controllers;

import com.amalitech.smartshop.dtos.requests.UpdateUserDTO;
import com.amalitech.smartshop.dtos.responses.ApiResponse;
import com.amalitech.smartshop.dtos.responses.PagedResponse;
import com.amalitech.smartshop.dtos.responses.UserRoleStatsDTO;
import com.amalitech.smartshop.dtos.responses.UserSummaryDTO;
import com.amalitech.smartshop.entities.User;
import com.amalitech.smartshop.interfaces.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for user management operations.
 * Uses Spring Security @PreAuthorize for role-based access control.
 */
@Tag(name = "User Management", description = "APIs for managing users")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "BearerAuth")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get all users (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<UserSummaryDTO>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<UserSummaryDTO> usersPage = userService.getAllUsers(pageable);
        PagedResponse<UserSummaryDTO> pagedResponse = new PagedResponse<>(
                usersPage.getContent(),
                usersPage.getNumber(),
                (int) usersPage.getTotalElements(),
                usersPage.getTotalPages(),
                usersPage.isLast()
        );
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Users fetched successfully", pagedResponse));
    }

    @Operation(summary = "Get authenticated user's profile")
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserSummaryDTO>> getProfile(@AuthenticationPrincipal User currentUser) {
        UserSummaryDTO user = userService.findUserById(currentUser.getId());
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "User profile fetched successfully", user));
    }

    @Operation(summary = "Update authenticated user's profile")
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserSummaryDTO>> updateProfile(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody UpdateUserDTO updateUserDTO) {
        UserSummaryDTO updatedUser = userService.updateUser(currentUser.getId(), updateUserDTO);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "User profile updated successfully", updatedUser));
    }

    @Operation(summary = "Get user by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserSummaryDTO>> getUserById(@PathVariable Long id) {
        UserSummaryDTO user = userService.findUserById(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "User fetched successfully", user));
    }

    @Operation(summary = "Update user details (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserSummaryDTO>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserDTO request) {
        UserSummaryDTO updatedUser = userService.updateUser(id, request);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "User updated successfully", updatedUser));
    }

    @Operation(summary = "Delete a user (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "User deleted successfully", null));
    }

    @Operation(summary = "Check if email exists")
    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<Boolean>> checkEmailExists(@RequestParam String email) {
        boolean exists = userService.emailExists(email);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Email check completed", exists));
    }

    @Operation(summary = "Get user role statistics (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/role-stats")
    public ResponseEntity<ApiResponse<List<UserRoleStatsDTO>>> getUserRoleStats() {
        List<UserRoleStatsDTO> stats = userService.getUserRoleStats();
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "User role stats fetched successfully", stats));
    }
}
