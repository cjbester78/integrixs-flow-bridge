package com.integrixs.adapters.social.linkedin;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import com.integrixs.adapters.social.base.SocialMediaAdapterConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "integrixs.adapters.linkedin.ads")
@EqualsAndHashCode(callSuper = true)
public class LinkedInAdsApiConfig extends SocialMediaAdapterConfig {
    
    private String clientId;
    private String clientSecret;
    private String adAccountId; // Sponsor account ID
    private String organizationUrn; // Organization URN for company ads
    private LinkedInAdsFeatures features = new LinkedInAdsFeatures();
    private LinkedInAdsLimits limits = new LinkedInAdsLimits();
    
    @Data
    public static class LinkedInAdsFeatures {
        private boolean enableCampaignManagement = true;
        private boolean enableAdCreativeManagement = true;
        private boolean enableAudienceTargeting = true;
        private boolean enableBudgetManagement = true;
        private boolean enableBidding = true;
        private boolean enableAnalytics = true;
        private boolean enableConversionTracking = true;
        private boolean enableLeadGenForms = true;
        private boolean enableMatchedAudiences = true;
        private boolean enableLookalikeAudiences = true;
        private boolean enableDynamicAds = true;
        private boolean enableSponsoredContent = true;
        private boolean enableSponsoredMessaging = true;
        private boolean enableTextAds = true;
        private boolean enableVideoAds = true;
        private boolean enableCarouselAds = true;
        private boolean enableEventAds = true;
        private boolean enableJobAds = true;
        private boolean enableFollowerAds = true;
        private boolean enableSpotlightAds = true;
        private boolean enableBoostPosts = true;
        private boolean enableABTesting = true;
        private boolean enableReporting = true;
    }
    
    @Data
    public static class LinkedInAdsLimits {
        private int maxCampaignsPerAccount = 1000;
        private int maxAdGroupsPerCampaign = 100;
        private int maxAdsPerAdGroup = 50;
        private double minDailyBudget = 10.00; // USD
        private double maxDailyBudget = 100000.00; // USD
        private double minLifetimeBudget = 10.00; // USD
        private double maxLifetimeBudget = 1000000.00; // USD
        private double minBidAmount = 2.00; // USD
        private double maxBidAmount = 1000.00; // USD
        private int maxAudienceSize = 300000000;
        private int minAudienceSize = 300;
        private int maxCustomAudiences = 500;
        private int maxLookalikeAudiences = 100;
        private int maxConversionEventsPerDay = 1000000;
        private int maxCreativesPerCampaign = 100;
    }
    
    // Campaign objectives
    public enum CampaignObjective {
        BRAND_AWARENESS,
        WEBSITE_VISITS,
        ENGAGEMENT,
        VIDEO_VIEWS,
        LEAD_GENERATION,
        WEBSITE_CONVERSIONS,
        JOB_APPLICANTS,
        TALENT_LEADS
    }
    
    // Campaign types
    public enum CampaignType {
        SPONSORED_CONTENT,
        SPONSORED_MESSAGING,
        TEXT_ADS,
        DYNAMIC_ADS,
        SPONSORED_JOBS
    }
    
    // Ad format types
    public enum AdFormat {
        SINGLE_IMAGE,
        CAROUSEL,
        VIDEO,
        TEXT_AD,
        SPOTLIGHT_AD,
        MESSAGE_AD,
        CONVERSATION_AD,
        EVENT_AD,
        SINGLE_JOB_AD,
        JOBS_AD,
        FOLLOWER_AD,
        DOCUMENT_AD
    }
    
    // Bidding types
    public enum BidType {
        CPM, // Cost per 1000 impressions
        CPC, // Cost per click
        CPV, // Cost per video view
        CPS  // Cost per send (for message ads)
    }
    
    // Optimization goals
    public enum OptimizationTarget {
        NONE,
        REACH,
        IMPRESSIONS,
        CLICKS,
        LANDING_PAGE_CLICKS,
        EXTERNAL_WEBSITE_CONVERSIONS,
        SPONSORED_INMAILS_OPENS,
        OTHER_ENGAGEMENTS,
        LEAD_GENERATION_MAIL_CONTACT_INFO,
        LEAD_GENERATION_MAIL_INTEREST
    }
    
    // Audience targeting criteria
    public enum TargetingCriteria {
        LOCATION,
        COMPANY,
        COMPANY_SIZE,
        COMPANY_INDUSTRY,
        JOB_TITLE,
        JOB_FUNCTION,
        JOB_SENIORITY,
        SKILLS,
        DEGREES,
        FIELDS_OF_STUDY,
        SCHOOLS,
        AGE,
        GENDER,
        MEMBER_TRAITS,
        MEMBER_INTERESTS,
        MEMBER_GROUPS,
        MATCHED_AUDIENCES,
        AUDIENCE_EXPANSION,
        LOOKALIKE_AUDIENCES
    }
    
    // Placement types
    public enum Placement {
        LINKEDIN_FEED,
        LINKEDIN_MESSAGING,
        LINKEDIN_RIGHT_RAIL,
        LINKEDIN_AUDIENCE_NETWORK
    }
    
    // Device targeting
    public enum DeviceType {
        DESKTOP,
        MOBILE,
        TABLET
    }
    
    // Conversion tracking types
    public enum ConversionType {
        ADD_TO_CART,
        DOWNLOAD,
        INSTALL,
        KEY_PAGE_VIEW,
        LEAD,
        PURCHASE,
        SIGN_UP,
        OTHER
    }
    
    // Reporting metrics
    public enum MetricType {
        IMPRESSIONS,
        CLICKS,
        CTR, // Click-through rate
        AVERAGE_CPC,
        AVERAGE_CPM,
        SPEND,
        REACH,
        FREQUENCY,
        VIDEO_STARTS,
        VIDEO_COMPLETIONS,
        VIDEO_COMPLETION_RATE,
        VIDEO_VIEWS,
        VIDEO_VIEW_RATE,
        CONVERSIONS,
        CONVERSION_RATE,
        COST_PER_CONVERSION,
        LEADS,
        LEAD_FORM_OPENS,
        LEAD_FORM_COMPLETION_RATE,
        COST_PER_LEAD,
        OPENS,
        OPEN_RATE,
        SENDS,
        COST_PER_SEND,
        SOCIAL_ACTIONS,
        COMMENTS,
        LIKES,
        SHARES,
        FOLLOWS,
        ENGAGEMENT_RATE,
        LANDING_PAGE_CLICKS,
        OTHER_CLICKS
    }
    
    // Campaign status
    public enum CampaignStatus {
        DRAFT,
        ACTIVE,
        PAUSED,
        CANCELED,
        COMPLETED,
        ARCHIVED
    }
    
    // Creative status
    public enum CreativeStatus {
        DRAFT,
        ACTIVE,
        PAUSED,
        CANCELED,
        PENDING_APPROVAL,
        APPROVED,
        REJECTED,
        SERVING_STOPPED
    }
    
    // Report time granularity
    public enum TimeGranularity {
        DAILY,
        MONTHLY,
        ALL
    }
}