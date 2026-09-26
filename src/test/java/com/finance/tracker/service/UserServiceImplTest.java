package com.finance.tracker.service;

import com.finance.tracker.dto.UpdateUserRequestDTO;
import com.finance.tracker.dto.UserResponseDTO;
import com.finance.tracker.model.User;
import com.finance.tracker.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void shouldReturnUserProfile() {

        // Arrange
        User user = new User();
        user.setUserId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john@gmail.com");
        user.setPhoneNumber("08012345678");

        // Act
        UserResponseDTO result = userService.getUserProfile(user);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("john@gmail.com", result.getEmail());
        assertEquals("08012345678", result.getPhoneNumber());
    }

    @Test
    void shouldUpdateUserProfile() {

        // Arrange
        User user = new User();
        user.setUserId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john@gmail.com");
        user.setPhoneNumber("08000000000");

        UpdateUserRequestDTO dto = new UpdateUserRequestDTO(
                "James",
                "Smith",
                "08111111111"
        );

        when(userRepository.save(user)).thenReturn(user);

        // Act
        UserResponseDTO result =
                userService.updateUserProfile(user, dto);

        // Assert
        assertEquals("James", result.getFirstName());
        assertEquals("Smith", result.getLastName());
        assertEquals("08111111111", result.getPhoneNumber());

        verify(userRepository).save(user);
    }

    @Test
    void shouldDeleteUser() {

        // Arrange
        User user = new User();
        user.setUserId(1L);

        // Act
        userService.deleteUser(user);

        // Assert
        verify(userRepository).delete(user);
    }
}