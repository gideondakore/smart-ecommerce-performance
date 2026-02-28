package com.amalitech.smartshop.interfaces;

import com.amalitech.smartshop.dtos.requests.LoginDTO;
import com.amalitech.smartshop.dtos.requests.UpdateUserDTO;
import com.amalitech.smartshop.dtos.requests.UserRegistrationDTO;
import com.amalitech.smartshop.dtos.responses.LoginResponseDTO;
import com.amalitech.smartshop.dtos.responses.UserSummaryDTO;
import com.amalitech.smartshop.dtos.responses.UserRoleStatsDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for user-related business operations.
 */
public interface UserService {

    /**
     * Register a new user.
     *
     * @param userDTO the user registration data
     * @return the login response with token
     */
    LoginResponseDTO addUser(UserRegistrationDTO userDTO);

    /**
     * Authenticate a user.
     *
     * @param loginDTO the login credentials
     * @return the login response with token
     */
    LoginResponseDTO loginUser(LoginDTO loginDTO);

    /**
     * Find a user by their ID.
     *
     * @param id the user ID
     * @return the user summary
     */
    UserSummaryDTO findUserById(Long id);

    /**
     * Update a user's information.
     *
     * @param id the user ID
     * @param userDTO the update data
     * @return the updated user summary
     */
    UserSummaryDTO updateUser(Long id, UpdateUserDTO userDTO);

    /**
     * Get all users with pagination.
     *
     * @param pageable pagination information
     * @return a page of user summaries
     */
    Page<UserSummaryDTO> getAllUsers(Pageable pageable);

    /**
     * Delete a user and their associated data.
     *
     * @param id the user ID to delete
     */
    void deleteUser(Long id);

    /**
     * Check if an email is already registered.
     *
     * @param email the email to check
     * @return true if the email exists
     */
    boolean emailExists(String email);

    /**
     * Get user count grouped by role via native SQL.
     *
     * @return list of role statistics
     */
    List<UserRoleStatsDTO> getUserRoleStats();
}
