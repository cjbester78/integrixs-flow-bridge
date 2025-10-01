package com.integrixs.adapters.messaging.sms;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.adapters.core.AbstractInboundAdapter;
import com.integrixs.adapters.core.AdapterResult;
import com.integrixs.adapters.domain.model.AdapterOperationResult;
import com.integrixs.adapters.domain.model.AdapterConfiguration;
import com.integrixs.adapters.messaging.sms.SMSConfig.*;
import com.integrixs.shared.dto.MessageDTO;
import com.integrixs.shared.enums.AdapterType;
import com.integrixs.shared.exceptions.AdapterException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

// @Component  // REMOVED - was causing auto-initialization
public class SMSInboundAdapter extends AbstractInboundAdapter {

    public SMSInboundAdapter() {
        super(com.integrixs.adapters.domain.model.AdapterConfiguration.AdapterTypeEnum.SMS);
    }
    private static final Logger log = LoggerFactory.getLogger(SMSInboundAdapter.class);


    @Autowired
    private SMSConfig config;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RestTemplate restTemplate;

    // Metrics
    private final AtomicLong messagesReceived = new AtomicLong(0);
    private final AtomicLong deliveryReportsReceived = new AtomicLong(0);
    private final AtomicLong optOutsReceived = new AtomicLong(0);
    private final AtomicLong keywordMessagesReceived = new AtomicLong(0);

    // Message processing
    private final ExecutorService messageProcessor = Executors.newCachedThreadPool();
    private final Map<String, Instant> processedMessages = new ConcurrentHashMap<>();
    private final Set<String> optedOutNumbers = ConcurrentHashMap.newKeySet();

    // Keyword handlers
    private final Map<String, KeywordHandler> keywordHandlers = new ConcurrentHashMap<>();

    // Provider - specific webhook validators
    private final Map<SMSProvider, WebhookValidator> webhookValidators = new HashMap<>();

    // Initialization flag
    private volatile boolean isInitialized = false;

    public AdapterType getType() {
        return AdapterType.SMS;
    }

    public String getName() {
        return "SMS Inbound Adapter";
    }

    @PostConstruct
    public void initialize() {
        try {
            setupProviderConfiguration();
            initializeKeywordHandlers();
            initializeWebhookValidators();
            loadOptedOutNumbers();
            isInitialized = true;
            log.info("SMS Inbound Adapter initialized successfully for provider: {}", config.getProvider());
        } catch(Exception e) {
            log.error("Failed to initialize SMS Inbound Adapter", e);
            throw new RuntimeException("Failed to initialize SMS adapter", e);
        }
    }

    private void setupProviderConfiguration() {
        switch(config.getProvider()) {
            case TWILIO:
                setupTwilioConfiguration();
                break;
            case VONAGE:
                setupVonageConfiguration();
                break;
            case AWS_SNS:
                setupAwsSnsConfiguration();
                break;
            case MESSAGEBIRD:
                setupMessageBirdConfiguration();
                break;
            case INFOBIP:
                setupInfobipConfiguration();
                break;
            default:
                log.warn("Provider {} not fully configured for inbound", config.getProvider());
        }
    }

    private void setupTwilioConfiguration() {
        // TODO: Add getters/setters to TwilioConfig if needed
        // TwilioConfig twilioConfig = config.getTwilioConfig();
        // if(twilioConfig.getAccountSid() == null) {
        //     twilioConfig.setAccountSid(config.getAccountId());
        // }
        // if(twilioConfig.getAuthToken() == null) {
        //     twilioConfig.setAuthToken(config.getAuthToken());
        // }
    }

    private void setupVonageConfiguration() {
        // TODO: Add getters/setters to VonageConfig if needed
        // VonageConfig vonageConfig = config.getVonageConfig();
        // if(vonageConfig.getApiKey() == null) {
        //     vonageConfig.setApiKey(config.getApiKey());
        // }
        // if(vonageConfig.getApiSecret() == null) {
        //     vonageConfig.setApiSecret(config.getApiSecret());
        // }
    }

