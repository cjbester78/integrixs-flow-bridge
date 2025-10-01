package com.integrixs.adapters.social.twitter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import com.integrixs.adapters.social.base.SocialMediaAdapterConfig;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "integrixs.adapters.twitter.ads")
public class TwitterAdsApiConfig extends SocialMediaAdapterConfig {

    private String adsAccountId;
    private String apiKey;
    private String apiKeySecret;
    private String accessToken;
    private String accessTokenSecret;
    private TwitterAdsFeatures features = new TwitterAdsFeatures();
    private TwitterAdsLimits limits = new TwitterAdsLimits();

    @Override
    public String getPlatformName() {
        return "twitter";
    }

    @Override
    public String getAuthorizationUrl() {
        return "https://api.twitter.com/oauth/authorize";
    }

    @Override
    public String getTokenUrl() {
        return "https://api.twitter.com/oauth/access_token";
    }

        public static class TwitterAdsFeatures {
        private boolean enableCampaignManagement = true;
        private boolean enableAdGroupManagement = true;
        private boolean enableCreativeManagement = true;
        private boolean enableAudienceTargeting = true;
        private boolean enableBudgetManagement = true;
        private boolean enableBidding = true;
        private boolean enableAnalytics = true;
        private boolean enableConversionTracking = true;
        private boolean enableReporting = true;
        private boolean enableCustomAudiences = true;
        private boolean enableTailoredAudiences = true;
        private boolean enablePromotedTweets = true;
        private boolean enablePromotedAccounts = true;
        private boolean enablePromotedTrends = true;
        private boolean enableVideoAds = true;
        private boolean enableCarouselAds = true;
        private boolean enableMomentAds = true;
        private boolean enableWebsiteCards = true;
        private boolean enableAppCards = true;
        private boolean enableLeadGenCards = true;

        // Getters and setters for Features
        public boolean isEnableCampaignManagement() { return enableCampaignManagement; }
        public void setEnableCampaignManagement(boolean enableCampaignManagement) { this.enableCampaignManagement = enableCampaignManagement; }
        public boolean isEnableAdGroupManagement() { return enableAdGroupManagement; }
        public void setEnableAdGroupManagement(boolean enableAdGroupManagement) { this.enableAdGroupManagement = enableAdGroupManagement; }
        public boolean isEnableCreativeManagement() { return enableCreativeManagement; }
        public void setEnableCreativeManagement(boolean enableCreativeManagement) { this.enableCreativeManagement = enableCreativeManagement; }
        public boolean isEnableAudienceTargeting() { return enableAudienceTargeting; }
        public void setEnableAudienceTargeting(boolean enableAudienceTargeting) { this.enableAudienceTargeting = enableAudienceTargeting; }
        public boolean isEnableBudgetManagement() { return enableBudgetManagement; }
        public void setEnableBudgetManagement(boolean enableBudgetManagement) { this.enableBudgetManagement = enableBudgetManagement; }
        public boolean isEnableBidding() { return enableBidding; }
        public void setEnableBidding(boolean enableBidding) { this.enableBidding = enableBidding; }
        public boolean isEnableAnalytics() { return enableAnalytics; }
        public void setEnableAnalytics(boolean enableAnalytics) { this.enableAnalytics = enableAnalytics; }
        public boolean isEnableConversionTracking() { return enableConversionTracking; }
        public void setEnableConversionTracking(boolean enableConversionTracking) { this.enableConversionTracking = enableConversionTracking; }
        public boolean isEnableReporting() { return enableReporting; }
        public void setEnableReporting(boolean enableReporting) { this.enableReporting = enableReporting; }
        public boolean isEnableCustomAudiences() { return enableCustomAudiences; }
        public void setEnableCustomAudiences(boolean enableCustomAudiences) { this.enableCustomAudiences = enableCustomAudiences; }
        public boolean isEnableTailoredAudiences() { return enableTailoredAudiences; }
        public void setEnableTailoredAudiences(boolean enableTailoredAudiences) { this.enableTailoredAudiences = enableTailoredAudiences; }
        public boolean isEnablePromotedTweets() { return enablePromotedTweets; }
        public void setEnablePromotedTweets(boolean enablePromotedTweets) { this.enablePromotedTweets = enablePromotedTweets; }
        public boolean isEnablePromotedAccounts() { return enablePromotedAccounts; }
        public void setEnablePromotedAccounts(boolean enablePromotedAccounts) { this.enablePromotedAccounts = enablePromotedAccounts; }
        public boolean isEnablePromotedTrends() { return enablePromotedTrends; }
        public void setEnablePromotedTrends(boolean enablePromotedTrends) { this.enablePromotedTrends = enablePromotedTrends; }
        public boolean isEnableVideoAds() { return enableVideoAds; }
        public void setEnableVideoAds(boolean enableVideoAds) { this.enableVideoAds = enableVideoAds; }
        public boolean isEnableCarouselAds() { return enableCarouselAds; }
        public void setEnableCarouselAds(boolean enableCarouselAds) { this.enableCarouselAds = enableCarouselAds; }
        public boolean isEnableMomentAds() { return enableMomentAds; }
        public void setEnableMomentAds(boolean enableMomentAds) { this.enableMomentAds = enableMomentAds; }
        public boolean isEnableWebsiteCards() { return enableWebsiteCards; }
        public void setEnableWebsiteCards(boolean enableWebsiteCards) { this.enableWebsiteCards = enableWebsiteCards; }
        public boolean isEnableAppCards() { return enableAppCards; }
        public void setEnableAppCards(boolean enableAppCards) { this.enableAppCards = enableAppCards; }
        public boolean isEnableLeadGenCards() { return enableLeadGenCards; }
        public void setEnableLeadGenCards(boolean enableLeadGenCards) { this.enableLeadGenCards = enableLeadGenCards; }
    }

