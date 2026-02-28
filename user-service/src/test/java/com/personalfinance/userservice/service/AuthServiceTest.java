package com.personalfinance.userservice.service;

import com.personalfinance.userservice.dto.LoginRequest;
import com.personalfinance.userservice.entity.User;
import com.personalfinance.userservice.repository.UserRepository;
import com.personalfinance.userservice.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("alice@example.com");
        user.setUsername("alice");
        user.setPassword("encodedPassword");
        user.setRoles(Set.of("ROLE_USER"));
    }

    @Test
    void login_shouldReturnToken_whenCredentialsAreValid() {
        LoginRequest request = new LoginRequest();
        request.setEmail("alice@example.com");
        request.setPassword("StrongPass123!");

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("StrongPass123!", "encodedPassword")).thenReturn(true);
        when(jwtService.generateToken(eq("alice@example.com"), anyMap())).thenReturn("jwt-token");

        String token = authService.login(request);
        assertEquals("jwt-token", token);
    }

    @Test
    void login_shouldThrow_whenEmailNotFound() {
        LoginRequest request = new LoginRequest();
        request.setEmail("unknown@example.com");
        request.setPassword("pass");

        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.login(request));
        assertEquals("Invalid email or password", ex.getMessage());
    }

    @Test
    void login_shouldThrow_whenPasswordIsWrong() {
        LoginRequest request = new LoginRequest();
        request.setEmail("alice@example.com");
        request.setPassword("wrongPassword");

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.login(request));
        assertEquals("Invalid email or password", ex.getMessage());
    }
}
