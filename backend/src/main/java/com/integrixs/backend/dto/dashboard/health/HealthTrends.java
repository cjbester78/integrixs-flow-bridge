package com.integrixs.backend.dto.dashboard.health;

import lombok.Data;

/**
 * Health trends across all adapters.
 */
@Data
public class HealthTrends {
    private double errorRateTrend; // positive means increasing errors
    private double responseTimeTrend; // positive means slower
    private double availabilityTrend; // positive means improving
}
