package com.integrixs.adapters.domain.model;

import java.util.Map;

/**
 * Domain model for authentication configuration
 */
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
    // Getters and Setters
    public AuthenticationType getType() {
        return type;
    }
    public void setType(AuthenticationType type) {
        this.type = type;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getApiKey() {
        return apiKey;
    }
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }
    public String getCertificatePath() {
        return certificatePath;
    }
    public void setCertificatePath(String certificatePath) {
        this.certificatePath = certificatePath;
    }
    public String getCertificatePassword() {
        return certificatePassword;
    }
    public void setCertificatePassword(String certificatePassword) {
        this.certificatePassword = certificatePassword;
    }
    public Map<String, String> getCustomHeaders() {
        return customHeaders;
    }
    public void setCustomHeaders(Map<String, String> customHeaders) {
        this.customHeaders = customHeaders;
    }
    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }
    public void setAdditionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private AuthenticationType type;
        private String username;
        private String password;
        private String apiKey;
        private String token;
        private String certificatePath;
        private String certificatePassword;
        private Map<String, String> customHeaders;
        private Map<String, Object> additionalProperties;

        public Builder type(AuthenticationType type) {
            this.type = type;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public Builder token(String token) {
            this.token = token;
            return this;
        }

        public Builder certificatePath(String certificatePath) {
            this.certificatePath = certificatePath;
            return this;
        }

        public Builder certificatePassword(String certificatePassword) {
            this.certificatePassword = certificatePassword;
            return this;
        }

        public Builder customHeaders(Map<String, String> customHeaders) {
            this.customHeaders = customHeaders;
            return this;
        }

        public Builder additionalProperties(Map<String, Object> additionalProperties) {
            this.additionalProperties = additionalProperties;
            return this;
        }

        public AuthenticationConfig build() {
            AuthenticationConfig obj = new AuthenticationConfig();
            obj.type = this.type;
            obj.username = this.username;
            obj.password = this.password;
            obj.apiKey = this.apiKey;
            obj.token = this.token;
            obj.certificatePath = this.certificatePath;
            obj.certificatePassword = this.certificatePassword;
            obj.customHeaders = this.customHeaders;
            obj.additionalProperties = this.additionalProperties;
            return obj;
        }
    }
}
