package com.integrixs.adapters.messaging.sms;

import com.integrixs.adapters.config.BaseAdapterConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

@Data
@EqualsAndHashCode(callSuper = true)
@Configuration
@ConfigurationProperties(prefix = "integrixs.adapters.sms")
public class SMSConfig extends BaseAdapterConfig {
    
    // Provider configuration
    private SMSProvider provider = SMSProvider.TWILIO;
    
    // Common authentication
    private String accountId;
    private String authToken;
    private String apiKey;
    private String apiSecret;
    
    // Sender configuration
    private String defaultSenderNumber;
    private String senderName;
    private List<String> senderNumbers = new ArrayList<>();
    
    // Message settings
    private MessageType defaultMessageType = MessageType.SMS;
    private int maxMessageLength = 160;
    private boolean enableUnicode = true;
    private boolean enableConcatenation = true;
    private int maxConcatenatedParts = 10;
    
    // Delivery settings
    private boolean requestDeliveryReceipt = true;
    private String callbackUrl;
    private int messageValidityPeriod = 48; // hours
    private DeliveryPriority priority = DeliveryPriority.NORMAL;
    
    // Rate limiting
    private int messagesPerSecond = 10;
    private int messagesPerMinute = 100;
    private int messagesPerHour = 1000;
    private int messagesPerDay = 10000;
    
    // Retry configuration
    private int maxRetries = 3;
    private long retryDelay = 5000; // milliseconds
    private boolean exponentialBackoff = true;
    
    // Provider-specific endpoints
    private String apiEndpoint;
    private String apiVersion;
    
    // Regional settings
    private String defaultCountryCode = "+1";
    private String preferredRoute = "standard";
    private List<String> blockedCountries = new ArrayList<>();
    private List<String> allowedCountries = new ArrayList<>();
    
    // Content filtering
    private boolean enableContentFiltering = true;
    private List<String> blockedKeywords = new ArrayList<>();
    private boolean enableSpamDetection = true;
    
    // Number validation
    private boolean validateNumbers = true;
    private boolean enableNumberLookup = false;
    private boolean rejectLandlines = false;
    private boolean rejectVoip = false;
    
    // Templates
    private Map<String, MessageTemplate> messageTemplates = new HashMap<>();
    
    // Opt-out management
    private boolean enableOptOutManagement = true;
    private List<String> optOutKeywords = List.of("STOP", "UNSUBSCRIBE", "CANCEL", "END", "QUIT");
    private String optOutConfirmationMessage = "You have been unsubscribed. Reply START to resubscribe.";
    
    // Analytics
    private boolean enableAnalytics = true;
    private boolean trackLinks = false;
    private String linkTrackingDomain;
    
    public enum SMSProvider {
        TWILIO("Twilio", "api.twilio.com"),
        VONAGE("Vonage/Nexmo", "api.nexmo.com"),
        AWS_SNS("AWS SNS", "sns.amazonaws.com"),
        MESSAGEBIRD("MessageBird", "rest.messagebird.com"),
        CLICKATELL("Clickatell", "platform.clickatell.com"),
        TEXTLOCAL("TextLocal", "api.textlocal.in"),
        PLIVO("Plivo", "api.plivo.com"),
        SINCH("Sinch", "sms.api.sinch.com"),
        INFOBIP("Infobip", "api.infobip.com"),
        AFRICA_TALKING("Africa's Talking", "api.africastalking.com"),
        BANDWIDTH("Bandwidth", "messaging.bandwidth.com"),
        TELNYX("Telnyx", "api.telnyx.com"),
        SIGNALWIRE("SignalWire", "api.signalwire.com"),
        CUSTOM("Custom Provider", null);
        
        private final String displayName;
        private final String defaultEndpoint;
        
        SMSProvider(String displayName, String defaultEndpoint) {
            this.displayName = displayName;
            this.defaultEndpoint = defaultEndpoint;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getDefaultEndpoint() {
            return defaultEndpoint;
        }
    }
    
    public enum MessageType {
        SMS("sms"),
        MMS("mms"),
        RCS("rcs"),
        WHATSAPP("whatsapp"),
        FLASH("flash");
        
        private final String type;
        
        MessageType(String type) {
            this.type = type;
        }
        
        public String getType() {
            return type;
        }
    }
    
    public enum DeliveryPriority {
        LOW(0),
        NORMAL(1),
        HIGH(2),
        URGENT(3);
        
        private final int level;
        
        DeliveryPriority(int level) {
            this.level = level;
        }
        
        public int getLevel() {
            return level;
        }
    }
    
    // Message template
    @Data
    public static class MessageTemplate {
        private String id;
        private String name;
        private String content;
        private Map<String, String> variables = new HashMap<>();
        private boolean approved = false;
        private String language = "en";
        private MessageType type = MessageType.SMS;
    }
    
