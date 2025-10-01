package com.integrixs.adapters.infrastructure.adapter;

import com.integrixs.adapters.config.JdbcOutboundAdapterConfig;
import com.integrixs.adapters.domain.model.*;
import com.integrixs.adapters.domain.port.OutboundAdapterPort;
import com.integrixs.shared.exceptions.AdapterException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * JDBC Outbound Adapter implementation for database operations.
 * Follows middleware convention: Outbound = sends data TO external systems.
 * Executes SQL statements against target databases.
 */
public class JdbcOutboundAdapter extends AbstractAdapter implements OutboundAdapterPort {
    private static final Logger log = LoggerFactory.getLogger(JdbcOutboundAdapter.class);

    private final JdbcOutboundAdapterConfig config;
    private HikariDataSource dataSource;

    public JdbcOutboundAdapter(JdbcOutboundAdapterConfig config) {
        super();
        this.config = config;
    }

    @Override
    protected AdapterOperationResult performInitialization() {
        log.info("Initializing JDBC outbound adapter with URL: {}", maskSensitiveUrl(config.getJdbcUrl()));

        try {
            validateConfiguration();
            dataSource = createDataSource();
        } catch (Exception e) {
            log.error("Error during initialization", e);
            return AdapterOperationResult.failure("Initialization error: " + e.getMessage());
        }

        log.info("JDBC outbound adapter initialized successfully");
        return AdapterOperationResult.success("JDBC outbound adapter initialized");
    }

    @Override
    protected AdapterOperationResult performShutdown() {
        log.info("Destroying JDBC outbound adapter");
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            dataSource = null;
        }
        return AdapterOperationResult.success("JDBC outbound adapter destroyed");
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

        // Test 1: Database connectivity
        testResults.add(performDatabaseConnectivityTest());

        // Test 2: Query validation
        if (config.getSelectQuery() != null && !config.getSelectQuery().isEmpty()) {
            testResults.add(performQueryValidationTest());
        }

        // Test 3: Table access test
        if (config.getTargetTable() != null && !config.getTargetTable().isEmpty()) {
            testResults.add(performTableAccessTest());
        }

