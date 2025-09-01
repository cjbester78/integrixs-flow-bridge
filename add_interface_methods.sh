#!/bin/bash

cd /Users/cjbester/git/integrix-flow-bridge/integrix-flow-bridge-backend/adapters/src/main/java/com/integrixs/adapters/infrastructure/adapter

# Function to add SenderAdapterPort methods
add_sender_methods() {
    local file=$1
    echo "Adding SenderAdapterPort methods to $file..."
    
    # Check if methods already exist
    if ! grep -q "public AdapterOperationResult fetch" "$file"; then
        # Add before the closing brace
        sed -i '' '/^}$/i\
\
    // SenderAdapterPort implementation\
    @Override\
    public AdapterOperationResult fetch(FetchRequest request) {\
        try {\
            return performSend(request.getParameters() != null ? request.getParameters().get("payload") : null, request.getParameters());\
        } catch (Exception e) {\
            return AdapterOperationResult.failure("Fetch failed: " + e.getMessage());\
        }\
    }\
\
    @Override\
    public CompletableFuture<AdapterOperationResult> fetchAsync(FetchRequest request) {\
        return CompletableFuture.supplyAsync(() -> fetch(request));\
    }\
\
    @Override\
    public void startListening(DataReceivedCallback callback) {\
        // Not implemented for this adapter type\
        throw new UnsupportedOperationException("This adapter does not support push-based listening");\
    }\
\
    @Override\
    public void stopListening() {\
        // Not implemented for this adapter type\
    }\
\
    @Override\
    public boolean isListening() {\
        return false;\
    }\
\
    @Override\
    public void startPolling(long intervalMillis) {\
        // Implement if polling is supported\
        throw new UnsupportedOperationException("Polling not implemented");\
    }\
\
    @Override\
    public void stopPolling() {\
        // Implement if polling is supported\
    }\
\
    @Override\
    public void registerDataCallback(DataReceivedCallback callback) {\
        // Implement if callbacks are supported\
    }\
\
    @Override\
    public AdapterMetadata getMetadata() {\
        return AdapterMetadata.builder()\
                .adapterType(getAdapterType().name())\
                .adapterMode(AdapterMode.SENDER.name())\
                .description("Sender adapter implementation")\
                .version("1.0.0")\
                .supportsBatch(false)\
                .supportsAsync(true)\
                .build();\
    }\
\
    @Override\
    protected AdapterOperationResult performStart() {\
        return AdapterOperationResult.success("Started");\
    }\
\
    @Override\
    protected AdapterOperationResult performStop() {\
        return AdapterOperationResult.success("Stopped");\
    }' "$file"
    fi
}

# Function to add ReceiverAdapterPort methods
add_receiver_methods() {
    local file=$1
    echo "Adding ReceiverAdapterPort methods to $file..."
    
    # Check if methods already exist
    if ! grep -q "public AdapterOperationResult send" "$file"; then
        # Add before the closing brace
        sed -i '' '/^}$/i\
\
    // ReceiverAdapterPort implementation\
    @Override\
    public AdapterOperationResult send(SendRequest request) {\
        try {\
            return performReceive(request.getPayload());\
        } catch (Exception e) {\
            return AdapterOperationResult.failure("Send failed: " + e.getMessage());\
        }\
    }\
\
    @Override\
    public CompletableFuture<AdapterOperationResult> sendAsync(SendRequest request) {\
        return CompletableFuture.supplyAsync(() -> send(request));\
    }\
\
    @Override\
    public AdapterOperationResult sendBatch(List<SendRequest> requests) {\
        List<AdapterOperationResult> results = new ArrayList<>();\
        for (SendRequest request : requests) {\
            results.add(send(request));\
        }\
        boolean allSuccess = results.stream().allMatch(AdapterOperationResult::isSuccess);\
        return allSuccess ? \
            AdapterOperationResult.success("Batch send completed").withData(results) : \
            AdapterOperationResult.failure("Some batch operations failed").withData(results);\
    }\
\
    @Override\
    public CompletableFuture<AdapterOperationResult> sendBatchAsync(List<SendRequest> requests) {\
        return CompletableFuture.supplyAsync(() -> sendBatch(requests));\
    }\
\
    @Override\
    public boolean supportsBatchOperations() {\
        return false;\
    }\
\
    @Override\
    public int getMaxBatchSize() {\
        return 100;\
    }\
\
    @Override\
    public AdapterMetadata getMetadata() {\
        return AdapterMetadata.builder()\
                .adapterType(getAdapterType().name())\
                .adapterMode(AdapterMode.RECEIVER.name())\
                .description("Receiver adapter implementation")\
                .version("1.0.0")\
                .supportsBatch(supportsBatchOperations())\
                .supportsAsync(true)\
                .build();\
    }\
\
    @Override\
    protected AdapterOperationResult performStart() {\
        return AdapterOperationResult.success("Started");\
    }\
\
    @Override\
    protected AdapterOperationResult performStop() {\
        return AdapterOperationResult.success("Stopped");\
    }' "$file"
    fi
}

# Process sender adapters
for adapter in Jdbc Rest Mail Odata Idoc Rfc Sftp; do
    file="${adapter}SenderAdapter.java"
    if [ -f "$file" ]; then
        add_sender_methods "$file"
    fi
done

# Process receiver adapters  
for adapter in Http Jdbc Rest Mail Odata Idoc Rfc Sftp; do
    file="${adapter}ReceiverAdapter.java"
    if [ -f "$file" ]; then
        add_receiver_methods "$file"
    fi
done

echo "Interface methods added to all adapters!"