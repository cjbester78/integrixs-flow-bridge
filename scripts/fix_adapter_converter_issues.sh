#!/bin/bash

echo "Fixing AdapterTypeConverter issues..."

ADAPTER_DIR="/Users/cjbester/git/integrix-flow-bridge/integrix-flow-bridge-backend/adapters/src/main/java/com/integrixs/adapters/infrastructure/adapter"

# Fix multiple nested AdapterTypeConverter calls
echo "1. Fixing nested AdapterTypeConverter calls..."
for file in $(find "$ADAPTER_DIR" -name "*.java" -type f); do
    # Fix triple nested calls
    sed -i '' 's/AdapterTypeConverter\.toCoreType(AdapterTypeConverter\.toCoreType(AdapterTypeConverter\.toCoreType(\([^)]*\)))/AdapterTypeConverter.toCoreType(\1)/g' "$file"
    
    # Fix double nested calls  
    sed -i '' 's/AdapterTypeConverter\.toCoreType(AdapterTypeConverter\.toCoreType(\([^)]*\)))/AdapterTypeConverter.toCoreType(\1)/g' "$file"
done

# Fix FtpSenderAdapter syntax error
echo "2. Fixing FtpSenderAdapter syntax error..."
perl -i -0pe 's/return AdapterOperationResult\.success\("FTP sender adapter initialized"\);\s*\}\s*catch/return AdapterOperationResult.success("FTP sender adapter initialized");\n    } catch/g' "$ADAPTER_DIR/FtpSenderAdapter.java"

# Fix FtpReceiverAdapter broken methods
echo "3. Fixing FtpReceiverAdapter broken methods..."
perl -i -0pe 's/\}\(\) \{\s*return true;\s*\}\(\) \{/\n\n    public boolean supportsBatchOperations() {\n        return true;\n    }\n\n    public int getMaxBatchSize() {/g' "$ADAPTER_DIR/FtpReceiverAdapter.java"

# Fix AdapterMetadata builder issues
echo "4. Fixing AdapterMetadata builder issues..."
for file in "$ADAPTER_DIR/FtpReceiverAdapter.java" "$ADAPTER_DIR/FtpSenderAdapter.java" "$ADAPTER_DIR/FileSenderAdapter.java"; do
    if [ -f "$file" ]; then
        # Fix metadata builder to use correct enum types
        perl -i -pe 's/\.adapterType\(AdapterTypeConverter\.toCoreType\(AdapterConfiguration\.AdapterTypeEnum\.[A-Z]+\)\)/.adapterType(AdapterConfiguration.AdapterTypeEnum.FTP)/g' "$file"
        perl -i -pe 's/\.adapterType\(AdapterTypeConverter\.toCoreType\(AdapterConfiguration\.AdapterTypeEnum\.FILE\)\)/.adapterType(AdapterConfiguration.AdapterTypeEnum.FILE)/g' "$file"
    fi
done

echo "Adapter converter issues fixed!"