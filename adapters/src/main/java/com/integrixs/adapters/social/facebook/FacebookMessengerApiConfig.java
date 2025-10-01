package com.integrixs.adapters.social.facebook;

import com.integrixs.adapters.social.base.SocialMediaAdapterConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Facebook Messenger API
 */
@Configuration
@ConfigurationProperties(prefix = "adapters.facebook.messenger")
public class FacebookMessengerApiConfig extends SocialMediaAdapterConfig {
    public FacebookMessengerApiConfig() {
    }


    // Facebook Messenger specific configuration
    private String pageId;
    private String pageAccessToken;
    private String appId;
    private String appSecret;
    private String verifyToken;

    // Messenger API settings
    private boolean enableTypingIndicator;
    private boolean enableReadReceipts;
    private boolean enableQuickReplies;
    private Integer messageTimeout;
    private Long pollingInterval;

    // Default values configured in application.yml
    private String defaultApiVersion;
    private String defaultApiBaseUrl;

    @Override
    public String getAuthorizationUrl() {
        return String.format("https://www.facebook.com/%s/dialog/oauth",
                            getApiVersion() != null ? getApiVersion() : defaultApiVersion);
    }

    @Override
    public String getTokenUrl() {
        return String.format("%s/%s/oauth/access_token",
                            getApiBaseUrl() != null ? getApiBaseUrl() : defaultApiBaseUrl,
                            getApiVersion() != null ? getApiVersion() : defaultApiVersion);
    }

    @Override
    public String getPlatformName() {
        return "facebook_messenger";
    }

    /**
     * Get the full API endpoint for sending messages
     */
    public String getMessagesEndpoint() {
        return String.format("%s/%s/me/messages",
                            getApiBaseUrl() != null ? getApiBaseUrl() : defaultApiBaseUrl,
                            getApiVersion() != null ? getApiVersion() : defaultApiVersion);
    }

    /**
     * Get the full API endpoint for user profile
     */
    public String getUserProfileEndpoint(String userId) {
        return String.format("%s/%s/%s",
                            getApiBaseUrl() != null ? getApiBaseUrl() : defaultApiBaseUrl,
                            getApiVersion() != null ? getApiVersion() : defaultApiVersion,
                            userId);
    }
    // Getters and Setters
    public String getPageId() {
        return pageId;
    }
    public void setPageId(String pageId) {
        this.pageId = pageId;
    }
    public String getPageAccessToken() {
        return pageAccessToken;
    }
    public void setPageAccessToken(String pageAccessToken) {
        this.pageAccessToken = pageAccessToken;
    }
    public String getAppId() {
        return appId;
    }
    public void setAppId(String appId) {
        this.appId = appId;
    }
    public String getAppSecret() {
        return appSecret;
    }
    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }
    public String getVerifyToken() {
        return verifyToken;
    }
    public void setVerifyToken(String verifyToken) {
        this.verifyToken = verifyToken;
    }
    public boolean isEnableTypingIndicator() {
        return enableTypingIndicator;
    }
    public void setEnableTypingIndicator(boolean enableTypingIndicator) {
        this.enableTypingIndicator = enableTypingIndicator;
    }
    public boolean isEnableReadReceipts() {
        return enableReadReceipts;
    }
    public void setEnableReadReceipts(boolean enableReadReceipts) {
        this.enableReadReceipts = enableReadReceipts;
    }
    public boolean isEnableQuickReplies() {
        return enableQuickReplies;
    }
    public void setEnableQuickReplies(boolean enableQuickReplies) {
        this.enableQuickReplies = enableQuickReplies;
    }
    public Integer getMessageTimeout() {
        return messageTimeout;
    }
    public void setMessageTimeout(Integer messageTimeout) {
        this.messageTimeout = messageTimeout;
    }
    public Long getPollingInterval() {
        return pollingInterval;
    }
    public void setPollingInterval(Long pollingInterval) {
        this.pollingInterval = pollingInterval;
    }
    public String getDefaultApiVersion() {
        return defaultApiVersion;
    }
    public void setDefaultApiVersion(String defaultApiVersion) {
        this.defaultApiVersion = defaultApiVersion;
    }
    public String getDefaultApiBaseUrl() {
        return defaultApiBaseUrl;
    }
    public void setDefaultApiBaseUrl(String defaultApiBaseUrl) {
        this.defaultApiBaseUrl = defaultApiBaseUrl;
    }
}
