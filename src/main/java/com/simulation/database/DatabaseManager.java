package com.simulation.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Database connection manager using HikariCP connection pooling.
 *
 * <p>This class implements the Singleton pattern to ensure only one connection pool
 * exists throughout the application lifecycle. It provides thread-safe database
 * connections using HikariCP, a high-performance JDBC connection pool.
 *
 * <p>Configuration is loaded from {@code database.properties} file in the classpath,
 * which should contain the following properties:
 * <pre>
 * db.url=jdbc:mariadb://localhost:3306/simulation_db
 * db.username=root
 * db.password=yourpassword
 * db.driver=org.mariadb.jdbc.Driver
 * </pre>
 *
 * <p>Graceful Degradation: If database connection fails (e.g., MariaDB server not running),
 * the application continues to function without database features. Error messages are
 * logged to stderr.
 *
 * <p>Connection Pool Configuration:
 * <ul>
 *   <li>Maximum Pool Size: 10 connections</li>
 *   <li>Minimum Idle: 2 connections</li>
 *   <li>Connection Timeout: 30 seconds</li>
 *   <li>Idle Timeout: 600 seconds (10 minutes)</li>
 *   <li>Max Lifetime: 1800 seconds (30 minutes)</li>
 * </ul>
 *
 * @author Cloud Simulation Team
 * @version 2.0
 * @see HikariDataSource
 * @see SimulationConfigDAO
 * @see SimulationResultsDAO
 */
public class DatabaseManager {
    private static DatabaseManager instance;
    private HikariDataSource dataSource;

    private DatabaseManager() {
        try {
            initializeDataSource();
        } catch (Exception e) {
            System.err.println("⚠️  Database connection failed: " + e.getMessage());
            System.err.println("⚠️  The application will continue without database features.");
            System.err.println("⚠️  Please check your MariaDB server and database.properties configuration.");
            // Don't throw exception - allow app to continue without database
        }
    }

    /**
     * Get singleton instance of DatabaseManager
     */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /**
     * Initialize HikariCP data source from properties file
     */
    private void initializeDataSource() throws IOException {
        Properties props = new Properties();

        // Load database properties
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("database.properties")) {
            if (input == null) {
                throw new IOException("Unable to find database.properties");
            }
            props.load(input);
        }

        // Configure HikariCP
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(props.getProperty("db.url"));
        config.setUsername(props.getProperty("db.username"));
        config.setPassword(props.getProperty("db.password", ""));

        // Connection pool settings
        config.setMaximumPoolSize(Integer.parseInt(
            props.getProperty("db.pool.maximumPoolSize", "10")));
        config.setMinimumIdle(Integer.parseInt(
            props.getProperty("db.pool.minimumIdle", "2")));
        config.setConnectionTimeout(Long.parseLong(
            props.getProperty("db.pool.connectionTimeout", "30000")));
        config.setIdleTimeout(Long.parseLong(
            props.getProperty("db.pool.idleTimeout", "600000")));
        config.setMaxLifetime(Long.parseLong(
            props.getProperty("db.pool.maxLifetime", "1800000")));

        // Performance settings
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        dataSource = new HikariDataSource(config);
    }

    /**
     * Get a connection from the pool
     * @return Database connection
     * @throws SQLException if connection cannot be obtained
     */
    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("Database connection pool not initialized");
        }
        return dataSource.getConnection();
    }

    /**
     * Test database connectivity
     * @return true if connection is successful
     */
    public boolean testConnection() {
        if (dataSource == null) {
            return false;
        }
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Close the connection pool
     * Should be called when application is shutting down
     */
    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    /**
     * Get connection pool statistics
     */
    public String getPoolStats() {
        if (dataSource != null) {
            return String.format(
                "Active: %d, Idle: %d, Total: %d, Waiting: %d",
                dataSource.getHikariPoolMXBean().getActiveConnections(),
                dataSource.getHikariPoolMXBean().getIdleConnections(),
                dataSource.getHikariPoolMXBean().getTotalConnections(),
                dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection()
            );
        }
        return "Pool not initialized";
    }
}

