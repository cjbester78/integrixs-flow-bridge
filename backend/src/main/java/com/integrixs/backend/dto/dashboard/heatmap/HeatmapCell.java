package com.integrixs.backend.dto.dashboard.heatmap;

/**
 * Individual cell in a heatmap.
 */
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

    // Default constructor
    public HeatmapCell() {
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getErrors() {
        return errors;
    }

    public void setErrors(int errors) {
        this.errors = errors;
    }

    public long getTotalDuration() {
        return totalDuration;
    }

    public void setTotalDuration(long totalDuration) {
        this.totalDuration = totalDuration;
    }

    public double getIntensity() {
        return intensity;
    }

    public void setIntensity(double intensity) {
        this.intensity = intensity;
    }
}
