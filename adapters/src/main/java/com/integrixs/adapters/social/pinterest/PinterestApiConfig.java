package com.integrixs.adapters.social.pinterest;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import com.integrixs.adapters.social.base.SocialMediaAdapterConfig;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "integrixs.adapters.pinterest")
public class PinterestApiConfig extends SocialMediaAdapterConfig {

    @Override
    public String getPlatformName() {
        return "Pinterest";
    }

    private String appId;
    private String appSecret;
    private String advertiserId;
    private PinterestFeatures features = new PinterestFeatures();
    private PinterestLimits limits = new PinterestLimits();

    public static class PinterestFeatures {
        private boolean enablePinManagement = true;
        private boolean enableBoardManagement = true;
        private boolean enableAnalytics = true;
        private boolean enableShopping = true;
        private boolean enableAds = true;
        private boolean enableCatalogs = true;
        private boolean enableAudiences = true;
        private boolean enableConversionTracking = true;
        private boolean enableBulkOperations = true;
        private boolean enableVideoContent = true;
        private boolean enableStoryPins = true;
        private boolean enableIdeaPins = true;
        private boolean enableTryOn = true;
        private boolean enableCollections = true;
        private boolean enableMerchant = true;
        private boolean enableCreatorTools = true;

        // Getters and setters for PinterestFeatures
        public boolean isEnablePinManagement() {
            return enablePinManagement;
        }
        public void setEnablePinManagement(boolean enablePinManagement) {
            this.enablePinManagement = enablePinManagement;
        }
        public boolean isEnableBoardManagement() {
            return enableBoardManagement;
        }
        public void setEnableBoardManagement(boolean enableBoardManagement) {
            this.enableBoardManagement = enableBoardManagement;
        }
        public boolean isEnableAnalytics() {
            return enableAnalytics;
        }
        public void setEnableAnalytics(boolean enableAnalytics) {
            this.enableAnalytics = enableAnalytics;
        }
        public boolean isEnableShopping() {
            return enableShopping;
        }
        public void setEnableShopping(boolean enableShopping) {
            this.enableShopping = enableShopping;
        }
        public boolean isEnableAds() {
            return enableAds;
        }
        public void setEnableAds(boolean enableAds) {
            this.enableAds = enableAds;
        }
        public boolean isEnableCatalogs() {
            return enableCatalogs;
        }
        public void setEnableCatalogs(boolean enableCatalogs) {
            this.enableCatalogs = enableCatalogs;
        }
        public boolean isEnableAudiences() {
            return enableAudiences;
        }
        public void setEnableAudiences(boolean enableAudiences) {
            this.enableAudiences = enableAudiences;
        }
        public boolean isEnableConversionTracking() {
            return enableConversionTracking;
        }
        public void setEnableConversionTracking(boolean enableConversionTracking) {
            this.enableConversionTracking = enableConversionTracking;
        }
        public boolean isEnableBulkOperations() {
            return enableBulkOperations;
        }
        public void setEnableBulkOperations(boolean enableBulkOperations) {
            this.enableBulkOperations = enableBulkOperations;
        }
        public boolean isEnableVideoContent() {
            return enableVideoContent;
        }
        public void setEnableVideoContent(boolean enableVideoContent) {
            this.enableVideoContent = enableVideoContent;
        }
        public boolean isEnableStoryPins() {
            return enableStoryPins;
        }
        public void setEnableStoryPins(boolean enableStoryPins) {
            this.enableStoryPins = enableStoryPins;
        }
        public boolean isEnableIdeaPins() {
            return enableIdeaPins;
        }
        public void setEnableIdeaPins(boolean enableIdeaPins) {
            this.enableIdeaPins = enableIdeaPins;
        }
        public boolean isEnableTryOn() {
            return enableTryOn;
        }
        public void setEnableTryOn(boolean enableTryOn) {
            this.enableTryOn = enableTryOn;
        }
        public boolean isEnableCollections() {
            return enableCollections;
        }
        public void setEnableCollections(boolean enableCollections) {
            this.enableCollections = enableCollections;
        }
        public boolean isEnableMerchant() {
            return enableMerchant;
        }
        public void setEnableMerchant(boolean enableMerchant) {
            this.enableMerchant = enableMerchant;
        }
        public boolean isEnableCreatorTools() {
            return enableCreatorTools;
        }
        public void setEnableCreatorTools(boolean enableCreatorTools) {
            this.enableCreatorTools = enableCreatorTools;
        }
    }

