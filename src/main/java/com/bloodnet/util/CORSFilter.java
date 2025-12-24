package com.bloodnet.util;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * CORS Filter for BloodNet Application
 * Handles Cross-Origin Resource Sharing for API endpoints
 */
public class CORSFilter implements Filter {
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("CORSFilter initialized");
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // Get origin from request
        String origin = httpRequest.getHeader("Origin");
        
        // Set CORS headers
        if (origin != null && isAllowedOrigin(origin)) {
            httpResponse.setHeader("Access-Control-Allow-Origin", origin);
        } else {
            // Allow requests from same origin
            httpResponse.setHeader("Access-Control-Allow-Origin", "*");
        }
        
        httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        httpResponse.setHeader("Access-Control-Allow-Headers", 
            "Content-Type, Authorization, X-Requested-With, Accept, Origin");
        httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
        httpResponse.setHeader("Access-Control-Max-Age", "3600");
        
        // Handle preflight requests
        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            httpResponse.setStatus(HttpServletResponse.SC_OK);
            return;
        }
        
        // Continue with the request
        chain.doFilter(request, response);
    }
    
    @Override
    public void destroy() {
        System.out.println("CORSFilter destroyed");
    }
    
    /**
     * Check if the origin is allowed
     */
    private boolean isAllowedOrigin(String origin) {
        // In production, you should maintain a whitelist of allowed origins
        // For development, allow common localhost origins
        return origin.startsWith("http://localhost") || 
               origin.startsWith("https://localhost") ||
               origin.startsWith("http://127.0.0.1") ||
               origin.startsWith("https://127.0.0.1");
    }
}
