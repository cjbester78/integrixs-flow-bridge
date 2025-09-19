package com.integrixs.backend.dto.dashboard.heatmap;

import java.util.List;

public class ErrorHotspot {
    private String location;
    private int errorCount;
    private List<String> errorTypes;
    private double errorRate;
    private List<ErrorEvent> recentErrors;

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
}
