package com.example.rate_limiter.strategy;

public class TokenBucketStrategy {

    public boolean isAllowed(String identifier, int maxRequests, int windowSeconds){
        String key = getKey(identifier);
        
        return false;
    }

    public long getRemainingRequests(String identifier, int maxRequests, int windowSeconds){
        return 0;
    }

    public long getResetTime(String identifier, int windowSeconds){
        return 0;
    }

    private String getKey(String identifier) {
        return "rate:sliding:" + identifier;
    }

}
