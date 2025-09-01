#!/bin/bash

echo "Fixing all adapter compilation errors..."

ADAPTER_DIR="/Users/cjbester/git/integrix-flow-bridge/integrix-flow-bridge-backend/adapters/src/main/java/com/integrixs/adapters/infrastructure/adapter"

# 1. Fix FtpSenderAdapter exception handling
echo "1. Fixing FtpSenderAdapter exception handling..."
perl -i -0pe 's/(protected AdapterOperationResult performInitialization\(\) \{[^}]*?)\n(\s*validateConfiguration\(\);)/\1\n        try {\n\2/g' "$ADAPTER_DIR/FtpSenderAdapter.java"
perl -i -0pe 's/(validateConfiguration\(\);.*?return AdapterOperationResult\.success[^;]*;)/\1\n        } catch (Exception e) {\n            log.error("Failed to initialize FTP sender adapter", e);\n            return AdapterOperationResult.failure("Initialization failed: " + e.getMessage());\n        }/s' "$ADAPTER_DIR/FtpSenderAdapter.java"

# 2. Fix incorrect @Override annotations on all adapters
echo "2. Removing incorrect @Override annotations..."

# List of methods that should NOT have @Override
METHODS=(
    "performSend\(Object"
    "performReceive\(\)"
    "performReceive\(Object"
    "getPollingIntervalMs"
    "fetchBatch"
    "fetchBatchAsync"
    "supportsBatchOperations"
    "getMaxBatchSize"
    "startPolling"
    "stopPolling"
    "registerDataCallback"
    "performBatchSend"
    "performBatchReceive"
    "protected AdapterOperationResult performSend"
)

for file in $(find "$ADAPTER_DIR" -name "*.java" -type f); do
    for method in "${METHODS[@]}"; do
        perl -i -0pe "s/\\s*\\@Override\\s*\\n\\s*(.*$method)/$1/g" "$file"
    done
done

# 3. Fix enum type conversions - Add AdapterTypeConverter import to all files missing it
echo "3. Adding missing AdapterTypeConverter imports..."
for file in $(find "$ADAPTER_DIR" -name "*.java" -type f); do
    if ! grep -q "import com.integrixs.adapters.infrastructure.util.AdapterTypeConverter;" "$file"; then
        if grep -q "AdapterConfiguration.AdapterTypeEnum" "$file"; then
            perl -i -pe 's/(import com\.integrixs\.adapters\.core\.\*;)/$1\nimport com.integrixs.adapters.infrastructure.util.AdapterTypeConverter;/' "$file"
        fi
    fi
done

# 4. Replace all direct enum conversions with AdapterTypeConverter
echo "4. Fixing enum conversions..."
for file in $(find "$ADAPTER_DIR" -name "*.java" -type f); do
    # Fix patterns like AdapterConfiguration.AdapterTypeEnum.JDBC
    sed -i '' 's/\(AdapterConfiguration\.AdapterTypeEnum\.\([A-Z_]*\)\)/AdapterTypeConverter.toCoreType(AdapterConfiguration.AdapterTypeEnum.\2)/g' "$file"
    
    # Fix patterns where just the enum value is used
    sed -i '' 's/throw new AdapterException\.\([A-Za-z]*\)Exception(\([^,]*\), /throw new AdapterException.\1Exception(AdapterTypeConverter.toCoreType(\2), /g' "$file"
done

# 5. Fix JdbcReceiverAdapter boolean comparison issues
echo "5. Fixing JdbcReceiverAdapter boolean comparisons..."
sed -i '' '287s/config\.getEnableTransactionSupport() != null/config.getEnableTransactionSupport() != null \&\& config.getEnableTransactionSupport()/' "$ADAPTER_DIR/JdbcReceiverAdapter.java"
sed -i '' '330s/config\.getEnableTransactionSupport() != null/config.getEnableTransactionSupport() != null \&\& config.getEnableTransactionSupport()/' "$ADAPTER_DIR/JdbcReceiverAdapter.java"

# 6. Fix KafkaReceiverAdapter SendRequest constructor
echo "6. Fixing KafkaReceiverAdapter SendRequest issues..."
perl -i -pe 's/SendRequest request = new SendRequest\(\);/Map<String, Object> headers = new HashMap<>();/' "$ADAPTER_DIR/KafkaReceiverAdapter.java"
perl -i -pe 's/request\.setHeaders\(headers\);/\/\/ Headers already in map/' "$ADAPTER_DIR/KafkaReceiverAdapter.java"
perl -i -pe 's/request\.setPayload\(record\.value\(\)\);/Object payload = record.value();/' "$ADAPTER_DIR/KafkaReceiverAdapter.java"
perl -i -pe 's/performSend\(request\.getPayload\(\), request\.getHeaders\(\)\)/performSend(payload, headers)/' "$ADAPTER_DIR/KafkaReceiverAdapter.java"

# 7. Fix missing getAdapterType and getAdapterMode methods
echo "7. Adding missing getAdapterType and getAdapterMode methods..."

# Add missing methods to MailReceiverAdapter
if ! grep -q "protected AdapterType getAdapterType()" "$ADAPTER_DIR/MailReceiverAdapter.java"; then
    perl -i -pe 's/^}$/    @Override\n    protected AdapterType getAdapterType() {\n        return AdapterType.MAIL;\n    }\n\n    @Override\n    protected AdapterMode getAdapterMode() {\n        return AdapterMode.RECEIVER;\n    }\n}/' "$ADAPTER_DIR/MailReceiverAdapter.java"
fi

# Add missing methods to MailSenderAdapter
if ! grep -q "protected AdapterType getAdapterType()" "$ADAPTER_DIR/MailSenderAdapter.java"; then
    perl -i -pe 's/^}$/    @Override\n    protected AdapterType getAdapterType() {\n        return AdapterType.MAIL;\n    }\n\n    @Override\n    protected AdapterMode getAdapterMode() {\n        return AdapterMode.SENDER;\n    }\n}/' "$ADAPTER_DIR/MailSenderAdapter.java"
fi

# 8. Fix KafkaSenderAdapter issues
echo "8. Fixing KafkaSenderAdapter issues..."
# Fix getAdditionalProperties - add the method to config if missing
# Fix package references
perl -i -pe 's/package AdapterConfiguration does not exist/AdapterConfiguration.AdapterTypeEnum/g' "$ADAPTER_DIR/KafkaSenderAdapter.java"
perl -i -pe 's/AdapterConfiguration\./com.integrixs.adapters.domain.model.AdapterConfiguration./g' "$ADAPTER_DIR/KafkaSenderAdapter.java"

# 9. Fix Map type incompatibilities
echo "9. Fixing Map type incompatibilities..."
perl -i -pe 's/Map<String, String> headers/Map<String, Object> headers/g' "$ADAPTER_DIR/IdocReceiverAdapter.java"

# 10. Fix cast issues
echo "10. Fixing cast issues..."
perl -i -pe 's/String topic = request\.getHeaders\(\)\.get\("topic"\)/String topic = (String) request.getHeaders().get("topic")/g' "$ADAPTER_DIR/KafkaSenderAdapter.java"

echo "All adapter compilation errors should now be fixed!"