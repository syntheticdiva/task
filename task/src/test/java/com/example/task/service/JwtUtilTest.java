package com.example.task.service;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtUtilTest {

    @InjectMocks
    private JwtUtil jwtUtil;

    @Mock
    private UserDetails userDetails;

    private static final String SECRET_KEY = "this-is-a-very-secure-secret-key-1234567890";
    private static final String USERNAME = "testuser";

    @BeforeEach
    void setUp() throws Exception {
        // Устанавливаем значение поля secretString через рефлексию
        Field secretStringField = JwtUtil.class.getDeclaredField("secretString");
        secretStringField.setAccessible(true);
        secretStringField.set(jwtUtil, SECRET_KEY);
    }

    @Test
    void generateToken_ValidUserDetails_ReturnsToken() {
        // Arrange
        when(userDetails.getUsername()).thenReturn(USERNAME);

        // Act
        String token = jwtUtil.generateToken(userDetails);

        // Assert
        assertNotNull(token);
        verify(userDetails, times(1)).getUsername();
    }

    @Test
    void extractUsername_ValidToken_ReturnsUsername() {
        // Arrange
        String token = Jwts.builder()
                .subject(USERNAME)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
                .compact();

        // Act
        String username = jwtUtil.extractUsername(token);

        // Assert
        assertEquals(USERNAME, username);
    }

    @Test
    void validateToken_ValidTokenAndUserDetails_ReturnsTrue() {
        // Arrange
        when(userDetails.getUsername()).thenReturn(USERNAME);
        String token = Jwts.builder()
                .subject(USERNAME)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
                .compact();

        // Act
        boolean isValid = jwtUtil.validateToken(token, userDetails);

        // Assert
        assertTrue(isValid);
        verify(userDetails, times(1)).getUsername();
    }

    @Test
    void validateToken_InvalidUsername_ReturnsFalse() {
        // Arrange
        when(userDetails.getUsername()).thenReturn("anotheruser");
        String token = Jwts.builder()
                .subject(USERNAME)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
                .compact();

        // Act
        boolean isValid = jwtUtil.validateToken(token, userDetails);

        // Assert
        assertFalse(isValid);
        verify(userDetails, times(1)).getUsername();
    }

    @Test
    void extractAllClaims_ValidToken_ReturnsClaims() {
        // Arrange
        String token = Jwts.builder()
                .subject(USERNAME)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
                .compact();

        // Act
        Claims claims = jwtUtil.extractAllClaims(token);

        // Assert
        assertNotNull(claims);
        assertEquals(USERNAME, claims.getSubject());
    }

}