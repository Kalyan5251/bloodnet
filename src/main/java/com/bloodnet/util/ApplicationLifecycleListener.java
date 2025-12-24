package com.bloodnet.util;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Application Lifecycle Listener for BloodNet Application
 * Handles application startup and shutdown events
 */
@WebListener
public class ApplicationLifecycleListener implements ServletContextListener {
    
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        System.out.println("==========================================");
        System.out.println("BloodNet Application Starting...");
        System.out.println("Timestamp: " + timestamp);
        System.out.println("==========================================");
        
        try {
            // Initialize database connection pool
            DBConnection.initialize();
            System.out.println("✓ Database connection pool initialized");
            
            // Test database connectivity
            if (DBConnection.testConnection()) {
                System.out.println("✓ Database connection test successful");
            } else {
                System.err.println("✗ Database connection test failed");
            }
            
            // Print application information
            String appName = sce.getServletContext().getInitParameter("app.name");
            String appVersion = sce.getServletContext().getInitParameter("app.version");
            
            if (appName != null) {
                System.out.println("Application: " + appName);
            }
            if (appVersion != null) {
                System.out.println("Version: " + appVersion);
            }
            
            System.out.println("Server Info: " + sce.getServletContext().getServerInfo());
            System.out.println("Context Path: " + sce.getServletContext().getContextPath());
            
            System.out.println("==========================================");
            System.out.println("BloodNet Application Started Successfully!");
            System.out.println("==========================================");
            
        } catch (Exception e) {
            System.err.println("Error during application initialization: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        System.out.println("==========================================");
        System.out.println("BloodNet Application Shutting Down...");
        System.out.println("Timestamp: " + timestamp);
        System.out.println("==========================================");
        
        try {
            // Close database connections
            DBConnection.closeAllConnections();
            System.out.println("✓ Database connections closed");
            
            System.out.println("==========================================");
            System.out.println("BloodNet Application Shutdown Complete!");
            System.out.println("==========================================");
            
        } catch (Exception e) {
            System.err.println("Error during application shutdown: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
