package com.integrixs.webclient.domain.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Domain model representing an inbound message
 */
@Data
@Builder
public class InboundMessage {
    private String messageId;
    private MessageType messageType;
    private String source;
    private String adapterId;
    private Object payload;
    private String contentType;
    @Builder.Default
    private Map<String, String> headers = new HashMap<>();
    @Builder.Default
    private Map<String, String> metadata = new HashMap<>();
    private LocalDateTime receivedAt;
    private MessageStatus status;
    private String correlationId;
    private String flowId;

    /**
     * Message types
     */
    public enum MessageType {
        HTTP_REQUEST,
        SOAP_REQUEST,
        WEBHOOK,
        API_CALL,
        FILE_UPLOAD,
        STREAM
    }

    /**
     * Message status
     */
    public enum MessageStatus {
        RECEIVED,
        VALIDATED,
        PROCESSING,
        PROCESSED,
        FAILED,
        REJECTED
    }

    /**
     * Pre - build operations
     */
    public static InboundMessageBuilder builder() {
        return new InboundMessageBuilder() {
            @Override
            public InboundMessage build() {
                if(super.messageId == null) {
                    super.messageId = UUID.randomUUID().toString();
                }
                if(super.receivedAt == null) {
                    super.receivedAt = LocalDateTime.now();
                }
                if(super.status == null) {
                    super.status = MessageStatus.RECEIVED;
                }
                return super.build();
            }
        };
    }

    /**
     * Add header
     * @param key Header key
     * @param value Header value
     */
    public void addHeader(String key, String value) {
        this.headers.put(key, value);
    }

    /**
     * Add metadata
     * @param key Metadata key
     * @param value Metadata value
     */
    public void addMetadata(String key, String value) {
        this.metadata.put(key, value);
    }

    /**
     * Check if message is processed
     * @return true if processed
     */
    public boolean isProcessed() {
        return status == MessageStatus.PROCESSED;
    }

    /**
     * Check if message failed
     * @return true if failed
     */
    public boolean isFailed() {
        return status == MessageStatus.FAILED || status == MessageStatus.REJECTED;
    }

    /**
     * Get payload as specific type
     * @param type Target type
     * @return Payload cast to type
     */
    public <T> T getPayloadAs(Class<T> type) {
        return type.cast(payload);
    }
}