    private void setupAwsSnsConfiguration() {
        // AWS SNS specific setup
    }

    private void setupMessageBirdConfiguration() {
        // MessageBird specific setup
    }

    private void setupInfobipConfiguration() {
        // Infobip specific setup
    }

    private void initializeKeywordHandlers() {
        // Default opt - out keywords
        for(String keyword : config.getOptOutKeywords()) {
            keywordHandlers.put(keyword.toUpperCase(), new OptOutKeywordHandler());
        }

        // Custom keyword handlers
        // TODO: Add isEnableKeywordProcessing() to Features class if needed
        // if(config.getFeatures().isEnableKeywordProcessing()) {
        //     // Add custom keyword handlers here
        //     keywordHandlers.put("HELP", new HelpKeywordHandler());
        //     keywordHandlers.put("INFO", new InfoKeywordHandler());
        //     keywordHandlers.put("START", new OptInKeywordHandler());
        //     keywordHandlers.put("YES", new ConfirmationKeywordHandler());
        //     keywordHandlers.put("NO", new RejectionKeywordHandler());
        // }
    }

    private void initializeWebhookValidators() {
        webhookValidators.put(SMSProvider.TWILIO, new TwilioWebhookValidator());
        webhookValidators.put(SMSProvider.VONAGE, new VonageWebhookValidator());
        webhookValidators.put(SMSProvider.MESSAGEBIRD, new MessageBirdWebhookValidator());
        webhookValidators.put(SMSProvider.INFOBIP, new InfobipWebhookValidator());
    }

    private void loadOptedOutNumbers() {
        // Load opted - out numbers from persistent storage
        // This would typically load from a database
        log.info("Loaded {} opted - out numbers", optedOutNumbers.size());
    }

    // Webhook handling for incoming messages
    public void handleWebhook(Map<String, Object> webhookData, Map<String, String> headers) {
        try {
            // Validate webhook signature
            WebhookValidator validator = webhookValidators.get(config.getProvider());
            if(validator != null && !validator.validate(webhookData, headers)) {
                log.warn("Invalid webhook signature for provider {}", config.getProvider());
                return;
            }

            // Process based on webhook type
            String webhookType = detectWebhookType(webhookData);

            switch(webhookType) {
                case "incoming_message":
                    handleIncomingMessage(webhookData);
                    break;
                case "delivery_report":
                    handleDeliveryReport(webhookData);
                    break;
                case "opt_out":
                    handleOptOut(webhookData);
                    break;
                default:
                    log.warn("Unknown webhook type: {}", webhookType);
            }
        } catch(Exception e) {
            log.error("Error handling webhook", e);
        }
    }

    private String detectWebhookType(Map<String, Object> webhookData) {
        // Provider - specific webhook type detection
        switch(config.getProvider()) {
            case TWILIO:
                if(webhookData.containsKey("MessageStatus")) {
                    return "delivery_report";
                } else if(webhookData.containsKey("Body")) {
                    return "incoming_message";
                }
                break;
            case VONAGE:
                if(webhookData.containsKey("status")) {
                    return "delivery_report";
                } else if(webhookData.containsKey("text")) {
                    return "incoming_message";
                }
                break;
            default:
                if(webhookData.containsKey("message") || webhookData.containsKey("text")) {
                    return "incoming_message";
                } else if(webhookData.containsKey("status")) {
                    return "delivery_report";
                }
        }
        return "unknown";
    }

