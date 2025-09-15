package com.integrixs.adapters.collaboration.base;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.adapters.core.AbstractOutboundAdapter;
import com.integrixs.adapters.domain.model.AdapterConfiguration;
import com.integrixs.shared.dto.MessageDTO;
import com.integrixs.shared.enums.MessageStatus;
import java.util.Map;

/**
 * Abstract base class for collaboration platform outbound adapters.
 * Provides common functionality for sending messages to collaboration tools like Slack, Teams, etc.
 */
public abstract class AbstractCollaborationOutboundAdapter extends AbstractOutboundAdapter {
    private static final Logger log = LoggerFactory.getLogger(AbstractCollaborationOutboundAdapter.class);


    protected AbstractCollaborationOutboundAdapter() {
        super(AdapterConfiguration.AdapterTypeEnum.REST);
    }

    /**
     * Send a message to the collaboration platform
     */
    public abstract MessageDTO sendMessage(String channelId, String text, Map<String, Object> options);

    /**
     * Send a file to the collaboration platform
     */
    public abstract MessageDTO sendFile(String channelId, byte[] fileContent, String fileName, String comment);

    /**
     * Update an existing message
     */
    public abstract MessageDTO updateMessage(String channelId, String messageId, String newText);

    /**
     * Delete a message
     */
    public abstract MessageDTO deleteMessage(String channelId, String messageId);

    /**
     * React to a message
     */
    public abstract MessageDTO addReaction(String channelId, String messageId, String reaction);

    /**
     * Create a success response
     */
    protected MessageDTO createSuccessResponse(String messageId, Map<String, Object> data) {
        MessageDTO response = new MessageDTO();
        response.setCorrelationId(messageId);
        response.setStatus(MessageStatus.SUCCESS);
        response.setHeaders(Map.of(
            "adapterType", getAdapterType().name(),
            "timestamp", System.currentTimeMillis()
       ));
        if(data != null) {
            try {
                response.setPayload(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(data));
            } catch(com.fasterxml.jackson.core.JsonProcessingException e) {
                response.setPayload(data.toString());
            }
        }
        return response;
    }

    /**
     * Create an error response
     */
    protected MessageDTO createErrorResponse(String messageId, String error) {
        MessageDTO response = new MessageDTO();
        response.setCorrelationId(messageId);
        response.setStatus(MessageStatus.FAILED);
        response.setHeaders(Map.of(
            "adapterType", getAdapterType().name(),
            "error", error,
            "timestamp", System.currentTimeMillis()
       ));
        return response;
    }

    /**
     * Get the adapter type identifier
     */
    public abstract AdapterConfiguration.AdapterTypeEnum getAdapterType();

    /**
     * Format message for the specific platform
     */
    protected abstract String formatMessage(String text, Map<String, Object> formatting);
}
