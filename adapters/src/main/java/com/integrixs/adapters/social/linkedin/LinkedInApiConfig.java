package com.integrixs.adapters.social.linkedin;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import com.integrixs.adapters.social.base.SocialMediaAdapterConfig;
@Component
@ConfigurationProperties(prefix = "integrixs.adapters.linkedin.api")
public class LinkedInApiConfig extends SocialMediaAdapterConfig {

    private String clientId;
    private String clientSecret;
    private String memberUrn; // Member URN(e.g., urn:li:person:123ABC)
    private String organizationId; // Organization ID for company pages
    private LinkedInFeatures features = new LinkedInFeatures();
    private LinkedInLimits limits = new LinkedInLimits();

        public static class LinkedInFeatures {
        private boolean enableProfileAccess = true;
        private boolean enableCompanyPageManagement = true;
        private boolean enableArticlePublishing = true;
        private boolean enablePostSharing = true;
        private boolean enableVideoSharing = true;
        private boolean enableImageSharing = true;
        private boolean enableDocumentSharing = true;
        private boolean enableCommentManagement = true;
        private boolean enableMessaging = true;
        private boolean enableConnectionManagement = true;
        private boolean enableAnalytics = true;
        private boolean enableEventCreation = true;
        private boolean enableGroupManagement = false;
        private boolean enableLiveVideo = false;
        private boolean enableNewsletters = true;
        private boolean enableHashtagTracking = true;
        private boolean enableEmployeeAdvocacy = true;
        private boolean enableContentSuggestions = true;
        private boolean enableAudienceTargeting = true;
        private boolean enableScheduledPosts = true;
    }

        public static class LinkedInLimits {
        private int maxPostLength = 3000; // characters
        private int maxArticleLength = 125000; // characters
        private int maxCommentLength = 1250; // characters
        private int maxHashtagsPerPost = 30;
        private int maxMentionsPerPost = 50;
        private int maxImagesPerPost = 20;
        private int maxVideoSizeMB = 5120; // 5GB
        private int maxVideoLength = 600; // 10 minutes in seconds
        private int maxImageSizeMB = 10;
        private int maxDocumentSizeMB = 100;
        private int maxConnectionsPerDay = 100;
        private int maxMessagesPerDay = 250;
        private int maxPostsPerDay = 100;
        private int maxBatchSize = 50; // for batch operations
    }

    // Post visibility types
    public enum Visibility {
        CONNECTIONS, // Visible to 1st - degree connections
        PUBLIC,     // Visible to anyone on LinkedIn
        LOGGED_IN, // Visible to logged - in LinkedIn members
        CONTAINER   // Visible only within a container(e.g., a group)
    }

    // Share media categories
    public enum MediaCategory {
        NONE,
        ARTICLE,
        IMAGE,
        VIDEO,
        NATIVE_DOCUMENT,
        LIVE_VIDEO,
        LEARNING_COURSE
    }

    // Content distribution types
    public enum Distribution {
        MAIN_FEED,
        NONE,
        CONTAINER_ENTITY
    }

    // Analytics metrics
    public enum MetricType {
        IMPRESSIONS,
        CLICKS,
        ENGAGEMENT,
        ENGAGEMENT_RATE,
        LIKES,
        COMMENTS,
        SHARES,
        FOLLOWS,
        VIDEO_VIEWS,
        VIDEO_COMPLETION_RATE,
        CLICK_THROUGH_RATE,
        SOCIAL_ACTIONS,
        UNIQUE_IMPRESSIONS
    }

    // Connection types
    public enum ConnectionType {
        FIRST_DEGREE,
        SECOND_DEGREE,
        THIRD_DEGREE,
        OUT_OF_NETWORK
    }

    // Organization role types
    public enum OrganizationRole {
        ADMIN,
        ANALYST,
        ASSOCIATE,
        CONTENT_ADMIN,
        CURATOR,
        LEAD_GEN_FORMS_MANAGER,
        MANAGER,
        MESSAGING_AGENT,
        PAID_MEDIA_ADMIN,
        RECRUITER,
        RECRUITMENT_ADMIN
    }

    // Profile fields available
    public enum ProfileField {
        ID,
        FIRST_NAME,
        LAST_NAME,
        MAIDEN_NAME,
        LOCALIZED_FIRST_NAME,
        LOCALIZED_LAST_NAME,
        HEADLINE,
        VANITY_NAME,
        LOCATION,
        INDUSTRY,
        SUMMARY,
        SPECIALTIES,
        POSITIONS,
        PICTURE_URL,
        SITE_STANDARD_PROFILE_REQUEST,
        API_STANDARD_PROFILE_REQUEST,
        PUBLIC_PROFILE_URL,
        EMAIL_ADDRESS,
        NUM_CONNECTIONS,
        NUM_CONNECTIONS_CAPPED,
        CURRENT_SHARE,
        TIMESTAMP,
        BOUND_ACCOUNT_TYPES,
        PHONE_NUMBERS,
        IM_ACCOUNTS
    }

