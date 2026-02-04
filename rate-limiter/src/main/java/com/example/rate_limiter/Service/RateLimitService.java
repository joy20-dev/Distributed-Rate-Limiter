package com.example.rate_limiter.Service;

import com.example.rate_limiter.strategy.RateLimitStrategy;
import com.example.rate_limiter.strategy.SlidingWindowStrategy;

import org.springframework.stereotype.Service;

@Service
public class RateLimitService {
    private final RateLimitStrategy strategy;
    private static final int MAX_REQUESTS = 15;
    private static final int WINDOW_SECONDS = 60;

    public RateLimitService(SlidingWindowStrategy strategy) {
        this.strategy = strategy;
    }

    public boolean isAllowed(String userIp) {
        return strategy.isAllowed(userIp, MAX_REQUESTS, WINDOW_SECONDS);
    }

    public long getRemainingRequests(String userIp) {
        return strategy.getRemainingRequests(userIp, MAX_REQUESTS, WINDOW_SECONDS);
    }

    public long getResetTime(String userIp) {
        return strategy.getResetTime(userIp, WINDOW_SECONDS);
    }

    public String currStatus(String userIp) {
        long remaining = getRemainingRequests(userIp);
        long resetTime = getResetTime(userIp);
        
        return "user: " + userIp + 
               " | remaining: " + remaining + 
               " | reset in: " + resetTime + "s";
    }
}