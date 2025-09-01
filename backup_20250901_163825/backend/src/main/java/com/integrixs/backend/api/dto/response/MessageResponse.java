package com.integrixs.backend.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Response object for message details
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    
    private String id;
    private String correlationId;
    private String status;
    private String source;
    private String target;
    private String type;
    
    private String flowId;
    private String flowName;
    
    private String payload;
    private Map<String, String> headers;
    private Map<String, Object> metadata;
    
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private LocalDateTime completedAt;
    
    private Long executionTimeMs;
    private Integer retryCount;
    private Integer priority;
    
    private String errorMessage;
    private String errorDetails;
    
    private String businessComponentId;
    private String businessComponentName;
}