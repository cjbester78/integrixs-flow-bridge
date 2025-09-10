package com.integrixs.adapters.social.twitter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import com.integrixs.adapters.social.base.SocialMediaAdapterConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "integrixs.adapters.twitter.ads")
@EqualsAndHashCode(callSuper = true)
public class TwitterAdsApiConfig extends SocialMediaAdapterConfig {
    
    private String adsAccountId;
    private String apiKey;
    private String apiKeySecret;
    private String accessToken;
    private String accessTokenSecret;
    private TwitterAdsFeatures features = new TwitterAdsFeatures();
    private TwitterAdsLimits limits = new TwitterAdsLimits();
    
    @Data
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
    }
    
    @Data
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
}