package com.integrixs.backend.dto.dashboard.trend;

import lombok.Data;

/**
 * Capacity projection for a resource.
 */
@Data
public class CapacityProjection {
    private String resource;
    private double currentUsage;
    private double projectedUsage;
    private int daysUntilCapacity;
    private double confidence; // 0.0 to 1.0
}