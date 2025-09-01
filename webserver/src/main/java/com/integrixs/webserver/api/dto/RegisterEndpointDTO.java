package com.integrixs.webserver.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

/**
 * DTO for registering service endpoint
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterEndpointDTO {
    
    @NotNull(message = "Endpoint name is required")
    private String name;
    
    @NotNull(message = "Base URL is required")
    private String baseUrl;
    
    @NotNull(message = "Endpoint type is required")
    private String type;
    
    private String description;
    
    private AuthenticationConfigDTO defaultAuth;
    
    @Builder.Default
    private Map<String, String> defaultHeaders = new HashMap<>();
    
    private ConnectionConfigDTO connectionConfig;
    
    private String version;
    
    @Builder.Default
    private Map<String, String> metadata = new HashMap<>();
}