package com.integrixs.adapters.api.dto;

import java.util.Map;

/**
 * DTO for authentication configuration
 */
public class AuthenticationConfigDTO {
    private String type;
    private String username;
    private String password;
    private String apiKey;
    private String token;
    private String certificatePath;
    private String certificatePassword;
    private Map<String, String> customHeaders;
    // Getters and Setters
    public String getType() {
        return type;
    }
    public void setType(String type) {
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
}
