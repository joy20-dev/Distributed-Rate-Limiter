package com.example.rate_limiter.Service;
import org.springframework.stereotype.Service;

@Service
public class HealthService {

    public String status(){
        return "OK\n";
    }

}
