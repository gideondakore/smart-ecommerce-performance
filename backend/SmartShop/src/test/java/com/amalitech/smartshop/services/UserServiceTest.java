package com.amalitech.smartshop.services;

import com.amalitech.smartshop.dtos.requests.LoginDTO;
import com.amalitech.smartshop.dtos.requests.UpdateUserDTO;
import com.amalitech.smartshop.dtos.requests.UserRegistrationDTO;
import com.amalitech.smartshop.dtos.responses.LoginResponseDTO;
import com.amalitech.smartshop.dtos.responses.UserSummaryDTO;
import com.amalitech.smartshop.entities.User;
import com.amalitech.smartshop.exceptions.ResourceAlreadyExistsException;
import com.amalitech.smartshop.exceptions.ResourceNotFoundException;
import com.amalitech.smartshop.mappers.UserMapper;
import com.amalitech.smartshop.repositories.jpa.UserJpaRepository;
import com.amalitech.smartshop.repositories.jpa.OrderJpaRepository;
import com.amalitech.smartshop.interfaces.SessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserServiceImpl userService;

    @Mock
    private UserJpaRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private OrderJpaRepository orderRepository;

    @Mock
    private SessionService sessionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserServiceImpl(userRepository, userMapper, orderRepository, sessionService);
    }

    @Test
    void addUser_Success() {
        UserRegistrationDTO dto = new UserRegistrationDTO();
        dto.setEmail("test@example.com");
        dto.setPassword("password123");
        dto.setFirstName("John");
        dto.setLastName("Doe");
        
        User entity = new User();
        entity.setPassword("password123");
        entity.setEmail("test@example.com");
        
        User savedEntity = new User();
        savedEntity.setId(1L);
        
        LoginResponseDTO responseDTO = new LoginResponseDTO();
        responseDTO.setId(1L);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(userMapper.toEntity(dto)).thenReturn(entity);
        when(userRepository.save(any(User.class))).thenReturn(savedEntity);
        when(userMapper.toResponseDTO(savedEntity)).thenReturn(responseDTO);
        when(sessionService.createSession(1L)).thenReturn("test-token");

        LoginResponseDTO result = userService.addUser(dto);

        assertNotNull(result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void addUser_EmailAlreadyExists() {
        UserRegistrationDTO dto = new UserRegistrationDTO();
        dto.setEmail("test@example.com");
        
        User existingUser = new User();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(existingUser));

        assertThrows(ResourceAlreadyExistsException.class, () -> userService.addUser(dto));
    }

    @Test
    void loginUser_Success() {
        String email = "test@example.com";
        String password = "password123";
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        User user = new User();
        user.setId(1L);
        user.setEmail(email);
        user.setPassword(hashedPassword);

        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail(email);
        loginDTO.setPassword(password);

        LoginResponseDTO expectedResponse = new LoginResponseDTO();
        expectedResponse.setId(1L);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(userMapper.toResponseDTO(user)).thenReturn(expectedResponse);
        when(sessionService.createSession(1L)).thenReturn("test-token");

        LoginResponseDTO actualResponse = userService.loginUser(loginDTO);

        assertNotNull(actualResponse);
        assertNotNull(actualResponse.getToken());
    }

    @Test
    void loginUser_InvalidPassword() {
        String email = "test@example.com";
        String password = "password123";
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        User user = new User();
        user.setPassword(hashedPassword);

        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail(email);
        loginDTO.setPassword("wrong");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class, () -> userService.loginUser(loginDTO));
    }

    @Test
    void loginUser_UserNotFound() {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail("notfound@example.com");

        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.loginUser(loginDTO));
    }

    @Test
    void findUserById_Success() {
        User entity = new User();
        entity.setId(1L);
        
        UserSummaryDTO summaryDTO = new UserSummaryDTO();
        summaryDTO.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(userMapper.toSummaryDTO(entity)).thenReturn(summaryDTO);

        UserSummaryDTO result = userService.findUserById(1L);

        assertNotNull(result);
    }

    @Test
    void findUserById_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.findUserById(1L));
    }

    @Test
    void updateUser_Success() {
        UpdateUserDTO updateDTO = new UpdateUserDTO();
        updateDTO.setFirstName("Updated");
        
        User existingEntity = new User();
        existingEntity.setId(1L);
        
        User updatedEntity = new User();
        updatedEntity.setId(1L);
        
        UserSummaryDTO summaryDTO = new UserSummaryDTO();
        summaryDTO.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingEntity));
        when(userRepository.save(existingEntity)).thenReturn(updatedEntity);
        when(userMapper.toSummaryDTO(updatedEntity)).thenReturn(summaryDTO);

        UserSummaryDTO result = userService.updateUser(1L, updateDTO);

        assertNotNull(result);
        verify(userMapper).updateEntity(updateDTO, existingEntity);
    }

    @Test
    void deleteUser_Success() {
        User entity = new User();
        entity.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(orderRepository.findByUser_Id(1L)).thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> userService.deleteUser(1L));
        verify(userRepository).delete(entity);
    }
}
