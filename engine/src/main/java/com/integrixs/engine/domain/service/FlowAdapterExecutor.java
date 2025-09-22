package com.integrixs.engine.domain.service;

import com.integrixs.engine.domain.model.AdapterExecutionContext;
import com.integrixs.engine.domain.model.AdapterExecutionResult;

import java.util.Map;

/**
 * Domain service for adapter execution within flow processing
 * Handles adapter operations in the context of integration flows
 */
public interface FlowAdapterExecutor {

    /**
     * Execute adapter to fetch data
     * @param adapterId The adapter identifier
     * @param context Execution context
     * @return Execution result containing data
     */
    AdapterExecutionResult fetchData(String adapterId, AdapterExecutionContext context);

    /**
     * Execute adapter to send data
     * @param adapterId The adapter identifier
     * @param data Data to send
     * @param context Execution context
     * @return Execution result
     */
    AdapterExecutionResult sendData(String adapterId, Object data, AdapterExecutionContext context);

    /**
     * Validate adapter configuration
     * @param adapterId The adapter identifier
     * @param config Adapter configuration
     * @throws IllegalArgumentException if configuration is invalid
     */
    void validateAdapterConfig(String adapterId, Map<String, Object> config);

    /**
     * Check if adapter is available and ready
     * @param adapterId The adapter identifier
     * @return true if adapter is ready
     */
    boolean isAdapterReady(String adapterId);

    /**
     * Get adapter capabilities
     * @param adapterId The adapter identifier
     * @return Map of capabilities
     */
    Map<String, Object> getAdapterCapabilities(String adapterId);
}
