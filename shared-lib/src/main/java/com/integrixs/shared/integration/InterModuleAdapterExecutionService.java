package com.integrixs.shared.integration;

import java.util.List;

/**
 * Interface for adapter execution operations across modules
 * Used by Engine module to execute adapters in other modules
 */
public interface InterModuleAdapterExecutionService {

    /**
     * Execute an adapter with given context
     * @param adapterId Adapter ID
     * @param context Execution context
     * @return Adapter execution result
     */
    AdapterResult executeAdapter(String adapterId, AdapterContext context);

    /**
     * Test adapter connectivity
     * @param adapterId Adapter ID
     * @return True if adapter is reachable
     */
    boolean testAdapter(String adapterId);

    /**
     * Get adapter metadata
     * @param adapterId Adapter ID
     * @return Adapter metadata
     */
    AdapterMetadata getAdapterMetadata(String adapterId);

    /**
     * List all available adapters
     * @return List of adapter metadata
     */
    List<AdapterMetadata> listAdapters();

    /**
     * Validate adapter configuration
     * @param adapterId Adapter ID
     * @return Validation result
     */
    ValidationResult validateAdapterConfig(String adapterId);
}
