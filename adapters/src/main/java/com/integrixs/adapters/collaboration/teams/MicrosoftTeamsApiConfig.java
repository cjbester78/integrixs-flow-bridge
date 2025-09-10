package com.integrixs.adapters.collaboration.teams;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import com.integrixs.adapters.social.base.SocialMediaAdapterConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "integrixs.adapters.teams")
@EqualsAndHashCode(callSuper = true)
public class MicrosoftTeamsApiConfig extends SocialMediaAdapterConfig {
    
    private String tenantId;
    private String clientId;
    private String clientSecret;
    private String botId;
    private String botName;
    private String webhookUrl;
    private String notificationUrl;
    private String graphApiUrl = "https://graph.microsoft.com/v1.0";
    private String botFrameworkUrl = "https://smba.trafficmanager.net/teams";
    private TeamsFeatures features = new TeamsFeatures();
    private TeamsLimits limits = new TeamsLimits();
    private List<String> scopes; // Microsoft Graph API scopes
    
    @Data
    public static class TeamsFeatures {
        private boolean enableMessaging = true;
        private boolean enableChannels = true;
        private boolean enableTeams = true;
        private boolean enableMeetings = true;
        private boolean enableCalls = true;
        private boolean enableFiles = true;
        private boolean enableTabs = true;
        private boolean enableBots = true;
        private boolean enableCards = true;
        private boolean enableConnectors = true;
        private boolean enableWebhooks = true;
        private boolean enableProactiveMessaging = true;
        private boolean enableAdaptiveCards = true;
        private boolean enableMessageExtensions = true;
        private boolean enableActivityFeed = true;
        private boolean enableShifts = true;
        private boolean enablePlanner = true;
        private boolean enableOneNote = true;
        private boolean enableSharePoint = true;
        private boolean enablePowerApps = true;
        private boolean enablePowerAutomate = true;
        private boolean enableYammer = false;
        private boolean enableStream = true;
        private boolean enableForms = true;
        private boolean enableWhiteboard = true;
        private boolean enableLists = true;
        private boolean enableApprovals = true;
        private boolean enableBookings = true;
        private boolean enablePolls = true;
        private boolean enablePraise = true;
    }
    
    @Data
    public static class TeamsLimits {
        private int maxMessageLength = 28000; // characters
        private int maxCardSize = 25; // KB for adaptive cards
        private int maxAttachmentsPerMessage = 10;
        private int maxTabsPerChannel = 30;
        private int maxPrivateChannels = 30;
        private int maxChannelsPerTeam = 200;
        private int maxMembersPerTeam = 25000;
        private int maxTeamsPerUser = 250;
        private int maxMeetingDuration = 24; // hours
        private int maxMeetingParticipants = 1000;
        private int maxFileSizeMB = 250; // for uploads
        private int maxBotMessagesPerSecond = 8; // per conversation
        private int maxBotMessagesPerMinute = 60; // per conversation
        private int maxBotConversationsPerSecond = 15;
        private int maxCardActions = 6; // per card
        private int maxSuggestedActions = 3;
        private int maxMessageExtensionResults = 25;
        private int rateLimitPerSecond = 30; // Graph API
        private int rateLimitPerMinute = 500; // Graph API
        private int burstLimit = 100; // Graph API
        private int maxBatchRequests = 20; // Graph API batch
        private int webhookExpiryHours = 72; // Change notifications
    }
    
    // Microsoft Graph API scopes
    public enum GraphScope {
        // Teams and channels
        TEAM_CREATE("Team.Create"),
        TEAMWORK_MIGRATE("TeamworkAppSettings.ReadWrite.All"),
        CHANNEL_BASIC_READ("Channel.ReadBasic.All"),
        CHANNEL_READ_ALL("ChannelSettings.Read.All"),
        CHANNEL_READ_WRITE("ChannelSettings.ReadWrite.All"),
        CHANNEL_CREATE("Channel.Create"),
        CHANNEL_DELETE("Channel.Delete.All"),
        CHANNEL_MESSAGE_READ("ChannelMessage.Read.All"),
        CHANNEL_MESSAGE_SEND("ChannelMessage.Send"),
        CHANNEL_MESSAGE_EDIT("ChannelMessage.Edit"),
        TEAM_SETTINGS_READ("TeamSettings.Read.All"),
        TEAM_SETTINGS_READ_WRITE("TeamSettings.ReadWrite.All"),
        TEAM_MEMBER_READ("TeamMember.Read.All"),
        TEAM_MEMBER_READ_WRITE("TeamMember.ReadWrite.All"),
        
