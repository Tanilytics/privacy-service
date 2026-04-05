package com.tanalytics.privacy.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
public class RedisCleanupService {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisCleanupService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void cleanupVisitorData(UUID siteId, String visitorId) {
        String sessionKey = "session:%s:%s".formatted(siteId, visitorId);
        redisTemplate.delete(sessionKey);

        Set<String> matchingKeys = redisTemplate.keys("rt:active:%s*".formatted(siteId));
        if (matchingKeys != null && !matchingKeys.isEmpty()) {
            redisTemplate.delete(matchingKeys);
        }
    }
}
