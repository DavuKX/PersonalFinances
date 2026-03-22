package com.personalfinance.userservice.domain.port;

import com.personalfinance.userservice.domain.model.RefreshToken;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository {
    RefreshToken save(RefreshToken refreshToken);
    Optional<RefreshToken> findByToken(String token);
    void revokeAllByUserId(UUID userId);
}
