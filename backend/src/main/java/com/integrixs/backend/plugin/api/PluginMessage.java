package com.integrixs.backend.plugin.api;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Message structure for plugin communication
 */
@Data
@Builder
public class PluginMessage {
    
    /**
     * Unique message ID
     */
    @Builder.Default
    private String id = UUID.randomUUID().toString();
    
    /**
     * Message headers/metadata
     */
    private Map<String, String> headers;
    
    /**
     * Message body/payload
     */
    private Object body;
    
    /**
     * Content type of the body
     */
    private String contentType;
    
    /**
     * Encoding of the body (for text content)
     */
    @Builder.Default
    private String encoding = "UTF-8";
    
    /**
     * Message timestamp
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    
    /**
     * Correlation ID for request/response matching
     */
    private String correlationId;
    
    /**
     * Reply-to address (if applicable)
     */
    private String replyTo;
    
    /**
     * Message priority
     */
    @Builder.Default
    private Priority priority = Priority.NORMAL;
    
    /**
     * Time-to-live in milliseconds
     */
    private Long ttl;
    
    /**
     * Custom properties
     */
    private Map<String, Object> properties;
    
    /**
     * Message priority levels
     */
    public enum Priority {
        LOW,
        NORMAL,
        HIGH,
        URGENT
    }
}