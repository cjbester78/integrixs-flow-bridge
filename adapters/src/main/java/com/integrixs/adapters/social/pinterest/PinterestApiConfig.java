package com.integrixs.adapters.social.pinterest;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import com.integrixs.adapters.social.base.SocialMediaAdapterConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "integrixs.adapters.pinterest")
@EqualsAndHashCode(callSuper = true)
public class PinterestApiConfig extends SocialMediaAdapterConfig {
    
    private String appId;
    private String appSecret;
    private String advertiserId;
    private PinterestFeatures features = new PinterestFeatures();
    private PinterestLimits limits = new PinterestLimits();
    
    @Data
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
    }
    
    @Data
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
}