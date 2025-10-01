package com.integrixs.backend.application.service;

import com.integrixs.backend.api.dto.RouteDTO;
import com.integrixs.backend.api.dto.RoutingDecisionDTO;
import com.integrixs.backend.api.dto.RouterConfigDTO;
import com.integrixs.backend.domain.model.RouterConfiguration;
import com.integrixs.backend.domain.model.RouterConfiguration.RouteChoice;
import com.integrixs.backend.domain.model.RoutingDecision;
import com.integrixs.backend.domain.service.RoutingManagementService;
import com.integrixs.backend.infrastructure.routing.RoutingEvaluator;
import com.integrixs.backend.service.FlowContextService;
import com.integrixs.backend.service.FlowContextService.FlowContext;
import com.integrixs.data.model.FlowRoute;
import com.integrixs.data.model.IntegrationFlow;
import com.integrixs.data.model.RouteCondition;
import com.integrixs.data.sql.repository.FlowRouteSqlRepository;
import com.integrixs.data.sql.repository.IntegrationFlowSqlRepository;
import com.integrixs.data.sql.repository.RouteConditionSqlRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application service for conditional routing and decision making
 */
@Service
public class RoutingApplicationService {

    private static final Logger log = LoggerFactory.getLogger(RoutingApplicationService.class);

    private final RoutingManagementService routingManagementService;
    private final RoutingEvaluator routingEvaluator;
    private final FlowContextService contextService;
    private final FlowRouteSqlRepository routeRepository;
    private final RouteConditionSqlRepository conditionRepository;
    private final IntegrationFlowSqlRepository flowRepository;

    public RoutingApplicationService(RoutingManagementService routingManagementService,
                                   RoutingEvaluator routingEvaluator,
                                   FlowContextService contextService,
                                   FlowRouteSqlRepository routeRepository,
                                   RouteConditionSqlRepository conditionRepository,
                                   IntegrationFlowSqlRepository flowRepository) {
        this.routingManagementService = routingManagementService;
        this.routingEvaluator = routingEvaluator;
        this.contextService = contextService;
        this.routeRepository = routeRepository;
        this.conditionRepository = conditionRepository;
        this.flowRepository = flowRepository;
    }

    /**
     * Evaluate routing decision for a flow step
     * @param flowId The flow ID
     * @param stepId The step ID
     * @param context The flow context
     * @return Routing decision
     */
    public RoutingDecisionDTO evaluateRouting(String flowId, String stepId, FlowContext context) {
        try {
            // Get routes for this step
            List<FlowRoute> routes = routeRepository.findByFlowIdAndSourceStep(
                UUID.fromString(flowId),
                stepId
           );

            if(routes.isEmpty()) {
                return convertToDTO(routingManagementService.createRoutingDecision(null));
            }

            // Sort routes by priority
            routes = routingManagementService.sortRoutesByPriority(routes);

            // Evaluate each route's conditions
            for(FlowRoute route : routes) {
                List<RouteCondition> conditions = conditionRepository.findByFlowRouteId(route.getId());
                if(routingEvaluator.evaluateRouteConditions(route, conditions, context)) {
                    log.info("Route matched: {} for step {}", route.getRouteName(), stepId);
                    return convertToDTO(routingManagementService.createRoutingDecision(route));
                }
            }

            // Find default route
            FlowRoute defaultRoute = routes.stream()
                .filter(routingManagementService::isDefaultRoute)
                .findFirst()
                .orElse(null);

            if(defaultRoute != null) {
                log.info("Using default route: {} for step {}", defaultRoute.getRouteName(), stepId);
                return convertToDTO(routingManagementService.createRoutingDecision(defaultRoute));
            }

            log.warn("No matching route found for step {}", stepId);
            return convertToDTO(routingManagementService.createNoMatchDecision());

        } catch(Exception e) {
            log.error("Error evaluating routing for step {}", stepId, e);
            return convertToDTO(routingManagementService.createErrorDecision(e.getMessage()));
        }
    }

