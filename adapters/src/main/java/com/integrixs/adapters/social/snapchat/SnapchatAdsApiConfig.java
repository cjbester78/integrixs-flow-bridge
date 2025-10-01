package com.integrixs.adapters.social.snapchat;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import com.integrixs.adapters.social.base.SocialMediaAdapterConfig;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "integrixs.adapters.snapchat.ads")
public class SnapchatAdsApiConfig extends SocialMediaAdapterConfig {

    private String adAccountId;
    private String organizationId;
    private String pixelId;
    private String mediaUploadUrl;
    private SnapchatAdsFeatures features = new SnapchatAdsFeatures();
    private SnapchatAdsLimits limits = new SnapchatAdsLimits();

    @Override
    public String getPlatformName() {
        return "snapchat";
    }

    @Override
    public String getAuthorizationUrl() {
        return "https://accounts.snapchat.com/accounts/oauth2/auth";
    }

    @Override
    public String getTokenUrl() {
        return "https://accounts.snapchat.com/accounts/oauth2/token";
    }

        public static class SnapchatAdsFeatures {
        private boolean enableCampaignManagement = true;
        private boolean enableAdManagement = true;
        private boolean enableCreativeManagement = true;
        private boolean enableAudienceManagement = true;
        private boolean enablePixelTracking = true;
        private boolean enableReporting = true;
        private boolean enableBulkOperations = true;
        private boolean enableDynamicAds = true;
        private boolean enableCatalogManagement = true;
        private boolean enableAppInstallAds = true;
        private boolean enableWebConversions = true;
        private boolean enableStoryAds = true;
        private boolean enableCollectionAds = true;
        private boolean enableARLenses = true;
        private boolean enableFilters = true;
        private boolean enableBrandedMoments = true;
        private boolean enableCommercializedLenses = true;
        private boolean enableMeasurement = true;
        private boolean enableAutoOptimization = true;
        private boolean enableCreativeLibrary = true;

        // Getters and setters
        public boolean isEnableCampaignManagement() { return enableCampaignManagement; }
        public void setEnableCampaignManagement(boolean enableCampaignManagement) { this.enableCampaignManagement = enableCampaignManagement; }
        public boolean isEnableAdManagement() { return enableAdManagement; }
        public void setEnableAdManagement(boolean enableAdManagement) { this.enableAdManagement = enableAdManagement; }
        public boolean isEnableCreativeManagement() { return enableCreativeManagement; }
        public void setEnableCreativeManagement(boolean enableCreativeManagement) { this.enableCreativeManagement = enableCreativeManagement; }
        public boolean isEnableAudienceManagement() { return enableAudienceManagement; }
        public void setEnableAudienceManagement(boolean enableAudienceManagement) { this.enableAudienceManagement = enableAudienceManagement; }
        public boolean isEnablePixelTracking() { return enablePixelTracking; }
        public void setEnablePixelTracking(boolean enablePixelTracking) { this.enablePixelTracking = enablePixelTracking; }
        public boolean isEnableReporting() { return enableReporting; }
        public void setEnableReporting(boolean enableReporting) { this.enableReporting = enableReporting; }
        public boolean isEnableBulkOperations() { return enableBulkOperations; }
        public void setEnableBulkOperations(boolean enableBulkOperations) { this.enableBulkOperations = enableBulkOperations; }
        public boolean isEnableDynamicAds() { return enableDynamicAds; }
        public void setEnableDynamicAds(boolean enableDynamicAds) { this.enableDynamicAds = enableDynamicAds; }
        public boolean isEnableCatalogManagement() { return enableCatalogManagement; }
        public void setEnableCatalogManagement(boolean enableCatalogManagement) { this.enableCatalogManagement = enableCatalogManagement; }
        public boolean isEnableAppInstallAds() { return enableAppInstallAds; }
        public void setEnableAppInstallAds(boolean enableAppInstallAds) { this.enableAppInstallAds = enableAppInstallAds; }
        public boolean isEnableWebConversions() { return enableWebConversions; }
        public void setEnableWebConversions(boolean enableWebConversions) { this.enableWebConversions = enableWebConversions; }
        public boolean isEnableStoryAds() { return enableStoryAds; }
        public void setEnableStoryAds(boolean enableStoryAds) { this.enableStoryAds = enableStoryAds; }
        public boolean isEnableCollectionAds() { return enableCollectionAds; }
        public void setEnableCollectionAds(boolean enableCollectionAds) { this.enableCollectionAds = enableCollectionAds; }
        public boolean isEnableARLenses() { return enableARLenses; }
        public void setEnableARLenses(boolean enableARLenses) { this.enableARLenses = enableARLenses; }
        public boolean isEnableFilters() { return enableFilters; }
        public void setEnableFilters(boolean enableFilters) { this.enableFilters = enableFilters; }
        public boolean isEnableBrandedMoments() { return enableBrandedMoments; }
        public void setEnableBrandedMoments(boolean enableBrandedMoments) { this.enableBrandedMoments = enableBrandedMoments; }
        public boolean isEnableCommercializedLenses() { return enableCommercializedLenses; }
        public void setEnableCommercializedLenses(boolean enableCommercializedLenses) { this.enableCommercializedLenses = enableCommercializedLenses; }
        public boolean isEnableMeasurement() { return enableMeasurement; }
        public void setEnableMeasurement(boolean enableMeasurement) { this.enableMeasurement = enableMeasurement; }
        public boolean isEnableAutoOptimization() { return enableAutoOptimization; }
        public void setEnableAutoOptimization(boolean enableAutoOptimization) { this.enableAutoOptimization = enableAutoOptimization; }
        public boolean isEnableCreativeLibrary() { return enableCreativeLibrary; }
        public void setEnableCreativeLibrary(boolean enableCreativeLibrary) { this.enableCreativeLibrary = enableCreativeLibrary; }
    }

