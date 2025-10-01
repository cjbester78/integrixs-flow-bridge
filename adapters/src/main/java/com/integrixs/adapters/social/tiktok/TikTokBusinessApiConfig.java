package com.integrixs.adapters.social.tiktok;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import com.integrixs.adapters.social.base.SocialMediaAdapterConfig;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "integrixs.adapters.tiktok.business")
public class TikTokBusinessApiConfig extends SocialMediaAdapterConfig {

    private String appId;
    private String appSecret;
    private String advertiserId;
    private String businessId;
    private TikTokBusinessFeatures features = new TikTokBusinessFeatures();
    private TikTokBusinessLimits limits = new TikTokBusinessLimits();

    // Default values configuration
    private Integer defaultPageSize = 100;
    private Integer defaultReportPageSize = 1000;
    private String placementTypeAutomatic = "PLACEMENT_TYPE_AUTOMATIC";
    private String placementTypeNormal = "PLACEMENT_TYPE_NORMAL";
    private String scheduleTypeStartEnd = "SCHEDULE_START_END";
    private String defaultReportType = "BASIC";
    private String defaultDataLevel = "AUCTION_CAMPAIGN";
    private String defaultAudienceAction = "APPEND";
    private String defaultCodeType = "PUBLIC_AUTH";
    private String defaultTargetingScene = "GENERAL_INTEREST";
    private String defaultBehaviorScene = "ACTION_CATEGORY";
    private Long defaultPollingInterval = 60000L;

    @Override
    public String getPlatformName() {
        return "tiktok";
    }

    @Override
    public String getAuthorizationUrl() {
        return "https://business-api.tiktok.com/open_api/v1.3/oauth2/authorize";
    }

    @Override
    public String getTokenUrl() {
        return "https://business-api.tiktok.com/open_api/v1.3/oauth2/access_token";
    }

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

