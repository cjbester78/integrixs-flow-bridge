package com.integrixs.adapters.social.youtube;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import com.integrixs.adapters.social.base.SocialMediaAdapterConfig;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "integrixs.adapters.youtube.data")
public class YouTubeDataApiConfig extends SocialMediaAdapterConfig {

    private String clientId;
    private String clientSecret;
    private String channelId;
    private String uploadPlaylistId; // Usually "uploads" playlist
    private String uploadBaseUrl;
    private YouTubeDataFeatures features = new YouTubeDataFeatures();
    private YouTubeDataLimits limits = new YouTubeDataLimits();

    @Override
    public String getPlatformName() {
        return "youtube-data";
    }

    @Override
    public String getAuthorizationUrl() {
        return "https://accounts.google.com/o/oauth2/v2/auth";
    }

    @Override
    public String getTokenUrl() {
        return "https://oauth2.googleapis.com/token";
    }

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

        // Getters and setters
        public boolean isEnableVideoUpload() {
            return enableVideoUpload;
        }

        public void setEnableVideoUpload(boolean enableVideoUpload) {
            this.enableVideoUpload = enableVideoUpload;
        }

        public boolean isEnableVideoManagement() {
            return enableVideoManagement;
        }

        public void setEnableVideoManagement(boolean enableVideoManagement) {
            this.enableVideoManagement = enableVideoManagement;
        }

        public boolean isEnablePlaylistManagement() {
            return enablePlaylistManagement;
        }

        public void setEnablePlaylistManagement(boolean enablePlaylistManagement) {
            this.enablePlaylistManagement = enablePlaylistManagement;
        }

        public boolean isEnableCommentManagement() {
            return enableCommentManagement;
        }

        public void setEnableCommentManagement(boolean enableCommentManagement) {
            this.enableCommentManagement = enableCommentManagement;
        }

        public boolean isEnableLiveStreaming() {
            return enableLiveStreaming;
        }

        public void setEnableLiveStreaming(boolean enableLiveStreaming) {
            this.enableLiveStreaming = enableLiveStreaming;
        }

        public boolean isEnableCommunityPosts() {
            return enableCommunityPosts;
        }

        public void setEnableCommunityPosts(boolean enableCommunityPosts) {
            this.enableCommunityPosts = enableCommunityPosts;
        }

        public boolean isEnableChannelManagement() {
            return enableChannelManagement;
        }

        public void setEnableChannelManagement(boolean enableChannelManagement) {
            this.enableChannelManagement = enableChannelManagement;
        }

        public boolean isEnableSubscriberManagement() {
            return enableSubscriberManagement;
        }

        public void setEnableSubscriberManagement(boolean enableSubscriberManagement) {
            this.enableSubscriberManagement = enableSubscriberManagement;
        }

        public boolean isEnableAnalytics() {
            return enableAnalytics;
        }

        public void setEnableAnalytics(boolean enableAnalytics) {
            this.enableAnalytics = enableAnalytics;
        }

        public boolean isEnableCaptions() {
            return enableCaptions;
        }

        public void setEnableCaptions(boolean enableCaptions) {
            this.enableCaptions = enableCaptions;
        }

        public boolean isEnableThumbnailUpload() {
            return enableThumbnailUpload;
        }

        public void setEnableThumbnailUpload(boolean enableThumbnailUpload) {
            this.enableThumbnailUpload = enableThumbnailUpload;
        }

        public boolean isEnableVideoEditing() {
            return enableVideoEditing;
        }

        public void setEnableVideoEditing(boolean enableVideoEditing) {
            this.enableVideoEditing = enableVideoEditing;
        }

        public boolean isEnableMonetization() {
            return enableMonetization;
        }

        public void setEnableMonetization(boolean enableMonetization) {
            this.enableMonetization = enableMonetization;
        }

        public boolean isEnableContentId() {
            return enableContentId;
        }

        public void setEnableContentId(boolean enableContentId) {
            this.enableContentId = enableContentId;
        }

        public boolean isEnablePremiere() {
            return enablePremiere;
        }

        public void setEnablePremiere(boolean enablePremiere) {
            this.enablePremiere = enablePremiere;
        }

        public boolean isEnableStories() {
            return enableStories;
        }

        public void setEnableStories(boolean enableStories) {
            this.enableStories = enableStories;
        }

        public boolean isEnableShorts() {
            return enableShorts;
        }

        public void setEnableShorts(boolean enableShorts) {
            this.enableShorts = enableShorts;
        }

        public boolean isEnableMemberships() {
            return enableMemberships;
        }

        public void setEnableMemberships(boolean enableMemberships) {
            this.enableMemberships = enableMemberships;
        }

