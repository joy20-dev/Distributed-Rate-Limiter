package com.example.rate_limiter.Filter;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.IOException;


import com.example.rate_limiter.Service.RateLimitService;

/*this filter will run before request reaches our dispatch servlet and check if user has tokens in the bucket */

@Component

public class RateLimitFilter extends OncePerRequestFilter {
    
    private final RateLimitService rateLimitService;

    public RateLimitFilter(RateLimitService rateLimitService){
        this.rateLimitService = rateLimitService;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
    
    



    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
    throws ServletException, IOException{

        

        String ip= getClientIp(request);
        if(!rateLimitService.isAllowed(ip)){
            response.setStatus(429);
            response.getWriter().write("too many requests");
            return;
        }
        
        

        filterChain.doFilter(request,response);

        
    }

}
