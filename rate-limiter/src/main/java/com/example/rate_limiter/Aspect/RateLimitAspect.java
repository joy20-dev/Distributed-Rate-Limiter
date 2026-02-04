package com.example.rate_limiter.Aspect;

import com.example.rate_limiter.Annotations.RateLimit;
import com.example.rate_limiter.strategy.FixedWindowStrategy;
import com.example.rate_limiter.strategy.RateLimitStrategy;
import com.example.rate_limiter.strategy.SlidingWindowStrategy;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;

@Aspect
@Component
public class RateLimitAspect {
    
    private final FixedWindowStrategy fixedWindowStrategy;
    private final SlidingWindowStrategy slidingWindowStrategy;
    private final HttpServletRequest request;
    
    public RateLimitAspect(FixedWindowStrategy fixedWindowStrategy,
                          SlidingWindowStrategy slidingWindowStrategy,
                          HttpServletRequest request) {
        this.fixedWindowStrategy = fixedWindowStrategy;
        this.slidingWindowStrategy = slidingWindowStrategy;
        this.request = request;
    }
    
    @Around("@annotation(com.example.rate_limiter.annotation.RateLimit)")
    public Object rateLimit(ProceedingJoinPoint joinPoint) throws Throwable {
        // Get the annotation
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        RateLimit rateLimit = signature.getMethod().getAnnotation(RateLimit.class);
        
        // Get user identifier (IP address)
        String identifier = request.getRemoteAddr();
        
        // Select strategy
        RateLimitStrategy strategy = selectStrategy(rateLimit.strategy()); // rateLimit.strategy() will return a enum fixed window or sliding window
        
        // Check rate limit
        boolean allowed = strategy.isAllowed(
            identifier,
            rateLimit.requests(),
            rateLimit.windowSeconds()
        );
        
        if (!allowed) {
            throw new ResponseStatusException(
                HttpStatus.TOO_MANY_REQUESTS,
                "Rate limit exceeded"
            );
        }
        
        // Allow request to proceed
        return joinPoint.proceed();
    }
    
    private RateLimitStrategy selectStrategy(RateLimit.StrategyType strategyType) {
        if(strategyType == RateLimit.StrategyType.SLIDING_WINDOW){ // check which enum is selected in controller 
            return SlidingWindowStrategy;
        }
        else {
            return fixedWindowStrategy ;
        }
    }
}