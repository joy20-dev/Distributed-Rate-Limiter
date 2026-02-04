package com.example.rate_limiter.Controller;

import org.springframework.web.bind.annotation.*;

import com.example.rate_limiter.Annotations.RateLimit;
import com.example.rate_limiter.Service.HealthService;
import com.example.rate_limiter.Service.RateLimitService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1")
public class HealthController {

    private final HealthService healthService;
    private final RateLimitService rateLimitService;

    public HealthController(HealthService healthService ,RateLimitService rateLimitService){
        this.healthService =healthService;
        this.rateLimitService= rateLimitService;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    @GetMapping("/health")
    public String healthCheck(){

        return healthService.status();
    }

    @GetMapping("/status")
    public String status(HttpServletRequest request) {
        // return "in status";
        String ip = getClientIp(request);
        return rateLimitService.currStatus(ip);
    }


    @RateLimit(requests = 100, windowSeconds = 60, strategy = StrategyType.SLIDING_WINDOW)
    @GetMapping("/premiumEndPoint")
    public String premium(){
        return "you are a premium user ";
    }

    @RateLimit(requests = 10, windowSeconds = 60, strategy = StrategyType.FIXED_WINDOW)
    @GetMapping("/freeEndPoint")
    public String free(){
        return "you are a free user ";
    }


}
