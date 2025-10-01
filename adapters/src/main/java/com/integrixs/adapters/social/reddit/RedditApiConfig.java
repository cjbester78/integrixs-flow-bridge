package com.integrixs.adapters.social.reddit;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import com.integrixs.adapters.social.base.SocialMediaAdapterConfig;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "integrixs.adapters.reddit")
public class RedditApiConfig extends SocialMediaAdapterConfig {

    private String userAgent;
    private String username;
    private String password;
    private RedditFeatures features = new RedditFeatures();
    private RedditLimits limits = new RedditLimits();
    private RedditPollingConfig pollingConfig = new RedditPollingConfig();

    @Override
    public String getPlatformName() {
        return "reddit";
    }

    @Override
    public String getAuthorizationUrl() {
        return "https://www.reddit.com/api/v1/authorize";
    }

    @Override
    public String getTokenUrl() {
        return "https://www.reddit.com/api/v1/access_token";
    }

    public static class RedditFeatures {
        private boolean enablePostManagement = true;
        private boolean enableCommentManagement = true;
        private boolean enableSubredditMonitoring = true;
        private boolean enableUserTracking = true;
        private boolean enableModeration = true;
        private boolean enableWikiManagement = true;
        private boolean enableFlairManagement = true;
        private boolean enableMultireddit = true;
        private boolean enableLiveThreads = true;
        private boolean enablePrivateMessages = true;
        private boolean enableSearch = true;
        private boolean enableVoting = true;
        private boolean enableAwards = true;
        private boolean enablePolls = true;
        private boolean enableCollections = true;
        private boolean enableCrossposting = true;
        private boolean enableScheduledPosts = true;
        private boolean enableAnalytics = true;

        // Getters and Setters for RedditFeatures
        public boolean isEnablePostManagement() { return enablePostManagement; }
        public void setEnablePostManagement(boolean enablePostManagement) { this.enablePostManagement = enablePostManagement; }
        public boolean isEnableCommentManagement() { return enableCommentManagement; }
        public void setEnableCommentManagement(boolean enableCommentManagement) { this.enableCommentManagement = enableCommentManagement; }
        public boolean isEnableSubredditMonitoring() { return enableSubredditMonitoring; }
        public void setEnableSubredditMonitoring(boolean enableSubredditMonitoring) { this.enableSubredditMonitoring = enableSubredditMonitoring; }
        public boolean isEnableUserTracking() { return enableUserTracking; }
        public void setEnableUserTracking(boolean enableUserTracking) { this.enableUserTracking = enableUserTracking; }
        public boolean isEnableModeration() { return enableModeration; }
        public void setEnableModeration(boolean enableModeration) { this.enableModeration = enableModeration; }
        public boolean isEnableWikiManagement() { return enableWikiManagement; }
        public void setEnableWikiManagement(boolean enableWikiManagement) { this.enableWikiManagement = enableWikiManagement; }
        public boolean isEnableFlairManagement() { return enableFlairManagement; }
        public void setEnableFlairManagement(boolean enableFlairManagement) { this.enableFlairManagement = enableFlairManagement; }
        public boolean isEnableMultireddit() { return enableMultireddit; }
        public void setEnableMultireddit(boolean enableMultireddit) { this.enableMultireddit = enableMultireddit; }
        public boolean isEnableLiveThreads() { return enableLiveThreads; }
        public void setEnableLiveThreads(boolean enableLiveThreads) { this.enableLiveThreads = enableLiveThreads; }
        public boolean isEnablePrivateMessages() { return enablePrivateMessages; }
        public void setEnablePrivateMessages(boolean enablePrivateMessages) { this.enablePrivateMessages = enablePrivateMessages; }
        public boolean isEnableSearch() { return enableSearch; }
        public void setEnableSearch(boolean enableSearch) { this.enableSearch = enableSearch; }
        public boolean isEnableVoting() { return enableVoting; }
        public void setEnableVoting(boolean enableVoting) { this.enableVoting = enableVoting; }
        public boolean isEnableAwards() { return enableAwards; }
        public void setEnableAwards(boolean enableAwards) { this.enableAwards = enableAwards; }
        public boolean isEnablePolls() { return enablePolls; }
        public void setEnablePolls(boolean enablePolls) { this.enablePolls = enablePolls; }
        public boolean isEnableCollections() { return enableCollections; }
        public void setEnableCollections(boolean enableCollections) { this.enableCollections = enableCollections; }
        public boolean isEnableCrossposting() { return enableCrossposting; }
        public void setEnableCrossposting(boolean enableCrossposting) { this.enableCrossposting = enableCrossposting; }
        public boolean isEnableScheduledPosts() { return enableScheduledPosts; }
        public void setEnableScheduledPosts(boolean enableScheduledPosts) { this.enableScheduledPosts = enableScheduledPosts; }
        public boolean isEnableAnalytics() { return enableAnalytics; }
        public void setEnableAnalytics(boolean enableAnalytics) { this.enableAnalytics = enableAnalytics; }
    }

