package com.integrixs.backend.infrastructure.routing;

import com.integrixs.backend.domain.model.RouterConfiguration;
import com.integrixs.backend.domain.model.RouterConfiguration.RouteChoice;
import com.integrixs.backend.domain.service.RoutingManagementService;
import com.integrixs.backend.service.FlowContextService;
import com.integrixs.backend.service.FlowContextService.FlowContext;
import com.integrixs.data.model.FlowRoute;
import com.integrixs.data.model.RouteCondition;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Infrastructure service for evaluating routing conditions and executing routing logic
 */
@Service
public class RoutingEvaluator {

    private static final Logger log = LoggerFactory.getLogger(RoutingEvaluator.class);


    private final FlowContextService contextService;
    private final RoutingManagementService routingManagementService;

    public RoutingEvaluator(FlowContextService contextService,
                          RoutingManagementService routingManagementService) {
        this.contextService = contextService;
        this.routingManagementService = routingManagementService;
    }

    /**
     * Evaluate route conditions
     * @param route The route to evaluate
     * @param conditions List of conditions for the route
     * @param context Flow context
     * @return true if conditions are met
     */
    public boolean evaluateRouteConditions(FlowRoute route, List<RouteCondition> conditions, FlowContext context) {
        if(conditions.isEmpty()) {
            // Route with no conditions always matches(unless it's default)
            return !routingManagementService.isDefaultRoute(route);
        }

        // Evaluate based on condition operator(AND/OR)
        boolean useAnd = route.getConditionOperator() == null || "AND".equalsIgnoreCase(route.getConditionOperator());

        for(RouteCondition condition : conditions) {
            boolean result = evaluateCondition(condition, context);

            if(useAnd && !result) {
                return false; // AND: any false means route doesn't match
            } else if(!useAnd && result) {
                return true; // OR: any true means route matches
            }
        }

        // AND: all true, OR: all false
        return useAnd;
    }

    /**
     * Evaluate a single condition
     * @param condition The condition to evaluate
     * @param context Flow context
     * @return true if condition is met
     */
    public boolean evaluateCondition(RouteCondition condition, FlowContext context) {
        try {
            String value = extractValue(condition, context);
            if(value == null && condition.getOperator() != RouteCondition.Operator.IS_NULL) {
                return false;
            }

            switch(condition.getOperator()) {
                case EQUALS:
                    return value != null && value.equals(condition.getExpectedValue());

                case NOT_EQUALS:
                    return value == null || !value.equals(condition.getExpectedValue());

                case CONTAINS:
                    return value != null && value.contains(condition.getExpectedValue());

                case NOT_CONTAINS:
                    return value == null || !value.contains(condition.getExpectedValue());

                case MATCHES_REGEX:
                    return value != null && value.matches(condition.getExpectedValue());

                case GREATER_THAN:
                    return routingManagementService.compareNumeric(value, condition.getExpectedValue()) > 0;

                case LESS_THAN:
                    return routingManagementService.compareNumeric(value, condition.getExpectedValue()) < 0;

                case IS_NULL:
                    return value == null;

                case IS_NOT_NULL:
                    return value != null;

                case IN_LIST:
                    return value != null && routingManagementService.parseListValues(condition.getExpectedValue())
                        .contains(value.trim());

                case NOT_IN_LIST:
                    return value == null || !routingManagementService.parseListValues(condition.getExpectedValue())
                        .contains(value.trim());

                default:
                    log.warn("Unknown operator: {}", condition.getOperator());
                    return false;
            }
        } catch(Exception e) {
            log.error("Error evaluating condition: {}", condition.getId(), e);
            return false;
        }
    }

    /**
     * Extract value based on condition source
     * @param condition The condition containing source information
     * @param context Flow context
     * @return Extracted value as string
     */
    private String extractValue(RouteCondition condition, FlowContext context) {
        String sourcePath = condition.getSourcePath();
        if(sourcePath == null) {
            // Fall back to fieldPath for backward compatibility
            sourcePath = condition.getFieldPath();
        }
        if(sourcePath == null) return null;

        RouteCondition.SourceType sourceType = condition.getSourceType();
        if(sourceType == null) {
            // Default to VARIABLE for backward compatibility
            sourceType = RouteCondition.SourceType.VARIABLE;
        }

        switch(sourceType) {
            case HEADER:
                return context.getHeader(sourcePath);

            case VARIABLE:
                Object var = context.getVariable(sourcePath);
                return var != null ? var.toString() : null;

            case XPATH:
                Object payload = context.getPayload();
                if(payload instanceof String) {
                    return contextService.extractValueFromXml((String) payload, sourcePath);
                }
                return null;

            case JSONPATH:
                Object jsonPayload = context.getPayload();
                if(jsonPayload instanceof String) {
                    return contextService.extractValueFromJson((String) jsonPayload, sourcePath);
                }
                return null;

            case CONSTANT:
                return sourcePath; // The path itself is the constant value

            default:
                return null;
        }
    }

