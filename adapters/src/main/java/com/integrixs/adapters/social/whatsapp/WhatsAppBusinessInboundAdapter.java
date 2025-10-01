package com.integrixs.adapters.social.whatsapp;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.adapters.social.base.AbstractSocialMediaInboundAdapter;
import com.integrixs.adapters.social.whatsapp.WhatsAppBusinessApiConfig.*;
import com.integrixs.shared.dto.MessageDTO;
import com.integrixs.shared.exceptions.AdapterException;
import com.integrixs.adapters.core.AdapterResult;
import com.integrixs.adapters.domain.model.AdapterConfiguration;
import com.integrixs.shared.services.RateLimiterService;
import com.integrixs.shared.services.OAuth2TokenRefreshService;
import com.integrixs.shared.services.CredentialEncryptionService;
import com.integrixs.shared.enums.MessageStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;

@Component("whatsappBusinessInboundAdapter")
public class WhatsAppBusinessInboundAdapter extends AbstractSocialMediaInboundAdapter {
    private static final Logger log = LoggerFactory.getLogger(WhatsAppBusinessInboundAdapter.class);


    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final Map<String, LocalDateTime> messageTimestamps = new ConcurrentHashMap<>();
    private final Set<String> processedMessageIds = ConcurrentHashMap.newKeySet();

    private WhatsAppBusinessApiConfig config;
    private final RateLimiterService rateLimiterService;
    private final OAuth2TokenRefreshService tokenRefreshService;
    private final CredentialEncryptionService credentialEncryptionService;
    private volatile boolean isListening = false;

