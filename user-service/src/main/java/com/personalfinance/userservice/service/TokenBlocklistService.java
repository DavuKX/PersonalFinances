package com.personalfinance.userservice.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
public class TokenBlocklistService {

    private static final String BLOCKLIST_PREFIX = "blocklist:";

    private final StringRedisTemplate redisTemplate;

    public TokenBlocklistService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Block a token until it naturally expires.
     *
     * @param jti       the JWT ID (jti claim)
     * @param expiresAt the token expiration date — used as TTL so Redis auto-cleans the key
     */
    public void block(String jti, Date expiresAt) {
        Duration ttl = Duration.between(Instant.now(), expiresAt.toInstant());
        if (!ttl.isNegative()) {
            redisTemplate.opsForValue().set(BLOCKLIST_PREFIX + jti, "1", ttl);
        }
    }

    public boolean isBlocked(String jti) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLOCKLIST_PREFIX + jti));
    }
}
