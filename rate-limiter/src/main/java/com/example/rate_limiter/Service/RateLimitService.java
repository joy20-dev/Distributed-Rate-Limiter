package com.example.rate_limiter.Service;


import org.springframework.stereotype.Service;

@Service
public class RateLimitService {
    private int count=0;

    public boolean isAllowed(){
        count ++;
        return count <=5;
    }

}
