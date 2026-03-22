package com.personalfinance.userservice.infrastructure.redis;

import com.personalfinance.userservice.domain.port.TokenBlocklistPort;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
public class TokenBlocklistAdapter implements TokenBlocklistPort {

    private static final String BLOCKLIST_PREFIX = "blocklist:";

    private final StringRedisTemplate redisTemplate;

    public TokenBlocklistAdapter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void block(String jti, Instant expiresAt) {
        Duration ttl = Duration.between(Instant.now(), expiresAt);
        if (!ttl.isNegative()) {
            redisTemplate.opsForValue().set(BLOCKLIST_PREFIX + jti, "1", ttl);
        }
    }

    @Override
    public boolean isBlocked(String jti) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLOCKLIST_PREFIX + jti));
    }
}
