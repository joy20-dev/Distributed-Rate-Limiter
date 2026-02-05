package com.example.rate_limiter.Controller;

import com.example.rate_limiter.Annotations.RateLimit;
import com.example.rate_limiter.Annotations.RateLimit.StrategyType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TestController {
    
    @RateLimit(requests = 5, windowSeconds = 120, strategy = StrategyType.FIXED_WINDOW)
    @GetMapping("/free")
    public String freeEndpoint() {
        return "Free tier - 5 requests per minute (Fixed Window)";
    }
    
    @RateLimit(requests = 20, windowSeconds = 60, strategy = StrategyType.SLIDING_WINDOW)
    @GetMapping("/premium")
    public String premiumEndpoint() {
        return "Premium tier - 20 requests per minute (Sliding Window)";
    }
    
    @GetMapping("/unlimited")
    public String unlimitedEndpoint() {
        return "No annotation - only global filter limit (100/min)";
    }
}