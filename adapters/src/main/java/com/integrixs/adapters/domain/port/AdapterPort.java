package com.integrixs.adapters.domain.port;

import com.integrixs.adapters.domain.model.AdapterConfiguration;
import com.integrixs.adapters.domain.model.AdapterMetadata;
import com.integrixs.adapters.domain.model.AdapterOperationResult;

/**
 * Domain port interface for all adapters
 * Defines the contract for adapter operations in the domain layer
 */
public interface AdapterPort {

    /**
     * Get adapter metadata
     * @return Adapter metadata including type, capabilities, etc.
     */
    AdapterMetadata getMetadata();

    /**
     * Validate adapter configuration
     * @param configuration The adapter configuration to validate
     * @return Validation result
     */
    AdapterOperationResult validateConfiguration(AdapterConfiguration configuration);

    /**
     * Test adapter connection
     * @param configuration The adapter configuration
     * @return Test result
     */
    AdapterOperationResult testConnection(AdapterConfiguration configuration);

    /**
     * Initialize the adapter
     * @param configuration The adapter configuration
     */
    void initialize(AdapterConfiguration configuration);

    /**
     * Check if adapter is ready
     * @return true if adapter is ready for operations
     */
    boolean isReady();

    /**
     * Shutdown the adapter
     */
    void shutdown();

    /**
     * Get adapter health status
     * @return Health status information
     */
    AdapterOperationResult getHealthStatus();
}