    /**
     * Create a choice router configuration
     * @param routerId Router ID
     * @param choices List of route choices
     * @return Router configuration
     */
    public RouterConfigDTO createChoiceRouter(String routerId, List<Map<String, Object>> choices) {
        RouterConfiguration config = new RouterConfiguration();
        config.setRouterId(routerId);
        config.setRouterType(RouterConfiguration.RouterType.CHOICE);

        List<RouteChoice> routeChoices = choices.stream()
            .map(choice -> new RouteChoice(
                (String) choice.get("condition"),
                (String) choice.get("targetStepId"),
                Boolean.TRUE.equals(choice.get("isDefault"))
           ))
            .collect(Collectors.toList());

        config.setChoices(routeChoices);
        routingManagementService.validateRouterConfig(config);

        return convertToDTO(config);
    }

    /**
     * Create a content - based router configuration
     * @param routerId Router ID
     * @param extractionPath Path to extract value
     * @param sourceType Source type for extraction
     * @param routes Content routes map
     * @return Router configuration
     */
    public RouterConfigDTO createContentRouter(String routerId, String extractionPath,
                                             String sourceType, Map<String, String> routes) {
        RouterConfiguration config = new RouterConfiguration();
        config.setRouterId(routerId);
        config.setRouterType(RouterConfiguration.RouterType.CONTENT_BASED);
        config.setExtractionPath(extractionPath);
        config.setSourceType(RouterConfiguration.SourceType.valueOf(sourceType));
        config.setContentRoutes(routes);

        routingManagementService.validateRouterConfig(config);

        return convertToDTO(config);
    }

    /**
     * Execute routing and return target step IDs
     * @param config Router configuration
     * @param context Flow context
     * @return Future with list of target steps
     */
    public CompletableFuture<List<String>> executeRouting(RouterConfigDTO configDto, FlowContext context) {
        return CompletableFuture.supplyAsync(() -> {
            RouterConfiguration config = convertFromDTO(configDto);
            routingManagementService.validateRouterConfig(config);

            List<String> targetSteps = new ArrayList<>();

            switch(config.getRouterType()) {
                case CHOICE:
                    targetSteps.addAll(routingEvaluator.executeChoiceRouting(config, context));
                    break;

                case CONTENT_BASED:
                    targetSteps.addAll(routingEvaluator.executeContentBasedRouting(config, context));
                    break;

                case RECIPIENT_LIST:
                    targetSteps.addAll(routingEvaluator.executeRecipientListRouting(config, context));
                    break;

                case ROUND_ROBIN:
                    String roundRobinTarget = routingEvaluator.executeRoundRobinRouting(config);
                    if(roundRobinTarget != null) {
                        targetSteps.add(roundRobinTarget);
                    }
                    break;

                case WEIGHTED:
                    String weightedTarget = routingEvaluator.executeWeightedRouting(config);
                    if(weightedTarget != null) {
                        targetSteps.add(weightedTarget);
                    }
                    break;

                default:
                    log.warn("Unknown router type: {}", config.getRouterType());
            }

            log.info("Router {} selected targets: {}", config.getRouterId(), targetSteps);
            return targetSteps;
        });
    }