        // Chat and messaging
        CHAT_CREATE("Chat.Create"),
        CHAT_READ("Chat.Read"),
        CHAT_READ_ALL("Chat.Read.All"),
        CHAT_READ_WRITE("Chat.ReadWrite"),
        CHAT_READ_WRITE_ALL("Chat.ReadWrite.All"),
        CHAT_MESSAGE_READ("ChatMessage.Read"),
        CHAT_MESSAGE_READ_ALL("ChatMessage.Read.All"),
        CHAT_MESSAGE_SEND("ChatMessage.Send"),
        CHAT_MEMBER_READ("ChatMember.Read"),
        CHAT_MEMBER_READ_ALL("ChatMember.Read.All"),
        CHAT_MEMBER_READ_WRITE("ChatMember.ReadWrite"),
        CHAT_MEMBER_READ_WRITE_ALL("ChatMember.ReadWrite.All"),
        
        // Meetings and calls
        ONLINE_MEETINGS_READ("OnlineMeetings.Read"),
        ONLINE_MEETINGS_READ_ALL("OnlineMeetings.Read.All"),
        ONLINE_MEETINGS_READ_WRITE("OnlineMeetings.ReadWrite"),
        ONLINE_MEETINGS_READ_WRITE_ALL("OnlineMeetings.ReadWrite.All"),
        ONLINE_MEETING_ARTIFACT_READ("OnlineMeetingArtifact.Read.All"),
        CALLS_INITIATE("Calls.Initiate.All"),
        CALLS_ACCESS_MEDIA("Calls.AccessMedia.All"),
        CALLS_JOIN_GROUP_CALL("Calls.JoinGroupCall.All"),
        CALLS_JOIN_GROUP_CALL_AS_GUEST("Calls.JoinGroupCallAsGuest.All"),
        CALL_RECORDS_READ("CallRecords.Read.All"),
        
        // Users and groups
        USER_READ("User.Read"),
        USER_READ_ALL("User.Read.All"),
        USER_READ_BASIC_ALL("User.ReadBasic.All"),
        GROUP_READ_ALL("Group.Read.All"),
        GROUP_READ_WRITE_ALL("Group.ReadWrite.All"),
        DIRECTORY_READ_ALL("Directory.Read.All"),
        
        // Files and sites
        FILES_READ("Files.Read"),
        FILES_READ_ALL("Files.Read.All"),
        FILES_READ_WRITE("Files.ReadWrite"),
        FILES_READ_WRITE_ALL("Files.ReadWrite.All"),
        SITES_READ_ALL("Sites.Read.All"),
        SITES_READ_WRITE_ALL("Sites.ReadWrite.All"),
        SITES_MANAGE_ALL("Sites.Manage.All"),
        
        // Applications
        APPLICATION_READ_ALL("Application.Read.All"),
        APP_CATALOG_READ_ALL("AppCatalog.Read.All"),
        APP_CATALOG_SUBMIT("AppCatalog.Submit"),
        
        // Presence
        PRESENCE_READ("Presence.Read"),
        PRESENCE_READ_ALL("Presence.Read.All"),
        
        // Notifications
        NOTIFICATIONS_READ_WRITE_CREATED_BY_APP("Notifications.ReadWrite.CreatedByApp"),
        
