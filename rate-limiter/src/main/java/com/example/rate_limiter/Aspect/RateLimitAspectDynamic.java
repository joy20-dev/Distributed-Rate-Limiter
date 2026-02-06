package com.example.rate_limiter.Aspect;

import com.example.rate_limiter.Annotations.RateLimiterDynamic;
import com.example.rate_limiter.Service.RateLimitConfigService;
import com.example.rate_limiter.dto.RateLimitConfig;
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
public class RateLimitAspectDynamic {
    private final FixedWindowStrategy fixedWindowStrategy;
    private final SlidingWindowStrategy slidingWindowStrategy;
    private final RateLimitConfigService configService;

    public RateLimitAspectDynamic(
            FixedWindowStrategy fixedWindowStrategy,
            SlidingWindowStrategy slidingWindowStrategy,
            RateLimitConfigService configService
    ) {
        this.fixedWindowStrategy = fixedWindowStrategy;
        this.slidingWindowStrategy = slidingWindowStrategy;
        this.configService = configService;
    }

    @Around("@annotation(com.example.rate_limiter.Annotations.RateLimitDynamic)")
    public Object rateLimit(ProceedingJoinPoint joinPoint) throws Throwable {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        RateLimiterDynamic rateLimit = signature.getMethod().getAnnotation(RateLimiterDynamic.class);

        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = attrs.getRequest();

        String endpoint = request.getRequestURI();
        String identifier = request.getRemoteAddr();

        //  Get config from Redis
        RateLimitConfig config = configService.getConfig(endpoint);

        //  If not present, use annotation defaults store 
        if (config == null) {
            config = new RateLimitConfig(
                    rateLimit.requests(),
                    rateLimit.windowSeconds(),
                    rateLimit.strategy().name()
                    
            );
        }

        RateLimitStrategy strategy = selectStrategy(config.getStrategy()); // get strategy based on stored value

        boolean allowed = strategy.isAllowed(
                identifier,
                config.getRequests(),
                config.getWindowSeconds()
        );

        // Get rate limit info
        long remaining = strategy.getRemainingRequests(identifier, config.getRequests(), config.getWindowSeconds());
        long resetTime = strategy.getResetTime(identifier, config.getWindowSeconds());

        HttpServletResponse response = attrs.getResponse(); // get response object 
        if (response != null) {
            response.setHeader("X-RateLimit-Limit", String.valueOf(config.getRequests()));
            response.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(0, remaining)));
            response.setHeader("X-RateLimit-Reset", String.valueOf(resetTime));
        }

        if (!allowed) {
            throw new RateLimitExceededException("Rate limit exceeded, try again in"+ String.valueOf(resetTime) +"s" );
        }

        // Allow request to proceed
        return joinPoint.proceed();

    }

    private RateLimitStrategy selectStrategy(String strat){ //get string from config, convert to enum n compare
        RateLimiterDynamic.StrategyType strategy = RateLimiterDynamic.StrategyType.valueOf(strat);
        if(strategy == RateLimiterDynamic.StrategyType.SLIDING_WINDOW){ //  
            return slidingWindowStrategy;
        }
        else {
            return fixedWindowStrategy ;
        }
    }

}
