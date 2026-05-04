package com.orchestrix.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class TokenBlacklistService {
    private static final String BLACKLIST_MARKER = "blacklisted";

    private final StringRedisTemplate redisTemplate;
    private final Duration blacklistTtl;

    public TokenBlacklistService(StringRedisTemplate redisTemplate,
                                 @Value("${token-blacklist.ttl-hours}") int ttlHours) {
        this.redisTemplate = redisTemplate;
        this.blacklistTtl = Duration.ofHours(ttlHours);
    }

    public void blacklist(String accessToken) {
        redisTemplate.opsForValue().set(
                accessToken, BLACKLIST_MARKER, blacklistTtl);
    }

    public boolean isBlacklisted(String accessToken) {
        return redisTemplate.hasKey(accessToken);
    }
}