        // Other
        CALENDAR_READ("Calendar.Read"),
        CALENDAR_READ_WRITE("Calendar.ReadWrite"),
        MAIL_READ("Mail.Read"),
        MAIL_SEND("Mail.Send"),
        TASKS_READ_WRITE("Tasks.ReadWrite"),
        NOTES_READ("Notes.Read"),
        NOTES_READ_ALL("Notes.Read.All");
        
        private final String scope;
        
        GraphScope(String scope) {
            this.scope = scope;
        }
        
        public String getScope() {
            return scope;
        }
    }
    
    // Card types for rich messaging
    public enum CardType {
        ADAPTIVE_CARD("adaptiveCard"),
        HERO_CARD("heroCard"),
        THUMBNAIL_CARD("thumbnailCard"),
        RECEIPT_CARD("receiptCard"),
        SIGNIN_CARD("signinCard"),
        LIST_CARD("listCard"),
        O365_CONNECTOR_CARD("o365ConnectorCard"),
        MESSAGE_BACK_CARD("messageBackCard");
        
        private final String type;
        
        CardType(String type) {
            this.type = type;
        }
        
        public String getType() {
            return type;
        }
    }
    
    // Activity types
    public enum ActivityType {
        MESSAGE("message"),
        MESSAGE_UPDATE("messageUpdate"),
        MESSAGE_DELETE("messageDelete"),
        MESSAGE_REACTION("messageReaction"),
        CONVERSATION_UPDATE("conversationUpdate"),
        CONTACT_RELATION_UPDATE("contactRelationUpdate"),
        TYPING("typing"),
        INSTALLATION_UPDATE("installationUpdate"),
        END_OF_CONVERSATION("endOfConversation"),
        EVENT("event"),
        INVOKE("invoke"),
        DELETE_USER_DATA("deleteUserData"),
        HANDOFF("handoff"),
        SUGGESTION("suggestion"),
        TRACE("trace"),
        COMMAND("command");
        
        private final String type;
        
        ActivityType(String type) {
            this.type = type;
        }
        
        public String getType() {
            return type;
        }
    }
    
    // Channel types
    public enum ChannelType {
        REGULAR("regular"),
        PRIVATE("private"),
        SHARED("shared");
        
        private final String type;
        
        ChannelType(String type) {
            this.type = type;
        }
        
        public String getType() {
            return type;
        }
    }
    
    // Meeting types
    public enum MeetingType {
        SCHEDULED("scheduled"),
        INSTANT("instant"),
        RECURRING("recurring"),
        BROADCAST("broadcast"),
        MEET_NOW("meetNow");
        
        private final String type;
        
        MeetingType(String type) {
            this.type = type;
        }
        
        public String getType() {
            return type;
        }
    }
    
    // Message importance
    public enum MessageImportance {
        NORMAL("normal"),
        HIGH("high"),
        URGENT("urgent");
        
        private final String importance;
        
        MessageImportance(String importance) {
            this.importance = importance;
        }
        
        public String getImportance() {
            return importance;
        }
    }
    
    // Mention types
    public enum MentionType {
        USER("person"),
        CHANNEL("channel"),
        TEAM("team"),
        BOT("bot");
        
        private final String type;
        
        MentionType(String type) {
            this.type = type;
        }
        
        public String getType() {
            return type;
        }
    }
    
    // Tab entity types
    public enum TabEntityType {
        WEBSITE("websiteTab"),
        WORD("wordTab"),
        EXCEL("excelTab"),
        POWERPOINT("powerpointTab"),
        PDF("pdfTab"),
        ONENOTE("onenoteTab"),
        PLANNER("plannerTab"),
        SHAREPOINT("sharepointTab"),
        STREAM("microsoftStreamTab"),
        FORMS("formsTab"),
        WIKI("wikiTab"),
        POWER_BI("powerBITab"),
        CUSTOM("customTab");
        
        private final String type;
        
        TabEntityType(String type) {
            this.type = type;
        }
        
        public String getType() {
            return type;
        }
    }
    
    // Teams app distribution methods
    public enum AppDistributionMethod {
        STORE("store"),
        ORGANIZATION("organization"),
        SIDELOADED("sideloaded"),
        TEAMS_APP_CATALOG("teamsAppCatalog");
        
