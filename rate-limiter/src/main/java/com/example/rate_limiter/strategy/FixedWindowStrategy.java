package com.example.rate_limiter.strategy;

import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;
import org.springframework.data.redis.core.RedisTemplate;

@Component
public class FixedWindowStrategy implements RateLimitStrategy {
    private final RedisTemplate<String, String> redisTemplate; // stored as plain strings

    public FixedWindowStrategy(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean isAllowed(String identifier, int maxRequests, int windowSeconds) {
        String key = getKey(identifier, windowSeconds);
        Long count = redisTemplate.opsForValue().increment(key);
        
        if (count == 1) {
            redisTemplate.expire(key, windowSeconds, TimeUnit.SECONDS);
        }
        
        return count <= maxRequests;
    }
    
    @Override
    public long getRemainingRequests(String identifier, int maxRequests, int windowSeconds) {
        String key = getKey(identifier, windowSeconds);
        String countStr = redisTemplate.opsForValue().get(key);
        
        if (countStr == null) {
            return maxRequests; // No requests yet
        }
        
        long currentCount = Long.parseLong(countStr);
        return Math.max(0, maxRequests - currentCount);
    }
    
    @Override
    public long getResetTime(String identifier, int windowSeconds) {
        String key = getKey(identifier, windowSeconds);
        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        
        if (ttl == null || ttl < 0) {
            return 0; // Already expired or doesn't exist
        }
        
        return ttl;
    }
    
    private String getKey(String identifier, int windowSeconds) {
        long currentTime = System.currentTimeMillis() / 1000;
        long windowNumber = currentTime / windowSeconds;
        return "rate:fixed:" + identifier + ":" + windowNumber;
    }
}