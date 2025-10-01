package com.integrixs.adapters.social.discord;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import com.integrixs.adapters.social.base.SocialMediaAdapterConfig;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Component
@ConfigurationProperties(prefix = "integrixs.adapters.discord")
public class DiscordApiConfig extends SocialMediaAdapterConfig {

    private String clientId;
    private String clientSecret;
    private String botToken;
    private String publicKey;
    private String guildId; // Optional: specific guild/server to operate in
    private String applicationId;
    private DiscordFeatures features = new DiscordFeatures();
    private DiscordLimits limits = new DiscordLimits();

    // API configuration
    private String apiBaseUrl;
    private String gatewayUrl;
    private String apiVersion;
    private String userAgent;

    // Default values for Discord operations
    private int defaultChannelType;
    private int defaultPrivacyLevel;
    private int defaultAutoArchiveDuration;
    private int defaultThreadType;

    // Polling configuration
    private Long pollingIntervalMs = 30000L;

    // OAuth URLs
    private String authorizationUrl;
    private String tokenUrl;

    // Gateway intent bit positions
    private Map<String, Integer> gatewayIntentBits = new HashMap<>();

    {
        // Initialize default intent bit positions
        gatewayIntentBits.put("GUILDS", 0);
        gatewayIntentBits.put("GUILD_MEMBERS", 1);
        gatewayIntentBits.put("GUILD_MESSAGES", 9);
        gatewayIntentBits.put("DIRECT_MESSAGES", 12);
        gatewayIntentBits.put("MESSAGE_CONTENT", 15);
        gatewayIntentBits.put("GUILD_VOICE_STATES", 7);
        gatewayIntentBits.put("GUILD_SCHEDULED_EVENTS", 16);
    }

    // Missing fields from DiscordFeatures (referenced by getters/setters)
    private boolean enableGuildManagement;
    private boolean enableChannelOperations;
    private boolean enableMessageManagement;
    private boolean enableVoiceSupport;
    private boolean enableSlashCommands;
    private boolean enableWebhooks;
    private boolean enableRoleManagement;
    private boolean enableMemberManagement;
    private boolean enableEmojiManagement;
    private boolean enableEventManagement;
    private boolean enableThreadSupport;
    private boolean enableStageChannels;
    private boolean enableAutoModeration;
    private boolean enableInteractions;
    private boolean enableEmbeds;
    private boolean enableReactions;
    private boolean enableDirectMessages;
    private boolean enableFileUploads;
    private boolean enableVoiceRecording;
    private boolean enableStreamNotifications;

    // Missing fields from DiscordLimits (referenced by getters/setters)
    private int maxMessageLength;
    private int maxEmbedLength;
    private int maxEmbedFields;
    private int maxFileSize;
    private int maxReactionsPerMessageDTO;
    private int maxChannelsPerGuild;
    private int maxRolesPerGuild;
    private int maxEmojisPerGuild;
    private int maxWebhooksPerChannel;
    private int maxInvitesPerGuild;
    private int maxBansPerGuild;
    private int rateLimitPerMinute;
    private int bulkDeleteLimit;
    private int messageHistoryLimit;
    private int guildMemberLimit;

        public static class DiscordFeatures {
        private boolean enableGuildManagement = true;
        private boolean enableChannelOperations = true;
        private boolean enableMessageManagement = true;
        private boolean enableVoiceSupport = true;
        private boolean enableSlashCommands = true;
        private boolean enableWebhooks = true;
        private boolean enableRoleManagement = true;
        private boolean enableMemberManagement = true;
        private boolean enableEmojiManagement = true;
        private boolean enableEventManagement = true;
        private boolean enableThreadSupport = true;
        private boolean enableStageChannels = true;
        private boolean enableAutoModeration = true;
        private boolean enableInteractions = true;
        private boolean enableEmbeds = true;
        private boolean enableReactions = true;
        private boolean enableDirectMessages = true;
        private boolean enableFileUploads = true;
        private boolean enableVoiceRecording = false;
        private boolean enableStreamNotifications = true;
    }

        public static class DiscordLimits {
        private int maxMessageLength = 2000;
        private int maxEmbedLength = 6000;
        private int maxEmbedFields = 25;
        private int maxFileSize = 8388608; // 8MB for free, 50MB for Nitro
        private int maxReactionsPerMessageDTO = 20;
        private int maxChannelsPerGuild = 500;
        private int maxRolesPerGuild = 250;
        private int maxEmojisPerGuild = 50; // Without boosts
        private int maxWebhooksPerChannel = 10;
        private int maxInvitesPerGuild = 1000;
        private int maxBansPerGuild = 1000;
        private int rateLimitPerMinute = 5; // For most endpoints
        private int bulkDeleteLimit = 100; // Messages at once
        private int messageHistoryLimit = 100; // Per request
        private int guildMemberLimit = 250000; // Without verification

