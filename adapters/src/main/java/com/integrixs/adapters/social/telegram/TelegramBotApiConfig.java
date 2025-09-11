package com.integrixs.adapters.social.telegram;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import com.integrixs.adapters.social.base.SocialMediaAdapterConfig;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "integrixs.adapters.telegram.bot")
public class TelegramBotApiConfig extends SocialMediaAdapterConfig {
    
    private String botToken;
    private String botUsername;
    private String webhookUrl;
    private String webhookPath;
    private String webhookSecret; // For webhook signature verification
    private Long defaultChatId; // Optional: default chat/channel to monitor
    private String apiUrl = "https://api.telegram.org"; // Allow custom API URL for local bot API server
    private TelegramFeatures features = new TelegramFeatures();
    private TelegramLimits limits = new TelegramLimits();
    
        public static class TelegramFeatures {
        private boolean enableMessages = true;
        private boolean enableInlineQueries = true;
        private boolean enableCallbackQueries = true;
        private boolean enableChannelPosts = true;
        private boolean enableGroupManagement = true;
        private boolean enableFileHandling = true;
        private boolean enablePayments = true;
        private boolean enableWebhooks = true;
        private boolean enablePolling = true;
        private boolean enableKeyboards = true;
        private boolean enableCommands = true;
        private boolean enableStickers = true;
        private boolean enableGames = true;
        private boolean enablePolls = true;
        private boolean enablePassport = false;
        private boolean enableBusinessConnection = true;
        private boolean enableWebApps = true;
        private boolean enableInlineMode = true;
        private boolean enableForumSupport = true;
        private boolean enableReactions = true;
        private boolean enableTopics = true;
        private boolean enableBotAPI60 = true; // Features from Bot API 6.0+
        private boolean enableBotAPI70 = true; // Features from Bot API 7.0+
    }
    
        public static class TelegramLimits {
        private int maxMessageLength = 4096; // UTF-8 characters
        private int maxCaptionLength = 1024;
        private int maxInlineResultsPerQuery = 50;
        private int maxCallbackDataLength = 64; // bytes
        private int maxInlineQueryLength = 256;
        private int maxFileSizeMB = 50; // For downloads
        private int maxPhotoSizeMB = 10;
        private int maxVideoSizeMB = 50;
        private int maxAnimationSizeMB = 50;
        private int maxAudioSizeMB = 50;
        private int maxDocumentSizeMB = 50;
        private int maxStickerSizeKB = 512; // Static stickers
        private int maxAnimatedStickerSizeKB = 64;
        private int maxVideoStickerSizeKB = 256;
        private int webhookMaxConnections = 40; // 1-100
        private int rateLimitPerSecond = 30; // Messages per second
        private int rateLimitPerMinute = 20; // Different chats per minute
        private int maxKeyboardButtons = 100;
        private int maxInlineKeyboardButtons = 100;
    }
    
    // Update types
    public enum UpdateType {
        MESSAGE,
        EDITED_MESSAGE,
        CHANNEL_POST,
        EDITED_CHANNEL_POST,
        INLINE_QUERY,
        CHOSEN_INLINE_RESULT,
        CALLBACK_QUERY,
        SHIPPING_QUERY,
        PRE_CHECKOUT_QUERY,
        POLL,
        POLL_ANSWER,
        MY_CHAT_MEMBER,
        CHAT_MEMBER,
        CHAT_JOIN_REQUEST,
        MESSAGE_REACTION,
        MESSAGE_REACTION_COUNT,
        CHAT_BOOST,
        REMOVED_CHAT_BOOST,
        BUSINESS_CONNECTION,
        BUSINESS_MESSAGE,
        EDITED_BUSINESS_MESSAGE,
        DELETED_BUSINESS_MESSAGES
    }
    
