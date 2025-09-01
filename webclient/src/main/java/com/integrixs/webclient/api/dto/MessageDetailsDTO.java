package com.integrixs.webclient.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for message details
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDetailsDTO {
    
    private String messageId;
    private String messageType;
    private String source;
    private String adapterId;
    private Object payload;
    private String contentType;
    private Map<String, String> headers;
    private Map<String, String> metadata;
    private String status;
    private String correlationId;
    private String flowId;
    private LocalDateTime receivedAt;
}