package com.integrixs.adapters.domain.service;

import com.integrixs.adapters.domain.model.*;

import java.util.List;

/**
 * Domain service interface for adapter management
 * Handles business logic for adapter operations
 */
public interface AdapterManagementService {

    /**
     * Create and configure a new adapter
     * @param configuration Adapter configuration
     * @return Created adapter ID
     */
    String createAdapter(AdapterConfiguration configuration);

    /**
     * Update adapter configuration
     * @param adapterId Adapter ID
     * @param configuration New configuration
     */
    void updateAdapterConfiguration(String adapterId, AdapterConfiguration configuration);

    /**
     * Delete an adapter
     * @param adapterId Adapter ID
     */
    void deleteAdapter(String adapterId);

    /**
     * Test adapter connection
     * @param adapterId Adapter ID
     * @return Test result
     */
    AdapterOperationResult testAdapterConnection(String adapterId);

    /**
     * Validate adapter configuration
     * @param configuration Configuration to validate
     * @return Validation result
     */
    AdapterOperationResult validateConfiguration(AdapterConfiguration configuration);

    /**
     * Start adapter
     * @param adapterId Adapter ID
     */
    void startAdapter(String adapterId);

    /**
     * Stop adapter
     * @param adapterId Adapter ID
     */
    void stopAdapter(String adapterId);

    /**
     * Get adapter status
     * @param adapterId Adapter ID
     * @return Adapter status
     */
    AdapterOperationResult getAdapterStatus(String adapterId);

    /**
     * Get adapter configuration
     * @param adapterId Adapter ID
     * @return Adapter configuration
     */
    AdapterConfiguration getAdapterConfiguration(String adapterId);

    /**
     * List all adapters
     * @return List of adapter configurations
     */
    List<AdapterConfiguration> listAdapters();

    /**
     * Get adapter metadata by type
     * @param adapterType Adapter type
     * @param adapterMode Adapter mode
     * @return Adapter metadata
     */
    AdapterMetadata getAdapterMetadata(
            AdapterConfiguration.AdapterTypeEnum adapterType,
            AdapterConfiguration.AdapterModeEnum adapterMode);

    /**
     * Check adapter health
     * @param adapterId Adapter ID
     * @return Health status
     */
    AdapterOperationResult checkAdapterHealth(String adapterId);

    /**
     * Reset adapter
     * @param adapterId Adapter ID
     */
    void resetAdapter(String adapterId);
}
