package com.integrixs.adapters.social.facebook;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import com.integrixs.adapters.social.base.SocialMediaAdapterConfig;
@Component
@ConfigurationProperties(prefix = "integrixs.adapters.facebook.ads")
public class FacebookAdsApiConfig extends SocialMediaAdapterConfig {
    
    private String adAccountId;
    private String businessId;
    private FacebookAdsFeatures features = new FacebookAdsFeatures();
    private FacebookAdsLimits limits = new FacebookAdsLimits();
    
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
    // Getters and Setters
    public String getAdAccountId() {
        return adAccountId;
    }
    public void setAdAccountId(String adAccountId) {
        this.adAccountId = adAccountId;
    }
    public String getBusinessId() {
        return businessId;
    }
    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }
    public FacebookAdsFeatures getFeatures() {
        return features;
    }
    public void setFeatures(FacebookAdsFeatures features) {
        this.features = features;
    }
    public FacebookAdsLimits getLimits() {
        return limits;
    }
    public void setLimits(FacebookAdsLimits limits) {
        this.limits = limits;
    }
    public boolean isEnableCampaignManagement() {
        return enableCampaignManagement;
    }
    public void setEnableCampaignManagement(boolean enableCampaignManagement) {
        this.enableCampaignManagement = enableCampaignManagement;
    }
    public boolean isEnableAudienceTargeting() {
        return enableAudienceTargeting;
    }
    public void setEnableAudienceTargeting(boolean enableAudienceTargeting) {
        this.enableAudienceTargeting = enableAudienceTargeting;
    }
    public boolean isEnableBudgetOptimization() {
        return enableBudgetOptimization;
    }
    public void setEnableBudgetOptimization(boolean enableBudgetOptimization) {
        this.enableBudgetOptimization = enableBudgetOptimization;
    }
    public boolean isEnableCreativeAssets() {
        return enableCreativeAssets;
    }
    public void setEnableCreativeAssets(boolean enableCreativeAssets) {
        this.enableCreativeAssets = enableCreativeAssets;
    }
    public boolean isEnablePerformanceTracking() {
        return enablePerformanceTracking;
    }
    public void setEnablePerformanceTracking(boolean enablePerformanceTracking) {
        this.enablePerformanceTracking = enablePerformanceTracking;
    }
    public boolean isEnableAutomatedRules() {
        return enableAutomatedRules;
    }
    public void setEnableAutomatedRules(boolean enableAutomatedRules) {
        this.enableAutomatedRules = enableAutomatedRules;
    }
    public boolean isEnableA_BTesting() {
        return enableA_BTesting;
    }
    public void setEnableA_BTesting(boolean enableA_BTesting) {
        this.enableA_BTesting = enableA_BTesting;
    }
    public boolean isEnableCustomConversions() {
        return enableCustomConversions;
    }
    public void setEnableCustomConversions(boolean enableCustomConversions) {
        this.enableCustomConversions = enableCustomConversions;
    }
    public boolean isEnablePixelTracking() {
        return enablePixelTracking;
    }
    public void setEnablePixelTracking(boolean enablePixelTracking) {
        this.enablePixelTracking = enablePixelTracking;
    }
    public boolean isEnableLeadForms() {
        return enableLeadForms;
    }
    public void setEnableLeadForms(boolean enableLeadForms) {
        this.enableLeadForms = enableLeadForms;
    }
    public int getMaxCampaignsPerAccount() {
        return maxCampaignsPerAccount;
    }
    public void setMaxCampaignsPerAccount(int maxCampaignsPerAccount) {
        this.maxCampaignsPerAccount = maxCampaignsPerAccount;
    }
    public int getMaxAdSetsPerCampaign() {
        return maxAdSetsPerCampaign;
    }
    public void setMaxAdSetsPerCampaign(int maxAdSetsPerCampaign) {
        this.maxAdSetsPerCampaign = maxAdSetsPerCampaign;
    }
    public int getMaxAdsPerAdSet() {
        return maxAdsPerAdSet;
    }
    public void setMaxAdsPerAdSet(int maxAdsPerAdSet) {
        this.maxAdsPerAdSet = maxAdsPerAdSet;
    }
    public int getMaxCustomAudiences() {
        return maxCustomAudiences;
    }
    public void setMaxCustomAudiences(int maxCustomAudiences) {
        this.maxCustomAudiences = maxCustomAudiences;
    }
    public long getMinDailyBudgetCents() {
        return minDailyBudgetCents;
    }
    public void setMinDailyBudgetCents(long minDailyBudgetCents) {
        this.minDailyBudgetCents = minDailyBudgetCents;
    }
    public long getMaxDailyBudgetCents() {
        return maxDailyBudgetCents;
    }
    public void setMaxDailyBudgetCents(long maxDailyBudgetCents) {
        this.maxDailyBudgetCents = maxDailyBudgetCents;
    }
    public int getMaxBatchSize() {
        return maxBatchSize;
    }
    public void setMaxBatchSize(int maxBatchSize) {
        this.maxBatchSize = maxBatchSize;
    }
}