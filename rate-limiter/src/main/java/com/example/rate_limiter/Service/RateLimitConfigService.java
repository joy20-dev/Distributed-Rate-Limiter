package com.example.rate_limiter.Service;

import com.example.rate_limiter.dto.RateLimitConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;


@Service
public class RateLimitConfigService {

    private final String key_prefix ="config:endpoint:";

    public RateLimitConfigService(RedisTemplate<String, String> redisTemplate, ObjectMapper mapper) {
        this.redisTemplate = redisTemplate;
        this.mapper = mapper;
    }

    public RateLimitConfig getConfig(String endpoint){
        String key = key_prefix+endpoint;
        String json = redisTemplate.opsForValue().get(key);

        if(json !=null){
            try 
            {RateLimitConfig config = mapper.readValue(json,RateLimitConfig.class);
            return config;
            }
            catch(JsonProcessingException e){
                throw new IllegalStateException("Invalid config",e);
            }

        }
        else{
            return null;

        }
    }

    public void setConfig(String endpoint, RateLimitConfig config){
        String key = key_prefix+endpoint;
        try
        {String json = mapper.writeValueAsString(config);
        redisTemplate.opsForValue().set(key,json);
        
        }
        catch(JsonProcessingException e){
                throw new IllegalArgumentException("invalid config",e);
        }

    }





}
