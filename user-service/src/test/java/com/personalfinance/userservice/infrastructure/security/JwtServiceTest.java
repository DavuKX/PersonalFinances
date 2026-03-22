package com.personalfinance.userservice.infrastructure.security;

import com.personalfinance.userservice.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private User user;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService("my-super-secret-key-for-dev-environment-at-least-256-bits-long!!");
        user = new User(UUID.randomUUID(), "alice", "alice@example.com", "hashed", Set.of("ROLE_USER"), OffsetDateTime.now());
    }

    @Test
    void generateAccessToken_producesValidToken() {
        String token = jwtService.generateAccessToken(user, 900_000L);

        assertNotNull(token);
        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void extractEmail_returnsCorrectEmail() {
        String token = jwtService.generateAccessToken(user, 900_000L);

        assertEquals("alice@example.com", jwtService.extractEmail(token));
    }

    @Test
    void extractJti_returnsNonBlankId() {
        String token = jwtService.generateAccessToken(user, 900_000L);

        assertNotNull(jwtService.extractJti(token));
        assertFalse(jwtService.extractJti(token).isBlank());
    }

    @Test
    void isTokenValid_returnsFalse_forTamperedToken() {
        assertFalse(jwtService.isTokenValid("not.a.valid.token"));
    }

    @Test
    void generateRefreshTokenValue_returnsUuidString() {
        String value = jwtService.generateRefreshTokenValue();

        assertNotNull(value);
        assertDoesNotThrow(() -> UUID.fromString(value));
    }
}
