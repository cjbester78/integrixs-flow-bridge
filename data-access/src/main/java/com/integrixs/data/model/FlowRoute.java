package com.integrixs.data.model;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a flow route for conditional routing
 */
public class FlowRoute extends BaseEntity {

    private String routeName;

    private RouteType routeType = RouteType.CONDITIONAL;

    private IntegrationFlow flow;

    private String sourceStep;

    private String targetStep;

    private boolean active = true;

    private Integer priority = 0;

    private List<RouteCondition> conditions = new ArrayList<>();

    private String description;

    private String conditionOperator = "AND";

    public enum RouteType {
        CONDITIONAL,
        CONTENT_BASED,
        ROUND_ROBIN,
        WEIGHTED,
        FAILOVER
    }

    // Getters and setters

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public RouteType getRouteType() {
        return routeType;
    }

    public void setRouteType(RouteType routeType) {
        this.routeType = routeType;
    }

    public IntegrationFlow getFlow() {
        return flow;
    }

    public void setFlow(IntegrationFlow flow) {
        this.flow = flow;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public List<RouteCondition> getConditions() {
        return conditions;
    }

    public void setConditions(List<RouteCondition> conditions) {
        this.conditions = conditions;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getConditionOperator() {
        return conditionOperator;
    }

    public void setConditionOperator(String conditionOperator) {
        this.conditionOperator = conditionOperator;
    }

    // Additional helper methods
    public String getName() {
        return routeName;
    }

    public boolean isDefault() {
        // Route with priority 0 is considered default
        return priority != null && priority == 0;
    }
}