    private void handleIncomingMessage(Map<String, Object> webhookData) {
        messagesReceived.incrementAndGet();

        messageProcessor.execute(() -> {
            try {
                // Extract message details based on provider
                IncomingMessage message = extractIncomingMessage(webhookData);

                // Check for duplicate
                String messageId = message.getMessageId();
                if(processedMessages.putIfAbsent(messageId, Instant.now()) != null) {
                    log.debug("Duplicate message detected: {}", messageId);
                    return;
                }

                // Check opt - out status
                if(optedOutNumbers.contains(message.getFrom())) {
                    log.info("Message from opted - out number: {}", message.getFrom());
                    return;
                }

                // Process keywords
                // TODO: Add isEnableKeywordProcessing() to Features class if needed
                // if(config.getFeatures().isEnableKeywordProcessing()) {
                //     processKeywords(message);
                // }

                // Create MessageDTO
                Map<String, Object> messageContent = new HashMap<>();
                messageContent.put("from", message.getFrom());
                messageContent.put("to", message.getTo());
                messageContent.put("body", message.getBody());
                messageContent.put("messageId", message.getMessageId());
                messageContent.put("timestamp", message.getTimestamp());
                messageContent.put("provider", config.getProvider().name());
                messageContent.put("mediaUrls", message.getMediaUrls());
                messageContent.put("segments", message.getSegments());

                MessageDTO dto = new MessageDTO();
                dto.setCorrelationId(messageId);
                dto.setPayload(objectMapper.writeValueAsString(messageContent));
                dto.setHeaders(new HashMap<String, Object>() {{
                    put("type", "sms_incoming");
                    put("source", message.getFrom());
                    put("destination", "sms-inbound-queue");
                    put("timestamp", message.getTimestamp());
                    put("provider", config.getProvider().name());
                    put("to", message.getTo());
                    put("messageType", message.getType().name());
                }});

                // TODO: Implement message publishing
                // publishToQueue(dto);
                log.info("Received SMS message: {}", messageId);

            } catch(Exception e) {
                log.error("Error processing incoming message", e);
            }
        });
    }

    private IncomingMessage extractIncomingMessage(Map<String, Object> webhookData) {
        IncomingMessage message = new IncomingMessage();

        switch(config.getProvider()) {
            case TWILIO:
                message.setMessageId((String) webhookData.get("MessageSid"));
                message.setFrom((String) webhookData.get("From"));
                message.setTo((String) webhookData.get("To"));
                message.setBody((String) webhookData.get("Body"));
                message.setTimestamp(Instant.now().toEpochMilli());

                // Extract media URLs for MMS
                int numMedia = Integer.parseInt(webhookData.getOrDefault("NumMedia", "0").toString());
                List<String> mediaUrls = new ArrayList<>();
                for(int i = 0; i < numMedia; i++) {
                    String mediaUrl = (String) webhookData.get("MediaUrl" + i);
                    if(mediaUrl != null) {
                        mediaUrls.add(mediaUrl);
                    }
                }
                message.setMediaUrls(mediaUrls);
                message.setType(numMedia > 0 ? MessageType.MMS : MessageType.SMS);
                break;

            case VONAGE:
                message.setMessageId((String) webhookData.get("messageId"));
                message.setFrom((String) webhookData.get("msisdn"));
                message.setTo((String) webhookData.get("to"));
                message.setBody((String) webhookData.get("text"));
                message.setTimestamp(Long.parseLong(webhookData.get("message - timestamp").toString()));
                message.setType(MessageType.SMS);
                break;

            default:
                // Generic extraction
                message.setMessageId((String) webhookData.getOrDefault("id", UUID.randomUUID().toString()));
                message.setFrom((String) webhookData.get("from"));
                message.setTo((String) webhookData.get("to"));
                message.setBody((String) webhookData.get("message"));
                message.setTimestamp(Instant.now().toEpochMilli());
                message.setType(MessageType.SMS);
        }

        return message;
    }

    private void processKeywords(IncomingMessage message) {
        String body = message.getBody();
        if(body == null || body.trim().isEmpty()) {
            return;
        }

        // Extract first word as potential keyword
        String firstWord = body.trim().split("\\s + ")[0].toUpperCase();

        KeywordHandler handler = keywordHandlers.get(firstWord);
        if(handler != null) {
            keywordMessagesReceived.incrementAndGet();
            handler.handle(message, this);
        }
    }

