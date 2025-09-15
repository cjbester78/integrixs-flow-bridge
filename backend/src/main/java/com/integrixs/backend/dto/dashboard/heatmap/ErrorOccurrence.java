package com.integrixs.backend.dto.dashboard.heatmap;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Single error occurrence.
 */
@Data
public class ErrorOccurrence {
    private LocalDateTime timestamp;
    private String flowId;
    private String component;
    private String errorMessage;
}
