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


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
    throws ServletException, IOException{

        String ip= request.getRemoteAddr();
        if(!rateLimitService.isAllowed()){
            response.setStatus(429);
            response.getWriter().write("too many requests");
            return;
        }

        filterChain.doFilter(request,response);

        
    }

}
