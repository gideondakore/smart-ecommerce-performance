package com.amalitech.smartshop.services;

import com.amalitech.smartshop.cache.CacheManager;
import com.amalitech.smartshop.dtos.requests.LoginDTO;
import com.amalitech.smartshop.dtos.requests.UpdateUserDTO;
import com.amalitech.smartshop.dtos.requests.UserRegistrationDTO;
import com.amalitech.smartshop.dtos.responses.LoginResponseDTO;
import com.amalitech.smartshop.dtos.responses.UserSummaryDTO;
import com.amalitech.smartshop.entities.Order;
import com.amalitech.smartshop.entities.User;
import com.amalitech.smartshop.exceptions.ResourceAlreadyExistsException;
import com.amalitech.smartshop.exceptions.ResourceNotFoundException;
import com.amalitech.smartshop.interfaces.UserService;
import com.amalitech.smartshop.interfaces.SessionService;
import com.amalitech.smartshop.mappers.UserMapper;
import com.amalitech.smartshop.repositories.jpa.OrderItemJpaRepository;
import com.amalitech.smartshop.repositories.jpa.OrderJpaRepository;
import com.amalitech.smartshop.repositories.jpa.UserJpaRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementation of the UserService interface.
 * Handles all user-related business logic including registration,
 * authentication, and user management.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final UserJpaRepository userRepository;
    private final UserMapper userMapper;
    private final CacheManager cacheManager;
    private final OrderJpaRepository orderRepository;
    private final OrderItemJpaRepository orderItemRepository;

    private final SessionService sessionService;

    @Override
    public LoginResponseDTO addUser(UserRegistrationDTO userDTO) {
        log.info("Registering new user with email: {}", userDTO.getEmail());
        
        userRepository.findByEmail(userDTO.getEmail())
                .ifPresent(user -> {
                    throw new ResourceAlreadyExistsException("Email already exists: " + userDTO.getEmail());
                });

        User user = userMapper.toEntity(userDTO);
        user.setFirstName(capitalize(user.getFirstName()));
        user.setLastName(capitalize(user.getLastName()));
        user.setEmail(user.getEmail().toLowerCase());
        user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));

        User savedUser = userRepository.save(user);
        LoginResponseDTO responseDTO = userMapper.toResponseDTO(savedUser);
        responseDTO.setToken(sessionService.createSession(savedUser.getId()));
        
        log.info("User registered successfully with id: {}", savedUser.getId());
        return responseDTO;
    }

    @Override
    public LoginResponseDTO loginUser(LoginDTO loginDTO) {
        log.info("Attempting login for email: {}", loginDTO.getEmail());
        
        User user = userRepository.findByEmail(loginDTO.getEmail().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + loginDTO.getEmail()));

        if (!BCrypt.checkpw(loginDTO.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid password");
        }

        LoginResponseDTO responseDTO = userMapper.toResponseDTO(user);
        responseDTO.setToken(sessionService.createSession(user.getId()));
        
        log.info("User logged in successfully: {}", user.getId());
        return responseDTO;
    }

    @Override
    public UserSummaryDTO findUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        return userMapper.toSummaryDTO(user);
    }

    @Override
    public UserSummaryDTO updateUser(Long id, @Valid UpdateUserDTO userDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        String oldEmail = user.getEmail();
        userMapper.updateEntity(userDTO, user);


        if (user.getFirstName() != null) user.setFirstName(capitalize(user.getFirstName()));
        if (user.getLastName() != null) user.setLastName(capitalize(user.getLastName()));
        if (user.getEmail() != null) user.setEmail(user.getEmail().toLowerCase());

        User updatedUser = userRepository.save(user);

        invalidateUserCache(id, oldEmail, userDTO.getEmail());

        return userMapper.toSummaryDTO(updatedUser);
    }

    @Override
    public Page<UserSummaryDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(user ->
                cacheManager.get("usr:" + user.getId(), () -> userMapper.toSummaryDTO(user))
        );
    }

    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        String email = user.getEmail();

        List<Order> orders = orderRepository.findByUserId(id);
        for (Order order : orders) {
            orderItemRepository.deleteAll(orderItemRepository.findByOrderId(order.getId()));
            orderRepository.delete(order);
            cacheManager.invalidate("ord:" + order.getId());
        }

        userRepository.delete(user);
        cacheManager.invalidate("usr:" + id);
        cacheManager.invalidate("usr:" + email);
        
        log.info("User deleted successfully: {}", id);
    }



    private void invalidateUserCache(Long id, String oldEmail, String newEmail) {
        cacheManager.invalidate("usr:" + id);
        cacheManager.invalidate("usr:" + oldEmail);
        if (newEmail != null && !oldEmail.equals(newEmail)) {
            cacheManager.invalidate("usr:" + newEmail);
        }
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
