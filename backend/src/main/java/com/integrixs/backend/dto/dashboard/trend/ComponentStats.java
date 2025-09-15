package com.integrixs.backend.dto.dashboard.trend;

import lombok.Data;

/**
 * Statistical summary for a component.
 */
@Data
public class ComponentStats {
    private double mean;
    private double max;
    private double min;
    private double stdDev;
    private String trend;
}
