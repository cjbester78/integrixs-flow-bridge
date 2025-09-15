package com.integrixs.webclient.domain.service;

import com.integrixs.webclient.domain.model.InboundMessage;
import com.integrixs.webclient.domain.model.ProcessingResult;
import com.integrixs.webclient.domain.model.ValidationResult;

/**
 * Domain service interface for inbound message processing
 */
public interface InboundMessageService {

    /**
     * Process an inbound message
     * @param message The message to process
     * @return Processing result
     */
    ProcessingResult processMessage(InboundMessage message);

    /**
     * Validate an inbound message
     * @param message The message to validate
     * @return Validation result
     */
    ValidationResult validateMessage(InboundMessage message);

    /**
     * Route message to appropriate flow
     * @param message The message to route
     * @return Flow ID if routed successfully
     */
    String routeMessage(InboundMessage message);

    /**
     * Transform message payload
     * @param message The message to transform
     * @param targetFormat Target format
     * @return Transformed payload
     */
    Object transformMessage(InboundMessage message, String targetFormat);

    /**
     * Check if message is duplicate
     * @param message The message to check
     * @return true if duplicate
     */
    boolean isDuplicateMessage(InboundMessage message);

    /**
     * Store message for processing
     * @param message The message to store
     * @return Stored message ID
     */
    String storeMessage(InboundMessage message);

    /**
     * Get message by ID
     * @param messageId Message ID
     * @return Message if found
     */
    InboundMessage getMessage(String messageId);

    /**
     * Update message status
     * @param messageId Message ID
     * @param status New status
     */
    void updateMessageStatus(String messageId, InboundMessage.MessageStatus status);
}
