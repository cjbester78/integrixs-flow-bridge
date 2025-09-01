package com.integrixs.adapters.domain.model;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * Domain model for authentication configuration
 */
@Data
@Builder
public class AuthenticationConfig {
    private AuthenticationType type;
    private String username;
    private String password;
    private String apiKey;
    private String token;
    private String certificatePath;
    private String certificatePassword;
    private Map<String, String> customHeaders;
    private Map<String, Object> additionalProperties;
    
    /**
     * Authentication types
     */
    public enum AuthenticationType {
        NONE,
        BASIC,
        BEARER,
        API_KEY,
        OAUTH2,
        CERTIFICATE,
        CUSTOM
    }
}