    public static class PinterestLimits {
        private int maxPinsPerBoard = 200000;
        private int maxBoardsPerUser = 2000;
        private int maxSectionsPerBoard = 500;
        private int maxPinDescriptionLength = 500;
        private int maxBoardDescriptionLength = 500;
        private int maxImageSizeMB = 32;
        private int maxVideoSizeMB = 2048;
        private int maxVideoLengthSeconds = 900;
        private int maxBulkPinsPerRequest = 100;
        private int maxAudienceSize = 10000000;
        private int rateLimitPerHour = 1000;
        private int rateLimitPerMinute = 300;
        private int maxProductsPerCatalog = 20000000;

        // Getters and setters for PinterestLimits
        public int getMaxPinsPerBoard() {
            return maxPinsPerBoard;
        }
        public void setMaxPinsPerBoard(int maxPinsPerBoard) {
            this.maxPinsPerBoard = maxPinsPerBoard;
        }
        public int getMaxBoardsPerUser() {
            return maxBoardsPerUser;
        }
        public void setMaxBoardsPerUser(int maxBoardsPerUser) {
            this.maxBoardsPerUser = maxBoardsPerUser;
        }
        public int getMaxSectionsPerBoard() {
            return maxSectionsPerBoard;
        }
        public void setMaxSectionsPerBoard(int maxSectionsPerBoard) {
            this.maxSectionsPerBoard = maxSectionsPerBoard;
        }
        public int getMaxPinDescriptionLength() {
            return maxPinDescriptionLength;
        }
        public void setMaxPinDescriptionLength(int maxPinDescriptionLength) {
            this.maxPinDescriptionLength = maxPinDescriptionLength;
        }
        public int getMaxBoardDescriptionLength() {
            return maxBoardDescriptionLength;
        }
        public void setMaxBoardDescriptionLength(int maxBoardDescriptionLength) {
            this.maxBoardDescriptionLength = maxBoardDescriptionLength;
        }
        public int getMaxImageSizeMB() {
            return maxImageSizeMB;
        }
        public void setMaxImageSizeMB(int maxImageSizeMB) {
            this.maxImageSizeMB = maxImageSizeMB;
        }
        public int getMaxVideoSizeMB() {
            return maxVideoSizeMB;
        }
        public void setMaxVideoSizeMB(int maxVideoSizeMB) {
            this.maxVideoSizeMB = maxVideoSizeMB;
        }
        public int getMaxVideoLengthSeconds() {
            return maxVideoLengthSeconds;
        }
        public void setMaxVideoLengthSeconds(int maxVideoLengthSeconds) {
            this.maxVideoLengthSeconds = maxVideoLengthSeconds;
        }
        public int getMaxBulkPinsPerRequest() {
            return maxBulkPinsPerRequest;
        }
        public void setMaxBulkPinsPerRequest(int maxBulkPinsPerRequest) {
            this.maxBulkPinsPerRequest = maxBulkPinsPerRequest;
        }
        public int getMaxAudienceSize() {
            return maxAudienceSize;
        }
        public void setMaxAudienceSize(int maxAudienceSize) {
            this.maxAudienceSize = maxAudienceSize;
        }
        public int getRateLimitPerHour() {
            return rateLimitPerHour;
        }
        public void setRateLimitPerHour(int rateLimitPerHour) {
            this.rateLimitPerHour = rateLimitPerHour;
        }
        public int getRateLimitPerMinute() {
            return rateLimitPerMinute;
        }
        public void setRateLimitPerMinute(int rateLimitPerMinute) {
            this.rateLimitPerMinute = rateLimitPerMinute;
        }
        public int getMaxProductsPerCatalog() {
            return maxProductsPerCatalog;
        }
        public void setMaxProductsPerCatalog(int maxProductsPerCatalog) {
            this.maxProductsPerCatalog = maxProductsPerCatalog;
        }
    }

