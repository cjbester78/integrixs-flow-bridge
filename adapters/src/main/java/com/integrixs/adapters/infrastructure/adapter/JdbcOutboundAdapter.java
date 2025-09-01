package com.integrixs.adapters.infrastructure.adapter;

import com.integrixs.adapters.core.AdapterException;

import com.integrixs.adapters.domain.model.*;
import com.integrixs.adapters.domain.port.OutboundAdapterPort;
import com.integrixs.adapters.config.JdbcOutboundAdapterConfig;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * JDBC Receiver Adapter implementation for database operations (OUTBOUND).
 * Follows middleware convention: Outbound = sends data TO external systems.
 * Supports INSERT, UPDATE, DELETE operations with batch processing and transaction management.
 */
@Slf4j
public class JdbcOutboundAdapter extends AbstractAdapter implements OutboundAdapterPort {
    
    private final JdbcOutboundAdapterConfig config;
    private HikariDataSource dataSource;
    
    public JdbcOutboundAdapter(JdbcOutboundAdapterConfig config) {
        super();
        this.config = config;
    }
    
    @Override
    protected AdapterOperationResult performInitialization() {
        log.info("Initializing JDBC outbound adapter (outbound) with URL: {}", maskSensitiveUrl(config.getJdbcUrl()));
        
        try {
            validateConfiguration();
            dataSource = createDataSource();
        } catch (Exception e) {
            log.error("Error during initialization", e);
            return AdapterOperationResult.failure("Initialization error: " + e.getMessage());
        }
        
        log.info("JDBC outbound adapter initialized successfully");
        return AdapterOperationResult.success("JDBC outbound adapter initialized successfully");
    }
    
