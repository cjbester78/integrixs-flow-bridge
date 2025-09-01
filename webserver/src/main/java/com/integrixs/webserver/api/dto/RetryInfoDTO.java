package com.integrixs.webserver.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for retry information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetryInfoDTO {
    
    private int attemptCount;
    private boolean wasRetried;
    private String lastRetryReason;
    private LocalDateTime lastRetryTime;
}