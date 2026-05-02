package com.orchestrix.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TokenBlacklistServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    ValueOperations<String, String> valueOperations;

    @InjectMocks
    private TokenBlacklistService tokenBlacklistService;

    @Test
    void testBlacklistStoresTokenInRedis() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        tokenBlacklistService.blacklist("token");
        verify(valueOperations).set(eq("token"), eq("blacklisted"), any(Duration.class));
    }

    @Test
    void testIsBlacklistedReturnsTrueIfTokenIsBlacklisted() {
        when(redisTemplate.hasKey("token")).thenReturn(true);

        assertTrue(
                tokenBlacklistService.isBlacklisted("token"),
                "Expected true to be returned if token was blacklisted."
        );
    }

    @Test
    void testIsBlacklistedReturnsFalseIfTokenIsNotBlacklisted() {
        when(redisTemplate.hasKey("token")).thenReturn(false);

        assertFalse(
                tokenBlacklistService.isBlacklisted("token"),
                "Expected false to be returned if token was not blacklisted."
        );
    }
}