        public static class TwitterAdsLimits {
        private int maxCampaignsPerAccount = 200;
        private int maxAdGroupsPerCampaign = 100;
        private int maxAdsPerAdGroup = 50;
        private int maxKeywordsPerAdGroup = 1000;
        private int maxTargetingCriteriaPerAdGroup = 50;
        private double minDailyBudget = 1.00; // USD
        private double maxDailyBudget = 1000000.00; // USD
        private int maxCustomAudienceSize = 500000;
        private int minCustomAudienceSize = 100;
        private int maxConversionEventsPerDay = 100000;

        // Getters and setters for Limits
        public int getMaxCampaignsPerAccount() { return maxCampaignsPerAccount; }
        public void setMaxCampaignsPerAccount(int maxCampaignsPerAccount) { this.maxCampaignsPerAccount = maxCampaignsPerAccount; }
        public int getMaxAdGroupsPerCampaign() { return maxAdGroupsPerCampaign; }
        public void setMaxAdGroupsPerCampaign(int maxAdGroupsPerCampaign) { this.maxAdGroupsPerCampaign = maxAdGroupsPerCampaign; }
        public int getMaxAdsPerAdGroup() { return maxAdsPerAdGroup; }
        public void setMaxAdsPerAdGroup(int maxAdsPerAdGroup) { this.maxAdsPerAdGroup = maxAdsPerAdGroup; }
        public int getMaxKeywordsPerAdGroup() { return maxKeywordsPerAdGroup; }
        public void setMaxKeywordsPerAdGroup(int maxKeywordsPerAdGroup) { this.maxKeywordsPerAdGroup = maxKeywordsPerAdGroup; }
        public int getMaxTargetingCriteriaPerAdGroup() { return maxTargetingCriteriaPerAdGroup; }
        public void setMaxTargetingCriteriaPerAdGroup(int maxTargetingCriteriaPerAdGroup) { this.maxTargetingCriteriaPerAdGroup = maxTargetingCriteriaPerAdGroup; }
        public double getMinDailyBudget() { return minDailyBudget; }
        public void setMinDailyBudget(double minDailyBudget) { this.minDailyBudget = minDailyBudget; }
        public double getMaxDailyBudget() { return maxDailyBudget; }
        public void setMaxDailyBudget(double maxDailyBudget) { this.maxDailyBudget = maxDailyBudget; }
        public int getMaxCustomAudienceSize() { return maxCustomAudienceSize; }
        public void setMaxCustomAudienceSize(int maxCustomAudienceSize) { this.maxCustomAudienceSize = maxCustomAudienceSize; }
        public int getMinCustomAudienceSize() { return minCustomAudienceSize; }
        public void setMinCustomAudienceSize(int minCustomAudienceSize) { this.minCustomAudienceSize = minCustomAudienceSize; }
        public int getMaxConversionEventsPerDay() { return maxConversionEventsPerDay; }
        public void setMaxConversionEventsPerDay(int maxConversionEventsPerDay) { this.maxConversionEventsPerDay = maxConversionEventsPerDay; }
    }

