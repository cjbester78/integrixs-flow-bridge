package com.integrixs.backend.performance;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.concurrent.TimeUnit;

/**
 * Configuration for connection pool tuning across different components.
 * Provides optimized settings for database and HTTP connection pools.
 */
@Slf4j
@Configuration
public class ConnectionPoolConfiguration {
    
    // Database connection pool settings
    @Value("${spring.datasource.hikari.maximum-pool-size:20}")
    private int dbMaxPoolSize;
    
    @Value("${spring.datasource.hikari.minimum-idle:5}")
    private int dbMinIdle;
    
    @Value("${spring.datasource.hikari.connection-timeout:30000}")
    private long dbConnectionTimeout;
    
    @Value("${spring.datasource.hikari.idle-timeout:600000}")
    private long dbIdleTimeout;
    
    @Value("${spring.datasource.hikari.max-lifetime:1800000}")
    private long dbMaxLifetime;
    
    // HTTP connection pool settings
    @Value("${http.connection.pool.max-total:200}")
    private int httpMaxTotal;
    
    @Value("${http.connection.pool.max-per-route:20}")
    private int httpMaxPerRoute;
    
    @Value("${http.connection.pool.validate-after-inactivity:2000}")
    private int httpValidateAfterInactivity;
    
    @Value("${http.connection.pool.connection-timeout:5000}")
    private int httpConnectionTimeout;
    
    @Value("${http.connection.pool.socket-timeout:30000}")
    private int httpSocketTimeout;
    
    /**
     * Optimized database connection pool configuration.
     */
    @Bean
    @Primary
    @ConditionalOnProperty(name = "database.pool.optimization.enabled", havingValue = "true", matchIfMissing = true)
    public DataSource optimizedDataSource(
            @Value("${spring.datasource.url}") String jdbcUrl,
            @Value("${spring.datasource.username}") String username,
            @Value("${spring.datasource.password}") String password,
            @Value("${spring.datasource.driver-class-name}") String driverClassName,
            MeterRegistry meterRegistry) {
        
        HikariConfig config = new HikariConfig();
        
        // Basic configuration
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(driverClassName);
        
        // Pool sizing - optimized based on CPU cores
        int cpuCores = Runtime.getRuntime().availableProcessors();
        int optimalPoolSize = Math.min(dbMaxPoolSize, cpuCores * 2 + 1);
        config.setMaximumPoolSize(optimalPoolSize);
        config.setMinimumIdle(Math.min(dbMinIdle, optimalPoolSize / 2));
        
        // Timeouts
        config.setConnectionTimeout(dbConnectionTimeout);
        config.setIdleTimeout(dbIdleTimeout);
        config.setMaxLifetime(dbMaxLifetime);
        
        // Performance optimizations
        config.setAutoCommit(false); // Explicit transaction management
        config.setConnectionTestQuery("SELECT 1"); // Lightweight validation query
        config.setLeakDetectionThreshold(60000); // Detect connection leaks after 1 minute
        
        // Connection pool name for monitoring
        config.setPoolName("IntegrixsFlowBridge-DB-Pool");
        
        // Metrics
        config.setMetricRegistry(meterRegistry);
        
        // Additional optimizations based on database type
        if (driverClassName.contains("postgresql")) {
            config.addDataSourceProperty("prepareThreshold", "3");
            config.addDataSourceProperty("preparedStatementCacheQueries", "256");
            config.addDataSourceProperty("preparedStatementCacheSizeMiB", "5");
        } else if (driverClassName.contains("mysql")) {
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "256");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");
        }
        
        HikariDataSource dataSource = new HikariDataSource(config);
        
        log.info("Optimized database connection pool created with size: {}", optimalPoolSize);
        
