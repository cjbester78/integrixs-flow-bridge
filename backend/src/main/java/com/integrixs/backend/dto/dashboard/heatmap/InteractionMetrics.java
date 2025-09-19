package com.integrixs.backend.dto.dashboard.heatmap;

/**
 * Metrics for component interactions.
 */
public class InteractionMetrics {
    private int count = 0;
    private long totalDuration = 0;
    private int successCount = 0;

    public void addInteraction(long duration, boolean success) {
        count++;
        totalDuration += duration;
        if(success) {
            successCount++;
        }
    }

    public double getAverageDuration() {
        return count > 0 ? (double) totalDuration / count : 0;
    }

    public double getSuccessRate() {
        return count > 0 ? (double) successCount / count * 100 : 0;
    }

    // Default constructor
    public InteractionMetrics() {
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public long getTotalDuration() {
        return totalDuration;
    }

    public void setTotalDuration(long totalDuration) {
        this.totalDuration = totalDuration;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }
}