        public static class SnapchatAdsLimits {
        private int maxCampaignsPerAccount = 1000;
        private int maxAdSquadsPerCampaign = 1000;
        private int maxAdsPerAdSquad = 100;
        private int maxCreativesPerAccount = 10000;
        private int maxAudiencesPerAccount = 5000;
        private int maxAudienceSize = 200000000;
        private int minAudienceSize = 1000;
        private int maxVideoSizeMB = 1024;
        private int maxVideoLengthSeconds = 180;
        private int maxImageSizeMB = 5;
        private int maxCatalogItems = 10000000;
        private int rateLimitPerSecond = 10;
        private int rateLimitPerMinute = 500;
        private int rateLimitPerHour = 10000;
        private int maxBulkOperations = 100;
        private int maxReportRows = 100000;

        // Getters and setters
        public int getMaxCampaignsPerAccount() { return maxCampaignsPerAccount; }
        public void setMaxCampaignsPerAccount(int maxCampaignsPerAccount) { this.maxCampaignsPerAccount = maxCampaignsPerAccount; }
        public int getMaxAdSquadsPerCampaign() { return maxAdSquadsPerCampaign; }
        public void setMaxAdSquadsPerCampaign(int maxAdSquadsPerCampaign) { this.maxAdSquadsPerCampaign = maxAdSquadsPerCampaign; }
        public int getMaxAdsPerAdSquad() { return maxAdsPerAdSquad; }
        public void setMaxAdsPerAdSquad(int maxAdsPerAdSquad) { this.maxAdsPerAdSquad = maxAdsPerAdSquad; }
        public int getMaxCreativesPerAccount() { return maxCreativesPerAccount; }
        public void setMaxCreativesPerAccount(int maxCreativesPerAccount) { this.maxCreativesPerAccount = maxCreativesPerAccount; }
        public int getMaxAudiencesPerAccount() { return maxAudiencesPerAccount; }
        public void setMaxAudiencesPerAccount(int maxAudiencesPerAccount) { this.maxAudiencesPerAccount = maxAudiencesPerAccount; }
        public int getMaxAudienceSize() { return maxAudienceSize; }
        public void setMaxAudienceSize(int maxAudienceSize) { this.maxAudienceSize = maxAudienceSize; }
        public int getMinAudienceSize() { return minAudienceSize; }
        public void setMinAudienceSize(int minAudienceSize) { this.minAudienceSize = minAudienceSize; }
        public int getMaxVideoSizeMB() { return maxVideoSizeMB; }
        public void setMaxVideoSizeMB(int maxVideoSizeMB) { this.maxVideoSizeMB = maxVideoSizeMB; }
        public int getMaxVideoLengthSeconds() { return maxVideoLengthSeconds; }
        public void setMaxVideoLengthSeconds(int maxVideoLengthSeconds) { this.maxVideoLengthSeconds = maxVideoLengthSeconds; }
        public int getMaxImageSizeMB() { return maxImageSizeMB; }
        public void setMaxImageSizeMB(int maxImageSizeMB) { this.maxImageSizeMB = maxImageSizeMB; }
        public int getMaxCatalogItems() { return maxCatalogItems; }
        public void setMaxCatalogItems(int maxCatalogItems) { this.maxCatalogItems = maxCatalogItems; }
        public int getRateLimitPerSecond() { return rateLimitPerSecond; }
        public void setRateLimitPerSecond(int rateLimitPerSecond) { this.rateLimitPerSecond = rateLimitPerSecond; }
        public int getRateLimitPerMinute() { return rateLimitPerMinute; }
        public void setRateLimitPerMinute(int rateLimitPerMinute) { this.rateLimitPerMinute = rateLimitPerMinute; }
        public int getRateLimitPerHour() { return rateLimitPerHour; }
        public void setRateLimitPerHour(int rateLimitPerHour) { this.rateLimitPerHour = rateLimitPerHour; }
        public int getMaxBulkOperations() { return maxBulkOperations; }
        public void setMaxBulkOperations(int maxBulkOperations) { this.maxBulkOperations = maxBulkOperations; }
        public int getMaxReportRows() { return maxReportRows; }
        public void setMaxReportRows(int maxReportRows) { this.maxReportRows = maxReportRows; }
    }