        public static class RedditLimits {
        private int maxTitleLength = 300;
        private int maxTextLength = 40000;
        private int maxCommentLength = 10000;
        private int maxSubredditsPerMulti = 100;
        private int maxFlairLength = 64;
        private int maxWikiPageSize = 524288; // 512KB
        private int maxSearchResults = 1000;
        private int maxListingItems = 100;
        private int rateLimitPerMinute = 60;
        private int oauthRateLimitPerMinute = 600;
        private int maxImageSizeMB = 20;
        private int maxVideoSizeMB = 1024;
        private int maxGifSizeMB = 200;
        private int maxPollOptions = 6;
        private int maxPollDurationDays = 7;

        // Getters and Setters for RedditLimits
        public int getMaxTitleLength() { return maxTitleLength; }
        public void setMaxTitleLength(int maxTitleLength) { this.maxTitleLength = maxTitleLength; }
        public int getMaxTextLength() { return maxTextLength; }
        public void setMaxTextLength(int maxTextLength) { this.maxTextLength = maxTextLength; }
        public int getMaxCommentLength() { return maxCommentLength; }
        public void setMaxCommentLength(int maxCommentLength) { this.maxCommentLength = maxCommentLength; }
        public int getMaxSubredditsPerMulti() { return maxSubredditsPerMulti; }
        public void setMaxSubredditsPerMulti(int maxSubredditsPerMulti) { this.maxSubredditsPerMulti = maxSubredditsPerMulti; }
        public int getMaxFlairLength() { return maxFlairLength; }
        public void setMaxFlairLength(int maxFlairLength) { this.maxFlairLength = maxFlairLength; }
        public int getMaxWikiPageSize() { return maxWikiPageSize; }
        public void setMaxWikiPageSize(int maxWikiPageSize) { this.maxWikiPageSize = maxWikiPageSize; }
        public int getMaxSearchResults() { return maxSearchResults; }
        public void setMaxSearchResults(int maxSearchResults) { this.maxSearchResults = maxSearchResults; }
        public int getMaxListingItems() { return maxListingItems; }
        public void setMaxListingItems(int maxListingItems) { this.maxListingItems = maxListingItems; }
        public int getRateLimitPerMinute() { return rateLimitPerMinute; }
        public void setRateLimitPerMinute(int rateLimitPerMinute) { this.rateLimitPerMinute = rateLimitPerMinute; }
        public int getOauthRateLimitPerMinute() { return oauthRateLimitPerMinute; }
        public void setOauthRateLimitPerMinute(int oauthRateLimitPerMinute) { this.oauthRateLimitPerMinute = oauthRateLimitPerMinute; }
        public int getMaxImageSizeMB() { return maxImageSizeMB; }
        public void setMaxImageSizeMB(int maxImageSizeMB) { this.maxImageSizeMB = maxImageSizeMB; }
        public int getMaxVideoSizeMB() { return maxVideoSizeMB; }
        public void setMaxVideoSizeMB(int maxVideoSizeMB) { this.maxVideoSizeMB = maxVideoSizeMB; }
        public int getMaxGifSizeMB() { return maxGifSizeMB; }
        public void setMaxGifSizeMB(int maxGifSizeMB) { this.maxGifSizeMB = maxGifSizeMB; }
        public int getMaxPollOptions() { return maxPollOptions; }
        public void setMaxPollOptions(int maxPollOptions) { this.maxPollOptions = maxPollOptions; }
        public int getMaxPollDurationDays() { return maxPollDurationDays; }
        public void setMaxPollDurationDays(int maxPollDurationDays) { this.maxPollDurationDays = maxPollDurationDays; }
    }

