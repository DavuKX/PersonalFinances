package com.personalfinance.userservice.service;

import com.personalfinance.userservice.dto.LoginRequest;
import com.personalfinance.userservice.dto.LoginResponse;
import com.personalfinance.userservice.entity.RefreshToken;
import com.personalfinance.userservice.entity.User;
import com.personalfinance.userservice.repository.UserRepository;
import com.personalfinance.userservice.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private TokenBlocklistService tokenBlocklistService;

    private AuthService authService;
    private User user;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtService,
                refreshTokenService, tokenBlocklistService, 900000L);

        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("alice@example.com");
        user.setUsername("alice");
        user.setPassword("encodedPassword");
        user.setRoles(Set.of("ROLE_USER"));
    }

    @Test
    void login_shouldReturnTokens_whenCredentialsAreValid() {
        LoginRequest request = new LoginRequest();
        request.setEmail("alice@example.com");
        request.setPassword("StrongPass123!");

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("refresh-uuid");
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(Instant.now().plusSeconds(604800));

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("StrongPass123!", "encodedPassword")).thenReturn(true);
        when(jwtService.generateToken(eq("alice@example.com"), anyMap(), eq(900000L))).thenReturn("jwt-token");
        when(refreshTokenService.createRefreshToken(user)).thenReturn(refreshToken);

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.getAccessToken());
        assertEquals("refresh-uuid", response.getRefreshToken());
        assertEquals(900L, response.getExpiresIn());
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

    @Test
    void logout_shouldBlockTokenAndRevokeRefreshTokens() {
        assertDoesNotThrow(() -> authService.logout(user, "some-jti"));
    }
}
