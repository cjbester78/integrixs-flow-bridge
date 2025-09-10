package com.integrixs.adapters.social.linkedin;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import com.integrixs.adapters.social.base.SocialMediaAdapterConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Component
@ConfigurationProperties(prefix = "integrixs.adapters.linkedin.api")
@EqualsAndHashCode(callSuper = true)
public class LinkedInApiConfig extends SocialMediaAdapterConfig {
    
    private String clientId;
    private String clientSecret;
    private String memberUrn; // Member URN (e.g., urn:li:person:123ABC)
    private String organizationId; // Organization ID for company pages
    private LinkedInFeatures features = new LinkedInFeatures();
    private LinkedInLimits limits = new LinkedInLimits();
    
    @Data
    public static class LinkedInFeatures {
        private boolean enableProfileAccess = true;
        private boolean enableCompanyPageManagement = true;
        private boolean enableArticlePublishing = true;
        private boolean enablePostSharing = true;
        private boolean enableVideoSharing = true;
        private boolean enableImageSharing = true;
        private boolean enableDocumentSharing = true;
        private boolean enableCommentManagement = true;
        private boolean enableMessaging = true;
        private boolean enableConnectionManagement = true;
        private boolean enableAnalytics = true;
        private boolean enableEventCreation = true;
        private boolean enableGroupManagement = false;
        private boolean enableLiveVideo = false;
        private boolean enableNewsletters = true;
        private boolean enableHashtagTracking = true;
        private boolean enableEmployeeAdvocacy = true;
        private boolean enableContentSuggestions = true;
        private boolean enableAudienceTargeting = true;
        private boolean enableScheduledPosts = true;
    }
    
    @Data
    public static class LinkedInLimits {
        private int maxPostLength = 3000; // characters
        private int maxArticleLength = 125000; // characters
        private int maxCommentLength = 1250; // characters
        private int maxHashtagsPerPost = 30;
        private int maxMentionsPerPost = 50;
        private int maxImagesPerPost = 20;
        private int maxVideoSizeMB = 5120; // 5GB
        private int maxVideoLength = 600; // 10 minutes in seconds
        private int maxImageSizeMB = 10;
        private int maxDocumentSizeMB = 100;
        private int maxConnectionsPerDay = 100;
        private int maxMessagesPerDay = 250;
        private int maxPostsPerDay = 100;
        private int maxBatchSize = 50; // for batch operations
    }
    
    // Post visibility types
    public enum Visibility {
        CONNECTIONS, // Visible to 1st-degree connections
        PUBLIC,      // Visible to anyone on LinkedIn
        LOGGED_IN,   // Visible to logged-in LinkedIn members
        CONTAINER   // Visible only within a container (e.g., a group)
    }
    
    // Share media categories
    public enum MediaCategory {
        NONE,
        ARTICLE,
        IMAGE,
        VIDEO,
        NATIVE_DOCUMENT,
        LIVE_VIDEO,
        LEARNING_COURSE
    }
    
    // Content distribution types
    public enum Distribution {
        MAIN_FEED,
        NONE,
        CONTAINER_ENTITY
    }
    
    // Analytics metrics
    public enum MetricType {
        IMPRESSIONS,
        CLICKS,
        ENGAGEMENT,
        ENGAGEMENT_RATE,
        LIKES,
        COMMENTS,
        SHARES,
        FOLLOWS,
        VIDEO_VIEWS,
        VIDEO_COMPLETION_RATE,
        CLICK_THROUGH_RATE,
        SOCIAL_ACTIONS,
        UNIQUE_IMPRESSIONS
    }
    
    // Connection types
    public enum ConnectionType {
        FIRST_DEGREE,
        SECOND_DEGREE,
        THIRD_DEGREE,
        OUT_OF_NETWORK
    }
    
    // Organization role types
    public enum OrganizationRole {
        ADMIN,
        ANALYST,
        ASSOCIATE,
        CONTENT_ADMIN,
        CURATOR,
        LEAD_GEN_FORMS_MANAGER,
        MANAGER,
        MESSAGING_AGENT,
        PAID_MEDIA_ADMIN,
        RECRUITER,
        RECRUITMENT_ADMIN
    }
    
    // Profile fields available
    public enum ProfileField {
        ID,
        FIRST_NAME,
        LAST_NAME,
        MAIDEN_NAME,
        LOCALIZED_FIRST_NAME,
        LOCALIZED_LAST_NAME,
        HEADLINE,
        VANITY_NAME,
        LOCATION,
        INDUSTRY,
        SUMMARY,
        SPECIALTIES,
        POSITIONS,
        PICTURE_URL,
        SITE_STANDARD_PROFILE_REQUEST,
        API_STANDARD_PROFILE_REQUEST,
        PUBLIC_PROFILE_URL,
        EMAIL_ADDRESS,
        NUM_CONNECTIONS,
        NUM_CONNECTIONS_CAPPED,
        CURRENT_SHARE,
        TIMESTAMP,
        BOUND_ACCOUNT_TYPES,
        PHONE_NUMBERS,
        IM_ACCOUNTS
    }
    
    // Activity types for analytics
    public enum ActivityType {
        POST_SHARE,
        ARTICLE_SHARE,
        VIDEO_SHARE,
        IMAGE_SHARE,
        DOCUMENT_SHARE,
        POLL_SHARE,
        EVENT_SHARE,
        CELEBRATION_CREATE,
        NATIVE_DOCUMENT_SHARE,
        COMMENT,
        REACTION,
        FOLLOW
    }
    
    // Reaction types
    public enum ReactionType {
        LIKE,
        PRAISE,
        APPRECIATION,
        EMPATHY,
        INTEREST,
        CELEBRATION,
        ENTERTAINMENT,
        MAYBE
    }
}