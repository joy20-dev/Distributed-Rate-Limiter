package com.example.rate_limiter.Controller;

import com.example.rate_limiter.Service.RateLimitConfigService;
import com.example.rate_limiter.dto.RateLimitConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    
    private final RateLimitConfigService configService;

    public AdminController(RateLimitConfigService configService) {
        
        this.configService= configService;
    }

    


    @GetMapping("/config/{endpoint}")
    public ResponseEntity<?> getConfig(@PathVariable String endpoint){
        try 
        {RateLimitConfig config = configService.viewConfig(endpoint);
            if(config==null){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message","config not found"));

            }
            return ResponseEntity.
                            status(HttpStatus.OK)
                            .body(config);
        }
        catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to parse config"));
        }

        
        
    }

    @PutMapping("/config/{endpoint}")
    public ResponseEntity<?> putConfig(@PathVariable String endpoint,@RequestBody RateLimitConfig config){
        
        try
        {configService.setConfig(endpoint, config);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message","config set successfully"));
        }
        catch(Exception e){
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to parse config"));
        }



    }

    


}
