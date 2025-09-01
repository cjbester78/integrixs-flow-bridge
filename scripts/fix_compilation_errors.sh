#!/bin/bash

echo "Fixing remaining compilation errors..."

ADAPTER_DIR="/Users/cjbester/git/integrix-flow-bridge/integrix-flow-bridge-backend/adapters/src/main/java/com/integrixs/adapters/infrastructure/adapter"

# Remove incorrect @Override annotations from FileSenderAdapter
echo "Fixing FileSenderAdapter @Override issues..."
perl -i -0pe 's/\s*\@Override\s*\n\s*public AdapterOperationResult fetchBatch/public AdapterOperationResult fetchBatch/g' "$ADAPTER_DIR/FileSenderAdapter.java"
perl -i -0pe 's/\s*\@Override\s*\n\s*public CompletableFuture<AdapterOperationResult> fetchBatchAsync/public CompletableFuture<AdapterOperationResult> fetchBatchAsync/g' "$ADAPTER_DIR/FileSenderAdapter.java"
perl -i -0pe 's/\s*\@Override\s*\n\s*public boolean supportsBatchOperations/public boolean supportsBatchOperations/g' "$ADAPTER_DIR/FileSenderAdapter.java"
perl -i -0pe 's/\s*\@Override\s*\n\s*public int getMaxBatchSize/public int getMaxBatchSize/g' "$ADAPTER_DIR/FileSenderAdapter.java"
perl -i -0pe 's/\s*\@Override\s*\n\s*protected AdapterOperationResult performSend/protected AdapterOperationResult performSend/g' "$ADAPTER_DIR/FileSenderAdapter.java"

echo "Done fixing compilation errors!"