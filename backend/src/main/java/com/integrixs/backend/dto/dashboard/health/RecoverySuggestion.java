package com.integrixs.backend.dto.dashboard.health;

import java.util.List;

/**
 * Recovery suggestion for adapter issues.
 */
public class RecoverySuggestion {
    private String issue;
    private String description;
    private String priority; // HIGH, MEDIUM, LOW
    private List<String> steps;
    private String estimatedImpact;

    // Default constructor
    public RecoverySuggestion() {
    }

    public String getIssue() {
        return issue;
    }

    public void setIssue(String issue) {
        this.issue = issue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public List<String> getSteps() {
        return steps;
    }

    public void setSteps(List<String> steps) {
        this.steps = steps;
    }

    public String getEstimatedImpact() {
        return estimatedImpact;
    }

    public void setEstimatedImpact(String estimatedImpact) {
        this.estimatedImpact = estimatedImpact;
    }
}
