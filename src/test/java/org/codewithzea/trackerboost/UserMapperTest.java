package org.codewithzea.trackerboost;


import org.codewithzea.trackerboost.user.Role;
import org.codewithzea.trackerboost.user.UserEntity;
import org.codewithzea.trackerboost.user.dto.UserDTO;
import org.codewithzea.trackerboost.user.dto.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class UserMapperTest {

    private final UserMapper userMapper = new UserMapper();

    @Test
    void toDTO_ShouldMapAllFieldsCorrectly() {
        // Arrange
        UserEntity user = UserEntity.builder()
                .id(2L)
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@example.com")
                .password("password123")
                .role(Role.ROLE_MANAGER)
                .build();

        // Act
        UserDTO result = userMapper.toDTO(user);

        // Assert
        assertAll(
                () -> assertEquals(2L, result.getId()),
                () -> assertEquals("Jane", result.getFirstName()),
                () -> assertEquals("Smith", result.getLastName()),
                () -> assertEquals("jane.smith@example.com", result.getEmail()),
                () -> assertEquals(Role.ROLE_MANAGER, result.getRole())
        );
    }
}