    // Campaign objectives
    public enum CampaignObjective {
        AWARENESS,
        TWEET_ENGAGEMENTS,
        VIDEO_VIEWS,
        PRE_ROLL_VIEWS,
        APP_INSTALLS,
        WEBSITE_TRAFFIC,
        FOLLOWERS,
        APP_ENGAGEMENTS,
        WEBSITE_CONVERSIONS,
        IN_STREAM_VIDEO_VIEWS
    }

    // Funding instruments
    public enum FundingInstrumentType {
        CREDIT_CARD,
        AGENCY_CREDIT_LINE,
        INSERTION_ORDER,
        CREDIT_LINE
    }

    // Bidding types
    public enum BidType {
        AUTO,
        MAX,
        TARGET
    }

    // Placement types
    public enum PlacementType {
        ALL_ON_TWITTER,
        PUBLISHER_NETWORK,
        TWITTER_SEARCH,
        TWITTER_TIMELINE,
        TWITTER_PROFILE
    }

    // Creative types
    public enum CreativeType {
        TWEET,
        VIDEO,
        IMAGE,
        CAROUSEL,
        MOMENT,
        WEBSITE_CARD,
        IMAGE_APP_DOWNLOAD_CARD,
        VIDEO_APP_DOWNLOAD_CARD,
        IMAGE_CONVERSATION_CARD,
        VIDEO_CONVERSATION_CARD,
        DIRECT_MESSAGE_CARD,
        POLL_IMAGE_CARD,
        POLL_VIDEO_CARD,
        CONVERSATIONAL_VIDEO_CARD
    }

    // Audience types
    public enum AudienceType {
        CUSTOM_AUDIENCE,
        TAILORED_AUDIENCE,
        LOOKALIKE_AUDIENCE,
        KEYWORD,
        FOLLOWER,
        DEVICE,
        BEHAVIOR,
        LANGUAGE,
        GENDER,
        AGE,
        LOCATION,
        PLATFORM,
        CARRIER,
        NEW_DEVICE,
        EVENT,
        INSTALLED_APP_CATEGORY,
        INTEREST,
        TV_SHOW,
        TV_GENRE,
        TV_CHANNEL
    }

    // Metric types for reporting
    public enum MetricType {
        IMPRESSIONS,
        ENGAGEMENTS,
        ENGAGEMENT_RATE,
        RETWEETS,
        REPLIES,
        LIKES,
        FOLLOWS,
        CLICKS,
        LINK_CLICKS,
        APP_CLICKS,
        APP_INSTALLS,
        QUALIFIED_IMPRESSIONS,
        COST_PER_ENGAGEMENT,
        COST_PER_FOLLOWER,
        VIDEO_VIEWS,
        VIDEO_VIEWS_25,
        VIDEO_VIEWS_50,
        VIDEO_VIEWS_75,
        VIDEO_VIEWS_100,
        VIDEO_AVERAGE_DURATION,
        SPEND,
        BILLED_CHARGE_LOCAL_MICRO,
        CONVERSION_PURCHASES,
        CONVERSION_SIGN_UPS,
        CONVERSION_SITE_VISITS,
        CONVERSION_DOWNLOADS,
        CONVERSION_CUSTOM
    }

