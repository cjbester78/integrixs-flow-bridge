package com.integrixs.adapters.social.twitter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import com.integrixs.adapters.social.base.SocialMediaAdapterConfig;
@Component
@ConfigurationProperties(prefix = "adapters.twitter")
public class TwitterApiV2Config extends SocialMediaAdapterConfig {

    private String apiKey;
    private String apiKeySecret;
    private String bearerToken;
    private String accessTokenSecret;
    private Integer defaultMaxResults;
    private String crcPrefix;
    // Note: clientId and clientSecret are already in parent class
    private TwitterFeatures features = new TwitterFeatures();
    private TwitterLimits limits = new TwitterLimits();

    public static class TwitterFeatures {
        private boolean enableTweetComposition;
        private boolean enableThreading;
        private boolean enableTimelineRetrieval;
        private boolean enableMentionMonitoring;
        private boolean enableFollowerAnalytics;
        private boolean enableMediaUpload;
        private boolean enableSpacesIntegration;
        private boolean enableDirectMessages;
        private boolean enableLists;
        private boolean enableBookmarks;
        private boolean enablePolls;
        private boolean enableScheduledTweets;
        private boolean enableQuoteTweets;
        private boolean enableRetweets;
        private boolean enableLikes;

        // Getters and setters for TwitterFeatures
        public boolean isEnableTweetComposition() {
            return enableTweetComposition;
        }
        public void setEnableTweetComposition(boolean enableTweetComposition) {
            this.enableTweetComposition = enableTweetComposition;
        }
        public boolean isEnableThreading() {
            return enableThreading;
        }
        public void setEnableThreading(boolean enableThreading) {
            this.enableThreading = enableThreading;
        }
        public boolean isEnableTimelineRetrieval() {
            return enableTimelineRetrieval;
        }
        public void setEnableTimelineRetrieval(boolean enableTimelineRetrieval) {
            this.enableTimelineRetrieval = enableTimelineRetrieval;
        }
        public boolean isEnableMentionMonitoring() {
            return enableMentionMonitoring;
        }
        public void setEnableMentionMonitoring(boolean enableMentionMonitoring) {
            this.enableMentionMonitoring = enableMentionMonitoring;
        }
        public boolean isEnableFollowerAnalytics() {
            return enableFollowerAnalytics;
        }
        public void setEnableFollowerAnalytics(boolean enableFollowerAnalytics) {
            this.enableFollowerAnalytics = enableFollowerAnalytics;
        }
        public boolean isEnableMediaUpload() {
            return enableMediaUpload;
        }
        public void setEnableMediaUpload(boolean enableMediaUpload) {
            this.enableMediaUpload = enableMediaUpload;
        }
        public boolean isEnableSpacesIntegration() {
            return enableSpacesIntegration;
        }
        public void setEnableSpacesIntegration(boolean enableSpacesIntegration) {
            this.enableSpacesIntegration = enableSpacesIntegration;
        }
        public boolean isEnableDirectMessages() {
            return enableDirectMessages;
        }
        public void setEnableDirectMessages(boolean enableDirectMessages) {
            this.enableDirectMessages = enableDirectMessages;
        }
        public boolean isEnableLists() {
            return enableLists;
        }
        public void setEnableLists(boolean enableLists) {
            this.enableLists = enableLists;
        }
        public boolean isEnableBookmarks() {
            return enableBookmarks;
        }
        public void setEnableBookmarks(boolean enableBookmarks) {
            this.enableBookmarks = enableBookmarks;
        }
        public boolean isEnablePolls() {
            return enablePolls;
        }
        public void setEnablePolls(boolean enablePolls) {
            this.enablePolls = enablePolls;
        }
        public boolean isEnableScheduledTweets() {
            return enableScheduledTweets;
        }
        public void setEnableScheduledTweets(boolean enableScheduledTweets) {
            this.enableScheduledTweets = enableScheduledTweets;
        }
        public boolean isEnableQuoteTweets() {
            return enableQuoteTweets;
        }
        public void setEnableQuoteTweets(boolean enableQuoteTweets) {
            this.enableQuoteTweets = enableQuoteTweets;
        }
        public boolean isEnableRetweets() {
            return enableRetweets;
        }
        public void setEnableRetweets(boolean enableRetweets) {
            this.enableRetweets = enableRetweets;
        }
        public boolean isEnableLikes() {
            return enableLikes;
        }
        public void setEnableLikes(boolean enableLikes) {
            this.enableLikes = enableLikes;
        }
    }