    /**
     * Get all routes for a flow
     * @param flowId Flow ID
     * @return List of routes
     */
    public List<RouteDTO> getRoutesForFlow(String flowId) {
        UUID flowUuid = UUID.fromString(flowId);
        IntegrationFlow flow = flowRepository.findById(flowUuid)
            .orElseThrow(() -> new IllegalArgumentException("Flow not found: " + flowId));

        List<FlowRoute> routes = routeRepository.findByFlowId(flowUuid);
        return routes.stream()
            .map(this::convertRouteToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Create a new route
     * @param flowId Flow ID
     * @param routeDto Route data
     * @return Created route
     */
    public RouteDTO createRoute(String flowId, RouteDTO routeDto) {
        UUID flowUuid = UUID.fromString(flowId);
        IntegrationFlow flow = flowRepository.findById(flowUuid)
            .orElseThrow(() -> new IllegalArgumentException("Flow not found: " + flowId));

        FlowRoute route = new FlowRoute();
        route.setFlow(flow);
        route.setRouteName(routeDto.getRouteName());
        route.setSourceStep(routeDto.getSourceStep());
        route.setTargetStep(routeDto.getTargetStep());
        route.setPriority(routeDto.getPriority());
        route.setConditionOperator(routeDto.getConditionOperator());
        route.setActive(routeDto.isEnabled());

        routingManagementService.validateRoute(route);
        route = routeRepository.save(route);

        return convertRouteToDTO(route);
    }

    /**
     * Update an existing route
     * @param routeId Route ID
     * @param routeDto Updated route data
     * @return Updated route
     */
    public RouteDTO updateRoute(String routeId, RouteDTO routeDto) {
        FlowRoute route = routeRepository.findById(UUID.fromString(routeId))
            .orElseThrow(() -> new IllegalArgumentException("Route not found: " + routeId));

        route.setRouteName(routeDto.getRouteName());
        route.setSourceStep(routeDto.getSourceStep());
        route.setTargetStep(routeDto.getTargetStep());
        route.setPriority(routeDto.getPriority());
        route.setConditionOperator(routeDto.getConditionOperator());
        route.setActive(routeDto.isEnabled());

        routingManagementService.validateRoute(route);
        route = routeRepository.save(route);

        return convertRouteToDTO(route);
    }

    /**
     * Delete a route
     * @param routeId Route ID
     */
    public void deleteRoute(String routeId) {
        if(!routeRepository.existsById(UUID.fromString(routeId))) {
            throw new IllegalArgumentException("Route not found: " + routeId);
        }
        routeRepository.deleteById(UUID.fromString(routeId));
    }

    // Conversion methods

    private RoutingDecisionDTO convertToDTO(RoutingDecision decision) {
        RoutingDecisionDTO dto = new RoutingDecisionDTO();
        dto.setSuccess(decision.isSuccess());
        dto.setErrorMessage(decision.getErrorMessage());

        if(decision.hasRoute()) {
            dto.setRouteId(decision.getRoute().getId().toString());
            dto.setRouteName(decision.getRoute().getRouteName());
            dto.setTargetStep(decision.getRoute().getTargetStep());
        }

        return dto;
    }

    private RouteDTO convertRouteToDTO(FlowRoute route) {
        RouteDTO dto = new RouteDTO();
        dto.setId(route.getId().toString());
        dto.setFlowId(route.getFlow().getId().toString());
        dto.setRouteName(route.getRouteName());
        dto.setSourceStep(route.getSourceStep());
        dto.setTargetStep(route.getTargetStep());
        dto.setPriority(route.getPriority());
        dto.setConditionOperator(route.getConditionOperator());
        dto.setEnabled(route.isActive());
        return dto;
    }

    private RouterConfigDTO convertToDTO(RouterConfiguration config) {
        RouterConfigDTO dto = new RouterConfigDTO();
        dto.setRouterId(config.getRouterId());
        dto.setRouterType(config.getRouterType().name());

        if(config.getChoices() != null) {
            dto.setChoices(config.getChoices().stream()
                .map(choice -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("condition", choice.getCondition());
                    map.put("targetStepId", choice.getTargetStepId());
                    map.put("isDefault", choice.isDefault());
                    return map;
                })
                .collect(Collectors.toList()));
        }

        dto.setContentRoutes(config.getContentRoutes());
        dto.setExtractionPath(config.getExtractionPath());
        dto.setSourceType(config.getSourceType() != null ? config.getSourceType().name() : null);
        dto.setRecipients(config.getRecipients());
        dto.setRecipientListVariable(config.getRecipientListVariable());
        dto.setRoundRobinTargets(config.getRoundRobinTargets());
        dto.setWeightedTargets(config.getWeightedTargets());

        return dto;
    }

    private RouterConfiguration convertFromDTO(RouterConfigDTO dto) {
        RouterConfiguration config = new RouterConfiguration();
        config.setRouterId(dto.getRouterId());
        config.setRouterType(RouterConfiguration.RouterType.valueOf(dto.getRouterType()));

        if(dto.getChoices() != null) {
            config.setChoices(dto.getChoices().stream()
                .map(choice -> new RouteChoice(
                    (String) choice.get("condition"),
                    (String) choice.get("targetStepId"),
                    Boolean.TRUE.equals(choice.get("isDefault"))
               ))
                .collect(Collectors.toList()));
        }

        config.setContentRoutes(dto.getContentRoutes());
        config.setExtractionPath(dto.getExtractionPath());
        if(dto.getSourceType() != null) {
            config.setSourceType(RouterConfiguration.SourceType.valueOf(dto.getSourceType()));
        }
        config.setRecipients(dto.getRecipients());
        config.setRecipientListVariable(dto.getRecipientListVariable());
        config.setRoundRobinTargets(dto.getRoundRobinTargets());
        config.setWeightedTargets(dto.getWeightedTargets());

        return config;
    }
}
