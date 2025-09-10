package com.integrixs.adapters.social.youtube;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import com.integrixs.adapters.social.base.SocialMediaAdapterConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "integrixs.adapters.youtube.data")
@EqualsAndHashCode(callSuper = true)
public class YouTubeDataApiConfig extends SocialMediaAdapterConfig {
    
    private String clientId;
    private String clientSecret;
    private String channelId;
    private String uploadPlaylistId; // Usually "uploads" playlist
    private YouTubeDataFeatures features = new YouTubeDataFeatures();
    private YouTubeDataLimits limits = new YouTubeDataLimits();
    
    @Data
    public static class YouTubeDataFeatures {
        private boolean enableVideoUpload = true;
        private boolean enableVideoManagement = true;
        private boolean enablePlaylistManagement = true;
        private boolean enableCommentManagement = true;
        private boolean enableLiveStreaming = true;
        private boolean enableCommunityPosts = true;
        private boolean enableChannelManagement = true;
        private boolean enableSubscriberManagement = true;
        private boolean enableAnalytics = true;
        private boolean enableCaptions = true;
        private boolean enableThumbnailUpload = true;
        private boolean enableVideoEditing = true;
        private boolean enableMonetization = true;
        private boolean enableContentId = true;
        private boolean enablePremiere = true;
        private boolean enableStories = true;
        private boolean enableShorts = true;
        private boolean enableMemberships = true;
        private boolean enableSuperChat = true;
        private boolean enableMerchandise = true;
    }
    
    @Data
    public static class YouTubeDataLimits {
        private long maxVideoSize = 128L * 1024 * 1024 * 1024; // 128GB
        private int maxVideoLength = 12 * 60 * 60; // 12 hours in seconds
        private int maxTitleLength = 100;
        private int maxDescriptionLength = 5000;
        private int maxTags = 500;
        private int maxPlaylistItems = 5000;
        private int maxPlaylistsPerChannel = 200;
        private int maxCommentLength = 10000;
        private int maxThumbnailSize = 2 * 1024 * 1024; // 2MB
        private int maxCaptionSize = 1 * 1024 * 1024; // 1MB
        private int dailyUploadLimit = 50;
        private int rateLimit = 10000; // Quota units per day
    }
    
    // Video categories
    public enum VideoCategory {
        FILM_ANIMATION("1", "Film & Animation"),
        AUTOS_VEHICLES("2", "Autos & Vehicles"),
        MUSIC("10", "Music"),
        PETS_ANIMALS("15", "Pets & Animals"),
        SPORTS("17", "Sports"),
        TRAVEL_EVENTS("19", "Travel & Events"),
        GAMING("20", "Gaming"),
        PEOPLE_BLOGS("22", "People & Blogs"),
        COMEDY("23", "Comedy"),
        ENTERTAINMENT("24", "Entertainment"),
        NEWS_POLITICS("25", "News & Politics"),
        HOWTO_STYLE("26", "Howto & Style"),
        EDUCATION("27", "Education"),
        SCIENCE_TECHNOLOGY("28", "Science & Technology"),
        NONPROFITS_ACTIVISM("29", "Nonprofits & Activism");
        
        private final String id;
        private final String name;
        
        VideoCategory(String id, String name) {
            this.id = id;
            this.name = name;
        }
        
        public String getId() { return id; }
        public String getName() { return name; }
    }
    
    // Privacy status
    public enum PrivacyStatus {
        PRIVATE,    // Only visible to channel owner
        UNLISTED,   // Visible to anyone with the link
        PUBLIC      // Visible to everyone
    }
    
    // Upload status
    public enum UploadStatus {
        UPLOADED,
        PROCESSED,
        FAILED,
        REJECTED,
        DELETED
    }
    
    // License types
    public enum License {
        YOUTUBE,         // Standard YouTube License
        CREATIVE_COMMONS // Creative Commons - Attribution
    }
    
    // Video dimensions
    public enum VideoDimension {
        TWO_D("2d"),
        THREE_D("3d");
        
