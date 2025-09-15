package com.integrixs.shared.config;

import java.util.Map;

/**
 * Base configuration interface for all adapters
 */
public interface AdapterConfig {

    /**
     * Get the adapter type identifier
     */
    String getType();

    /**
     * Get the adapter name
     */
    String getName();

    /**
     * Get the adapter description
     */
    String getDescription();

    /**
     * Check if the adapter is enabled
     */
    boolean isEnabled();

    /**
     * Get adapter - specific configuration as a map
     */
    Map<String, Object> getConfiguration();

    /**
     * Get a specific configuration value
     */
    default Object getConfigValue(String key) {
        return getConfiguration() != null ? getConfiguration().get(key) : null;
    }

    /**
     * Get a specific configuration value with a default
     */
    default Object getConfigValue(String key, Object defaultValue) {
        Object value = getConfigValue(key);
        return value != null ? value : defaultValue;
    }
}
