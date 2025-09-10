package com.integrixs.adapters.social.reddit;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import com.integrixs.adapters.social.base.SocialMediaAdapterConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "integrixs.adapters.reddit")
@EqualsAndHashCode(callSuper = true)
public class RedditApiConfig extends SocialMediaAdapterConfig {
    
    private String clientId;
    private String clientSecret;
    private String userAgent;
    private String username;
    private String password;
    private RedditFeatures features = new RedditFeatures();
    private RedditLimits limits = new RedditLimits();
    
    @Data
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
    }
    
    @Data
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
    }
    
    // Post types
    public enum PostType {
        SELF,        // Text post
        LINK,        // Link post
        IMAGE,       // Image post
        VIDEO,       // Video post
        GALLERY,     // Multiple images
        POLL,        // Poll post
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
    
    // Message type
    public enum MessageType {
        INBOX,
        UNREAD,
        SENT,
        MESSAGES,
        COMMENTS,
        SELFREPLY,
        MENTIONS
    }
    
    // Award types (common ones)
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
}