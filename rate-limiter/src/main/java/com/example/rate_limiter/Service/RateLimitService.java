package com.example.rate_limiter.Service;

import com.example.rate_limiter.strategy.FixedWindowStrategy;
import org.springframework.stereotype.Service;

@Service
public class RateLimitService {
    private final FixedWindowStrategy strategy;
    
    // Global limits - high to prevent DDoS, not per-endpoint limits
    private static final int GLOBAL_MAX_REQUESTS = 100;  // 60 global requests
    private static final int GLOBAL_WINDOW_SECONDS = 60; // per minute

    public RateLimitService(FixedWindowStrategy strategy) {
        this.strategy = strategy;
    }

    public boolean isAllowed(String userIp) {
        String identifier = "global"+ userIp; // pass a different key for global request, else aspect rate limiter will update the same key as global
        return strategy.isAllowed(identifier, GLOBAL_MAX_REQUESTS, GLOBAL_WINDOW_SECONDS);
    }

    public long getRemainingRequests(String userIp) {
        return strategy.getRemainingRequests(userIp, GLOBAL_MAX_REQUESTS, GLOBAL_WINDOW_SECONDS);
    }

    public long getResetTime(String userIp) {
        return strategy.getResetTime(userIp, GLOBAL_WINDOW_SECONDS);
    }
}