package com.integrixs.backend.dto.dashboard.heatmap;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ErrorHotspot {
    private String location;
    private int errorCount;
    private List<String> errorTypes;
    private double errorRate;
    private List<ErrorEvent> recentErrors;
    private String component;
    private LocalDateTime timestamp;

    // Default constructor
    public ErrorHotspot() {
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }

    public List<String> getErrorTypes() {
        return errorTypes;
    }

    public void setErrorTypes(List<String> errorTypes) {
        this.errorTypes = errorTypes;
    }

    public double getErrorRate() {
        return errorRate;
    }

    public void setErrorRate(double errorRate) {
        this.errorRate = errorRate;
    }

    public List<ErrorEvent> getRecentErrors() {
        return recentErrors;
    }

    public void setRecentErrors(List<ErrorEvent> recentErrors) {
        this.recentErrors = recentErrors;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public void setErrorCount(long errorCount) {
        this.errorCount = (int) errorCount;
    }

    public void setErrorTypes(Map<String, Long> errorTypes) {
        // Convert map to list of error type strings
        this.errorTypes = new ArrayList<>(errorTypes.keySet());
    }
}
