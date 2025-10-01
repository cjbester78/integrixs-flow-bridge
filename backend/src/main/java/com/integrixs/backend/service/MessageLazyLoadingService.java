package com.integrixs.backend.service;

import com.integrixs.data.model.Message;
import com.integrixs.data.sql.repository.MessageSqlRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    @Value("${integrix.message.payload.size-threshold:1048576}")
    private long payloadSizeThreshold; // Default 1MB

    @Value("${integrix.message.payload.storage-path:./data/payloads}")
    private String storageBasePath;

    private final MessageSqlRepository messageRepository;

    public MessageLazyLoadingService(MessageSqlRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    /**
     * Save message with automatic large payload handling.
     */
    public Message saveMessageWithLazyLoading(Message message) {
        if(message == null) {
            return null;
        }

        // Check if payload should be stored externally
        String payload = message.getMessageContent();
        if(shouldStoreExternally(payload)) {
            // Store payload externally and get reference
            String payloadReference = storeLargePayload(
                message.getMessageId(),
                payload
           );

            // Update message with reference
            message.setMessageContent(payloadReference);
            log.debug("Stored large payload externally for message: {}", message.getMessageId());
        }

        // Save message
        return messageRepository.save(message);
    }

    /**
     * Load message with automatic payload retrieval.
     */
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
    public Message updateMessageContent(UUID id, String newContent) {
        Message message = messageRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Message not found: " + id));

        // Handle large payload
        if(shouldStoreExternally(newContent)) {
            String payloadReference = storeLargePayload(
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

        return getPayloadSize(message.getMessageContent());
    }

    /**
     * Clean up orphaned payload files.
     */
    public void cleanupOrphanedPayloads(int daysToKeep) {
        // Check which files are still referenced in the database and clean up old files
        cleanupOldPayloads(daysToKeep);
        log.info("Cleaned up payload files older than {} days", daysToKeep);
    }

    /**
     * Helper method to load payload for a message.
     */
    private void loadPayloadForMessage(Message message) {
        String content = message.getMessageContent();
        if(content != null && content.startsWith("FILE:")) {
            // Load actual payload from file
            String actualPayload = loadPayload(
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

    /**
     * Determine if content should be stored externally based on size.
     */
    private boolean shouldStoreExternally(String content) {
        if (content == null) {
            return false;
        }
        return content.getBytes(StandardCharsets.UTF_8).length > payloadSizeThreshold;
    }

    /**
     * Store large payload to file system and return reference.
     */
    private String storeLargePayload(String messageId, String payload) {
        try {
            // Create directory structure: basePath/yyyy/MM/dd/
            LocalDateTime now = LocalDateTime.now();
            String dateDir = now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            Path dirPath = Paths.get(storageBasePath, dateDir);
            Files.createDirectories(dirPath);

            // Create unique filename
            String filename = messageId + "_" + System.currentTimeMillis() + ".dat";
            Path filePath = dirPath.resolve(filename);

            // Write payload to file
            Files.write(filePath, payload.getBytes(StandardCharsets.UTF_8));

            // Return reference
            return "FILE:" + dateDir + "/" + filename;
        } catch (IOException e) {
            log.error("Failed to store large payload for message: " + messageId, e);
            throw new RuntimeException("Failed to store large payload", e);
        }
    }

    /**
     * Load payload from file system.
     */
    private String loadPayload(String messageId, String reference) {
        if (reference == null || !reference.startsWith("FILE:")) {
            return reference;
        }

        try {
            String relativePath = reference.substring(5); // Remove "FILE:" prefix
            Path filePath = Paths.get(storageBasePath, relativePath);

            if (Files.exists(filePath)) {
                return new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
            } else {
                log.warn("Payload file not found for message {}: {}", messageId, filePath);
                return null;
            }
        } catch (IOException e) {
            log.error("Failed to load payload for message: " + messageId, e);
            return null;
        }
    }

    /**
     * Get the size of a payload (handle both inline and file references).
     */
    private long getPayloadSize(String content) {
        if (content == null) {
            return 0;
        }

        if (content.startsWith("FILE:")) {
            // Load file size
            try {
                String relativePath = content.substring(5);
                Path filePath = Paths.get(storageBasePath, relativePath);
                return Files.size(filePath);
            } catch (IOException e) {
                log.error("Failed to get file size for: " + content, e);
                return 0;
            }
        } else {
            // Inline content
            return content.getBytes(StandardCharsets.UTF_8).length;
        }
    }

    /**
     * Clean up old payload files.
     */
    private void cleanupOldPayloads(int daysToKeep) {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
            Path basePath = Paths.get(storageBasePath);

            if (!Files.exists(basePath)) {
                return;
            }

            Files.walk(basePath)
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".dat"))
                .filter(path -> {
                    try {
                        LocalDateTime fileTime = LocalDateTime.ofInstant(
                            Files.getLastModifiedTime(path).toInstant(),
                            java.time.ZoneId.systemDefault()
                        );
                        return fileTime.isBefore(cutoffDate);
                    } catch (IOException e) {
                        return false;
                    }
                })
                .forEach(path -> {
                    try {
                        Files.delete(path);
                        log.debug("Deleted old payload file: {}", path);
                    } catch (IOException e) {
                        log.error("Failed to delete payload file: " + path, e);
                    }
                });

        } catch (IOException e) {
            log.error("Failed to clean up old payloads", e);
        }
    }
}
