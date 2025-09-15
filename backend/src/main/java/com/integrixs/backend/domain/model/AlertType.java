package com.integrixs.backend.domain.model;

/**
 * Domain enum for alert types
 */
public enum AlertType {
    LONG_RUNNING("warning", "Long Running Execution"),
    EXECUTION_FAILED("error", "Execution Failed"),
    HIGH_ERROR_RATE("warning", "High Error Rate"),
    PERFORMANCE_DEGRADATION("warning", "Performance Degradation");

    private final String severity;
    private final String displayName;

    AlertType(String severity, String displayName) {
        this.severity = severity;
        this.displayName = displayName;
    }

    public String getSeverity() {
        return severity;
    }

    public String getDisplayName() {
        return displayName;
    }
}