    public static class RedditPollingConfig {
        private boolean enabled = true;
        private List<String> monitoredSubreddits;
        private List<String> moderatedSubreddits;
        private boolean pollSubscribedSubreddits = true;
        private boolean pollUserComments = true;
        private boolean pollCommentReplies = true;

        // Getters and Setters
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public List<String> getMonitoredSubreddits() { return monitoredSubreddits; }
        public void setMonitoredSubreddits(List<String> monitoredSubreddits) { this.monitoredSubreddits = monitoredSubreddits; }
        public List<String> getModeratedSubreddits() { return moderatedSubreddits; }
        public void setModeratedSubreddits(List<String> moderatedSubreddits) { this.moderatedSubreddits = moderatedSubreddits; }
        public boolean isPollSubscribedSubreddits() { return pollSubscribedSubreddits; }
        public void setPollSubscribedSubreddits(boolean pollSubscribedSubreddits) { this.pollSubscribedSubreddits = pollSubscribedSubreddits; }
        public boolean isPollUserComments() { return pollUserComments; }
        public void setPollUserComments(boolean pollUserComments) { this.pollUserComments = pollUserComments; }
        public boolean isPollCommentReplies() { return pollCommentReplies; }
        public void setPollCommentReplies(boolean pollCommentReplies) { this.pollCommentReplies = pollCommentReplies; }
    }

    // Post types
    public enum PostType {
        SELF,       // Text post
        LINK,       // Link post
        IMAGE,      // Image post
        VIDEO,      // Video post
        GALLERY,    // Multiple images
        POLL,       // Poll post
        CROSSPOST    // Crosspost from another subreddit
    }

    // Sorting options
    public enum SortType {
        HOT,
        NEW,
        RISING,
        TOP,
        CONTROVERSIAL,
        BEST,
        GILDED
    }

    // Time filter for top/controversial
    public enum TimeFilter {
        HOUR,
        DAY,
        WEEK,
        MONTH,
        YEAR,
        ALL
    }

    // Vote direction
    public enum VoteDirection {
        UPVOTE(1),
        DOWNVOTE(-1),
        UNVOTE(0);

        private final int value;

        VoteDirection(int value) {
            this.value = value;
        }

        public int getValue() { return value; }
    }

    // Listing type
    public enum ListingType {
        POSTS,
        COMMENTS,
        SUBREDDITS,
        USERS,
        MULTI,
        MIXED
    }

    // Moderation action types
    public enum ModAction {
        APPROVE,
        REMOVE,
        SPAM,
        DISTINGUISH,
        STICKY,
        LOCK,
        NSFW,
        SPOILER,
        CONTEST_MODE,
        IGNORE_REPORTS,
        BAN_USER,
        UNBAN_USER,
        MUTE_USER,
        UNMUTE_USER,
        INVITE_MOD,
        REMOVE_MOD,
        SET_FLAIR
    }

    // User relationship
    public enum UserRelationship {
        FRIEND,
        BLOCKED,
        MODERATOR,
        CONTRIBUTOR,
        BANNED,
        MUTED,
        WIKIBANNED,
        WIKICONTRIBUTOR
    }

    // Subreddit type
    public enum SubredditType {
        PUBLIC,
        PRIVATE,
        RESTRICTED,
        GOLD_RESTRICTED,
        ARCHIVED,
        EMPLOYEES_ONLY
    }