    // Provider-specific configurations
    private TwilioConfig twilioConfig = new TwilioConfig();
    private VonageConfig vonageConfig = new VonageConfig();
    private AwsSnsConfig awsSnsConfig = new AwsSnsConfig();
    private MessageBirdConfig messageBirdConfig = new MessageBirdConfig();
    private InfobipConfig infobipConfig = new InfobipConfig();
    
    @Data
    public static class TwilioConfig {
        private String accountSid;
        private String authToken;
        private String messagingServiceSid;
        private boolean useMessagingService = false;
        private String region = "us1";
        private boolean enableEdgeLocations = false;
        private List<String> edgeLocations = new ArrayList<>();
        private boolean enableStickySender = false;
        private boolean enableSmartEncoding = true;
        private boolean enableValidityPeriod = true;
        private boolean enableShortenUrls = false;
        private String statusCallbackUrl;
    }
    
    @Data
    public static class VonageConfig {
        private String apiKey;
        private String apiSecret;
        private String signatureSecret;
        private String privateKey;
        private boolean enableSignatureValidation = true;
        private String defaultType = "text";
        private boolean enableDlr = true;
        private String dlrUrl;
        private int dlrMask = 7;
        private String clientRef;
        private boolean enableFlashMessage = false;
        private String protocolId;
    }
    
    @Data
    public static class AwsSnsConfig {
        private String region = "us-east-1";
        private String accessKeyId;
        private String secretAccessKey;
        private String roleArn;
        private String snsTopicArn;
        private boolean useTopic = false;
        private Map<String, String> messageAttributes = new HashMap<>();
        private String senderIdPoolName;
        private boolean enableNumberPooling = false;
        private String originationNumber;
        private String registeredKeyword;
    }
    
    @Data
    public static class MessageBirdConfig {
        private String accessKey;
        private String signingKey;
        private boolean enableSigning = false;
        private String dataEncoding = "auto";
        private String gateway;
        private Map<String, Integer> gatewayMapping = new HashMap<>();
        private String reportUrl;
        private int validity;
        private String reference;
        private boolean enableHlrLookup = false;
    }
    
    @Data
    public static class InfobipConfig {
        private String apiKey;
        private String apiKeyPrefix = "App";
        private String baseUrl = "https://api.infobip.com";
        private boolean enableDeliveryReports = true;
        private String notifyUrl;
        private String notifyContentType = "application/json";
        private boolean intermediateReport = false;
        private String callbackData;
        private boolean flash = false;
        private String transliteration = "NON_UNICODE";
        private String languageCode = "NONE";
        private boolean enableUrlShortening = false;
        private boolean trackClicks = false;
        private String trackingUrl;
        private boolean includeSmsCountInResponse = true;
    }
    
    // Features configuration
    private Features features = new Features();
    
    @Data
    public static class Features {
        private boolean enable2WayMessaging = false;
        private boolean enableKeywordProcessing = false;
        private boolean enableAutoResponse = false;
        private boolean enableScheduling = false;
        private boolean enableBulkMessaging = true;
        private boolean enablePersonalization = true;
        private boolean enableGeofencing = false;
        private boolean enableA2PCompliance = true;
        private boolean enableNumberPooling = false;
        private boolean enableFailover = true;
        private boolean enableLoadBalancing = false;
        private boolean enableEncryption = false;
        private boolean enableArchiving = true;
        private boolean enableCampaignManagement = false;
    }
    
    // Compliance settings
    private ComplianceSettings compliance = new ComplianceSettings();
    
    @Data
    public static class ComplianceSettings {
        private boolean enableTCPA = true; // US
        private boolean enableGDPR = true; // EU
        private boolean enablePECR = true; // UK
        private boolean enableCANSPAM = true; // US
        private boolean enableCASL = true; // Canada
        private boolean requireExplicitConsent = true;
        private boolean honorQuietHours = true;
        private String quietHoursStart = "21:00";
        private String quietHoursEnd = "09:00";
        private String quietHoursTimezone = "recipient";
        private int consentExpiryDays = 365;
        private boolean enableConsentTracking = true;
    }
    
    // Error codes mapping
    public static class ErrorCodes {
        public static final Map<String, String> TWILIO_ERRORS = Map.of(
            "21211", "Invalid phone number",
            "21612", "Cannot send to unsubscribed recipient",
            "30003", "Unreachable destination handset",
            "30004", "Message blocked",
            "30005", "Unknown destination handset"
        );
        
        public static final Map<String, String> VONAGE_ERRORS = Map.of(
            "1", "Throttled",
            "2", "Missing parameters",
            "3", "Invalid parameters",
            "4", "Invalid credentials",
            "5", "Internal error"
        );
    }
}