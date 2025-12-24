package com.bloodnet.util;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Request Logging Filter for BloodNet Application
 * Logs all incoming requests for monitoring and debugging purposes
 */
public class RequestLoggingFilter implements Filter {
    
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("RequestLoggingFilter initialized at " + LocalDateTime.now().format(TIMESTAMP_FORMAT));
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // Log request details
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String method = httpRequest.getMethod();
        String uri = httpRequest.getRequestURI();
        String queryString = httpRequest.getQueryString();
        String userAgent = httpRequest.getHeader("User-Agent");
        String remoteAddr = getClientIpAddress(httpRequest);
        
        // Build log message
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("[").append(timestamp).append("] ");
        logMessage.append(method).append(" ");
        logMessage.append(uri);
        
        if (queryString != null && !queryString.isEmpty()) {
            logMessage.append("?").append(queryString);
        }
        
        logMessage.append(" - IP: ").append(remoteAddr);
        
        // Add user agent for non-static resources
        if (!isStaticResource(uri) && userAgent != null) {
            logMessage.append(" - UA: ").append(userAgent.length() > 50 ? 
                userAgent.substring(0, 50) + "..." : userAgent);
        }
        
        // Log the request
        System.out.println(logMessage.toString());
        
        // Start timing
        long startTime = System.currentTimeMillis();
        
        try {
            // Continue with the request
            chain.doFilter(request, response);
        } finally {
            // Log response time
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            if (duration > 1000) { // Log slow requests (>1 second)
                System.out.println("[" + timestamp + "] SLOW REQUEST: " + method + " " + uri + 
                                 " took " + duration + "ms");
            }
        }
    }
    
    @Override
    public void destroy() {
        System.out.println("RequestLoggingFilter destroyed at " + LocalDateTime.now().format(TIMESTAMP_FORMAT));
    }
    
    /**
     * Get client IP address, considering proxy headers
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    /**
     * Check if the request is for a static resource
     */
    private boolean isStaticResource(String uri) {
        return uri.endsWith(".css") || uri.endsWith(".js") || uri.endsWith(".png") || 
               uri.endsWith(".jpg") || uri.endsWith(".jpeg") || uri.endsWith(".gif") || 
               uri.endsWith(".ico") || uri.endsWith(".svg") || uri.endsWith(".woff") || 
               uri.endsWith(".woff2") || uri.endsWith(".ttf") || uri.endsWith(".eot");
    }
}
