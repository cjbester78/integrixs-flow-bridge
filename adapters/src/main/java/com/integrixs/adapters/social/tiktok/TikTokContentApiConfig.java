package com.integrixs.adapters.social.tiktok;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import com.integrixs.adapters.social.base.SocialMediaAdapterConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "integrixs.adapters.tiktok.content")
@EqualsAndHashCode(callSuper = true)
public class TikTokContentApiConfig extends SocialMediaAdapterConfig {
    
    private String clientKey;
    private String clientSecret;
    private String userId;
    private String username;
    private TikTokContentFeatures features = new TikTokContentFeatures();
    private TikTokContentLimits limits = new TikTokContentLimits();
    
    @Data
    public static class TikTokContentFeatures {
        private boolean enableVideoPublishing = true;
        private boolean enableVideoRetrieval = true;
        private boolean enableCommentManagement = true;
        private boolean enableHashtagAnalytics = true;
        private boolean enableTrendingContent = true;
        private boolean enableUserProfile = true;
        private boolean enableFollowerAnalytics = true;
        private boolean enableEngagementMetrics = true;
        private boolean enableMusicIntegration = true;
        private boolean enableEffectsManagement = true;
        private boolean enableDuetStitch = true;
        private boolean enableLiveStreaming = true;
        private boolean enableCreatorSearch = true;
        private boolean enableContentDiscovery = true;
        private boolean enableSoundLibrary = true;
        private boolean enableContentInsights = true;
        private boolean enableViralTrends = true;
        private boolean enableChallengeParticipation = true;
        private boolean enableCollaboration = true;
        private boolean enableAnalyticsExport = true;
    }
    
    @Data
    public static class TikTokContentLimits {
        private int maxVideoDurationSeconds = 180; // 3 minutes for most accounts
        private int maxVideoSizeMB = 287; // TikTok limit
        private int maxDescriptionLength = 2200;
        private int maxHashtagsPerPost = 100;
        private int maxMentionsPerPost = 30;
        private int maxVideosPerDay = 10;
        private int maxCommentsToRetrieve = 1000;
        private int maxFollowersToRetrieve = 10000;
        private int maxTrendingVideos = 100;
        private int maxSearchResults = 100;
        private int minVideoDurationSeconds = 3;
        private int maxCaptionLength = 150; // For display
        private int analyticsRetentionDays = 90;
    }
    
    // Video privacy settings
    public enum VideoPrivacy {
        PUBLIC_TO_EVERYONE,
        FRIENDS_ONLY,
        PRIVATE_ONLY_ME
    }
    
    // Video status
    public enum VideoStatus {
        PUBLISHED,
        DRAFT,
        PRIVATE,
        UNDER_REVIEW,
        REMOVED,
        SCHEDULED
    }
    
    // Content categories
    public enum ContentCategory {
        DANCE,
        MUSIC,
        COMEDY,
        SPORTS,
        EDUCATION,
        LIFESTYLE,
        FASHION,
        BEAUTY,
        FOOD,
        TRAVEL,
        PETS,
        GAMING,
        TECHNOLOGY,
        ART,
        DIY,
        FITNESS,
        ENTERTAINMENT,
        NEWS,
        FAMILY,
        BUSINESS
    }
    
    // Engagement types
    public enum EngagementType {
        LIKE,
        COMMENT,
        SHARE,
        FOLLOW,
        VIEW,
        SAVE,
        DUET,
        STITCH,
        DOWNLOAD
    }
    
    // Comment filter
    public enum CommentFilter {
        ALL,
        FRIENDS_ONLY,
        NO_ONE,
        FILTERED_KEYWORDS
    }
    
    // Video source
    public enum VideoSource {
        CAMERA,
        UPLOAD,
        DUET,
        STITCH,
        TEMPLATE,
        EFFECT,
        LIVE_REPLAY
    }
    
    // Trending type
    public enum TrendingType {
        HASHTAG,
        SOUND,
        EFFECT,
        CHALLENGE,
        CREATOR,
        VIDEO
    }
    
    // Analytics metric
    public enum AnalyticsMetric {
        VIEWS,
        LIKES,
        COMMENTS,
        SHARES,
        COMPLETION_RATE,
        AVERAGE_WATCH_TIME,
        TOTAL_WATCH_TIME,
        UNIQUE_VIEWERS,
        FOLLOWERS_GAINED,
        PROFILE_VIEWS,
        ENGAGEMENT_RATE,
        REACH,
        IMPRESSIONS,
        CLICK_THROUGH_RATE,
        SAVES
    }
    
    // Time period for analytics
    public enum TimePeriod {
        LAST_7_DAYS,
        LAST_28_DAYS,
        LAST_60_DAYS,
        LAST_90_DAYS,
        CUSTOM
    }
    
    // Video resolution
    public enum VideoResolution {
        RESOLUTION_540P("960x540"),
        RESOLUTION_720P("1280x720"),
        RESOLUTION_1080P("1920x1080");
        
        private final String resolution;
        
        VideoResolution(String resolution) {
            this.resolution = resolution;
        }
        
        public String getResolution() { return resolution; }
    }
    
    // Video aspect ratio
    public enum VideoAspectRatio {
        RATIO_9_16("9:16"),  // Standard TikTok
        RATIO_1_1("1:1"),    // Square
        RATIO_16_9("16:9");  // Horizontal
        
        private final String ratio;
        
        VideoAspectRatio(String ratio) {
            this.ratio = ratio;
        }
        
        public String getRatio() { return ratio; }
    }
    
    // Music/Sound categories
    public enum SoundCategory {
        TRENDING,
        RECOMMENDED,
        FAVORITES,
        LOCAL,
        DISCOVER,
        GENRES,
        MOODS,
        THEMES,
        ORIGINAL_SOUNDS
    }
    
    // Effect types
    public enum EffectType {
        BEAUTY,
        FUNNY,
        WORLD,
        ANIMAL,
        EDITING,
        TRENDING,
        NEW,
        INTERACTIVE,
        TRANSITION
    }
    
    // Collaboration type
    public enum CollaborationType {
        DUET,
        STITCH,
        REACT,
        COLLAB,
        RESPONSE
    }
    
    // Discovery feed type
    public enum DiscoveryFeedType {
        FOR_YOU,
        FOLLOWING,
        LIVE,
        EXPLORE,
        NEARBY
    }
    
    // Creator type
    public enum CreatorType {
        INDIVIDUAL,
        BUSINESS,
        CREATOR_FUND,
        VERIFIED,
        POPULAR_CREATOR
    }
    
    // Content moderation status
    public enum ModerationStatus {
        PENDING,
        APPROVED,
        REJECTED,
        FLAGGED,
        SHADOW_BANNED,
        AGE_RESTRICTED
    }
    
    // Notification type
    public enum NotificationType {
        NEW_FOLLOWER,
        LIKE,
        COMMENT,
        MENTION,
        VIDEO_POSTED,
        LIVE_STARTED,
        DUET_CREATED,
        MILESTONE_REACHED
    }
    
    // Live stream status
    public enum LiveStreamStatus {
        SCHEDULED,
        LIVE,
        ENDED,
        CANCELLED,
        INTERRUPTED
    }
    
    // Challenge participation status
    public enum ChallengeStatus {
        ACTIVE,
        UPCOMING,
        ENDED,
        PARTICIPATING,
        COMPLETED
    }
}