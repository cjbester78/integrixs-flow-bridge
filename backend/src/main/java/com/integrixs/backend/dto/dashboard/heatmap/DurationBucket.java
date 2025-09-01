package com.integrixs.backend.dto.dashboard.heatmap;

import lombok.Data;

/**
 * Duration bucket for performance distribution.
 */
@Data
public class DurationBucket {
    private long min;
    private long max;
    private String label;
}