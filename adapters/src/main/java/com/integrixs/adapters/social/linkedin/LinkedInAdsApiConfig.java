package com.integrixs.adapters.social.linkedin;

import com.integrixs.adapters.social.base.SocialMediaAdapterConfig;
public class LinkedInAdsApiConfig extends SocialMediaAdapterConfig {
    private String adAccountId;
    private String organizationId;

    // Feature flags
    private boolean enableAnalytics;
    private boolean enableAudienceTargeting;
    private boolean enableConversionTracking;
    private boolean enableBudgetManagement;
    private boolean enableLeadGenForms;
    private boolean enableVideoAnalytics;

    // API limits
    private int maxCampaignsPerRequest;
    private int maxCreativesPerRequest;
    private int maxAudiencesPerRequest;

    // Polling intervals(in milliseconds)
    private long campaignPollingInterval;
    private long adGroupPollingInterval;
    private long creativePollingInterval;
    private long audiencePollingInterval;
    private long conversionPollingInterval;
    private long budgetCheckInterval;
    private long leadGenPollingInterval;

    private String authorizationUrl;
    private String tokenUrl;
    private String platformName;
    private String linkedInApiVersion;
    private String restliProtocolVersion;
    private String defaultAlertLevel;

    @Override
    public String getAuthorizationUrl() {
        return authorizationUrl;
    }

    @Override
    public String getTokenUrl() {
        return tokenUrl;
    }

    @Override
    public String getPlatformName() {
        return platformName;
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
    // No overrides with defaults - all values must come from configuration
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
    public void setAuthorizationUrl(String authorizationUrl) {
        this.authorizationUrl = authorizationUrl;
    }
    public void setTokenUrl(String tokenUrl) {
        this.tokenUrl = tokenUrl;
    }
    public void setPlatformName(String platformName) {
        this.platformName = platformName;
    }
    public String getLinkedInApiVersion() {
        return linkedInApiVersion;
    }
    public void setLinkedInApiVersion(String linkedInApiVersion) {
        this.linkedInApiVersion = linkedInApiVersion;
    }
    public String getRestliProtocolVersion() {
        return restliProtocolVersion;
    }
    public void setRestliProtocolVersion(String restliProtocolVersion) {
        this.restliProtocolVersion = restliProtocolVersion;
    }
    public String getDefaultAlertLevel() {
        return defaultAlertLevel;
    }
    public void setDefaultAlertLevel(String defaultAlertLevel) {
        this.defaultAlertLevel = defaultAlertLevel;
    }
}
