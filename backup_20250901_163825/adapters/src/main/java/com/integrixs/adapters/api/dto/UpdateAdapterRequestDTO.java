package com.integrixs.adapters.api.dto;

import lombok.Data;

import java.util.Map;

/**
 * DTO for update adapter requests
 */
@Data
public class UpdateAdapterRequestDTO {
    private String name;
    private String description;
    private Map<String, Object> connectionProperties;
    private Map<String, Object> operationProperties;
    private AuthenticationConfigDTO authentication;
    private RetryConfigDTO retryConfig;
    private Long timeout;
}