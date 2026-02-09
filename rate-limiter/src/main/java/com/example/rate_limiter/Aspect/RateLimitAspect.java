package com.example.rate_limiter.Aspect;

import com.example.rate_limiter.Annotations.RateLimit;
import com.example.rate_limiter.exception.RateLimitExceededException;
import com.example.rate_limiter.strategy.FixedWindowStrategy;
import com.example.rate_limiter.strategy.RateLimitStrategy;
import com.example.rate_limiter.strategy.SlidingWindowStrategy;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Aspect
@Component
public class RateLimitAspect {
    
    private final FixedWindowStrategy fixedWindowStrategy;
    private final SlidingWindowStrategy slidingWindowStrategy;
    
    
    public RateLimitAspect(FixedWindowStrategy fixedWindowStrategy,
                          SlidingWindowStrategy slidingWindowStrategy
                          ) {
        this.fixedWindowStrategy = fixedWindowStrategy;
        this.slidingWindowStrategy = slidingWindowStrategy;}
    private int count =0;
    
    @Around("@annotation(com.example.rate_limiter.Annotations.RateLimit)")
    public Object rateLimit(ProceedingJoinPoint joinPoint) throws Throwable {

        System.out.println("rate limit being called" + count);
        count ++;


        // Get the annotation on the method
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        RateLimit rateLimit = signature.getMethod().getAnnotation(RateLimit.class); //returns our custom annotation
        
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = attrs.getRequest(); // get request object.
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

        // Get rate limit info
        long remaining = strategy.getRemainingRequests(identifier, rateLimit.requests(), rateLimit.windowSeconds());
        long resetTime = strategy.getResetTime(identifier, rateLimit.windowSeconds());

        HttpServletResponse response = attrs.getResponse(); // get response object 
        if (response != null) {
            response.setHeader("X-RateLimit-Limit", String.valueOf(rateLimit.requests()));
            response.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(0, remaining)));
            response.setHeader("X-RateLimit-Reset", String.valueOf(resetTime));
        }

        
        
        if (!allowed) {
            throw new RateLimitExceededException("Rate limit exceeded, try again in"+ String.valueOf(resetTime) +"s" );
        }


        
        // Allow request to proceed
        return joinPoint.proceed();
    }
    
    private RateLimitStrategy selectStrategy(RateLimit.StrategyType strategyType) {
        if(strategyType == RateLimit.StrategyType.SLIDING_WINDOW){ // check which enum is selected in controller 
            return slidingWindowStrategy;
        }
        else {
            return fixedWindowStrategy ;
        }
    }
}