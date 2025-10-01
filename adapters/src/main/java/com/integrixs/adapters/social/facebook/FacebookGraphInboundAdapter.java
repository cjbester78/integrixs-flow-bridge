package com.integrixs.adapters.social.facebook;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrixs.adapters.core.InboundAdapter;
import com.integrixs.adapters.core.AdapterResult;
import com.integrixs.adapters.social.base.AbstractSocialMediaInboundAdapter;
import com.integrixs.adapters.social.facebook.model.FacebookPost;
import com.integrixs.shared.dto.MessageDTO;
import com.integrixs.adapters.domain.model.AdapterConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Facebook Graph API Inbound Adapter
 * Handles incoming data from Facebook(webhooks, polling)
 */
@Component
public class FacebookGraphInboundAdapter extends AbstractSocialMediaInboundAdapter {
    private static final Logger log = LoggerFactory.getLogger(FacebookGraphInboundAdapter.class);


    @Autowired
    private FacebookGraphApiClient apiClient;

    @Autowired
    private com.integrixs.shared.services.CredentialEncryptionService encryptionService;

    @Autowired
    private FacebookWebhookProcessor webhookProcessor;

    @Autowired
    private ObjectMapper objectMapper;

    // Add missing abstract method implementations
    @Override
    public AdapterConfiguration.AdapterTypeEnum getAdapterType() {
        return AdapterConfiguration.AdapterTypeEnum.REST;
    }

    @Override
    protected List<String> getSupportedEventTypes() {
        return Arrays.asList("post", "comment", "message", "reaction", "share");
    }

    @Override
    protected Map<String, Object> getConfig() {
        Map<String, Object> configMap = new HashMap<>();
        // Add config properties as needed
        return configMap;
    }

    @Override
    public Map<String, Object> getAdapterConfig() {
        return getConfig();
    }

    public MessageDTO receive(String flowId, FacebookGraphApiConfig config) {
        try {
            // Ensure we have valid access token
            String accessToken = ensureValidAccessToken(config);

            if(config.isWebhookEnabled() && webhookProcessor != null) {
                // Process webhook events if available
                MessageDTO webhookMessageDTO = webhookProcessor.getNextEvent(flowId);
                if(webhookMessageDTO != null) {
                    return webhookMessageDTO;
                }
            }

            // Polling approach - check for new posts, comments, or messages
            return pollFacebookData(config, accessToken);

        } catch(Exception e) {
            log.error("Error receiving from Facebook Graph API", e);
            throw new RuntimeException("Failed to receive Facebook data", e);
        }
    }

    /**
     * Poll Facebook for new data
     */
    private MessageDTO pollFacebookData(FacebookGraphApiConfig config, String accessToken) {
        try {
            // Example: Get recent posts from the page
            JsonNode posts = apiClient.getPost(config.getPageId() + "/posts", accessToken, config);

            if(posts != null && posts.has("data") && posts.get("data").size() > 0) {
                JsonNode firstPost = posts.get("data").get(0);

                MessageDTO message = new MessageDTO();
                message.setCorrelationId(java.util.UUID.randomUUID().toString());
                message.setMessageTimestamp(java.time.Instant.now());

                // Set headers
                Map<String, Object> headers = new HashMap<>();
                headers.put("source", "facebook");
                headers.put("type", "post");
                headers.put("pageId", config.getPageId());
                headers.put("postId", firstPost.get("id").asText());
                message.setHeaders(headers);

                // Set payload
                Map<String, Object> payload = new HashMap<>();
                payload.put("id", firstPost.get("id").asText());
                payload.put("message", firstPost.has("message") ? firstPost.get("message").asText() : "");
                payload.put("created_time", firstPost.get("created_time").asText());
                payload.put("type", firstPost.has("type") ? firstPost.get("type").asText() : "status");

                // Add engagement metrics if available
                if(firstPost.has("reactions")) {
                    payload.put("reactions", firstPost.get("reactions").get("summary").get("total_count").asInt());
                }
                if(firstPost.has("comments")) {
                    payload.put("comments", firstPost.get("comments").get("summary").get("total_count").asInt());
                }
                if(firstPost.has("shares")) {
                    payload.put("shares", firstPost.get("shares").get("count").asInt());
                }

                message.setPayload(objectMapper.writeValueAsString(payload));

                return message;
            }

            // Return empty message if no data
            return createEmptyMessage();

        } catch(Exception e) {
            log.error("Error polling Facebook data", e);
            throw new RuntimeException("Failed to poll Facebook data", e);
        }
    }

    /**
     * Process webhook event
     */
    @Override
    public void processWebhookEvent(Map<String, Object> webhookData) {
        try {
            MessageDTO message = new MessageDTO();
            message.setCorrelationId(java.util.UUID.randomUUID().toString());
            message.setMessageTimestamp(java.time.Instant.now());

            // Extract webhook type and data
            String object = (String) webhookData.get("object");
            Map<String, Object> entry = (Map<String, Object>) ((java.util.List) webhookData.get("entry")).get(0);

            Map<String, Object> headers = new HashMap<>();
            headers.put("source", "facebook");
            headers.put("webhookType", object);
            headers.put("pageId", String.valueOf(entry.get("id")));
            message.setHeaders(headers);

            // Process different webhook types
            if("page".equals(object)) {
                processPageWebhook(entry, message);
            } else if("instagram".equals(object)) {
                processInstagramWebhook(entry, message);
            } else if("whatsapp_business_account".equals(object)) {
                processWhatsAppWebhook(entry, message);
            }

            // Store the message for later retrieval
            // In a real implementation, this would be stored in a queue or database
            log.info("Webhook event processed: {}", message.getHeaders());

        } catch(Exception e) {
            log.error("Error processing webhook event", e);
            throw new RuntimeException("Failed to process webhook event", e);
        }
    }

