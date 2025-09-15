package com.integrixs.backend.dto.dashboard.health;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Recent error occurrence.
 */
@Data
public class RecentError {
    private LocalDateTime timestamp;
    private String message;
    private int count;
}
