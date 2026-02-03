package com.example.rate_limiter.Controller;

import org.springframework.web.bind.annotation.*;

import com.example.rate_limiter.Service.HealthService;

@RestController
public class HealthController {

    HealthService healthService;
    public HealthController(HealthService healthService){
        this.healthService =healthService;
    }

    @GetMapping("/health")
    public String healthCheck(){
        return healthService.status();
    }

}
