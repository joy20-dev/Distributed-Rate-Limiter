package com.example.rate_limiter.Strategy;

public interface RateLimitStrategy {

    public boolean isAllowed(String identifier, int maxRequests, int windowSeconds);

    public long getRemainingRequests(String identifier, int maxRequests, int windowSeconds);

    public long getResetTime(String identifier, int windowSeconds);


}
