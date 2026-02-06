package com.example.rate_limiter.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RateLimitConfig {
    private int requests;
    private int windowSeconds;
    private String strategy;
}