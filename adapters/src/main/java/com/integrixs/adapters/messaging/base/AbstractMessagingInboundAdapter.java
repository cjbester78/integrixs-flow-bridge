package com.integrixs.adapters.messaging.base;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.adapters.core.AbstractInboundAdapter;
import com.integrixs.adapters.domain.model.AdapterConfiguration;
import com.integrixs.shared.dto.MessageDTO;
import java.util.Map;

/**
 * Abstract base class for messaging inbound adapters.
 * Provides common functionality for receiving messages from messaging systems.
 */
public abstract class AbstractMessagingInboundAdapter extends AbstractInboundAdapter {
    private static final Logger log = LoggerFactory.getLogger(AbstractMessagingInboundAdapter.class);


    protected AbstractMessagingInboundAdapter() {
        super(AdapterConfiguration.AdapterTypeEnum.IBMMQ); // Default to IBMMQ, subclasses should override
    }

    /**
     * Connect to the messaging system
     */
    protected abstract void connect() throws Exception;

    /**
     * Disconnect from the messaging system
     */
    protected abstract void disconnect() throws Exception;

    /**
     * Subscribe to a queue or topic
     */
    protected abstract void subscribe(String destination) throws Exception;

    /**
     * Unsubscribe from a queue or topic
     */
    protected abstract void unsubscribe(String destination) throws Exception;

    /**
     * Process an incoming message
     */
    protected abstract void processMessage(Object message) throws Exception;

    /**
     * Convert messaging system message to MessageDTO
     */
    protected MessageDTO convertToMessageDTO(String messageId, Map<String, Object> headers, Object payload) {
        MessageDTO messageDTO = new MessageDTO();
        messageDTO.setCorrelationId(messageId);
        messageDTO.setHeaders(headers);
        messageDTO.setPayload(payload != null ? payload.toString() : null);
        return messageDTO;
    }

    /**
     * Acknowledge message processing
     */
    protected abstract void acknowledgeMessage(Object message) throws Exception;

    /**
     * Handle connection errors
     */
    protected void handleConnectionError(Exception e) {
        log.error("Connection error in {} adapter: {}", getAdapterType(), e.getMessage(), e);
        // Implement reconnection logic
    }

    /**
     * Get the adapter type identifier
     */
    public abstract AdapterConfiguration.AdapterTypeEnum getAdapterType();
}