    // MessageDTO types
    public enum MessageType {
        TEXT,
        PHOTO,
        VIDEO,
        AUDIO,
        VOICE,
        DOCUMENT,
        ANIMATION,
        STICKER,
        VIDEO_NOTE,
        LOCATION,
        VENUE,
        CONTACT,
        POLL,
        DICE,
        GAME,
        INVOICE,
        SUCCESSFUL_PAYMENT,
        FORUM_TOPIC_CREATED,
        FORUM_TOPIC_CLOSED,
        FORUM_TOPIC_REOPENED,
        FORUM_TOPIC_EDITED,
        GENERAL_FORUM_TOPIC_HIDDEN,
        GENERAL_FORUM_TOPIC_UNHIDDEN,
        GIVEAWAY_CREATED,
        GIVEAWAY,
        GIVEAWAY_WINNERS,
        GIVEAWAY_COMPLETED,
        VIDEO_CHAT_SCHEDULED,
        VIDEO_CHAT_STARTED,
        VIDEO_CHAT_ENDED,
        VIDEO_CHAT_PARTICIPANTS_INVITED,
        WEB_APP_DATA
    }
    
    // Chat types
    public enum ChatType {
        PRIVATE,
        GROUP,
        SUPERGROUP,
        CHANNEL
    }
    
    // Chat member status
    public enum ChatMemberStatus {
        CREATOR,
        ADMINISTRATOR,
        MEMBER,
        RESTRICTED,
        LEFT,
        KICKED
    }
    
    // Parse modes
    public enum ParseMode {
        NONE,
        MARKDOWN,
        MARKDOWNV2,
        HTML
    }
    
    // Keyboard button types
    public enum KeyboardButtonType {
        TEXT,
        REQUEST_CONTACT,
        REQUEST_LOCATION,
        REQUEST_POLL,
        REQUEST_USERS,
        REQUEST_CHAT,
        WEB_APP
    }
    
    // Inline keyboard button types
    public enum InlineKeyboardButtonType {
        URL,
        CALLBACK_DATA,
        WEB_APP,
        LOGIN_URL,
        SWITCH_INLINE_QUERY,
        SWITCH_INLINE_QUERY_CURRENT_CHAT,
        SWITCH_INLINE_QUERY_CHOSEN_CHAT,
        CALLBACK_GAME,
        PAY
    }
    
    // Bot command scope types
    public enum BotCommandScopeType {
        DEFAULT,
        ALL_PRIVATE_CHATS,
        ALL_GROUP_CHATS,
        ALL_CHAT_ADMINISTRATORS,
        CHAT,
        CHAT_ADMINISTRATORS,
        CHAT_MEMBER
    }
    
    // Menu button types
    public enum MenuButtonType {
        COMMANDS,
        WEB_APP,
        DEFAULT
    }
    
    // Media group types
    public enum MediaGroupType {
        PHOTO,
        VIDEO,
        AUDIO,
        DOCUMENT
    }
    
    // Reaction types
    public enum ReactionType {
        EMOJI,
        CUSTOM_EMOJI,
        PAID
    }
    
    // Sticker types
    public enum StickerType {
        REGULAR,
        MASK,
        CUSTOM_EMOJI
    }
    
    // Sticker format
    public enum StickerFormat {
        STATIC,
        ANIMATED,
        VIDEO
    }
    
    // Poll types
    public enum PollType {
        REGULAR,
        QUIZ
    }
    
    // Dice emoji types
    public enum DiceEmoji {
        DICE("🎲"),
        DART("🎯"),
        BASKETBALL("🏀"),
        FOOTBALL("⚽"),
        BOWLING("🎳"),
        SLOT_MACHINE("🎰");
        
        private final String emoji;
        
        DiceEmoji(String emoji) {
            this.emoji = emoji;
        }
        
        public String getEmoji() {
            return emoji;
        }
    }
    
    // Chat action types
    public enum ChatAction {
        TYPING,
        UPLOAD_PHOTO,
        RECORD_VIDEO,
        UPLOAD_VIDEO,
        RECORD_VOICE,
        UPLOAD_VOICE,
        UPLOAD_DOCUMENT,
        CHOOSE_STICKER,
        FIND_LOCATION,
        RECORD_VIDEO_NOTE,
        UPLOAD_VIDEO_NOTE
    }
    
