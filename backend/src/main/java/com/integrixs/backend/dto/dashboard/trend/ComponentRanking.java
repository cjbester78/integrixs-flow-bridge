package com.integrixs.backend.dto.dashboard.trend;

/**
 * Component ranking by performance score.
 */
public class ComponentRanking {
    private String componentId;
    private double score;

    // Default constructor
    public ComponentRanking() {
    }

    public String getComponentId() {
        return componentId;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
}