        // Getters
        public int getMessageHistoryLimit() {
            return messageHistoryLimit;
        }
        public void setMessageHistoryLimit(int messageHistoryLimit) {
            this.messageHistoryLimit = messageHistoryLimit;
        }
    }

    // Gateway intents
    public enum GatewayIntent {
        GUILDS,
        GUILD_MEMBERS,
        GUILD_BANS,
        GUILD_EMOJIS,
        GUILD_INTEGRATIONS,
        GUILD_WEBHOOKS,
        GUILD_INVITES,
        GUILD_VOICE_STATES,
        GUILD_PRESENCES,
        GUILD_MESSAGES,
        GUILD_MESSAGE_REACTIONS,
        GUILD_MESSAGE_TYPING,
        DIRECT_MESSAGES,
        DIRECT_MESSAGE_REACTIONS,
        DIRECT_MESSAGE_TYPING,
        MESSAGE_CONTENT,
        GUILD_SCHEDULED_EVENTS,
        AUTO_MODERATION_CONFIGURATION,
        AUTO_MODERATION_EXECUTION
    }

    // Channel types
    public enum ChannelType {
        GUILD_TEXT,
        DM,
        GUILD_VOICE,
        GROUP_DM,
        GUILD_CATEGORY,
        GUILD_NEWS,
        GUILD_STORE,
        GUILD_NEWS_THREAD,
        GUILD_PUBLIC_THREAD,
        GUILD_PRIVATE_THREAD,
        GUILD_STAGE_VOICE,
        GUILD_DIRECTORY,
        GUILD_FORUM
    }

    // MessageDTO types
    public enum MessageType {
        DEFAULT,
        RECIPIENT_ADD,
        RECIPIENT_REMOVE,
        CALL,
        CHANNEL_NAME_CHANGE,
        CHANNEL_ICON_CHANGE,
        CHANNEL_PINNED_MESSAGE,
        GUILD_MEMBER_JOIN,
        USER_PREMIUM_GUILD_SUBSCRIPTION,
        USER_PREMIUM_GUILD_SUBSCRIPTION_TIER_1,
        USER_PREMIUM_GUILD_SUBSCRIPTION_TIER_2,
        USER_PREMIUM_GUILD_SUBSCRIPTION_TIER_3,
        CHANNEL_FOLLOW_ADD,
        GUILD_DISCOVERY_DISQUALIFIED,
        GUILD_DISCOVERY_REQUALIFIED,
        GUILD_DISCOVERY_GRACE_PERIOD_INITIAL_WARNING,
        GUILD_DISCOVERY_GRACE_PERIOD_FINAL_WARNING,
        THREAD_CREATED,
        REPLY,
        CHAT_INPUT_COMMAND,
        THREAD_STARTER_MESSAGE,
        GUILD_INVITE_REMINDER,
        CONTEXT_MENU_COMMAND,
        AUTO_MODERATION_ACTION
    }

    // Permission flags
    public enum Permission {
        CREATE_INSTANT_INVITE,
        KICK_MEMBERS,
        BAN_MEMBERS,
        ADMINISTRATOR,
        MANAGE_CHANNELS,
        MANAGE_GUILD,
        ADD_REACTIONS,
        VIEW_AUDIT_LOG,
        PRIORITY_SPEAKER,
        STREAM,
        VIEW_CHANNEL,
        SEND_MESSAGES,
        SEND_TTS_MESSAGES,
        MANAGE_MESSAGES,
        EMBED_LINKS,
        ATTACH_FILES,
        READ_MESSAGE_HISTORY,
        MENTION_EVERYONE,
        USE_EXTERNAL_EMOJIS,
        VIEW_GUILD_INSIGHTS,
        CONNECT,
        SPEAK,
        MUTE_MEMBERS,
        DEAFEN_MEMBERS,
        MOVE_MEMBERS,
        USE_VAD,
        CHANGE_NICKNAME,
        MANAGE_NICKNAMES,
        MANAGE_ROLES,
        MANAGE_WEBHOOKS,
        MANAGE_EMOJIS_AND_STICKERS,
        USE_APPLICATION_COMMANDS,
        REQUEST_TO_SPEAK,
        MANAGE_EVENTS,
        MANAGE_THREADS,
        CREATE_PUBLIC_THREADS,
        CREATE_PRIVATE_THREADS,
        USE_EXTERNAL_STICKERS,
        SEND_MESSAGES_IN_THREADS,
        USE_EMBEDDED_ACTIVITIES,
        MODERATE_MEMBERS
    }

    // Interaction types
    public enum InteractionType {
        PING,
        APPLICATION_COMMAND,
        MESSAGE_COMPONENT,
        APPLICATION_COMMAND_AUTOCOMPLETE,
        MODAL_SUBMIT
    }