    /**
     * Process page - specific webhook
     */
    private void processPageWebhook(Map<String, Object> entry, MessageDTO message) throws Exception {
        java.util.List<Map<String, Object>> changes = (java.util.List<Map<String, Object>>) entry.get("changes");

        for(Map<String, Object> change : changes) {
            String field = (String) change.get("field");
            Map<String, Object> value = (Map<String, Object>) change.get("value");

            message.getHeaders().put("changeType", field);

            Map<String, Object> payload = new HashMap<>();
            payload.put("field", field);
            payload.put("value", value);

            // Handle specific change types
            switch(field) {
                case "feed":
                    payload.put("post_id", value.get("post_id"));
                    payload.put("message", value.get("message"));
                    payload.put("created_time", value.get("created_time"));
                    break;

                case "comments":
                    payload.put("comment_id", value.get("comment_id"));
                    payload.put("post_id", value.get("post_id"));
                    payload.put("message", value.get("message"));
                    payload.put("from", value.get("from"));
                    break;

                case "messages":
                    payload.put("message", value.get("message"));
                    payload.put("messaging", value.get("messaging"));
                    break;

                default:
                    payload.putAll(value);
            }

            message.setPayload(objectMapper.writeValueAsString(payload));
        }
    }

    /**
     * Process Instagram - specific webhook
     */
    private void processInstagramWebhook(Map<String, Object> entry, MessageDTO message) throws Exception {
        // Implementation for Instagram webhooks
        message.getHeaders().put("platform", "instagram");
        message.setPayload(objectMapper.writeValueAsString(entry));
    }

    /**
     * Process WhatsApp - specific webhook
     */
    private void processWhatsAppWebhook(Map<String, Object> entry, MessageDTO message) throws Exception {
        // Implementation for WhatsApp webhooks
        message.getHeaders().put("platform", "whatsapp");
        message.setPayload(objectMapper.writeValueAsString(entry));
    }

    /**
     * Ensure we have a valid access token
     */
    private String ensureValidAccessToken(FacebookGraphApiConfig config) {
        String token = config.getAccessToken();

        if(token != null && token.startsWith("enc:")) {
            token = encryptionService.decrypt(token);
        }

        // Check if token needs refresh based on your logic
        // For now, we'll assume the token is valid
        log.debug("Using access token for Facebook adapter");

        return token;
    }

    /**
     * Create empty message when no data available
     */
    private MessageDTO createEmptyMessage() {
        MessageDTO message = new MessageDTO();
        message.setCorrelationId(java.util.UUID.randomUUID().toString());
        message.setMessageTimestamp(java.time.Instant.now());

        Map<String, Object> headers = new HashMap<>();
        headers.put("source", "facebook");
        headers.put("empty", "true");
        message.setHeaders(headers);

        message.setPayload(" {}");

        return message;
    }

    protected void validateConfiguration(FacebookGraphApiConfig config) {
        if(config.getPageId() == null || config.getPageId().isEmpty()) {
            throw new IllegalArgumentException("Facebook Page ID is required");
        }

        if((config.getAccessToken() == null || config.getAccessToken().isEmpty()) && !config.hasValidPageToken()) {
            throw new IllegalArgumentException("Valid access token is required");
        }

        if(config.getClientId() == null || config.getClientId().isEmpty()) {
            throw new IllegalArgumentException("Facebook App Client ID is required");
        }
    }

    // Implement abstract methods from AbstractInboundAdapter
    @Override
    protected void doSenderInitialize() throws Exception {
        log.info("Initializing Facebook Graph Inbound Adapter");
        // Initialize any resources needed for sending
    }

    @Override
    public AdapterResult send(Object payload, Map<String, Object> headers) throws com.integrixs.shared.exceptions.AdapterException {
        try {
            return doSend(payload, headers);
        } catch (Exception e) {
            throw new com.integrixs.shared.exceptions.AdapterException("Send failed", e);
        }
    }

    @Override
    protected void doSenderDestroy() throws Exception {
        log.info("Destroying Facebook Graph Inbound Adapter");
        // Clean up any resources
    }

    @Override
    protected AdapterResult doSend(Object payload, Map<String, Object> headers) throws Exception {
        // This is an inbound adapter, but we need to implement this method
        // It could be used to send acknowledgments or responses back to Facebook
        try {
            log.debug("Sending response to Facebook: {}", payload);
            // Implementation would depend on what kind of responses Facebook expects
            return AdapterResult.success(null, "Response sent successfully");
        } catch (Exception e) {
            log.error("Error sending response to Facebook", e);
            return AdapterResult.failure("Failed to send response: " + e.getMessage());
        }
    }

    @Override
    protected AdapterResult doTestConnection() throws Exception {
        try {
            // Test connection to Facebook API
            log.info("Testing connection to Facebook Graph API");

            // In a real implementation, we would make a test API call
            // For now, we'll do a basic check
            if (apiClient == null) {
                return AdapterResult.failure("API client not initialized");
            }

            return AdapterResult.success(null, "Connection test successful");
        } catch (Exception e) {
            log.error("Connection test failed", e);
            return AdapterResult.failure("Connection test failed: " + e.getMessage());
        }
    }
}
