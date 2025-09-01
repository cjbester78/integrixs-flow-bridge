#!/bin/bash

# Script to fix primitive types in getter/setter methods for JdbcReceiverAdapterConfig

CONFIG_FILE="/Users/cjbester/git/integrix-flow-bridge/integrix-flow-bridge-backend/adapters/src/main/java/com/integrixs/adapters/config/JdbcReceiverAdapterConfig.java"

echo "Fixing primitive type getters/setters in JdbcReceiverAdapterConfig..."

# Fix connectionTimeoutSeconds
sed -i '' 's/public int getConnectionTimeoutSeconds()/public Integer getConnectionTimeoutSeconds()/g' "$CONFIG_FILE"
sed -i '' 's/public void setConnectionTimeoutSeconds(int /public void setConnectionTimeoutSeconds(Integer /g' "$CONFIG_FILE"

# Fix queryTimeoutSeconds
sed -i '' 's/public int getQueryTimeoutSeconds()/public Integer getQueryTimeoutSeconds()/g' "$CONFIG_FILE"
sed -i '' 's/public void setQueryTimeoutSeconds(int /public void setQueryTimeoutSeconds(Integer /g' "$CONFIG_FILE"

# Fix boolean methods to Boolean
sed -i '' 's/public boolean isEnableBatching()/public Boolean isEnableBatching()/g' "$CONFIG_FILE"
sed -i '' 's/public void setEnableBatching(boolean /public void setEnableBatching(Boolean /g' "$CONFIG_FILE"

sed -i '' 's/public boolean isUseTransactions()/public Boolean isUseTransactions()/g' "$CONFIG_FILE"
sed -i '' 's/public void setUseTransactions(boolean /public void setUseTransactions(Boolean /g' "$CONFIG_FILE"

sed -i '' 's/public boolean isAutoCommit()/public Boolean isAutoCommit()/g' "$CONFIG_FILE"
sed -i '' 's/public void setAutoCommit(boolean /public void setAutoCommit(Boolean /g' "$CONFIG_FILE"

sed -i '' 's/public boolean isValidateData()/public Boolean isValidateData()/g' "$CONFIG_FILE"
sed -i '' 's/public void setValidateData(boolean /public void setValidateData(Boolean /g' "$CONFIG_FILE"

sed -i '' 's/public boolean isContinueOnError()/public Boolean isContinueOnError()/g' "$CONFIG_FILE"
sed -i '' 's/public void setContinueOnError(boolean /public void setContinueOnError(Boolean /g' "$CONFIG_FILE"

sed -i '' 's/public boolean isUseConnectionPooling()/public Boolean isUseConnectionPooling()/g' "$CONFIG_FILE"
sed -i '' 's/public void setUseConnectionPooling(boolean /public void setUseConnectionPooling(Boolean /g' "$CONFIG_FILE"

sed -i '' 's/public boolean isEnableStatementCaching()/public Boolean isEnableStatementCaching()/g' "$CONFIG_FILE"
sed -i '' 's/public void setEnableStatementCaching(boolean /public void setEnableStatementCaching(Boolean /g' "$CONFIG_FILE"

sed -i '' 's/public boolean isAnalyzePerformance()/public Boolean isAnalyzePerformance()/g' "$CONFIG_FILE"
sed -i '' 's/public void setAnalyzePerformance(boolean /public void setAnalyzePerformance(Boolean /g' "$CONFIG_FILE"

sed -i '' 's/public boolean isUseExponentialBackoff()/public Boolean isUseExponentialBackoff()/g' "$CONFIG_FILE"
sed -i '' 's/public void setUseExponentialBackoff(boolean /public void setUseExponentialBackoff(Boolean /g' "$CONFIG_FILE"

sed -i '' 's/public boolean isEnableAuditLogging()/public Boolean isEnableAuditLogging()/g' "$CONFIG_FILE"
sed -i '' 's/public void setEnableAuditLogging(boolean /public void setEnableAuditLogging(Boolean /g' "$CONFIG_FILE"

sed -i '' 's/public boolean isLogDataChanges()/public Boolean isLogDataChanges()/g' "$CONFIG_FILE"
sed -i '' 's/public void setLogDataChanges(boolean /public void setLogDataChanges(Boolean /g' "$CONFIG_FILE"

sed -i '' 's/public boolean isEnableMetrics()/public Boolean isEnableMetrics()/g' "$CONFIG_FILE"
sed -i '' 's/public void setEnableMetrics(boolean /public void setEnableMetrics(Boolean /g' "$CONFIG_FILE"

sed -i '' 's/public boolean isUseTransaction()/public Boolean isUseTransaction()/g' "$CONFIG_FILE"
sed -i '' 's/public void setUseTransaction(boolean /public void setUseTransaction(Boolean /g' "$CONFIG_FILE"

# Fix long methods to Long
sed -i '' 's/public long getBatchTimeoutMs()/public Long getBatchTimeoutMs()/g' "$CONFIG_FILE"
sed -i '' 's/public void setBatchTimeoutMs(long /public void setBatchTimeoutMs(Long /g' "$CONFIG_FILE"

sed -i '' 's/public long getTransactionTimeoutMs()/public Long getTransactionTimeoutMs()/g' "$CONFIG_FILE"
sed -i '' 's/public void setTransactionTimeoutMs(long /public void setTransactionTimeoutMs(Long /g' "$CONFIG_FILE"

sed -i '' 's/public long getSlowQueryThresholdMs()/public Long getSlowQueryThresholdMs()/g' "$CONFIG_FILE"
sed -i '' 's/public void setSlowQueryThresholdMs(long /public void setSlowQueryThresholdMs(Long /g' "$CONFIG_FILE"

sed -i '' 's/public long getRetryDelayMs()/public Long getRetryDelayMs()/g' "$CONFIG_FILE"
sed -i '' 's/public void setRetryDelayMs(long /public void setRetryDelayMs(Long /g' "$CONFIG_FILE"

# Fix int methods to Integer
sed -i '' 's/public int getMaxErrorThreshold()/public Integer getMaxErrorThreshold()/g' "$CONFIG_FILE"
sed -i '' 's/public void setMaxErrorThreshold(int /public void setMaxErrorThreshold(Integer /g' "$CONFIG_FILE"

sed -i '' 's/public int getStatementCacheSize()/public Integer getStatementCacheSize()/g' "$CONFIG_FILE"
sed -i '' 's/public void setStatementCacheSize(int /public void setStatementCacheSize(Integer /g' "$CONFIG_FILE"

sed -i '' 's/public int getMaxRetryAttempts()/public Integer getMaxRetryAttempts()/g' "$CONFIG_FILE"
sed -i '' 's/public void setMaxRetryAttempts(int /public void setMaxRetryAttempts(Integer /g' "$CONFIG_FILE"

# Also rename isEnableBatchProcessing to match the field name
sed -i '' 's/public Boolean isEnableBatching()/public Boolean isEnableBatchProcessing()/g' "$CONFIG_FILE"
sed -i '' 's/public void setEnableBatching(/public void setEnableBatchProcessing(/g' "$CONFIG_FILE"

echo "Done fixing types!"