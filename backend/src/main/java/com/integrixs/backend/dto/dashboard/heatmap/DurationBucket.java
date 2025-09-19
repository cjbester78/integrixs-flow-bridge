package com.integrixs.backend.dto.dashboard.heatmap;

/**
 * Duration bucket for performance distribution.
 */
public class DurationBucket {
    private long min;
    private long max;
    private String label;

    // Default constructor
    public DurationBucket() {
    }

    public long getMin() {
        return min;
    }

    public void setMin(long min) {
        this.min = min;
    }

    public long getMax() {
        return max;
    }

    public void setMax(long max) {
        this.max = max;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