    // Campaign objectives
    public enum CampaignObjective {
        AWARENESS,
        TRAFFIC,
        APP_INSTALLS,
        APP_VISITS,
        ENGAGEMENT,
        VIDEO_VIEWS,
        LEAD_GENERATION,
        WEBSITE_CONVERSIONS,
        CATALOG_SALES,
        STORE_VISITS,
        BRAND_AWARENESS,
        LOCAL_AWARENESS
    }

    // Ad squad types
    public enum AdSquadType {
        SNAP_ADS,
        STORY_ADS,
        COLLECTION_ADS,
        DYNAMIC_ADS,
        COMMERCIALS,
        AR_EXPERIENCE
    }

    // Creative types
    public enum CreativeType {
        SINGLE_IMAGE,
        SINGLE_VIDEO,
        TOP_SNAP_ONLY,
        WEB_VIEW,
        APP_INSTALL,
        DEEP_LINK,
        LONGFORM_VIDEO,
        COLLECTION,
        DYNAMIC_COLLECTION,
        LENS,
        FILTER,
        BRANDED_MOMENT
    }

    // Targeting types
    public enum TargetingType {
        DEMOGRAPHICS,
        INTERESTS,
        BEHAVIORS,
        LOCATION,
        DEVICE,
        CARRIER,
        OS_VERSION,
        CONNECTION_TYPE,
        CUSTOM_AUDIENCE,
        LOOKALIKE_AUDIENCE,
        ENGAGEMENT_AUDIENCE,
        PIXEL_AUDIENCE,
        APP_ACTIVITY,
        LANGUAGE,
        ADVANCED_DEMOGRAPHICS,
        LIFESTYLE,
        SHOPPERS,
        CONTENT_KEYWORDS
    }

    // Audience types
    public enum AudienceType {
        CUSTOMER_LIST,
        PIXEL,
        ENGAGEMENT,
        LOOKALIKE,
        SAVED_AUDIENCE,
        APP_ACTIVITY,
        CATALOG,
        LOCATION_BASED
    }

    // Bid strategy
    public enum BidStrategy {
        LOWEST_COST,
        COST_CAP,
        BID_CAP,
        MIN_ROAS,
        TARGET_COST
    }

