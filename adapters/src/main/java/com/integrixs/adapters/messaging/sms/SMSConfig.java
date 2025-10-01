package com.integrixs.adapters.messaging.sms;

import com.integrixs.adapters.config.BaseAdapterConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

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

    // Provider - specific endpoints
    private String apiEndpoint;
    private String apiVersion;

    // Regional settings
    private String defaultCountryCode = " + 1";
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

    // Opt - out management
    private boolean enableOptOutManagement = true;
    private List<String> optOutKeywords = List.of("STOP", "UNSUBSCRIBE", "CANCEL", "END", "QUIT");
    private String optOutConfirmationMessage = "You have been unsubscribed. Reply START to resubscribe.";

    // Analytics
    private boolean enableAnalytics = true;
    private boolean trackLinks = false;
    private String linkTrackingDomain;

    // Cleanup and retention settings
    private long cleanupInterval = 3600000; // milliseconds (1 hour)
    private long messageRetentionSeconds = 86400; // 24 hours

    // Template fields - these seem to be misplaced getters/setters
    private String id;
    private String name;
    private String content;
    private Map<String, String> variables = new HashMap<>();
    private boolean approved = false;
    private String language = "en";
    private MessageType type = MessageType.SMS;

    // Twilio specific fields
    private String accountSid;
    private String messagingServiceSid;
    private boolean useMessagingService = false;
    private String region = "us1";
    private boolean enableStickySender = false;
    private boolean enableSmartEncoding = true;
    private boolean enableValidityPeriod = false;
    private boolean enableShortenUrls = false;
    private String statusCallbackUrl;
    private String signatureSecret;
    private String privateKey;
    private boolean enableSignatureValidation = false;
    private String defaultType = "text";
    private boolean enableDlr = false;
    private String dlrUrl;
    private int dlrMask = 1;
    private boolean enableEdgeLocations = false;
    private List<String> edgeLocations = new ArrayList<>();
    private String notifyUrl;
    private String notifyContentType = "application/json";
    private boolean intermediateReport = false;
    private Map<String, String> gatewayMapping = new HashMap<>();
    private String reportUrl;
    private int validity = 48; // hours
    private String reference;
    private boolean enableHlrLookup = false;
    private String apiKeyPrefix = "Bearer";
    private String baseUrl;
    private boolean enableDeliveryReports = true;
    private String callbackData;
    private boolean flash = false;
    private String transliteration = "false";
    private boolean enableScheduling = false;
    private String languageCode = "en";
    private boolean enableUrlShortening = false;
    private boolean trackClicks = false;
    private String trackingUrl;
    private boolean includeSmsCountInResponse = false;
    private boolean enable2WayMessaging = false;
    private boolean enableKeywordProcessing = false;
    private boolean enableAutoResponse = false;
    private boolean enableBulkMessaging = false;

    // Scheduler configuration
    private long bulkProcessorDelay = 1000; // milliseconds
    private long bulkProcessorInterval = 1000; // milliseconds
    private int bulkBatchSize = 100; // max messages per batch

    // Phone validation
    private int minPhoneLength = 10;
    private int maxPhoneLength = 15;

    // Quiet hours configuration
    private long quietHoursDelayHours = 8; // hours to delay during quiet hours
    private int quietHoursStartHour = 21; // 9 PM
    private int quietHoursEndHour = 8; // 8 AM

    // Template configuration
    private String templateVariablePrefix = " {{";
    private String templateVariableSuffix = "}}";

    // Retry configuration
    private double exponentialBackoffBase = 2.0;

    // API endpoints
    private String twilioApiBaseUrl;
    private String vonageApiBaseUrl;
    private String messageBirdApiBaseUrl;

    // Additional missing fields
    private String accessKey;
    private String accessKeyId;
    private String clientRef;
    private String dataEncoding = "text";
    private boolean enableA2PCompliance = false;
    private boolean enableArchiving = false;
    private boolean enableCampaignManagement = false;
    private boolean enableEncryption = false;
    private boolean enableFailover = false;
    private boolean enableFlashMessage = false;
    private boolean enableGDPR = false;
    private boolean enableGeofencing = false;
    private boolean enableLoadBalancing = false;
    private boolean enableNumberPooling = false;
    private boolean enablePersonalization = false;
    private boolean enableSigning = false;
    private boolean enableTCPA = false;
    private String gateway;
    private Map<String, String> messageAttributes = new HashMap<>();
    private String originationNumber;
    private int protocolId = 0;
    private String registeredKeyword;
    private String roleArn;
    private String secretAccessKey;
    private String senderIdPoolName;
    private String signingKey;
    private String snsTopicArn;
    private boolean useTopic = false;

    // Compliance fields (duplicated from inner class for getter/setter access)
    private boolean enablePECR = true;
    private boolean enableCANSPAM = true;
    private boolean enableCASL = true;
    private boolean requireExplicitConsent = true;
    private boolean honorQuietHours = true;
    private String quietHoursStart = "21:00";
    private String quietHoursEnd = "09:00";
    private String quietHoursTimezone = "recipient";
    private int consentExpiryDays = 365;
    private boolean enableConsentTracking = true;

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
        public static class MessageTemplate {
        private String id;
        private String name;
        private String content;
        private Map<String, String> variables = new HashMap<>();
        private boolean approved = false;
        private String language = "en";
        private MessageType type = MessageType.SMS;

        // Getters
        public String getContent() {
            return content;
        }
        public void setContent(String content) {
            this.content = content;
        }
    }

    // Provider - specific configurations
    private TwilioConfig twilioConfig = new TwilioConfig();
    private VonageConfig vonageConfig = new VonageConfig();
    private AwsSnsConfig awsSnsConfig = new AwsSnsConfig();
    private MessageBirdConfig messageBirdConfig = new MessageBirdConfig();
    private InfobipConfig infobipConfig = new InfobipConfig();

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

        // Getters and Setters
        public String getAccountSid() {
            return accountSid;
        }
        public void setAccountSid(String accountSid) {
            this.accountSid = accountSid;
        }
        public String getAuthToken() {
            return authToken;
        }
        public void setAuthToken(String authToken) {
            this.authToken = authToken;
        }
        public String getStatusCallbackUrl() {
            return statusCallbackUrl;
        }
        public void setStatusCallbackUrl(String statusCallbackUrl) {
            this.statusCallbackUrl = statusCallbackUrl;
        }
    }

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

        // Getters and Setters
        public String getApiKey() {
            return apiKey;
        }
        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }
        public String getApiSecret() {
            return apiSecret;
        }
        public void setApiSecret(String apiSecret) {
            this.apiSecret = apiSecret;
        }
        public String getClientRef() {
            return clientRef;
        }
        public void setClientRef(String clientRef) {
            this.clientRef = clientRef;
        }
    }

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

        // Getters and Setters
        public String getRegion() {
            return region;
        }
        public void setRegion(String region) {
            this.region = region;
        }
        public String getAccessKeyId() {
            return accessKeyId;
        }
        public void setAccessKeyId(String accessKeyId) {
            this.accessKeyId = accessKeyId;
        }
        public String getSecretAccessKey() {
            return secretAccessKey;
        }
        public void setSecretAccessKey(String secretAccessKey) {
            this.secretAccessKey = secretAccessKey;
        }
        public String getRoleArn() {
            return roleArn;
        }
        public void setRoleArn(String roleArn) {
            this.roleArn = roleArn;
        }
        public String getSnsTopicArn() {
            return snsTopicArn;
        }
        public void setSnsTopicArn(String snsTopicArn) {
            this.snsTopicArn = snsTopicArn;
        }
        public boolean isUseTopic() {
            return useTopic;
        }
        public void setUseTopic(boolean useTopic) {
            this.useTopic = useTopic;
        }
        public Map<String, String> getMessageAttributes() {
            return messageAttributes;
        }
        public void setMessageAttributes(Map<String, String> messageAttributes) {
            this.messageAttributes = messageAttributes;
        }
        public String getSenderIdPoolName() {
            return senderIdPoolName;
        }
        public void setSenderIdPoolName(String senderIdPoolName) {
            this.senderIdPoolName = senderIdPoolName;
        }
        public boolean isEnableNumberPooling() {
            return enableNumberPooling;
        }
        public void setEnableNumberPooling(boolean enableNumberPooling) {
            this.enableNumberPooling = enableNumberPooling;
        }
        public String getOriginationNumber() {
            return originationNumber;
        }
        public void setOriginationNumber(String originationNumber) {
            this.originationNumber = originationNumber;
        }
        public String getRegisteredKeyword() {
            return registeredKeyword;
        }
        public void setRegisteredKeyword(String registeredKeyword) {
            this.registeredKeyword = registeredKeyword;
        }
    }

        public static class MessageBirdConfig {
        private String accessKey;
        private String signingKey;
        private boolean enableSigning = false;
        private String dataEncoding = "auto";
        private String gateway;
        private String reportUrl;
        private int validity;
        private String reference;
        private boolean enableHlrLookup = false;

        // Getters and Setters
        public String getAccessKey() {
            return accessKey;
        }
        public void setAccessKey(String accessKey) {
            this.accessKey = accessKey;
        }
        public String getSigningKey() {
            return signingKey;
        }
        public void setSigningKey(String signingKey) {
            this.signingKey = signingKey;
        }
        public boolean isEnableSigning() {
            return enableSigning;
        }
        public void setEnableSigning(boolean enableSigning) {
            this.enableSigning = enableSigning;
        }
        public String getDataEncoding() {
            return dataEncoding;
        }
        public void setDataEncoding(String dataEncoding) {
            this.dataEncoding = dataEncoding;
        }
        public String getGateway() {
            return gateway;
        }
        public void setGateway(String gateway) {
            this.gateway = gateway;
        }
        public String getReportUrl() {
            return reportUrl;
        }
        public void setReportUrl(String reportUrl) {
            this.reportUrl = reportUrl;
        }
        public int getValidity() {
            return validity;
        }
        public void setValidity(int validity) {
            this.validity = validity;
        }
        public String getReference() {
            return reference;
        }
        public void setReference(String reference) {
            this.reference = reference;
        }
        public boolean isEnableHlrLookup() {
            return enableHlrLookup;
        }
        public void setEnableHlrLookup(boolean enableHlrLookup) {
            this.enableHlrLookup = enableHlrLookup;
        }
    }

        public static class InfobipConfig {
        private String apiKey;
        private String apiKeyPrefix = "App";
        private String baseUrl;
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
    // Getters and Setters
    public SMSProvider getProvider() {
        return provider;
    }
    public void setProvider(SMSProvider provider) {
        this.provider = provider;
    }
    public String getAccountId() {
        return accountId;
    }
    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }
    public String getAuthToken() {
        return authToken;
    }
    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }
    public String getApiKey() {
        return apiKey;
    }
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
    public String getApiSecret() {
        return apiSecret;
    }
    public void setApiSecret(String apiSecret) {
        this.apiSecret = apiSecret;
    }
    public String getDefaultSenderNumber() {
        return defaultSenderNumber;
    }
    public void setDefaultSenderNumber(String defaultSenderNumber) {
        this.defaultSenderNumber = defaultSenderNumber;
    }
    public String getSenderName() {
        return senderName;
    }
    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }
    public List<String> getSenderNumbers() {
        return senderNumbers;
    }
    public void setSenderNumbers(List<String> senderNumbers) {
        this.senderNumbers = senderNumbers;
    }
    public MessageType getDefaultMessageType() {
        return defaultMessageType;
    }
    public void setDefaultMessageType(MessageType defaultMessageType) {
        this.defaultMessageType = defaultMessageType;
    }
    public int getMaxMessageLength() {
        return maxMessageLength;
    }
    public void setMaxMessageLength(int maxMessageLength) {
        this.maxMessageLength = maxMessageLength;
    }
    public boolean isEnableUnicode() {
        return enableUnicode;
    }
    public void setEnableUnicode(boolean enableUnicode) {
        this.enableUnicode = enableUnicode;
    }
    public boolean isEnableConcatenation() {
        return enableConcatenation;
    }
    public void setEnableConcatenation(boolean enableConcatenation) {
        this.enableConcatenation = enableConcatenation;
    }
    public int getMaxConcatenatedParts() {
        return maxConcatenatedParts;
    }
    public void setMaxConcatenatedParts(int maxConcatenatedParts) {
        this.maxConcatenatedParts = maxConcatenatedParts;
    }
    public boolean isRequestDeliveryReceipt() {
        return requestDeliveryReceipt;
    }
    public void setRequestDeliveryReceipt(boolean requestDeliveryReceipt) {
        this.requestDeliveryReceipt = requestDeliveryReceipt;
    }
    public String getCallbackUrl() {
        return callbackUrl;
    }
    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }
    public int getMessageValidityPeriod() {
        return messageValidityPeriod;
    }
    public void setMessageValidityPeriod(int messageValidityPeriod) {
        this.messageValidityPeriod = messageValidityPeriod;
    }
    public DeliveryPriority getPriority() {
        return priority;
    }
    public void setPriority(DeliveryPriority priority) {
        this.priority = priority;
    }
    public int getMessagesPerSecond() {
        return messagesPerSecond;
    }
    public void setMessagesPerSecond(int messagesPerSecond) {
        this.messagesPerSecond = messagesPerSecond;
    }
    public int getMessagesPerMinute() {
        return messagesPerMinute;
    }
    public void setMessagesPerMinute(int messagesPerMinute) {
        this.messagesPerMinute = messagesPerMinute;
    }
    public int getMessagesPerHour() {
        return messagesPerHour;
    }
    public void setMessagesPerHour(int messagesPerHour) {
        this.messagesPerHour = messagesPerHour;
    }
    public int getMessagesPerDay() {
        return messagesPerDay;
    }
    public void setMessagesPerDay(int messagesPerDay) {
        this.messagesPerDay = messagesPerDay;
    }
    public int getMaxRetries() {
        return maxRetries;
    }
    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }
    public long getRetryDelay() {
        return retryDelay;
    }
    public void setRetryDelay(long retryDelay) {
        this.retryDelay = retryDelay;
    }
    public boolean isExponentialBackoff() {
        return exponentialBackoff;
    }
    public void setExponentialBackoff(boolean exponentialBackoff) {
        this.exponentialBackoff = exponentialBackoff;
    }
    public String getApiEndpoint() {
        return apiEndpoint;
    }
    public void setApiEndpoint(String apiEndpoint) {
        this.apiEndpoint = apiEndpoint;
    }
    public String getApiVersion() {
        return apiVersion;
    }
    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }
    public String getDefaultCountryCode() {
        return defaultCountryCode;
    }
    public void setDefaultCountryCode(String defaultCountryCode) {
        this.defaultCountryCode = defaultCountryCode;
    }
    public String getPreferredRoute() {
        return preferredRoute;
    }
    public void setPreferredRoute(String preferredRoute) {
        this.preferredRoute = preferredRoute;
    }
    public List<String> getBlockedCountries() {
        return blockedCountries;
    }
    public void setBlockedCountries(List<String> blockedCountries) {
        this.blockedCountries = blockedCountries;
    }
    public List<String> getAllowedCountries() {
        return allowedCountries;
    }
    public void setAllowedCountries(List<String> allowedCountries) {
        this.allowedCountries = allowedCountries;
    }
    public boolean isEnableContentFiltering() {
        return enableContentFiltering;
    }
    public void setEnableContentFiltering(boolean enableContentFiltering) {
        this.enableContentFiltering = enableContentFiltering;
    }
    public List<String> getBlockedKeywords() {
        return blockedKeywords;
    }
    public void setBlockedKeywords(List<String> blockedKeywords) {
        this.blockedKeywords = blockedKeywords;
    }
    public boolean isEnableSpamDetection() {
        return enableSpamDetection;
    }
    public void setEnableSpamDetection(boolean enableSpamDetection) {
        this.enableSpamDetection = enableSpamDetection;
    }
    public boolean isValidateNumbers() {
        return validateNumbers;
    }
    public void setValidateNumbers(boolean validateNumbers) {
        this.validateNumbers = validateNumbers;
    }
    public boolean isEnableNumberLookup() {
        return enableNumberLookup;
    }
    public void setEnableNumberLookup(boolean enableNumberLookup) {
        this.enableNumberLookup = enableNumberLookup;
    }
    public boolean isRejectLandlines() {
        return rejectLandlines;
    }
    public void setRejectLandlines(boolean rejectLandlines) {
        this.rejectLandlines = rejectLandlines;
    }
    public boolean isRejectVoip() {
        return rejectVoip;
    }
    public void setRejectVoip(boolean rejectVoip) {
        this.rejectVoip = rejectVoip;
    }
    public Map<String, MessageTemplate> getMessageTemplates() {
        return messageTemplates;
    }
    public void setMessageTemplates(Map<String, MessageTemplate> messageTemplates) {
        this.messageTemplates = messageTemplates;
    }
    public boolean isEnableOptOutManagement() {
        return enableOptOutManagement;
    }
    public void setEnableOptOutManagement(boolean enableOptOutManagement) {
        this.enableOptOutManagement = enableOptOutManagement;
    }
    public List<String> getOptOutKeywords() {
        return optOutKeywords;
    }
    public void setOptOutKeywords(List<String> optOutKeywords) {
        this.optOutKeywords = optOutKeywords;
    }
    public String getOptOutConfirmationMessage() {
        return optOutConfirmationMessage;
    }
    public void setOptOutConfirmationMessage(String optOutConfirmationMessage) {
        this.optOutConfirmationMessage = optOutConfirmationMessage;
    }
    public boolean isEnableAnalytics() {
        return enableAnalytics;
    }
    public void setEnableAnalytics(boolean enableAnalytics) {
        this.enableAnalytics = enableAnalytics;
    }
    public boolean isTrackLinks() {
        return trackLinks;
    }
    public void setTrackLinks(boolean trackLinks) {
        this.trackLinks = trackLinks;
    }
    public String getLinkTrackingDomain() {
        return linkTrackingDomain;
    }
    public void setLinkTrackingDomain(String linkTrackingDomain) {
        this.linkTrackingDomain = linkTrackingDomain;
    }
    public long getCleanupInterval() {
        return cleanupInterval;
    }
    public void setCleanupInterval(long cleanupInterval) {
        this.cleanupInterval = cleanupInterval;
    }
    public long getMessageRetentionSeconds() {
        return messageRetentionSeconds;
    }
    public void setMessageRetentionSeconds(long messageRetentionSeconds) {
        this.messageRetentionSeconds = messageRetentionSeconds;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public Map<String, String> getVariables() {
        return variables;
    }
    public void setVariables(Map<String, String> variables) {
        this.variables = variables;
    }
    public boolean isApproved() {
        return approved;
    }
    public void setApproved(boolean approved) {
        this.approved = approved;
    }
    public String getLanguage() {
        return language;
    }
    public void setLanguage(String language) {
        this.language = language;
    }
    public MessageType getType() {
        return type;
    }
    public void setType(MessageType type) {
        this.type = type;
    }
    public TwilioConfig getTwilioConfig() {
        return twilioConfig;
    }
    public void setTwilioConfig(TwilioConfig twilioConfig) {
        this.twilioConfig = twilioConfig;
    }
    public VonageConfig getVonageConfig() {
        return vonageConfig;
    }
    public void setVonageConfig(VonageConfig vonageConfig) {
        this.vonageConfig = vonageConfig;
    }
    public AwsSnsConfig getAwsSnsConfig() {
        return awsSnsConfig;
    }
    public void setAwsSnsConfig(AwsSnsConfig awsSnsConfig) {
        this.awsSnsConfig = awsSnsConfig;
    }
    public MessageBirdConfig getMessageBirdConfig() {
        return messageBirdConfig;
    }
    public void setMessageBirdConfig(MessageBirdConfig messageBirdConfig) {
        this.messageBirdConfig = messageBirdConfig;
    }
    public InfobipConfig getInfobipConfig() {
        return infobipConfig;
    }
    public void setInfobipConfig(InfobipConfig infobipConfig) {
        this.infobipConfig = infobipConfig;
    }
    public String getAccountSid() {
        return accountSid;
    }
    public void setAccountSid(String accountSid) {
        this.accountSid = accountSid;
    }
    public String getMessagingServiceSid() {
        return messagingServiceSid;
    }
    public void setMessagingServiceSid(String messagingServiceSid) {
        this.messagingServiceSid = messagingServiceSid;
    }
    public boolean isUseMessagingService() {
        return useMessagingService;
    }
    public void setUseMessagingService(boolean useMessagingService) {
        this.useMessagingService = useMessagingService;
    }
    public String getRegion() {
        return region;
    }
    public void setRegion(String region) {
        this.region = region;
    }
    public boolean isEnableEdgeLocations() {
        return enableEdgeLocations;
    }
    public void setEnableEdgeLocations(boolean enableEdgeLocations) {
        this.enableEdgeLocations = enableEdgeLocations;
    }
    public List<String> getEdgeLocations() {
        return edgeLocations;
    }
    public void setEdgeLocations(List<String> edgeLocations) {
        this.edgeLocations = edgeLocations;
    }
    public boolean isEnableStickySender() {
        return enableStickySender;
    }
    public void setEnableStickySender(boolean enableStickySender) {
        this.enableStickySender = enableStickySender;
    }
    public boolean isEnableSmartEncoding() {
        return enableSmartEncoding;
    }
    public void setEnableSmartEncoding(boolean enableSmartEncoding) {
        this.enableSmartEncoding = enableSmartEncoding;
    }
    public boolean isEnableValidityPeriod() {
        return enableValidityPeriod;
    }
    public void setEnableValidityPeriod(boolean enableValidityPeriod) {
        this.enableValidityPeriod = enableValidityPeriod;
    }
    public boolean isEnableShortenUrls() {
        return enableShortenUrls;
    }
    public void setEnableShortenUrls(boolean enableShortenUrls) {
        this.enableShortenUrls = enableShortenUrls;
    }
    public String getStatusCallbackUrl() {
        return statusCallbackUrl;
    }
    public void setStatusCallbackUrl(String statusCallbackUrl) {
        this.statusCallbackUrl = statusCallbackUrl;
    }
    public String getSignatureSecret() {
        return signatureSecret;
    }
    public void setSignatureSecret(String signatureSecret) {
        this.signatureSecret = signatureSecret;
    }
    public String getPrivateKey() {
        return privateKey;
    }
    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }
    public boolean isEnableSignatureValidation() {
        return enableSignatureValidation;
    }
    public void setEnableSignatureValidation(boolean enableSignatureValidation) {
        this.enableSignatureValidation = enableSignatureValidation;
    }
    public String getDefaultType() {
        return defaultType;
    }
    public void setDefaultType(String defaultType) {
        this.defaultType = defaultType;
    }
    public boolean isEnableDlr() {
        return enableDlr;
    }
    public void setEnableDlr(boolean enableDlr) {
        this.enableDlr = enableDlr;
    }
    public String getDlrUrl() {
        return dlrUrl;
    }
    public void setDlrUrl(String dlrUrl) {
        this.dlrUrl = dlrUrl;
    }
    public int getDlrMask() {
        return dlrMask;
    }
    public void setDlrMask(int dlrMask) {
        this.dlrMask = dlrMask;
    }
    public String getClientRef() {
        return clientRef;
    }
    public void setClientRef(String clientRef) {
        this.clientRef = clientRef;
    }
    public boolean isEnableFlashMessage() {
        return enableFlashMessage;
    }
    public void setEnableFlashMessage(boolean enableFlashMessage) {
        this.enableFlashMessage = enableFlashMessage;
    }
    public int getProtocolId() {
        return protocolId;
    }
    public void setProtocolId(int protocolId) {
        this.protocolId = protocolId;
    }
    public String getAccessKeyId() {
        return accessKeyId;
    }
    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }
    public String getSecretAccessKey() {
        return secretAccessKey;
    }
    public void setSecretAccessKey(String secretAccessKey) {
        this.secretAccessKey = secretAccessKey;
    }
    public String getRoleArn() {
        return roleArn;
    }
    public void setRoleArn(String roleArn) {
        this.roleArn = roleArn;
    }
    public String getSnsTopicArn() {
        return snsTopicArn;
    }
    public void setSnsTopicArn(String snsTopicArn) {
        this.snsTopicArn = snsTopicArn;
    }
    public boolean isUseTopic() {
        return useTopic;
    }
    public void setUseTopic(boolean useTopic) {
        this.useTopic = useTopic;
    }
    public Map<String, String> getMessageAttributes() {
        return messageAttributes;
    }
    public void setMessageAttributes(Map<String, String> messageAttributes) {
        this.messageAttributes = messageAttributes;
    }
    public String getSenderIdPoolName() {
        return senderIdPoolName;
    }
    public void setSenderIdPoolName(String senderIdPoolName) {
        this.senderIdPoolName = senderIdPoolName;
    }
    public boolean isEnableNumberPooling() {
        return enableNumberPooling;
    }
    public void setEnableNumberPooling(boolean enableNumberPooling) {
        this.enableNumberPooling = enableNumberPooling;
    }
    public String getOriginationNumber() {
        return originationNumber;
    }
    public void setOriginationNumber(String originationNumber) {
        this.originationNumber = originationNumber;
    }
    public String getRegisteredKeyword() {
        return registeredKeyword;
    }
    public void setRegisteredKeyword(String registeredKeyword) {
        this.registeredKeyword = registeredKeyword;
    }
    public String getAccessKey() {
        return accessKey;
    }
    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }
    public String getSigningKey() {
        return signingKey;
    }
    public void setSigningKey(String signingKey) {
        this.signingKey = signingKey;
    }
    public boolean isEnableSigning() {
        return enableSigning;
    }
    public void setEnableSigning(boolean enableSigning) {
        this.enableSigning = enableSigning;
    }
    public String getDataEncoding() {
        return dataEncoding;
    }
    public void setDataEncoding(String dataEncoding) {
        this.dataEncoding = dataEncoding;
    }
    public String getGateway() {
        return gateway;
    }
    public void setGateway(String gateway) {
        this.gateway = gateway;
    }
    public Map<String, String> getGatewayMapping() {
        return gatewayMapping;
    }
    public void setGatewayMapping(Map<String, String> gatewayMapping) {
        this.gatewayMapping = gatewayMapping;
    }
    public String getReportUrl() {
        return reportUrl;
    }
    public void setReportUrl(String reportUrl) {
        this.reportUrl = reportUrl;
    }
    public int getValidity() {
        return validity;
    }
    public void setValidity(int validity) {
        this.validity = validity;
    }
    public String getReference() {
        return reference;
    }
    public void setReference(String reference) {
        this.reference = reference;
    }
    public boolean isEnableHlrLookup() {
        return enableHlrLookup;
    }
    public void setEnableHlrLookup(boolean enableHlrLookup) {
        this.enableHlrLookup = enableHlrLookup;
    }
    public String getApiKeyPrefix() {
        return apiKeyPrefix;
    }
    public void setApiKeyPrefix(String apiKeyPrefix) {
        this.apiKeyPrefix = apiKeyPrefix;
    }
    public String getBaseUrl() {
        return baseUrl;
    }
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    public boolean isEnableDeliveryReports() {
        return enableDeliveryReports;
    }
    public void setEnableDeliveryReports(boolean enableDeliveryReports) {
        this.enableDeliveryReports = enableDeliveryReports;
    }
    public String getNotifyUrl() {
        return notifyUrl;
    }
    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }
    public String getNotifyContentType() {
        return notifyContentType;
    }
    public void setNotifyContentType(String notifyContentType) {
        this.notifyContentType = notifyContentType;
    }
    public boolean isIntermediateReport() {
        return intermediateReport;
    }
    public void setIntermediateReport(boolean intermediateReport) {
        this.intermediateReport = intermediateReport;
    }
    public String getCallbackData() {
        return callbackData;
    }
    public void setCallbackData(String callbackData) {
        this.callbackData = callbackData;
    }
    public boolean isFlash() {
        return flash;
    }
    public void setFlash(boolean flash) {
        this.flash = flash;
    }
    public String getTransliteration() {
        return transliteration;
    }
    public void setTransliteration(String transliteration) {
        this.transliteration = transliteration;
    }
    public String getLanguageCode() {
        return languageCode;
    }
    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }
    public boolean isEnableUrlShortening() {
        return enableUrlShortening;
    }
    public void setEnableUrlShortening(boolean enableUrlShortening) {
        this.enableUrlShortening = enableUrlShortening;
    }
    public boolean isTrackClicks() {
        return trackClicks;
    }
    public void setTrackClicks(boolean trackClicks) {
        this.trackClicks = trackClicks;
    }
    public String getTrackingUrl() {
        return trackingUrl;
    }
    public void setTrackingUrl(String trackingUrl) {
        this.trackingUrl = trackingUrl;
    }
    public boolean isIncludeSmsCountInResponse() {
        return includeSmsCountInResponse;
    }
    public void setIncludeSmsCountInResponse(boolean includeSmsCountInResponse) {
        this.includeSmsCountInResponse = includeSmsCountInResponse;
    }
    public Features getFeatures() {
        return features;
    }
    public void setFeatures(Features features) {
        this.features = features;
    }
    public boolean isEnable2WayMessaging() {
        return enable2WayMessaging;
    }
    public void setEnable2WayMessaging(boolean enable2WayMessaging) {
        this.enable2WayMessaging = enable2WayMessaging;
    }
    public boolean isEnableKeywordProcessing() {
        return enableKeywordProcessing;
    }
    public void setEnableKeywordProcessing(boolean enableKeywordProcessing) {
        this.enableKeywordProcessing = enableKeywordProcessing;
    }
    public boolean isEnableAutoResponse() {
        return enableAutoResponse;
    }
    public void setEnableAutoResponse(boolean enableAutoResponse) {
        this.enableAutoResponse = enableAutoResponse;
    }
    public boolean isEnableScheduling() {
        return enableScheduling;
    }
    public void setEnableScheduling(boolean enableScheduling) {
        this.enableScheduling = enableScheduling;
    }
    public boolean isEnableBulkMessaging() {
        return enableBulkMessaging;
    }
    public void setEnableBulkMessaging(boolean enableBulkMessaging) {
        this.enableBulkMessaging = enableBulkMessaging;
    }
    public long getBulkProcessorDelay() {
        return bulkProcessorDelay;
    }
    public void setBulkProcessorDelay(long bulkProcessorDelay) {
        this.bulkProcessorDelay = bulkProcessorDelay;
    }
    public long getBulkProcessorInterval() {
        return bulkProcessorInterval;
    }
    public void setBulkProcessorInterval(long bulkProcessorInterval) {
        this.bulkProcessorInterval = bulkProcessorInterval;
    }
    public int getBulkBatchSize() {
        return bulkBatchSize;
    }
    public void setBulkBatchSize(int bulkBatchSize) {
        this.bulkBatchSize = bulkBatchSize;
    }
    public int getMinPhoneLength() {
        return minPhoneLength;
    }
    public void setMinPhoneLength(int minPhoneLength) {
        this.minPhoneLength = minPhoneLength;
    }
    public int getMaxPhoneLength() {
        return maxPhoneLength;
    }
    public void setMaxPhoneLength(int maxPhoneLength) {
        this.maxPhoneLength = maxPhoneLength;
    }
    public long getQuietHoursDelayHours() {
        return quietHoursDelayHours;
    }
    public void setQuietHoursDelayHours(long quietHoursDelayHours) {
        this.quietHoursDelayHours = quietHoursDelayHours;
    }
    public int getQuietHoursStartHour() {
        return quietHoursStartHour;
    }
    public void setQuietHoursStartHour(int quietHoursStartHour) {
        this.quietHoursStartHour = quietHoursStartHour;
    }
    public int getQuietHoursEndHour() {
        return quietHoursEndHour;
    }
    public void setQuietHoursEndHour(int quietHoursEndHour) {
        this.quietHoursEndHour = quietHoursEndHour;
    }
    public String getTemplateVariablePrefix() {
        return templateVariablePrefix;
    }
    public void setTemplateVariablePrefix(String templateVariablePrefix) {
        this.templateVariablePrefix = templateVariablePrefix;
    }
    public String getTemplateVariableSuffix() {
        return templateVariableSuffix;
    }
    public void setTemplateVariableSuffix(String templateVariableSuffix) {
        this.templateVariableSuffix = templateVariableSuffix;
    }
    public double getExponentialBackoffBase() {
        return exponentialBackoffBase;
    }
    public void setExponentialBackoffBase(double exponentialBackoffBase) {
        this.exponentialBackoffBase = exponentialBackoffBase;
    }
    public String getTwilioApiBaseUrl() {
        return twilioApiBaseUrl;
    }
    public void setTwilioApiBaseUrl(String twilioApiBaseUrl) {
        this.twilioApiBaseUrl = twilioApiBaseUrl;
    }
    public String getVonageApiBaseUrl() {
        return vonageApiBaseUrl;
    }
    public void setVonageApiBaseUrl(String vonageApiBaseUrl) {
        this.vonageApiBaseUrl = vonageApiBaseUrl;
    }
    public String getMessageBirdApiBaseUrl() {
        return messageBirdApiBaseUrl;
    }
    public void setMessageBirdApiBaseUrl(String messageBirdApiBaseUrl) {
        this.messageBirdApiBaseUrl = messageBirdApiBaseUrl;
    }
    public boolean isEnablePersonalization() {
        return enablePersonalization;
    }
    public void setEnablePersonalization(boolean enablePersonalization) {
        this.enablePersonalization = enablePersonalization;
    }
    public boolean isEnableGeofencing() {
        return enableGeofencing;
    }
    public void setEnableGeofencing(boolean enableGeofencing) {
        this.enableGeofencing = enableGeofencing;
    }
    public boolean isEnableA2PCompliance() {
        return enableA2PCompliance;
    }
    public void setEnableA2PCompliance(boolean enableA2PCompliance) {
        this.enableA2PCompliance = enableA2PCompliance;
    }
    public boolean isEnableFailover() {
        return enableFailover;
    }
    public void setEnableFailover(boolean enableFailover) {
        this.enableFailover = enableFailover;
    }
    public boolean isEnableLoadBalancing() {
        return enableLoadBalancing;
    }
    public void setEnableLoadBalancing(boolean enableLoadBalancing) {
        this.enableLoadBalancing = enableLoadBalancing;
    }
    public boolean isEnableEncryption() {
        return enableEncryption;
    }
    public void setEnableEncryption(boolean enableEncryption) {
        this.enableEncryption = enableEncryption;
    }
    public boolean isEnableArchiving() {
        return enableArchiving;
    }
    public void setEnableArchiving(boolean enableArchiving) {
        this.enableArchiving = enableArchiving;
    }
    public boolean isEnableCampaignManagement() {
        return enableCampaignManagement;
    }
    public void setEnableCampaignManagement(boolean enableCampaignManagement) {
        this.enableCampaignManagement = enableCampaignManagement;
    }
    public ComplianceSettings getCompliance() {
        return compliance;
    }
    public void setCompliance(ComplianceSettings compliance) {
        this.compliance = compliance;
    }
    public boolean isEnableTCPA() {
        return enableTCPA;
    }
    public void setEnableTCPA(boolean enableTCPA) {
        this.enableTCPA = enableTCPA;
    }
    public boolean isEnableGDPR() {
        return enableGDPR;
    }
    public void setEnableGDPR(boolean enableGDPR) {
        this.enableGDPR = enableGDPR;
    }
    public boolean isEnablePECR() {
        return enablePECR;
    }
    public void setEnablePECR(boolean enablePECR) {
        this.enablePECR = enablePECR;
    }
    public boolean isEnableCANSPAM() {
        return enableCANSPAM;
    }
    public void setEnableCANSPAM(boolean enableCANSPAM) {
        this.enableCANSPAM = enableCANSPAM;
    }
    public boolean isEnableCASL() {
        return enableCASL;
    }
    public void setEnableCASL(boolean enableCASL) {
        this.enableCASL = enableCASL;
    }
    public boolean isRequireExplicitConsent() {
        return requireExplicitConsent;
    }
    public void setRequireExplicitConsent(boolean requireExplicitConsent) {
        this.requireExplicitConsent = requireExplicitConsent;
    }
    public boolean isHonorQuietHours() {
        return honorQuietHours;
    }
    public void setHonorQuietHours(boolean honorQuietHours) {
        this.honorQuietHours = honorQuietHours;
    }
    public String getQuietHoursStart() {
        return quietHoursStart;
    }
    public void setQuietHoursStart(String quietHoursStart) {
        this.quietHoursStart = quietHoursStart;
    }
    public String getQuietHoursEnd() {
        return quietHoursEnd;
    }
    public void setQuietHoursEnd(String quietHoursEnd) {
        this.quietHoursEnd = quietHoursEnd;
    }
    public String getQuietHoursTimezone() {
        return quietHoursTimezone;
    }
    public void setQuietHoursTimezone(String quietHoursTimezone) {
        this.quietHoursTimezone = quietHoursTimezone;
    }
    public int getConsentExpiryDays() {
        return consentExpiryDays;
    }
    public void setConsentExpiryDays(int consentExpiryDays) {
        this.consentExpiryDays = consentExpiryDays;
    }
    public boolean isEnableConsentTracking() {
        return enableConsentTracking;
    }
    public void setEnableConsentTracking(boolean enableConsentTracking) {
        this.enableConsentTracking = enableConsentTracking;
    }
}
