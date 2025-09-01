package com.integrixs.webserver.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for retry configuration
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetryConfigDTO {
    
    @Builder.Default
    private int maxRetries = 3;
    
    @Builder.Default
    private long retryDelayMillis = 1000;
    
    @Builder.Default
    private boolean exponentialBackoff = true;
    
    @Builder.Default
    private double backoffMultiplier = 2.0;
}