        return AdapterOperationResult.success(testResults);
    }

    // OutboundAdapterPort implementation
    @Override
    public AdapterOperationResult send(SendRequest request) {
        try {
            return executeSqlOperation(request.getPayload());
        } catch (Exception e) {
            return AdapterOperationResult.failure("SQL execution failed: " + e.getMessage());
        }
    }

    @Override
    public CompletableFuture<AdapterOperationResult> sendAsync(SendRequest request) {
        return CompletableFuture.supplyAsync(() -> send(request));
    }

    @Override
    public AdapterOperationResult sendBatch(List<SendRequest> requests) {
        List<AdapterOperationResult> results = new ArrayList<>();
        for (SendRequest request : requests) {
            results.add(send(request));
        }

        long successCount = results.stream().filter(AdapterOperationResult::isSuccess).count();
        return AdapterOperationResult.success(
                String.format("Batch executed %d/%d operations successfully", successCount, results.size()));
    }

    @Override
    public CompletableFuture<AdapterOperationResult> sendBatchAsync(List<SendRequest> requests) {
        return CompletableFuture.supplyAsync(() -> sendBatch(requests));
    }

    @Override
    public boolean supportsBatchOperations() {
        return true;
    }

    @Override
    public int getMaxBatchSize() {
        return config.getBatchSize() > 0 ? config.getBatchSize() : 1000;
    }

    private AdapterOperationResult executeSqlOperation(Object payload) throws Exception {
        if (payload == null) {
            return AdapterOperationResult.failure("Payload cannot be null");
        }

        // Determine operation type
        String operationType = config.getOperationType();
        if (operationType == null || operationType.isEmpty()) {
            operationType = "INSERT"; // Default
        }

        switch (operationType.toUpperCase()) {
            case "INSERT":
                return executeInsert(payload);
            case "UPDATE":
                return executeUpdate(payload);
            case "DELETE":
                return executeDelete(payload);
            case "QUERY":
            case "SELECT":
                return executeQuery(payload);
            case "STORED_PROCEDURE":
                return executeStoredProcedure(payload);
            default:
                return executeCustomSql(payload);
        }
    }

    private AdapterOperationResult executeInsert(Object payload) throws Exception {
        Map<String, Object> data = extractDataMap(payload);

        String tableName = config.getTargetTable();
        if (tableName == null || tableName.isEmpty()) {
            return AdapterOperationResult.failure("Table name is required for INSERT operation");
        }

        // Build INSERT statement
        StringBuilder sql = new StringBuilder("INSERT INTO ").append(tableName).append(" (");
        StringBuilder values = new StringBuilder(" VALUES (");

        List<Object> params = new ArrayList<>();
        boolean first = true;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (!first) {
                sql.append(", ");
                values.append(", ");
            }
            sql.append(entry.getKey());
            values.append("?");
            params.add(entry.getValue());
            first = false;
        }

        sql.append(")").append(values).append(")");

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            // Set parameters
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            int rowsAffected = stmt.executeUpdate();

            return AdapterOperationResult.success(
                    String.format("Successfully inserted %d row(s) into %s", rowsAffected, tableName));
        }
    }

    private AdapterOperationResult executeUpdate(Object payload) throws Exception {
        Map<String, Object> data = extractDataMap(payload);

        String tableName = config.getTargetTable();
        if (tableName == null || tableName.isEmpty()) {
            return AdapterOperationResult.failure("Table name is required for UPDATE operation");
        }

        // Extract WHERE condition from update query
        String updateQuery = config.getUpdateQuery();
        if (updateQuery == null || updateQuery.isEmpty()) {
            return AdapterOperationResult.failure("UPDATE query is required for UPDATE operation");
        }

        // For now, use the update query directly
        // In production, you'd parse the WHERE clause from the query
        String whereClause = "1=1"; // Placeholder

        // Build UPDATE statement
        StringBuilder sql = new StringBuilder("UPDATE ").append(tableName).append(" SET ");

        List<Object> params = new ArrayList<>();
        boolean first = true;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (!first) {
                sql.append(", ");
            }
            sql.append(entry.getKey()).append(" = ?");
            params.add(entry.getValue());
            first = false;
        }

        sql.append(" WHERE ").append(whereClause);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            // Set parameters
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            int rowsAffected = stmt.executeUpdate();

            return AdapterOperationResult.success(
                    String.format("Successfully updated %d row(s) in %s", rowsAffected, tableName));
        }
    }

    private AdapterOperationResult executeDelete(Object payload) throws Exception {
        String tableName = config.getTargetTable();
        if (tableName == null || tableName.isEmpty()) {
            return AdapterOperationResult.failure("Table name is required for DELETE operation");
        }

        // Extract WHERE condition from delete query
        String deleteQuery = config.getDeleteQuery();
        if (deleteQuery == null || deleteQuery.isEmpty()) {
            return AdapterOperationResult.failure("DELETE query is required for DELETE operation");
        }

        // For now, use a placeholder WHERE clause
        // In production, you'd parse it from the delete query
        String whereClause = "1=1"; // Placeholder

        String sql = "DELETE FROM " + tableName + " WHERE " + whereClause;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int rowsAffected = stmt.executeUpdate();

            return AdapterOperationResult.success(
                    String.format("Successfully deleted %d row(s) from %s", rowsAffected, tableName));
        }
    }

    private AdapterOperationResult executeQuery(Object payload) throws Exception {
        String query = config.getSelectQuery();
        if (query == null || query.isEmpty()) {
            return AdapterOperationResult.failure("Query is required for SELECT operation");
        }

        List<Map<String, Object>> results = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    Object value = rs.getObject(i);
                    row.put(columnName, value);
                }
                results.add(row);

                // Limit results if configured
                if (config.getMaxResults() > 0 && results.size() >= config.getMaxResults()) {
                    break;
                }
            }
        }

        return AdapterOperationResult.success(results,
                String.format("Retrieved %d rows", results.size()));
    }

    private AdapterOperationResult executeStoredProcedure(Object payload) throws Exception {
        String procedureName = config.getStoredProcedureName();
        if (procedureName == null || procedureName.isEmpty()) {
            return AdapterOperationResult.failure("Stored procedure name is required");
        }

        // This is a simplified implementation
        // In production, you'd need to handle IN/OUT parameters properly
        try (Connection conn = dataSource.getConnection();
             CallableStatement stmt = conn.prepareCall("{call " + procedureName + "}")) {

            boolean hasResults = stmt.execute();

            return AdapterOperationResult.success(
                    String.format("Successfully executed stored procedure: %s", procedureName));
        }
    }

    private AdapterOperationResult executeCustomSql(Object payload) throws Exception {
        // Custom SQL not directly available in config, need to determine from operation type
        String sql = null;
        String operationType = config.getOperationType();

        if ("INSERT".equalsIgnoreCase(operationType)) {
            sql = config.getInsertQuery();
        } else if ("UPDATE".equalsIgnoreCase(operationType)) {
            sql = config.getUpdateQuery();
        } else if ("DELETE".equalsIgnoreCase(operationType)) {
            sql = config.getDeleteQuery();
        } else if ("UPSERT".equalsIgnoreCase(operationType)) {
            sql = config.getUpsertQuery();
        }

        if (sql == null || sql.isEmpty()) {
            return AdapterOperationResult.failure("No SQL query configured for operation type: " + operationType);
        }

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            boolean hasResults = stmt.execute(sql);

            if (hasResults) {
                // Handle SELECT results
                List<Map<String, Object>> results = new ArrayList<>();
                try (ResultSet rs = stmt.getResultSet()) {
                    ResultSetMetaData metaData = rs.getMetaData();
                    int columnCount = metaData.getColumnCount();

                    while (rs.next()) {
                        Map<String, Object> row = new LinkedHashMap<>();
                        for (int i = 1; i <= columnCount; i++) {
                            row.put(metaData.getColumnName(i), rs.getObject(i));
                        }
                        results.add(row);
                    }
                }
                return AdapterOperationResult.success(results,
                        String.format("Query returned %d rows", results.size()));
            } else {
                // Handle UPDATE/INSERT/DELETE
                int updateCount = stmt.getUpdateCount();
                return AdapterOperationResult.success(
                        String.format("Statement affected %d rows", updateCount));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> extractDataMap(Object payload) {
        if (payload instanceof Map) {
            return (Map<String, Object>) payload;
        }

        // For other types, create a simple map
        Map<String, Object> data = new HashMap<>();
        data.put("data", payload);
        return data;
    }

    private void validateConfiguration() throws AdapterException {
        if (config.getJdbcUrl() == null || config.getJdbcUrl().isEmpty()) {
            throw new AdapterException("JDBC URL is required");
        }
        if (config.getUsername() == null || config.getUsername().isEmpty()) {
            throw new AdapterException("Username is required");
        }
    }

    private HikariDataSource createDataSource() throws Exception {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(config.getJdbcUrl());
        hikariConfig.setUsername(config.getUsername());
        hikariConfig.setPassword(config.getPassword());

        // Set pool configuration
        hikariConfig.setMinimumIdle(config.getMinPoolSize());
        hikariConfig.setMaximumPoolSize(config.getMaxPoolSize());
        hikariConfig.setConnectionTimeout(config.getConnectionTimeoutSeconds() != null ? config.getConnectionTimeoutSeconds() * 1000L : 30000L);
        hikariConfig.setIdleTimeout(600000); // 10 minutes
        hikariConfig.setMaxLifetime(1800000); // 30 minutes

        return new HikariDataSource(hikariConfig);
    }

    private String maskSensitiveUrl(String url) {
        if (url == null) return null;
        return url.replaceAll("password=[^&;]*", "password=****");
    }

    @Override
    public String getConfigurationSummary() {
        return String.format("JDBC Outbound: %s, User: %s, Operation: %s",
                maskSensitiveUrl(config.getJdbcUrl()),
                config.getUsername(),
                config.getOperationType());
    }

    @Override
    public AdapterMetadata getMetadata() {
        return AdapterMetadata.builder()
                .adapterType(AdapterConfiguration.AdapterTypeEnum.JDBC)
                .adapterMode(AdapterConfiguration.AdapterModeEnum.OUTBOUND)
                .description("JDBC Outbound adapter - executes SQL operations on databases")
                .version("1.0.0")
                .supportsBatch(true)
                .supportsAsync(true)
                .build();
    }

    @Override
    protected AdapterConfiguration.AdapterTypeEnum getAdapterType() {
        return AdapterConfiguration.AdapterTypeEnum.JDBC;
    }

    @Override
    protected AdapterConfiguration.AdapterModeEnum getAdapterMode() {
        return AdapterConfiguration.AdapterModeEnum.OUTBOUND;
    }

    // Helper methods for connection testing
    private AdapterOperationResult performDatabaseConnectivityTest() {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            String dbInfo = String.format("Database: %s %s",
                    metaData.getDatabaseProductName(),
                    metaData.getDatabaseProductVersion());
            return AdapterOperationResult.success(dbInfo);
        } catch (Exception e) {
            return AdapterOperationResult.failure(
                    "Failed to connect to database: " + e.getMessage());
        }
    }

    private AdapterOperationResult performQueryValidationTest() {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(config.getSelectQuery())) {

            // Just prepare the statement to validate syntax
            return AdapterOperationResult.success("Query syntax is valid");
        } catch (Exception e) {
            return AdapterOperationResult.failure(
                    "Query validation failed: " + e.getMessage());
        }
    }

    private AdapterOperationResult performTableAccessTest() {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();

            try (ResultSet tables = metaData.getTables(null, null, config.getTargetTable(), null)) {
                if (tables.next()) {
                    return AdapterOperationResult.success(
                            "Table " + config.getTargetTable() + " exists and is accessible");
                } else {
                    return AdapterOperationResult.failure(
                            "Table " + config.getTargetTable() + " not found");
                }
            }
        } catch (Exception e) {
            return AdapterOperationResult.failure(
                    "Table access test failed: " + e.getMessage());
        }
    }
}