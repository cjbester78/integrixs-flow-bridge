package com.integrixs.adapters.social.twitter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import com.integrixs.adapters.social.base.SocialMediaAdapterConfig;
@Component
@ConfigurationProperties(prefix = "integrixs.adapters.twitter.api")
public class TwitterApiV2Config extends SocialMediaAdapterConfig {

    private String apiKey;
    private String apiKeySecret;
    private String bearerToken;
    private String clientId;
    private String clientSecret;
    private TwitterFeatures features = new TwitterFeatures();
    private TwitterLimits limits = new TwitterLimits();

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
    public int getMaxVideoSizeMB() {
        return maxVideoSizeMB;
    }
    public void setMaxVideoSizeMB(int maxVideoSizeMB) {
        this.maxVideoSizeMB = maxVideoSizeMB;
    }
    public int getMaxGifSizeMB() {
        return maxGifSizeMB;
    }
    public void setMaxGifSizeMB(int maxGifSizeMB) {
        this.maxGifSizeMB = maxGifSizeMB;
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
