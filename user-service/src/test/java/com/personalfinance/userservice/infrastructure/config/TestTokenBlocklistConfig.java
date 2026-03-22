package com.personalfinance.userservice.infrastructure.config;

import com.personalfinance.userservice.domain.port.TokenBlocklistPort;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.time.Instant;

@TestConfiguration
public class TestTokenBlocklistConfig {

    @Bean
    @Primary
    public TokenBlocklistPort noOpTokenBlocklistPort() {
        return new TokenBlocklistPort() {
            @Override
            public void block(String jti, Instant expiresAt) {}

            @Override
            public boolean isBlocked(String jti) { return false; }
        };
    }
}
