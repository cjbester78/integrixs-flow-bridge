package com.integrixs.adapters.config;

import java.util.Map;

/**
 * Configuration for JDBC Receiver Adapter(Frontend).
 * In middleware terminology, outbound adapters send data TO external target systems.
 * This configuration focuses on connecting TO target databases to insert/update data.
 */
public class JdbcOutboundAdapterConfig {

    // Target Database Connection Details
    private String jdbcUrl; // Connection URL to target database
    private String driverClass;
    private String username;
    private String password;
    private String databaseType; // MYSQL, POSTGRESQL, ORACLE, SQLSERVER, etc.

    // Connection Pool Settings for target database
    private Integer minPoolSize; // Global setting available: jdbc.minPoolSize
    private Integer maxPoolSize; // Global setting available: jdbc.maxPoolSize
    private Integer connectionTimeoutSeconds; // Global setting available: jdbc.connectionTimeoutSeconds

    // Data Writing Configuration
    private String insertQuery; // SQL for inserting new records
    private String updateQuery; // SQL for updating existing records
    private String deleteQuery; // SQL for deleting records
    private String upsertQuery; // SQL for insert - or - update operations
    private Integer queryTimeoutSeconds; // Global setting available: jdbc.queryTimeoutSeconds

    // Data Reading Configuration(for compatibility with receiver implementation)
    private String selectQuery; // SQL for reading/polling data
    private String incrementalColumn; // Column for incremental processing
    private Integer fetchSize; // Fetch size for result sets
    private Integer maxResults; // Maximum results per query
    private Long pollingInterval; // Polling interval in milliseconds

    // Batch Processing
    private Integer batchSize; // Global setting available: jdbc.batchSize
    private Boolean enableBatching; // Global setting available: jdbc.enableBatching
    private Long batchTimeoutMs; // Global setting available: jdbc.batchTimeoutMs
    private String batchStrategy; // SIZE_BASED, TIME_BASED, MIXED

    // Transaction Configuration
    private Boolean useTransactions; // Global setting available: jdbc.useTransactions
    private String transactionIsolationLevel; // Global setting available: jdbc.transactionIsolationLevel
    private Boolean autoCommit; // Global setting available: jdbc.autoCommit
    private Long transactionTimeoutMs; // Global setting available: jdbc.transactionTimeoutMs

    // Data Mapping & Transformation
    private String businessComponentId;
    private String targetDataStructureId; // Expected data structure for target
    private String dataMapping; // Field mapping configuration
    private Boolean validateData; // Global setting available: jdbc.validateData
    private String dataValidationRules;

    // Conflict Resolution & Error Handling
    private String conflictResolutionStrategy; // FAIL, SKIP, OVERWRITE, MERGE - Global setting available: jdbc.conflictResolutionStrategy
    private String duplicateKeyHandling; // ERROR, IGNORE, UPDATE - Global setting available: jdbc.duplicateKeyHandling
    private Boolean continueOnError; // Global setting available: jdbc.continueOnError
    private Integer maxErrorThreshold; // Global setting available: jdbc.maxErrorThreshold
    private String errorHandlingStrategy; // FAIL_FAST, SKIP_ERRORS, LOG_AND_CONTINUE - Global setting available: jdbc.errorHandlingStrategy

    // Performance & Optimization
    private Boolean useConnectionPooling; // Global setting available: jdbc.useConnectionPooling
    private Boolean enableStatementCaching; // Global setting available: jdbc.enableStatementCaching
    private Integer statementCacheSize; // Global setting available: jdbc.statementCacheSize
    private Boolean analyzePerformance; // Global setting available: jdbc.analyzePerformance
    private Long slowQueryThresholdMs; // Global setting available: jdbc.slowQueryThresholdMs

    // Retry Configuration
    private Integer maxRetryAttempts; // Global setting available: jdbc.maxRetryAttempts
    private Long retryDelayMs; // Global setting available: jdbc.retryDelayMs
    private Boolean useExponentialBackoff; // Global setting available: jdbc.useExponentialBackoff
    private String[] retryableErrorCodes; // Connection errors - Global setting available: jdbc.retryableErrorCodes

