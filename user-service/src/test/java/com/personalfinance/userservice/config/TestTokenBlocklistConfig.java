package com.personalfinance.userservice.config;

import com.personalfinance.userservice.service.TokenBlocklistService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.Date;

/**
 * Replaces TokenBlocklistService with a no-op during tests
 * (Redis is excluded from the test context).
 */
@TestConfiguration
public class TestTokenBlocklistConfig {

    @Bean
    @Primary
    public TokenBlocklistService noOpTokenBlocklistService() {
        return new TokenBlocklistService(null) {
            @Override
            public void block(String jti, Date expiresAt) { /* no-op */ }

            @Override
            public boolean isBlocked(String jti) { return false; }
        };
    }
}
