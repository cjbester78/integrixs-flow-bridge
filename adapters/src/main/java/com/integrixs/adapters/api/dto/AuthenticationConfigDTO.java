package com.integrixs.adapters.api.dto;

import lombok.Data;

import java.util.Map;

/**
 * DTO for authentication configuration
 */
@Data
public class AuthenticationConfigDTO {
    private String type;
    private String username;
    private String password;
    private String apiKey;
    private String token;
    private String certificatePath;
    private String certificatePassword;
    private Map<String, String> customHeaders;
}