    // Audit & Logging
    private Boolean enableAuditLogging; // Global setting available: jdbc.enableAuditLogging
    private String auditTableName; // Optional audit table
    private Boolean logDataChanges; // Log actual data being written - Global setting available: jdbc.logDataChanges
    private Boolean enableMetrics; // Global setting available: jdbc.enableMetrics

    // Target System Specific
    private String targetSystem; // Name/identifier of target system
    private String operationType; // INSERT, UPDATE, DELETE, UPSERT - Global setting available: jdbc.defaultOperationType
    private String targetSchema; // Database schema to write to
    private String targetTable; // Primary table for operations

    // Stored Procedure Support
    private String storedProcedureName; // Name of stored procedure to call

    // Additional compatibility properties
    private String driverClassName; // Alternative name for driverClass
    private Integer minIdle; // Alternative name for minPoolSize
    private Long connectionTimeout; // Alternative for connectionTimeoutSeconds
    private Long idleTimeout; // 10 minutes - user configurable
    private Long maxLifetime; // 30 minutes - user configurable
    private String validationQuery; // Validation query - user configurable
    private String connectionName; // Connection pool name
    private Boolean useTransaction; // Alternative name for useTransactions
    private Map<String, String> connectionProperties; // Additional connection properties
    private Map<String, Integer> parameterMappings; // Maps field names to parameter positions
    private Map<String, String> outputParameters; // Output parameter definitions for stored procedures

    // Constructors
    public JdbcOutboundAdapterConfig() {}

    // Getters and Setters
    public String getJdbcUrl() { return jdbcUrl; }
    public void setJdbcUrl(String jdbcUrl) { this.jdbcUrl = jdbcUrl; }

