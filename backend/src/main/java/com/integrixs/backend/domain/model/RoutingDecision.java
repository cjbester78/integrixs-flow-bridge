package com.integrixs.backend.domain.model;

import com.integrixs.data.model.FlowRoute;
/**
 * Domain model for routing decision result
 */
public class RoutingDecision {
    private boolean success;
    private FlowRoute route;
    private String errorMessage;

    private RoutingDecision(boolean success, FlowRoute route, String errorMessage) {
        this.success = success;
        this.route = route;
        this.errorMessage = errorMessage;
    }

    public static RoutingDecision route(FlowRoute route) {
        return new RoutingDecision(true, route, null);
    }

    public static RoutingDecision defaultRoute() {
        return new RoutingDecision(true, null, null);
    }

    public static RoutingDecision noMatch() {
        return new RoutingDecision(false, null, "No matching route found");
    }

    public static RoutingDecision error(String message) {
        return new RoutingDecision(false, null, message);
    }

    public boolean hasRoute() {
        return route != null;
    }

    // Default constructor
    public RoutingDecision() {
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public FlowRoute getRoute() {
        return route;
    }

    public void setRoute(FlowRoute route) {
        this.route = route;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