    // Component types
    public enum ComponentType {
        ACTION_ROW,
        BUTTON,
        SELECT_MENU,
        TEXT_INPUT
    }

    // Button styles
    public enum ButtonStyle {
        PRIMARY,
        SECONDARY,
        SUCCESS,
        DANGER,
        LINK
    }

    // Event types
    public enum EventType {
        READY,
        RESUMED,
        CHANNEL_CREATE,
        CHANNEL_UPDATE,
        CHANNEL_DELETE,
        CHANNEL_PINS_UPDATE,
        THREAD_CREATE,
        THREAD_UPDATE,
        THREAD_DELETE,
        THREAD_LIST_SYNC,
        THREAD_MEMBER_UPDATE,
        THREAD_MEMBERS_UPDATE,
        GUILD_CREATE,
        GUILD_UPDATE,
        GUILD_DELETE,
        GUILD_BAN_ADD,
        GUILD_BAN_REMOVE,
        GUILD_EMOJIS_UPDATE,
        GUILD_STICKERS_UPDATE,
        GUILD_INTEGRATIONS_UPDATE,
        GUILD_MEMBER_ADD,
        GUILD_MEMBER_REMOVE,
        GUILD_MEMBER_UPDATE,
        GUILD_MEMBERS_CHUNK,
        GUILD_ROLE_CREATE,
        GUILD_ROLE_UPDATE,
        GUILD_ROLE_DELETE,
        GUILD_SCHEDULED_EVENT_CREATE,
        GUILD_SCHEDULED_EVENT_UPDATE,
        GUILD_SCHEDULED_EVENT_DELETE,
        GUILD_SCHEDULED_EVENT_USER_ADD,
        GUILD_SCHEDULED_EVENT_USER_REMOVE,
        INTEGRATION_CREATE,
        INTEGRATION_UPDATE,
        INTEGRATION_DELETE,
        INTERACTION_CREATE,
        INVITE_CREATE,
        INVITE_DELETE,
        MESSAGE_CREATE,
        MESSAGE_UPDATE,
        MESSAGE_DELETE,
        MESSAGE_DELETE_BULK,
        MESSAGE_REACTION_ADD,
        MESSAGE_REACTION_REMOVE,
        MESSAGE_REACTION_REMOVE_ALL,
        MESSAGE_REACTION_REMOVE_EMOJI,
        PRESENCE_UPDATE,
        STAGE_INSTANCE_CREATE,
        STAGE_INSTANCE_DELETE,
        STAGE_INSTANCE_UPDATE,
        TYPING_START,
        USER_UPDATE,
        VOICE_STATE_UPDATE,
        VOICE_SERVER_UPDATE,
        WEBHOOKS_UPDATE
    }

    // Audit log event types
    public enum AuditLogEvent {
        GUILD_UPDATE,
        CHANNEL_CREATE,
        CHANNEL_UPDATE,
        CHANNEL_DELETE,
        CHANNEL_OVERWRITE_CREATE,
        CHANNEL_OVERWRITE_UPDATE,
        CHANNEL_OVERWRITE_DELETE,
        MEMBER_KICK,
        MEMBER_PRUNE,
        MEMBER_BAN_ADD,
        MEMBER_BAN_REMOVE,
        MEMBER_UPDATE,
        MEMBER_ROLE_UPDATE,
        MEMBER_MOVE,
        MEMBER_DISCONNECT,
        BOT_ADD,
        ROLE_CREATE,
        ROLE_UPDATE,
        ROLE_DELETE,
        INVITE_CREATE,
        INVITE_UPDATE,
        INVITE_DELETE,
        WEBHOOK_CREATE,
        WEBHOOK_UPDATE,
        WEBHOOK_DELETE,
        EMOJI_CREATE,
        EMOJI_UPDATE,
        EMOJI_DELETE,
        MESSAGE_DELETE,
        MESSAGE_BULK_DELETE,
        MESSAGE_PIN,
        MESSAGE_UNPIN,
        INTEGRATION_CREATE,
        INTEGRATION_UPDATE,
        INTEGRATION_DELETE,
        STAGE_INSTANCE_CREATE,
        STAGE_INSTANCE_UPDATE,
        STAGE_INSTANCE_DELETE,
        STICKER_CREATE,
        STICKER_UPDATE,
        STICKER_DELETE,
        GUILD_SCHEDULED_EVENT_CREATE,
        GUILD_SCHEDULED_EVENT_UPDATE,
        GUILD_SCHEDULED_EVENT_DELETE,
        THREAD_CREATE,
        THREAD_UPDATE,
        THREAD_DELETE,
        APPLICATION_COMMAND_PERMISSION_UPDATE,
        AUTO_MODERATION_RULE_CREATE,
        AUTO_MODERATION_RULE_UPDATE,
        AUTO_MODERATION_RULE_DELETE,
        AUTO_MODERATION_BLOCK_MESSAGE
    }