    // MessageDTO type
    public enum MessageType {
        INBOX,
        UNREAD,
        SENT,
        MESSAGES,
        COMMENTS,
        SELFREPLY,
        MENTIONS
    }

    // Award types(common ones)
    public enum AwardType {
        SILVER,
        GOLD,
        PLATINUM,
        ARGENTIUM,
        TERNION,
        WHOLESOME,
        HELPFUL,
        HUGZ,
        ROCKET_LIKE,
        TABLE_SLAP,
        CUSTOM
    }

    // Flair types
    public enum FlairType {
        USER_FLAIR,
        LINK_FLAIR,
        MOD_FLAIR
    }

    // Search syntax
    public enum SearchSyntax {
        TITLE,
        SELFTEXT,
        URL,
        AUTHOR,
        SUBREDDIT,
        FLAIR,
        SITE,
        NSFW,
        SELF,
        BEFORE,
        AFTER,
        SCORE,
        NUM_COMMENTS
    }

    // Report reasons
    public enum ReportReason {
        SPAM,
        PERSONAL_INFO,
        SEXUALIZING_MINORS,
        INVOLUNTARY_PORN,
        HARASSMENT,
        THREATENING_VIOLENCE,
        VOTE_MANIPULATION,
        BREAKING_REDDIT,
        OTHER,
        SITE_REASON_SELECTED,
        RULE_REASON_SELECTED,
        NO_REASON_SELECTED
    }

    // Notification types
    public enum NotificationType {
        POST_REPLY,
        COMMENT_REPLY,
        USERNAME_MENTION,
        PRIVATE_MESSAGE,
        CHAT_REQUEST,
        CHAT_MESSAGE,
        AWARD_RECEIVED,
        UPVOTE_MILESTONE,
        FOLLOWER,
        TRENDING_POST,
        COMMUNITY_RECOMMENDATION,
        BROADCAST_RECOMMENDATION
    }

    // Trophy types
    public enum TrophyType {
        VERIFIED_EMAIL,
        REDDIT_PREMIUM,
        NEW_USER,
        WELL_ROUNDED,
        YEAR_CLUB,
        SEQUENCE_EDITOR,
        BETA_TESTER,
        GILDING,
        EXTRA_LIFE,
        UNDEAD,
        COMBO_LINKER,
        COMBO_COMMENTER,
        BELLWETHER,
        INCITEFUL_COMMENT,
        INCITEFUL_LINK,
        BEST_COMMENT,
        BEST_LINK
    }

    // NSFW levels
    public enum NSFWLevel {
        NONE,
        SOFT,
        HARD
    }

    // Spoiler tags
    public enum SpoilerTag {
        NONE,
        SPOILER,
        NSFW,
        NSFW_AND_SPOILER
    }

    // Webhook events
    public enum WebhookEvent {
        POST_CREATE,
        POST_UPDATE,
        POST_DELETE,
        COMMENT_CREATE,
        COMMENT_UPDATE,
        COMMENT_DELETE,
        USER_JOIN,
        USER_LEAVE,
        MOD_ACTION,
        REPORT_CREATE,
        FLAIR_UPDATE,
        WIKI_UPDATE,
        AWARD_GIVEN
    }

    // Error codes
    public enum RedditErrorCode {
        INVALID_TOKEN(401),
        FORBIDDEN(403),
        NOT_FOUND(404),
        CONFLICT(409),
        RATE_LIMITED(429),
        INVALID_REQUEST(400),
        INTERNAL_ERROR(500),
        SERVICE_UNAVAILABLE(503),
        USER_REQUIRED(403),
        SUBREDDIT_NOEXIST(404),
        SUBREDDIT_NOTALLOWED(403),
        ALREADY_SUBMITTED(409),
        NO_SELF_LINKS(403),
        TOO_LONG(400),
        NO_TEXT(400),
        INVALID_OPTION(400),
        THREAD_LOCKED(403);

