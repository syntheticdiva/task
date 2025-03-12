package com.example.task.service;

import com.example.task.entity.User;
import com.example.task.enums.Role;
import com.example.task.exception.EmailAlreadyExistsException;
import com.example.task.exception.InvalidEmailException;
import com.example.task.exception.InvalidPasswordException;
import com.example.task.exception.InvalidRoleException;
import com.example.task.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Pattern EMAIL_REGEX =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final int MIN_PASSWORD_LENGTH = 8;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User registerUser(String email, String password, Set<Role> roles) {
        validateEmail(email);
        validatePassword(password);
        validateRoles(roles);

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email already exists: " + email);
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(roles);

        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            throw new EmailAlreadyExistsException("Email already exists: " + email);
        }
    }

    public User createAdmin(String email, String password) {
        return registerUser(email, password, Set.of(Role.ROLE_ADMIN));
    }

    public User createUser(String email, String password) {
        return registerUser(email, password, Set.of(Role.ROLE_USER));
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new InvalidEmailException("Email cannot be empty");
        }

        if (!EMAIL_REGEX.matcher(email).matches()) {
            throw new InvalidEmailException("Invalid email format: " + email);
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new InvalidPasswordException("Password cannot be empty");
        }

        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new InvalidPasswordException("Password must be at least "
                    + MIN_PASSWORD_LENGTH + " characters");
        }

        if (!password.matches(".*\\d.*")) {
            throw new InvalidPasswordException("Password must contain at least one digit");
        }

        if (!password.matches(".*[A-Za-z].*")) {
            throw new InvalidPasswordException("Password must contain at least one letter");
        }
    }

    private void validateRoles(Set<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            throw new InvalidRoleException("User must have at least one role");
        }
    }
}