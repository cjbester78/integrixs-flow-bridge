package com.integrixs.adapters.social.facebook;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import com.integrixs.adapters.social.base.SocialMediaAdapterConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Component
@ConfigurationProperties(prefix = "integrixs.adapters.facebook.ads")
@EqualsAndHashCode(callSuper = true)
public class FacebookAdsApiConfig extends SocialMediaAdapterConfig {
    
    private String adAccountId;
    private String businessId;
    private FacebookAdsFeatures features = new FacebookAdsFeatures();
    private FacebookAdsLimits limits = new FacebookAdsLimits();
    
    @Data
    public static class FacebookAdsFeatures {
        private boolean enableCampaignManagement = true;
        private boolean enableAudienceTargeting = true;
        private boolean enableBudgetOptimization = true;
        private boolean enableCreativeAssets = true;
        private boolean enablePerformanceTracking = true;
        private boolean enableAutomatedRules = false;
        private boolean enableA_BTesting = true;
        private boolean enableCustomConversions = true;
        private boolean enablePixelTracking = true;
        private boolean enableLeadForms = true;
    }
    
    @Data
    public static class FacebookAdsLimits {
        private int maxCampaignsPerAccount = 5000;
        private int maxAdSetsPerCampaign = 5000;
        private int maxAdsPerAdSet = 5000;
        private int maxCustomAudiences = 500;
        private long minDailyBudgetCents = 100; // $1.00
        private long maxDailyBudgetCents = 100000000; // $1,000,000
        private int maxBatchSize = 50;
    }
    
    // Campaign objectives
    public enum CampaignObjective {
        BRAND_AWARENESS,
        REACH,
        TRAFFIC,
        ENGAGEMENT,
        APP_INSTALLS,
        VIDEO_VIEWS,
        LEAD_GENERATION,
        MESSAGES,
        CONVERSIONS,
        CATALOG_SALES,
        STORE_TRAFFIC
    }
    
    // Billing events
    public enum BillingEvent {
        APP_INSTALLS,
        CLICKS,
        IMPRESSIONS,
        LINK_CLICKS,
        OFFER_CLAIMS,
        PAGE_LIKES,
        POST_ENGAGEMENT,
        VIDEO_VIEWS,
        THRUPLAY
    }
    
    // Optimization goals
    public enum OptimizationGoal {
        APP_INSTALLS,
        BRAND_AWARENESS,
        CLICKS,
        CONVERSATIONS,
        ENGAGED_USERS,
        EVENT_RESPONSES,
        IMPRESSIONS,
        LANDING_PAGE_VIEWS,
        LEAD_GENERATION,
        LINK_CLICKS,
        OFFSITE_CONVERSIONS,
        PAGE_LIKES,
        POST_ENGAGEMENT,
        REACH,
        SOCIAL_IMPRESSIONS,
        VIDEO_VIEWS,
        VALUE,
        THRUPLAY,
        DERIVED_EVENTS
    }
}