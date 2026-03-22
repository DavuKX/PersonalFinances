package com.personalfinance.userservice.application.service;

import com.personalfinance.userservice.application.dto.LoginCommand;
import com.personalfinance.userservice.application.dto.TokenPair;
import com.personalfinance.userservice.domain.exception.InvalidCredentialsException;
import com.personalfinance.userservice.domain.exception.TokenExpiredException;
import com.personalfinance.userservice.domain.exception.TokenRevokedException;
import com.personalfinance.userservice.domain.model.RefreshToken;
import com.personalfinance.userservice.domain.model.User;
import com.personalfinance.userservice.domain.port.PasswordHasher;
import com.personalfinance.userservice.domain.port.RefreshTokenRepository;
import com.personalfinance.userservice.domain.port.TokenBlocklistPort;
import com.personalfinance.userservice.domain.port.TokenProvider;
import com.personalfinance.userservice.domain.port.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthApplicationServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private TokenBlocklistPort tokenBlocklistPort;
    @Mock private TokenProvider tokenProvider;
    @Mock private PasswordHasher passwordHasher;

    private AuthApplicationService authService;
    private User user;

    @BeforeEach
    void setUp() {
        authService = new AuthApplicationService(
                userRepository, refreshTokenRepository, tokenBlocklistPort,
                tokenProvider, passwordHasher, 900_000L, 604_800_000L);

        user = new User(UUID.randomUUID(), "alice", "alice@example.com", "hashed", Set.of("ROLE_USER"), OffsetDateTime.now());
    }

    @Test
    void login_returnsTokenPair_whenCredentialsAreValid() {
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(passwordHasher.matches("pass", "hashed")).thenReturn(true);
        when(tokenProvider.generateAccessToken(eq(user), eq(900_000L))).thenReturn("access-token");
        when(tokenProvider.generateRefreshTokenValue()).thenReturn("refresh-value");
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TokenPair pair = authService.login(new LoginCommand("alice@example.com", "pass"));

        assertEquals("access-token", pair.accessToken());
        assertEquals("refresh-value", pair.refreshToken());
        assertEquals(900L, pair.expiresInSeconds());
    }

    @Test
    void login_throws_whenEmailNotFound() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class,
                () -> authService.login(new LoginCommand("unknown@example.com", "pass")));
    }

    @Test
    void login_throws_whenPasswordIsWrong() {
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(passwordHasher.matches("wrong", "hashed")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
                () -> authService.login(new LoginCommand("alice@example.com", "wrong")));
    }

    @Test
    void refresh_returnsNewTokenPair_whenTokenIsValid() {
        RefreshToken token = new RefreshToken(UUID.randomUUID(), "refresh-value", user.getId(),
                Instant.now().plusSeconds(3600), false);

        when(refreshTokenRepository.findByToken("refresh-value")).thenReturn(Optional.of(token));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(tokenProvider.generateAccessToken(eq(user), eq(900_000L))).thenReturn("new-access-token");
        when(tokenProvider.generateRefreshTokenValue()).thenReturn("new-refresh-value");
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TokenPair pair = authService.refresh("refresh-value");

        assertEquals("new-access-token", pair.accessToken());
        assertEquals("new-refresh-value", pair.refreshToken());
    }

    @Test
    void refresh_throws_whenTokenIsRevoked() {
        RefreshToken token = new RefreshToken(UUID.randomUUID(), "refresh-value", user.getId(),
                Instant.now().plusSeconds(3600), true);

        when(refreshTokenRepository.findByToken("refresh-value")).thenReturn(Optional.of(token));

        assertThrows(TokenRevokedException.class, () -> authService.refresh("refresh-value"));
    }

    @Test
    void refresh_throws_whenTokenIsExpired() {
        RefreshToken token = new RefreshToken(UUID.randomUUID(), "refresh-value", user.getId(),
                Instant.now().minusSeconds(1), false);

        when(refreshTokenRepository.findByToken("refresh-value")).thenReturn(Optional.of(token));

        assertThrows(TokenExpiredException.class, () -> authService.refresh("refresh-value"));
    }

    @Test
    void logout_blocksJtiAndRevokesTokens() {
        UUID userId = user.getId();

        authService.logout(userId, "some-jti");

        verify(tokenBlocklistPort).block(eq("some-jti"), any(Instant.class));
        verify(refreshTokenRepository).revokeAllByUserId(userId);
    }
}
