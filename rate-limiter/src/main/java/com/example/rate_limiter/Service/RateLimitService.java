package com.example.rate_limiter.Service;


import java.sql.Time;
import java.util.concurrent.TimeUnit;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;

import org.springframework.data.redis.core.RedisTemplate;

@Service
public class RateLimitService {
    private final RedisTemplate<String,String> redisTemplate;

    public RateLimitService(RedisTemplate<String,String> redisTemplate){
        this.redisTemplate= redisTemplate;
    }

    private String getCurrentMinute() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
    }
    
    private String getKey(String userIp){
        String key = "rate:" + userIp + ":" + getCurrentMinute();
        return key;
    }
    

    public boolean isAllowed(String userIp) {
        String key = getKey(userIp);
        Long count = redisTemplate.opsForValue().increment(key);

        if (count == 1) {
            redisTemplate.expire(key, 1, TimeUnit.MINUTES);
        }

        return count <= 15;
    }

    public String currStatus(String userIp){
        String key = getKey(userIp);

        String count = redisTemplate.opsForValue().get(key);
        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);

        return "user: " + userIp + " | count: " + (count == null ? "0" : count) + " | TTL: " + ttl + "s";
     
    }

}
