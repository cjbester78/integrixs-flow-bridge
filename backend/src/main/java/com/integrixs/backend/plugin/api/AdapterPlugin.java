package com.integrixs.backend.plugin.api;

import java.util.Map;

/**
 * Core interface that all adapter plugins must implement.
 * Provides the contract for custom adapter development.
 */
public interface AdapterPlugin {

    /**
     * Get metadata about this adapter plugin
     * @return adapter metadata including name, version, description
     */
    AdapterMetadata getMetadata();

    /**
     * Initialize the adapter with configuration
     * @param configuration adapter configuration map
     * @throws PluginInitializationException if initialization fails
     */
    void initialize(Map<String, Object> configuration) throws PluginInitializationException;

    /**
     * Clean up resources when adapter is destroyed
     */
    void destroy();

    /**
     * Get the inbound handler for receiving data
     * @return inbound handler or null if not supported
     */
    InboundHandler getInboundHandler();

    /**
     * Get the outbound handler for sending data
     * @return outbound handler or null if not supported
     */
    OutboundHandler getOutboundHandler();

    /**
     * Test the connection with current configuration
     * @param direction the direction to test(INBOUND or OUTBOUND)
     * @return connection test result
     */
    ConnectionTestResult testConnection(Direction direction);

    /**
     * Check the health status of the adapter
     * @return current health status
     */
    HealthStatus checkHealth();

    /**
     * Get the configuration schema for this adapter
     * @return configuration schema in JSON format
     */
    default ConfigurationSchema getConfigurationSchema() {
        return null;
    }

    /**
     * Validate configuration before initialization
     * @param configuration configuration to validate
     * @return validation result
     */
    default ValidationResult validateConfiguration(Map<String, Object> configuration) {
        return ValidationResult.success();
    }

    /**
     * Direction enumeration
     */
    enum Direction {
        INBOUND,
        OUTBOUND,
        BIDIRECTIONAL
    }
}
