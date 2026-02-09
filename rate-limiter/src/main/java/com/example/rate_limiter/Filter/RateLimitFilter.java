package com.example.rate_limiter.Filter;

import com.example.rate_limiter.Service.RateLimitService;
import com.example.rate_limiter.Service.RateLimitService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class RateLimitFilter implements Filter {

    private final RateLimitService rateLimitService;
    private final ObjectMapper objectMapper;

    public RateLimitFilter(RateLimitService rateLimitService,ObjectMapper objectMapper) {
        this.rateLimitService = rateLimitService;
        this.objectMapper= objectMapper;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        
        String path = httpRequest.getRequestURI();
        String clientIp = httpRequest.getRemoteAddr();

        System.out.println("=== FILTER ACTIVE ===");
        System.out.println("Path: " + path);
        System.out.println("IP: " + clientIp);

        // Global rate limit check
        boolean allowed = rateLimitService.isAllowed(clientIp);
        
        System.out.println("isAllowed returned: " + allowed);

        
        

        // Global rate limit check
        if (!allowed) {
            // Get remaining time
            long resetTime = rateLimitService.getResetTime(clientIp);
            
            // Build error response
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Global rate limit exceeded");
            errorResponse.put("message", "Too many requests. Please try again later.");
            errorResponse.put("retryAfter", resetTime + "s");
            errorResponse.put("status", 429);

            // Set response
            httpResponse.setStatus(429);
            httpResponse.setContentType("application/json");
            httpResponse.setCharacterEncoding("UTF-8");
            
            // Add Retry-After header
            httpResponse.setHeader("Retry-After", String.valueOf(resetTime));
            
            // Write JSON response
            httpResponse.getWriter().write(objectMapper.writeValueAsString(errorResponse));
            httpResponse.getWriter().flush();
            
            return; // Stop processing - DON'T call chain.doFilter()
        }

        chain.doFilter(request, response);
    }
}