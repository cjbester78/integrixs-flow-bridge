package com.integrixs.backend.domain.model;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * Domain model for trace event
 */
@Data
public class TraceEvent {
    private String eventType;
    private String message;
    private LocalDateTime timestamp;
}