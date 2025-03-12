package com.example.task.service;

import com.example.task.entity.User;
import com.example.task.enums.Role;
import com.example.task.exception.*;
import com.example.task.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private String validEmail;
    private String validPassword;
    private Set<Role> validRoles;

    @BeforeEach
    void setUp() {
        validEmail = "test@example.com";
        validPassword = "Password1";
        validRoles = Set.of(Role.ROLE_USER);
    }

    @Test
    void registerUser_ValidInput_ReturnsUser() {
        // Arrange
        User expectedUser = new User();
        expectedUser.setEmail(validEmail);
        expectedUser.setPassword("encodedPassword");
        expectedUser.setRoles(validRoles);

        when(userRepository.existsByEmail(validEmail)).thenReturn(false);
        when(passwordEncoder.encode(validPassword)).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(expectedUser);

        // Act
        User actualUser = userService.registerUser(validEmail, validPassword, validRoles);

        // Assert
        assertNotNull(actualUser);
        assertEquals(validEmail, actualUser.getEmail());
        assertEquals("encodedPassword", actualUser.getPassword());
        assertEquals(validRoles, actualUser.getRoles());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerUser_EmailAlreadyExists_ThrowsEmailAlreadyExistsException() {
        // Arrange
        when(userRepository.existsByEmail(validEmail)).thenReturn(true);

        // Act & Assert
        assertThrows(EmailAlreadyExistsException.class, () -> {
            userService.registerUser(validEmail, validPassword, validRoles);
        });
    }

    @Test
    void registerUser_InvalidEmail_ThrowsInvalidEmailException() {
        // Arrange
        String invalidEmail = "invalid-email";

        // Act & Assert
        assertThrows(InvalidEmailException.class, () -> {
            userService.registerUser(invalidEmail, validPassword, validRoles);
        });
    }

    @Test
    void registerUser_InvalidPassword_ThrowsInvalidPasswordException() {
        // Arrange
        String invalidPassword = "short";

        // Act & Assert
        assertThrows(InvalidPasswordException.class, () -> {
            userService.registerUser(validEmail, invalidPassword, validRoles);
        });
    }

    @Test
    void registerUser_EmptyRoles_ThrowsInvalidRoleException() {
        // Arrange
        Set<Role> emptyRoles = Set.of();

        // Act & Assert
        assertThrows(InvalidRoleException.class, () -> {
            userService.registerUser(validEmail, validPassword, emptyRoles);
        });
    }

    @Test
    void createAdmin_ValidInput_ReturnsAdminUser() {
        // Arrange
        User expectedUser = new User();
        expectedUser.setEmail(validEmail);
        expectedUser.setPassword("encodedPassword");
        expectedUser.setRoles(Set.of(Role.ROLE_ADMIN));

        when(userRepository.existsByEmail(validEmail)).thenReturn(false);
        when(passwordEncoder.encode(validPassword)).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(expectedUser);

        // Act
        User actualUser = userService.createAdmin(validEmail, validPassword);

        // Assert
        assertNotNull(actualUser);
        assertEquals(validEmail, actualUser.getEmail());
        assertEquals("encodedPassword", actualUser.getPassword());
        assertEquals(Set.of(Role.ROLE_ADMIN), actualUser.getRoles());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUser_ValidInput_ReturnsUser() {
        // Arrange
        User expectedUser = new User();
        expectedUser.setEmail(validEmail);
        expectedUser.setPassword("encodedPassword");
        expectedUser.setRoles(Set.of(Role.ROLE_USER));

        when(userRepository.existsByEmail(validEmail)).thenReturn(false);
        when(passwordEncoder.encode(validPassword)).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(expectedUser);

        // Act
        User actualUser = userService.createUser(validEmail, validPassword);

        // Assert
        assertNotNull(actualUser);
        assertEquals(validEmail, actualUser.getEmail());
        assertEquals("encodedPassword", actualUser.getPassword());
        assertEquals(Set.of(Role.ROLE_USER), actualUser.getRoles());
        verify(userRepository, times(1)).save(any(User.class));
    }
}