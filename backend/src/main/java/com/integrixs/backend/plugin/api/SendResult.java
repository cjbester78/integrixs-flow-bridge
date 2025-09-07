package com.integrixs.backend.plugin.api;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Result of a send operation
 */
@Data
@Builder
public class SendResult {
    
    /**
     * Whether the send was successful
     */
    private boolean successful;
    
    /**
     * Message ID assigned by the external system
     */
    private String externalMessageId;
    
    /**
     * Original message ID
     */
    private String messageId;
    
    /**
     * Status message
     */
    private String message;
    
    /**
     * Error details if failed
     */
    private String errorDetails;
    
    /**
     * Timestamp when sent
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    
    /**
     * Response from external system
     */
    private Object response;
    
    /**
     * Response headers/metadata
     */
    private Map<String, String> responseHeaders;
    
    /**
     * Additional details
     */
    private Map<String, Object> details;
    
    /**
     * Static factory for success
     */
    public static SendResult success(String messageId, String externalMessageId) {
        return SendResult.builder()
                .successful(true)
                .messageId(messageId)
                .externalMessageId(externalMessageId)
                .message("Message sent successfully")
                .build();
    }
    
    /**
     * Static factory for failure
     */
    public static SendResult failure(String messageId, String error) {
        return SendResult.builder()
                .successful(false)
                .messageId(messageId)
                .message("Failed to send message")
                .errorDetails(error)
                .build();
    }
}