    // Entity types
    public enum MessageEntityType {
        MENTION,
        HASHTAG,
        CASHTAG,
        BOT_COMMAND,
        URL,
        EMAIL,
        PHONE_NUMBER,
        BOLD,
        ITALIC,
        UNDERLINE,
        STRIKETHROUGH,
        SPOILER,
        BLOCKQUOTE,
        EXPANDABLE_BLOCKQUOTE,
        CODE,
        PRE,
        TEXT_LINK,
        TEXT_MENTION,
        CUSTOM_EMOJI
    }
    
    // Chat permissions
    public enum ChatPermission {
        CAN_SEND_MESSAGES,
        CAN_SEND_AUDIOS,
        CAN_SEND_DOCUMENTS,
        CAN_SEND_PHOTOS,
        CAN_SEND_VIDEOS,
        CAN_SEND_VIDEO_NOTES,
        CAN_SEND_VOICE_NOTES,
        CAN_SEND_POLLS,
        CAN_SEND_OTHER_MESSAGES,
        CAN_ADD_WEB_PAGE_PREVIEWS,
        CAN_CHANGE_INFO,
        CAN_INVITE_USERS,
        CAN_PIN_MESSAGES,
        CAN_MANAGE_TOPICS
    }
    
    // Administrator rights
    public enum AdministratorRight {
        CAN_MANAGE_CHAT,
        CAN_DELETE_MESSAGES,
        CAN_MANAGE_VIDEO_CHATS,
        CAN_RESTRICT_MEMBERS,
        CAN_PROMOTE_MEMBERS,
        CAN_CHANGE_INFO,
        CAN_INVITE_USERS,
        CAN_POST_MESSAGES,
        CAN_EDIT_MESSAGES,
        CAN_PIN_MESSAGES,
        CAN_POST_STORIES,
        CAN_EDIT_STORIES,
        CAN_DELETE_STORIES,
        CAN_MANAGE_TOPICS
    }
    
    // Inline query result types
    public enum InlineQueryResultType {
        ARTICLE,
        PHOTO,
        GIF,
        MPEG4_GIF,
        VIDEO,
        AUDIO,
        VOICE,
        DOCUMENT,
        LOCATION,
        VENUE,
        CONTACT,
        GAME,
        STICKER,
        CACHED_PHOTO,
        CACHED_GIF,
        CACHED_MPEG4_GIF,
        CACHED_STICKER,
        CACHED_DOCUMENT,
        CACHED_VIDEO,
        CACHED_VOICE,
        CACHED_AUDIO
    }
    
    // Input media types
    public enum InputMediaType {
        PHOTO,
        VIDEO,
        ANIMATION,
        AUDIO,
        DOCUMENT
    }
    
    // Webhook info status
    public enum WebhookStatus {
        NOT_SET,
        ACTIVE,
        ERROR,
        PENDING_UPDATE
    }
    
    // Error codes
    public enum TelegramErrorCode {
        // General errors
        BAD_REQUEST(400, "Bad Request"),
        UNAUTHORIZED(401, "Unauthorized"),
        FORBIDDEN(403, "Forbidden"),
        NOT_FOUND(404, "Not Found"),
        CONFLICT(409, "Conflict"),
        TOO_MANY_REQUESTS(429, "Too Many Requests"),
        
        // Telegram specific errors
        MESSAGE_NOT_MODIFIED(400, "Bad Request: message is not modified"),
        MESSAGE_TO_DELETE_NOT_FOUND(400, "Bad Request: message to delete not found"),
        MESSAGE_TOO_LONG(400, "Bad Request: message is too long"),
        MESSAGE_CANT_BE_EDITED(400, "Bad Request: message can't be edited"),
        MESSAGE_CANT_BE_DELETED(400, "Bad Request: message can't be deleted"),
        
        // Bot errors
        BOT_BLOCKED_BY_USER(403, "Forbidden: bot was blocked by the user"),
        BOT_KICKED_FROM_CHAT(403, "Forbidden: bot was kicked from the group chat"),
        BOT_NOT_IN_CHAT(403, "Forbidden: bot is not a member of the chat"),
        BOT_CANT_INIT_CONVERSATION(403, "Forbidden: bot can't initiate conversation with a user"),
        
