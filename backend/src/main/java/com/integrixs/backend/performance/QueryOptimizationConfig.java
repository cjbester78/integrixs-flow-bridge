package com.integrixs.backend.performance;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Properties;

/**
 * Database query optimization configuration.
 * Enhances Hibernate and connection pool settings for optimal performance.
 */
@Slf4j
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {
    "com.integrixs.data.repository",
    "com.integrixs.backend.domain.repository"
})
public class QueryOptimizationConfig {

    @Value("$ {spring.jpa.properties.hibernate.jdbc.batch_size:50}")
    private int batchSize;

    @Value("$ {spring.jpa.properties.hibernate.order_inserts:true}")
    private boolean orderInserts;

    @Value("$ {spring.jpa.properties.hibernate.order_updates:true}")
    private boolean orderUpdates;

    @Value("$ {spring.jpa.properties.hibernate.jdbc.batch_versioned_data:true}")
    private boolean batchVersionedData;

    @Value("$ {spring.datasource.hikari.maximum - pool - size:20}")
    private int maxPoolSize;

    @Value("$ {spring.datasource.hikari.minimum - idle:5}")
    private int minIdle;

    /**
     * Hibernate properties customizer for query optimization.
     */
    @Bean
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer() {
        return hibernateProperties -> {
            // Batch processing optimization
            hibernateProperties.put(AvailableSettings.STATEMENT_BATCH_SIZE, batchSize);
            hibernateProperties.put(AvailableSettings.ORDER_INSERTS, orderInserts);
            hibernateProperties.put(AvailableSettings.ORDER_UPDATES, orderUpdates);
            hibernateProperties.put(AvailableSettings.BATCH_VERSIONED_DATA, batchVersionedData);

            // Query plan cache
            hibernateProperties.put(AvailableSettings.QUERY_PLAN_CACHE_MAX_SIZE, 2048);
            hibernateProperties.put(AvailableSettings.QUERY_PLAN_CACHE_PARAMETER_METADATA_MAX_SIZE, 128);

            // Second - level cache configuration
            hibernateProperties.put(AvailableSettings.USE_SECOND_LEVEL_CACHE, true);
            hibernateProperties.put(AvailableSettings.USE_QUERY_CACHE, true);
            hibernateProperties.put(AvailableSettings.CACHE_REGION_PREFIX, "integrix");

            // Statement caching
            hibernateProperties.put("hibernate.jdbc.use_scrollable_resultset", true);
            hibernateProperties.put("hibernate.jdbc.wrap_result_sets", true);

            // Fetch optimization
            hibernateProperties.put(AvailableSettings.DEFAULT_BATCH_FETCH_SIZE, 16);
            hibernateProperties.put(AvailableSettings.USE_STREAMS_FOR_BINARY, true);

            // Statistics for monitoring
            hibernateProperties.put(AvailableSettings.GENERATE_STATISTICS, false); // Enable in dev only

            // Connection handling
            hibernateProperties.put(AvailableSettings.CONNECTION_HANDLING, "DELAYED_ACQUISITION_AND_RELEASE_AFTER_TRANSACTION");

            log.info("Configured Hibernate with optimized query settings: batch_size = {}, order_inserts = {}, order_updates = {}",
                batchSize, orderInserts, orderUpdates);
        };
    }

    /**
     * Optimized data source configuration.
     */
    @Bean
    @Primary
    public DataSource optimizedDataSource(
            @Value("$ {spring.datasource.url}") String jdbcUrl,
            @Value("$ {spring.datasource.username}") String username,
            @Value("$ {spring.datasource.password}") String password,
            @Value("$ {spring.datasource.driver - class - name:org.postgresql.Driver}") String driverClassName) {

        HikariConfig config = new HikariConfig();

        // Basic configuration
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(driverClassName);

        // Pool configuration
        config.setMaximumPoolSize(maxPoolSize);
        config.setMinimumIdle(minIdle);
        config.setIdleTimeout(300000); // 5 minutes
        config.setMaxLifetime(1800000); // 30 minutes
        config.setConnectionTimeout(30000); // 30 seconds

        // Performance optimizations
        config.setAutoCommit(false);
        config.setConnectionTestQuery("SELECT 1");
        config.setPoolName("IntegrixHikariPool");

        // PostgreSQL specific optimizations
        if(driverClassName.contains("postgresql")) {
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");
            config.addDataSourceProperty("reWriteBatchedInserts", "true");
            config.addDataSourceProperty("cacheResultSetMetadata", "true");
            config.addDataSourceProperty("cacheServerConfiguration", "true");
            config.addDataSourceProperty("elideSetAutoCommits", "true");
            config.addDataSourceProperty("maintainTimeStats", "false");
        }

        // MySQL specific optimizations
        if(driverClassName.contains("mysql")) {
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");
            config.addDataSourceProperty("useLocalSessionState", "true");
            config.addDataSourceProperty("rewriteBatchedStatements", "true");
            config.addDataSourceProperty("cacheResultSetMetadata", "true");
            config.addDataSourceProperty("cacheServerConfiguration", "true");
            config.addDataSourceProperty("elideSetAutoCommits", "true");
            config.addDataSourceProperty("maintainTimeStats", "false");
        }

        log.info("Configured optimized HikariCP data source with pool size: {} - {}", minIdle, maxPoolSize);

        return new HikariDataSource(config);
    }

    /**
     * Query hints for common operations.
     */
    @Bean
    public QueryHints queryHints() {
        return new QueryHints();
    }

    /**
     * Query hints configuration.
     */
    public static class QueryHints {

        public static final String HINT_CACHEABLE = "org.hibernate.cacheable";
        public static final String HINT_CACHE_REGION = "org.hibernate.cacheRegion";
        public static final String HINT_FETCH_SIZE = "org.hibernate.fetchSize";
        public static final String HINT_READONLY = "org.hibernate.readOnly";
        public static final String HINT_TIMEOUT = "jakarta.persistence.query.timeout";
        public static final String HINT_COMMENT = "org.hibernate.comment";

        /**
         * Get standard read - only query hints.
         */
        public Map<String, Object> getReadOnlyHints() {
            return Map.of(
                HINT_READONLY, true,
                HINT_CACHEABLE, true,
                HINT_FETCH_SIZE, 100
           );
        }

        /**
         * Get batch processing hints.
         */
        public Map<String, Object> getBatchHints() {
            return Map.of(
                HINT_FETCH_SIZE, 1000,
                HINT_TIMEOUT, 60000 // 60 seconds
           );
        }

        /**
         * Get cached query hints.
         */
        public Map<String, Object> getCachedHints(String region) {
            return Map.of(
                HINT_CACHEABLE, true,
                HINT_CACHE_REGION, region,
                HINT_READONLY, true
           );
        }
    }
}
