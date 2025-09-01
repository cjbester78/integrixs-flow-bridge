#!/bin/bash

echo "Fixing all syntax errors in adapter files..."

ADAPTER_DIR="/Users/cjbester/git/integrix-flow-bridge/integrix-flow-bridge-backend/adapters/src/main/java/com/integrixs/adapters/infrastructure/adapter"

# Fix the exception constructor syntax - the closing parenthesis is in the wrong place
echo "1. Fixing exception constructor syntax..."
for file in $(find "$ADAPTER_DIR" -name "*.java" -type f); do
    # Fix pattern: AdapterTypeEnum.FTP)), should be AdapterTypeEnum.FTP),
    sed -i '' 's/AdapterConfiguration\.AdapterTypeEnum\.\([A-Z_]*\))), /AdapterConfiguration.AdapterTypeEnum.\1), /g' "$file"
done

# Fix FtpSenderAdapter specific issues
echo "2. Fixing FtpSenderAdapter performInitialization..."
perl -i -0pe 's/(return AdapterOperationResult\.success\("FTP sender adapter initialized"\);)\s*\}\s*catch/\1\n    } catch/g' "$ADAPTER_DIR/FtpSenderAdapter.java"

# Fix FtpReceiverAdapter broken method
echo "3. Fixing FtpReceiverAdapter sendBatchAsync..."
perl -i -0pe 's/(return CompletableFuture\.supplyAsync\(\(\) -> sendBatch\(requests\)\);)\s*$/\1\n    }/g' "$ADAPTER_DIR/FtpReceiverAdapter.java"

echo "All syntax errors should now be fixed!"