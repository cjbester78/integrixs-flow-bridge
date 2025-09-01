package com.integrixs.backend.dto.dashboard.heatmap;

import lombok.Data;

/**
 * Individual cell in a heatmap.
 */
@Data
public class HeatmapCell {
    private int count = 0;
    private int errors = 0;
    private long totalDuration = 0;
    private double intensity = 0.0; // 0.0 to 1.0
    
    public void incrementCount() {
        this.count++;
    }
    
    public void incrementErrors() {
        this.errors++;
    }
    
    public void addDuration(long duration) {
        this.totalDuration += duration;
    }
    
    public double getAverageDuration() {
        return count > 0 ? (double) totalDuration / count : 0;
    }
    
    public double getErrorRate() {
        return count > 0 ? (double) errors / count * 100 : 0;
    }
}