    // Activity types for analytics
    public enum ActivityType {
        POST_SHARE,
        ARTICLE_SHARE,
        VIDEO_SHARE,
        IMAGE_SHARE,
        DOCUMENT_SHARE,
        POLL_SHARE,
        EVENT_SHARE,
        CELEBRATION_CREATE,
        NATIVE_DOCUMENT_SHARE,
        COMMENT,
        REACTION,
        FOLLOW
    }

    // Reaction types
    public enum ReactionType {
        LIKE,
        PRAISE,
        APPRECIATION,
        EMPATHY,
        INTEREST,
        CELEBRATION,
        ENTERTAINMENT,
        MAYBE
    }
    // Getters and Setters
    public String getClientId() {
        return clientId;
    }
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    public String getClientSecret() {
        return clientSecret;
    }
    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }
    public String getMemberUrn() {
        return memberUrn;
    }
    public void setMemberUrn(String memberUrn) {
        this.memberUrn = memberUrn;
    }
    public String getOrganizationId() {
        return organizationId;
    }
    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }
    public LinkedInFeatures getFeatures() {
        return features;
    }
    public void setFeatures(LinkedInFeatures features) {
        this.features = features;
    }
    public LinkedInLimits getLimits() {
        return limits;
    }
    public void setLimits(LinkedInLimits limits) {
        this.limits = limits;
    }
    public boolean isEnableProfileAccess() {
        return enableProfileAccess;
    }
    public void setEnableProfileAccess(boolean enableProfileAccess) {
        this.enableProfileAccess = enableProfileAccess;
    }
    public boolean isEnableCompanyPageManagement() {
        return enableCompanyPageManagement;
    }
    public void setEnableCompanyPageManagement(boolean enableCompanyPageManagement) {
        this.enableCompanyPageManagement = enableCompanyPageManagement;
    }
    public boolean isEnableArticlePublishing() {
        return enableArticlePublishing;
    }
    public void setEnableArticlePublishing(boolean enableArticlePublishing) {
        this.enableArticlePublishing = enableArticlePublishing;
    }
    public boolean isEnablePostSharing() {
        return enablePostSharing;
    }
    public void setEnablePostSharing(boolean enablePostSharing) {
        this.enablePostSharing = enablePostSharing;
    }
    public boolean isEnableVideoSharing() {
        return enableVideoSharing;
    }
    public void setEnableVideoSharing(boolean enableVideoSharing) {
        this.enableVideoSharing = enableVideoSharing;
    }
    public boolean isEnableImageSharing() {
        return enableImageSharing;
    }
    public void setEnableImageSharing(boolean enableImageSharing) {
        this.enableImageSharing = enableImageSharing;
    }
    public boolean isEnableDocumentSharing() {
        return enableDocumentSharing;
    }
    public void setEnableDocumentSharing(boolean enableDocumentSharing) {
        this.enableDocumentSharing = enableDocumentSharing;
    }
    public boolean isEnableCommentManagement() {
        return enableCommentManagement;
    }
    public void setEnableCommentManagement(boolean enableCommentManagement) {
        this.enableCommentManagement = enableCommentManagement;
    }
    public boolean isEnableMessaging() {
        return enableMessaging;
    }
    public void setEnableMessaging(boolean enableMessaging) {
        this.enableMessaging = enableMessaging;
    }
    public boolean isEnableConnectionManagement() {
        return enableConnectionManagement;
    }
    public void setEnableConnectionManagement(boolean enableConnectionManagement) {
        this.enableConnectionManagement = enableConnectionManagement;
    }
    public boolean isEnableAnalytics() {
        return enableAnalytics;
    }
    public void setEnableAnalytics(boolean enableAnalytics) {
        this.enableAnalytics = enableAnalytics;
    }
    public boolean isEnableEventCreation() {
        return enableEventCreation;
    }
    public void setEnableEventCreation(boolean enableEventCreation) {
        this.enableEventCreation = enableEventCreation;
    }
    public boolean isEnableGroupManagement() {
        return enableGroupManagement;
    }
    public void setEnableGroupManagement(boolean enableGroupManagement) {
        this.enableGroupManagement = enableGroupManagement;
    }
    public boolean isEnableLiveVideo() {
        return enableLiveVideo;
    }
    public void setEnableLiveVideo(boolean enableLiveVideo) {
        this.enableLiveVideo = enableLiveVideo;
    }
    public boolean isEnableNewsletters() {
        return enableNewsletters;
    }
    public void setEnableNewsletters(boolean enableNewsletters) {
        this.enableNewsletters = enableNewsletters;
    }
    public boolean isEnableHashtagTracking() {
        return enableHashtagTracking;
    }
    public void setEnableHashtagTracking(boolean enableHashtagTracking) {
        this.enableHashtagTracking = enableHashtagTracking;
    }
    public boolean isEnableEmployeeAdvocacy() {
        return enableEmployeeAdvocacy;
    }
    public void setEnableEmployeeAdvocacy(boolean enableEmployeeAdvocacy) {
        this.enableEmployeeAdvocacy = enableEmployeeAdvocacy;
    }
    public boolean isEnableContentSuggestions() {
        return enableContentSuggestions;
    }
    public void setEnableContentSuggestions(boolean enableContentSuggestions) {
        this.enableContentSuggestions = enableContentSuggestions;
    }
    public boolean isEnableAudienceTargeting() {
        return enableAudienceTargeting;
    }
    public void setEnableAudienceTargeting(boolean enableAudienceTargeting) {
        this.enableAudienceTargeting = enableAudienceTargeting;
    }
    public boolean isEnableScheduledPosts() {
        return enableScheduledPosts;
    }
    public void setEnableScheduledPosts(boolean enableScheduledPosts) {
        this.enableScheduledPosts = enableScheduledPosts;
    }
    public int getMaxPostLength() {
        return maxPostLength;
    }
    public void setMaxPostLength(int maxPostLength) {
        this.maxPostLength = maxPostLength;
    }
    public int getMaxArticleLength() {
        return maxArticleLength;
    }
    public void setMaxArticleLength(int maxArticleLength) {
        this.maxArticleLength = maxArticleLength;
    }
    public int getMaxCommentLength() {
        return maxCommentLength;
    }
    public void setMaxCommentLength(int maxCommentLength) {
        this.maxCommentLength = maxCommentLength;
    }
    public int getMaxHashtagsPerPost() {
        return maxHashtagsPerPost;
    }
    public void setMaxHashtagsPerPost(int maxHashtagsPerPost) {
        this.maxHashtagsPerPost = maxHashtagsPerPost;
    }
    public int getMaxMentionsPerPost() {
        return maxMentionsPerPost;
    }
    public void setMaxMentionsPerPost(int maxMentionsPerPost) {
        this.maxMentionsPerPost = maxMentionsPerPost;
    }
    public int getMaxImagesPerPost() {
        return maxImagesPerPost;
    }
    public void setMaxImagesPerPost(int maxImagesPerPost) {
        this.maxImagesPerPost = maxImagesPerPost;
    }
    public int getMaxVideoSizeMB() {
        return maxVideoSizeMB;
    }
    public void setMaxVideoSizeMB(int maxVideoSizeMB) {
        this.maxVideoSizeMB = maxVideoSizeMB;
    }
    public int getMaxVideoLength() {
        return maxVideoLength;
    }
    public void setMaxVideoLength(int maxVideoLength) {
        this.maxVideoLength = maxVideoLength;
    }
    public int getMaxImageSizeMB() {
        return maxImageSizeMB;
    }
    public void setMaxImageSizeMB(int maxImageSizeMB) {
        this.maxImageSizeMB = maxImageSizeMB;
    }
    public int getMaxDocumentSizeMB() {
        return maxDocumentSizeMB;
    }
    public void setMaxDocumentSizeMB(int maxDocumentSizeMB) {
        this.maxDocumentSizeMB = maxDocumentSizeMB;
    }
    public int getMaxConnectionsPerDay() {
        return maxConnectionsPerDay;
    }
    public void setMaxConnectionsPerDay(int maxConnectionsPerDay) {
        this.maxConnectionsPerDay = maxConnectionsPerDay;
    }
    public int getMaxMessagesPerDay() {
        return maxMessagesPerDay;
    }
    public void setMaxMessagesPerDay(int maxMessagesPerDay) {
        this.maxMessagesPerDay = maxMessagesPerDay;
    }
    public int getMaxPostsPerDay() {
        return maxPostsPerDay;
    }
    public void setMaxPostsPerDay(int maxPostsPerDay) {
        this.maxPostsPerDay = maxPostsPerDay;
    }
    public int getMaxBatchSize() {
        return maxBatchSize;
    }
    public void setMaxBatchSize(int maxBatchSize) {
        this.maxBatchSize = maxBatchSize;
    }
}