    // Voice states
    public enum VoiceState {
        DEAF,
        MUTE,
        SELF_DEAF,
        SELF_MUTE,
        SELF_STREAM,
        SELF_VIDEO,
        SUPPRESS,
        REQUEST_TO_SPEAK_TIMESTAMP
    }

    // Activity types
    public enum ActivityType {
        GAME,
        STREAMING,
        LISTENING,
        WATCHING,
        CUSTOM,
        COMPETING
    }

    // Status types
    public enum StatusType {
        ONLINE,
        DND,
        IDLE,
        INVISIBLE,
        OFFLINE
    }

    // Embed types
    public enum EmbedType {
        RICH,
        IMAGE,
        VIDEO,
        GIFV,
        ARTICLE,
        LINK
    }

    // Error codes
    public enum DiscordErrorCode {
        GENERAL_ERROR(0),
        UNKNOWN_ACCOUNT(10001),
        UNKNOWN_APPLICATION(10002),
        UNKNOWN_CHANNEL(10003),
        UNKNOWN_GUILD(10004),
        UNKNOWN_INTEGRATION(10005),
        UNKNOWN_INVITE(10006),
        UNKNOWN_MEMBER(10007),
        UNKNOWN_MESSAGE(10008),
        UNKNOWN_PERMISSION_OVERWRITE(10009),
        UNKNOWN_PROVIDER(10010),
        UNKNOWN_ROLE(10011),
        UNKNOWN_TOKEN(10012),
        UNKNOWN_USER(10013),
        UNKNOWN_EMOJI(10014),
        UNKNOWN_WEBHOOK(10015),
        UNKNOWN_WEBHOOK_SERVICE(10016),
        UNKNOWN_SESSION(10020),
        UNKNOWN_BAN(10026),
        UNKNOWN_SKU(10027),
        UNKNOWN_STORE_LISTING(10028),
        UNKNOWN_ENTITLEMENT(10029),
        UNKNOWN_BUILD(10030),
        UNKNOWN_LOBBY(10031),
        UNKNOWN_BRANCH(10032),
        UNKNOWN_STORE_DIRECTORY_LAYOUT(10033),
        UNKNOWN_REDISTRIBUTABLE(10036),
        UNKNOWN_GIFT_CODE(10038),
        UNKNOWN_STREAM(10049),
        UNKNOWN_PREMIUM_SERVER_SUBSCRIBE_COOLDOWN(10050),
        UNKNOWN_GUILD_TEMPLATE(10057),
        UNKNOWN_DISCOVERABLE_SERVER_CATEGORY(10059),
        UNKNOWN_STICKER(10060),
        UNKNOWN_INTERACTION(10062),
        UNKNOWN_APPLICATION_COMMAND(10063),
        UNKNOWN_VOICE_STATE(10065),
        UNKNOWN_APPLICATION_COMMAND_PERMISSIONS(10066),
        UNKNOWN_STAGE_INSTANCE(10067),
        UNKNOWN_GUILD_MEMBER_VERIFICATION_FORM(10068),
        UNKNOWN_GUILD_WELCOME_SCREEN(10069),
        UNKNOWN_GUILD_SCHEDULED_EVENT(10070),
        UNKNOWN_GUILD_SCHEDULED_EVENT_USER(10071),
        UNKNOWN_TAG(10087),
        BOTS_CANNOT_USE_ENDPOINT(20001),
        ONLY_BOTS_CAN_USE_ENDPOINT(20002),
        EXPLICIT_CONTENT_SENT_TO_NON_NSFW_CHANNEL(20009),
        APPLICATION_ACTION_UNAUTHORIZED(20012),
        ACTION_SLOW_MODE_RATE_LIMITED(20016),
        ACCOUNT_OWNER_ONLY(20018),
        ANNOUNCEMENT_RATE_LIMIT(20022),
        CHANNEL_WRITE_RATE_LIMIT(20028),
        WRITE_RATE_LIMIT_REACHED(20029),
        WORDS_NOT_ALLOWED(20031),
        GUILD_PREMIUM_SUBSCRIPTION_LEVEL_TOO_LOW(20035),
        MAXIMUM_GUILDS(30001),
        MAXIMUM_FRIENDS(30002),
        MAXIMUM_PINS(30003),
        MAXIMUM_RECIPIENTS(30004),
        MAXIMUM_GUILD_ROLES(30005),
        MAXIMUM_WEBHOOKS(30007),
        MAXIMUM_EMOJIS(30008),
        MAXIMUM_REACTIONS(30010),
        MAXIMUM_CHANNELS(30013),
        MAXIMUM_ATTACHMENTS(30015),
        MAXIMUM_INVITES(30016),
        MAXIMUM_ANIMATED_EMOJIS(30018),
        MAXIMUM_SERVER_MEMBERS(30019),
        MAXIMUM_SERVER_CATEGORIES(30030),
        GUILD_TEMPLATE_EXISTS(30031),
        MAXIMUM_APPLICATION_COMMANDS(30032),
        MAXIMUM_THREAD_PARTICIPANTS(30033),
        MAXIMUM_DAILY_APPLICATION_COMMAND_CREATES(30034),
        MAXIMUM_BANS_FOR_NON_GUILD_MEMBERS(30035),
        MAXIMUM_BAN_FETCHES(30037),
        MAXIMUM_UNCOMPLETED_GUILD_SCHEDULED_EVENTS(30038),
        MAXIMUM_STICKERS(30039),
        MAXIMUM_PRUNE_REQUESTS(30040),
        MAXIMUM_GUILD_WIDGET_SETTINGS_UPDATES(30042),
        MAXIMUM_EDITS_TO_OLD_MESSAGES(30046),
        MAXIMUM_PINNED_THREADS(30047),
        MAXIMUM_FORUM_TAGS(30048),
        BITRATE_TOO_HIGH(30052),
        UNAUTHORIZED(40001),
        ACCOUNT_VERIFICATION_REQUIRED(40002),
        DM_RATE_LIMIT_HIT(40003),
        REQUEST_ENTITY_TOO_LARGE(40005),
        FEATURE_TEMPORARILY_DISABLED(40006),
        USER_BANNED(40007),
        CONNECTION_REVOKED(40012),
        TARGET_USER_NOT_CONNECTED_TO_VOICE(40032),
        MESSAGE_ALREADY_CROSSPOSTED(40033),
        APPLICATION_COMMAND_NAME_EXISTS(40041),
        APPLICATION_INTERACTION_FAILED_TO_SEND(40043),
        CANNOT_SEND_DM(40060),
        INTERACTION_ALREADY_ACKNOWLEDGED(40061),
        TAG_NAME_MUST_BE_UNIQUE(40066),
        SERVICE_RESOURCE_RATE_LIMITED(40067),
        NO_TAGS_AVAILABLE(40068),
        TAG_REQUIRED(40069),
        MISSING_ACCESS(50001),
        INVALID_ACCOUNT_TYPE(50002),
        CANNOT_EXECUTE_DM(50003),
        GUILD_WIDGET_DISABLED(50004),
        CANNOT_EDIT_OTHER_USER_MESSAGE(50005),
        CANNOT_SEND_EMPTY_MESSAGE(50006),
        CANNOT_DM_USER(50007),
        CANNOT_SEND_TO_VOICE_CHANNEL(50008),
        CHANNEL_VERIFICATION_TOO_HIGH(50009),
        OAUTH2_APPLICATION_NO_BOT(50010),
        OAUTH2_APPLICATION_LIMIT(50011),
        INVALID_OAUTH2_STATE(50012),
        MISSING_PERMISSIONS(50013),
        INVALID_TOKEN(50014),
        NOTE_TOO_LONG(50015),
        INVALID_MESSAGE_DELETE_RANGE(50016),
        INVALID_INVITE_CHANNEL(50019),
        CANNOT_ACTION_SYSTEM_MESSAGE(50021),
        CANNOT_ACTION_BOT_CHANNEL(50024),
        INVALID_OAUTH2_TOKEN(50025),
        MISSING_OAUTH2_SCOPE(50026),
        INVALID_WEBHOOK_TOKEN(50027),
        INVALID_ROLE(50028),
        INVALID_RECIPIENTS(50033),
        MESSAGE_TOO_OLD_TO_DELETE(50034),
        INVALID_FORM_BODY(50035),
        INVITE_ACCEPTED_TO_NON_MEMBER_GUILD(50036),
        INVALID_ACTIVITY_ACTION(50039),
        INVALID_API_VERSION(50041),
        FILE_SIZE_EXCEEDED(50045),
        INVALID_FILE_UPLOAD(50046),
        CANNOT_SELF_REDEEM_GIFT(50054),
        INVALID_GUILD(50055),
        INVALID_REQUEST_ORIGIN(50067),
        INVALID_MESSAGE_TYPE(50068),
        PAYMENT_SOURCE_REQUIRED(50070),
        CANNOT_MODIFY_SYSTEM_WEBHOOK(50073),
        CANNOT_DELETE_REQUIRED_CHANNEL(50074),
        INVALID_STICKER_SENT(50081),
        THREAD_OPERATION_INVALID(50083),
        INVALID_THREAD_NOTIFICATION_SETTINGS(50084),
        BEFORE_VALUE_EARLIER_THAN_THREAD(50085),
        COMMUNITY_CHANNELS_MUST_BE_TEXT(50086),
        ENTITY_TYPE_DIFFERENT_FROM_EVENT(50091),
        SERVER_NOT_AVAILABLE_IN_LOCATION(50095),
        SERVER_MONETIZATION_REQUIRED(50097),
        SERVER_BOOSTS_REQUIRED(50101),
        REQUEST_ALREADY_HAS_THREAD(50103),
        THREAD_LOCKED(50104),
        MAXIMUM_ACTIVE_THREADS(50106),
        MAXIMUM_ACTIVE_ANNOUNCEMENT_THREADS(50107),
        INVALID_JSON_LOTTIE(50109),
        UPLOADED_LOTTIE_INVALID(50110),
        STICKER_ANIMATION_DURATION_EXCEEDED(50111),
        CANNOT_UPDATE_ARCHIVED_THREAD(50113),
        TIME_OUT_AND_REMOVE_ALL_ROLES(50117),
        NO_PERMISSION_TO_SEND_STICKER(50600),
        TWO_FACTOR_REQUIRED(60003),
        NO_DM_PERMISSION(60004),
        CANNOT_REPLY_WITHOUT_MESSAGE_HISTORY(60005),
        EXPLICIT_CONTENT_BLOCKED(60006),
        NOT_AUTHORIZED_FOR_APPLICATION(60007),
        SLOWMODE_RATE_LIMITED(60013),
        ACCOUNT_OWNERSHIP_TRANSFER_TEAM_MEMBER(60015),
        CANNOT_RESIZE_BELOW_MAXIMUM(60016),
        CANNOT_MIX_SUBSCRIPTION_AND_NON_SUBSCRIPTION_ROLES(60017),
        CANNOT_CONVERT_BETWEEN_PREMIUM_EMOJI_AND_NORMAL(60018),
        UPLOADED_FILE_NOT_FOUND(60019),
        VOICE_MESSAGE_ADDITIONAL_CONTENT(60020),
        VOICE_MESSAGE_TOO_MANY_AUDIO_ATTACHMENTS(60021),
        VOICE_MESSAGE_AUDIO_DURATION_TOO_LONG(60022),
        CANNOT_SEND_VOICE_MESSAGES(60026),
        USER_MUST_FIRST_BE_VERIFIED(60028),
        YOU_DO_NOT_HAVE_SEND_VOICE_MESSAGE(60031),
        VANITY_URL_EMPLOYEE_ONLY_GUILD_DISABLED(60032),
        VANITY_URL_REQUIREMENTS_NOT_MET(60033),
        REACTION_BLOCKED(90001),
        API_RESOURCE_OVERLOADED(130000),
        STAGE_ALREADY_OPEN(150006),
        MESSAGE_THREAD_ALREADY_CREATED(160004),
        THREAD_ARCHIVED_LOCKED(160005),
        INVALID_THREAD_TYPE(160006),
        INVALID_CLIENT_ID(160007),
        REMOTE_AUTH_SESSION_ACTIVE(170001),
        REMOTE_AUTH_SESSION_NOT_FOUND(170002),
        REMOTE_AUTH_SESSION_EXPIRED(170003),
        REMOTE_AUTH_SESSION_REVOKED(170004),
        REMOTE_AUTH_SESSION_INVALID_AUDIENCE(170005),
        CANNOT_JOIN_VOICE_MORE_THAN_ONCE(180000),
        INVALID_REQUEST(190001);

