package com.integrixs.backend.api.dto;

/**
 * DTO for routing decision result
 */
public class RoutingDecisionDTO {
    private boolean success;
    private String routeId;
    private String routeName;
    private String targetStep;
    private String errorMessage;

    // Default constructor
    public RoutingDecisionDTO() {
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public String getTargetStep() {
        return targetStep;
    }

    public void setTargetStep(String targetStep) {
        this.targetStep = targetStep;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