        private final String method;
        
        AppDistributionMethod(String method) {
            this.method = method;
        }
        
        public String getMethod() {
            return method;
        }
    }
    
    // Error codes
    public enum TeamsErrorCode {
        // Authentication errors
        INVALID_AUTHENTICATION("InvalidAuthentication", "Invalid authentication token"),
        AUTHENTICATION_FAILED("AuthenticationFailed", "Authentication failed"),
        TOKEN_EXPIRED("TokenExpired", "Authentication token has expired"),
        INSUFFICIENT_PRIVILEGES("InsufficientPrivileges", "The user does not have sufficient privileges"),
        
        // Resource errors
        RESOURCE_NOT_FOUND("ResourceNotFound", "The requested resource does not exist"),
        TEAM_NOT_FOUND("TeamNotFound", "Team not found"),
        CHANNEL_NOT_FOUND("ChannelNotFound", "Channel not found"),
        CHAT_NOT_FOUND("ChatNotFound", "Chat not found"),
        MESSAGE_NOT_FOUND("MessageNotFound", "Message not found"),
        USER_NOT_FOUND("UserNotFound", "User not found"),
        
        // Permission errors
        FORBIDDEN("Forbidden", "The user does not have permission to perform this action"),
        ACCESS_DENIED("AccessDenied", "Access denied"),
        NOT_ALLOWED("NotAllowed", "Operation not allowed"),
        
        // Rate limiting
        TOO_MANY_REQUESTS("TooManyRequests", "Too many requests"),
        RATE_LIMIT_EXCEEDED("RateLimitExceeded", "Rate limit exceeded"),
        QUOTA_EXCEEDED("QuotaExceeded", "Quota exceeded"),
        
        // Validation errors
        INVALID_REQUEST("InvalidRequest", "The request is malformed or invalid"),
        INVALID_PARAMETER("InvalidParameter", "Invalid parameter value"),
        MISSING_REQUIRED_PARAMETER("MissingRequiredParameter", "Required parameter is missing"),
        INVALID_RANGE("InvalidRange", "The specified range is invalid"),
        
        // Operation errors
        OPERATION_NOT_SUPPORTED("OperationNotSupported", "Operation not supported"),
        METHOD_NOT_ALLOWED("MethodNotAllowed", "Method not allowed"),
        CONFLICT("Conflict", "Conflict occurred"),
        PRECONDITION_FAILED("PreconditionFailed", "Precondition failed"),
        
        // Teams specific errors
        TEAM_ARCHIVED("TeamArchived", "Team is archived"),
        CHANNEL_MODERATION_ENABLED("ChannelModerationEnabled", "Channel moderation is enabled"),
        BOT_NOT_IN_CONVERSATION("BotNotInConversation", "Bot is not part of the conversation"),
        APP_NOT_INSTALLED("AppNotInstalled", "App is not installed"),
        MEETING_NOT_FOUND("MeetingNotFound", "Meeting not found"),
        INVALID_MEETING_TIME("InvalidMeetingTime", "Invalid meeting time"),
        
        // Service errors
        SERVICE_UNAVAILABLE("ServiceUnavailable", "Service is temporarily unavailable"),
        INTERNAL_SERVER_ERROR("InternalServerError", "An internal server error occurred"),
        GATEWAY_TIMEOUT("GatewayTimeout", "Gateway timeout"),
        
        // Graph API specific
        INVALID_FILTER("InvalidFilter", "Invalid filter clause"),
        INVALID_QUERY_OPTION("InvalidQueryOption", "Invalid query option"),
        UNSUPPORTED_QUERY("UnsupportedQuery", "The query specified is not supported"),
        
        // Other errors
        UNKNOWN_ERROR("UnknownError", "An unknown error occurred");
        
        private final String code;
        private final String message;
        
        TeamsErrorCode(String code, String message) {
            this.code = code;
            this.message = message;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getMessage() {
            return message;
        }
    }
}