package com.integrixs.adapters.collaboration.teams;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import com.integrixs.adapters.social.base.SocialMediaAdapterConfig;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "integrix.adapters.teams")
public class MicrosoftTeamsApiConfig extends SocialMediaAdapterConfig {

    private String tenantId;
    private String clientId;
    private String clientSecret;
    private String botId;
    private String botName;
    private String webhookUrl;
    private String notificationUrl;
    private String graphApiUrl;
    private String botFrameworkUrl;
    private TeamsFeatures features = new TeamsFeatures();
    private TeamsLimits limits = new TeamsLimits();
    private List<String> scopes; // Microsoft Graph API scopes

    // OAuth URLs
    private String authorizationUrlBase;
    private String tokenUrlBase;

        public static class TeamsFeatures {
        public boolean enableMessaging;
        public boolean enableChannels;
        public boolean enableTeams;
        public boolean enableMeetings;
        public boolean enableCalls;
        public boolean enableFiles;
        public boolean enableTabs;
        public boolean enableBots;
        public boolean enableCards;
        public boolean enableConnectors;
        public boolean enableWebhooks;
        public boolean enableProactiveMessaging;
        public boolean enableAdaptiveCards;
        public boolean enableMessageExtensions;
        public boolean enableActivityFeed;
        public boolean enableShifts;
        public boolean enablePlanner;
        public boolean enableOneNote;
        public boolean enableSharePoint;
        public boolean enablePowerApps;
        public boolean enablePowerAutomate;
        public boolean enableYammer;
        public boolean enableStream;
        public boolean enableForms;
        public boolean enableWhiteboard;
        public boolean enableLists;
        public boolean enableApprovals;
        public boolean enableBookings;
        public boolean enablePolls;
        public boolean enablePraise;

        // Getter methods
        public boolean isEnableMessaging() { return enableMessaging; }
        public boolean isEnableChannels() { return enableChannels; }
        public boolean isEnableTeams() { return enableTeams; }
        public boolean isEnableMeetings() { return enableMeetings; }
        public boolean isEnableCalls() { return enableCalls; }
        public boolean isEnableFiles() { return enableFiles; }
        public boolean isEnableTabs() { return enableTabs; }
        public boolean isEnableBots() { return enableBots; }
        public boolean isEnableCards() { return enableCards; }
        public boolean isEnableConnectors() { return enableConnectors; }
        public boolean isEnableWebhooks() { return enableWebhooks; }
        public boolean isEnableProactiveMessaging() { return enableProactiveMessaging; }
        public boolean isEnableAdaptiveCards() { return enableAdaptiveCards; }
        public boolean isEnableMessageExtensions() { return enableMessageExtensions; }
        public boolean isEnableActivityFeed() { return enableActivityFeed; }
        public boolean isEnableShifts() { return enableShifts; }
        public boolean isEnablePlanner() { return enablePlanner; }
        public boolean isEnableOneNote() { return enableOneNote; }
        public boolean isEnableSharePoint() { return enableSharePoint; }
        public boolean isEnablePowerApps() { return enablePowerApps; }
        public boolean isEnablePowerAutomate() { return enablePowerAutomate; }
        public boolean isEnableYammer() { return enableYammer; }
        public boolean isEnableStream() { return enableStream; }
        public boolean isEnableForms() { return enableForms; }
        public boolean isEnableWhiteboard() { return enableWhiteboard; }
        public boolean isEnableLists() { return enableLists; }
        public boolean isEnableApprovals() { return enableApprovals; }
        public boolean isEnableBookings() { return enableBookings; }
        public boolean isEnablePolls() { return enablePolls; }
        public boolean isEnablePraise() { return enablePraise; }
    }

        public static class TeamsLimits {
        public int maxMessageLength;
        public int maxCardSize;
        public int maxAttachmentsPerMessage;
        public int maxTabsPerChannel;
        public int maxPrivateChannels;
        public int maxChannelsPerTeam;
        public int maxMembersPerTeam;
        public int maxTeamsPerUser;
        public int maxMeetingDuration;
        public int maxMeetingParticipants;
        public int maxFileSizeMB;
        public int maxBotMessagesPerSecond;
        public int maxBotMessagesPerMinute;
        public int maxBotConversationsPerSecond;
        public int maxCardActions;
        public int maxSuggestedActions;
        public int maxMessageExtensionResults;
        public int rateLimitPerSecond;
        public int rateLimitPerMinute;
        public int burstLimit;
        public int maxBatchRequests;
        public int webhookExpiryHours;

