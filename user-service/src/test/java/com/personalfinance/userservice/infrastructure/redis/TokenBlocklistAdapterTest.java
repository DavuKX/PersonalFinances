package com.personalfinance.userservice.infrastructure.redis;

import com.personalfinance.userservice.domain.port.TokenBlocklistPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenBlocklistAdapterTest {

    @Mock private StringRedisTemplate redisTemplate;
    @Mock private ValueOperations<String, String> valueOps;

    private TokenBlocklistPort adapter;

    @BeforeEach
    void setUp() {
        adapter = new TokenBlocklistAdapter(redisTemplate);
    }

    @Test
    void block_setsKeyInRedis_whenTtlIsPositive() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        adapter.block("jti-123", Instant.now().plusSeconds(60));

        verify(valueOps).set(eq("blocklist:jti-123"), eq("1"), any());
    }

    @Test
    void block_doesNotSetKey_whenTtlIsNegative() {
        adapter.block("jti-123", Instant.now().minusSeconds(60));

        verify(valueOps, never()).set(any(), any(), any());
    }

    @Test
    void isBlocked_returnsTrue_whenKeyExists() {
        when(redisTemplate.hasKey("blocklist:jti-123")).thenReturn(true);

        assertTrue(adapter.isBlocked("jti-123"));
    }

    @Test
    void isBlocked_returnsFalse_whenKeyDoesNotExist() {
        when(redisTemplate.hasKey("blocklist:jti-000")).thenReturn(false);

        assertFalse(adapter.isBlocked("jti-000"));
    }
}
