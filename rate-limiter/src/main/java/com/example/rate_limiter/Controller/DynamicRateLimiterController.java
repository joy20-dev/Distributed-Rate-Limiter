package com.example.rate_limiter.Controller;


import com.example.rate_limiter.Annotations.RateLimit;
import com.example.rate_limiter.Annotations.RateLimit.StrategyType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class DynamicRateLimiterController {

}
