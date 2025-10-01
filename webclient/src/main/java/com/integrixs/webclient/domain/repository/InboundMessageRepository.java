package com.integrixs.webclient.domain.repository;

import com.integrixs.webclient.domain.model.InboundMessage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for inbound messages
 */
public interface InboundMessageRepository {

    /**
     * Save message
     * @param message The message to save
     * @return Saved message
     */
    InboundMessage save(InboundMessage message);

    /**
     * Find message by ID
     * @param messageId Message ID
     * @return Message if found
     */
    Optional<InboundMessage> findById(String messageId);

    /**
     * Find messages by status
     * @param status Message status
     * @return List of messages
     */
    List<InboundMessage> findByStatus(InboundMessage.MessageStatus status);

    /**
     * Find messages by flow ID
     * @param flowId Flow ID
     * @return List of messages
     */
    List<InboundMessage> findByFlowId(String flowId);

    /**
     * Find messages by correlation ID
     * @param correlationId Correlation ID
     * @return List of messages
     */
    List<InboundMessage> findByCorrelationId(String correlationId);

    /**
     * Find messages received between dates
     * @param start Start date
     * @param end End date
     * @return List of messages
     */
    List<InboundMessage> findByReceivedAtBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Update message status
     * @param messageId Message ID
     * @param status New status
     */
    void updateStatus(String messageId, InboundMessage.MessageStatus status);

    /**
     * Delete message
     * @param messageId Message ID
     */
    void deleteById(String messageId);

    /**
     * Delete messages older than date
     * @param before Cutoff date
     * @return Number of deleted messages
     */
    int deleteByReceivedAtBefore(LocalDateTime before);

    /**
     * Check if duplicate exists
     * @param message The message to check
     * @return true if duplicate exists
     */
    boolean existsDuplicate(InboundMessage message);
}
