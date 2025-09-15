package com.integrixs.adapters.social.facebook;

import com.integrixs.adapters.social.base.SocialMediaAdapterConfig;
/**
 * Configuration class for Facebook Messenger API
 */
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

    // Default values
    private static final String DEFAULT_API_VERSION = "v18.0";
    private static final String DEFAULT_API_BASE_URL = "https://graph.facebook.com";

    @Override
    public String getAuthorizationUrl() {
        return String.format("https://www.facebook.com/%s/dialog/oauth",
                            getApiVersion() != null ? getApiVersion() : DEFAULT_API_VERSION);
    }

    @Override
    public String getTokenUrl() {
        return String.format("%s/%s/oauth/access_token",
                            getApiBaseUrl() != null ? getApiBaseUrl() : DEFAULT_API_BASE_URL,
                            getApiVersion() != null ? getApiVersion() : DEFAULT_API_VERSION);
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
                            getApiBaseUrl() != null ? getApiBaseUrl() : DEFAULT_API_BASE_URL,
                            getApiVersion() != null ? getApiVersion() : DEFAULT_API_VERSION);
    }

    /**
     * Get the full API endpoint for user profile
     */
    public String getUserProfileEndpoint(String userId) {
        return String.format("%s/%s/%s",
                            getApiBaseUrl() != null ? getApiBaseUrl() : DEFAULT_API_BASE_URL,
                            getApiVersion() != null ? getApiVersion() : DEFAULT_API_VERSION,
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
}
