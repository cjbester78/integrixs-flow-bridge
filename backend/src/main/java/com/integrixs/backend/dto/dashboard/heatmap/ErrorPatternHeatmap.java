package com.integrixs.backend.dto.dashboard.heatmap;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Error pattern heatmap showing error distributions.
 */
@Data
public class ErrorPatternHeatmap {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Map<String, List<ErrorOccurrence>> errorPatterns;
    private Map<String, ErrorFrequency> errorFrequency;
    private List<ErrorCorrelation> correlatedErrors;
}