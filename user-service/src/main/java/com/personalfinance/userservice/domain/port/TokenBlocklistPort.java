package com.personalfinance.userservice.domain.port;

import java.time.Instant;

public interface TokenBlocklistPort {
    void block(String jti, Instant expiresAt);
    boolean isBlocked(String jti);
}
