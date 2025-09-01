package com.integrixs.backend.dto.dashboard.health;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Health score at a point in time.
 */
@Data
public class HealthScorePoint {
    private LocalDateTime timestamp;
    private int score;
}