        // Getters and setters for Features
        public boolean isEnableCampaignManagement() {
            return enableCampaignManagement;
        }
        public void setEnableCampaignManagement(boolean enableCampaignManagement) {
            this.enableCampaignManagement = enableCampaignManagement;
        }
        public boolean isEnableAdGroupManagement() {
            return enableAdGroupManagement;
        }
        public void setEnableAdGroupManagement(boolean enableAdGroupManagement) {
            this.enableAdGroupManagement = enableAdGroupManagement;
        }
        public boolean isEnableAdManagement() {
            return enableAdManagement;
        }
        public void setEnableAdManagement(boolean enableAdManagement) {
            this.enableAdManagement = enableAdManagement;
        }
        public boolean isEnableCreativeManagement() {
            return enableCreativeManagement;
        }
        public void setEnableCreativeManagement(boolean enableCreativeManagement) {
            this.enableCreativeManagement = enableCreativeManagement;
        }
        public boolean isEnableAudienceTargeting() {
            return enableAudienceTargeting;
        }
        public void setEnableAudienceTargeting(boolean enableAudienceTargeting) {
            this.enableAudienceTargeting = enableAudienceTargeting;
        }
        public boolean isEnableCustomAudiences() {
            return enableCustomAudiences;
        }
        public void setEnableCustomAudiences(boolean enableCustomAudiences) {
            this.enableCustomAudiences = enableCustomAudiences;
        }
        public boolean isEnablePixelTracking() {
            return enablePixelTracking;
        }
        public void setEnablePixelTracking(boolean enablePixelTracking) {
            this.enablePixelTracking = enablePixelTracking;
        }
        public boolean isEnableConversionTracking() {
            return enableConversionTracking;
        }
        public void setEnableConversionTracking(boolean enableConversionTracking) {
            this.enableConversionTracking = enableConversionTracking;
        }
        public boolean isEnableReporting() {
            return enableReporting;
        }
        public void setEnableReporting(boolean enableReporting) {
            this.enableReporting = enableReporting;
        }
        public boolean isEnableBudgetManagement() {
            return enableBudgetManagement;
        }
        public void setEnableBudgetManagement(boolean enableBudgetManagement) {
            this.enableBudgetManagement = enableBudgetManagement;
        }
        public boolean isEnableBidStrategy() {
            return enableBidStrategy;
        }
        public void setEnableBidStrategy(boolean enableBidStrategy) {
            this.enableBidStrategy = enableBidStrategy;
        }
        public boolean isEnableCreativeOptimization() {
            return enableCreativeOptimization;
        }
        public void setEnableCreativeOptimization(boolean enableCreativeOptimization) {
            this.enableCreativeOptimization = enableCreativeOptimization;
        }
        public boolean isEnableVideoUpload() {
            return enableVideoUpload;
        }
        public void setEnableVideoUpload(boolean enableVideoUpload) {
            this.enableVideoUpload = enableVideoUpload;
        }
        public boolean isEnableImageUpload() {
            return enableImageUpload;
        }
        public void setEnableImageUpload(boolean enableImageUpload) {
            this.enableImageUpload = enableImageUpload;
        }
        public boolean isEnableCatalogManagement() {
            return enableCatalogManagement;
        }
        public void setEnableCatalogManagement(boolean enableCatalogManagement) {
            this.enableCatalogManagement = enableCatalogManagement;
        }
        public boolean isEnableDynamicAds() {
            return enableDynamicAds;
        }
        public void setEnableDynamicAds(boolean enableDynamicAds) {
            this.enableDynamicAds = enableDynamicAds;
        }
        public boolean isEnableSparkAds() {
            return enableSparkAds;
        }
        public void setEnableSparkAds(boolean enableSparkAds) {
            this.enableSparkAds = enableSparkAds;
        }
        public boolean isEnableBrandedContent() {
            return enableBrandedContent;
        }
        public void setEnableBrandedContent(boolean enableBrandedContent) {
            this.enableBrandedContent = enableBrandedContent;
        }
        public boolean isEnableAnalyticsExport() {
            return enableAnalyticsExport;
        }
        public void setEnableAnalyticsExport(boolean enableAnalyticsExport) {
            this.enableAnalyticsExport = enableAnalyticsExport;
        }
        public boolean isEnableAutomatedRules() {
            return enableAutomatedRules;
        }
        public void setEnableAutomatedRules(boolean enableAutomatedRules) {
            this.enableAutomatedRules = enableAutomatedRules;
        }
    }

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

        // Getters and setters for Limits
        public int getMaxCampaignsPerAccount() {
            return maxCampaignsPerAccount;
        }
        public void setMaxCampaignsPerAccount(int maxCampaignsPerAccount) {
            this.maxCampaignsPerAccount = maxCampaignsPerAccount;
        }
        public int getMaxAdGroupsPerCampaign() {
            return maxAdGroupsPerCampaign;
        }
        public void setMaxAdGroupsPerCampaign(int maxAdGroupsPerCampaign) {
            this.maxAdGroupsPerCampaign = maxAdGroupsPerCampaign;
        }
        public int getMaxAdsPerAdGroup() {
            return maxAdsPerAdGroup;
        }
        public void setMaxAdsPerAdGroup(int maxAdsPerAdGroup) {
            this.maxAdsPerAdGroup = maxAdsPerAdGroup;
        }
        public int getMaxCreativesPerAd() {
            return maxCreativesPerAd;
        }
        public void setMaxCreativesPerAd(int maxCreativesPerAd) {
            this.maxCreativesPerAd = maxCreativesPerAd;
        }
        public int getMaxCustomAudiences() {
            return maxCustomAudiences;
        }
        public void setMaxCustomAudiences(int maxCustomAudiences) {
            this.maxCustomAudiences = maxCustomAudiences;
        }
        public int getMaxPixelsPerAccount() {
            return maxPixelsPerAccount;
        }
        public void setMaxPixelsPerAccount(int maxPixelsPerAccount) {
            this.maxPixelsPerAccount = maxPixelsPerAccount;
        }
        public int getMaxVideoSizeMB() {
            return maxVideoSizeMB;
        }
        public void setMaxVideoSizeMB(int maxVideoSizeMB) {
            this.maxVideoSizeMB = maxVideoSizeMB;
        }
        public int getMaxVideoDurationSeconds() {
            return maxVideoDurationSeconds;
        }
        public void setMaxVideoDurationSeconds(int maxVideoDurationSeconds) {
            this.maxVideoDurationSeconds = maxVideoDurationSeconds;
        }
        public int getMaxImageSizeMB() {
            return maxImageSizeMB;
        }
        public void setMaxImageSizeMB(int maxImageSizeMB) {
            this.maxImageSizeMB = maxImageSizeMB;
        }
        public int getMinBudgetUSD() {
            return minBudgetUSD;
        }
        public void setMinBudgetUSD(int minBudgetUSD) {
            this.minBudgetUSD = minBudgetUSD;
        }
        public int getMaxBulkOperations() {
            return maxBulkOperations;
        }
        public void setMaxBulkOperations(int maxBulkOperations) {
            this.maxBulkOperations = maxBulkOperations;
        }
        public int getReportingDelayHours() {
            return reportingDelayHours;
        }
        public void setReportingDelayHours(int reportingDelayHours) {
            this.reportingDelayHours = reportingDelayHours;
        }
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
        AGE_55_PLUS("55 + ");

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
    // Getters and Setters
    public String getAppId() {
        return appId;
    }
    public void setAppId(String appId) {
        this.appId = appId;
    }
    public String getAppSecret() {
        return appSecret;
    }
    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }
    public String getAdvertiserId() {
        return advertiserId;
    }
    public void setAdvertiserId(String advertiserId) {
        this.advertiserId = advertiserId;
    }
    public String getBusinessId() {
        return businessId;
    }
    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }
    public TikTokBusinessFeatures getFeatures() {
        return features;
    }
    public void setFeatures(TikTokBusinessFeatures features) {
        this.features = features;
    }
    public TikTokBusinessLimits getLimits() {
        return limits;
    }
    public void setLimits(TikTokBusinessLimits limits) {
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
    public boolean isEnableAdManagement() {
        return features.isEnableAdManagement();
    }
    public void setEnableAdManagement(boolean enableAdManagement) {
        features.setEnableAdManagement(enableAdManagement);
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
    public boolean isEnableCustomAudiences() {
        return features.isEnableCustomAudiences();
    }
    public void setEnableCustomAudiences(boolean enableCustomAudiences) {
        features.setEnableCustomAudiences(enableCustomAudiences);
    }
    public boolean isEnablePixelTracking() {
        return features.isEnablePixelTracking();
    }
    public void setEnablePixelTracking(boolean enablePixelTracking) {
        features.setEnablePixelTracking(enablePixelTracking);
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
    public boolean isEnableBudgetManagement() {
        return features.isEnableBudgetManagement();
    }
    public void setEnableBudgetManagement(boolean enableBudgetManagement) {
        features.setEnableBudgetManagement(enableBudgetManagement);
    }
    public boolean isEnableBidStrategy() {
        return features.isEnableBidStrategy();
    }
    public void setEnableBidStrategy(boolean enableBidStrategy) {
        features.setEnableBidStrategy(enableBidStrategy);
    }
    public boolean isEnableCreativeOptimization() {
        return features.isEnableCreativeOptimization();
    }
    public void setEnableCreativeOptimization(boolean enableCreativeOptimization) {
        features.setEnableCreativeOptimization(enableCreativeOptimization);
    }
    public boolean isEnableVideoUpload() {
        return features.isEnableVideoUpload();
    }
    public void setEnableVideoUpload(boolean enableVideoUpload) {
        features.setEnableVideoUpload(enableVideoUpload);
    }
    public boolean isEnableImageUpload() {
        return features.isEnableImageUpload();
    }
    public void setEnableImageUpload(boolean enableImageUpload) {
        features.setEnableImageUpload(enableImageUpload);
    }
    public boolean isEnableCatalogManagement() {
        return features.isEnableCatalogManagement();
    }
    public void setEnableCatalogManagement(boolean enableCatalogManagement) {
        features.setEnableCatalogManagement(enableCatalogManagement);
    }
    public boolean isEnableDynamicAds() {
        return features.isEnableDynamicAds();
    }
    public void setEnableDynamicAds(boolean enableDynamicAds) {
        features.setEnableDynamicAds(enableDynamicAds);
    }
    public boolean isEnableSparkAds() {
        return features.isEnableSparkAds();
    }
    public void setEnableSparkAds(boolean enableSparkAds) {
        features.setEnableSparkAds(enableSparkAds);
    }
    public boolean isEnableBrandedContent() {
        return features.isEnableBrandedContent();
    }
    public void setEnableBrandedContent(boolean enableBrandedContent) {
        features.setEnableBrandedContent(enableBrandedContent);
    }
    public boolean isEnableAnalyticsExport() {
        return features.isEnableAnalyticsExport();
    }
    public void setEnableAnalyticsExport(boolean enableAnalyticsExport) {
        features.setEnableAnalyticsExport(enableAnalyticsExport);
    }
    public boolean isEnableAutomatedRules() {
        return features.isEnableAutomatedRules();
    }
    public void setEnableAutomatedRules(boolean enableAutomatedRules) {
        features.setEnableAutomatedRules(enableAutomatedRules);
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
    public int getMaxCreativesPerAd() {
        return limits.getMaxCreativesPerAd();
    }
    public void setMaxCreativesPerAd(int maxCreativesPerAd) {
        limits.setMaxCreativesPerAd(maxCreativesPerAd);
    }
    public int getMaxCustomAudiences() {
        return limits.getMaxCustomAudiences();
    }
    public void setMaxCustomAudiences(int maxCustomAudiences) {
        limits.setMaxCustomAudiences(maxCustomAudiences);
    }
    public int getMaxPixelsPerAccount() {
        return limits.getMaxPixelsPerAccount();
    }
    public void setMaxPixelsPerAccount(int maxPixelsPerAccount) {
        limits.setMaxPixelsPerAccount(maxPixelsPerAccount);
    }
    public int getMaxVideoSizeMB() {
        return limits.getMaxVideoSizeMB();
    }
    public void setMaxVideoSizeMB(int maxVideoSizeMB) {
        limits.setMaxVideoSizeMB(maxVideoSizeMB);
    }
    public int getMaxVideoDurationSeconds() {
        return limits.getMaxVideoDurationSeconds();
    }
    public void setMaxVideoDurationSeconds(int maxVideoDurationSeconds) {
        limits.setMaxVideoDurationSeconds(maxVideoDurationSeconds);
    }
    public int getMaxImageSizeMB() {
        return limits.getMaxImageSizeMB();
    }
    public void setMaxImageSizeMB(int maxImageSizeMB) {
        limits.setMaxImageSizeMB(maxImageSizeMB);
    }
    public int getMinBudgetUSD() {
        return limits.getMinBudgetUSD();
    }
    public void setMinBudgetUSD(int minBudgetUSD) {
        limits.setMinBudgetUSD(minBudgetUSD);
    }
    public int getMaxBulkOperations() {
        return limits.getMaxBulkOperations();
    }
    public void setMaxBulkOperations(int maxBulkOperations) {
        limits.setMaxBulkOperations(maxBulkOperations);
    }
    public int getReportingDelayHours() {
        return limits.getReportingDelayHours();
    }
    public void setReportingDelayHours(int reportingDelayHours) {
        limits.setReportingDelayHours(reportingDelayHours);
    }

    // Getters and setters for default values
    public Integer getDefaultPageSize() {
        return defaultPageSize;
    }
    public void setDefaultPageSize(Integer defaultPageSize) {
        this.defaultPageSize = defaultPageSize;
    }
    public Integer getDefaultReportPageSize() {
        return defaultReportPageSize;
    }
    public void setDefaultReportPageSize(Integer defaultReportPageSize) {
        this.defaultReportPageSize = defaultReportPageSize;
    }
    public String getPlacementTypeAutomatic() {
        return placementTypeAutomatic;
    }
    public void setPlacementTypeAutomatic(String placementTypeAutomatic) {
        this.placementTypeAutomatic = placementTypeAutomatic;
    }
    public String getPlacementTypeNormal() {
        return placementTypeNormal;
    }
    public void setPlacementTypeNormal(String placementTypeNormal) {
        this.placementTypeNormal = placementTypeNormal;
    }
    public String getScheduleTypeStartEnd() {
        return scheduleTypeStartEnd;
    }
    public void setScheduleTypeStartEnd(String scheduleTypeStartEnd) {
        this.scheduleTypeStartEnd = scheduleTypeStartEnd;
    }
    public String getDefaultReportType() {
        return defaultReportType;
    }
    public void setDefaultReportType(String defaultReportType) {
        this.defaultReportType = defaultReportType;
    }
    public String getDefaultDataLevel() {
        return defaultDataLevel;
    }
    public void setDefaultDataLevel(String defaultDataLevel) {
        this.defaultDataLevel = defaultDataLevel;
    }
    public String getDefaultAudienceAction() {
        return defaultAudienceAction;
    }
    public void setDefaultAudienceAction(String defaultAudienceAction) {
        this.defaultAudienceAction = defaultAudienceAction;
    }
    public String getDefaultCodeType() {
        return defaultCodeType;
    }
    public void setDefaultCodeType(String defaultCodeType) {
        this.defaultCodeType = defaultCodeType;
    }
    public String getDefaultTargetingScene() {
        return defaultTargetingScene;
    }
    public void setDefaultTargetingScene(String defaultTargetingScene) {
        this.defaultTargetingScene = defaultTargetingScene;
    }
    public String getDefaultBehaviorScene() {
        return defaultBehaviorScene;
    }
    public void setDefaultBehaviorScene(String defaultBehaviorScene) {
        this.defaultBehaviorScene = defaultBehaviorScene;
    }
    public Long getDefaultPollingInterval() {
        return defaultPollingInterval;
    }
    public void setDefaultPollingInterval(Long defaultPollingInterval) {
        this.defaultPollingInterval = defaultPollingInterval;
    }
}
