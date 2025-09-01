package com.integrixs.adapters.api.dto;

import lombok.Data;

/**
 * DTO for retry configuration
 */
@Data
public class RetryConfigDTO {
    private boolean enabled = false;
    private int maxAttempts = 3;
    private long initialDelay = 1000;
    private double backoffMultiplier = 2.0;
    private long maxDelay = 60000;
}