        // Chat errors
        CHAT_NOT_FOUND(400, "Bad Request: chat not found"),
        CHAT_ADMIN_REQUIRED(400, "Bad Request: need administrator rights in the chat"),
        NOT_ENOUGH_RIGHTS(400, "Bad Request: not enough rights"),
        PARTICIPANT_ID_INVALID(400, "Bad Request: participant_id invalid"),
        
        // File errors
        FILE_TOO_LARGE(413, "Request Entity Too Large"),
        FILE_ID_INVALID(400, "Bad Request: invalid file_id"),
        
        // Query errors
        QUERY_ID_INVALID(400, "Bad Request: query is too old and response timeout expired"),
        RESULT_ID_DUPLICATE(400, "Bad Request: result_id is duplicated"),
        
        // Webhook errors
        WEBHOOK_REQUIRE_HTTPS(400, "Bad Request: webhook URL must be HTTPS"),
        BAD_WEBHOOK_PORT(400, "Bad Request: bad webhook port"),
        BAD_WEBHOOK_ADDR_INFO(400, "Bad Request: bad webhook: IP address is reserved"),
        WEBHOOK_URL_NOT_VALID(400, "Bad Request: invalid webhook URL"),
        
        // Payment errors
        PAYMENT_PROVIDER_INVALID(400, "Bad Request: payment provider invalid"),
        CURRENCY_TOTAL_AMOUNT_INVALID(400, "Bad Request: currency_total_amount invalid");
        
        private final int code;
        private final String message;
        
        TelegramErrorCode(int code, String message) {
            this.code = code;
            this.message = message;
        }
        
        public int getCode() {
            return code;
        }
        
