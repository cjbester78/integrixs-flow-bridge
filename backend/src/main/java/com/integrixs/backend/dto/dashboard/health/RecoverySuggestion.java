package com.integrixs.backend.dto.dashboard.health;

import lombok.Data;

import java.util.List;

/**
 * Recovery suggestion for adapter issues.
 */
@Data
public class RecoverySuggestion {
    private String issue;
    private String description;
    private String priority; // HIGH, MEDIUM, LOW
    private List<String> steps;
    private String estimatedImpact;
}
