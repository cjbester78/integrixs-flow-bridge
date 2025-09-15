package com.integrixs.adapters.config;

import java.util.Map;
import java.util.HashMap;

/**
 * Base configuration class for all adapters using Map - based configuration.
 *
 * Configuration Resolution Order:
 * 1. Adapter - specific configuration(highest priority)
 * 2. Global adapter type settings(e.g., global JDBC settings)
 * 3. System - wide defaults(lowest priority)
 *
 * Example:
 * - System default: connectionTimeout = 30000
 * - Global JDBC setting: connectionTimeout = 60000
 * - Specific adapter: connectionTimeout = 120000(this wins)
 *
 * If a specific adapter doesn't have a value, it falls back to global,
 * then to system defaults.
 */
public abstract class BaseAdapterConfig {
    protected final Map<String, Object> configuration;

    // Common configuration keys used across adapters
    public static final String CONNECTION_TIMEOUT = "connectionTimeout";
    public static final String READ_TIMEOUT = "readTimeout";
    public static final String MAX_RETRIES = "maxRetries";
    public static final String RETRY_DELAY = "retryDelay";
    public static final String ENABLE_LOGGING = "enableLogging";
    public static final String LOG_LEVEL = "logLevel";

    public BaseAdapterConfig() {
        this.configuration = new HashMap<>();
    }


    /**
     * Get configuration value with type safety and null handling.
     * Frontend will handle the fallback logic:
     * 1. Check adapter - specific config
     * 2. Check global adapter type config
     * 3. Use system default
     */
    protected String getString(String key) {
        Object value = configuration.get(key);
        return value != null ? value.toString() : null;
    }

    protected Integer getInteger(String key) {
        Object value = configuration.get(key);
        if(value == null) return null;
        if(value instanceof Integer) return(Integer) value;
        try {
            return Integer.parseInt(value.toString());
        } catch(NumberFormatException e) {
            return null;
        }
    }

    protected Long getLong(String key) {
        Object value = configuration.get(key);
        if(value == null) return null;
        if(value instanceof Long) return(Long) value;
        try {
            return Long.parseLong(value.toString());
        } catch(NumberFormatException e) {
            return null;
        }
    }

    protected Boolean getBoolean(String key) {
        Object value = configuration.get(key);
        if(value == null) return null;
        if(value instanceof Boolean) return(Boolean) value;
        return Boolean.parseBoolean(value.toString());
    }

    protected Double getDouble(String key) {
        Object value = configuration.get(key);
        if(value == null) return null;
        if(value instanceof Double) return(Double) value;
        try {
            return Double.parseDouble(value.toString());
        } catch(NumberFormatException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    protected Map<String, String> getStringMap(String key) {
        Object value = configuration.get(key);
        if(value instanceof Map) {
            return(Map<String, String>) value;
        }
        return null;
    }

    /**
     * Get the raw configuration map.
     * Used by adapters that need direct access.
     */
    public Map<String, Object> getConfiguration() {
        return new HashMap<>(configuration);
    }

    /**
     * Set a configuration value.
     * Used during runtime configuration updates.
     */
    public void setConfigValue(String key, Object value) {
        configuration.put(key, value);
    }
}