        public String getMessage() {
            return message;
        }
    }
    // Getters and Setters
    public String getBotToken() {
        return botToken;
    }
    public void setBotToken(String botToken) {
        this.botToken = botToken;
    }
    public String getBotUsername() {
        return botUsername;
    }
    public void setBotUsername(String botUsername) {
        this.botUsername = botUsername;
    }
    public String getWebhookUrl() {
        return webhookUrl;
    }
    public void setWebhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }
    public String getWebhookPath() {
        return webhookPath;
    }
    public void setWebhookPath(String webhookPath) {
        this.webhookPath = webhookPath;
    }
    public String getWebhookSecret() {
        return webhookSecret;
    }
    public void setWebhookSecret(String webhookSecret) {
        this.webhookSecret = webhookSecret;
    }
    public Long getDefaultChatId() {
        return defaultChatId;
    }
    public void setDefaultChatId(Long defaultChatId) {
        this.defaultChatId = defaultChatId;
    }
    public String getApiUrl() {
        return apiUrl;
    }
    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }
    public TelegramFeatures getFeatures() {
        return features;
    }
    public void setFeatures(TelegramFeatures features) {
        this.features = features;
    }
    public TelegramLimits getLimits() {
        return limits;
    }
    public void setLimits(TelegramLimits limits) {
        this.limits = limits;
    }
    public boolean isEnableMessages() {
        return enableMessages;
    }
    public void setEnableMessages(boolean enableMessages) {
        this.enableMessages = enableMessages;
    }
    public boolean isEnableInlineQueries() {
        return enableInlineQueries;
    }
    public void setEnableInlineQueries(boolean enableInlineQueries) {
        this.enableInlineQueries = enableInlineQueries;
    }
    public boolean isEnableCallbackQueries() {
        return enableCallbackQueries;
    }
    public void setEnableCallbackQueries(boolean enableCallbackQueries) {
        this.enableCallbackQueries = enableCallbackQueries;
    }
    public boolean isEnableChannelPosts() {
        return enableChannelPosts;
    }
    public void setEnableChannelPosts(boolean enableChannelPosts) {
        this.enableChannelPosts = enableChannelPosts;
    }
    public boolean isEnableGroupManagement() {
        return enableGroupManagement;
    }
    public void setEnableGroupManagement(boolean enableGroupManagement) {
        this.enableGroupManagement = enableGroupManagement;
    }
    public boolean isEnableFileHandling() {
        return enableFileHandling;
    }
    public void setEnableFileHandling(boolean enableFileHandling) {
        this.enableFileHandling = enableFileHandling;
    }
    public boolean isEnablePayments() {
        return enablePayments;
    }
    public void setEnablePayments(boolean enablePayments) {
        this.enablePayments = enablePayments;
    }
    public boolean isEnableWebhooks() {
        return enableWebhooks;
    }
    public void setEnableWebhooks(boolean enableWebhooks) {
        this.enableWebhooks = enableWebhooks;
    }
    public boolean isEnablePolling() {
        return enablePolling;
    }
    public void setEnablePolling(boolean enablePolling) {
        this.enablePolling = enablePolling;
    }
    public boolean isEnableKeyboards() {
        return enableKeyboards;
    }
    public void setEnableKeyboards(boolean enableKeyboards) {
        this.enableKeyboards = enableKeyboards;
    }
    public boolean isEnableCommands() {
        return enableCommands;
    }
    public void setEnableCommands(boolean enableCommands) {
        this.enableCommands = enableCommands;
    }
    public boolean isEnableStickers() {
        return enableStickers;
    }
    public void setEnableStickers(boolean enableStickers) {
        this.enableStickers = enableStickers;
    }
    public boolean isEnableGames() {
        return enableGames;
    }
    public void setEnableGames(boolean enableGames) {
        this.enableGames = enableGames;
    }
    public boolean isEnablePolls() {
        return enablePolls;
    }
    public void setEnablePolls(boolean enablePolls) {
        this.enablePolls = enablePolls;
    }
    public boolean isEnablePassport() {
        return enablePassport;
    }
    public void setEnablePassport(boolean enablePassport) {
        this.enablePassport = enablePassport;
    }
    public boolean isEnableBusinessConnection() {
        return enableBusinessConnection;
    }
    public void setEnableBusinessConnection(boolean enableBusinessConnection) {
        this.enableBusinessConnection = enableBusinessConnection;
    }
    public boolean isEnableWebApps() {
        return enableWebApps;
    }
    public void setEnableWebApps(boolean enableWebApps) {
        this.enableWebApps = enableWebApps;
    }
    public boolean isEnableInlineMode() {
        return enableInlineMode;
    }
    public void setEnableInlineMode(boolean enableInlineMode) {
        this.enableInlineMode = enableInlineMode;
    }
    public boolean isEnableForumSupport() {
        return enableForumSupport;
    }
    public void setEnableForumSupport(boolean enableForumSupport) {
        this.enableForumSupport = enableForumSupport;
    }
    public boolean isEnableReactions() {
        return enableReactions;
    }
    public void setEnableReactions(boolean enableReactions) {
        this.enableReactions = enableReactions;
    }
    public boolean isEnableTopics() {
        return enableTopics;
    }
    public void setEnableTopics(boolean enableTopics) {
        this.enableTopics = enableTopics;
    }
    public boolean isEnableBotAPI60() {
        return enableBotAPI60;
    }
    public void setEnableBotAPI60(boolean enableBotAPI60) {
        this.enableBotAPI60 = enableBotAPI60;
    }
    public boolean isEnableBotAPI70() {
        return enableBotAPI70;
    }
    public void setEnableBotAPI70(boolean enableBotAPI70) {
        this.enableBotAPI70 = enableBotAPI70;
    }
    public int getMaxMessageLength() {
        return maxMessageLength;
    }
    public void setMaxMessageLength(int maxMessageLength) {
        this.maxMessageLength = maxMessageLength;
    }
    public int getMaxCaptionLength() {
        return maxCaptionLength;
    }
    public void setMaxCaptionLength(int maxCaptionLength) {
        this.maxCaptionLength = maxCaptionLength;
    }
    public int getMaxInlineResultsPerQuery() {
        return maxInlineResultsPerQuery;
    }
    public void setMaxInlineResultsPerQuery(int maxInlineResultsPerQuery) {
        this.maxInlineResultsPerQuery = maxInlineResultsPerQuery;
    }
    public int getMaxCallbackDataLength() {
        return maxCallbackDataLength;
    }
    public void setMaxCallbackDataLength(int maxCallbackDataLength) {
        this.maxCallbackDataLength = maxCallbackDataLength;
    }
    public int getMaxInlineQueryLength() {
        return maxInlineQueryLength;
    }
    public void setMaxInlineQueryLength(int maxInlineQueryLength) {
        this.maxInlineQueryLength = maxInlineQueryLength;
    }
    public int getMaxFileSizeMB() {
        return maxFileSizeMB;
    }
    public void setMaxFileSizeMB(int maxFileSizeMB) {
        this.maxFileSizeMB = maxFileSizeMB;
    }
    public int getMaxPhotoSizeMB() {
        return maxPhotoSizeMB;
    }
    public void setMaxPhotoSizeMB(int maxPhotoSizeMB) {
        this.maxPhotoSizeMB = maxPhotoSizeMB;
    }
    public int getMaxVideoSizeMB() {
        return maxVideoSizeMB;
    }
    public void setMaxVideoSizeMB(int maxVideoSizeMB) {
        this.maxVideoSizeMB = maxVideoSizeMB;
    }
    public int getMaxAnimationSizeMB() {
        return maxAnimationSizeMB;
    }
    public void setMaxAnimationSizeMB(int maxAnimationSizeMB) {
        this.maxAnimationSizeMB = maxAnimationSizeMB;
    }
    public int getMaxAudioSizeMB() {
        return maxAudioSizeMB;
    }
    public void setMaxAudioSizeMB(int maxAudioSizeMB) {
        this.maxAudioSizeMB = maxAudioSizeMB;
    }
    public int getMaxDocumentSizeMB() {
        return maxDocumentSizeMB;
    }
    public void setMaxDocumentSizeMB(int maxDocumentSizeMB) {
        this.maxDocumentSizeMB = maxDocumentSizeMB;
    }
    public int getMaxStickerSizeKB() {
        return maxStickerSizeKB;
    }
    public void setMaxStickerSizeKB(int maxStickerSizeKB) {
        this.maxStickerSizeKB = maxStickerSizeKB;
    }
    public int getMaxAnimatedStickerSizeKB() {
        return maxAnimatedStickerSizeKB;
    }
    public void setMaxAnimatedStickerSizeKB(int maxAnimatedStickerSizeKB) {
        this.maxAnimatedStickerSizeKB = maxAnimatedStickerSizeKB;
    }
    public int getMaxVideoStickerSizeKB() {
        return maxVideoStickerSizeKB;
    }
    public void setMaxVideoStickerSizeKB(int maxVideoStickerSizeKB) {
        this.maxVideoStickerSizeKB = maxVideoStickerSizeKB;
    }
    public int getWebhookMaxConnections() {
        return webhookMaxConnections;
    }
    public void setWebhookMaxConnections(int webhookMaxConnections) {
        this.webhookMaxConnections = webhookMaxConnections;
    }
    public int getRateLimitPerSecond() {
        return rateLimitPerSecond;
    }
    public void setRateLimitPerSecond(int rateLimitPerSecond) {
        this.rateLimitPerSecond = rateLimitPerSecond;
    }
    public int getRateLimitPerMinute() {
        return rateLimitPerMinute;
    }
    public void setRateLimitPerMinute(int rateLimitPerMinute) {
        this.rateLimitPerMinute = rateLimitPerMinute;
    }
    public int getMaxKeyboardButtons() {
        return maxKeyboardButtons;
    }
    public void setMaxKeyboardButtons(int maxKeyboardButtons) {
        this.maxKeyboardButtons = maxKeyboardButtons;
    }
    public int getMaxInlineKeyboardButtons() {
        return maxInlineKeyboardButtons;
    }
    public void setMaxInlineKeyboardButtons(int maxInlineKeyboardButtons) {
        this.maxInlineKeyboardButtons = maxInlineKeyboardButtons;
    }
}