        public boolean isEnableSuperChat() {
            return enableSuperChat;
        }

        public void setEnableSuperChat(boolean enableSuperChat) {
            this.enableSuperChat = enableSuperChat;
        }

        public boolean isEnableMerchandise() {
            return enableMerchandise;
        }

        public void setEnableMerchandise(boolean enableMerchandise) {
            this.enableMerchandise = enableMerchandise;
        }
    }

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

        // Getters and setters
        public long getMaxVideoSize() {
            return maxVideoSize;
        }

        public void setMaxVideoSize(long maxVideoSize) {
            this.maxVideoSize = maxVideoSize;
        }

        public int getMaxVideoLength() {
            return maxVideoLength;
        }

        public void setMaxVideoLength(int maxVideoLength) {
            this.maxVideoLength = maxVideoLength;
        }

        public int getMaxTitleLength() {
            return maxTitleLength;
        }

        public void setMaxTitleLength(int maxTitleLength) {
            this.maxTitleLength = maxTitleLength;
        }

        public int getMaxDescriptionLength() {
            return maxDescriptionLength;
        }

        public void setMaxDescriptionLength(int maxDescriptionLength) {
            this.maxDescriptionLength = maxDescriptionLength;
        }

        public int getMaxTags() {
            return maxTags;
        }

        public void setMaxTags(int maxTags) {
            this.maxTags = maxTags;
        }

        public int getMaxPlaylistItems() {
            return maxPlaylistItems;
        }

        public void setMaxPlaylistItems(int maxPlaylistItems) {
            this.maxPlaylistItems = maxPlaylistItems;
        }

        public int getMaxPlaylistsPerChannel() {
            return maxPlaylistsPerChannel;
        }

        public void setMaxPlaylistsPerChannel(int maxPlaylistsPerChannel) {
            this.maxPlaylistsPerChannel = maxPlaylistsPerChannel;
        }

        public int getMaxCommentLength() {
            return maxCommentLength;
        }

        public void setMaxCommentLength(int maxCommentLength) {
            this.maxCommentLength = maxCommentLength;
        }

        public int getMaxThumbnailSize() {
            return maxThumbnailSize;
        }

        public void setMaxThumbnailSize(int maxThumbnailSize) {
            this.maxThumbnailSize = maxThumbnailSize;
        }

        public int getMaxCaptionSize() {
            return maxCaptionSize;
        }

        public void setMaxCaptionSize(int maxCaptionSize) {
            this.maxCaptionSize = maxCaptionSize;
        }

        public int getDailyUploadLimit() {
            return dailyUploadLimit;
        }

        public void setDailyUploadLimit(int dailyUploadLimit) {
            this.dailyUploadLimit = dailyUploadLimit;
        }

        public int getRateLimit() {
            return rateLimit;
        }

        public void setRateLimit(int rateLimit) {
            this.rateLimit = rateLimit;
        }
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
        PRIVATE,   // Only visible to channel owner
        UNLISTED, // Visible to anyone with the link
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
        YOUTUBE,        // Standard YouTube License
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
        HD, // High Definition
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
        YT_AGE_RESTRICTED, // Age - restricted content
        MADE_FOR_KIDS,     // Made for kids
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
        DATE,       // Newest first
        RATING,     // Highest rated first
        RELEVANCE, // Most relevant first
        TITLE,      // Alphabetical by title
        VIDEO_COUNT, // Most videos first(for playlists)
        VIEW_COUNT   // Most viewed first
    }

    // Safe search options
    public enum SafeSearch {
        NONE,     // No filtering
        MODERATE, // Filter some content
        STRICT     // Filter all potentially inappropriate content
    }
    // Getters and Setters
    public String getClientId() {
        return clientId;
    }
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    public String getClientSecret() {
        return clientSecret;
    }
    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }
    public String getChannelId() {
        return channelId;
    }
    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }
    public String getUploadPlaylistId() {
        return uploadPlaylistId;
    }
    public void setUploadPlaylistId(String uploadPlaylistId) {
        this.uploadPlaylistId = uploadPlaylistId;
    }
    public String getUploadBaseUrl() {
        return uploadBaseUrl;
    }
    public void setUploadBaseUrl(String uploadBaseUrl) {
        this.uploadBaseUrl = uploadBaseUrl;
    }
    public YouTubeDataFeatures getFeatures() {
        return features;
    }
    public void setFeatures(YouTubeDataFeatures features) {
        this.features = features;
    }
    public YouTubeDataLimits getLimits() {
        return limits;
    }
    public void setLimits(YouTubeDataLimits limits) {
        this.limits = limits;
    }
}