        // Getter methods
        public int getWebhookExpiryHours() { return webhookExpiryHours; }
        public int getMaxFileSizeMb() { return maxFileSizeMB; }
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
    // Getters and Setters
    public String getTenantId() {
        return tenantId;
    }
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
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
    public String getBotId() {
        return botId;
    }
    public void setBotId(String botId) {
        this.botId = botId;
    }
    public String getBotName() {
        return botName;
    }
    public void setBotName(String botName) {
        this.botName = botName;
    }
    public String getWebhookUrl() {
        return webhookUrl;
    }
    public void setWebhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }
    public String getNotificationUrl() {
        return notificationUrl;
    }
    public void setNotificationUrl(String notificationUrl) {
        this.notificationUrl = notificationUrl;
    }
    public String getGraphApiUrl() {
        return graphApiUrl;
    }
    public void setGraphApiUrl(String graphApiUrl) {
        this.graphApiUrl = graphApiUrl;
    }
    public String getBotFrameworkUrl() {
        return botFrameworkUrl;
    }
    public void setBotFrameworkUrl(String botFrameworkUrl) {
        this.botFrameworkUrl = botFrameworkUrl;
    }
    public TeamsFeatures getFeatures() {
        return features;
    }
    public void setFeatures(TeamsFeatures features) {
        this.features = features;
    }
    public TeamsLimits getLimits() {
        return limits;
    }
    public void setLimits(TeamsLimits limits) {
        this.limits = limits;
    }
    public List<String> getScopes() {
        return scopes;
    }
    public void setScopes(List<String> scopes) {
        this.scopes = scopes;
    }

    public void setAuthorizationUrlBase(String authorizationUrlBase) {
        this.authorizationUrlBase = authorizationUrlBase;
    }

    public void setTokenUrlBase(String tokenUrlBase) {
        this.tokenUrlBase = tokenUrlBase;
    }
    // Implement abstract methods from SocialMediaAdapterConfig
    @Override
    public String getAuthorizationUrl() {
        String baseUrl = authorizationUrlBase != null ? authorizationUrlBase : "https://login.microsoftonline.com";
        return String.format("%s/%s/oauth2/v2.0/authorize", baseUrl, tenantId);
    }

    @Override
    public String getTokenUrl() {
        String baseUrl = tokenUrlBase != null ? tokenUrlBase : "https://login.microsoftonline.com";
        return String.format("%s/%s/oauth2/v2.0/token", baseUrl, tenantId);
    }

    @Override
    public String getPlatformName() {
        return "teams";
    }

