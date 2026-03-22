package com.personalfinance.userservice.domain.model;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

public class User {

    private final UUID id;
    private final String username;
    private final String email;
    private final String hashedPassword;
    private final Set<String> roles;
    private final OffsetDateTime createdAt;

    public User(UUID id, String username, String email, String hashedPassword, Set<String> roles, OffsetDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.hashedPassword = hashedPassword;
        this.roles = roles;
        this.createdAt = createdAt;
    }

    public static User register(String username, String email, String hashedPassword) {
        return new User(UUID.randomUUID(), username, email, hashedPassword, Set.of("ROLE_USER"), OffsetDateTime.now());
    }

    public UUID getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getHashedPassword() { return hashedPassword; }
    public Set<String> getRoles() { return roles; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
