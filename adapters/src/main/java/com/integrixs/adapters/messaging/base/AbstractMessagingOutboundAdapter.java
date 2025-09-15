package com.integrixs.adapters.messaging.base;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.adapters.core.AbstractOutboundAdapter;
import com.integrixs.adapters.domain.model.AdapterConfiguration;
import com.integrixs.shared.dto.MessageDTO;
import com.integrixs.shared.enums.MessageStatus;
import java.util.Map;

/**
 * Abstract base class for messaging outbound adapters.
 * Provides common functionality for sending messages to messaging systems.
 */
public abstract class AbstractMessagingOutboundAdapter extends AbstractOutboundAdapter {
    private static final Logger log = LoggerFactory.getLogger(AbstractMessagingOutboundAdapter.class);


    protected AbstractMessagingOutboundAdapter() {
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
     * Send a message to a destination
     */
    protected abstract void sendMessage(String destination, MessageDTO message) throws Exception;

    /**
     * Send a message with specific properties
     */
    protected abstract void sendMessage(String destination, MessageDTO message, Map<String, Object> properties) throws Exception;

    /**
     * Create a connection configuration
     */
    protected Map<String, Object> createConnectionConfig(Map<String, Object> adapterConfig) {
        return Map.of(
            "host", adapterConfig.getOrDefault("host", "localhost"),
            "port", adapterConfig.getOrDefault("port", 5672),
            "username", adapterConfig.getOrDefault("username", "guest"),
            "password", adapterConfig.getOrDefault("password", "guest"),
            "virtualHost", adapterConfig.getOrDefault("virtualHost", "/")
       );
    }

    /**
     * Create a success response
     */
    protected MessageDTO createSuccessResponse(MessageDTO originalMessage) {
        MessageDTO response = new MessageDTO();
        response.setCorrelationId(originalMessage.getCorrelationId());
        response.setStatus(MessageStatus.SUCCESS);
        response.setHeaders(Map.of(
            "adapterType", getAdapterType().name(),
            "timestamp", System.currentTimeMillis()
       ));
        return response;
    }

    /**
     * Create an error response
     */
    protected MessageDTO createErrorResponse(MessageDTO originalMessage, String error) {
        MessageDTO response = new MessageDTO();
        response.setCorrelationId(originalMessage.getCorrelationId());
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
     * Validate connection configuration
     */
    protected void validateConnectionConfig(Map<String, Object> config) {
        if(!config.containsKey("host")) {
            throw new IllegalArgumentException("Host is required");
        }
        if(!config.containsKey("port")) {
            throw new IllegalArgumentException("Port is required");
        }
    }
}