    // Pin types
    public enum PinType {
        STANDARD,
        VIDEO,
        STORY,
        IDEA,
        COLLECTION,
        CAROUSEL,
        APP,
        PRODUCT,
        RECIPE,
        ARTICLE
    }

    // Board types
    public enum BoardPrivacy {
        PUBLIC,
        SECRET,
        PROTECTED
    }

    // Pin status
    public enum PinStatus {
        ACTIVE,
        INACTIVE,
        PENDING,
        REJECTED,
        DELETED
    }

    // Media types
    public enum MediaType {
        IMAGE,
        VIDEO,
        MULTIPLE_IMAGE,
        MULTIPLE_VIDEO,
        MIXED_MEDIA
    }

    // Analytics metrics
    public enum AnalyticsMetric {
        IMPRESSION,
        SAVE,
        PIN_CLICK,
        OUTBOUND_CLICK,
        VIDEO_MRC_VIEW,
        VIDEO_AVG_WATCH_TIME,
        VIDEO_V50_WATCH_TIME,
        QUARTILE_95_PERCENT_VIEW,
        ENGAGEMENT,
        ENGAGEMENT_RATE,
        TOTAL_ENGAGEMENT,
        SAVE_RATE,
        VIDEO_START,
        TOTAL_COMMENTS,
        TOTAL_REACTIONS,
        CAROUSEL_SWIPE,
        QUIZ_COMPLETED,
        QUIZ_START,
        SHOWCASE_PIN_CLICK,
        SHOWCASE_SUBPIN_CLICKTHROUGH,
        WEB_SESSIONS,
        WEB_CHECKOUT_CONVERSION_RATE,
        TOTAL_CLICK_CHECKOUT,
        TOTAL_CLICK_LEAD,
        TOTAL_VIEW_CATEGORY,
        TOTAL_VIEW_BRAND,
        TOTAL_SIGNUP,
        TOTAL_PAGE_VISIT,
        TOTAL_ADD_TO_CART,
        TOTAL_CHECKOUT,
        TOTAL_CUSTOM,
        TOTAL_LEAD,
        TOTAL_SEARCH,
        TOTAL_VIEW_VIDEO,
        TOTAL_WATCH_VIDEO
    }

    // Ad objectives
    public enum CampaignObjective {
        AWARENESS,
        BRAND_AWARENESS,
        VIDEO_VIEW,
        CONSIDERATION,
        TRAFFIC,
        APP_INSTALL,
        CONVERSIONS,
        CATALOG_SALES,
        WEB_CONVERSIONS,
        LEAD_GENERATION,
        SHOPPING_CONVERSIONS
    }

    // Ad creative types
    public enum CreativeType {
        REGULAR,
        VIDEO,
        SHOPPING,
        CAROUSEL,
        MAX_VIDEO,
        SHOP_THE_PIN,
        COLLECTION,
        IDEA,
        SHOWCASE,
        QUIZ
    }

    // Targeting types
    public enum TargetingType {
        INTEREST,
        KEYWORD,
        AUDIENCE_INCLUDE,
        AUDIENCE_EXCLUDE,
        GENDER,
        AGE,
        LOCATION,
        PLACEMENT,
        DEVICE,
        LANGUAGE,
        EXPANDED,
        SHOPPING_RETARGETING,
        ACTALIKE
    }

    // Audience types
    public enum AudienceType {
        CUSTOMER_LIST,
        VISITOR,
        ENGAGEMENT,
        ACTALIKE,
        PERSONA
    }

    // Product availability
    public enum ProductAvailability {
        IN_STOCK,
        OUT_OF_STOCK,
        PREORDER,
        AVAILABLE_FOR_ORDER,
        DISCONTINUED
    }

