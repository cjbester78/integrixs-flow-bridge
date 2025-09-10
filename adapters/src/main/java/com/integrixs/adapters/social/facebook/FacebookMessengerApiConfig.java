package com.integrixs.adapters.social.facebook;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import com.integrixs.adapters.social.base.SocialMediaAdapterConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "integrixs.adapters.facebook.messenger")
@EqualsAndHashCode(callSuper = true)
public class FacebookMessengerApiConfig extends SocialMediaAdapterConfig {
    
    private String pageId;
    private String pageAccessToken;
    private String appSecret;
    private String verifyToken;
    private FacebookMessengerFeatures features = new FacebookMessengerFeatures();
    private FacebookMessengerLimits limits = new FacebookMessengerLimits();
    
    @Data
    public static class FacebookMessengerFeatures {
        private boolean enableMessaging = true;
        private boolean enableChatbot = true;
        private boolean enablePersistentMenu = true;
        private boolean enableGetStarted = true;
        private boolean enableGreeting = true;
        private boolean enableQuickReplies = true;
        private boolean enableTypingIndicator = true;
        private boolean enableMessageTemplates = true;
        private boolean enableAttachments = true;
        private boolean enableBroadcasting = true;
        private boolean enableCustomerMatching = true;
        private boolean enableHandover = true;
        private boolean enablePersonas = true;
        private boolean enableNaturalLanguageProcessing = true;
        private boolean enableMessageInsights = true;
        private boolean enableMessageTags = true;
        private boolean enableSponsoredMessages = true;
        private boolean enablePrivateReplies = true;
        private boolean enableMessageReactions = true;
        private boolean enableIceBreakers = true;
    }
    
    @Data
    public static class FacebookMessengerLimits {
        private int maxQuickReplies = 13;
        private int maxPersistentMenuItems = 3;
        private int maxMessageLength = 2000;
        private int maxButtonsPerTemplate = 3;
        private int maxElementsPerCarousel = 10;
        private int maxPersonas = 5;
        private int maxIceBreakers = 4;
        private int maxAttachmentSizeMB = 25;
        private int maxBroadcastRecipients = 10000;
        private int rateLimitPerMinute = 200;
        private int messagingWindowHours = 24;
        private int maxTemplatesPerAccount = 250;
    }
    
    // Message types
    public enum MessageType {
        TEXT,
        IMAGE,
        VIDEO,
        AUDIO,
        FILE,
        TEMPLATE,
        QUICK_REPLY,
        BUTTON,
        CAROUSEL,
        MEDIA,
        RECEIPT,
        LOCATION,
        LIST
    }
    
    // Message tags for sending outside 24hr window
    public enum MessageTag {
        CONFIRMED_EVENT_UPDATE,
        POST_PURCHASE_UPDATE,
        ACCOUNT_UPDATE,
        HUMAN_AGENT
    }
    
    // Notification types
    public enum NotificationType {
        REGULAR,
        SILENT_PUSH,
        NO_PUSH
    }
    
    // Sender actions
    public enum SenderAction {
        TYPING_ON,
        TYPING_OFF,
        MARK_SEEN
    }
    
    // Template types
    public enum TemplateType {
        GENERIC,
        BUTTON,
        LIST,
        MEDIA,
        RECEIPT,
        AIRLINE_BOARDINGPASS,
        AIRLINE_CHECKIN,
        AIRLINE_ITINERARY,
        AIRLINE_UPDATE
    }
    
    // Button types
    public enum ButtonType {
        WEB_URL,
        POSTBACK,
        CALL,
        SHARE,
        BUY,
        LOG_IN,
        LOG_OUT,
        GAME_PLAY
    }
    
    // Webhook events
    public enum WebhookEvent {
        MESSAGE,
        MESSAGE_DELIVERED,
        MESSAGE_READ,
        MESSAGE_ECHO,
        POSTBACK,
        REFERRAL,
        OPTIN,
        ACCOUNT_LINKING,
        POLICY_ENFORCEMENT,
        APP_ROLES,
        PASS_THREAD_CONTROL,
        TAKE_THREAD_CONTROL,
        REQUEST_THREAD_CONTROL,
        REACTION,
        HANDOVER
    }
    
    // Quick reply types
    public enum QuickReplyType {
        TEXT,
        LOCATION,
        USER_PHONE_NUMBER,
        USER_EMAIL
    }
    
    // Handover protocols
    public enum HandoverApp {
        PRIMARY_RECEIVER,
        PAGE_INBOX,
        HELPDESK
    }
    
    // Persona properties
    public enum PersonaImageSize {
        SMALL,
        MEDIUM,
        LARGE
    }
    
    // NLP entities
    public enum NLPEntity {
        GREETINGS,
        THANKS,
        BYE,
        SENTIMENT,
        DATETIME,
        LOCATION,
        AMOUNT_OF_MONEY,
        PHONE_NUMBER,
        EMAIL,
        URL
    }
    
    // Messaging types
    public enum MessagingType {
        RESPONSE,
        UPDATE,
        MESSAGE_TAG
    }
    
    // Referral sources
    public enum ReferralSource {
        MESSENGER_CODE,
        DISCOVER_TAB,
        ADS,
        SHORTLINK,
        CUSTOMER_CHAT_PLUGIN
    }
    
    // Thread settings
    public enum ThreadSettingType {
        GREETING,
        GET_STARTED,
        PERSISTENT_MENU,
        WHITELISTED_DOMAINS,
        ACCOUNT_LINKING_URL,
        PAYMENT_SETTINGS,
        TARGET_AUDIENCE,
        ICE_BREAKERS
    }
    
    // Audience types
    public enum AudienceType {
        ALL,
        CUSTOM,
        NONE
    }
    
    // Payment types
    public enum PaymentType {
        FIXED_AMOUNT,
        FLEXIBLE_AMOUNT
    }
    
    // Message insights metrics
    public enum InsightMetric {
        TOTAL_MESSAGING_CONNECTIONS,
        NEW_MESSAGING_CONNECTIONS,
        MESSAGING_BLOCKED_CONVERSATIONS,
        REPORTED_CONVERSATIONS,
        REPORTED_CONVERSATIONS_BY_REPORT_TYPE,
        FEEDBACK_BY_ACTION_TYPE
    }
    
    // Broadcast status
    public enum BroadcastStatus {
        SCHEDULED,
        IN_PROGRESS,
        FINISHED,
        CANCELED,
        FAILED
    }
    
    // Customer match types
    public enum CustomerMatchType {
        PHONE_NUMBER,
        EMAIL,
        APP_USER_ID,
        PAGE_SCOPED_USER_ID
    }
    
    // Error codes
    public enum MessengerErrorCode {
        MESSAGING_DISABLED(10),
        MESSAGE_WINDOW_EXCEEDED(20),
        DAILY_LIMIT_REACHED(4),
        ATTACHMENT_SIZE_EXCEEDED(100),
        INVALID_TEMPLATE(190),
        ACCESS_TOKEN_INVALID(190),
        PERMISSION_DENIED(200),
        RATE_LIMIT_EXCEEDED(613)
        ;
        
        private final int code;
        
        MessengerErrorCode(int code) {
            this.code = code;
        }
        
        public int getCode() { return code; }
    }
}