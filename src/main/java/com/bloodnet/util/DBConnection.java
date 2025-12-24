package com.bloodnet.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Database Connection Pool Manager for BloodNet Application
 * Provides connection pooling for better performance and resource management
 */
public class DBConnection {
    
    private static final String DB_URL = "jdbc:mysql://localhost:3306/bloodnet_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "root"; // Change this to your MySQL password
    private static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver";
    
    // Connection pool settings
    private static final int INITIAL_POOL_SIZE = 5;
    private static final int MAX_POOL_SIZE = 20;
    private static final int CONNECTION_TIMEOUT = 30; // seconds
    
    private static BlockingQueue<Connection> connectionPool;
    private static volatile boolean initialized = false;
    
    /**
     * Initialize the connection pool
     */
    public static synchronized void initialize() {
        if (initialized) {
            return;
        }
        
        try {
            // Load MySQL JDBC driver
            Class.forName(DB_DRIVER);
            
            // Initialize connection pool
            connectionPool = new ArrayBlockingQueue<>(MAX_POOL_SIZE);
            
            // Create initial connections
            for (int i = 0; i < INITIAL_POOL_SIZE; i++) {
                Connection connection = createConnection();
                if (connection != null) {
                    connectionPool.offer(connection);
                }
            }
            
            initialized = true;
            System.out.println("Database connection pool initialized with " + INITIAL_POOL_SIZE + " connections");
            
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
            throw new RuntimeException("Database driver not found", e);
        } catch (SQLException e) {
            System.err.println("Failed to initialize database connection pool: " + e.getMessage());
            throw new RuntimeException("Database initialization failed", e);
        }
    }
    
    /**
     * Get a connection from the pool
     * @return Connection object
     * @throws SQLException if no connection is available
     */
    public static Connection getConnection() throws SQLException {
        if (!initialized) {
            initialize();
        }
        
        try {
            // Try to get connection from pool with timeout
            Connection connection = connectionPool.poll(CONNECTION_TIMEOUT, TimeUnit.SECONDS);
            
            if (connection == null || connection.isClosed()) {
                // Create new connection if pool is empty or connection is closed
                connection = createConnection();
            }
            
            if (connection == null) {
                throw new SQLException("Unable to get database connection");
            }
            
            return connection;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("Connection request interrupted", e);
        }
    }
    
    /**
     * Return a connection to the pool
     * @param connection Connection to return
     */
    public static void returnConnection(Connection connection) {
        if (connection != null && initialized) {
            try {
                if (!connection.isClosed() && connectionPool.size() < MAX_POOL_SIZE) {
                    // Reset connection state
                    connection.setAutoCommit(true);
                    connectionPool.offer(connection);
                } else {
                    // Close connection if pool is full or connection is closed
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println("Error returning connection to pool: " + e.getMessage());
                try {
                    connection.close();
                } catch (SQLException ex) {
                    System.err.println("Error closing connection: " + ex.getMessage());
                }
            }
        }
    }
    
    /**
     * Create a new database connection
     * @return Connection object or null if failed
     */
    private static Connection createConnection() throws SQLException {
        try {
            Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
            connection.setAutoCommit(true);
            return connection;
        } catch (SQLException e) {
            System.err.println("Failed to create database connection: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Close all connections in the pool
     */
    public static synchronized void closeAllConnections() {
        if (connectionPool != null) {
            Connection connection;
            while ((connection = connectionPool.poll()) != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    System.err.println("Error closing connection: " + e.getMessage());
                }
            }
            initialized = false;
            System.out.println("All database connections closed");
        }
    }
    
    /**
     * Get current pool status
     * @return String with pool statistics
     */
    public static String getPoolStatus() {
        if (!initialized || connectionPool == null) {
            return "Connection pool not initialized";
        }
        
        return String.format("Pool Status - Available: %d, Max: %d", 
                           connectionPool.size(), MAX_POOL_SIZE);
    }
    
    /**
     * Test database connectivity
     * @return true if connection successful, false otherwise
     */
    public static boolean testConnection() {
        try {
            Connection connection = getConnection();
            boolean isValid = connection.isValid(5); // 5 second timeout
            returnConnection(connection);
            return isValid;
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            return false;
        }
    }
}
