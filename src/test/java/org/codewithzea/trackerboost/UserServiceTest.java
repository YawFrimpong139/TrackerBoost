package org.codewithzea.trackerboost;


import org.codewithzea.trackerboost.user.Role;
import org.codewithzea.trackerboost.user.UserEntity;
import org.codewithzea.trackerboost.user.UserRepository;
import org.codewithzea.trackerboost.user.UserService;
import org.codewithzea.trackerboost.user.dto.UserDTO;
import org.codewithzea.trackerboost.user.dto.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Mock
    private UserMapper userMapper;

    private UserEntity testUser;
    private UserEntity savedUser;
    private UserDTO expectedDto;

    @BeforeEach
    void setUp() {
        testUser = UserEntity.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password("plainPassword")
                .role(Role.ROLE_DEVELOPER)
                .build();

        savedUser = UserEntity.builder()
                .id(1L)
                .firstName(testUser.getFirstName())
                .lastName(testUser.getLastName())
                .email(testUser.getEmail())
                .password("encodedPassword")
                .role(testUser.getRole())
                .build();

        expectedDto = UserDTO.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .role(Role.ROLE_DEVELOPER)
                .build();
    }

    @Test
    void registerUser_ShouldEncodePasswordAndSaveUser() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword");
        when(userRepository.save(any(UserEntity.class))).thenReturn(savedUser);
        when(userMapper.toDTO(savedUser)).thenReturn(expectedDto);

        // Act
        UserDTO result = userService.registerUser(testUser);

        // Assert
        assertNotNull(result);
        assertEquals(expectedDto, result);

        verify(userRepository).existsByEmail("john.doe@example.com");
        verify(passwordEncoder).encode("plainPassword");
        verify(userRepository).save(any(UserEntity.class));
        verify(userMapper).toDTO(savedUser);
    }

    @Test
    void registerUser_ShouldThrowWhenEmailExists() {
        // Arrange
        when(userRepository.existsByEmail("john.doe@example.com")).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> userService.registerUser(testUser),
                "Should throw when email exists");

        verify(userRepository, never()).save(any());

    }

    @Test
    void registerUser_ShouldHandleAllRoleTypes() {
        // Test for each role type
        for (Role role : Role.values()) {
            // Reset mocks for each iteration
            reset(userRepository, passwordEncoder, userMapper);

            // Arrange
            testUser.setRole(role);
            savedUser.setRole(role);

            UserDTO roleDto = UserDTO.builder()
                    .role(role)
                    .build();

            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(passwordEncoder.encode(testUser.getPassword())).thenReturn("encodedPassword");
            when(userRepository.save(any(UserEntity.class))).thenReturn(savedUser);
            when(userMapper.toDTO(savedUser)).thenReturn(roleDto);

            // Act
            UserDTO result = userService.registerUser(testUser);

            // Assert
            assertEquals(role, result.getRole(),
                    "Should handle role: " + role.name());
        }
    }


    @Test
    void registerUser_ShouldNeverStorePlainTextPassword() {
        // Arrange
        when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword");
        when(userRepository.save(any(UserEntity.class))).thenReturn(savedUser);

        // Act
        userService.registerUser(testUser);

        // Assert
        verify(userRepository).save(argThat(user ->
                !user.getPassword().equals("plainPassword") &&
                        user.getPassword().equals("encodedPassword")
        ));
    }

    @Test
    void registerUser_ShouldThrowWhenUserIsNull() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> userService.registerUser(null),
                "Should throw when user is null");

        assertEquals("User cannot be null", exception.getMessage());
    }
}
