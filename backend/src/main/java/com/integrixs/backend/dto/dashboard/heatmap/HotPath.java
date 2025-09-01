package com.integrixs.backend.dto.dashboard.heatmap;

import lombok.Data;

/**
 * Hot path in component interactions.
 */
@Data
public class HotPath {
    private String source;
    private String target;
    private int interactionCount;
    private double averageDuration;
    private double successRate;
}