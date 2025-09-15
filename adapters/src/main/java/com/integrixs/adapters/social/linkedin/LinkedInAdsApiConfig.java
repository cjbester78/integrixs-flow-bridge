package com.integrixs.adapters.social.linkedin;

import com.integrixs.adapters.social.base.SocialMediaAdapterConfig;
public class LinkedInAdsApiConfig extends SocialMediaAdapterConfig {
    private String adAccountId;
    private String organizationId;
    private String baseUrl = "https://api.linkedin.com";
    private String apiVersion = "v2";

    // Feature flags
    private boolean enableAnalytics = true;
    private boolean enableAudienceTargeting = false;
    private boolean enableConversionTracking = false;
    private boolean enableBudgetManagement = true;
    private boolean enableLeadGenForms = false;
    private boolean enableVideoAnalytics = false;

    // API limits
    private int maxCampaignsPerRequest = 100;
    private int maxCreativesPerRequest = 100;
    private int maxAudiencesPerRequest = 50;

    // Polling intervals(in milliseconds)
    private long campaignPollingInterval = 300000; // 5 minutes
    private long adGroupPollingInterval = 600000; // 10 minutes
    private long creativePollingInterval = 900000; // 15 minutes
    private long audiencePollingInterval = 3600000; // 1 hour
    private long conversionPollingInterval = 1800000; // 30 minutes
    private long budgetCheckInterval = 600000; // 10 minutes
    private long leadGenPollingInterval = 300000; // 5 minutes
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
    public String getBaseUrl() {
        return baseUrl;
    }
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    public String getApiVersion() {
        return apiVersion;
    }
    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }
    public boolean isEnableAnalytics() {
        return enableAnalytics;
    }
    public void setEnableAnalytics(boolean enableAnalytics) {
        this.enableAnalytics = enableAnalytics;
    }
    public boolean isEnableAudienceTargeting() {
        return enableAudienceTargeting;
    }
    public void setEnableAudienceTargeting(boolean enableAudienceTargeting) {
        this.enableAudienceTargeting = enableAudienceTargeting;
    }
    public boolean isEnableConversionTracking() {
        return enableConversionTracking;
    }
    public void setEnableConversionTracking(boolean enableConversionTracking) {
        this.enableConversionTracking = enableConversionTracking;
    }
    public boolean isEnableBudgetManagement() {
        return enableBudgetManagement;
    }
    public void setEnableBudgetManagement(boolean enableBudgetManagement) {
        this.enableBudgetManagement = enableBudgetManagement;
    }
    public boolean isEnableLeadGenForms() {
        return enableLeadGenForms;
    }
    public void setEnableLeadGenForms(boolean enableLeadGenForms) {
        this.enableLeadGenForms = enableLeadGenForms;
    }
    public boolean isEnableVideoAnalytics() {
        return enableVideoAnalytics;
    }
    public void setEnableVideoAnalytics(boolean enableVideoAnalytics) {
        this.enableVideoAnalytics = enableVideoAnalytics;
    }
    public int getMaxCampaignsPerRequest() {
        return maxCampaignsPerRequest;
    }
    public void setMaxCampaignsPerRequest(int maxCampaignsPerRequest) {
        this.maxCampaignsPerRequest = maxCampaignsPerRequest;
    }
    public int getMaxCreativesPerRequest() {
        return maxCreativesPerRequest;
    }
    public void setMaxCreativesPerRequest(int maxCreativesPerRequest) {
        this.maxCreativesPerRequest = maxCreativesPerRequest;
    }
    public int getMaxAudiencesPerRequest() {
        return maxAudiencesPerRequest;
    }
    public void setMaxAudiencesPerRequest(int maxAudiencesPerRequest) {
        this.maxAudiencesPerRequest = maxAudiencesPerRequest;
    }
    public long getCampaignPollingInterval() {
        return campaignPollingInterval;
    }
    public void setCampaignPollingInterval(long campaignPollingInterval) {
        this.campaignPollingInterval = campaignPollingInterval;
    }
    public long getAdGroupPollingInterval() {
        return adGroupPollingInterval;
    }
    public void setAdGroupPollingInterval(long adGroupPollingInterval) {
        this.adGroupPollingInterval = adGroupPollingInterval;
    }
    public long getCreativePollingInterval() {
        return creativePollingInterval;
    }
    public void setCreativePollingInterval(long creativePollingInterval) {
        this.creativePollingInterval = creativePollingInterval;
    }
    public long getAudiencePollingInterval() {
        return audiencePollingInterval;
    }
    public void setAudiencePollingInterval(long audiencePollingInterval) {
        this.audiencePollingInterval = audiencePollingInterval;
    }
    public long getConversionPollingInterval() {
        return conversionPollingInterval;
    }
    public void setConversionPollingInterval(long conversionPollingInterval) {
        this.conversionPollingInterval = conversionPollingInterval;
    }
    public long getBudgetCheckInterval() {
        return budgetCheckInterval;
    }
    public void setBudgetCheckInterval(long budgetCheckInterval) {
        this.budgetCheckInterval = budgetCheckInterval;
    }
    public long getLeadGenPollingInterval() {
        return leadGenPollingInterval;
    }
    public void setLeadGenPollingInterval(long leadGenPollingInterval) {
        this.leadGenPollingInterval = leadGenPollingInterval;
    }
}
