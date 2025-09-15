package com.integrixs.backend.dto.dashboard.trend;

import lombok.Data;

/**
 * Component ranking by performance score.
 */
@Data
public class ComponentRanking {
    private String componentId;
    private double score;
}
