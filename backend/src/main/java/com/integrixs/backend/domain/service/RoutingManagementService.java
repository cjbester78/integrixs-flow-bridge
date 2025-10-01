package com.integrixs.backend.domain.service;

import com.integrixs.backend.domain.model.RoutingDecision;
import com.integrixs.backend.domain.model.RouterConfiguration;
import com.integrixs.data.model.FlowRoute;
import com.integrixs.data.model.RouteCondition;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Domain service for routing business logic and decision making
 */
@Service
public class RoutingManagementService {

    /**
     * Validate route configuration
     * @param route The route to validate
     * @throws IllegalArgumentException if route is invalid
     */
    public void validateRoute(FlowRoute route) {
        if(route == null) {
            throw new IllegalArgumentException("Route cannot be null");
        }

        if(route.getRouteName() == null || route.getRouteName().trim().isEmpty()) {
            throw new IllegalArgumentException("Route name is required");
        }

        if(route.getTargetStep() == null || route.getTargetStep().trim().isEmpty()) {
            throw new IllegalArgumentException("Target step is required");
        }

        if(route.getPriority() != null && route.getPriority() < 0) {
            throw new IllegalArgumentException("Route priority cannot be negative");
        }
    }

    /**
     * Determine if a route is the default route
     * @param route The route to check
     * @return true if this is the default route
     */
    public boolean isDefaultRoute(FlowRoute route) {
        return route.getPriority() != null && route.getPriority() == 0;
    }

    /**
     * Sort routes by priority(higher priority first, default last)
     * @param routes List of routes to sort
     * @return Sorted list of routes
     */
    public List<FlowRoute> sortRoutesByPriority(List<FlowRoute> routes) {
        return routes.stream()
            .sorted((a, b) -> {
                Integer priorityA = a.getPriority() != null ? a.getPriority() : 100;
                Integer priorityB = b.getPriority() != null ? b.getPriority() : 100;

                // Default route(priority 0) should be last
                if(priorityA == 0) return 1;
                if(priorityB == 0) return -1;

                // Higher priority first
                return priorityB.compareTo(priorityA);
            })
            .toList();
    }

    /**
     * Create routing decision for a matched route
     * @param route The matched route
     * @return RoutingDecision
     */
    public RoutingDecision createRoutingDecision(FlowRoute route) {
        return RoutingDecision.route(route);
    }

    /**
     * Create routing decision for no match
     * @return RoutingDecision indicating no match
     */
    public RoutingDecision createNoMatchDecision() {
        return RoutingDecision.noMatch();
    }

    /**
     * Create routing decision for error
     * @param errorMessage The error message
     * @return RoutingDecision indicating error
     */
    public RoutingDecision createErrorDecision(String errorMessage) {
        return RoutingDecision.error(errorMessage);
    }

    /**
     * Validate condition configuration
     * @param condition The condition to validate
     * @throws IllegalArgumentException if condition is invalid
     */
    public void validateCondition(RouteCondition condition) {
        if(condition == null) {
            throw new IllegalArgumentException("Condition cannot be null");
        }

        if(condition.getOperator() == null) {
            throw new IllegalArgumentException("Condition operator is required");
        }

        // Validate that conditions requiring values have them
        switch(condition.getOperator()) {
            case EQUALS:
            case NOT_EQUALS:
            case CONTAINS:
            case NOT_CONTAINS:
            case MATCHES_REGEX:
            case GREATER_THAN:
            case LESS_THAN:
            case IN_LIST:
            case NOT_IN_LIST:
                if(condition.getExpectedValue() == null || condition.getExpectedValue().trim().isEmpty()) {
                    throw new IllegalArgumentException("Expected value is required for operator: " + condition.getOperator());
                }
                break;
            case IS_NULL:
            case IS_NOT_NULL:
                // These operators don't need expected values
                break;
        }
    }

    /**
     * Compare numeric values safely
     * @param value1 First value
     * @param value2 Second value
     * @return Comparison result
     */
    public int compareNumeric(String value1, String value2) {
        try {
            double d1 = Double.parseDouble(value1);
            double d2 = Double.parseDouble(value2);
            return Double.compare(d1, d2);
        } catch(NumberFormatException e) {
            // Fall back to string comparison
            return value1.compareTo(value2);
        }
    }

