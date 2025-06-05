package com.userapp.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Service
public class TokenBlacklistService {
    private final RedisTemplate<String, String> redisTemplate;
    private static final String KEY_PREFIX = "blacklisted_token:";
    private static final long TOKEN_EXPIRATION = 1; // hours

    public TokenBlacklistService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void blacklistToken(String token) {
        String key = KEY_PREFIX + token;
        redisTemplate.opsForValue().set(key, "blacklisted", TOKEN_EXPIRATION, TimeUnit.HOURS);
    }

    public boolean isBlacklisted(String token) {
        String key = KEY_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void removeFromBlacklist(String token) {
        String key = KEY_PREFIX + token;
        redisTemplate.delete(key);
    }
} 