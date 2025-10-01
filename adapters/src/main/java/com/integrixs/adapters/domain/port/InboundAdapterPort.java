package com.integrixs.adapters.domain.port;

import com.integrixs.adapters.domain.model.AdapterOperationResult;
import com.integrixs.adapters.domain.model.FetchRequest;

import java.util.concurrent.CompletableFuture;

/**
 * Domain port interface for inbound adapters(receive FROM external systems)
 * Following industry - standard terminology
 */
public interface InboundAdapterPort extends AdapterPort {

    /**
     * Fetch data from external system
     * @param request The fetch request parameters
     * @return Operation result with fetched data
     */
    AdapterOperationResult fetch(FetchRequest request);

    /**
     * Fetch data asynchronously
     * @param request The fetch request parameters
     * @return Future with operation result
     */
    CompletableFuture<AdapterOperationResult> fetchAsync(FetchRequest request);

    /**
     * Start listening for incoming data(for push - based adapters)
     * @param callback Callback to handle incoming data
     */
    void startListening(DataReceivedCallback callback);

    /**
     * Stop listening for incoming data
     */
    void stopListening();

    /**
     * Check if adapter is currently listening
     * @return true if listening
     */
    boolean isListening();

    /**
     * Callback interface for handling received data
     */
    @FunctionalInterface
    interface DataReceivedCallback {
        void onDataReceived(Object data, AdapterOperationResult result);
    }
}
