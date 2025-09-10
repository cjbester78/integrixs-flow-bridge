package com.integrixs.adapters.social.base;

import com.integrixs.adapters.config.BaseAdapterConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

/**
 * Base configuration for all social media adapters
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class SocialMediaAdapterConfig extends BaseAdapterConfig {
    
    // OAuth2 Configuration
    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String accessToken;
    private String refreshToken;
    private Long tokenExpiresAt;
    private String[] scopes;
    
    // API Configuration
    private String apiVersion;
    private String apiEndpoint;
    
    // Rate Limiting Configuration
    private RateLimitConfig rateLimitConfig = new RateLimitConfig();
    
    // Webhook Configuration
    private boolean webhookEnabled;
    private String webhookUrl;
    private String webhookVerifyToken;
    private String webhookSecret;
    
    // Platform-specific Configuration
    private Map<String, Object> platformConfig;
    
    // Retry Configuration
    private RetryConfig retryConfig = new RetryConfig();
    
    @Data
    public static class RateLimitConfig {
        private int requestsPerHour = 200;
        private int requestsPerMinute = 30;
        private int burstSize = 100;
        private boolean adaptiveRateLimiting = true;
    }
    
    @Data
    public static class RetryConfig {
        private int maxRetries = 3;
        private long initialDelayMs = 1000;
        private double backoffMultiplier = 2.0;
        private long maxDelayMs = 30000;
        private boolean retryOnRateLimit = true;
    }
    
    /**
     * Validates if the configuration has valid OAuth tokens
     */
    public boolean hasValidToken() {
        return accessToken != null && 
               (tokenExpiresAt == null || tokenExpiresAt > System.currentTimeMillis());
    }
    
    /**
     * Checks if token needs refresh
     */
    public boolean needsTokenRefresh() {
        if (tokenExpiresAt == null) return false;
        // Refresh if token expires in less than 5 minutes
        return tokenExpiresAt - System.currentTimeMillis() < 300000;
    }
}