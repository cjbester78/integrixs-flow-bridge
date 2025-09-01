package com.integrixs.webserver.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

/**
 * DTO for outbound request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutboundRequestDTO {
    
    @NotNull(message = "Request type is required")
    private String requestType;
    
    @NotNull(message = "Target URL is required")
    private String targetUrl;
    
    @NotNull(message = "HTTP method is required")
    private String httpMethod;
    
    private Object payload;
    private String contentType;
    
    @Builder.Default
    private Map<String, String> headers = new HashMap<>();
    
    @Builder.Default
    private Map<String, String> queryParams = new HashMap<>();
    
    private AuthenticationConfigDTO authentication;
    
    @Builder.Default
    private int timeoutSeconds = 30;
    
    private RetryConfigDTO retryConfig;
    
    private String flowId;
    private String adapterId;
}