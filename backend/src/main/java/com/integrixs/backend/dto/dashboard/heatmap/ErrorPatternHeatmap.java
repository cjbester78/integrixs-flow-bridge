package com.integrixs.backend.dto.dashboard.heatmap;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Error pattern heatmap showing error distributions.
 */
public class ErrorPatternHeatmap {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Map<String, List<ErrorOccurrence>> errorPatterns;
    private Map<String, ErrorFrequency> errorFrequency;
    private List<ErrorCorrelation> correlatedErrors;

    // Default constructor
    public ErrorPatternHeatmap() {
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public List<ErrorCorrelation> getCorrelatedErrors() {
        return correlatedErrors;
    }

    public void setCorrelatedErrors(List<ErrorCorrelation> correlatedErrors) {
        this.correlatedErrors = correlatedErrors;
    }
}