    // Optimization goal
    public enum OptimizationGoal {
        IMPRESSIONS,
        SWIPES,
        APP_INSTALLS,
        APP_OPENS,
        USES,
        PURCHASES,
        SIGN_UPS,
        PAGE_VIEWS,
        ADD_TO_CART,
        PURCHASE_WEB,
        LEAD,
        COMPLETE_TUTORIAL,
        PIXEL_PAGE_VIEW,
        PIXEL_ADD_CART,
        PIXEL_PURCHASE,
        PIXEL_SIGNUP,
        PIXEL_CUSTOM,
        STORY_OPENS,
        STORY_COMPLETES
    }

    // Delivery status
    public enum DeliveryStatus {
        ACTIVE,
        PAUSED,
        SCHEDULED,
        COMPLETED,
        DRAFT,
        PENDING_REVIEW,
        REJECTED,
        DELETED
    }

    // Media types
    public enum MediaType {
        IMAGE,
        VIDEO,
        AR_LENS,
        FILTER,
        BRANDED_MOMENT,
        GIF
    }

    // Report level
    public enum ReportLevel {
        ACCOUNT,
        CAMPAIGN,
        AD_SQUAD,
        AD,
        CREATIVE,
        PIXEL,
        AUDIENCE
    }

    // Report metrics
    public enum ReportMetric {
        IMPRESSIONS,
        SWIPE_UPS,
        SPEND,
        REACH,
        FREQUENCY,
        UNIQUES,
        TOTAL_REACH,
        CPM,
        CPC,
        CTR,
        VIDEO_VIEWS,
        VIEW_TIME,
        QUARTILE_1,
        QUARTILE_2,
        QUARTILE_3,
        QUARTILE_4,
        SCREEN_TIME,
        PLAY_TIME,
        ATTACHMENT_AVG_VIEW_TIME,
        ATTACHMENT_FREQUENCY,
        ATTACHMENT_IMPRESSIONS,
        ATTACHMENT_REACH,
        ATTACHMENT_SWIPE_UPS,
        ATTACHMENT_TOTAL_VIEW_TIME,
        ATTACHMENT_UNIQUES,
        AVG_SCREEN_TIME,
        AVG_VIEW_TIME,
        CONVERSION_PURCHASES,
        CONVERSION_SAVE,
        CONVERSION_START_CHECKOUT,
        CONVERSION_ADD_CART,
        CONVERSION_VIEW_CONTENT,
        CONVERSION_ADD_BILLING,
        CONVERSION_SIGN_UPS,
        CONVERSION_SEARCHES,
        CONVERSION_LEVEL_COMPLETES,
        CONVERSION_APP_OPENS,
        CONVERSION_PAGE_VIEWS,
        CONVERSION_SUBSCRIBE,
        CONVERSION_AD_CLICK,
        CONVERSION_AD_VIEW,
        CONVERSION_COMPLETE_TUTORIAL,
        CONVERSION_CUSTOM_EVENT_1,
        CONVERSION_CUSTOM_EVENT_2,
        CONVERSION_CUSTOM_EVENT_3,
        CONVERSION_CUSTOM_EVENT_4,
        CONVERSION_CUSTOM_EVENT_5,
        SHARES,
        SAVES,
        STORY_COMPLETES,
        STORY_OPENS
    }

    // Report dimensions
    public enum ReportDimension {
        COUNTRY,
        REGION,
        DMA,
        GENDER,
        AGE_BUCKET,
        INTEREST_CATEGORY,
        OS,
        MAKE,
        CAMPAIGN,
        AD_SQUAD,
        AD,
        CREATIVE,
        DAY,
        HOUR,
        DAY_HOUR,
        PLACEMENT,
        PRODUCT_ID,
        SKU
    }

    // Creative properties
    public enum CreativeProperty {
        HEADLINE,
        BRAND_NAME,
        SHAREABLE,
        FORCED_VIEW_ELIGIBILITY,
        PLAYBACK_TYPE,
        CALL_TO_ACTION,
        TOP_SNAP_MEDIA_ID,
        TOP_SNAP_CROP_POSITION,
        LONGFORM_VIDEO_PROPERTIES,
        COLLECTION_PROPERTIES,
        WEB_VIEW_PROPERTIES,
        DEEP_LINK_PROPERTIES,
        APP_INSTALL_PROPERTIES,
        DYNAMIC_PROPERTIES,
        AR_PROPERTIES
    }

