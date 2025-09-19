package com.integrixs.backend.api.dto;

/**
 * DTO for flow route
 */
public class RouteDTO {
    private String id;
    private String flowId;
    private String routeName;
    private String sourceStep;
    private String targetStep;
    private Integer priority;
    private String conditionOperator;
    private boolean enabled = true;

    // Default constructor
    public RouteDTO() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFlowId() {
        return flowId;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public String getSourceStep() {
        return sourceStep;
    }

    public void setSourceStep(String sourceStep) {
        this.sourceStep = sourceStep;
    }

    public String getTargetStep() {
        return targetStep;
    }

    public void setTargetStep(String targetStep) {
        this.targetStep = targetStep;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getConditionOperator() {
        return conditionOperator;
    }

    public void setConditionOperator(String conditionOperator) {
        this.conditionOperator = conditionOperator;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
