package com.integrixs.webclient;

import com.integrixs.adapters.domain.model.AdapterOperationResult;
import com.integrixs.adapters.domain.port.OutboundAdapterPort;
import com.integrixs.adapters.factory.AdapterFactoryManager;
import com.integrixs.adapters.domain.model.SendRequest;
import com.integrixs.adapters.domain.model.AdapterConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service for processing inbound messages through the adapter framework.
 * Handles routing, transformation, and validation of incoming messages.
 */
@Service
public class InboundMessageProcessor {

    private static final Logger logger = LoggerFactory.getLogger(InboundMessageProcessor.class);
    private final AdapterFactoryManager adapterFactory;

    public InboundMessageProcessor() {
        this.adapterFactory = AdapterFactoryManager.getInstance();
    }

    /**
     * Process inbound message using the specified adapter type and configuration
     */
    public AdapterOperationResult processMessage(AdapterConfiguration.AdapterTypeEnum adapterType, Object configuration, Object payload) {
        logger.debug("Processing inbound message with adapter type: {}", adapterType);

        OutboundAdapterPort adapter = null;
        try {
            // Create and initialize adapter
            adapter = adapterFactory.createReceiver(adapterType, configuration);
            adapter.initialize((AdapterConfiguration) configuration);

            // Process the message
            SendRequest request = SendRequest.builder()
                    .payload(payload)
                    .build();
            AdapterOperationResult result = adapter.send(request);

            if(result.isSuccess()) {
                logger.info("Successfully processed inbound message with adapter: {}", adapterType);
                // Here you could add additional processing like:
                // - Message transformation
                // - Business rule validation
                // - Routing to integration flows
                // - Persistence or logging
            } else {
                // Use reflection to access error details
                String errorMsg = "Unknown error";
                try {
                    java.lang.reflect.Field errorDetailsField = AdapterOperationResult.class.getDeclaredField("errorDetails");
                    errorDetailsField.setAccessible(true);
                    Object errorDetails = errorDetailsField.get(result);
                    if(errorDetails != null) {
                        errorMsg = errorDetails.toString();
                    } else {
                        java.lang.reflect.Field messageField = AdapterOperationResult.class.getDeclaredField("message");
                        messageField.setAccessible(true);
                        Object message = messageField.get(result);
                        if(message != null) {
                            errorMsg = message.toString();
                        }
                    }
                } catch(Exception e) {
                    logger.debug("Failed to get error details via reflection", e);
                }
                logger.error("Failed to process inbound message with adapter {}: {}",
                        adapterType, errorMsg);
            }

            return result;

        } catch(Exception e) {
            logger.error("Error processing inbound message with adapter: {}", adapterType, e);
            return AdapterOperationResult.failure("Processing error: " + e.getMessage());

        } finally {
            if(adapter != null) {
                try {
                    adapter.shutdown();
                } catch(Exception e) {
                    logger.warn("Error destroying adapter: {}", adapterType, e);
                }
            }
        }
    }

    /**
     * Validate inbound message format and content
     */
    public boolean validateMessage(Object payload) {
        if(payload == null) {
            logger.warn("Received null payload for validation");
            return false;
        }

        // Add specific validation logic here
        // - Schema validation
        // - Business rule validation
        // - Security checks

        logger.debug("Message validation passed for payload type: {}", payload.getClass().getSimpleName());
        return true;
    }

    /**
     * Transform inbound message if needed
     */
    public Object transformMessage(Object payload, String transformationType) {
        logger.debug("Transforming message with type: {}", transformationType);

        // Add transformation logic here
        // - Format conversions
        // - Field mappings
        // - Data enrichment

        return payload; // For now, return as - is
    }

    /**
     * Route message to appropriate integration flow
     */
    public void routeMessage(AdapterOperationResult result, String routingKey) {
        logger.debug("Routing message with key: {}", routingKey);

        // Add routing logic here
        // - Determine target integration flow
        // - Queue message for processing
        // - Trigger workflow execution

        if(result.isSuccess()) {
            logger.info("Message routed successfully with key: {}", routingKey);
        }
    }
}
