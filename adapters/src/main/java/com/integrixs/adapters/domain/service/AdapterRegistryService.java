package com.integrixs.adapters.domain.service;

import com.integrixs.adapters.domain.model.AdapterConfiguration;
import com.integrixs.adapters.domain.model.AdapterMetadata;
import com.integrixs.adapters.domain.port.AdapterPort;

import java.util.List;
import java.util.Optional;

/**
 * Domain service interface for adapter registry
 * Manages adapter registration and lifecycle
 */
public interface AdapterRegistryService {

    /**
     * Register an adapter
     * @param adapterId Unique adapter ID
     * @param adapter Adapter instance
     */
    void registerAdapter(String adapterId, AdapterPort adapter);

    /**
     * Unregister an adapter
     * @param adapterId Adapter ID
     */
    void unregisterAdapter(String adapterId);

    /**
     * Get adapter by ID
     * @param adapterId Adapter ID
     * @return Optional adapter
     */
    Optional<AdapterPort> getAdapter(String adapterId);

    /**
     * Get adapter metadata
     * @param adapterType Adapter type
     * @param adapterMode Adapter mode
     * @return Adapter metadata
     */
    AdapterMetadata getAdapterMetadata(
            AdapterConfiguration.AdapterTypeEnum adapterType,
            AdapterConfiguration.AdapterModeEnum adapterMode);

    /**
     * List all registered adapters
     * @return List of adapter IDs
     */
    List<String> listRegisteredAdapters();

    /**
     * List adapters by type
     * @param adapterType Adapter type
     * @return List of adapter IDs
     */
    List<String> listAdaptersByType(AdapterConfiguration.AdapterTypeEnum adapterType);

    /**
     * Check if adapter is registered
     * @param adapterId Adapter ID
     * @return true if registered
     */
    boolean isAdapterRegistered(String adapterId);

    /**
     * Create adapter instance
     * @param configuration Adapter configuration
     * @return Created adapter
     */
    AdapterPort createAdapter(AdapterConfiguration configuration);

    /**
     * Get supported adapter types
     * @return List of supported types
     */
    List<AdapterConfiguration.AdapterTypeEnum> getSupportedAdapterTypes();

    /**
     * Shutdown all adapters
     */
    void shutdownAll();
}
