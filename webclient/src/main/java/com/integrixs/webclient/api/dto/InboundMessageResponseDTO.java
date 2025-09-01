package com.integrixs.webclient.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for inbound message response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InboundMessageResponseDTO {
    
    private String messageId;
    private boolean success;
    private String status;
    private String flowId;
    private String executionId;
    private Object responseData;
    private String error;
    private List<ValidationErrorDTO> validationErrors;
    private LocalDateTime receivedAt;
    private LocalDateTime processedAt;
    private Long processingTimeMillis;
}