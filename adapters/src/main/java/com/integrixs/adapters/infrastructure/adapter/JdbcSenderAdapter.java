package com.integrixs.adapters.infrastructure.adapter;

import com.integrixs.adapters.core.AdapterException;

import com.integrixs.adapters.domain.model.*;
import java.util.concurrent.CompletableFuture;
import com.integrixs.adapters.domain.port.SenderAdapterPort;
import com.integrixs.adapters.config.JdbcSenderAdapterConfig;
import javax.sql.DataSource;
import java.sql.*;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.Collection;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;

/**
 * JDBC Sender Adapter implementation for database polling and data retrieval (INBOUND).
 * Follows middleware convention: Sender = receives data FROM external systems.
 * Supports SELECT operations with polling, pagination, and incremental data processing.
 */
@Slf4j
public class JdbcSenderAdapter extends AbstractAdapter implements SenderAdapterPort {
    
    private final JdbcSenderAdapterConfig config;
    private HikariDataSource dataSource;
    private Object lastProcessedValue; // For incremental polling
    
    public JdbcSenderAdapter(JdbcSenderAdapterConfig config) {
        super();
        this.config = config;
    }
    
    @Override
    protected AdapterOperationResult performInitialization() {
        log.info("Initializing JDBC sender adapter (inbound) with URL: {}", maskSensitiveUrl(config.getJdbcUrl()));
        
        try {
            validateConfiguration();
            dataSource = createDataSource();
        } catch (Exception e) {
            log.error("Error during initialization", e);
            return AdapterOperationResult.failure("Initialization error: " + e.getMessage());
        }
        
        log.info("JDBC sender adapter initialized successfully");
        return AdapterOperationResult.success("JDBC sender adapter initialized successfully");
    }
    