        return dataSource;
    }
    
    /**
     * Optimized HTTP connection pool for REST/SOAP adapters.
     */
    @Bean
    public PoolingHttpClientConnectionManager httpConnectionManager(MeterRegistry meterRegistry) {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        
        // Configure pool sizes
        connectionManager.setMaxTotal(httpMaxTotal);
        connectionManager.setDefaultMaxPerRoute(httpMaxPerRoute);
        
        // Validate connections after inactivity
        connectionManager.setValidateAfterInactivity(httpValidateAfterInactivity);
        
        // Clean up idle connections periodically
        connectionManager.closeIdleConnections(30, TimeUnit.SECONDS);
        
        // Register metrics
        meterRegistry.gauge("http.connection.pool.total", connectionManager, 
            cm -> cm.getTotalStats().getAvailable() + cm.getTotalStats().getLeased());
        meterRegistry.gauge("http.connection.pool.available", connectionManager, 
            cm -> cm.getTotalStats().getAvailable());
        meterRegistry.gauge("http.connection.pool.leased", connectionManager, 
            cm -> cm.getTotalStats().getLeased());
        meterRegistry.gauge("http.connection.pool.pending", connectionManager, 
            cm -> cm.getTotalStats().getPending());
        
        log.info("HTTP connection pool created with max total: {}, max per route: {}", 
                httpMaxTotal, httpMaxPerRoute);
        
        return connectionManager;
    }
    
    /**
     * Pre-configured HTTP client with optimized settings.
     */
    @Bean
    public CloseableHttpClient httpClient(PoolingHttpClientConnectionManager connectionManager) {
        return HttpClients.custom()
            .setConnectionManager(connectionManager)
            .setConnectionManagerShared(true)
            .evictIdleConnections(30, TimeUnit.SECONDS)
            .evictExpiredConnections()
            .setConnectionTimeToLive(5, TimeUnit.MINUTES)
            .build();
    }
    
    /**
     * Connection pool settings for JMS connections.
     */
    @Bean
    @ConditionalOnProperty(name = "jms.pool.optimization.enabled", havingValue = "true")
    public JmsConnectionPoolSettings jmsConnectionPoolSettings() {
        return JmsConnectionPoolSettings.builder()
            .maxConnections(10)
            .idleTimeout(300000) // 5 minutes
            .expiryTimeout(0) // No expiry
            .timeBetweenExpirationCheckMillis(60000) // Check every minute
            .blockIfSessionPoolIsFull(true)
            .blockIfSessionPoolIsFullTimeout(5000)
            .useAnonymousProducers(true) // Reuse producers
            .build();
    }
    
    /**
     * Connection pool settings for FTP/SFTP connections.
     */
    @Bean
    public FileTransferConnectionPoolSettings fileTransferConnectionPoolSettings() {
        return FileTransferConnectionPoolSettings.builder()
            .maxTotal(10)
            .maxPerHost(5)
            .minEvictableIdleTimeMillis(300000) // 5 minutes
            .timeBetweenEvictionRunsMillis(60000) // Check every minute
            .connectionTimeout(30000) // 30 seconds
            .dataTimeout(120000) // 2 minutes for data transfer
            .keepAliveTimeout(60000) // Keep alive every minute
            .build();
    }
    
    /**
     * Dynamic connection pool tuner that adjusts settings based on load.
     */
    @Bean
    @ConditionalOnProperty(name = "connection.pool.dynamic.tuning.enabled", havingValue = "true")
    public ConnectionPoolTuner connectionPoolTuner(
            DataSource dataSource,
            PoolingHttpClientConnectionManager httpConnectionManager,
            MeterRegistry meterRegistry) {
        
        return new ConnectionPoolTuner(dataSource, httpConnectionManager, meterRegistry);
    }
    
    @lombok.Builder
    @lombok.Data
    public static class JmsConnectionPoolSettings {
        private int maxConnections;
        private long idleTimeout;
        private long expiryTimeout;
        private long timeBetweenExpirationCheckMillis;
        private boolean blockIfSessionPoolIsFull;
        private long blockIfSessionPoolIsFullTimeout;
        private boolean useAnonymousProducers;
    }
    
    @lombok.Builder
    @lombok.Data
    public static class FileTransferConnectionPoolSettings {
        private int maxTotal;
        private int maxPerHost;
        private long minEvictableIdleTimeMillis;
        private long timeBetweenEvictionRunsMillis;
        private int connectionTimeout;
        private int dataTimeout;
        private int keepAliveTimeout;
    }
}