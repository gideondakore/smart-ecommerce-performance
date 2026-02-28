package com.amalitech.smartshop.controllers;

import com.amalitech.smartshop.config.RequiresRole;
import com.amalitech.smartshop.dtos.requests.LoginDTO;
import com.amalitech.smartshop.dtos.requests.UpdateUserDTO;
import com.amalitech.smartshop.dtos.requests.UserRegistrationDTO;
import com.amalitech.smartshop.dtos.responses.ApiResponse;
import com.amalitech.smartshop.dtos.responses.LoginResponseDTO;
import com.amalitech.smartshop.dtos.responses.PagedResponse;
import com.amalitech.smartshop.dtos.responses.UserRoleStatsDTO;
import com.amalitech.smartshop.dtos.responses.UserSummaryDTO;
import com.amalitech.smartshop.enums.UserRole;
import com.amalitech.smartshop.interfaces.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * REST controller for user management operations.
 * Handles user registration, authentication, and profile management.
 */
@Tag(name = "User Management", description = "APIs for managing users")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get all users")
    @RequiresRole(UserRole.ADMIN)
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
        ApiResponse<PagedResponse<UserSummaryDTO>> apiResponse =
                new ApiResponse<>(HttpStatus.OK.value(), "Users fetched successfully", pagedResponse);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Register a new user")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> registerUser(@Valid @RequestBody UserRegistrationDTO request) {
        LoginResponseDTO userResponseDTO = userService.addUser(request);
        ApiResponse<LoginResponseDTO> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "User registered successfully", userResponseDTO);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "User login")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> loginUser(@Valid @RequestBody LoginDTO request) {
        LoginResponseDTO user = userService.loginUser(request);
        ApiResponse<LoginResponseDTO> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "User logged in successfully", user);
        log.info("API RESPONSE: " + apiResponse);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Get authenticated user's profile")
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserSummaryDTO>> getProfile(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("authUserId");
        UserSummaryDTO user = userService.findUserById(userId);
        ApiResponse<UserSummaryDTO> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "User profile fetched successfully", user);
        return ResponseEntity.ok(apiResponse);  
    }

    @Operation(summary = "Update authenticated user's profile")
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserSummaryDTO>> updateProfile(HttpServletRequest request, @Valid @RequestBody UpdateUserDTO updateUserDTO) {
        // I update the authenticated user's profile
        Long userId = (Long) request.getAttribute("authUserId");
        UserSummaryDTO updatedUser = userService.updateUser(userId, updateUserDTO);
        ApiResponse<UserSummaryDTO> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "User profile updated successfully", updatedUser);
        return ResponseEntity.ok(apiResponse);
    }



    @Operation(summary = "Get user by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserSummaryDTO>> getUserById(@PathVariable Long id) {
        UserSummaryDTO user = userService.findUserById(id);
        ApiResponse<UserSummaryDTO> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "User fetched successfully", user);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Update user details")
    @RequiresRole(UserRole.ADMIN)
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserSummaryDTO>> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserDTO request) {
        UserSummaryDTO updatedUser = userService.updateUser(id, request);
        ApiResponse<UserSummaryDTO> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "User updated successfully", updatedUser);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Delete a user")
    @RequiresRole(UserRole.ADMIN)
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        ApiResponse<Void> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "User deleted successfully", null);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Check if email exists")
    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<Boolean>> checkEmailExists(@RequestParam String email) {
        boolean exists = userService.emailExists(email);
        ApiResponse<Boolean> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "Email check completed", exists);
        return ResponseEntity.ok(apiResponse);
    }

    @Operation(summary = "Get user role statistics")
    @RequiresRole(UserRole.ADMIN)
    @GetMapping("/role-stats")
    public ResponseEntity<ApiResponse<List<UserRoleStatsDTO>>> getUserRoleStats() {
        List<UserRoleStatsDTO> stats = userService.getUserRoleStats();
        ApiResponse<List<UserRoleStatsDTO>> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), "User role stats fetched successfully", stats);
        return ResponseEntity.ok(apiResponse);
    }
}