    @Override
    protected AdapterOperationResult performShutdown() {
        log.info("Destroying JDBC sender adapter");
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            dataSource = null;
        }
        return AdapterOperationResult.success("JDBC sender adapter shutdown successfully");
    }
    
    @Override
    protected AdapterOperationResult performConnectionTest() {
        List<AdapterOperationResult> testResults = new ArrayList<>();
        
        // Test 1: Basic connectivity
        testResults.add(executeTest(() -> {
            try (Connection conn = dataSource.getConnection()) {
                if (conn.isValid(5)) {
                    return AdapterOperationResult.success("Database connection valid");
                } else {
                    return AdapterOperationResult.failure("Database connection invalid");
                }
            } catch (Exception e) {
                return AdapterOperationResult.failure("Connection test failed: " + e.getMessage());
            }
        }));
        
        // Test 2: Database schema validation
        testResults.add(executeTest(() -> {
            try (Connection conn = dataSource.getConnection()) {
                DatabaseMetaData metaData = conn.getMetaData();
                
                String databaseName = metaData.getDatabaseProductName();
                String databaseVersion = metaData.getDatabaseProductVersion();
                log.debug("Connected to {} version {}", databaseName, databaseVersion);
                return AdapterOperationResult.success(String.format("Connected to %s v%s", databaseName, databaseVersion));
            } catch (Exception e) {
                return AdapterOperationResult.failure("Schema validation failed: " + e.getMessage());
            }
        }));
        
        // Test 3: SELECT Query validation test
        if (config.getSelectQuery() != null && !config.getSelectQuery().trim().isEmpty()) {
            testResults.add(executeTest(() -> {
                try (Connection conn = dataSource.getConnection()) {
                    // Test query preparation and execution with LIMIT 0 (doesn't return data)
                    String testQuery = addLimitToQuery(config.getSelectQuery(), 0);
                    
                    try (PreparedStatement stmt = conn.prepareStatement(testQuery)) {
                        stmt.setQueryTimeout(5); // Short timeout for test
                        stmt.executeQuery(); // Execute but don't fetch results
                        return AdapterOperationResult.success("SELECT query validated");
                    }
                } catch (Exception e) {
                    return AdapterOperationResult.failure("Query validation failed: " + e.getMessage());
                }
            }));
        }
        
        // Combine test results
        boolean allPassed = testResults.stream().allMatch(AdapterOperationResult::isSuccess);
        if (allPassed) {
            return AdapterOperationResult.success("All connection tests passed");
        } else {
            String failedTests = testResults.stream()
                    .filter(r -> !r.isSuccess())
                    .map(AdapterOperationResult::getMessage)
                    .reduce((a, b) -> a + "; " + b)
                    .orElse("Unknown failures");
            return AdapterOperationResult.failure("Some tests failed: " + failedTests);
        }
    }
    
    
    private AdapterOperationResult pollForData() throws Exception {
        if (config.getSelectQuery() == null || config.getSelectQuery().trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterConfiguration.AdapterTypeEnum.JDBC, "SELECT query not configured");
        }
        
        List<Map<String, Object>> results = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            conn.setReadOnly(config.isReadOnly());
            conn.setAutoCommit(config.isAutoCommit());
            
            String query = buildIncrementalQuery();
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setQueryTimeout(config.getQueryTimeoutSeconds());
                
                if (config.getFetchSize() != null) {
                    stmt.setFetchSize(config.getFetchSize());
                }
                
                if (config.getMaxResults() != null) {
                    stmt.setMaxRows(config.getMaxResults());
                }
                // Set incremental parameter if configured
                if (config.getIncrementalColumn() != null && lastProcessedValue != null) {
                    stmt.setObject(1, lastProcessedValue);
                }
                
                try (ResultSet rs = stmt.executeQuery()) {
                    ResultSetMetaData metaData = rs.getMetaData();
                    int columnCount = metaData.getColumnCount();
                    
                    while (rs.next()) {
                        Map<String, Object> row = new HashMap<>();
                        for (int i = 1; i <= columnCount; i++) {
                            String columnName = metaData.getColumnName(i);
                            Object value = rs.getObject(i);
                            row.put(columnName, value);
                        }
                        results.add(row);
                        
                        // Update last processed value for incremental polling
                        if (config.getIncrementalColumn() != null) {
                            Object incrementalValue = row.get(config.getIncrementalColumn());
                            if (incrementalValue != null) {
                                lastProcessedValue = incrementalValue;
                            }
                        }
                    }
                }
            }
        }
        
        log.info("JDBC sender adapter polled {} records from database", results.size());
        return AdapterOperationResult.success(results, 
                String.format("Retrieved %d records from database", results.size()));
    }
    
    private String buildIncrementalQuery() {
        String baseQuery = config.getSelectQuery();
        
        if (config.getIncrementalColumn() != null && lastProcessedValue != null) {
            // Add WHERE clause for incremental processing
            if (baseQuery.toUpperCase().contains("WHERE")) {
                baseQuery += " AND " + config.getIncrementalColumn() + " > ?";
            } else {
                baseQuery += " WHERE " + config.getIncrementalColumn() + " > ?";
            }
        }
        
        // Add ORDER BY for incremental column
        if (config.getIncrementalColumn() != null) {
            if (!baseQuery.toUpperCase().contains("ORDER BY")) {
                baseQuery += " ORDER BY " + config.getIncrementalColumn() + " ASC";
            }
        }
        
        return baseQuery;
    }
    
    private String addLimitToQuery(String query, int limit) {
        // Simple LIMIT addition - this would need to be database-specific in production
        return query + " LIMIT " + limit;
    }
    
    private void validateConfiguration() throws AdapterException.ConfigurationException {
        if (config.getJdbcUrl() == null || config.getJdbcUrl().trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterConfiguration.AdapterTypeEnum.JDBC, "JDBC URL is required");
        }
        if (config.getDriverClass() == null || config.getDriverClass().trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterConfiguration.AdapterTypeEnum.JDBC, "JDBC driver class is required");
        }
        if (config.getSelectQuery() == null || config.getSelectQuery().trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterConfiguration.AdapterTypeEnum.JDBC, "SELECT query is required for JDBC sender adapter");
        }
    }
    
    private HikariDataSource createDataSource() throws Exception {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(config.getJdbcUrl());
        hikariConfig.setDriverClassName(config.getDriverClass());
        hikariConfig.setUsername(config.getUsername());
        hikariConfig.setPassword(config.getPassword());
        
        // Connection pool settings
        hikariConfig.setMinimumIdle(config.getMinPoolSize());
        hikariConfig.setMaximumPoolSize(config.getMaxPoolSize());
        hikariConfig.setConnectionTimeout(config.getConnectionTimeoutSeconds() * 1000L);
        
        // Connection properties
        if (config.getConnectionProperties() != null && !config.getConnectionProperties().trim().isEmpty()) {
            Properties props = parseConnectionProperties(config.getConnectionProperties());
            hikariConfig.setDataSourceProperties(props);
        }
        
        return new HikariDataSource(hikariConfig);
    }
    
    private Properties parseConnectionProperties(String connectionProperties) {
        Properties props = new Properties();
        String[] pairs = connectionProperties.split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                props.setProperty(keyValue[0].trim(), keyValue[1].trim());
            }
        }
        return props;
    }
    
    private String maskSensitiveUrl(String url) {
        if (url == null) return null;
        return url.replaceAll("password=[^;&]*", "password=***");
    }
    public long getPollingInterval() {
        return config.getPollingInterval() != null ? config.getPollingInterval() : 0;
    }
    
    @Override
    public String getConfigurationSummary() {
        return String.format("JDBC Sender (Inbound): %s, Query: %s, Polling: %dms", 
                maskSensitiveUrl(config.getJdbcUrl()),
                config.getSelectQuery() != null ? config.getSelectQuery().substring(0, Math.min(50, config.getSelectQuery().length())) + "..." : "Not configured",
                config.getPollingInterval() != null ? config.getPollingInterval() : 0);
    }
    
    // SenderAdapterPort implementation
    @Override
    public AdapterOperationResult fetch(FetchRequest request) {
        try {
            return pollForData();
        } catch (Exception e) {
            return AdapterOperationResult.failure("Fetch failed: " + e.getMessage());
        }
    }
    
    @Override
    public CompletableFuture<AdapterOperationResult> fetchAsync(FetchRequest request) {
        return CompletableFuture.supplyAsync(() -> fetch(request));
    }
    
    @Override
    public void startListening(DataReceivedCallback callback) {
        // Not implemented for this adapter type
        throw new UnsupportedOperationException("This adapter does not support push-based listening");
    }
    
    @Override
    public void stopListening() {
        // Not implemented
    }
    
    @Override
    public boolean isListening() {
        return false;
    }
    public void startPolling(long intervalMillis) {
        // Implement if polling is supported
        throw new UnsupportedOperationException("Polling not implemented");
    }
    public void stopPolling() {
        // Not implemented
    }
    
    public void setDataReceivedCallback(DataReceivedCallback callback) {
        // Implement if callbacks are supported
    }
    public AdapterMetadata getMetadata() {
        return AdapterMetadata.builder()
                .adapterType(AdapterConfiguration.AdapterTypeEnum.JDBC)
                .adapterMode(AdapterConfiguration.AdapterModeEnum.SENDER)
                .description("JDBC Sender adapter implementation")
                .version("1.0.0")
                .supportsBatch(false)
                .supportsAsync(true)
                .build();
    }
    
    @Override
    protected AdapterOperationResult performStart() {
        return AdapterOperationResult.success("Started");
    }
    
    @Override
    protected AdapterOperationResult performStop() {
        return AdapterOperationResult.success("Stopped");
    }
    
    private AdapterOperationResult executeTest(java.util.concurrent.Callable<AdapterOperationResult> test) {
        try {
            return test.call();
        } catch (Exception e) {
            return AdapterOperationResult.failure("Test execution failed: " + e.getMessage());
        }
    }

    @Override
    protected AdapterConfiguration.AdapterTypeEnum getAdapterType() {
        return AdapterConfiguration.AdapterTypeEnum.JDBC;
    }

    @Override
    protected AdapterConfiguration.AdapterModeEnum getAdapterMode() {
        return AdapterConfiguration.AdapterModeEnum.SENDER;
    }

}
