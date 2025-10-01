package com.integrixs.adapters.social.instagram;

import com.integrixs.adapters.social.base.SocialMediaAdapterConfig;
/**
 * Configuration class for Instagram Graph API
 */
public class InstagramGraphApiConfig extends SocialMediaAdapterConfig {
    public InstagramGraphApiConfig() {
    }


    // Instagram specific configuration
    private String instagramBusinessAccountId;
    private String facebookPageId;
    private String facebookPageAccessToken;

    // Instagram API settings
    private boolean enableInsights;
    private boolean enableStories;
    private boolean enableReels;
    private boolean enableShopping;
    private boolean enableCommentFiltering;

    // Content moderation
    private boolean autoHideOffensiveComments;
    private String[] blockedKeywords;

    // API limits
    private Integer mediaLimit = 25;
    private Integer commentsLimit = 50;
    private Integer mentionsLimit = 25;
    private Integer insightsMediaLimit = 10;

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
        return "instagram";
    }

    /**
     * Get the base URL for API calls
     */
    public String getBaseUrl() {
        String baseUrl = getApiBaseUrl() != null ? getApiBaseUrl() : DEFAULT_API_BASE_URL;
        String version = getApiVersion() != null ? getApiVersion() : DEFAULT_API_VERSION;
        return String.format("%s/%s", baseUrl, version);
    }

    /**
     * Get the full API endpoint for media
     */
    public String getMediaEndpoint() {
        return String.format("%s/%s/media", getBaseUrl(), instagramBusinessAccountId);
    }

    /**
     * Get the full API endpoint for insights
     */
    public String getInsightsEndpoint() {
        return String.format("%s/%s/insights", getBaseUrl(), instagramBusinessAccountId);
    }
    // Getters and Setters
    public String getInstagramBusinessAccountId() {
        return instagramBusinessAccountId;
    }
    public void setInstagramBusinessAccountId(String instagramBusinessAccountId) {
        this.instagramBusinessAccountId = instagramBusinessAccountId;
    }
    public String getFacebookPageId() {
        return facebookPageId;
    }
    public void setFacebookPageId(String facebookPageId) {
        this.facebookPageId = facebookPageId;
    }
    public String getFacebookPageAccessToken() {
        return facebookPageAccessToken;
    }
    public void setFacebookPageAccessToken(String facebookPageAccessToken) {
        this.facebookPageAccessToken = facebookPageAccessToken;
    }
    public boolean isEnableInsights() {
        return enableInsights;
    }
    public void setEnableInsights(boolean enableInsights) {
        this.enableInsights = enableInsights;
    }
    public boolean isEnableStories() {
        return enableStories;
    }
    public void setEnableStories(boolean enableStories) {
        this.enableStories = enableStories;
    }
    public boolean isEnableReels() {
        return enableReels;
    }
    public void setEnableReels(boolean enableReels) {
        this.enableReels = enableReels;
    }
    public boolean isEnableShopping() {
        return enableShopping;
    }
    public void setEnableShopping(boolean enableShopping) {
        this.enableShopping = enableShopping;
    }
    public boolean isEnableCommentFiltering() {
        return enableCommentFiltering;
    }
    public void setEnableCommentFiltering(boolean enableCommentFiltering) {
        this.enableCommentFiltering = enableCommentFiltering;
    }
    public boolean isAutoHideOffensiveComments() {
        return autoHideOffensiveComments;
    }
    public void setAutoHideOffensiveComments(boolean autoHideOffensiveComments) {
        this.autoHideOffensiveComments = autoHideOffensiveComments;
    }
    public String[] getBlockedKeywords() {
        return blockedKeywords;
    }
    public void setBlockedKeywords(String[] blockedKeywords) {
        this.blockedKeywords = blockedKeywords;
    }
    public Integer getMediaLimit() {
        return mediaLimit;
    }
    public void setMediaLimit(Integer mediaLimit) {
        this.mediaLimit = mediaLimit;
    }
    public Integer getCommentsLimit() {
        return commentsLimit;
    }
    public void setCommentsLimit(Integer commentsLimit) {
        this.commentsLimit = commentsLimit;
    }
    public Integer getMentionsLimit() {
        return mentionsLimit;
    }
    public void setMentionsLimit(Integer mentionsLimit) {
        this.mentionsLimit = mentionsLimit;
    }
    public Integer getInsightsMediaLimit() {
        return insightsMediaLimit;
    }
    public void setInsightsMediaLimit(Integer insightsMediaLimit) {
        this.insightsMediaLimit = insightsMediaLimit;
    }
}
