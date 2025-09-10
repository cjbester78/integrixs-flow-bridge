package com.integrixs.adapters.social.tiktok;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import com.integrixs.adapters.social.base.SocialMediaAdapterConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "integrixs.adapters.tiktok.business")
@EqualsAndHashCode(callSuper = true)
public class TikTokBusinessApiConfig extends SocialMediaAdapterConfig {
    
    private String appId;
    private String appSecret;
    private String advertiserId;
    private String businessId;
    private TikTokBusinessFeatures features = new TikTokBusinessFeatures();
    private TikTokBusinessLimits limits = new TikTokBusinessLimits();
    
    @Data
    public static class TikTokBusinessFeatures {
        private boolean enableCampaignManagement = true;
        private boolean enableAdGroupManagement = true;
        private boolean enableAdManagement = true;
        private boolean enableCreativeManagement = true;
        private boolean enableAudienceTargeting = true;
        private boolean enableCustomAudiences = true;
        private boolean enablePixelTracking = true;
        private boolean enableConversionTracking = true;
        private boolean enableReporting = true;
        private boolean enableBudgetManagement = true;
        private boolean enableBidStrategy = true;
        private boolean enableCreativeOptimization = true;
        private boolean enableVideoUpload = true;
        private boolean enableImageUpload = true;
        private boolean enableCatalogManagement = true;
        private boolean enableDynamicAds = true;
        private boolean enableSparkAds = true;
        private boolean enableBrandedContent = true;
        private boolean enableAnalyticsExport = true;
        private boolean enableAutomatedRules = true;
    }
    
    @Data
    public static class TikTokBusinessLimits {
        private int maxCampaignsPerAccount = 500;
        private int maxAdGroupsPerCampaign = 1000;
        private int maxAdsPerAdGroup = 20;
        private int maxCreativesPerAd = 10;
        private int maxCustomAudiences = 500;
        private int maxPixelsPerAccount = 20;
        private int maxVideoSizeMB = 500;
        private int maxVideoDurationSeconds = 60;
        private int maxImageSizeMB = 30;
        private int minBudgetUSD = 20;
        private int maxBulkOperations = 100;
        private int reportingDelayHours = 3;
    }
    
    // Campaign objectives
    public enum CampaignObjective {
        REACH,
        TRAFFIC,
        APP_INSTALL,
        VIDEO_VIEWS,
        LEAD_GENERATION,
        COMMUNITY_INTERACTION,
        CONVERSIONS,
        CATALOG_SALES,
        BRAND_AWARENESS
    }
    
    // Ad formats
    public enum AdFormat {
        SINGLE_VIDEO,
        SINGLE_IMAGE,
        CAROUSEL,
        COLLECTION,
        DYNAMIC_SHOWCASE,
        SPARK_AD,
        PLAYABLE_AD,
        BRANDED_HASHTAG_CHALLENGE,
        BRANDED_EFFECT,
        TOP_VIEW,
        IN_FEED_VIDEO,
        BRANDED_MISSION
    }
    
    // Placement options
    public enum Placement {
        TIKTOK_AUTOMATIC,
        TIKTOK_FEED,
        TIKTOK_PANGLE,
        TIKTOK_TOP_BUZZ,
        TIKTOK_GLOBAL_APP_BUNDLE,
        AUDIENCE_NETWORK,
        BABE,
        BUZZBREAK,
        CAPCUT,
        DAILYHUNT,
        HELO,
        TIKTOK_LITE,
        TIKTOK_NOW
    }
    
    // Targeting options
    public enum TargetingDimension {
        LOCATION,
        LANGUAGE,
        GENDER,
        AGE,
        INTEREST,
        BEHAVIOR,
        DEVICE,
        CONNECTION_TYPE,
        OPERATING_SYSTEM,
        CARRIER,
        DEVICE_PRICE,
        SPENDING_POWER,
        VIDEO_INTERACTION,
        CREATOR_INTERACTION,
        HASHTAG_INTERACTION,
        CUSTOM_AUDIENCE,
        LOOKALIKE_AUDIENCE
    }
    
    // Age ranges
    public enum AgeRange {
        AGE_13_17("13-17"),
        AGE_18_24("18-24"),
        AGE_25_34("25-34"),
        AGE_35_44("35-44"),
        AGE_45_54("45-54"),
        AGE_55_PLUS("55+");
        
        private final String range;
        
        AgeRange(String range) {
            this.range = range;
        }
        
        public String getRange() { return range; }
    }
    
    // Gender options
    public enum Gender {
        MALE,
        FEMALE,
        ALL
    }
    
    // Budget types
    public enum BudgetType {
        DAILY,
        LIFETIME,
        NO_LIMIT
    }
    
    // Bid strategies
    public enum BidStrategy {
        LOWEST_COST,
        COST_CAP,
        BID_CAP,
        MINIMUM_ROAS,
        MAXIMUM_CONVERSIONS,
        TARGET_COST
    }
    