    @Autowired
    public WhatsAppBusinessInboundAdapter(
            RateLimiterService rateLimiterService,
            OAuth2TokenRefreshService tokenRefreshService,
            CredentialEncryptionService credentialEncryptionService,
            RestTemplate restTemplate,
            ObjectMapper objectMapper) {
        super();
        this.rateLimiterService = rateLimiterService;
        this.tokenRefreshService = tokenRefreshService;
        this.credentialEncryptionService = credentialEncryptionService;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public void startListening() throws AdapterException {
        if(!isConfigValid()) {
            throw new AdapterException("WhatsApp Business configuration is invalid");
        }

        log.info("Starting WhatsApp Business inbound adapter for phone: {}",
                config.getPhoneNumberId());
        isListening = true;

        // Initialize media storage if enabled
        if(config.getSettings().isSaveMediaLocally()) {
            initializeMediaStorage();
        }

        // WhatsApp primarily uses webhooks, but we can poll for status updates
        if(config.getFeatures().isEnableStatusUpdates()) {
            scheduleStatusPolling();
        }
    }

    public void stopListening() {
        log.info("Stopping WhatsApp Business inbound adapter");
        isListening = false;
    }

    protected MessageDTO processInboundData(String data, String type) {
        try {
            MessageDTO message = new MessageDTO();
            message.setCorrelationId(UUID.randomUUID().toString());
            message.setTimestamp(LocalDateTime.now());
            message.setStatus(MessageStatus.NEW);

            JsonNode dataNode = objectMapper.readTree(data);

            switch(type) {
                case "MESSAGE":
                    message = processInboundMessage(dataNode);
                    break;
                case "STATUS_UPDATE":
                    message = processStatusUpdate(dataNode);
                    break;
                case "TEMPLATE_STATUS":
                    message = processTemplateStatus(dataNode);
                    break;
                case "BUSINESS_UPDATE":
                    message = processBusinessUpdate(dataNode);
                    break;
                case "FLOW_UPDATE":
                    message = processFlowUpdate(dataNode);
                    break;
                default:
                    message.setPayload(data);
                    message.setHeaders(Map.of("type", type, "source", "whatsapp"));
            }

            return message;
        } catch(Exception e) {
            log.error("Error processing WhatsApp inbound data", e);
            MessageDTO errorMessage = new MessageDTO();
            errorMessage.setCorrelationId(UUID.randomUUID().toString());
            errorMessage.setTimestamp(LocalDateTime.now());
            errorMessage.setPayload("Error: " + e.getMessage());
            errorMessage.setHeaders(Map.of("error", "true", "errorMessage", e.getMessage()));
            return errorMessage;
        }
    }

    public MessageDTO processWebhookData(Map<String, Object> webhookData) {
        try {
            String object = (String) webhookData.get("object");
            if(!"whatsapp_business_account".equals(object)) {
                log.warn("Received webhook for non - WhatsApp object: {}", object);
                return null;
            }

            List<Map<String, Object>> entries = (List<Map<String, Object>>) webhookData.get("entry");
            if(entries == null || entries.isEmpty()) {
                return null;
            }

            for(Map<String, Object> entry : entries) {
                String entryId = (String) entry.get("id");
                List<Map<String, Object>> changes = (List<Map<String, Object>>) entry.get("changes");

                for(Map<String, Object> change : changes) {
                    String field = (String) change.get("field");
                    Map<String, Object> value = (Map<String, Object>) change.get("value");

                    if("messages".equals(field)) {
                        return processWebhookMessages(value);
                    } else if("statuses".equals(field)) {
                        return processWebhookStatuses(value);
                    } else if("business_profile".equals(field)) {
                        return processWebhookBusinessProfile(value);
                    }
                }
            }

            return null;
        } catch(Exception e) {
            log.error("Error processing WhatsApp webhook", e);
            return null;
        }
    }

    private MessageDTO processWebhookMessages(Map<String, Object> value) {
        List<Map<String, Object>> messages = (List<Map<String, Object>>) value.get("messages");
        if(messages == null || messages.isEmpty()) {
            return null;
        }

        Map<String, Object> messageData = messages.get(0);
        String messageId = (String) messageData.get("id");

        // Check if already processed
        if(processedMessageIds.contains(messageId)) {
            log.debug("MessageDTO {} already processed", messageId);
            return null;
        }

        MessageDTO message = new MessageDTO();
        message.setCorrelationId(messageId);
        message.setMessageTimestamp(Instant.now());
        message.setStatus(MessageStatus.NEW);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "INBOUND_MESSAGE");
        headers.put("source", "whatsapp");
        headers.put("from", messageData.get("from"));
        headers.put("message_type", messageData.get("type"));

        // Handle different message types
        String msgType = (String) messageData.get("type");
        switch(msgType) {
            case "text":
                headers.put("text", ((Map<String, Object>) messageData.get("text")).get("body"));
                break;
            case "image":
            case "audio":
            case "video":
            case "document":
                Map<String, Object> media = (Map<String, Object>) messageData.get(msgType);
                headers.put("media_id", media.get("id"));
                headers.put("mime_type", media.get("mime_type"));
                if(media.containsKey("caption")) {
                    headers.put("caption", media.get("caption"));
                }
                if(config.getSettings().isAutoDownloadMedia()) {
                    downloadAndSaveMedia((String) media.get("id"), msgType);
                }
                break;
            case "location":
                Map<String, Object> location = (Map<String, Object>) messageData.get("location");
                headers.put("latitude", location.get("latitude"));
                headers.put("longitude", location.get("longitude"));
                headers.put("location_name", location.get("name"));
                headers.put("location_address", location.get("address"));
                break;
            case "contacts":
                headers.put("contacts", messageData.get("contacts"));
                break;
            case "interactive":
                Map<String, Object> interactive = (Map<String, Object>) messageData.get("interactive");
                headers.put("interactive_type", interactive.get("type"));
                headers.put("interactive_reply", interactive.get("reply"));
                break;
            case "button":
                Map<String, Object> button = (Map<String, Object>) messageData.get("button");
                headers.put("button_payload", button.get("payload"));
                headers.put("button_text", button.get("text"));
                break;
        }

        // Handle context(replies)
        if(messageData.containsKey("context")) {
            Map<String, Object> context = (Map<String, Object>) messageData.get("context");
            headers.put("reply_to", context.get("message_id"));
        }

        message.setHeaders(headers);
        message.setPayload(objectMapper.valueToTree(messageData).toString());

        processedMessageIds.add(messageId);
        messageTimestamps.put(messageId, LocalDateTime.now());

        return message;
    }

    private MessageDTO processWebhookStatuses(Map<String, Object> value) {
        List<Map<String, Object>> statuses = (List<Map<String, Object>>) value.get("statuses");
        if(statuses == null || statuses.isEmpty()) {
            return null;
        }

        Map<String, Object> status = statuses.get(0);

        MessageDTO message = new MessageDTO();
        message.setCorrelationId(UUID.randomUUID().toString());
        message.setMessageTimestamp(Instant.now());
        message.setStatus(MessageStatus.NEW);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "STATUS_UPDATE");
        headers.put("source", "whatsapp");
        headers.put("message_id", status.get("id"));
        headers.put("recipient", status.get("recipient_id"));
        headers.put("status", status.get("status"));
        headers.put("timestamp", status.get("timestamp"));

        if(status.containsKey("errors")) {
            headers.put("errors", status.get("errors"));
        }

        message.setHeaders(headers);
        message.setPayload(objectMapper.valueToTree(status).toString());

