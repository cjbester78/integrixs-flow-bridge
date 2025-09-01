#!/bin/bash

echo "Fixing exception syntax errors..."

ADAPTER_DIR="/Users/cjbester/git/integrix-flow-bridge/integrix-flow-bridge-backend/adapters/src/main/java/com/integrixs/adapters/infrastructure/adapter"

# Fix exception syntax - the pattern is broken with ), instead of ,
echo "1. Fixing exception constructor syntax..."
for file in $(find "$ADAPTER_DIR" -name "*.java" -type f); do
    # Fix the broken syntax where we have AdapterTypeEnum.FTP)), instead of AdapterTypeEnum.FTP),
    perl -i -pe 's/\(AdapterTypeConverter\.toCoreType\(AdapterConfiguration\.AdapterTypeEnum\.([A-Z_]+)\)\)\),/(AdapterTypeConverter.toCoreType(AdapterConfiguration.AdapterTypeEnum.\1),/g' "$file"
done

# Fix FtpReceiverAdapter specifically
echo "2. Fixing FtpReceiverAdapter missing closing brace..."
perl -i -0pe 's/return CompletableFuture\.supplyAsync\(\(\) -> sendBatch\(requests\)\);\s*$/return CompletableFuture.supplyAsync(() -> sendBatch(requests));\n    }/g' "$ADAPTER_DIR/FtpReceiverAdapter.java"

# Fix FileSenderAdapter metadata - should be FILE not FTP
echo "3. Fixing FileSenderAdapter metadata..."
sed -i '' 's/\.adapterType(AdapterConfiguration\.AdapterTypeEnum\.FTP)/.adapterType(AdapterConfiguration.AdapterTypeEnum.FILE)/g' "$ADAPTER_DIR/FileSenderAdapter.java"

echo "Exception syntax errors fixed!"