    public boolean isEnableMessaging() {
        return features.enableMessaging;
    }
    public void setEnableMessaging(boolean enableMessaging) {
        features.enableMessaging = enableMessaging;
    }
    public boolean isEnableChannels() {
        return features.enableChannels;
    }
    public void setEnableChannels(boolean enableChannels) {
        features.enableChannels = enableChannels;
    }
    public boolean isEnableTeams() {
        return features.enableTeams;
    }
    public void setEnableTeams(boolean enableTeams) {
        features.enableTeams = enableTeams;
    }
    public boolean isEnableMeetings() {
        return features.enableMeetings;
    }
    public void setEnableMeetings(boolean enableMeetings) {
        features.enableMeetings = enableMeetings;
    }
    public boolean isEnableCalls() {
        return features.enableCalls;
    }
    public void setEnableCalls(boolean enableCalls) {
        features.enableCalls = enableCalls;
    }
    public boolean isEnableFiles() {
        return features.enableFiles;
    }
    public void setEnableFiles(boolean enableFiles) {
        features.enableFiles = enableFiles;
    }
    public boolean isEnableTabs() {
        return features.enableTabs;
    }
    public void setEnableTabs(boolean enableTabs) {
        features.enableTabs = enableTabs;
    }
    public boolean isEnableBots() {
        return features.enableBots;
    }
    public void setEnableBots(boolean enableBots) {
        features.enableBots = enableBots;
    }
    public boolean isEnableCards() {
        return features.enableCards;
    }
    public void setEnableCards(boolean enableCards) {
        features.enableCards = enableCards;
    }
    public boolean isEnableConnectors() {
        return features.enableConnectors;
    }
    public void setEnableConnectors(boolean enableConnectors) {
        features.enableConnectors = enableConnectors;
    }
    public boolean isEnableWebhooks() {
        return features.enableWebhooks;
    }
    public void setEnableWebhooks(boolean enableWebhooks) {
        features.enableWebhooks = enableWebhooks;
    }
    public boolean isEnableProactiveMessaging() {
        return features.enableProactiveMessaging;
    }
    public void setEnableProactiveMessaging(boolean enableProactiveMessaging) {
        features.enableProactiveMessaging = enableProactiveMessaging;
    }
    public boolean isEnableAdaptiveCards() {
        return features.enableAdaptiveCards;
    }
    public void setEnableAdaptiveCards(boolean enableAdaptiveCards) {
        features.enableAdaptiveCards = enableAdaptiveCards;
    }
    public boolean isEnableMessageExtensions() {
        return features.enableMessageExtensions;
    }
    public void setEnableMessageExtensions(boolean enableMessageExtensions) {
        features.enableMessageExtensions = enableMessageExtensions;
    }
    public boolean isEnableActivityFeed() {
        return features.enableActivityFeed;
    }
    public void setEnableActivityFeed(boolean enableActivityFeed) {
        features.enableActivityFeed = enableActivityFeed;
    }
    public boolean isEnableShifts() {
        return features.enableShifts;
    }
    public void setEnableShifts(boolean enableShifts) {
        features.enableShifts = enableShifts;
    }
    public boolean isEnablePlanner() {
        return features.enablePlanner;
    }
    public void setEnablePlanner(boolean enablePlanner) {
        features.enablePlanner = enablePlanner;
    }
    public boolean isEnableOneNote() {
        return features.enableOneNote;
    }
    public void setEnableOneNote(boolean enableOneNote) {
        features.enableOneNote = enableOneNote;
    }
    public boolean isEnableSharePoint() {
        return features.enableSharePoint;
    }
    public void setEnableSharePoint(boolean enableSharePoint) {
        features.enableSharePoint = enableSharePoint;
    }
    public boolean isEnablePowerApps() {
        return features.enablePowerApps;
    }
    public void setEnablePowerApps(boolean enablePowerApps) {
        features.enablePowerApps = enablePowerApps;
    }
    public boolean isEnablePowerAutomate() {
        return features.enablePowerAutomate;
    }
    public void setEnablePowerAutomate(boolean enablePowerAutomate) {
        features.enablePowerAutomate = enablePowerAutomate;
    }
    public boolean isEnableYammer() {
        return features.enableYammer;
    }
    public void setEnableYammer(boolean enableYammer) {
        features.enableYammer = enableYammer;
    }
    public boolean isEnableStream() {
        return features.enableStream;
    }
    public void setEnableStream(boolean enableStream) {
        features.enableStream = enableStream;
    }
    public boolean isEnableForms() {
        return features.enableForms;
    }
    public void setEnableForms(boolean enableForms) {
        features.enableForms = enableForms;
    }
    public boolean isEnableWhiteboard() {
        return features.enableWhiteboard;
    }
    public void setEnableWhiteboard(boolean enableWhiteboard) {
        features.enableWhiteboard = enableWhiteboard;
    }
    public boolean isEnableLists() {
        return features.enableLists;
    }
    public void setEnableLists(boolean enableLists) {
        features.enableLists = enableLists;
    }
    public boolean isEnableApprovals() {
        return features.enableApprovals;
    }
    public void setEnableApprovals(boolean enableApprovals) {
        features.enableApprovals = enableApprovals;
    }
    public boolean isEnableBookings() {
        return features.enableBookings;
    }
    public void setEnableBookings(boolean enableBookings) {
        features.enableBookings = enableBookings;
    }
    public boolean isEnablePolls() {
        return features.enablePolls;
    }
    public void setEnablePolls(boolean enablePolls) {
        features.enablePolls = enablePolls;
    }
    public boolean isEnablePraise() {
        return features.enablePraise;
    }
    public void setEnablePraise(boolean enablePraise) {
        features.enablePraise = enablePraise;
    }
    public int getMaxMessageLength() {
        return limits.maxMessageLength;
    }
    public void setMaxMessageLength(int maxMessageLength) {
        limits.maxMessageLength = maxMessageLength;
    }
    public int getMaxCardSize() {
        return limits.maxCardSize;
    }
    public void setMaxCardSize(int maxCardSize) {
        limits.maxCardSize = maxCardSize;
    }
    public int getMaxAttachmentsPerMessage() {
        return limits.maxAttachmentsPerMessage;
    }
    public void setMaxAttachmentsPerMessage(int maxAttachmentsPerMessage) {
        limits.maxAttachmentsPerMessage = maxAttachmentsPerMessage;
    }
    public int getMaxTabsPerChannel() {
        return limits.maxTabsPerChannel;
    }
    public void setMaxTabsPerChannel(int maxTabsPerChannel) {
        limits.maxTabsPerChannel = maxTabsPerChannel;
    }
    public int getMaxPrivateChannels() {
        return limits.maxPrivateChannels;
    }
    public void setMaxPrivateChannels(int maxPrivateChannels) {
        limits.maxPrivateChannels = maxPrivateChannels;
    }
    public int getMaxChannelsPerTeam() {
        return limits.maxChannelsPerTeam;
    }
    public void setMaxChannelsPerTeam(int maxChannelsPerTeam) {
        limits.maxChannelsPerTeam = maxChannelsPerTeam;
    }
    public int getMaxMembersPerTeam() {
        return limits.maxMembersPerTeam;
    }
    public void setMaxMembersPerTeam(int maxMembersPerTeam) {
        limits.maxMembersPerTeam = maxMembersPerTeam;
    }
    public int getMaxTeamsPerUser() {
        return limits.maxTeamsPerUser;
    }
    public void setMaxTeamsPerUser(int maxTeamsPerUser) {
        limits.maxTeamsPerUser = maxTeamsPerUser;
    }
    public int getMaxMeetingDuration() {
        return limits.maxMeetingDuration;
    }
    public void setMaxMeetingDuration(int maxMeetingDuration) {
        limits.maxMeetingDuration = maxMeetingDuration;
    }
    public int getMaxMeetingParticipants() {
        return limits.maxMeetingParticipants;
    }
    public void setMaxMeetingParticipants(int maxMeetingParticipants) {
        limits.maxMeetingParticipants = maxMeetingParticipants;
    }
    public int getMaxFileSizeMB() {
        return limits.maxFileSizeMB;
    }
    public void setMaxFileSizeMB(int maxFileSizeMB) {
        limits.maxFileSizeMB = maxFileSizeMB;
    }
    public int getMaxBotMessagesPerSecond() {
        return limits.maxBotMessagesPerSecond;
    }
    public void setMaxBotMessagesPerSecond(int maxBotMessagesPerSecond) {
        limits.maxBotMessagesPerSecond = maxBotMessagesPerSecond;
    }
    public int getMaxBotMessagesPerMinute() {
        return limits.maxBotMessagesPerMinute;
    }
    public void setMaxBotMessagesPerMinute(int maxBotMessagesPerMinute) {
        limits.maxBotMessagesPerMinute = maxBotMessagesPerMinute;
    }
    public int getMaxBotConversationsPerSecond() {
        return limits.maxBotConversationsPerSecond;
    }
    public void setMaxBotConversationsPerSecond(int maxBotConversationsPerSecond) {
        limits.maxBotConversationsPerSecond = maxBotConversationsPerSecond;
    }
    public int getMaxCardActions() {
        return limits.maxCardActions;
    }
    public void setMaxCardActions(int maxCardActions) {
        limits.maxCardActions = maxCardActions;
    }
    public int getMaxSuggestedActions() {
        return limits.maxSuggestedActions;
    }
    public void setMaxSuggestedActions(int maxSuggestedActions) {
        limits.maxSuggestedActions = maxSuggestedActions;
    }
    public int getMaxMessageExtensionResults() {
        return limits.maxMessageExtensionResults;
    }
    public void setMaxMessageExtensionResults(int maxMessageExtensionResults) {
        limits.maxMessageExtensionResults = maxMessageExtensionResults;
    }
    public int getRateLimitPerSecond() {
        return limits.rateLimitPerSecond;
    }
    public void setRateLimitPerSecond(int rateLimitPerSecond) {
        limits.rateLimitPerSecond = rateLimitPerSecond;
    }
    @Override
    public Integer getRateLimitPerMinute() {
        return limits.rateLimitPerMinute;
    }
    @Override
    public void setRateLimitPerMinute(Integer rateLimitPerMinute) {
        limits.rateLimitPerMinute = rateLimitPerMinute != null ? rateLimitPerMinute : 0;
    }
    public int getBurstLimit() {
        return limits.burstLimit;
    }
    public void setBurstLimit(int burstLimit) {
        limits.burstLimit = burstLimit;
    }
    public int getMaxBatchRequests() {
        return limits.maxBatchRequests;
    }
    public void setMaxBatchRequests(int maxBatchRequests) {
        limits.maxBatchRequests = maxBatchRequests;
    }
    public int getWebhookExpiryHours() {
        return limits.webhookExpiryHours;
    }
    public void setWebhookExpiryHours(int webhookExpiryHours) {
        limits.webhookExpiryHours = webhookExpiryHours;
    }
}
