package com.integrixs.backend.dto.dashboard.heatmap;

import lombok.Data;

/**
 * Correlation between different error types.
 */
@Data
public class ErrorCorrelation {
    private String errorType1;
    private String errorType2;
    private int correlationCount;
}
