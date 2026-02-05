package com.example.rate_limiter.Filter;

import com.example.rate_limiter.Service.RateLimitService;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RateLimitFilter implements Filter {

    private final RateLimitService rateLimitService;

    public RateLimitFilter(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        System.out.println("request is in filter");

        String clientIp = httpRequest.getRemoteAddr();
        
        

        // Global rate limit check
        if (!rateLimitService.isAllowed(clientIp)) {
            httpResponse.setStatus(429);
            httpResponse.getWriter().write("Global rate limit exceeded. Try again later.");
            return;
        }

        chain.doFilter(request, response);
    }
}