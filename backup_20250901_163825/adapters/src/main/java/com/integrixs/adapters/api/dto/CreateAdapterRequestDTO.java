package com.integrixs.adapters.api.dto;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * DTO for create adapter requests
 */
@Data
public class CreateAdapterRequestDTO {
    private String adapterType;
    private String adapterMode;
    private String name;
    private String description;
    private Map<String, Object> connectionProperties = new HashMap<>();
    private Map<String, Object> operationProperties = new HashMap<>();
    private AuthenticationConfigDTO authentication;
    private RetryConfigDTO retryConfig;
    private Long timeout;
    
    /**
     * Add connection property
     * @param key Property key
     * @param value Property value
     */
    public void addConnectionProperty(String key, Object value) {
        this.connectionProperties.put(key, value);
    }
    
    /**
     * Add operation property
     * @param key Property key
     * @param value Property value
     */
    public void addOperationProperty(String key, Object value) {
        this.operationProperties.put(key, value);
    }
}