package com.personalfinance.userservice.domain.model;

import java.time.Instant;
import java.util.UUID;

public class RefreshToken {

    private final UUID id;
    private final String token;
    private final UUID userId;
    private final Instant expiresAt;
    private boolean revoked;

    public RefreshToken(UUID id, String token, UUID userId, Instant expiresAt, boolean revoked) {
        this.id = id;
        this.token = token;
        this.userId = userId;
        this.expiresAt = expiresAt;
        this.revoked = revoked;
    }

    public boolean isExpired() {
        return expiresAt.isBefore(Instant.now());
    }

    public void revoke() {
        this.revoked = true;
    }

    public UUID getId() { return id; }
    public String getToken() { return token; }
    public UUID getUserId() { return userId; }
    public Instant getExpiresAt() { return expiresAt; }
    public boolean isRevoked() { return revoked; }
}