    // Pixel event types
    public enum PixelEventType {
        PAGE_VIEW,
        VIEW_CONTENT,
        SEARCH,
        ADD_TO_CART,
        ADD_TO_WISHLIST,
        START_CHECKOUT,
        ADD_BILLING,
        PURCHASE,
        SIGN_UP,
        CUSTOM_EVENT_1,
        CUSTOM_EVENT_2,
        CUSTOM_EVENT_3,
        CUSTOM_EVENT_4,
        CUSTOM_EVENT_5,
        AD_CLICK,
        AD_VIEW,
        COMPLETE_TUTORIAL,
        INVITE,
        LOGIN,
        SHARE,
        RESERVE,
        SUBSCRIBE,
        LIST_VIEW,
        APP_OPEN,
        APP_INSTALL,
        APP_EVENT,
        LEVEL_COMPLETE,
        SPEND_CREDIT,
        ACHIEVEMENT_UNLOCKED,
        ADD_TO_CART_MOBILE,
        RATE
    }

    // Attachment types
    public enum AttachmentType {
        LENS,
        FILTER,
        BRANDED_MOMENT,
        COLLECTION,
        WEB_VIEW,
        DEEP_LINK,
        APP_INSTALL
    }

    // Review status
    public enum ReviewStatus {
        PENDING,
        APPROVED,
        REJECTED,
        PERMANENTLY_REJECTED,
        APPEAL_PENDING,
        CHANGES_REQUESTED,
        EXPIRED
    }

    // Currency
    public enum Currency {
        USD,
        EUR,
        GBP,
        CAD,
        AUD,
        NZD,
        SEK,
        NOK,
        DKK,
        CHF,
        JPY,
        KRW,
        HKD,
        SGD,
        INR,
        BRL,
        MXN,
        AED
    }

    // Error codes
    public enum SnapchatAdsErrorCode {
        INVALID_PARAMETER(400),
        UNAUTHORIZED(401),
        FORBIDDEN(403),
        NOT_FOUND(404),
        RATE_LIMITED(429),
        INTERNAL_ERROR(500),
        SERVICE_UNAVAILABLE(503),
        INVALID_CREATIVE(4001),
        BUDGET_TOO_LOW(4002),
        AUDIENCE_TOO_SMALL(4003),
        INVALID_TARGETING(4004),
        DUPLICATE_NAME(4005),
        INVALID_DATE_RANGE(4006),
        EXCEEDED_LIMIT(4007);

        private final int code;

        SnapchatAdsErrorCode(int code) {
            this.code = code;
        }

        public int getCode() { return code; }
    }

