package com.integrixs.adapters.social.base;

import com.integrixs.shared.config.AdapterConfig;
import java.util.Map;

/**
 * Base configuration class for social media adapters
 */
public abstract class SocialMediaAdapterConfig implements AdapterConfig {
    public SocialMediaAdapterConfig() {
    }


    // Base adapter configuration
    private String type;
    private String name;
    private String description;
    private boolean enabled;
    private Map<String, Object> configuration;

    // OAuth2 Configuration
    private String clientId;
    private String clientSecret;
    private String accessToken;
    private String refreshToken;
    private String redirectUri;
    private String scope;

    // API Configuration
    private String apiVersion;
    private String apiBaseUrl;
    private Integer requestTimeout;
    private Integer maxRetries;

    // Rate Limiting
    private Integer rateLimitPerMinute;
    private Integer rateLimitPerHour;

    // Webhook Configuration
    private String webhookUrl;
    private String webhookVerifyToken;

    // Platform - specific configuration
    private Map<String, Object> platformConfig;

    /**
     * Get the OAuth2 authorization URL for this platform
     */
    public abstract String getAuthorizationUrl();

    /**
     * Get the OAuth2 token URL for this platform
     */
    public abstract String getTokenUrl();

    /**
     * Get the platform name(e.g., "facebook", "twitter", "linkedin")
     */
    public abstract String getPlatformName();
    // Getters and Setters
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
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
    public boolean isEnabled() {
        return enabled;
    }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    public Map<String, Object> getConfiguration() {
        return configuration;
    }
    public void setConfiguration(Map<String, Object> configuration) {
        this.configuration = configuration;
    }
    public String getClientId() {
        return clientId;
    }
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    public String getClientSecret() {
        return clientSecret;
    }
    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }
    public String getAccessToken() {
        return accessToken;
    }
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    public String getRefreshToken() {
        return refreshToken;
    }
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    public String getRedirectUri() {
        return redirectUri;
    }
    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }
    public String getScope() {
        return scope;
    }
    public void setScope(String scope) {
        this.scope = scope;
    }
    public String getApiVersion() {
        return apiVersion;
    }
    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }
    public String getApiBaseUrl() {
        return apiBaseUrl;
    }
    public void setApiBaseUrl(String apiBaseUrl) {
        this.apiBaseUrl = apiBaseUrl;
    }
    public Integer getRequestTimeout() {
        return requestTimeout;
    }
    public void setRequestTimeout(Integer requestTimeout) {
        this.requestTimeout = requestTimeout;
    }
    public Integer getMaxRetries() {
        return maxRetries;
    }
    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }
    public Integer getRateLimitPerMinute() {
        return rateLimitPerMinute;
    }
    public void setRateLimitPerMinute(Integer rateLimitPerMinute) {
        this.rateLimitPerMinute = rateLimitPerMinute;
    }
    public Integer getRateLimitPerHour() {
        return rateLimitPerHour;
    }
    public void setRateLimitPerHour(Integer rateLimitPerHour) {
        this.rateLimitPerHour = rateLimitPerHour;
    }
    public String getWebhookUrl() {
        return webhookUrl;
    }
    public void setWebhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }
    public String getWebhookVerifyToken() {
        return webhookVerifyToken;
    }
    public void setWebhookVerifyToken(String webhookVerifyToken) {
        this.webhookVerifyToken = webhookVerifyToken;
    }
    public Map<String, Object> getPlatformConfig() {
        return platformConfig;
    }
    public void setPlatformConfig(Map<String, Object> platformConfig) {
        this.platformConfig = platformConfig;
    }
}
