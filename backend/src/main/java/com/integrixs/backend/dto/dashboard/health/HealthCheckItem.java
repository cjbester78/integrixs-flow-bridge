package com.integrixs.backend.dto.dashboard.health;

/**
 * Individual health check item.
 */
public class HealthCheckItem {
    private String checkName;
    private String status; // OK, WARNING, ERROR
    private String message;
    private String details;

    // Default constructor
    public HealthCheckItem() {
    }

    public String getCheckName() {
        return checkName;
    }

    public void setCheckName(String checkName) {
        this.checkName = checkName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
