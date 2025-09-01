package com.integrixs.adapters.domain.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Domain model for adapter metadata
 */
@Data
@Builder
public class AdapterMetadata {
    private String adapterName;
    private AdapterConfiguration.AdapterTypeEnum adapterType;
    private AdapterConfiguration.AdapterModeEnum adapterMode;
    private String version;
    private String description;
    private List<String> supportedOperations;
    private List<String> requiredProperties;
    private List<String> optionalProperties;
    private Map<String, String> propertyDescriptions;
    private Map<String, Object> capabilities;
    private boolean supportsAsync;
    private boolean supportsBatch;
    private boolean supportsStreaming;
    private boolean requiresAuthentication;
    private List<AuthenticationConfig.AuthenticationType> supportedAuthTypes;
    
    /**
     * Check if adapter supports a specific operation
     * @param operation Operation name
     * @return true if supported
     */
    public boolean supportsOperation(String operation) {
        return supportedOperations != null && supportedOperations.contains(operation);
    }
    
    /**
     * Check if a property is required
     * @param property Property name
     * @return true if required
     */
    public boolean isPropertyRequired(String property) {
        return requiredProperties != null && requiredProperties.contains(property);
    }
}