        private final String value;
        
        VideoDimension(String value) {
            this.value = value;
        }
        
        public String getValue() { return value; }
    }
    
    // Video definition
    public enum VideoDefinition {
        HD,  // High Definition
        SD   // Standard Definition
    }
    
    // Video projection
    public enum VideoProjection {
        RECTANGULAR,
        SPHERICAL // 360° video
    }
    
    // Caption types
    public enum CaptionType {
        STANDARD,
        ASR // Automatic Speech Recognition
    }
    
    // Playlist status
    public enum PlaylistStatus {
        PRIVATE,
        UNLISTED,
        PUBLIC
    }
    
    // Comment moderation status
    public enum CommentModerationStatus {
        PUBLISHED,
        HELD_FOR_REVIEW,
        LIKELY_SPAM,
        REJECTED
    }
    
    // Live broadcast status
    public enum LiveBroadcastStatus {
        UPCOMING,
        ACTIVE,
        COMPLETED,
        REVOKED,
        TEST_STARTING,
        TESTING,
        LIVE_STARTING,
        LIVE,
        RECLAIMED
    }
    
    // Live stream status
    public enum LiveStreamStatus {
        CREATED,
        READY,
        ACTIVE,
        INACTIVE,
        ERROR
    }
    
    // Content rating
    public enum ContentRating {
        YT_AGE_RESTRICTED,  // Age-restricted content
        MADE_FOR_KIDS,      // Made for kids
        NOT_MADE_FOR_KIDS   // Not made for kids
    }
    
    // Monetization status
    public enum MonetizationStatus {
        ENABLED,
        DISABLED,
        PENDING_REVIEW,
        INELIGIBLE
    }
    
    // Thumbnail resolution
    public enum ThumbnailResolution {
        DEFAULT("default", 120, 90),
        MEDIUM("medium", 320, 180),
        HIGH("high", 480, 360),
        STANDARD("standard", 640, 480),
        MAXRES("maxres", 1280, 720);
        
        private final String key;
        private final int width;
        private final int height;
        
        ThumbnailResolution(String key, int width, int height) {
            this.key = key;
            this.width = width;
            this.height = height;
        }
        
        public String getKey() { return key; }
        public int getWidth() { return width; }
        public int getHeight() { return height; }
    }
    
    // Activity types
    public enum ActivityType {
        UPLOAD,
        LIKE,
        FAVORITE,
        COMMENT,
        SUBSCRIPTION,
        PLAYLIST_ITEM,
        RECOMMENDATION,
        BULLETIN,
        SOCIAL,
        CHANNEL_ITEM,
        PROMOTION_ITEM
    }
    
    // Resource types
    public enum ResourceType {
        VIDEO,
        CHANNEL,
        PLAYLIST,
        COMMENT,
        CAPTION,
        BROADCAST,
        STREAM,
        SUBSCRIPTION,
        ACTIVITY,
        CHANNEL_SECTION,
        PLAYLIST_ITEM,
        VIDEO_CATEGORY,
        I18N_LANGUAGE,
        I18N_REGION
    }
    
    // Video parts for API requests
    public enum VideoPart {
        ID,
        SNIPPET,
        CONTENT_DETAILS,
        FILE_DETAILS,
        LIVE_STREAMING_DETAILS,
        LOCALIZATIONS,
        PLAYER,
        PROCESSING_DETAILS,
        RECORDING_DETAILS,
        STATISTICS,
        STATUS,
        SUGGESTIONS,
        TOPIC_DETAILS
    }
    
    // Sorting options
    public enum SortOrder {
        DATE,        // Newest first
        RATING,      // Highest rated first
        RELEVANCE,   // Most relevant first
        TITLE,       // Alphabetical by title
        VIDEO_COUNT, // Most videos first (for playlists)
        VIEW_COUNT   // Most viewed first
    }
    
    // Safe search options
    public enum SafeSearch {
        NONE,      // No filtering
        MODERATE,  // Filter some content
        STRICT     // Filter all potentially inappropriate content
    }
}