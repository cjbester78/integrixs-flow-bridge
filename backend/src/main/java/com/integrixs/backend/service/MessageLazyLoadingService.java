package com.integrixs.backend.service;

import com.integrixs.backend.performance.LazyLoadingService;
import com.integrixs.data.model.Message;
import com.integrixs.data.repository.MessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service that integrates lazy loading with message persistence.
 * Automatically handles large payload storage and retrieval.
 */
@Service
public class MessageLazyLoadingService {

    private static final Logger log = LoggerFactory.getLogger(MessageLazyLoadingService.class);


    private final MessageRepository messageRepository;
    private final LazyLoadingService lazyLoadingService;

    /**
     * Save message with automatic large payload handling.
     */
    @Transactional
    public Message saveMessageWithLazyLoading(Message message) {
        if(message == null) {
            return null;
        }

        // Check if payload should be stored externally
        String payload = message.getMessageContent();
        if(lazyLoadingService.shouldStoreExternally(payload)) {
            // Store payload externally and get reference
            String payloadReference = lazyLoadingService.storeLargePayload(
                message.getMessageId(),
                payload
           );

            // Update message with reference
            message.setMessageContent(payloadReference);
            log.debug("Stored large payload externally for message: {}", message.getMessageId());
        }

        // Save message with JPA
        return messageRepository.save(message);
    }

    /**
     * Load message with automatic payload retrieval.
     */
    @Transactional(readOnly = true)
    public Optional<Message> loadMessageWithPayload(UUID id) {
        Optional<Message> messageOpt = messageRepository.findById(id);

        if(messageOpt.isPresent()) {
            Message message = messageOpt.get();
            loadPayloadForMessage(message);
        }

        return messageOpt;
    }

    /**
     * Load message by messageId with automatic payload retrieval.
     */
    @Transactional(readOnly = true)
    public Optional<Message> loadMessageByMessageId(String messageId) {
        Optional<Message> messageOpt = messageRepository.findByMessageId(messageId);

        if(messageOpt.isPresent()) {
            Message message = messageOpt.get();
            loadPayloadForMessage(message);
        }

        return messageOpt;
    }

    /**
     * Update message content with lazy loading support.
     */
    @Transactional
    public Message updateMessageContent(UUID id, String newContent) {
        Message message = messageRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Message not found: " + id));

        // Handle large payload
        if(lazyLoadingService.shouldStoreExternally(newContent)) {
            String payloadReference = lazyLoadingService.storeLargePayload(
                message.getMessageId(),
                newContent
           );
            message.setMessageContent(payloadReference);
        } else {
            message.setMessageContent(newContent);
        }

        return messageRepository.save(message);
    }

    /**
     * Get actual payload size for a message.
     */
    public long getMessagePayloadSize(UUID id) {
        Message message = messageRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Message not found: " + id));

        return lazyLoadingService.getPayloadSize(message.getMessageContent());
    }

    /**
     * Clean up orphaned payload files.
     */
    @Transactional
    public void cleanupOrphanedPayloads(int daysToKeep) {
        // This would ideally check which files are still referenced in the database
        // For now, just clean up old files
        lazyLoadingService.cleanupOldPayloads(daysToKeep);
        log.info("Cleaned up payload files older than {} days", daysToKeep);
    }

    /**
     * Helper method to load payload for a message.
     */
    private void loadPayloadForMessage(Message message) {
        String content = message.getMessageContent();
        if(content != null && content.startsWith("FILE:")) {
            // Load actual payload from file
            String actualPayload = lazyLoadingService.loadPayload(
                message.getMessageId(),
                content
           );

            if(actualPayload != null) {
                message.setMessageContent(actualPayload);
            } else {
                log.warn("Failed to load external payload for message: {}", message.getMessageId());
                message.setMessageContent(""); // Set empty to avoid null issues
            }
        }
    }

    /**
     * Check if a message has externally stored payload.
     */
    public boolean hasExternalPayload(Message message) {
        return message != null &&
               message.getMessageContent() != null &&
               message.getMessageContent().startsWith("FILE:");
    }
}
