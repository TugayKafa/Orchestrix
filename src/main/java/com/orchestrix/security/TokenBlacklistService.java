package com.orchestrix.security;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class TokenBlacklistService {
    private static final String BLACKLIST_MARKER = "blacklisted";
    private static final Duration BLACKLIST_TTL = Duration.ofHours(1);

    private final StringRedisTemplate redisTemplate;

    public TokenBlacklistService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void blacklist(String accessToken) {
        redisTemplate.opsForValue().set(
                accessToken, BLACKLIST_MARKER, BLACKLIST_TTL);
    }

    public boolean isBlacklisted(String accessToken) {
        return redisTemplate.hasKey(accessToken);
    }
}