        private final int code;

        RedditErrorCode(int code) {
            this.code = code;
        }

        public int getCode() { return code; }
    }
    // Getters and Setters
    public String getUserAgent() {
        return userAgent;
    }
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public RedditFeatures getFeatures() {
        return features;
    }
    public void setFeatures(RedditFeatures features) {
        this.features = features;
    }
    public RedditLimits getLimits() {
        return limits;
    }
    public void setLimits(RedditLimits limits) {
        this.limits = limits;
    }
    // Delegate methods to access feature flags
    public boolean isEnablePostManagement() {
        return features.isEnablePostManagement();
    }
    public void setEnablePostManagement(boolean enablePostManagement) {
        features.setEnablePostManagement(enablePostManagement);
    }
    public boolean isEnableCommentManagement() {
        return features.isEnableCommentManagement();
    }
    public void setEnableCommentManagement(boolean enableCommentManagement) {
        features.setEnableCommentManagement(enableCommentManagement);
    }
    public boolean isEnableSubredditMonitoring() {
        return features.isEnableSubredditMonitoring();
    }
    public void setEnableSubredditMonitoring(boolean enableSubredditMonitoring) {
        features.setEnableSubredditMonitoring(enableSubredditMonitoring);
    }
    public boolean isEnableUserTracking() {
        return features.isEnableUserTracking();
    }
    public void setEnableUserTracking(boolean enableUserTracking) {
        features.setEnableUserTracking(enableUserTracking);
    }
    public boolean isEnableModeration() {
        return features.isEnableModeration();
    }
    public void setEnableModeration(boolean enableModeration) {
        features.setEnableModeration(enableModeration);
    }
    public boolean isEnableWikiManagement() {
        return features.isEnableWikiManagement();
    }
    public void setEnableWikiManagement(boolean enableWikiManagement) {
        features.setEnableWikiManagement(enableWikiManagement);
    }
    public boolean isEnableFlairManagement() {
        return features.isEnableFlairManagement();
    }
    public void setEnableFlairManagement(boolean enableFlairManagement) {
        features.setEnableFlairManagement(enableFlairManagement);
    }
    public boolean isEnableMultireddit() {
        return features.isEnableMultireddit();
    }
    public void setEnableMultireddit(boolean enableMultireddit) {
        features.setEnableMultireddit(enableMultireddit);
    }
    public boolean isEnableLiveThreads() {
        return features.isEnableLiveThreads();
    }
    public void setEnableLiveThreads(boolean enableLiveThreads) {
        features.setEnableLiveThreads(enableLiveThreads);
    }
    public boolean isEnablePrivateMessages() {
        return features.isEnablePrivateMessages();
    }
    public void setEnablePrivateMessages(boolean enablePrivateMessages) {
        features.setEnablePrivateMessages(enablePrivateMessages);
    }
    public boolean isEnableSearch() {
        return features.isEnableSearch();
    }
    public void setEnableSearch(boolean enableSearch) {
        features.setEnableSearch(enableSearch);
    }
    public boolean isEnableVoting() {
        return features.isEnableVoting();
    }
    public void setEnableVoting(boolean enableVoting) {
        features.setEnableVoting(enableVoting);
    }
    public boolean isEnableAwards() {
        return features.isEnableAwards();
    }
    public void setEnableAwards(boolean enableAwards) {
        features.setEnableAwards(enableAwards);
    }
    public boolean isEnablePolls() {
        return features.isEnablePolls();
    }
    public void setEnablePolls(boolean enablePolls) {
        features.setEnablePolls(enablePolls);
    }
    public boolean isEnableCollections() {
        return features.isEnableCollections();
    }
    public void setEnableCollections(boolean enableCollections) {
        features.setEnableCollections(enableCollections);
    }
    public boolean isEnableCrossposting() {
        return features.isEnableCrossposting();
    }
    public void setEnableCrossposting(boolean enableCrossposting) {
        features.setEnableCrossposting(enableCrossposting);
    }
    public boolean isEnableScheduledPosts() {
        return features.isEnableScheduledPosts();
    }
    public void setEnableScheduledPosts(boolean enableScheduledPosts) {
        features.setEnableScheduledPosts(enableScheduledPosts);
    }
    public boolean isEnableAnalytics() {
        return features.isEnableAnalytics();
    }
    public void setEnableAnalytics(boolean enableAnalytics) {
        features.setEnableAnalytics(enableAnalytics);
    }