    public static class TwitterLimits {
        private int maxTweetLength;
        private int maxThreadLength;
        private int maxImagesPerTweet;
        private int maxVideoLength;
        private int maxVideoSizeMb;
        private int maxGifSizeMb;
        private int maxPollOptions;
        private int maxPollDurationHours;
        private int maxListsPerAccount;
        private int maxAccountsPerList;

        // Getters and setters for TwitterLimits
        public int getMaxTweetLength() {
            return maxTweetLength;
        }
        public void setMaxTweetLength(int maxTweetLength) {
            this.maxTweetLength = maxTweetLength;
        }
        public int getMaxThreadLength() {
            return maxThreadLength;
        }
        public void setMaxThreadLength(int maxThreadLength) {
            this.maxThreadLength = maxThreadLength;
        }
        public int getMaxImagesPerTweet() {
            return maxImagesPerTweet;
        }
        public void setMaxImagesPerTweet(int maxImagesPerTweet) {
            this.maxImagesPerTweet = maxImagesPerTweet;
        }
        public int getMaxVideoLength() {
            return maxVideoLength;
        }
        public void setMaxVideoLength(int maxVideoLength) {
            this.maxVideoLength = maxVideoLength;
        }
        public int getMaxVideoSizeMb() {
            return maxVideoSizeMb;
        }
        public void setMaxVideoSizeMb(int maxVideoSizeMb) {
            this.maxVideoSizeMb = maxVideoSizeMb;
        }
        public int getMaxGifSizeMb() {
            return maxGifSizeMb;
        }
        public void setMaxGifSizeMb(int maxGifSizeMb) {
            this.maxGifSizeMb = maxGifSizeMb;
        }
        public int getMaxPollOptions() {
            return maxPollOptions;
        }
        public void setMaxPollOptions(int maxPollOptions) {
            this.maxPollOptions = maxPollOptions;
        }
        public int getMaxPollDurationHours() {
            return maxPollDurationHours;
        }
        public void setMaxPollDurationHours(int maxPollDurationHours) {
            this.maxPollDurationHours = maxPollDurationHours;
        }
        public int getMaxListsPerAccount() {
            return maxListsPerAccount;
        }
        public void setMaxListsPerAccount(int maxListsPerAccount) {
            this.maxListsPerAccount = maxListsPerAccount;
        }
        public int getMaxAccountsPerList() {
            return maxAccountsPerList;
        }
        public void setMaxAccountsPerList(int maxAccountsPerList) {
            this.maxAccountsPerList = maxAccountsPerList;
        }
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
    // Implementation of abstract methods from SocialMediaAdapterConfig
    @Override
    public String getAuthorizationUrl() {
        return "https://twitter.com/i/oauth2/authorize";
    }

    @Override
    public String getTokenUrl() {
        return "https://api.twitter.com/2/oauth2/token";
    }

    @Override
    public String getPlatformName() {
        return "twitter";
    }

    // Getters and Setters
    public String getApiKey() {
        return apiKey;
    }
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
    public String getApiKeySecret() {
        return apiKeySecret;
    }
    public void setApiKeySecret(String apiKeySecret) {
        this.apiKeySecret = apiKeySecret;
    }
    public String getBearerToken() {
        return bearerToken;
    }
    public void setBearerToken(String bearerToken) {
        this.bearerToken = bearerToken;
    }
    public TwitterFeatures getFeatures() {
        return features;
    }
    public void setFeatures(TwitterFeatures features) {
        this.features = features;
    }
    public TwitterLimits getLimits() {
        return limits;
    }
    public void setLimits(TwitterLimits limits) {
        this.limits = limits;
    }
    public String getAccessTokenSecret() {
        return accessTokenSecret;
    }
    public void setAccessTokenSecret(String accessTokenSecret) {
        this.accessTokenSecret = accessTokenSecret;
    }
    public Integer getDefaultMaxResults() {
        return defaultMaxResults;
    }
    public void setDefaultMaxResults(Integer defaultMaxResults) {
        this.defaultMaxResults = defaultMaxResults;
    }
    public String getCrcPrefix() {
        return crcPrefix;
    }
    public void setCrcPrefix(String crcPrefix) {
        this.crcPrefix = crcPrefix;
    }
}