    // Billing events
    public enum BillingEvent {
        IMPRESSION,
        CLICK,
        VIEW_6S,
        VIEW_2S,
        VIEW_100_PERCENT,
        APP_INSTALL,
        CONVERSION,
        VIDEO_VIEW
    }
    
    // Creative types
    public enum CreativeType {
        VIDEO,
        IMAGE,
        CAROUSEL_VIDEO,
        CAROUSEL_IMAGE,
        DYNAMIC,
        PLAYABLE,
        SPARK_AD_POST,
        BRANDED_CONTENT
    }
    
    // Video resolutions
    public enum VideoResolution {
        RESOLUTION_540_960("540x960"),
        RESOLUTION_640_640("640x640"),
        RESOLUTION_720_1280("720x1280"),
        RESOLUTION_1080_1920("1080x1920");
        
        private final String resolution;
        
        VideoResolution(String resolution) {
            this.resolution = resolution;
        }
        
        public String getResolution() { return resolution; }
    }
    
    // Conversion events
    public enum ConversionEvent {
        REGISTRATION,
        ADD_TO_CART,
        PLACE_ORDER,
        PURCHASE,
        VIEW_CONTENT,
        ADD_PAYMENT_INFO,
        ADD_TO_WISHLIST,
        COMPLETE_TUTORIAL,
        GENERATE_LEAD,
        LOGIN,
        SEARCH,
        SPEND_CREDITS,
        UNLOCK_ACHIEVEMENT,
        RATE,
        START_TRIAL,
        SUBSCRIBE,
        CUSTOM_EVENT
    }
    
    // Report types
    public enum ReportType {
        BASIC,
        AUDIENCE,
        PLAYABLE_MATERIAL,
        CATALOG,
        LIVE_ROOM,
        REACH_FREQUENCY,
        VIDEO,
        ATTRIBUTION,
        CONVERSION,
        CUSTOM
    }
    
    // Report dimensions
    public enum ReportDimension {
        ADVERTISER_ID,
        CAMPAIGN_ID,
        ADGROUP_ID,
        AD_ID,
        CREATIVE_ID,
        DAY,
        HOUR,
        COUNTRY,
        PROVINCE,
        PLACEMENT,
        AGE,
        GENDER,
        PLATFORM,
        INTEREST_CATEGORY,
        BEHAVIOR_CATEGORY,
        DEVICE_MODEL,
        CONNECTION_TYPE
    }
    
    // Report metrics
    public enum ReportMetric {
        // Basic metrics
        IMPRESSIONS,
        CLICKS,
        CTR,
        CPM,
        CPC,
        SPEND,
        REACH,
        FREQUENCY,
        
        // Video metrics
        VIDEO_VIEWS,
        VIDEO_VIEWS_2S,
        VIDEO_VIEWS_6S,
        VIDEO_VIEWS_100_PERCENT,
        VIDEO_WATCHED_2S,
        VIDEO_WATCHED_6S,
        AVERAGE_VIDEO_PLAY_TIME,
        VIDEO_PLAY_ACTIONS,
        
        // Engagement metrics
        LIKES,
        COMMENTS,
        SHARES,
        FOLLOWS,
        PROFILE_VISITS,
        
        // Conversion metrics
        CONVERSIONS,
        CONVERSION_RATE,
        COST_PER_CONVERSION,
        CONVERSION_VALUE,
        ROAS,
        
        // App metrics
        APP_INSTALL,
        APP_EVENT,
        REGISTRATION,
        PURCHASE,
        RETENTION_DAY_1,
        RETENTION_DAY_7,
        
        // Interactive metrics
        INTERACTIVE_ADD_ON_IMPRESSIONS,
        INTERACTIVE_ADD_ON_CLICKS,
        INTERACTIVE_ADD_ON_CTR
    }
    
    // Creative optimization modes
    public enum CreativeOptimizationMode {
        DISABLE,
        ENABLE,
        DYNAMIC_CREATIVE,
        AUTOMATED_CREATIVE_OPTIMIZATION
    }
    
    // Pixel events
    public enum PixelEvent {
        PAGE_VIEW,
        VIEW_CONTENT,
        ADD_TO_CART,
        INITIATE_CHECKOUT,
        ADD_PAYMENT_INFO,
        COMPLETE_PAYMENT,
        SEARCH,
        CONTACT,
        DOWNLOAD,
        SUBMIT_FORM,
        COMPLETE_REGISTRATION,
        SUBSCRIBE
    }
    
    // Audience types
    public enum AudienceType {
        CUSTOM,
        LOOKALIKE,
        INTEREST,
        BEHAVIOR,
        ENGAGEMENT,
        APP_ACTIVITY,
        WEBSITE_TRAFFIC,
        CUSTOMER_FILE,
        LEAD_GENERATION,
        VIDEO_ENGAGEMENT
    }
    
    // Campaign status
    public enum CampaignStatus {
        ENABLE,
        DISABLE,
        DELETE
    }
    
    // Ad review status
    public enum AdReviewStatus {
        PENDING,
        APPROVED,
        REJECTED,
        CHANGES_REQUESTED,
        DELETED
    }
}