    private void handleDeliveryReport(Map<String, Object> webhookData) {
        deliveryReportsReceived.incrementAndGet();

        try {
            DeliveryReport report = extractDeliveryReport(webhookData);

            Map<String, Object> reportContent = new HashMap<>();
            reportContent.put("messageId", report.messageId);
            reportContent.put("status", report.getStatus());
            reportContent.put("errorCode", report.getErrorCode());
            reportContent.put("errorMessage", report.getErrorMessage());
            reportContent.put("timestamp", report.timestamp);
            reportContent.put("provider", config.getProvider().name());

            MessageDTO dto = new MessageDTO();
            dto.setCorrelationId(report.messageId + "_dr");
            dto.setPayload(objectMapper.writeValueAsString(reportContent));
            dto.setHeaders(new HashMap<String, Object>() {{
                put("type", "sms_delivery_report");
                put("source", config.getProvider().name());
                put("destination", "sms-inbound-queue");
                put("timestamp", report.timestamp);
                put("originalMessageId", report.messageId);
                put("status", report.getStatus().name());
            }});

            // TODO: Implement message publishing
            // publishToQueue(dto);
            log.info("Received SMS delivery report: {}", report.messageId);

        } catch(Exception e) {
            log.error("Error processing delivery report", e);
        }
    }

    private DeliveryReport extractDeliveryReport(Map<String, Object> webhookData) {
        DeliveryReport report = new DeliveryReport();

        switch(config.getProvider()) {
            case TWILIO:
                report.messageId = (String) webhookData.get("MessageSid");
                report.setStatus(mapTwilioStatus((String) webhookData.get("MessageStatus")));
                report.setErrorCode((String) webhookData.get("ErrorCode"));
                report.setErrorMessage((String) webhookData.get("ErrorMessage"));
                report.timestamp = Instant.now().toEpochMilli();
                break;

            case VONAGE:
                report.messageId = (String) webhookData.get("messageId");
                report.setStatus(mapVonageStatus((String) webhookData.get("status")));
                report.setErrorCode((String) webhookData.get("err - code"));
                report.timestamp = parseVonageTimestamp((String) webhookData.get("message - timestamp"));
                break;

            default:
                report.messageId = (String) webhookData.get("messageId");
                report.setStatus(DeliveryStatus.UNKNOWN);
                report.timestamp = Instant.now().toEpochMilli();
        }

        return report;
    }

    private void handleOptOut(Map<String, Object> webhookData) {
        optOutsReceived.incrementAndGet();

        try {
            String phoneNumber = extractPhoneNumber(webhookData);
            optedOutNumbers.add(phoneNumber);

            // Persist opt - out
            persistOptOut(phoneNumber);

            log.info("Added {} to opt - out list", phoneNumber);
        } catch(Exception e) {
            log.error("Error handling opt - out", e);
        }
    }

    private String extractPhoneNumber(Map<String, Object> webhookData) {
        switch(config.getProvider()) {
            case TWILIO:
                return(String) webhookData.get("From");
            case VONAGE:
                return(String) webhookData.get("msisdn");
            default:
                return(String) webhookData.get("from");
        }
    }

    private void persistOptOut(String phoneNumber) {
        // Persist to database
        // This would typically save to a database
    }

    // Provider - specific status mapping
    private DeliveryStatus mapTwilioStatus(String status) {
        switch(status) {
            case "delivered":
                return DeliveryStatus.DELIVERED;
            case "failed":
            case "undelivered":
                return DeliveryStatus.FAILED;
            case "sent":
            case "sending":
                return DeliveryStatus.SENT;
            case "queued":
                return DeliveryStatus.PENDING;
            default:
                return DeliveryStatus.UNKNOWN;
        }
    }

    private DeliveryStatus mapVonageStatus(String status) {
        switch(status) {
            case "delivered":
                return DeliveryStatus.DELIVERED;
            case "failed":
            case "rejected":
                return DeliveryStatus.FAILED;
            case "accepted":
                return DeliveryStatus.SENT;
            case "buffered":
                return DeliveryStatus.PENDING;
            default:
                return DeliveryStatus.UNKNOWN;
        }
    }

