#!/bin/bash

echo "Fixing final @Override issues..."

ADAPTER_DIR="/Users/cjbester/git/integrix-flow-bridge/integrix-flow-bridge-backend/adapters/src/main/java/com/integrixs/adapters/infrastructure/adapter"

# Fix FtpSenderAdapter
echo "Fixing FtpSenderAdapter @Override issues..."
perl -i -0pe 's/\s*\@Override\s*\n\s*(protected AdapterOperationResult performReceive\(\) throws)/$1/g' "$ADAPTER_DIR/FtpSenderAdapter.java"
perl -i -0pe 's/\s*\@Override\s*\n\s*(protected AdapterOperationResult performReceive\(Object criteria\) throws)/$1/g' "$ADAPTER_DIR/FtpSenderAdapter.java"
perl -i -0pe 's/\s*\@Override\s*\n\s*(public AdapterOperationResult fetchBatch)/$1/g' "$ADAPTER_DIR/FtpSenderAdapter.java"
perl -i -0pe 's/\s*\@Override\s*\n\s*(public CompletableFuture<AdapterOperationResult> fetchBatchAsync)/$1/g' "$ADAPTER_DIR/FtpSenderAdapter.java"
perl -i -0pe 's/\s*\@Override\s*\n\s*(public boolean supportsBatchOperations)/$1/g' "$ADAPTER_DIR/FtpSenderAdapter.java"
perl -i -0pe 's/\s*\@Override\s*\n\s*(public int getMaxBatchSize)/$1/g' "$ADAPTER_DIR/FtpSenderAdapter.java"

echo "Done fixing final @Override issues!"