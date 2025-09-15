package com.integrixs.adapters.api.dto;

import java.util.Map;

/**
 * DTO for update adapter requests
 */
public class UpdateAdapterRequestDTO {
    private String name;
    private String description;
    private Map<String, Object> connectionProperties;
    private Map<String, Object> operationProperties;
    private AuthenticationConfigDTO authentication;
    private RetryConfigDTO retryConfig;
    private Long timeout;
    // Getters and Setters
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
