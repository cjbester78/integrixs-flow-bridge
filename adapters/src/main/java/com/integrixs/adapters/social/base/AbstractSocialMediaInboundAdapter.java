package com.integrixs.adapters.social.base;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.adapters.core.AbstractInboundAdapter;
import com.integrixs.adapters.domain.model.AdapterConfiguration;
import com.integrixs.shared.dto.MessageDTO;
import java.util.List;
import java.util.Map;

/**
 * Abstract base class for social media inbound adapters.
 * Provides common functionality for polling social media platforms.
 */
public abstract class AbstractSocialMediaInboundAdapter extends AbstractInboundAdapter {
    private static final Logger log = LoggerFactory.getLogger(AbstractSocialMediaInboundAdapter.class);


    protected AbstractSocialMediaInboundAdapter() {
        super(AdapterConfiguration.AdapterTypeEnum.REST);
    }

    /**
     * Get the supported event types for this social media adapter
     */
    protected abstract List<String> getSupportedEventTypes();

    /**
     * Get the adapter - specific configuration
     */
    protected abstract Map<String, Object> getConfig();

    /**
     * Get the adapter configuration
     */
    public abstract Map<String, Object> getAdapterConfig();

    /**
     * Process webhook events from social media platforms
     */
    public void processWebhookEvent(Map<String, Object> event) {
        log.debug("Processing webhook event for adapter: {}", getAdapterType());
        // Default implementation - can be overridden by specific adapters
    }

    /**
     * Verify webhook signature for security
     */
    public boolean verifyWebhookSignature(String signature, String payload) {
        log.debug("Verifying webhook signature for adapter: {}", getAdapterType());
        // Default implementation - should be overridden by specific adapters
        return true;
    }

    /**
     * Get the adapter type identifier
     */
    public abstract AdapterConfiguration.AdapterTypeEnum getAdapterType();

    /**
     * Convert social media data to MessageDTO
     */
    protected MessageDTO convertToMessage(Map<String, Object> data, String eventType) {
        MessageDTO message = new MessageDTO();
        message.setHeaders(Map.of(
            "eventType", eventType,
            "adapterType", getAdapterType().name(),
            "timestamp", System.currentTimeMillis()
       ));
        if(data != null) {
            try {
                message.setPayload(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(data));
            } catch(com.fasterxml.jackson.core.JsonProcessingException e) {
                message.setPayload(data.toString());
            }
        }
        return message;
    }

    /**
     * Common error handling for social media API calls
     */
    protected void handleApiError(Exception e, String operation) {
        log.error("Error in {} operation for adapter {}: {}",
            operation, getAdapterType(), e.getMessage(), e);
    }
}
