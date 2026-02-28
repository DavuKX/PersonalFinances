package com.personalfinance.userservice.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(
                "test-secret-key-for-unit-tests-must-be-at-least-256-bits-long!!",
                86400000L
        );
    }

    @Test
    void generateToken_shouldReturnNonNullToken() {
        String token = jwtService.generateToken("alice@example.com", Map.of("roles", "ROLE_USER"));
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void extractEmail_shouldReturnCorrectEmail() {
        String token = jwtService.generateToken("alice@example.com", Map.of());
        assertEquals("alice@example.com", jwtService.extractEmail(token));
    }

    @Test
    void isTokenValid_shouldReturnTrueForValidToken() {
        String token = jwtService.generateToken("alice@example.com", Map.of());
        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void isTokenValid_shouldReturnFalseForTamperedToken() {
        String token = jwtService.generateToken("alice@example.com", Map.of());
        assertFalse(jwtService.isTokenValid(token + "tampered"));
    }

    @Test
    void isTokenValid_shouldReturnFalseForExpiredToken() {
        JwtService expiredService = new JwtService(
                "test-secret-key-for-unit-tests-must-be-at-least-256-bits-long!!",
                -1000L
        );
        String token = expiredService.generateToken("alice@example.com", Map.of());
        assertFalse(jwtService.isTokenValid(token));
    }

    @Test
    void isTokenValid_shouldReturnFalseForGarbageString() {
        assertFalse(jwtService.isTokenValid("not.a.jwt"));
    }
}
