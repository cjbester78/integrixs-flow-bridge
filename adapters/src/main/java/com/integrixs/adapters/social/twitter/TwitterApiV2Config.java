package com.integrixs.adapters.social.twitter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import com.integrixs.adapters.social.base.SocialMediaAdapterConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Component
@ConfigurationProperties(prefix = "integrixs.adapters.twitter.api")
@EqualsAndHashCode(callSuper = true)
public class TwitterApiV2Config extends SocialMediaAdapterConfig {
    
    private String apiKey;
    private String apiKeySecret;
    private String bearerToken;
    private String clientId;
    private String clientSecret;
    private TwitterFeatures features = new TwitterFeatures();
    private TwitterLimits limits = new TwitterLimits();
    
    @Data
    public static class TwitterFeatures {
        private boolean enableTweetComposition = true;
        private boolean enableThreading = true;
        private boolean enableTimelineRetrieval = true;
        private boolean enableMentionMonitoring = true;
        private boolean enableFollowerAnalytics = true;
        private boolean enableMediaUpload = true;
        private boolean enableSpacesIntegration = true;
        private boolean enableDirectMessages = true;
        private boolean enableLists = true;
        private boolean enableBookmarks = true;
        private boolean enablePolls = true;
        private boolean enableScheduledTweets = true;
        private boolean enableQuoteTweets = true;
        private boolean enableRetweets = true;
        private boolean enableLikes = true;
    }
    
    @Data
    public static class TwitterLimits {
        private int maxTweetLength = 280;
        private int maxThreadLength = 25;
        private int maxImagesPerTweet = 4;
        private int maxVideoLength = 140; // seconds
        private int maxVideoSizeMB = 512;
        private int maxGifSizeMB = 15;
        private int maxPollOptions = 4;
        private int maxPollDurationHours = 7 * 24; // 7 days
        private int maxListsPerAccount = 1000;
        private int maxAccountsPerList = 5000;
    }
    
    // Tweet types
    public enum TweetType {
        STANDARD,
        REPLY,
        QUOTE,
        THREAD,
        POLL,
        SCHEDULED
    }
    
    // Media types
    public enum MediaType {
        PHOTO,
        GIF,
        VIDEO,
        ANIMATED_GIF
    }
    
    // Timeline types
    public enum TimelineType {
        HOME,
        USER,
        MENTIONS,
        SEARCH,
        LIST
    }
    
    // Expansions available in API v2
    public enum Expansion {
        ATTACHMENTS_POLL_IDS,
        ATTACHMENTS_MEDIA_KEYS,
        AUTHOR_ID,
        EDIT_HISTORY_TWEET_IDS,
        ENTITIES_MENTIONS_USERNAME,
        GEO_PLACE_ID,
        IN_REPLY_TO_USER_ID,
        REFERENCED_TWEETS_ID,
        REFERENCED_TWEETS_ID_AUTHOR_ID
    }
    
    // Tweet fields available in API v2
    public enum TweetField {
        ATTACHMENTS,
        AUTHOR_ID,
        CONTEXT_ANNOTATIONS,
        CONVERSATION_ID,
        CREATED_AT,
        EDIT_CONTROLS,
        ENTITIES,
        GEO,
        ID,
        IN_REPLY_TO_USER_ID,
        LANG,
        PUBLIC_METRICS,
        POSSIBLY_SENSITIVE,
        REFERENCED_TWEETS,
        REPLY_SETTINGS,
        SOURCE,
        TEXT,
        WITHHELD
    }
    
    // User fields
    public enum UserField {
        CREATED_AT,
        DESCRIPTION,
        ENTITIES,
        ID,
        LOCATION,
        NAME,
        PINNED_TWEET_ID,
        PROFILE_IMAGE_URL,
        PROTECTED,
        PUBLIC_METRICS,
        URL,
        USERNAME,
        VERIFIED,
        VERIFIED_TYPE,
        WITHHELD
    }
    
    // Media fields
    public enum MediaField {
        DURATION_MS,
        HEIGHT,
        MEDIA_KEY,
        PREVIEW_IMAGE_URL,
        TYPE,
        URL,
        WIDTH,
        PUBLIC_METRICS,
        ALT_TEXT,
        VARIANTS
    }
}