    // Report granularity
    public enum Granularity {
        HOUR,
        DAY,
        TOTAL
    }
    // Getters and Setters
    public String getAdsAccountId() {
        return adsAccountId;
    }
    public void setAdsAccountId(String adsAccountId) {
        this.adsAccountId = adsAccountId;
    }
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
    public String getAccessToken() {
        return accessToken;
    }
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    public String getAccessTokenSecret() {
        return accessTokenSecret;
    }
    public void setAccessTokenSecret(String accessTokenSecret) {
        this.accessTokenSecret = accessTokenSecret;
    }
    public TwitterAdsFeatures getFeatures() {
        return features;
    }
    public void setFeatures(TwitterAdsFeatures features) {
        this.features = features;
    }
    public TwitterAdsLimits getLimits() {
        return limits;
    }
    public void setLimits(TwitterAdsLimits limits) {
        this.limits = limits;
    }
    public boolean isEnableCampaignManagement() {
        return features.isEnableCampaignManagement();
    }
    public void setEnableCampaignManagement(boolean enableCampaignManagement) {
        features.setEnableCampaignManagement(enableCampaignManagement);
    }
    public boolean isEnableAdGroupManagement() {
        return features.isEnableAdGroupManagement();
    }
    public void setEnableAdGroupManagement(boolean enableAdGroupManagement) {
        features.setEnableAdGroupManagement(enableAdGroupManagement);
    }
    public boolean isEnableCreativeManagement() {
        return features.isEnableCreativeManagement();
    }
    public void setEnableCreativeManagement(boolean enableCreativeManagement) {
        features.setEnableCreativeManagement(enableCreativeManagement);
    }
    public boolean isEnableAudienceTargeting() {
        return features.isEnableAudienceTargeting();
    }
    public void setEnableAudienceTargeting(boolean enableAudienceTargeting) {
        features.setEnableAudienceTargeting(enableAudienceTargeting);
    }
    public boolean isEnableBudgetManagement() {
        return features.isEnableBudgetManagement();
    }
    public void setEnableBudgetManagement(boolean enableBudgetManagement) {
        features.setEnableBudgetManagement(enableBudgetManagement);
    }
    public boolean isEnableBidding() {
        return features.isEnableBidding();
    }
    public void setEnableBidding(boolean enableBidding) {
        features.setEnableBidding(enableBidding);
    }
    public boolean isEnableAnalytics() {
        return features.isEnableAnalytics();
    }
    public void setEnableAnalytics(boolean enableAnalytics) {
        features.setEnableAnalytics(enableAnalytics);
    }
    public boolean isEnableConversionTracking() {
        return features.isEnableConversionTracking();
    }
    public void setEnableConversionTracking(boolean enableConversionTracking) {
        features.setEnableConversionTracking(enableConversionTracking);
    }
    public boolean isEnableReporting() {
        return features.isEnableReporting();
    }
    public void setEnableReporting(boolean enableReporting) {
        features.setEnableReporting(enableReporting);
    }
    public boolean isEnableCustomAudiences() {
        return features.isEnableCustomAudiences();
    }
    public void setEnableCustomAudiences(boolean enableCustomAudiences) {
        features.setEnableCustomAudiences(enableCustomAudiences);
    }
    public boolean isEnableTailoredAudiences() {
        return features.isEnableTailoredAudiences();
    }
    public void setEnableTailoredAudiences(boolean enableTailoredAudiences) {
        features.setEnableTailoredAudiences(enableTailoredAudiences);
    }
    public boolean isEnablePromotedTweets() {
        return features.isEnablePromotedTweets();
    }
    public void setEnablePromotedTweets(boolean enablePromotedTweets) {
        features.setEnablePromotedTweets(enablePromotedTweets);
    }
    public boolean isEnablePromotedAccounts() {
        return features.isEnablePromotedAccounts();
    }
    public void setEnablePromotedAccounts(boolean enablePromotedAccounts) {
        features.setEnablePromotedAccounts(enablePromotedAccounts);
    }
    public boolean isEnablePromotedTrends() {
        return features.isEnablePromotedTrends();
    }
    public void setEnablePromotedTrends(boolean enablePromotedTrends) {
        features.setEnablePromotedTrends(enablePromotedTrends);
    }
    public boolean isEnableVideoAds() {
        return features.isEnableVideoAds();
    }
    public void setEnableVideoAds(boolean enableVideoAds) {
        features.setEnableVideoAds(enableVideoAds);
    }
    public boolean isEnableCarouselAds() {
        return features.isEnableCarouselAds();
    }
    public void setEnableCarouselAds(boolean enableCarouselAds) {
        features.setEnableCarouselAds(enableCarouselAds);
    }
    public boolean isEnableMomentAds() {
        return features.isEnableMomentAds();
    }
    public void setEnableMomentAds(boolean enableMomentAds) {
        features.setEnableMomentAds(enableMomentAds);
    }
    public boolean isEnableWebsiteCards() {
        return features.isEnableWebsiteCards();
    }
    public void setEnableWebsiteCards(boolean enableWebsiteCards) {
        features.setEnableWebsiteCards(enableWebsiteCards);
    }
    public boolean isEnableAppCards() {
        return features.isEnableAppCards();
    }
    public void setEnableAppCards(boolean enableAppCards) {
        features.setEnableAppCards(enableAppCards);
    }
    public boolean isEnableLeadGenCards() {
        return features.isEnableLeadGenCards();
    }
    public void setEnableLeadGenCards(boolean enableLeadGenCards) {
        features.setEnableLeadGenCards(enableLeadGenCards);
    }
    public int getMaxCampaignsPerAccount() {
        return limits.getMaxCampaignsPerAccount();
    }
    public void setMaxCampaignsPerAccount(int maxCampaignsPerAccount) {
        limits.setMaxCampaignsPerAccount(maxCampaignsPerAccount);
    }
    public int getMaxAdGroupsPerCampaign() {
        return limits.getMaxAdGroupsPerCampaign();
    }
    public void setMaxAdGroupsPerCampaign(int maxAdGroupsPerCampaign) {
        limits.setMaxAdGroupsPerCampaign(maxAdGroupsPerCampaign);
    }
    public int getMaxAdsPerAdGroup() {
        return limits.getMaxAdsPerAdGroup();
    }
    public void setMaxAdsPerAdGroup(int maxAdsPerAdGroup) {
        limits.setMaxAdsPerAdGroup(maxAdsPerAdGroup);
    }
    public int getMaxKeywordsPerAdGroup() {
        return limits.getMaxKeywordsPerAdGroup();
    }
    public void setMaxKeywordsPerAdGroup(int maxKeywordsPerAdGroup) {
        limits.setMaxKeywordsPerAdGroup(maxKeywordsPerAdGroup);
    }
    public int getMaxTargetingCriteriaPerAdGroup() {
        return limits.getMaxTargetingCriteriaPerAdGroup();
    }
    public void setMaxTargetingCriteriaPerAdGroup(int maxTargetingCriteriaPerAdGroup) {
        limits.setMaxTargetingCriteriaPerAdGroup(maxTargetingCriteriaPerAdGroup);
    }
    public double getMinDailyBudget() {
        return limits.getMinDailyBudget();
    }
    public void setMinDailyBudget(double minDailyBudget) {
        limits.setMinDailyBudget(minDailyBudget);
    }
    public double getMaxDailyBudget() {
        return limits.getMaxDailyBudget();
    }
    public void setMaxDailyBudget(double maxDailyBudget) {
        limits.setMaxDailyBudget(maxDailyBudget);
    }
    public int getMaxCustomAudienceSize() {
        return limits.getMaxCustomAudienceSize();
    }
    public void setMaxCustomAudienceSize(int maxCustomAudienceSize) {
        limits.setMaxCustomAudienceSize(maxCustomAudienceSize);
    }
    public int getMinCustomAudienceSize() {
        return limits.getMinCustomAudienceSize();
    }
    public void setMinCustomAudienceSize(int minCustomAudienceSize) {
        limits.setMinCustomAudienceSize(minCustomAudienceSize);
    }
    public int getMaxConversionEventsPerDay() {
        return limits.getMaxConversionEventsPerDay();
    }
    public void setMaxConversionEventsPerDay(int maxConversionEventsPerDay) {
        limits.setMaxConversionEventsPerDay(maxConversionEventsPerDay);
    }
}
