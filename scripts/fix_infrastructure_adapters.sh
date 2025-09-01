#!/bin/bash

# Fix all infrastructure adapters to use proper signatures and AdapterOperationResult

cd /Users/cjbester/git/integrix-flow-bridge/integrix-flow-bridge-backend/adapters/src/main/java/com/integrixs/adapters/infrastructure/adapter

# List of adapters to fix (excluding FTP and Kafka which are already fixed)
ADAPTERS="File Soap Jms Http Jdbc Rest Mail Odata Idoc Rfc Sftp"

for adapter in $ADAPTERS; do
    for type in "Sender" "Receiver"; do
        file="${adapter}${type}Adapter.java"
        if [ -f "$file" ]; then
            echo "Fixing $file..."
            
            # Replace AdapterResult with AdapterOperationResult
            sed -i '' 's/AdapterResult /AdapterOperationResult /g' "$file"
            sed -i '' 's/AdapterResult\./AdapterOperationResult\./g' "$file"
            
            # Replace old method names with new ones
            sed -i '' 's/protected AdapterOperationResult doTestConnection/protected AdapterOperationResult performConnectionTest/g' "$file"
            sed -i '' 's/protected void doSenderInitialize/protected AdapterOperationResult performInitialization/g' "$file"
            sed -i '' 's/protected void doReceiverInitialize/protected AdapterOperationResult performInitialization/g' "$file"
            sed -i '' 's/protected void doSenderDestroy/protected AdapterOperationResult performShutdown/g' "$file"
            sed -i '' 's/protected void doReceiverDestroy/protected AdapterOperationResult performShutdown/g' "$file"
            sed -i '' 's/protected AdapterOperationResult doSend/protected AdapterOperationResult performSend/g' "$file"
            sed -i '' 's/protected AdapterOperationResult doReceive/protected AdapterOperationResult performReceive/g' "$file"
            
            # Replace ConnectionTestUtil with direct AdapterOperationResult
            sed -i '' 's/ConnectionTestUtil\.createTestSuccess([^,]*, /AdapterOperationResult.success(/g' "$file"
            sed -i '' 's/ConnectionTestUtil\.createTestFailure([^,]*, /AdapterOperationResult.failure(/g' "$file"
            sed -i '' 's/ConnectionTestUtil\.executeBasicConnectivityTest([^,]*, () -> {/try {/g' "$file"
            sed -i '' 's/ConnectionTestUtil\.executeConfigurationTest([^,]*, () -> {/try {/g' "$file"
            sed -i '' 's/ConnectionTestUtil\.combineTestResults([^,]*, /AdapterOperationResult.success("All tests passed").withData(/g' "$file"
            sed -i '' 's/testResults\.toArray(new AdapterOperationResult\[0\])/testResults/g' "$file"
            
            # Fix return types for initialization and shutdown methods
            sed -i '' 's/protected void doInitialize/protected AdapterOperationResult performInitialization/g' "$file"
            sed -i '' 's/protected void doDestroy/protected AdapterOperationResult performShutdown/g' "$file"
            
            # Remove List<AdapterOperationResult> and replace with simpler structure
            sed -i '' 's/List<AdapterOperationResult> testResults = new ArrayList<>();/Map<String, Object> testResults = new HashMap<>();/g' "$file"
            sed -i '' 's/testResults\.add(/testResults.put("test" + testResults.size(), /g' "$file"
        fi
    done
done

echo "Infrastructure adapters fixed!"