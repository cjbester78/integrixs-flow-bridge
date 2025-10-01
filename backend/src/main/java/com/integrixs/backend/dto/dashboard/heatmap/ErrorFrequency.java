package com.integrixs.backend.dto.dashboard.heatmap;

import java.util.Map;

/**
 * Error frequency analysis.
 */
public class ErrorFrequency {
    private String errorType;
    private int totalCount;
    private Map<Integer, Long> hourlyDistribution; // Hour -> Count

    // Default constructor
    public ErrorFrequency() {
    }

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
}
