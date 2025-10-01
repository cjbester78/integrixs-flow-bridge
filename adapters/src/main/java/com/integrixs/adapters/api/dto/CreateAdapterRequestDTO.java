package com.integrixs.adapters.api.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * DTO for create adapter requests
 */
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

    // Getters and Setters
    public String getAdapterType() {
        return adapterType;
    }

    public void setAdapterType(String adapterType) {
        this.adapterType = adapterType;
    }

    public String getAdapterMode() {
        return adapterMode;
    }

    public void setAdapterMode(String adapterMode) {
        this.adapterMode = adapterMode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, Object> getConnectionProperties() {
        return connectionProperties;
    }

    public void setConnectionProperties(Map<String, Object> connectionProperties) {
        this.connectionProperties = connectionProperties;
    }

    public Map<String, Object> getOperationProperties() {
        return operationProperties;
    }

    public void setOperationProperties(Map<String, Object> operationProperties) {
        this.operationProperties = operationProperties;
    }

    public AuthenticationConfigDTO getAuthentication() {
        return authentication;
    }

    public void setAuthentication(AuthenticationConfigDTO authentication) {
        this.authentication = authentication;
    }

    public RetryConfigDTO getRetryConfig() {
        return retryConfig;
    }

    public void setRetryConfig(RetryConfigDTO retryConfig) {
        this.retryConfig = retryConfig;
    }

    public Long getTimeout() {
        return timeout;
    }

    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }
}
