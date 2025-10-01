package com.integrixs.backend.performance;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.micrometer.core.instrument.MeterRegistry;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration for connection pool tuning across different components.
 * Provides optimized settings for database and HTTP connection pools.
 */
@Configuration
public class ConnectionPoolConfiguration {

    // Database connection pool settings

    private static final Logger log = LoggerFactory.getLogger(ConnectionPoolConfiguration.class);

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

    @Value("${connection-pool.database.leak-detection-threshold:60000}")
    private long dbLeakDetectionThreshold;

    // PostgreSQL specific
    @Value("${connection-pool.database.postgresql.prepare-threshold:3}")
    private int pgPrepareThreshold;

    @Value("${connection-pool.database.postgresql.prepared-statement-cache-queries:256}")
    private int pgPreparedStatementCacheQueries;

    @Value("${connection-pool.database.postgresql.prepared-statement-cache-size-mib:5}")
    private int pgPreparedStatementCacheSizeMiB;

    // MySQL specific
    @Value("${connection-pool.database.mysql.cache-prep-stmts:true}")
    private boolean mysqlCachePrepStmts;

    @Value("${connection-pool.database.mysql.prep-stmt-cache-size:256}")
    private int mysqlPrepStmtCacheSize;

    @Value("${connection-pool.database.mysql.prep-stmt-cache-sql-limit:2048}")
    private int mysqlPrepStmtCacheSqlLimit;

    @Value("${connection-pool.database.mysql.use-server-prep-stmts:true}")
    private boolean mysqlUseServerPrepStmts;

    // HTTP pool
    @Value("${connection-pool.http.idle-connection-timeout:30}")
    private long httpIdleConnectionTimeout;

    @Value("${connection-pool.http.connection-ttl-minutes:5}")
    private long httpConnectionTtlMinutes;

    // JMS pool
    @Value("${connection-pool.jms.max-connections:10}")
    private int jmsMaxConnections;

    @Value("${connection-pool.jms.idle-timeout:300000}")
    private long jmsIdleTimeout;

    @Value("${connection-pool.jms.expiry-timeout:0}")
    private long jmsExpiryTimeout;

    @Value("${connection-pool.jms.time-between-expiration-check-millis:60000}")
    private long jmsTimeBetweenExpirationCheckMillis;

    @Value("${connection-pool.jms.block-if-session-pool-is-full:true}")
    private boolean jmsBlockIfSessionPoolIsFull;

    @Value("${connection-pool.jms.block-if-session-pool-is-full-timeout:5000}")
    private long jmsBlockIfSessionPoolIsFullTimeout;

    @Value("${connection-pool.jms.use-anonymous-producers:true}")
    private boolean jmsUseAnonymousProducers;

    // FTP/SFTP pool
    @Value("${connection-pool.file-transfer.max-total:10}")
    private int ftpMaxTotal;

    @Value("${connection-pool.file-transfer.max-per-host:5}")
    private int ftpMaxPerHost;

    @Value("${connection-pool.file-transfer.min-evictable-idle-time-millis:300000}")
    private long ftpMinEvictableIdleTimeMillis;

    @Value("${connection-pool.file-transfer.time-between-eviction-runs-millis:60000}")
    private long ftpTimeBetweenEvictionRunsMillis;

    @Value("${connection-pool.file-transfer.connection-timeout:30000}")
    private int ftpConnectionTimeout;

    @Value("${connection-pool.file-transfer.data-timeout:120000}")
    private int ftpDataTimeout;

    @Value("${connection-pool.file-transfer.keep-alive-timeout:60000}")
    private int ftpKeepAliveTimeout;

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
        config.setLeakDetectionThreshold(dbLeakDetectionThreshold); // Detect connection leaks

        // Connection pool name for monitoring
        config.setPoolName("IntegrixsFlowBridge-DB-Pool");

        // Metrics
        config.setMetricRegistry(meterRegistry);