    // Currency codes
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
        PLN,
        CHF,
        HUF,
        CZK,
        RON,
        BGN,
        HRK,
        RUB,
        TRY,
        BRL,
        MXN,
        ARS,
        CLP,
        COP,
        PEN,
        UYU,
        JPY,
        CNY,
        HKD,
        TWD,
        KRW,
        SGD,
        THB,
        IDR,
        MYR,
        PHP,
        VND,
        INR,
        PKR,
        ZAR,
        EGP,
        MAD,
        AED,
        SAR,
        ILS
    }

    // Conversion event types
    public enum ConversionEventType {
        PAGE_VISIT,
        SIGNUP,
        CHECKOUT,
        CUSTOM,
        ADD_TO_CART,
        SEARCH,
        VIEW_CATEGORY,
        LEAD,
        APP_INSTALL,
        WATCH_VIDEO,
        UNKNOWN
    }

    // Board section types
    public enum BoardSectionType {
        NORMAL,
        SHOPPING,
        TRAVEL,
        DIY,
        FASHION,
        HOME,
        FOOD,
        BEAUTY,
        WEDDING,
        HOLIDAY,
        CUSTOM
    }

    // Shopping settings
    public enum ShoppingRecommendationType {
        DISABLED,
        CONSIDERED,
        RECOMMENDED,
        BESTSELLER,
        TRENDING,
        NEW_ARRIVAL
    }

    // Merchant status
    public enum MerchantStatus {
        ACTIVE,
        PENDING,
        REQUIRES_REVIEW,
        SUSPENDED,
        ARCHIVED
    }

    // Report format
    public enum ReportFormat {
        JSON,
        CSV,
        TSV,
        XML
    }

    // Report granularity
    public enum ReportGranularity {
        TOTAL,
        DAY,
        HOUR,
        WEEK,
        MONTH
    }

    // Filter operators
    public enum FilterOperator {
        LESS_THAN,
        GREATER_THAN,
        NOT_EQUALS,
        EQUALS,
        GREATER_THAN_OR_EQUALS,
        LESS_THAN_OR_EQUALS,
        IN_LIST,
        NOT_IN_LIST,
        CONTAINS,
        DOES_NOT_CONTAIN,
        STARTS_WITH,
        ENDS_WITH
    }

    // Error codes
    public enum PinterestErrorCode {
        UNAUTHORIZED(401),
        FORBIDDEN(403),
        NOT_FOUND(404),
        RATE_LIMIT_EXCEEDED(429),
        INVALID_PARAMETERS(400),
        INTERNAL_ERROR(500),
        SERVICE_UNAVAILABLE(503);

        private final int code;

        PinterestErrorCode(int code) {
            this.code = code;
        }

        public int getCode() { return code; }
    }

    // Webhook events
    public enum WebhookEvent {
        PIN_CREATE,
        PIN_UPDATE,
        PIN_DELETE,
        BOARD_CREATE,
        BOARD_UPDATE,
        BOARD_DELETE,
        USER_FOLLOW,
        USER_UNFOLLOW,
        COMMENT_CREATE,
        COMMENT_DELETE,
        SAVE_PIN,
        UNSAVE_PIN
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
    public PinterestFeatures getFeatures() {
        return features;
    }
    public void setFeatures(PinterestFeatures features) {
        this.features = features;
    }
    public PinterestLimits getLimits() {
        return limits;
    }
    public void setLimits(PinterestLimits limits) {
        this.limits = limits;
    }

    // Add required rate limit methods
    public Integer getMaxRequestsPerHour() {
        return limits != null ? limits.getRateLimitPerHour() : 1000;
    }

    public Integer getMaxRequestsPerMinute() {
        return limits != null ? limits.getRateLimitPerMinute() : 300;
    }

    public Integer getMaxRequestsPerSecond() {
        return 10; // Pinterest default
    }

    @Override
    public String getAuthorizationUrl() {
        return "https://www.pinterest.com/oauth/";
    }

    @Override
    public String getTokenUrl() {
        return "https://api.pinterest.com/v5/oauth/token";
    }
}
