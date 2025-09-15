package com.integrixs.backend.dto.dashboard.health;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Adapter status change event.
 */
@Data
public class StatusChangeEvent {
    private LocalDateTime timestamp;
    private String fromStatus;
    private String toStatus;
    private String reason;
}
