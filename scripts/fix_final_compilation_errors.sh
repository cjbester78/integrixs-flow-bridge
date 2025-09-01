#!/bin/bash

echo "Fixing final compilation errors..."

ADAPTER_DIR="/Users/cjbester/git/integrix-flow-bridge/integrix-flow-bridge-backend/adapters/src/main/java/com/integrixs/adapters/infrastructure/adapter"

# 1. Fix FileSenderAdapter @Override on getPollingIntervalMs
echo "Fixing FileSenderAdapter..."
perl -i -0pe 's/\s*\@Override\s*\n\s*(protected long getPollingIntervalMs)/$1/g' "$ADAPTER_DIR/FileSenderAdapter.java"

# 2. Fix FtpSenderAdapter issues
echo "Fixing FtpSenderAdapter..."

# First check if import exists, if not add it
if ! grep -q "import com.integrixs.adapters.infrastructure.util.AdapterTypeConverter;" "$ADAPTER_DIR/FtpSenderAdapter.java"; then
    perl -i -pe 's/(import com.integrixs.adapters.core.\*;)/$1\nimport com.integrixs.adapters.infrastructure.util.AdapterTypeConverter;/' "$ADAPTER_DIR/FtpSenderAdapter.java"
fi

# Fix all AdapterConfiguration.AdapterTypeEnum to use AdapterTypeConverter
sed -i '' 's/AdapterConfiguration\.AdapterTypeEnum\.FTP/AdapterTypeConverter.toCoreType(AdapterConfiguration.AdapterTypeEnum.FTP)/g' "$ADAPTER_DIR/FtpSenderAdapter.java"

# Remove incorrect @Override annotations
perl -i -0pe 's/\s*\@Override\s*\n\s*(protected AdapterOperationResult pollForNewData)/$1/g' "$ADAPTER_DIR/FtpSenderAdapter.java"
perl -i -0pe 's/\s*\@Override\s*\n\s*(protected long getPollingIntervalMs)/$1/g' "$ADAPTER_DIR/FtpSenderAdapter.java"
perl -i -0pe 's/\s*\@Override\s*\n\s*(public void startListening)/$1/g' "$ADAPTER_DIR/FtpSenderAdapter.java"
perl -i -0pe 's/\s*\@Override\s*\n\s*(public void stopListening)/$1/g' "$ADAPTER_DIR/FtpSenderAdapter.java"
perl -i -0pe 's/\s*\@Override\s*\n\s*(public boolean isListening)/$1/g' "$ADAPTER_DIR/FtpSenderAdapter.java"
perl -i -0pe 's/\s*\@Override\s*\n\s*(protected AdapterOperationResult performSend\(Object payload, Map)/$1/g' "$ADAPTER_DIR/FtpSenderAdapter.java"
perl -i -0pe 's/\s*\@Override\s*\n\s*(public void registerDataCallback)/$1/g' "$ADAPTER_DIR/FtpSenderAdapter.java"

echo "Done fixing final compilation errors!"