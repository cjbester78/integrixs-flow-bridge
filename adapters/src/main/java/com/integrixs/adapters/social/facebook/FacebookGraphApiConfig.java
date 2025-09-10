package com.integrixs.adapters.social.facebook;

import com.integrixs.adapters.social.base.SocialMediaAdapterConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Configuration for Facebook Graph API adapter
 */
@Data
@EqualsAndHashCode(callSuper = true)
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
        "ads_management"  // For ads integration
    );
    
    @Data
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
    
    @Data
    public static class ContentSettings {
        private int maxTextLength = 63206;  // Facebook's max post length
        private int maxHashtags = 30;
        private int maxMentions = 50;
        private int maxMediaItems = 10;
        private long maxVideoSizeMb = 4096;  // 4GB max video size
        private int maxVideoDurationMinutes = 240;  // 4 hours max
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
        if (super.getScopes() == null || super.getScopes().length == 0) {
            return requiredPermissions.toArray(new String[0]);
        }
        return super.getScopes();
    }
}