    // Webhook events
    public enum WebhookEvent {
        CAMPAIGN_CREATED,
        CAMPAIGN_UPDATED,
        CAMPAIGN_DELETED,
        AD_SQUAD_CREATED,
        AD_SQUAD_UPDATED,
        AD_SQUAD_DELETED,
        AD_CREATED,
        AD_UPDATED,
        AD_DELETED,
        CREATIVE_APPROVED,
        CREATIVE_REJECTED,
        BUDGET_EXHAUSTED,
        PIXEL_FIRED,
        AUDIENCE_UPDATED,
        REPORT_READY
    }
    // Getters and Setters
    public String getAdAccountId() {
        return adAccountId;
    }
    public void setAdAccountId(String adAccountId) {
        this.adAccountId = adAccountId;
    }
    public String getOrganizationId() {
        return organizationId;
    }
    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }
    public String getPixelId() {
        return pixelId;
    }
    public void setPixelId(String pixelId) {
        this.pixelId = pixelId;
    }
    public String getMediaUploadUrl() {
        return mediaUploadUrl;
    }
    public void setMediaUploadUrl(String mediaUploadUrl) {
        this.mediaUploadUrl = mediaUploadUrl;
    }
    public SnapchatAdsFeatures getFeatures() {
        return features;
    }
    public void setFeatures(SnapchatAdsFeatures features) {
        this.features = features;
    }
    public SnapchatAdsLimits getLimits() {
        return limits;
    }
    public void setLimits(SnapchatAdsLimits limits) {
        this.limits = limits;
    }
    public boolean isEnableCampaignManagement() {
        return features.isEnableCampaignManagement();
    }
    public void setEnableCampaignManagement(boolean enableCampaignManagement) {
        features.setEnableCampaignManagement(enableCampaignManagement);
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
    public boolean isEnableAudienceManagement() {
        return features.isEnableAudienceManagement();
    }
    public void setEnableAudienceManagement(boolean enableAudienceManagement) {
        features.setEnableAudienceManagement(enableAudienceManagement);
    }
    public boolean isEnablePixelTracking() {
        return features.isEnablePixelTracking();
    }
    public void setEnablePixelTracking(boolean enablePixelTracking) {
        features.setEnablePixelTracking(enablePixelTracking);
    }
    public boolean isEnableReporting() {
        return features.isEnableReporting();
    }
    public void setEnableReporting(boolean enableReporting) {
        features.setEnableReporting(enableReporting);
    }
    public boolean isEnableBulkOperations() {
        return features.isEnableBulkOperations();
    }
    public void setEnableBulkOperations(boolean enableBulkOperations) {
        features.setEnableBulkOperations(enableBulkOperations);
    }
    public boolean isEnableDynamicAds() {
        return features.isEnableDynamicAds();
    }
    public void setEnableDynamicAds(boolean enableDynamicAds) {
        features.setEnableDynamicAds(enableDynamicAds);
    }
    public boolean isEnableCatalogManagement() {
        return features.isEnableCatalogManagement();
    }
    public void setEnableCatalogManagement(boolean enableCatalogManagement) {
        features.setEnableCatalogManagement(enableCatalogManagement);
    }
    public boolean isEnableAppInstallAds() {
        return features.isEnableAppInstallAds();
    }
    public void setEnableAppInstallAds(boolean enableAppInstallAds) {
        features.setEnableAppInstallAds(enableAppInstallAds);
    }
    public boolean isEnableWebConversions() {
        return features.isEnableWebConversions();
    }
    public void setEnableWebConversions(boolean enableWebConversions) {
        features.setEnableWebConversions(enableWebConversions);
    }
    public boolean isEnableStoryAds() {
        return features.isEnableStoryAds();
    }
    public void setEnableStoryAds(boolean enableStoryAds) {
        features.setEnableStoryAds(enableStoryAds);
    }
    public boolean isEnableCollectionAds() {
        return features.isEnableCollectionAds();
    }
    public void setEnableCollectionAds(boolean enableCollectionAds) {
        features.setEnableCollectionAds(enableCollectionAds);
    }
    public boolean isEnableARLenses() {
        return features.isEnableARLenses();
    }
    public void setEnableARLenses(boolean enableARLenses) {
        features.setEnableARLenses(enableARLenses);
    }
    public boolean isEnableFilters() {
        return features.isEnableFilters();
    }
    public void setEnableFilters(boolean enableFilters) {
        features.setEnableFilters(enableFilters);
    }
    public boolean isEnableBrandedMoments() {
        return features.isEnableBrandedMoments();
    }
    public void setEnableBrandedMoments(boolean enableBrandedMoments) {
        features.setEnableBrandedMoments(enableBrandedMoments);
    }
    public boolean isEnableCommercializedLenses() {
        return features.isEnableCommercializedLenses();
    }
    public void setEnableCommercializedLenses(boolean enableCommercializedLenses) {
        features.setEnableCommercializedLenses(enableCommercializedLenses);
    }
    public boolean isEnableMeasurement() {
        return features.isEnableMeasurement();
    }
    public void setEnableMeasurement(boolean enableMeasurement) {
        features.setEnableMeasurement(enableMeasurement);
    }
    public boolean isEnableAutoOptimization() {
        return features.isEnableAutoOptimization();
    }
    public void setEnableAutoOptimization(boolean enableAutoOptimization) {
        features.setEnableAutoOptimization(enableAutoOptimization);
    }
    public boolean isEnableCreativeLibrary() {
        return features.isEnableCreativeLibrary();
    }
    public void setEnableCreativeLibrary(boolean enableCreativeLibrary) {
        features.setEnableCreativeLibrary(enableCreativeLibrary);
    }
    public int getMaxCampaignsPerAccount() {
        return limits.getMaxCampaignsPerAccount();
    }
    public void setMaxCampaignsPerAccount(int maxCampaignsPerAccount) {
        limits.setMaxCampaignsPerAccount(maxCampaignsPerAccount);
    }
    public int getMaxAdSquadsPerCampaign() {
        return limits.getMaxAdSquadsPerCampaign();
    }
    public void setMaxAdSquadsPerCampaign(int maxAdSquadsPerCampaign) {
        limits.setMaxAdSquadsPerCampaign(maxAdSquadsPerCampaign);
    }
    public int getMaxAdsPerAdSquad() {
        return limits.getMaxAdsPerAdSquad();
    }
    public void setMaxAdsPerAdSquad(int maxAdsPerAdSquad) {
        limits.setMaxAdsPerAdSquad(maxAdsPerAdSquad);
    }
    public int getMaxCreativesPerAccount() {
        return limits.getMaxCreativesPerAccount();
    }
    public void setMaxCreativesPerAccount(int maxCreativesPerAccount) {
        limits.setMaxCreativesPerAccount(maxCreativesPerAccount);
    }
    public int getMaxAudiencesPerAccount() {
        return limits.getMaxAudiencesPerAccount();
    }
    public void setMaxAudiencesPerAccount(int maxAudiencesPerAccount) {
        limits.setMaxAudiencesPerAccount(maxAudiencesPerAccount);
    }
    public int getMaxAudienceSize() {
        return limits.getMaxAudienceSize();
    }
    public void setMaxAudienceSize(int maxAudienceSize) {
        limits.setMaxAudienceSize(maxAudienceSize);
    }
    public int getMinAudienceSize() {
        return limits.getMinAudienceSize();
    }
    public void setMinAudienceSize(int minAudienceSize) {
        limits.setMinAudienceSize(minAudienceSize);
    }
    public int getMaxVideoSizeMB() {
        return limits.getMaxVideoSizeMB();
    }
    public void setMaxVideoSizeMB(int maxVideoSizeMB) {
        limits.setMaxVideoSizeMB(maxVideoSizeMB);
    }
    public int getMaxVideoLengthSeconds() {
        return limits.getMaxVideoLengthSeconds();
    }
    public void setMaxVideoLengthSeconds(int maxVideoLengthSeconds) {
        limits.setMaxVideoLengthSeconds(maxVideoLengthSeconds);
    }
    public int getMaxImageSizeMB() {
        return limits.getMaxImageSizeMB();
    }
    public void setMaxImageSizeMB(int maxImageSizeMB) {
        limits.setMaxImageSizeMB(maxImageSizeMB);
    }
    public int getMaxCatalogItems() {
        return limits.getMaxCatalogItems();
    }
    public void setMaxCatalogItems(int maxCatalogItems) {
        limits.setMaxCatalogItems(maxCatalogItems);
    }
    public int getRateLimitPerSecond() {
        return limits.getRateLimitPerSecond();
    }
    public void setRateLimitPerSecond(int rateLimitPerSecond) {
        limits.setRateLimitPerSecond(rateLimitPerSecond);
    }
    // Override parent class methods to return Integer instead of int
    @Override
    public Integer getRateLimitPerMinute() {
        return Integer.valueOf(limits.getRateLimitPerMinute());
    }
    @Override
    public void setRateLimitPerMinute(Integer rateLimitPerMinute) {
        if (rateLimitPerMinute != null) {
            limits.setRateLimitPerMinute(rateLimitPerMinute.intValue());
        }
    }
    @Override
    public Integer getRateLimitPerHour() {
        return Integer.valueOf(limits.getRateLimitPerHour());
    }
    @Override
    public void setRateLimitPerHour(Integer rateLimitPerHour) {
        if (rateLimitPerHour != null) {
            limits.setRateLimitPerHour(rateLimitPerHour.intValue());
        }
    }
    public int getMaxBulkOperations() {
        return limits.getMaxBulkOperations();
    }
    public void setMaxBulkOperations(int maxBulkOperations) {
        limits.setMaxBulkOperations(maxBulkOperations);
    }
    public int getMaxReportRows() {
        return limits.getMaxReportRows();
    }
    public void setMaxReportRows(int maxReportRows) {
        limits.setMaxReportRows(maxReportRows);
    }
}