    @Override
    protected AdapterOperationResult performShutdown() {
        log.info("Destroying JDBC outbound adapter");
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            dataSource = null;
        }
        return AdapterOperationResult.success("JDBC outbound adapter shutdown successfully");
    }
    
    @Override
    protected AdapterOperationResult performStart() {
        return AdapterOperationResult.success("Started");
    }
    
    @Override
    protected AdapterOperationResult performStop() {
        return AdapterOperationResult.success("Stopped");
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
        
        // Test 3: Query validation test
        if (config.getInsertQuery() != null && !config.getInsertQuery().trim().isEmpty()) {
            testResults.add(executeTest(() -> {
                try (Connection conn = dataSource.getConnection()) {
                    // Test query preparation (doesn't execute)
                    try (PreparedStatement stmt = conn.prepareStatement(config.getInsertQuery())) {
                        ParameterMetaData paramMetaData = stmt.getParameterMetaData();
                        int paramCount = paramMetaData.getParameterCount();
                        return AdapterOperationResult.success(String.format("Insert query valid with %d parameters", paramCount));
                    }
                } catch (Exception e) {
                    return AdapterOperationResult.failure("Query validation failed: " + e.getMessage());
                }
            }));
        }
        
        // Test 4: Transaction support test
        testResults.add(executeTest(() -> {
            try (Connection conn = dataSource.getConnection()) {
                boolean autoCommit = conn.getAutoCommit();
                boolean supportsTransactions = conn.getMetaData().supportsTransactions();
                String message = String.format("Transaction support: %s, Auto-commit: %s", 
                                              supportsTransactions, autoCommit);
                return AdapterOperationResult.success(message);
            } catch (Exception e) {
                return AdapterOperationResult.failure("Transaction test failed: " + e.getMessage());
            }
        }));
        
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
    
    // OutboundAdapterPort implementation
    @Override
    public AdapterOperationResult send(SendRequest request) {
        try {
            return performSend(request.getPayload());
        } catch (Exception e) {
            return AdapterOperationResult.failure("Send failed: " + e.getMessage());
        }
    }
    
    @Override
    public CompletableFuture<AdapterOperationResult> sendAsync(SendRequest request) {
        return CompletableFuture.supplyAsync(() -> send(request));
    }
    
    @Override
    public AdapterOperationResult sendBatch(List<SendRequest> requests) {
        try {
            return performBatchSend(requests);
        } catch (Exception e) {
            return AdapterOperationResult.failure("Batch send failed: " + e.getMessage());
        }
    }
    
    @Override
    public CompletableFuture<AdapterOperationResult> sendBatchAsync(List<SendRequest> requests) {
        return CompletableFuture.supplyAsync(() -> sendBatch(requests));
    }
    public boolean isBatchingEnabled() {
        return true;
    }
    public int getBatchSize() {
        return config.getBatchSize() != null ? config.getBatchSize() : 1000;
    }
    
    protected AdapterOperationResult performSend(Object payload) throws Exception {
        // For JDBC Receiver (outbound), this method sends data TO database
        return insertOrUpdateData(payload);
    }
    
    private AdapterOperationResult performBatchSend(List<SendRequest> requests) throws Exception {
        if (config.isEnableBatchProcessing() != null && !config.isEnableBatchProcessing()) {
            // Process one by one if batch processing is disabled
            List<AdapterOperationResult> results = new ArrayList<>();
            for (SendRequest request : requests) {
                results.add(send(request));
            }
            
            long successCount = results.stream().filter(AdapterOperationResult::isSuccess).count();
            return AdapterOperationResult.success(
                    String.format("Processed %d/%d records successfully", successCount, results.size()))
                    .withRecordsProcessed(successCount);
        }
        
        // Batch processing
        Connection conn = null;
        PreparedStatement stmt = null;
        int totalProcessed = 0;
        
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            
            stmt = conn.prepareStatement(config.getInsertQuery());
            
            for (SendRequest request : requests) {
                populateStatement(stmt, request.getPayload());
                stmt.addBatch();
                
                if ((totalProcessed + 1) % config.getBatchSize() == 0) {
                    int[] results = stmt.executeBatch();
                    totalProcessed += results.length;
                    stmt.clearBatch();
                }
            }
            
            // Execute remaining batch
            if (stmt != null) {
                int[] results = stmt.executeBatch();
                totalProcessed += results.length;
            }
            
            conn.commit();
            
            return AdapterOperationResult.success(
                    String.format("Batch processed %d records successfully", totalProcessed))
                    .withRecordsProcessed(totalProcessed);
        } catch (Exception e) {
            if (conn != null) {
                conn.rollback();
            }
            throw new AdapterException.ProcessingException(AdapterConfiguration.AdapterTypeEnum.JDBC,
                    "Batch processing failed: " + e.getMessage(), e);
        } finally {
            closeResources(null, stmt, conn);
        }
    }
    
    private AdapterOperationResult insertOrUpdateData(Object payload) throws Exception {
        if (payload == null) {
            throw new AdapterException.ValidationException(AdapterConfiguration.AdapterTypeEnum.JDBC, "Payload cannot be null");
        }
        
        String operationType = config.getOperationType();
        String query = null;
        
        switch (operationType.toUpperCase()) {
            case "INSERT":
                query = config.getInsertQuery();
                break;
            case "UPDATE":
                query = config.getUpdateQuery();
                break;
            case "DELETE":
                query = config.getDeleteQuery();
                break;
            case "UPSERT":
                return performUpsert(payload);
            case "STORED_PROCEDURE":
                return executeStoredProcedure(payload);
            default:
                throw new AdapterException.ConfigurationException(AdapterConfiguration.AdapterTypeEnum.JDBC,
                        "Invalid operation type: " + operationType);
        }
        
        if (query == null || query.trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterConfiguration.AdapterTypeEnum.JDBC,
                    "No query configured for operation type: " + operationType);
        }
        
        return executeQuery(query, payload);
    }
    
    private AdapterOperationResult executeQuery(String query, Object payload) throws Exception {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(config.isUseTransaction() == null || !config.isUseTransaction());
            
            stmt = conn.prepareStatement(query);
            populateStatement(stmt, payload);
            
            int affectedRows = stmt.executeUpdate();
            
            if (config.isUseTransaction() != null && config.isUseTransaction()) {
                conn.commit();
            }
            
            return AdapterOperationResult.success(
                    String.format("Query executed successfully, %d rows affected", affectedRows))
                    .withRecordsProcessed(affectedRows);
        } catch (Exception e) {
            if (conn != null && config.isUseTransaction() != null && config.isUseTransaction()) {
                conn.rollback();
            }
            throw new AdapterException.ProcessingException(AdapterConfiguration.AdapterTypeEnum.JDBC,
                    "Query execution failed: " + e.getMessage(), e);
        } finally {
            closeResources(null, stmt, conn);
        }
    }
    
    private AdapterOperationResult performUpsert(Object payload) throws Exception {
        // Try update first, if no rows affected, perform insert
        AdapterOperationResult updateResult = executeQuery(config.getUpdateQuery(), payload);
        
        if (updateResult.getRecordsProcessed() == 0) {
            // No rows updated, perform insert
            return executeQuery(config.getInsertQuery(), payload);
        }
        
        return updateResult;
    }
    
    private AdapterOperationResult executeStoredProcedure(Object payload) throws Exception {
        Connection conn = null;
        CallableStatement stmt = null;
        
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(config.isUseTransaction() == null || !config.isUseTransaction());
            
            stmt = conn.prepareCall(config.getStoredProcedureName());
            populateStatement(stmt, payload);
            
            boolean hasResultSet = stmt.execute();
            
            Map<String, Object> resultData = new HashMap<>();
            
            // Handle output parameters if any
            if (config.getOutputParameters() != null && !config.getOutputParameters().isEmpty()) {
                for (Map.Entry<String, String> param : config.getOutputParameters().entrySet()) {
                    Object value = stmt.getObject(param.getKey());
                    resultData.put(param.getKey(), value);
                }
            }
            
            if (config.isUseTransaction() != null && config.isUseTransaction()) {
                conn.commit();
            }
            
            return AdapterOperationResult.success(resultData);
        } catch (Exception e) {
            if (conn != null && config.isUseTransaction() != null && config.isUseTransaction()) {
                conn.rollback();
            }
            throw new AdapterException.ProcessingException(AdapterConfiguration.AdapterTypeEnum.JDBC,
                    "Stored procedure execution failed: " + e.getMessage(), e);
        } finally {
            closeResources(null, stmt, conn);
        }
    }
    
    private void populateStatement(PreparedStatement stmt, Object payload) throws SQLException {
        if (payload instanceof Map) {
            Map<?, ?> dataMap = (Map<?, ?>) payload;
            
            // Use parameter mapping if configured
            if (config.getParameterMappings() != null && !config.getParameterMappings().isEmpty()) {
                for (Map.Entry<String, Integer> mapping : config.getParameterMappings().entrySet()) {
                    Object value = dataMap.get(mapping.getKey());
                    stmt.setObject(mapping.getValue(), value);
                }
            } else {
                // Use positional parameters
                int index = 1;
                for (Object value : dataMap.values()) {
                    stmt.setObject(index++, value);
                }
            }
        } else if (payload instanceof List) {
            List<?> dataList = (List<?>) payload;
            int index = 1;
            for (Object value : dataList) {
                stmt.setObject(index++, value);
            }
        } else {
            // Single value
            stmt.setObject(1, payload);
        }
    }
    
    private HikariDataSource createDataSource() throws Exception {
        HikariConfig hikariConfig = new HikariConfig();
        
        // Basic configuration
        hikariConfig.setJdbcUrl(config.getJdbcUrl());
        hikariConfig.setUsername(config.getUsername());
        hikariConfig.setPassword(config.getPassword());
        
        // Driver class
        if (config.getDriverClassName() != null && !config.getDriverClassName().isEmpty()) {
            hikariConfig.setDriverClassName(config.getDriverClassName());
        }
        
        // Connection pool settings
        hikariConfig.setMaximumPoolSize(config.getMaxPoolSize());
        hikariConfig.setMinimumIdle(config.getMinIdle());
        hikariConfig.setConnectionTimeout(config.getConnectionTimeout() * 1000L);
        hikariConfig.setIdleTimeout(config.getIdleTimeout() * 1000L);
        hikariConfig.setMaxLifetime(config.getMaxLifetime() * 1000L);
        
        // Additional properties
        if (config.getConnectionProperties() != null && !config.getConnectionProperties().isEmpty()) {
            for (Map.Entry<String, String> prop : config.getConnectionProperties().entrySet()) {
                hikariConfig.addDataSourceProperty(prop.getKey(), prop.getValue());
            }
        }
        
        // Test query
        if (config.getValidationQuery() != null && !config.getValidationQuery().isEmpty()) {
            hikariConfig.setConnectionTestQuery(config.getValidationQuery());
        }
        
        hikariConfig.setPoolName("JdbcReceiverPool-" + (config.getConnectionName() != null ? config.getConnectionName() : "default"));
        
        return new HikariDataSource(hikariConfig);
    }
    
    private void validateConfiguration() throws AdapterException.ConfigurationException {
        if (config.getJdbcUrl() == null || config.getJdbcUrl().trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterConfiguration.AdapterTypeEnum.JDBC, "JDBC URL is required");
        }
        
        String operationType = config.getOperationType();
        if (operationType == null || operationType.trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterConfiguration.AdapterTypeEnum.JDBC, "Operation type is required");
        }
        
        // Validate required queries based on operation type
        switch (operationType.toUpperCase()) {
            case "INSERT":
                if (config.getInsertQuery() == null || config.getInsertQuery().trim().isEmpty()) {
                    throw new AdapterException.ConfigurationException(AdapterConfiguration.AdapterTypeEnum.JDBC, "Insert query is required for INSERT operation");
                }
                break;
            case "UPDATE":
                if (config.getUpdateQuery() == null || config.getUpdateQuery().trim().isEmpty()) {
                    throw new AdapterException.ConfigurationException(AdapterConfiguration.AdapterTypeEnum.JDBC, "Update query is required for UPDATE operation");
                }
                break;
            case "DELETE":
                if (config.getDeleteQuery() == null || config.getDeleteQuery().trim().isEmpty()) {
                    throw new AdapterException.ConfigurationException(AdapterConfiguration.AdapterTypeEnum.JDBC, "Delete query is required for DELETE operation");
                }
                break;
            case "UPSERT":
                if ((config.getInsertQuery() == null || config.getInsertQuery().trim().isEmpty()) ||
                    (config.getUpdateQuery() == null || config.getUpdateQuery().trim().isEmpty())) {
                    throw new AdapterException.ConfigurationException(AdapterConfiguration.AdapterTypeEnum.JDBC, "Both insert and update queries are required for UPSERT operation");
                }
                break;
            case "STORED_PROCEDURE":
                if (config.getStoredProcedureName() == null || config.getStoredProcedureName().trim().isEmpty()) {
                    throw new AdapterException.ConfigurationException(AdapterConfiguration.AdapterTypeEnum.JDBC, "Stored procedure name is required");
                }
                break;
        }
    }
    
    private void closeResources(ResultSet rs, Statement stmt, Connection conn) {
        if (rs != null) {
            try { rs.close(); } catch (SQLException ignored) {}
        }
        if (stmt != null) {
            try { stmt.close(); } catch (SQLException ignored) {}
        }
        if (conn != null) {
            try { conn.close(); } catch (SQLException ignored) {}
        }
    }
    
    private String maskSensitiveUrl(String url) {
        if (url == null) return null;
        // Mask password in JDBC URL
        return url.replaceAll("password=[^;&]*", "password=***")
                  .replaceAll("user=[^;&]*", "user=***");
    }
    public long getTimeout() {
        // JDBC receivers typically don't poll
        return 0;
    }
    
    @Override
    public String getConfigurationSummary() {
        return String.format("JDBC Receiver (Outbound): %s, Operation: %s, Batch: %s, Pool: %d-%d", 
                maskSensitiveUrl(config.getJdbcUrl()),
                config.getOperationType(),
                config.isEnableBatchProcessing() ? "Enabled" : "Disabled",
                config.getMinIdle(),
                config.getMaxPoolSize());
    }
    
    @Override
    public AdapterMetadata getMetadata() {
        return AdapterMetadata.builder()
                .adapterType(AdapterConfiguration.AdapterTypeEnum.JDBC)
                .adapterMode(AdapterConfiguration.AdapterModeEnum.OUTBOUND)
                .description("JDBC Receiver Adapter - sends data to databases")
                .version("1.0.0")
                .supportsBatch(true)
                .supportsAsync(true)
                .build();
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
        return AdapterConfiguration.AdapterModeEnum.OUTBOUND;
    }
    
    @Override
    public boolean supportsBatchOperations() {
        return true; // JDBC supports batch operations
    }
    
    @Override
    public int getMaxBatchSize() {
        return config.getBatchSize() != null ? config.getBatchSize() : 1000;
    }

}
