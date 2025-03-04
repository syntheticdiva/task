package com.example.task.service;

import com.example.task.entity.User;
import com.example.task.enums.Role;
import com.example.task.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User registerUser(String email, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(Set.of(Role.ROLE_USER));

        return userRepository.save(user);
    }

    public User createAdmin(String email, String password) {
        User user = registerUser(email, password);
        user.getRoles().add(Role.ROLE_ADMIN);
        return userRepository.save(user);
    }
}