    // Delegate methods to access limits
    public int getMaxTitleLength() {
        return limits.getMaxTitleLength();
    }
    public void setMaxTitleLength(int maxTitleLength) {
        limits.setMaxTitleLength(maxTitleLength);
    }
    public int getMaxTextLength() {
        return limits.getMaxTextLength();
    }
    public void setMaxTextLength(int maxTextLength) {
        limits.setMaxTextLength(maxTextLength);
    }
    public int getMaxCommentLength() {
        return limits.getMaxCommentLength();
    }
    public void setMaxCommentLength(int maxCommentLength) {
        limits.setMaxCommentLength(maxCommentLength);
    }
    public int getMaxSubredditsPerMulti() {
        return limits.getMaxSubredditsPerMulti();
    }
    public void setMaxSubredditsPerMulti(int maxSubredditsPerMulti) {
        limits.setMaxSubredditsPerMulti(maxSubredditsPerMulti);
    }
    public int getMaxFlairLength() {
        return limits.getMaxFlairLength();
    }
    public void setMaxFlairLength(int maxFlairLength) {
        limits.setMaxFlairLength(maxFlairLength);
    }
    public int getMaxWikiPageSize() {
        return limits.getMaxWikiPageSize();
    }
    public void setMaxWikiPageSize(int maxWikiPageSize) {
        limits.setMaxWikiPageSize(maxWikiPageSize);
    }
    public int getMaxSearchResults() {
        return limits.getMaxSearchResults();
    }
    public void setMaxSearchResults(int maxSearchResults) {
        limits.setMaxSearchResults(maxSearchResults);
    }
    public int getMaxListingItems() {
        return limits.getMaxListingItems();
    }
    public void setMaxListingItems(int maxListingItems) {
        limits.setMaxListingItems(maxListingItems);
    }
    public int getOauthRateLimitPerMinute() {
        return limits.getOauthRateLimitPerMinute();
    }
    public void setOauthRateLimitPerMinute(int oauthRateLimitPerMinute) {
        limits.setOauthRateLimitPerMinute(oauthRateLimitPerMinute);
    }
    public int getMaxImageSizeMB() {
        return limits.getMaxImageSizeMB();
    }
    public void setMaxImageSizeMB(int maxImageSizeMB) {
        limits.setMaxImageSizeMB(maxImageSizeMB);
    }
    public int getMaxVideoSizeMB() {
        return limits.getMaxVideoSizeMB();
    }
    public void setMaxVideoSizeMB(int maxVideoSizeMB) {
        limits.setMaxVideoSizeMB(maxVideoSizeMB);
    }
    public int getMaxGifSizeMB() {
        return limits.getMaxGifSizeMB();
    }
    public void setMaxGifSizeMB(int maxGifSizeMB) {
        limits.setMaxGifSizeMB(maxGifSizeMB);
    }
    public int getMaxPollOptions() {
        return limits.getMaxPollOptions();
    }
    public void setMaxPollOptions(int maxPollOptions) {
        limits.setMaxPollOptions(maxPollOptions);
    }
    public int getMaxPollDurationDays() {
        return limits.getMaxPollDurationDays();
    }
    public void setMaxPollDurationDays(int maxPollDurationDays) {
        limits.setMaxPollDurationDays(maxPollDurationDays);
    }

    public RedditPollingConfig getPollingConfig() {
        return pollingConfig;
    }

    public void setPollingConfig(RedditPollingConfig pollingConfig) {
        this.pollingConfig = pollingConfig;
    }
}
