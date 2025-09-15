package com.integrixs.adapters.social.facebook;

import com.integrixs.adapters.social.base.SocialMediaAdapterConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Configuration for Facebook Graph API adapter
 */
@Component
@ConfigurationProperties(prefix = "integrixs.adapters.facebook.graph")
public class FacebookGraphApiConfig extends SocialMediaAdapterConfig {

    // Facebook specific configuration
    private String pageId;
    private String pageAccessToken;
    private String appId;
    private String appSecret;

    // API Configuration
    private String graphApiVersion = "v18.0";
    private String baseUrl = "https://graph.facebook.com";

    // Feature toggles
    private FacebookFeatures features = new FacebookFeatures();

    // Content settings
    private ContentSettings contentSettings = new ContentSettings();

    // Permissions required
    private List<String> requiredPermissions = List.of(
        "pages_show_list",
        "pages_read_engagement",
        "pages_manage_posts",
        "pages_read_user_content",
        "pages_manage_engagement",
        "read_insights",
        "ads_management" // For ads integration
   );

        public static class FacebookFeatures {
        private boolean enablePageManagement = true;
        private boolean enableInsights = true;
        private boolean enableComments = true;
        private boolean enableMessaging = false;
        private boolean enableLiveVideo = false;
        private boolean enableStories = true;
        private boolean enableReels = true;
        private boolean enableScheduling = true;
        private boolean enableAudienceTargeting = true;
    }

        public static class ContentSettings {
        private int maxTextLength = 63206; // Facebook's max post length
        private int maxHashtags = 30;
        private int maxMentions = 50;
        private int maxMediaItems = 10;
        private long maxVideoSizeMb = 4096; // 4GB max video size
        private int maxVideoDurationMinutes = 240; // 4 hours max
        private List<String> supportedImageFormats = List.of("jpg", "jpeg", "png", "gif", "webp");
        private List<String> supportedVideoFormats = List.of("mp4", "mov", "avi");
        private boolean autoHashtagGeneration = false;
        private boolean profanityFilter = true;
    }

    /**
     * Get the full API URL for a given endpoint
     */
    public String getApiUrl(String endpoint) {
        return String.format("%s/%s/%s", baseUrl, graphApiVersion, endpoint);
    }

    /**
     * Check if we have valid page access token
     */
    public boolean hasValidPageToken() {
        return pageAccessToken != null && !pageAccessToken.isEmpty();
    }

    /**
     * Get configured scopes for OAuth2
     */
    @Override
    public String[] getScopes() {
        if(super.getScopes() == null || super.getScopes().length == 0) {
            return requiredPermissions.toArray(new String[0]);
        }
        return super.getScopes();
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
    public String getGraphApiVersion() {
        return graphApiVersion;
    }
    public void setGraphApiVersion(String graphApiVersion) {
        this.graphApiVersion = graphApiVersion;
    }
    public String getBaseUrl() {
        return baseUrl;
    }
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    public FacebookFeatures getFeatures() {
        return features;
    }
    public void setFeatures(FacebookFeatures features) {
        this.features = features;
    }
    public ContentSettings getContentSettings() {
        return contentSettings;
    }
    public void setContentSettings(ContentSettings contentSettings) {
        this.contentSettings = contentSettings;
    }
    public List<String> getRequiredPermissions() {
        return requiredPermissions;
    }
    public void setRequiredPermissions(List<String> requiredPermissions) {
        this.requiredPermissions = requiredPermissions;
    }
    public boolean isEnablePageManagement() {
        return enablePageManagement;
    }
    public void setEnablePageManagement(boolean enablePageManagement) {
        this.enablePageManagement = enablePageManagement;
    }
    public boolean isEnableInsights() {
        return enableInsights;
    }
    public void setEnableInsights(boolean enableInsights) {
        this.enableInsights = enableInsights;
    }
    public boolean isEnableComments() {
        return enableComments;
    }
    public void setEnableComments(boolean enableComments) {
        this.enableComments = enableComments;
    }
    public boolean isEnableMessaging() {
        return enableMessaging;
    }
    public void setEnableMessaging(boolean enableMessaging) {
        this.enableMessaging = enableMessaging;
    }
    public boolean isEnableLiveVideo() {
        return enableLiveVideo;
    }
    public void setEnableLiveVideo(boolean enableLiveVideo) {
        this.enableLiveVideo = enableLiveVideo;
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
    public boolean isEnableScheduling() {
        return enableScheduling;
    }
    public void setEnableScheduling(boolean enableScheduling) {
        this.enableScheduling = enableScheduling;
    }
    public boolean isEnableAudienceTargeting() {
        return enableAudienceTargeting;
    }
    public void setEnableAudienceTargeting(boolean enableAudienceTargeting) {
        this.enableAudienceTargeting = enableAudienceTargeting;
    }
    public int getMaxTextLength() {
        return maxTextLength;
    }
    public void setMaxTextLength(int maxTextLength) {
        this.maxTextLength = maxTextLength;
    }
    public int getMaxHashtags() {
        return maxHashtags;
    }
    public void setMaxHashtags(int maxHashtags) {
        this.maxHashtags = maxHashtags;
    }
    public int getMaxMentions() {
        return maxMentions;
    }
    public void setMaxMentions(int maxMentions) {
        this.maxMentions = maxMentions;
    }
    public int getMaxMediaItems() {
        return maxMediaItems;
    }
    public void setMaxMediaItems(int maxMediaItems) {
        this.maxMediaItems = maxMediaItems;
    }
    public long getMaxVideoSizeMb() {
        return maxVideoSizeMb;
    }
    public void setMaxVideoSizeMb(long maxVideoSizeMb) {
        this.maxVideoSizeMb = maxVideoSizeMb;
    }
    public int getMaxVideoDurationMinutes() {
        return maxVideoDurationMinutes;
    }
    public void setMaxVideoDurationMinutes(int maxVideoDurationMinutes) {
        this.maxVideoDurationMinutes = maxVideoDurationMinutes;
    }
    public List<String> getSupportedImageFormats() {
        return supportedImageFormats;
    }
    public void setSupportedImageFormats(List<String> supportedImageFormats) {
        this.supportedImageFormats = supportedImageFormats;
    }
    public List<String> getSupportedVideoFormats() {
        return supportedVideoFormats;
    }
    public void setSupportedVideoFormats(List<String> supportedVideoFormats) {
        this.supportedVideoFormats = supportedVideoFormats;
    }
    public boolean isAutoHashtagGeneration() {
        return autoHashtagGeneration;
    }
    public void setAutoHashtagGeneration(boolean autoHashtagGeneration) {
        this.autoHashtagGeneration = autoHashtagGeneration;
    }
    public boolean isProfanityFilter() {
        return profanityFilter;
    }
    public void setProfanityFilter(boolean profanityFilter) {
        this.profanityFilter = profanityFilter;
    }
}
