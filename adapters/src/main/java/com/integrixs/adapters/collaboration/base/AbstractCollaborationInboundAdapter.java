package com.integrixs.adapters.collaboration.base;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.adapters.core.AbstractInboundAdapter;
import com.integrixs.adapters.domain.model.AdapterConfiguration;
import com.integrixs.shared.dto.MessageDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

/**
 * Abstract base class for collaboration platform inbound adapters.
 * Provides common functionality for receiving messages from collaboration tools like Slack, Teams, etc.
 */
public abstract class AbstractCollaborationInboundAdapter extends AbstractInboundAdapter {
    private static final Logger log = LoggerFactory.getLogger(AbstractCollaborationInboundAdapter.class);

    protected final ObjectMapper objectMapper = new ObjectMapper();


    protected AbstractCollaborationInboundAdapter() {
        super(AdapterConfiguration.AdapterTypeEnum.REST);
    }

    /**
     * Process incoming webhook from collaboration platform
     */
    public abstract void processWebhook(Map<String, Object> webhookData);

    /**
     * Verify webhook signature for security
     */
    public abstract boolean verifyWebhookSignature(String signature, String timestamp, String body);

    /**
     * Poll for new messages(if webhook is not available)
     */
    protected abstract void pollMessages();

    /**
     * Convert collaboration message to MessageDTO
     */
    protected MessageDTO convertToMessage(String channelId, String userId, String text, Map<String, Object> metadata) {
        MessageDTO message = new MessageDTO();
        message.setHeaders(Map.of(
            "channelId", channelId,
            "userId", userId,
            "adapterType", getAdapterType().name(),
            "timestamp", System.currentTimeMillis()
       ));
        try {
            message.setPayload(objectMapper.writeValueAsString(Map.of(
                "text", text,
                "metadata", metadata
            )));
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize message payload", e);
        }
        return message;
    }

    /**
     * Get the adapter type identifier
     */
    public abstract AdapterConfiguration.AdapterTypeEnum getAdapterType();

    /**
     * Handle rate limiting from collaboration platform
     */
    protected void handleRateLimit(int retryAfterSeconds) {
        log.warn("Rate limited by {} platform. Retry after {} seconds", getAdapterType(), retryAfterSeconds);
        try {
            Thread.sleep(retryAfterSeconds * 1000L);
        } catch(InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while waiting for rate limit", e);
        }
    }
}