    public String getDriverClass() { return driverClass; }
    public void setDriverClass(String driverClass) { this.driverClass = driverClass; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getDatabaseType() { return databaseType; }
    public void setDatabaseType(String databaseType) { this.databaseType = databaseType; }

    public Integer getMinPoolSize() { return minPoolSize; }
    public void setMinPoolSize(Integer minPoolSize) { this.minPoolSize = minPoolSize; }

    public Integer getMaxPoolSize() { return maxPoolSize; }
    public void setMaxPoolSize(Integer maxPoolSize) { this.maxPoolSize = maxPoolSize; }

    public Integer getConnectionTimeoutSeconds() { return connectionTimeoutSeconds; }
    public void setConnectionTimeoutSeconds(Integer connectionTimeoutSeconds) { this.connectionTimeoutSeconds = connectionTimeoutSeconds; }


    public String getInsertQuery() { return insertQuery; }
    public void setInsertQuery(String insertQuery) { this.insertQuery = insertQuery; }

    public String getUpdateQuery() { return updateQuery; }
    public void setUpdateQuery(String updateQuery) { this.updateQuery = updateQuery; }

    public String getDeleteQuery() { return deleteQuery; }
    public void setDeleteQuery(String deleteQuery) { this.deleteQuery = deleteQuery; }

    public String getUpsertQuery() { return upsertQuery; }
    public void setUpsertQuery(String upsertQuery) { this.upsertQuery = upsertQuery; }

    public Integer getQueryTimeoutSeconds() { return queryTimeoutSeconds; }
    public void setQueryTimeoutSeconds(Integer queryTimeoutSeconds) { this.queryTimeoutSeconds = queryTimeoutSeconds; }

    public Integer getBatchSize() { return batchSize; }
    public void setBatchSize(Integer batchSize) { this.batchSize = batchSize; }

    public Boolean isEnableBatchProcessing() { return enableBatching; }
    public void setEnableBatchProcessing(Boolean enableBatching) { this.enableBatching = enableBatching; }

    public Long getBatchTimeoutMs() { return batchTimeoutMs; }
    public void setBatchTimeoutMs(Long batchTimeoutMs) { this.batchTimeoutMs = batchTimeoutMs; }

    public String getBatchStrategy() { return batchStrategy; }
    public void setBatchStrategy(String batchStrategy) { this.batchStrategy = batchStrategy; }

    public Boolean isUseTransactions() { return useTransactions; }
    public void setUseTransactions(Boolean useTransactions) { this.useTransactions = useTransactions; }

    public String getTransactionIsolationLevel() { return transactionIsolationLevel; }
    public void setTransactionIsolationLevel(String transactionIsolationLevel) { this.transactionIsolationLevel = transactionIsolationLevel; }

    public Boolean isAutoCommit() { return autoCommit; }
    public void setAutoCommit(Boolean autoCommit) { this.autoCommit = autoCommit; }

    public Long getTransactionTimeoutMs() { return transactionTimeoutMs; }
    public void setTransactionTimeoutMs(Long transactionTimeoutMs) { this.transactionTimeoutMs = transactionTimeoutMs; }

    public String getBusinessComponentId() { return businessComponentId; }
    public void setBusinessComponentId(String businessComponentId) { this.businessComponentId = businessComponentId; }

    public String getTargetDataStructureId() { return targetDataStructureId; }
    public void setTargetDataStructureId(String targetDataStructureId) { this.targetDataStructureId = targetDataStructureId; }

    public String getDataMapping() { return dataMapping; }
    public void setDataMapping(String dataMapping) { this.dataMapping = dataMapping; }

    public Boolean isValidateData() { return validateData; }
    public void setValidateData(Boolean validateData) { this.validateData = validateData; }

    public String getDataValidationRules() { return dataValidationRules; }
    public void setDataValidationRules(String dataValidationRules) { this.dataValidationRules = dataValidationRules; }

    public String getConflictResolutionStrategy() { return conflictResolutionStrategy; }
    public void setConflictResolutionStrategy(String conflictResolutionStrategy) { this.conflictResolutionStrategy = conflictResolutionStrategy; }

    public String getDuplicateKeyHandling() { return duplicateKeyHandling; }
    public void setDuplicateKeyHandling(String duplicateKeyHandling) { this.duplicateKeyHandling = duplicateKeyHandling; }

    public Boolean isContinueOnError() { return continueOnError; }
    public void setContinueOnError(Boolean continueOnError) { this.continueOnError = continueOnError; }

    public Integer getMaxErrorThreshold() { return maxErrorThreshold; }
    public void setMaxErrorThreshold(Integer maxErrorThreshold) { this.maxErrorThreshold = maxErrorThreshold; }

    public String getErrorHandlingStrategy() { return errorHandlingStrategy; }
    public void setErrorHandlingStrategy(String errorHandlingStrategy) { this.errorHandlingStrategy = errorHandlingStrategy; }

    public Boolean isUseConnectionPooling() { return useConnectionPooling; }
    public void setUseConnectionPooling(Boolean useConnectionPooling) { this.useConnectionPooling = useConnectionPooling; }

    public Boolean isEnableStatementCaching() { return enableStatementCaching; }
    public void setEnableStatementCaching(Boolean enableStatementCaching) { this.enableStatementCaching = enableStatementCaching; }

    public Integer getStatementCacheSize() { return statementCacheSize; }
    public void setStatementCacheSize(Integer statementCacheSize) { this.statementCacheSize = statementCacheSize; }

    public Boolean isAnalyzePerformance() { return analyzePerformance; }
    public void setAnalyzePerformance(Boolean analyzePerformance) { this.analyzePerformance = analyzePerformance; }

    public Long getSlowQueryThresholdMs() { return slowQueryThresholdMs; }
    public void setSlowQueryThresholdMs(Long slowQueryThresholdMs) { this.slowQueryThresholdMs = slowQueryThresholdMs; }

    public Integer getMaxRetryAttempts() { return maxRetryAttempts; }
    public void setMaxRetryAttempts(Integer maxRetryAttempts) { this.maxRetryAttempts = maxRetryAttempts; }

    public Long getRetryDelayMs() { return retryDelayMs; }
    public void setRetryDelayMs(Long retryDelayMs) { this.retryDelayMs = retryDelayMs; }

    public Boolean isUseExponentialBackoff() { return useExponentialBackoff; }
    public void setUseExponentialBackoff(Boolean useExponentialBackoff) { this.useExponentialBackoff = useExponentialBackoff; }

    public String[] getRetryableErrorCodes() { return retryableErrorCodes; }
    public void setRetryableErrorCodes(String[] retryableErrorCodes) { this.retryableErrorCodes = retryableErrorCodes; }

    public Boolean isEnableAuditLogging() { return enableAuditLogging; }
    public void setEnableAuditLogging(Boolean enableAuditLogging) { this.enableAuditLogging = enableAuditLogging; }

    public String getAuditTableName() { return auditTableName; }
    public void setAuditTableName(String auditTableName) { this.auditTableName = auditTableName; }

    public Boolean isLogDataChanges() { return logDataChanges; }
    public void setLogDataChanges(Boolean logDataChanges) { this.logDataChanges = logDataChanges; }

    public Boolean isEnableMetrics() { return enableMetrics; }
    public void setEnableMetrics(Boolean enableMetrics) { this.enableMetrics = enableMetrics; }

    public String getTargetSystem() { return targetSystem; }
    public void setTargetSystem(String targetSystem) { this.targetSystem = targetSystem; }

    public String getOperationType() { return operationType; }
    public void setOperationType(String operationType) { this.operationType = operationType; }

    public String getTargetSchema() { return targetSchema; }
    public void setTargetSchema(String targetSchema) { this.targetSchema = targetSchema; }

    public String getTargetTable() { return targetTable; }
    public void setTargetTable(String targetTable) { this.targetTable = targetTable; }

    // Getters and setters for reading configuration(compatibility methods)
    public String getSelectQuery() { return selectQuery; }
    public void setSelectQuery(String selectQuery) { this.selectQuery = selectQuery; }

    public String getIncrementalColumn() { return incrementalColumn; }
    public void setIncrementalColumn(String incrementalColumn) { this.incrementalColumn = incrementalColumn; }

    public Integer getFetchSize() { return fetchSize; }
    public void setFetchSize(Integer fetchSize) { this.fetchSize = fetchSize; }

    public Integer getMaxResults() { return maxResults; }
    public void setMaxResults(Integer maxResults) { this.maxResults = maxResults; }

    public Long getPollingInterval() { return pollingInterval; }
    public void setPollingInterval(Long pollingInterval) { this.pollingInterval = pollingInterval; }

    // Getters and setters for stored procedure support
    public String getStoredProcedureName() { return storedProcedureName; }
    public void setStoredProcedureName(String storedProcedureName) { this.storedProcedureName = storedProcedureName; }

    // Getters and setters for additional compatibility properties
    public String getDriverClassName() { return driverClassName; }
    public void setDriverClassName(String driverClassName) { this.driverClassName = driverClassName; }

    public Integer getMinIdle() { return minIdle; }
    public void setMinIdle(Integer minIdle) { this.minIdle = minIdle; }

    public Long getConnectionTimeout() { return connectionTimeout; }
    public void setConnectionTimeout(Long connectionTimeout) { this.connectionTimeout = connectionTimeout; }

    public Long getIdleTimeout() { return idleTimeout; }
    public void setIdleTimeout(Long idleTimeout) { this.idleTimeout = idleTimeout; }

    public Long getMaxLifetime() { return maxLifetime; }
    public void setMaxLifetime(Long maxLifetime) { this.maxLifetime = maxLifetime; }

    public String getValidationQuery() { return validationQuery; }
    public void setValidationQuery(String validationQuery) { this.validationQuery = validationQuery; }

    public String getConnectionName() { return connectionName; }
    public void setConnectionName(String connectionName) { this.connectionName = connectionName; }

    public Boolean isUseTransaction() { return useTransaction; }
    public void setUseTransaction(Boolean useTransaction) { this.useTransaction = useTransaction; }

    public Map<String, String> getConnectionProperties() { return connectionProperties; }
    public void setConnectionProperties(Map<String, String> connectionProperties) { this.connectionProperties = connectionProperties; }

    public Map<String, Integer> getParameterMappings() { return parameterMappings; }
    public void setParameterMappings(Map<String, Integer> parameterMappings) { this.parameterMappings = parameterMappings; }

    public Map<String, String> getOutputParameters() { return outputParameters; }
    public void setOutputParameters(Map<String, String> outputParameters) { this.outputParameters = outputParameters; }

    @Override
    public String toString() {
        return String.format("JdbcOutboundAdapterConfig {url = '%s', driver = '%s', user = '%s', operation = '%s', batching = %s, transactions = %s}",
                jdbcUrl != null ? jdbcUrl.replaceAll("password = [^;&]*", "password = ***") : null,
                driverClass, username, operationType, enableBatching, useTransactions);
    }
}
