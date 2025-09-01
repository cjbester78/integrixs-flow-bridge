package com.integrixs.backend.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Response object for message statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageStatsResponse {
    
    private long totalMessages;
    private long successfulMessages;
    private long failedMessages;
    private long pendingMessages;
    private long processingMessages;
    
    private Double avgExecutionTimeMs;
    private Long maxExecutionTimeMs;
    private Long minExecutionTimeMs;
    
    private Map<String, Long> messagesByStatus;
    private Map<String, Long> messagesByType;
    private Map<String, Long> messagesBySource;
    private Map<String, Long> messagesByTarget;
    
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    
    private Long messagesPerHour;
    private Long messagesPerDay;
    
    private Map<String, Object> additionalStats;
}