package com.integrixs.data.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a flow route for conditional routing
 */
@Entity
@Table(name = "flow_routes")
public class FlowRoute extends BaseEntity {


    @Column(name = "route_name", nullable = false)
    private String routeName;

    @Column(name = "route_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private RouteType routeType = RouteType.CONDITIONAL;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flow_id", nullable = false)
    private IntegrationFlow flow;

    @Column(name = "source_step")
    private String sourceStep;

    @Column(name = "target_step")
    private String targetStep;

    @Column(name = "is_active")
    private boolean active = true;

    @Column(name = "priority")
    private Integer priority = 0;

    @OneToMany(mappedBy = "flowRoute", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RouteCondition> conditions = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "condition_operator")
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