    private long parseVonageTimestamp(String timestamp) {
        try {
            LocalDateTime dt = LocalDateTime.parse(timestamp);
            return dt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        } catch(Exception e) {
            return Instant.now().toEpochMilli();
        }
    }

    // Scheduled tasks
    @Scheduled(fixedDelayString = "#{@sMSConfig.cleanupInterval}")
    public void cleanupProcessedMessages() {
        try {
            Instant cutoff = Instant.now().minusSeconds(config.getMessageRetentionSeconds());
            processedMessages.entrySet().removeIf(entry -> entry.getValue().isBefore(cutoff));
            log.debug("Cleaned up processed messages cache");
        } catch(Exception e) {
            log.error("Error cleaning up processed messages", e);
        }
    }

    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("messagesReceived", messagesReceived.get());
        metrics.put("deliveryReportsReceived", deliveryReportsReceived.get());
        metrics.put("optOutsReceived", optOutsReceived.get());
        metrics.put("keywordMessagesReceived", keywordMessagesReceived.get());
        metrics.put("optedOutNumbers", optedOutNumbers.size());
        metrics.put("keywordHandlers", keywordHandlers.size());
        return metrics;
    }

    @Override
    protected AdapterResult doSend(Object payload, Map<String, Object> headers) throws Exception {
        // SMS Inbound adapter doesn't send - it receives messages via webhook
        log.debug("Inbound adapter does not send data");
        return AdapterResult.success(null);
    }

    @Override
    protected void doSenderInitialize() throws Exception {
        // Initialize SMS provider webhook handlers
        initialize();
    }

    @Override
    protected void doSenderDestroy() throws Exception {
        // Cleanup SMS provider resources
        destroy();
    }

    @Override
    protected AdapterResult doTestConnection() throws Exception {
        // SMS Inbound adapter receives messages via webhook, so we just verify configuration
        if (config.getProvider() == null) {
            return AdapterResult.failure("No SMS provider configured", new AdapterException("No SMS provider configured"));
        }
        return AdapterResult.success(null, "SMS Inbound adapter configured for provider: " + config.getProvider());
    }

    @Override
    public AdapterResult send(Object payload, Map<String, Object> headers) {
        // SMS Inbound adapter doesn't send - it receives messages via webhook
        log.debug("SMS Inbound adapter only receives messages via webhook");
        return AdapterResult.success(null);
    }

    @PreDestroy
    public void destroy() {
        log.info("Shutting down SMS Inbound Adapter");

        messageProcessor.shutdown();
        try {
            if(!messageProcessor.awaitTermination(5, TimeUnit.SECONDS)) {
                messageProcessor.shutdownNow();
            }
        } catch(InterruptedException e) {
            messageProcessor.shutdownNow();
        }

        processedMessages.clear();
        optedOutNumbers.clear();

        log.info("SMS Inbound Adapter shut down successfully");
    }

    // Helper classes
    private static class IncomingMessage {
        private String messageId;
        private String from;
        private String to;
        private String body;
        private long timestamp;
        private MessageType type;
        private List<String> mediaUrls = new ArrayList<>();
        private int segments = 1;

        // Getters and setters
        public String getMessageId() { return messageId; }
        public void setMessageId(String messageId) { this.messageId = messageId; }
        public String getFrom() { return from; }
        public void setFrom(String from) { this.from = from; }
        public String getTo() { return to; }
        public void setTo(String to) { this.to = to; }
        public String getBody() { return body; }
        public void setBody(String body) { this.body = body; }
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
        public MessageType getType() { return type; }
        public void setType(MessageType type) { this.type = type; }
        public List<String> getMediaUrls() { return mediaUrls; }
        public void setMediaUrls(List<String> mediaUrls) { this.mediaUrls = mediaUrls; }
        public int getSegments() { return segments; }
        public void setSegments(int segments) { this.segments = segments; }
    }

    private static class DeliveryReport {
        String messageId;
        private DeliveryStatus status;
        private String errorCode;
        private String errorMessage;
        long timestamp;

        // Getters and setters
        public DeliveryStatus getStatus() { return status; }
        public void setStatus(DeliveryStatus status) { this.status = status; }
        public String getErrorCode() { return errorCode; }
        public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }

    private enum DeliveryStatus {
        PENDING, SENT, DELIVERED, FAILED, UNKNOWN
    }

    // Keyword handlers
    private interface KeywordHandler {
        void handle(IncomingMessage message, SMSInboundAdapter adapter);
    }

    private class OptOutKeywordHandler implements KeywordHandler {
        @Override
        public void handle(IncomingMessage message, SMSInboundAdapter adapter) {
            optedOutNumbers.add(message.getFrom());
            persistOptOut(message.getFrom());

            // Send confirmation if configured
            if(config.getOptOutConfirmationMessage() != null) {
                // This would trigger an outbound SMS
                log.info("Opt - out processed for: {}", message.getFrom());
            }
        }
    }

    private class OptInKeywordHandler implements KeywordHandler {
        @Override
        public void handle(IncomingMessage message, SMSInboundAdapter adapter) {
            optedOutNumbers.remove(message.getFrom());
            log.info("Opt - in processed for: {}", message.getFrom());
        }
    }

    private class HelpKeywordHandler implements KeywordHandler {
        @Override
        public void handle(IncomingMessage message, SMSInboundAdapter adapter) {
            log.info("Help request from: {}", message.getFrom());
        }
    }

    private class InfoKeywordHandler implements KeywordHandler {
        @Override
        public void handle(IncomingMessage message, SMSInboundAdapter adapter) {
            log.info("Info request from: {}", message.getFrom());
        }
    }

    private class ConfirmationKeywordHandler implements KeywordHandler {
        @Override
        public void handle(IncomingMessage message, SMSInboundAdapter adapter) {
            log.info("Confirmation from: {}", message.getFrom());
        }
    }

    private class RejectionKeywordHandler implements KeywordHandler {
        @Override
        public void handle(IncomingMessage message, SMSInboundAdapter adapter) {
            log.info("Rejection from: {}", message.getFrom());
        }
    }

    // Webhook validators
    private interface WebhookValidator {
        boolean validate(Map<String, Object> data, Map<String, String> headers);
    }

    private class TwilioWebhookValidator implements WebhookValidator {
        @Override
        public boolean validate(Map<String, Object> data, Map<String, String> headers) {
            String signature = headers.get("X - Twilio - Signature");
            if(signature == null) {
                return false;
            }

            // Implement Twilio signature validation
            // This would calculate HMAC - SHA1 signature
            return true; // Simplified for example
        }
    }

    private class VonageWebhookValidator implements WebhookValidator {
        @Override
        public boolean validate(Map<String, Object> data, Map<String, String> headers) {
            // TODO: Add isEnableSignatureValidation() to VonageConfig if needed
            // if(!config.getVonageConfig().isEnableSignatureValidation()) {
            //     return true;
            // }

            // Implement Vonage signature validation
            return true; // Simplified for example
        }
    }

    private class MessageBirdWebhookValidator implements WebhookValidator {
        @Override
        public boolean validate(Map<String, Object> data, Map<String, String> headers) {
            // TODO: Add isEnableSigning() to MessageBirdConfig if needed
            // if(!config.getMessageBirdConfig().isEnableSigning()) {
            //     return true;
            // }

            String signature = headers.get("MessageBird - Signature");
            String timestamp = headers.get("MessageBird - Request - Timestamp");

            // Implement MessageBird signature validation
            return true; // Simplified for example
        }
    }

    private class InfobipWebhookValidator implements WebhookValidator {
        @Override
        public boolean validate(Map<String, Object> data, Map<String, String> headers) {
            // Infobip doesn't use webhook signatures by default
            return true;
        }
    }
}
