package com.integrixs.backend.dto.dashboard.heatmap;

import lombok.Data;

/**
 * Metrics for component interactions.
 */
@Data
public class InteractionMetrics {
    private int count = 0;
    private long totalDuration = 0;
    private int successCount = 0;
    
    public void addInteraction(long duration, boolean success) {
        count++;
        totalDuration += duration;
        if (success) {
            successCount++;
        }
    }
    
    public double getAverageDuration() {
        return count > 0 ? (double) totalDuration / count : 0;
    }
    
    public double getSuccessRate() {
        return count > 0 ? (double) successCount / count * 100 : 0;
    }
}