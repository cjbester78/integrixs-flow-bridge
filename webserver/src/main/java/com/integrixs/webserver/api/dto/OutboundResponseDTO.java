package com.integrixs.webserver.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for outbound response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutboundResponseDTO {
    
    private String requestId;
    private boolean success;
    private int statusCode;
    private String statusMessage;
    private Object responseBody;
    private String contentType;
    private Map<String, String> headers;
    private LocalDateTime timestamp;
    private Long responseTimeMillis;
    private String errorMessage;
    private String errorCode;
    private RetryInfoDTO retryInfo;
}