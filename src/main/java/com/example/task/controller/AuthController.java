package com.example.task.controller;

import com.example.task.dto.AuthRequest;
import com.example.task.dto.AuthResponse;
import com.example.task.dto.RegisterRequest;
import com.example.task.entity.User;
import com.example.task.service.JwtUtil;
import com.example.task.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping(AuthController.API_AUTH_PATH)
@RequiredArgsConstructor
public class AuthController {
    public static final String API_AUTH_PATH = "/api/auth";
    private static final String LOGIN_PATH = "/login";
    private static final String REGISTER_USER_PATH = "/register/user";
    private static final String REGISTER_ADMIN_PATH = "/register/admin";

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    @PostMapping(LOGIN_PATH)
    public ResponseEntity<AuthResponse> login(@Valid @org.springframework.web.bind.annotation.RequestBody AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtUtil.generateToken(userDetails);
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping(REGISTER_USER_PATH)
    public ResponseEntity<User> registerUser(@Valid @org.springframework.web.bind.annotation.RequestBody RegisterRequest request) {
        User user = userService.createUser(request.getEmail(), request.getPassword());
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PostMapping(REGISTER_ADMIN_PATH)
    public ResponseEntity<User> registerAdmin(@Valid @org.springframework.web.bind.annotation.RequestBody RegisterRequest request) {
        User user = userService.createAdmin(request.getEmail(), request.getPassword());
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
}