    /**
     * Execute choice routing(if - else style)
     * @param config Router configuration
     * @param context Flow context
     * @return List of target step IDs
     */
    public List<String> executeChoiceRouting(RouterConfiguration config, FlowContext context) {
        for(RouteChoice choice : config.getChoices()) {
            if(contextService.evaluateCondition(context, choice.getCondition())) {
                return Collections.singletonList(choice.getTargetStepId());
            }
        }

        // Default choice
        return config.getChoices().stream()
            .filter(RouteChoice::isDefault)
            .map(RouteChoice::getTargetStepId)
            .collect(Collectors.toList());
    }

    /**
     * Execute content - based routing
     * @param config Router configuration
     * @param context Flow context
     * @return List of target step IDs
     */
    public List<String> executeContentBasedRouting(RouterConfiguration config, FlowContext context) {
        String value = extractValueForRouting(config, context);

        if(value != null && config.getContentRoutes().containsKey(value)) {
            return Collections.singletonList(config.getContentRoutes().get(value));
        }

        // Default route
        String defaultRoute = config.getContentRoutes().get("_default");
        if(defaultRoute != null) {
            return Collections.singletonList(defaultRoute);
        }

        return Collections.emptyList();
    }

    /**
     * Execute recipient list routing(multicast)
     * @param config Router configuration
     * @param context Flow context
     * @return List of recipient IDs
     */
    public List<String> executeRecipientListRouting(RouterConfiguration config, FlowContext context) {
        if(config.getRecipients() != null) {
            return new ArrayList<>(config.getRecipients());
        }

        // Dynamic recipient list from context variable
        if(config.getRecipientListVariable() != null) {
            Object recipients = context.getVariable(config.getRecipientListVariable());
            if(recipients instanceof List) {
                return((List<?>) recipients).stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());
            }
        }

        return Collections.emptyList();
    }

    /**
     * Execute round - robin routing
     * @param config Router configuration
     * @return Target step ID
     */
    public String executeRoundRobinRouting(RouterConfiguration config) {
        if(config.getRoundRobinTargets() == null || config.getRoundRobinTargets().isEmpty()) {
            return null;
        }

        int index = routingManagementService.calculateRoundRobinIndex(
            config.getRoundRobinIndex(),
            config.getRoundRobinTargets().size()
       );
        return config.getRoundRobinTargets().get(index);
    }

    /**
     * Execute weighted routing
     * @param config Router configuration
     * @return Target step ID
     */
    public String executeWeightedRouting(RouterConfiguration config) {
        if(config.getWeightedTargets() == null || config.getWeightedTargets().isEmpty()) {
            return null;
        }

        int totalWeight = routingManagementService.calculateTotalWeight(config.getWeightedTargets());
        int randomValue = new Random().nextInt(totalWeight);

        return routingManagementService.selectWeightedTarget(config.getWeightedTargets(), randomValue);
    }

    /**
     * Extract value for routing decision
     * @param config Router configuration
     * @param context Flow context
     * @return Extracted value
     */
    private String extractValueForRouting(RouterConfiguration config, FlowContext context) {
        if(config.getExtractionPath() == null) return null;

        switch(config.getSourceType()) {
            case HEADER:
                return context.getHeader(config.getExtractionPath());

            case VARIABLE:
                Object var = context.getVariable(config.getExtractionPath());
                return var != null ? var.toString() : null;

            case XPATH:
                Object payload = context.getPayload();
                if(payload instanceof String) {
                    return contextService.extractValueFromXml((String) payload, config.getExtractionPath());
                }
                return null;

            case JSONPATH:
                Object jsonPayload = context.getPayload();
                if(jsonPayload instanceof String) {
                    return contextService.extractValueFromJson((String) jsonPayload, config.getExtractionPath());
                }
                return null;

            default:
                return null;
        }
    }
}
