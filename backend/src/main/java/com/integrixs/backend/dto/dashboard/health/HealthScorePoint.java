package com.integrixs.backend.dto.dashboard.health;

import java.time.LocalDateTime;

/**
 * Health score at a point in time.
 */
public class HealthScorePoint {
    private LocalDateTime timestamp;
    private int score;

    // Default constructor
    public HealthScorePoint() {
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
