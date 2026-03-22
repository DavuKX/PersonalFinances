package com.personalfinance.userservice.domain.port;

import com.personalfinance.userservice.domain.model.User;

public interface TokenProvider {
    String generateAccessToken(User user, long ttlMs);
    String generateRefreshTokenValue();
}
