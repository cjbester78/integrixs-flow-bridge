package com.integrixs.adapters.domain.repository;

import com.integrixs.adapters.domain.model.AdapterConfiguration;

import java.util.List;
import java.util.Optional;

/**
 * Domain repository interface for adapter configurations
 */
public interface AdapterRepository {

    /**
     * Save adapter configuration
     * @param configuration Configuration to save
     * @return Saved configuration
     */
    AdapterConfiguration save(AdapterConfiguration configuration);

    /**
     * Find adapter by ID
     * @param adapterId Adapter ID
     * @return Optional configuration
     */
    Optional<AdapterConfiguration> findById(String adapterId);

    /**
     * Find all adapters
     * @return List of configurations
     */
    List<AdapterConfiguration> findAll();

    /**
     * Find adapters by type
     * @param adapterType Adapter type
     * @return List of configurations
     */
    List<AdapterConfiguration> findByType(AdapterConfiguration.AdapterTypeEnum adapterType);

    /**
     * Find adapters by mode
     * @param adapterMode Adapter mode
     * @return List of configurations
     */
    List<AdapterConfiguration> findByMode(AdapterConfiguration.AdapterModeEnum adapterMode);

    /**
     * Delete adapter
     * @param adapterId Adapter ID
     */
    void delete(String adapterId);

    /**
     * Check if adapter exists
     * @param adapterId Adapter ID
     * @return true if exists
     */
    boolean exists(String adapterId);

    /**
     * Count adapters
     * @return Total count
     */
    long count();

    /**
     * Delete all adapters
     */
    void deleteAll();
}
