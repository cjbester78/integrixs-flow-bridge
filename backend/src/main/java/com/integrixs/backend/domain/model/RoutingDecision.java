package com.integrixs.backend.domain.model;

import com.integrixs.data.model.FlowRoute;
import lombok.Getter;

/**
 * Domain model for routing decision result
 */
@Getter
public class RoutingDecision {
    private final boolean success;
    private final FlowRoute route;
    private final String errorMessage;

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
}