        private final int code;

        DiscordErrorCode(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }
    @Override
    public String getPlatformName() {
        return "discord";
    }

    @Override
    public String getAuthorizationUrl() {
        return authorizationUrl != null ? authorizationUrl : "https://discord.com/api/oauth2/authorize";
    }

    @Override
    public String getTokenUrl() {
        return tokenUrl != null ? tokenUrl : "https://discord.com/api/oauth2/token";
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
    public String getBotToken() {
        return botToken;
    }
    public void setBotToken(String botToken) {
        this.botToken = botToken;
    }
    public String getPublicKey() {
        return publicKey;
    }
    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
    public String getGuildId() {
        return guildId;
    }
    public void setGuildId(String guildId) {
        this.guildId = guildId;
    }
    public String getApplicationId() {
        return applicationId;
    }
    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }
    public DiscordFeatures getFeatures() {
        return features;
    }
    public void setFeatures(DiscordFeatures features) {
        this.features = features;
    }
    public DiscordLimits getLimits() {
        return limits;
    }
    public void setLimits(DiscordLimits limits) {
        this.limits = limits;
    }
    public boolean isEnableGuildManagement() {
        return enableGuildManagement;
    }
    public void setEnableGuildManagement(boolean enableGuildManagement) {
        this.enableGuildManagement = enableGuildManagement;
    }
    public boolean isEnableChannelOperations() {
        return enableChannelOperations;
    }
    public void setEnableChannelOperations(boolean enableChannelOperations) {
        this.enableChannelOperations = enableChannelOperations;
    }
    public boolean isEnableMessageManagement() {
        return enableMessageManagement;
    }
    public void setEnableMessageManagement(boolean enableMessageManagement) {
        this.enableMessageManagement = enableMessageManagement;
    }
    public boolean isEnableVoiceSupport() {
        return enableVoiceSupport;
    }
    public void setEnableVoiceSupport(boolean enableVoiceSupport) {
        this.enableVoiceSupport = enableVoiceSupport;
    }
    public boolean isEnableSlashCommands() {
        return enableSlashCommands;
    }
    public void setEnableSlashCommands(boolean enableSlashCommands) {
        this.enableSlashCommands = enableSlashCommands;
    }
    public boolean isEnableWebhooks() {
        return enableWebhooks;
    }
    public void setEnableWebhooks(boolean enableWebhooks) {
        this.enableWebhooks = enableWebhooks;
    }
    public boolean isEnableRoleManagement() {
        return enableRoleManagement;
    }
    public void setEnableRoleManagement(boolean enableRoleManagement) {
        this.enableRoleManagement = enableRoleManagement;
    }
    public boolean isEnableMemberManagement() {
        return enableMemberManagement;
    }
    public void setEnableMemberManagement(boolean enableMemberManagement) {
        this.enableMemberManagement = enableMemberManagement;
    }
    public boolean isEnableEmojiManagement() {
        return enableEmojiManagement;
    }
    public void setEnableEmojiManagement(boolean enableEmojiManagement) {
        this.enableEmojiManagement = enableEmojiManagement;
    }
    public boolean isEnableEventManagement() {
        return enableEventManagement;
    }
    public void setEnableEventManagement(boolean enableEventManagement) {
        this.enableEventManagement = enableEventManagement;
    }
    public boolean isEnableThreadSupport() {
        return enableThreadSupport;
    }
    public void setEnableThreadSupport(boolean enableThreadSupport) {
        this.enableThreadSupport = enableThreadSupport;
    }
    public boolean isEnableStageChannels() {
        return enableStageChannels;
    }
    public void setEnableStageChannels(boolean enableStageChannels) {
        this.enableStageChannels = enableStageChannels;
    }
    public boolean isEnableAutoModeration() {
        return enableAutoModeration;
    }
    public void setEnableAutoModeration(boolean enableAutoModeration) {
        this.enableAutoModeration = enableAutoModeration;
    }
    public boolean isEnableInteractions() {
        return enableInteractions;
    }
    public void setEnableInteractions(boolean enableInteractions) {
        this.enableInteractions = enableInteractions;
    }
    public boolean isEnableEmbeds() {
        return enableEmbeds;
    }
    public void setEnableEmbeds(boolean enableEmbeds) {
        this.enableEmbeds = enableEmbeds;
    }
    public boolean isEnableReactions() {
        return enableReactions;
    }
    public void setEnableReactions(boolean enableReactions) {
        this.enableReactions = enableReactions;
    }
    public boolean isEnableDirectMessages() {
        return enableDirectMessages;
    }
    public void setEnableDirectMessages(boolean enableDirectMessages) {
        this.enableDirectMessages = enableDirectMessages;
    }
    public boolean isEnableFileUploads() {
        return enableFileUploads;
    }
    public void setEnableFileUploads(boolean enableFileUploads) {
        this.enableFileUploads = enableFileUploads;
    }
    public boolean isEnableVoiceRecording() {
        return enableVoiceRecording;
    }
    public void setEnableVoiceRecording(boolean enableVoiceRecording) {
        this.enableVoiceRecording = enableVoiceRecording;
    }
    public boolean isEnableStreamNotifications() {
        return enableStreamNotifications;
    }
    public void setEnableStreamNotifications(boolean enableStreamNotifications) {
        this.enableStreamNotifications = enableStreamNotifications;
    }
    public int getMaxMessageLength() {
        return maxMessageLength;
    }
    public void setMaxMessageLength(int maxMessageLength) {
        this.maxMessageLength = maxMessageLength;
    }
    public int getMaxEmbedLength() {
        return maxEmbedLength;
    }
    public void setMaxEmbedLength(int maxEmbedLength) {
        this.maxEmbedLength = maxEmbedLength;
    }
    public int getMaxEmbedFields() {
        return maxEmbedFields;
    }
    public void setMaxEmbedFields(int maxEmbedFields) {
        this.maxEmbedFields = maxEmbedFields;
    }
    public int getMaxFileSize() {
        return maxFileSize;
    }
    public void setMaxFileSize(int maxFileSize) {
        this.maxFileSize = maxFileSize;
    }
    public int getMaxReactionsPerMessageDTO() {
        return maxReactionsPerMessageDTO;
    }
    public void setMaxReactionsPerMessageDTO(int maxReactionsPerMessageDTO) {
        this.maxReactionsPerMessageDTO = maxReactionsPerMessageDTO;
    }
    public int getMaxChannelsPerGuild() {
        return maxChannelsPerGuild;
    }
    public void setMaxChannelsPerGuild(int maxChannelsPerGuild) {
        this.maxChannelsPerGuild = maxChannelsPerGuild;
    }
    public int getMaxRolesPerGuild() {
        return maxRolesPerGuild;
    }
    public void setMaxRolesPerGuild(int maxRolesPerGuild) {
        this.maxRolesPerGuild = maxRolesPerGuild;
    }
    public int getMaxEmojisPerGuild() {
        return maxEmojisPerGuild;
    }
    public void setMaxEmojisPerGuild(int maxEmojisPerGuild) {
        this.maxEmojisPerGuild = maxEmojisPerGuild;
    }
    public int getMaxWebhooksPerChannel() {
        return maxWebhooksPerChannel;
    }
    public void setMaxWebhooksPerChannel(int maxWebhooksPerChannel) {
        this.maxWebhooksPerChannel = maxWebhooksPerChannel;
    }
    public int getMaxInvitesPerGuild() {
        return maxInvitesPerGuild;
    }
    public void setMaxInvitesPerGuild(int maxInvitesPerGuild) {
        this.maxInvitesPerGuild = maxInvitesPerGuild;
    }
    public int getMaxBansPerGuild() {
        return maxBansPerGuild;
    }
    public void setMaxBansPerGuild(int maxBansPerGuild) {
        this.maxBansPerGuild = maxBansPerGuild;
    }
    public Integer getRateLimitPerMinute() {
        return rateLimitPerMinute;
    }
    public void setRateLimitPerMinute(int rateLimitPerMinute) {
        this.rateLimitPerMinute = rateLimitPerMinute;
    }
    public int getBulkDeleteLimit() {
        return bulkDeleteLimit;
    }
    public void setBulkDeleteLimit(int bulkDeleteLimit) {
        this.bulkDeleteLimit = bulkDeleteLimit;
    }
    public int getMessageHistoryLimit() {
        return messageHistoryLimit;
    }
    public void setMessageHistoryLimit(int messageHistoryLimit) {
        this.messageHistoryLimit = messageHistoryLimit;
    }
    public int getGuildMemberLimit() {
        return guildMemberLimit;
    }
    public void setGuildMemberLimit(int guildMemberLimit) {
        this.guildMemberLimit = guildMemberLimit;
    }
    public String getApiBaseUrl() {
        return apiBaseUrl;
    }
    public void setApiBaseUrl(String apiBaseUrl) {
        this.apiBaseUrl = apiBaseUrl;
    }
    public String getGatewayUrl() {
        return gatewayUrl;
    }
    public void setGatewayUrl(String gatewayUrl) {
        this.gatewayUrl = gatewayUrl;
    }
    public Map<String, Integer> getGatewayIntentBits() {
        return gatewayIntentBits;
    }
    public void setGatewayIntentBits(Map<String, Integer> gatewayIntentBits) {
        this.gatewayIntentBits = gatewayIntentBits;
    }
    public String getApiVersion() {
        return apiVersion;
    }
    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }
    public String getUserAgent() {
        return userAgent;
    }
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    public int getDefaultChannelType() {
        return defaultChannelType;
    }
    public void setDefaultChannelType(int defaultChannelType) {
        this.defaultChannelType = defaultChannelType;
    }
    public int getDefaultPrivacyLevel() {
        return defaultPrivacyLevel;
    }
    public void setDefaultPrivacyLevel(int defaultPrivacyLevel) {
        this.defaultPrivacyLevel = defaultPrivacyLevel;
    }
    public int getDefaultAutoArchiveDuration() {
        return defaultAutoArchiveDuration;
    }
    public void setDefaultAutoArchiveDuration(int defaultAutoArchiveDuration) {
        this.defaultAutoArchiveDuration = defaultAutoArchiveDuration;
    }
    public int getDefaultThreadType() {
        return defaultThreadType;
    }
    public void setDefaultThreadType(int defaultThreadType) {
        this.defaultThreadType = defaultThreadType;
    }
    public Long getPollingIntervalMs() {
        return pollingIntervalMs;
    }
    public void setPollingIntervalMs(Long pollingIntervalMs) {
        this.pollingIntervalMs = pollingIntervalMs;
    }

    public void setAuthorizationUrl(String authorizationUrl) {
        this.authorizationUrl = authorizationUrl;
    }

    public void setTokenUrl(String tokenUrl) {
        this.tokenUrl = tokenUrl;
    }
}
