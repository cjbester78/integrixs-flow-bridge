package com.integrixs.adapters.domain.port;

import com.integrixs.adapters.domain.model.AdapterOperationResult;
import com.integrixs.adapters.domain.model.SendRequest;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Domain port interface for outbound adapters(send TO external systems)
 * Following industry - standard terminology
 */
public interface OutboundAdapterPort extends AdapterPort {

    /**
     * Send data to external system
     * @param request The send request containing data and parameters
     * @return Operation result
     */
    AdapterOperationResult send(SendRequest request);

    /**
     * Send data asynchronously
     * @param request The send request
     * @return Future with operation result
     */
    CompletableFuture<AdapterOperationResult> sendAsync(SendRequest request);

    /**
     * Send batch of data
     * @param requests List of send requests
     * @return Batch operation result
     */
    AdapterOperationResult sendBatch(List<SendRequest> requests);

    /**
     * Send batch asynchronously
     * @param requests List of send requests
     * @return Future with batch result
     */
    CompletableFuture<AdapterOperationResult> sendBatchAsync(List<SendRequest> requests);

    /**
     * Check if adapter supports batch operations
     * @return true if batch operations are supported
     */
    boolean supportsBatchOperations();

    /**
     * Get maximum batch size
     * @return Maximum number of items in a batch
     */
    int getMaxBatchSize();
}
