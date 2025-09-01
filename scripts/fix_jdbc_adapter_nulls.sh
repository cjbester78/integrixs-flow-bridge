#!/bin/bash

# Script to fix null handling in JdbcReceiverAdapter

ADAPTER_FILE="/Users/cjbester/git/integrix-flow-bridge/integrix-flow-bridge-backend/adapters/src/main/java/com/integrixs/adapters/infrastructure/adapter/JdbcReceiverAdapter.java"

echo "Fixing null handling in JdbcReceiverAdapter..."

# Create a temporary fix file
cat > /tmp/jdbc_adapter_fix.txt << 'EOF'
        // Connection pool settings
        if (config.getMaxPoolSize() != null) {
            hikariConfig.setMaximumPoolSize(config.getMaxPoolSize());
        }
        if (config.getMinIdle() != null) {
            hikariConfig.setMinimumIdle(config.getMinIdle());
        }
        if (config.getConnectionTimeout() != null) {
            hikariConfig.setConnectionTimeout(config.getConnectionTimeout() * 1000L);
        }
        if (config.getIdleTimeout() != null) {
            hikariConfig.setIdleTimeout(config.getIdleTimeout() * 1000L);
        }
        if (config.getMaxLifetime() != null) {
            hikariConfig.setMaxLifetime(config.getMaxLifetime() * 1000L);
        }
EOF

# Replace the connection pool settings section
perl -i -pe 'BEGIN{undef $/;} s/        \/\/ Connection pool settings\n        hikariConfig\.setMaximumPoolSize.*?\n        hikariConfig\.setMaxLifetime\(config\.getMaxLifetime\(\) \* 1000L\);/`cat \/tmp\/jdbc_adapter_fix.txt | sed "s\/\\\\\\/\\\\\\\\\\\\\\/g"`/smge' "$ADAPTER_FILE"

# Fix other null-prone areas
echo "Fixing isEnableBatchProcessing calls..."
sed -i '' 's/!config\.isEnableBatchProcessing()/config.isEnableBatchProcessing() != null \&\& !config.isEnableBatchProcessing()/g' "$ADAPTER_FILE"

echo "Fixing Boolean method calls..."
sed -i '' 's/config\.isUseTransaction()/config.isUseTransaction() != null \&\& config.isUseTransaction()/g' "$ADAPTER_FILE"
sed -i '' 's/!config\.isUseTransaction()/config.isUseTransaction() == null || !config.isUseTransaction()/g' "$ADAPTER_FILE"

echo "Fixing config.getConnectionName() for pool name..."
sed -i '' 's/"JdbcReceiverPool-" + config.getConnectionName()/"JdbcReceiverPool-" + (config.getConnectionName() != null ? config.getConnectionName() : "default")/g' "$ADAPTER_FILE"

# Clean up
rm -f /tmp/jdbc_adapter_fix.txt

echo "Done fixing null handling!"