package com.integrixs.adapters.social.instagram;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import com.integrixs.adapters.social.base.SocialMediaAdapterConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Component
@ConfigurationProperties(prefix = "integrixs.adapters.instagram.graph")
@EqualsAndHashCode(callSuper = true)
public class InstagramGraphApiConfig extends SocialMediaAdapterConfig {
    
    private String instagramBusinessAccountId;
    private String facebookPageId;
    private InstagramFeatures features = new InstagramFeatures();
    
    @Data
    public static class InstagramFeatures {
        private boolean enablePostPublishing = true;
        private boolean enableStories = true;
        private boolean enableReels = true;
        private boolean enableCommentManagement = true;
        private boolean enableHashtagAnalytics = true;
        private boolean enableUserInsights = true;
        private boolean enableShoppingTags = true;
        private boolean enableIGTV = true;
        private boolean enableContentScheduling = true;
        private boolean enableMentionMonitoring = true;
        private boolean enableCarouselPosts = true;
        private boolean enableProductTagging = true;
    }
    
    // Content types
    public enum ContentType {
        IMAGE,
        VIDEO,
        CAROUSEL_ALBUM,
        REELS,
        STORIES,
        IGTV
    }
    
    // Media types for upload
    public enum MediaType {
        IMAGE("image/jpeg", "image/png", "image/gif"),
        VIDEO("video/mp4", "video/mpeg", "video/quicktime");
        
        private final String[] mimeTypes;
        
        MediaType(String... mimeTypes) {
            this.mimeTypes = mimeTypes;
        }
        
        public String[] getMimeTypes() {
            return mimeTypes;
        }
    }
    
    // Insights metrics
    public enum InsightMetric {
        IMPRESSIONS,
        REACH,
        PROFILE_VIEWS,
        WEBSITE_CLICKS,
        EMAIL_CONTACTS,
        GET_DIRECTIONS_CLICKS,
        PHONE_CALL_CLICKS,
        TEXT_MESSAGE_CLICKS,
        FOLLOWER_COUNT,
        ONLINE_FOLLOWERS,
        AUDIENCE_CITY,
        AUDIENCE_COUNTRY,
        AUDIENCE_GENDER_AGE,
        AUDIENCE_LOCALE
    }
    
    // Shopping product availability
    public enum ProductAvailability {
        IN_STOCK,
        OUT_OF_STOCK,
        PREORDER,
        AVAILABLE_FOR_ORDER,
        DISCONTINUED
    }
}