    /**
     * Parse list values from comma - separated string
     * @param listValue Comma - separated list
     * @return List of trimmed values
     */
    public List<String> parseListValues(String listValue) {
        if(listValue == null || listValue.trim().isEmpty()) {
            return Collections.emptyList();
        }

        return Arrays.stream(listValue.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .toList();
    }

    /**
     * Calculate total weight for weighted routing
     * @param weightedTargets Map of targets to weights
     * @return Total weight
     */
    public int calculateTotalWeight(Map<String, Integer> weightedTargets) {
        if(weightedTargets == null || weightedTargets.isEmpty()) {
            return 0;
        }

        return weightedTargets.values().stream()
            .mapToInt(Integer::intValue)
            .sum();
    }

    /**
     * Select target based on weight distribution
     * @param weightedTargets Map of targets to weights
     * @param randomValue Random value for selection
     * @return Selected target
     */
    public String selectWeightedTarget(Map<String, Integer> weightedTargets, int randomValue) {
        if(weightedTargets == null || weightedTargets.isEmpty()) {
            return null;
        }

        int currentWeight = 0;

        for(Map.Entry<String, Integer> entry : weightedTargets.entrySet()) {
            currentWeight += entry.getValue();
            if(randomValue < currentWeight) {
                return entry.getKey();
            }
        }

        // Fallback to first entry
        return weightedTargets.keySet().iterator().next();
    }

    /**
     * Calculate round - robin index
     * @param currentIndex Current index
     * @param targetCount Number of targets
     * @return Next index
     */
    public int calculateRoundRobinIndex(AtomicInteger currentIndex, int targetCount) {
        if(targetCount <= 0) {
            throw new IllegalArgumentException("Target count must be positive");
        }

        return currentIndex.getAndIncrement() % targetCount;
    }

    /**
     * Validate router configuration
     * @param config Router configuration
     * @throws IllegalArgumentException if configuration is invalid
     */
    public void validateRouterConfig(RouterConfiguration config) {
        if(config == null) {
            throw new IllegalArgumentException("Router configuration cannot be null");
        }

        if(config.getRouterId() == null || config.getRouterId().trim().isEmpty()) {
            throw new IllegalArgumentException("Router ID is required");
        }

        if(config.getRouterType() == null) {
            throw new IllegalArgumentException("Router type is required");
        }

        // Validate based on router type
        switch(config.getRouterType()) {
            case CHOICE:
                if(config.getChoices() == null || config.getChoices().isEmpty()) {
                    throw new IllegalArgumentException("Choice router requires at least one choice");
                }
                break;

            case CONTENT_BASED:
                if(config.getContentRoutes() == null || config.getContentRoutes().isEmpty()) {
                    throw new IllegalArgumentException("Content - based router requires content routes");
                }
                if(config.getExtractionPath() == null || config.getExtractionPath().trim().isEmpty()) {
                    throw new IllegalArgumentException("Content - based router requires extraction path");
                }
                break;

            case RECIPIENT_LIST:
                if((config.getRecipients() == null || config.getRecipients().isEmpty()) &&
                    (config.getRecipientListVariable() == null || config.getRecipientListVariable().trim().isEmpty())) {
                    throw new IllegalArgumentException("Recipient list router requires recipients or recipient variable");
                }
                break;

            case ROUND_ROBIN:
                if(config.getRoundRobinTargets() == null || config.getRoundRobinTargets().isEmpty()) {
                    throw new IllegalArgumentException("Round - robin router requires targets");
                }
                break;

            case WEIGHTED:
                if(config.getWeightedTargets() == null || config.getWeightedTargets().isEmpty()) {
                    throw new IllegalArgumentException("Weighted router requires weighted targets");
                }
                break;
        }
    }

    /**
     * Get condition operator display name
     * @param operator The operator
     * @return Display name
     */
    public String getOperatorDisplayName(RouteCondition.Operator operator) {
        if(operator == null) {
            return "Unknown";
        }

        switch(operator) {
            case EQUALS: return "Equals";
            case NOT_EQUALS: return "Not Equals";
            case CONTAINS: return "Contains";
            case NOT_CONTAINS: return "Not Contains";
            case MATCHES_REGEX: return "Matches Regex";
            case GREATER_THAN: return "Greater Than";
            case LESS_THAN: return "Less Than";
            case IS_NULL: return "Is Null";
            case IS_NOT_NULL: return "Is Not Null";
            case IN_LIST: return "In List";
            case NOT_IN_LIST: return "Not In List";
            default: return operator.toString();
        }
    }
}
