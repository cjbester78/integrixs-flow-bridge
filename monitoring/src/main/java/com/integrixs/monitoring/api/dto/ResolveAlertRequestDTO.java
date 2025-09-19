package com.integrixs.monitoring.api.dto;


/**
 * DTO for resolve alert request
 */
public class ResolveAlertRequestDTO {
    private String resolution;
    private String resolvedBy;


    // Getters
    public String getResolution() {
        return resolution;
    }

    public String getResolvedBy() {
        return resolvedBy;
    }

    // Setters
    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public void setResolvedBy(String resolvedBy) {
        this.resolvedBy = resolvedBy;
    }

}