        return message;
    }

    private MessageDTO processWebhookBusinessProfile(Map<String, Object> value) {
        MessageDTO message = new MessageDTO();
        message.setCorrelationId(UUID.randomUUID().toString());
        message.setMessageTimestamp(Instant.now());
        message.setStatus(MessageStatus.NEW);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "BUSINESS_PROFILE_UPDATE");
        headers.put("source", "whatsapp");

        message.setHeaders(headers);
        message.setPayload(objectMapper.valueToTree(value).toString());

        return message;
    }

    private MessageDTO processInboundMessage(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setCorrelationId(data.path("id").asText());
        message.setMessageTimestamp(Instant.now());
        message.setStatus(MessageStatus.NEW);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "MESSAGE");
        headers.put("source", "whatsapp");
        headers.put("from", data.path("from").asText());
        headers.put("message_type", data.path("type").asText());

        message.setHeaders(headers);
        message.setPayload(data.toString());

        return message;
    }

    private MessageDTO processStatusUpdate(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setCorrelationId(UUID.randomUUID().toString());
        message.setMessageTimestamp(Instant.now());
        message.setStatus(MessageStatus.NEW);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "STATUS_UPDATE");
        headers.put("source", "whatsapp");
        headers.put("message_id", data.path("id").asText());
        headers.put("delivery_status", data.path("status").asText());

        message.setHeaders(headers);
        message.setPayload(data.toString());

        return message;
    }

    private MessageDTO processTemplateStatus(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setCorrelationId(UUID.randomUUID().toString());
        message.setMessageTimestamp(Instant.now());
        message.setStatus(MessageStatus.NEW);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "TEMPLATE_STATUS");
        headers.put("source", "whatsapp");
        headers.put("template_name", data.path("template_name").asText());
        headers.put("template_status", data.path("status").asText());

        message.setHeaders(headers);
        message.setPayload(data.toString());

        return message;
    }

    private MessageDTO processBusinessUpdate(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setCorrelationId(UUID.randomUUID().toString());
        message.setMessageTimestamp(Instant.now());
        message.setStatus(MessageStatus.NEW);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "BUSINESS_UPDATE");
        headers.put("source", "whatsapp");
        headers.put("update_type", data.path("update_type").asText());

        message.setHeaders(headers);
        message.setPayload(data.toString());

        return message;
    }

    private MessageDTO processFlowUpdate(JsonNode data) {
        MessageDTO message = new MessageDTO();
        message.setCorrelationId(UUID.randomUUID().toString());
        message.setMessageTimestamp(Instant.now());
        message.setStatus(MessageStatus.NEW);

        Map<String, Object> headers = new HashMap<>();
        headers.put("type", "FLOW_UPDATE");
        headers.put("source", "whatsapp");
        headers.put("flow_id", data.path("flow_id").asText());
        headers.put("flow_status", data.path("status").asText());

        message.setHeaders(headers);
        message.setPayload(data.toString());

        return message;
    }

    private void downloadAndSaveMedia(String mediaId, String mediaType) {
        try {
            // Get media URL
            String url = String.format("%s/%s/%s",
                config.getApiBaseUrl(),
                config.getApiVersion(),
                mediaId);

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(getAccessToken());
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, String.class);

            if(response.getStatusCode().is2xxSuccessful()) {
                JsonNode mediaInfo = objectMapper.readTree(response.getBody());
                String mediaUrl = mediaInfo.path("url").asText();

                // Download media
                HttpEntity<String> downloadEntity = new HttpEntity<>(headers);
                ResponseEntity<byte[]> mediaResponse = restTemplate.exchange(
                    mediaUrl, HttpMethod.GET, downloadEntity, byte[].class);

                if(mediaResponse.getStatusCode().is2xxSuccessful() && config.getSettings().isSaveMediaLocally()) {
                    saveMediaToFile(mediaId, mediaType, mediaResponse.getBody());
                }
            }
        } catch(Exception e) {
            log.error("Error downloading media {}", mediaId, e);
        }
    }

    private void saveMediaToFile(String mediaId, String mediaType, byte[] data) {
        try {
            Path mediaDir = Paths.get(config.getSettings().getMediaStoragePath(), mediaType);
            Files.createDirectories(mediaDir);

            String extension = getFileExtension(mediaType);
            Path filePath = mediaDir.resolve(mediaId + extension);
            Files.write(filePath, data);

            log.info("Saved {} media to {}", mediaType, filePath);
        } catch(IOException e) {
            log.error("Error saving media to file", e);
        }
    }

    private String getFileExtension(String mediaType) {
        switch(mediaType) {
            case "image":
                return ".jpg";
            case "audio":
                return ".ogg";
            case "video":
                return ".mp4";
            case "document":
                return ".pdf";
            default:
                return "";
        }
    }

    private void initializeMediaStorage() {
        try {
            Path mediaPath = Paths.get(config.getSettings().getMediaStoragePath());
            Files.createDirectories(mediaPath);
            log.info("Initialized media storage at {}", mediaPath);
        } catch(IOException e) {
            log.error("Failed to initialize media storage", e);
        }
    }

    @Scheduled(fixedDelayString = "${integrixs.adapters.whatsapp.business.status-polling-interval:60000}")
    private void pollMessageStatuses() {
        if(!isListening) return;

        // Clean up old processed message IDs(older than 24 hours)
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(24);
        messageTimestamps.entrySet().removeIf(entry -> entry.getValue().isBefore(cutoffTime));
        processedMessageIds.removeIf(id -> !messageTimestamps.containsKey(id));
    }

    private void scheduleStatusPolling() {
        log.info("Scheduled status polling for WhatsApp Business");
    }

    private String getAccessToken() {
        String encryptedToken = config.getSystemUserAccessToken() != null ?
            config.getSystemUserAccessToken() : config.getAccessToken();
        return credentialEncryptionService.decrypt(encryptedToken);
    }

    private boolean isConfigValid() {
        return config != null
            && config.getPhoneNumberId() != null
            && (config.getSystemUserAccessToken() != null || config.getAccessToken() != null);
    }

    // Verify webhook challenge(required by WhatsApp)
    public String verifyWebhookChallenge(String mode, String token, String challenge) {
        if("subscribe".equals(mode) && config.getVerifyToken().equals(token)) {
            log.info("WhatsApp webhook verified successfully");
            return challenge;
        }
        log.warn("WhatsApp webhook verification failed");
        return null;
    }

    public void setConfiguration(WhatsAppBusinessApiConfig config) {
        this.config = config;
    }

    // Abstract methods implementation
    @Override
    protected List<String> getSupportedEventTypes() {
        return Arrays.asList(
            "MESSAGE",
            "STATUS_UPDATE",
            "TEMPLATE_STATUS",
            "BUSINESS_UPDATE",
            "FLOW_UPDATE"
        );
    }

    @Override
    protected Map<String, Object> getConfig() {
        Map<String, Object> configMap = new HashMap<>();
        if (config != null) {
            configMap.put("phoneNumberId", config.getPhoneNumberId());
            configMap.put("businessAccountId", config.getBusinessAccountId());
            configMap.put("verifyToken", config.getVerifyToken());
            configMap.put("apiVersion", config.getApiVersion());
            configMap.put("apiBaseUrl", config.getApiBaseUrl());
        }
        return configMap;
    }

    @Override
    public Map<String, Object> getAdapterConfig() {
        return getConfig();
    }

    @Override
    public AdapterConfiguration.AdapterTypeEnum getAdapterType() {
        return AdapterConfiguration.AdapterTypeEnum.WHATSAPP;
    }

    @Override
    protected void doInitialize() {
        // Initialization logic if needed
        if (config != null) {
            log.info("WhatsApp Business adapter initialized with phone number: {}",
                config.getPhoneNumberId());
        }
    }

    @Override
    protected void doDestroy() {
        // Cleanup logic if needed
        stopListening();
        processedMessageIds.clear();
        messageTimestamps.clear();
    }

    @Override
    protected AdapterResult doSend(Object payload, Map<String, Object> headers) throws Exception {
        // This is an inbound adapter, sending is not supported
        log.debug("Inbound adapter does not send data");
        return AdapterResult.success(null);
    }

    @Override
    protected AdapterResult doTestConnection() throws Exception {
        if (!isConfigValid()) {
            return AdapterResult.failure("Invalid configuration", null);
        }

        try {
            // Test API connection
            String url = String.format("%s/%s/me",
                config.getApiBaseUrl(),
                config.getApiVersion());

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(getAccessToken());
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return AdapterResult.success(null, "Connection successful");
            } else {
                return AdapterResult.failure("Connection failed with status: " + response.getStatusCode(), null);
            }
        } catch (Exception e) {
            return AdapterResult.failure("Connection test failed: " + e.getMessage(), e);
        }
    }

    @Override
    public String getConfigurationSummary() {
        if (config != null) {
            return String.format("WhatsApp Business: %s", config.getPhoneNumberId());
        }
        return "WhatsApp Business: Not configured";
    }

    @Override
    protected void doSenderInitialize() throws Exception {
        // Not used for inbound adapter
    }

    @Override
    protected void doSenderDestroy() throws Exception {
        // Not used for inbound adapter
    }

    @Override
    public AdapterResult send(Object payload, Map<String, Object> headers) throws AdapterException {
        // This is an inbound adapter, sending is not supported
        log.debug("Inbound adapter does not send data");
        return AdapterResult.success(null);
    }

    protected long getPollingIntervalMs() {
        // Return polling interval from configuration or default
        return config != null && config.getStatusPollingInterval() != null
            ? config.getStatusPollingInterval()
            : 60000L;
    }
}