        // Additional optimizations based on database type
        if(driverClassName.contains("postgresql")) {
            config.addDataSourceProperty("prepareThreshold", String.valueOf(pgPrepareThreshold));
            config.addDataSourceProperty("preparedStatementCacheQueries", String.valueOf(pgPreparedStatementCacheQueries));
            config.addDataSourceProperty("preparedStatementCacheSizeMiB", String.valueOf(pgPreparedStatementCacheSizeMiB));
        } else if(driverClassName.contains("mysql")) {
            config.addDataSourceProperty("cachePrepStmts", String.valueOf(mysqlCachePrepStmts));
            config.addDataSourceProperty("prepStmtCacheSize", String.valueOf(mysqlPrepStmtCacheSize));
            config.addDataSourceProperty("prepStmtCacheSqlLimit", String.valueOf(mysqlPrepStmtCacheSqlLimit));
            config.addDataSourceProperty("useServerPrepStmts", String.valueOf(mysqlUseServerPrepStmts));
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
        connectionManager.closeIdleConnections(httpIdleConnectionTimeout, TimeUnit.SECONDS);

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
     * Pre - configured HTTP client with optimized settings.
     */
    @Bean
    public CloseableHttpClient httpClient(PoolingHttpClientConnectionManager connectionManager) {
        return HttpClients.custom()
            .setConnectionManager(connectionManager)
            .setConnectionManagerShared(true)
            .evictIdleConnections(httpIdleConnectionTimeout, TimeUnit.SECONDS)
            .evictExpiredConnections()
            .setConnectionTimeToLive(httpConnectionTtlMinutes, TimeUnit.MINUTES)
            .build();
    }

    /**
     * Connection pool settings for JMS connections.
     */
    @Bean
    @ConditionalOnProperty(name = "jms.pool.optimization.enabled", havingValue = "true")
    public JmsConnectionPoolSettings jmsConnectionPoolSettings() {
        return JmsConnectionPoolSettings.builder()
            .maxConnections(jmsMaxConnections)
            .idleTimeout(jmsIdleTimeout)
            .expiryTimeout(jmsExpiryTimeout)
            .timeBetweenExpirationCheckMillis(jmsTimeBetweenExpirationCheckMillis)
            .blockIfSessionPoolIsFull(jmsBlockIfSessionPoolIsFull)
            .blockIfSessionPoolIsFullTimeout(jmsBlockIfSessionPoolIsFullTimeout)
            .useAnonymousProducers(jmsUseAnonymousProducers)
            .build();
    }

    /**
     * Connection pool settings for FTP/SFTP connections.
     */
    @Bean
    public FileTransferConnectionPoolSettings fileTransferConnectionPoolSettings() {
        return FileTransferConnectionPoolSettings.builder()
            .maxTotal(ftpMaxTotal)
            .maxPerHost(ftpMaxPerHost)
            .minEvictableIdleTimeMillis(ftpMinEvictableIdleTimeMillis)
            .timeBetweenEvictionRunsMillis(ftpTimeBetweenEvictionRunsMillis)
            .connectionTimeout(ftpConnectionTimeout)
            .dataTimeout(ftpDataTimeout)
            .keepAliveTimeout(ftpKeepAliveTimeout)
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

    public static class JmsConnectionPoolSettings {
        private int maxConnections;
        private long idleTimeout;
        private long expiryTimeout;
        private long timeBetweenExpirationCheckMillis;
        private boolean blockIfSessionPoolIsFull;
        private long blockIfSessionPoolIsFullTimeout;
        private boolean useAnonymousProducers;

        // Default constructor
        public JmsConnectionPoolSettings() {}

        // Builder pattern
        public static JmsConnectionPoolSettingsBuilder builder() {
            return new JmsConnectionPoolSettingsBuilder();
        }

        // Getters and setters
        public int getMaxConnections() { return maxConnections; }
        public void setMaxConnections(int maxConnections) { this.maxConnections = maxConnections; }

        public long getIdleTimeout() { return idleTimeout; }
        public void setIdleTimeout(long idleTimeout) { this.idleTimeout = idleTimeout; }

        public long getExpiryTimeout() { return expiryTimeout; }
        public void setExpiryTimeout(long expiryTimeout) { this.expiryTimeout = expiryTimeout; }

        public long getTimeBetweenExpirationCheckMillis() { return timeBetweenExpirationCheckMillis; }
        public void setTimeBetweenExpirationCheckMillis(long timeBetweenExpirationCheckMillis) { this.timeBetweenExpirationCheckMillis = timeBetweenExpirationCheckMillis; }

        public boolean isBlockIfSessionPoolIsFull() { return blockIfSessionPoolIsFull; }
        public void setBlockIfSessionPoolIsFull(boolean blockIfSessionPoolIsFull) { this.blockIfSessionPoolIsFull = blockIfSessionPoolIsFull; }

        public long getBlockIfSessionPoolIsFullTimeout() { return blockIfSessionPoolIsFullTimeout; }
        public void setBlockIfSessionPoolIsFullTimeout(long blockIfSessionPoolIsFullTimeout) { this.blockIfSessionPoolIsFullTimeout = blockIfSessionPoolIsFullTimeout; }

        public boolean isUseAnonymousProducers() { return useAnonymousProducers; }
        public void setUseAnonymousProducers(boolean useAnonymousProducers) { this.useAnonymousProducers = useAnonymousProducers; }

        // Builder class
        public static class JmsConnectionPoolSettingsBuilder {
            private int maxConnections;
            private long idleTimeout;
            private long expiryTimeout;
            private long timeBetweenExpirationCheckMillis;
            private boolean blockIfSessionPoolIsFull;
            private long blockIfSessionPoolIsFullTimeout;
            private boolean useAnonymousProducers;

            public JmsConnectionPoolSettingsBuilder maxConnections(int maxConnections) {
                this.maxConnections = maxConnections;
                return this;
            }

            public JmsConnectionPoolSettingsBuilder idleTimeout(long idleTimeout) {
                this.idleTimeout = idleTimeout;
                return this;
            }

            public JmsConnectionPoolSettingsBuilder expiryTimeout(long expiryTimeout) {
                this.expiryTimeout = expiryTimeout;
                return this;
            }

            public JmsConnectionPoolSettingsBuilder timeBetweenExpirationCheckMillis(long timeBetweenExpirationCheckMillis) {
                this.timeBetweenExpirationCheckMillis = timeBetweenExpirationCheckMillis;
                return this;
            }

            public JmsConnectionPoolSettingsBuilder blockIfSessionPoolIsFull(boolean blockIfSessionPoolIsFull) {
                this.blockIfSessionPoolIsFull = blockIfSessionPoolIsFull;
                return this;
            }

            public JmsConnectionPoolSettingsBuilder blockIfSessionPoolIsFullTimeout(long blockIfSessionPoolIsFullTimeout) {
                this.blockIfSessionPoolIsFullTimeout = blockIfSessionPoolIsFullTimeout;
                return this;
            }

            public JmsConnectionPoolSettingsBuilder useAnonymousProducers(boolean useAnonymousProducers) {
                this.useAnonymousProducers = useAnonymousProducers;
                return this;
            }

            public JmsConnectionPoolSettings build() {
                JmsConnectionPoolSettings settings = new JmsConnectionPoolSettings();
                settings.setMaxConnections(this.maxConnections);
                settings.setIdleTimeout(this.idleTimeout);
                settings.setExpiryTimeout(this.expiryTimeout);
                settings.setTimeBetweenExpirationCheckMillis(this.timeBetweenExpirationCheckMillis);
                settings.setBlockIfSessionPoolIsFull(this.blockIfSessionPoolIsFull);
                settings.setBlockIfSessionPoolIsFullTimeout(this.blockIfSessionPoolIsFullTimeout);
                settings.setUseAnonymousProducers(this.useAnonymousProducers);
                return settings;
            }
        }
    }

    public static class FileTransferConnectionPoolSettings {
        private int maxTotal;
        private int maxPerHost;
        private long minEvictableIdleTimeMillis;
        private long timeBetweenEvictionRunsMillis;
        private int connectionTimeout;
        private int dataTimeout;
        private int keepAliveTimeout;

        // Default constructor
        public FileTransferConnectionPoolSettings() {}

        // Builder pattern
        public static FileTransferConnectionPoolSettingsBuilder builder() {
            return new FileTransferConnectionPoolSettingsBuilder();
        }

        // Getters and setters
        public int getMaxTotal() { return maxTotal; }
        public void setMaxTotal(int maxTotal) { this.maxTotal = maxTotal; }

        public int getMaxPerHost() { return maxPerHost; }
        public void setMaxPerHost(int maxPerHost) { this.maxPerHost = maxPerHost; }

        public long getMinEvictableIdleTimeMillis() { return minEvictableIdleTimeMillis; }
        public void setMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis) { this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis; }

        public long getTimeBetweenEvictionRunsMillis() { return timeBetweenEvictionRunsMillis; }
        public void setTimeBetweenEvictionRunsMillis(long timeBetweenEvictionRunsMillis) { this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis; }

        public int getConnectionTimeout() { return connectionTimeout; }
        public void setConnectionTimeout(int connectionTimeout) { this.connectionTimeout = connectionTimeout; }

        public int getDataTimeout() { return dataTimeout; }
        public void setDataTimeout(int dataTimeout) { this.dataTimeout = dataTimeout; }

        public int getKeepAliveTimeout() { return keepAliveTimeout; }
        public void setKeepAliveTimeout(int keepAliveTimeout) { this.keepAliveTimeout = keepAliveTimeout; }

        // Builder class
        public static class FileTransferConnectionPoolSettingsBuilder {
            private int maxTotal;
            private int maxPerHost;
            private long minEvictableIdleTimeMillis;
            private long timeBetweenEvictionRunsMillis;
            private int connectionTimeout;
            private int dataTimeout;
            private int keepAliveTimeout;

            public FileTransferConnectionPoolSettingsBuilder maxTotal(int maxTotal) {
                this.maxTotal = maxTotal;
                return this;
            }

            public FileTransferConnectionPoolSettingsBuilder maxPerHost(int maxPerHost) {
                this.maxPerHost = maxPerHost;
                return this;
            }

            public FileTransferConnectionPoolSettingsBuilder minEvictableIdleTimeMillis(long minEvictableIdleTimeMillis) {
                this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
                return this;
            }

            public FileTransferConnectionPoolSettingsBuilder timeBetweenEvictionRunsMillis(long timeBetweenEvictionRunsMillis) {
                this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
                return this;
            }

            public FileTransferConnectionPoolSettingsBuilder connectionTimeout(int connectionTimeout) {
                this.connectionTimeout = connectionTimeout;
                return this;
            }

            public FileTransferConnectionPoolSettingsBuilder dataTimeout(int dataTimeout) {
                this.dataTimeout = dataTimeout;
                return this;
            }

            public FileTransferConnectionPoolSettingsBuilder keepAliveTimeout(int keepAliveTimeout) {
                this.keepAliveTimeout = keepAliveTimeout;
                return this;
            }

            public FileTransferConnectionPoolSettings build() {
                FileTransferConnectionPoolSettings settings = new FileTransferConnectionPoolSettings();
                settings.setMaxTotal(this.maxTotal);
                settings.setMaxPerHost(this.maxPerHost);
                settings.setMinEvictableIdleTimeMillis(this.minEvictableIdleTimeMillis);
                settings.setTimeBetweenEvictionRunsMillis(this.timeBetweenEvictionRunsMillis);
                settings.setConnectionTimeout(this.connectionTimeout);
                settings.setDataTimeout(this.dataTimeout);
                settings.setKeepAliveTimeout(this.keepAliveTimeout);
                return settings;
            }
        }
    }
}
