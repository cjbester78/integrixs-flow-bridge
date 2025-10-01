package com.integrixs.webserver.api.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * DTO for authentication configuration
 */
public class AuthenticationConfigDTO {

    private String authType;
    private Map<String, String> credentials = new HashMap<>();

    // Default constructor
    public AuthenticationConfigDTO() {
    }

    // All args constructor
    public AuthenticationConfigDTO(String authType) {
        this.authType = authType;
        this.credentials = new HashMap<>();
    }

    // Full args constructor
    public AuthenticationConfigDTO(String authType, Map<String, String> credentials) {
        this.authType = authType;
        this.credentials = credentials != null ? credentials : new HashMap<>();
    }

    // Getters
    public String getAuthType() {
        return authType;
    }

    public Map<String, String> getCredentials() {
        return credentials;
    }

    // Setters
    public void setAuthType(String authType) {
        this.authType = authType;
    }

    public void setCredentials(Map<String, String> credentials) {
        this.credentials = credentials;
    }

    // Builder
    public static AuthenticationConfigDTOBuilder builder() {
        return new AuthenticationConfigDTOBuilder();
    }

    public static class AuthenticationConfigDTOBuilder {
        private String authType;

        public AuthenticationConfigDTOBuilder authType(String authType) {
            this.authType = authType;
            return this;
        }

        public AuthenticationConfigDTO build() {
            return new AuthenticationConfigDTO(authType);
        }
    }}
