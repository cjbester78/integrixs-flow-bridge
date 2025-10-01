package com.integrixs.backend.application.service;

import com.integrixs.backend.api.controller.OrchestrationTargetController.TargetOrderRequest;
import com.integrixs.backend.api.dto.request.CreateOrchestrationTargetRequest;
import com.integrixs.backend.api.dto.request.UpdateOrchestrationTargetRequest;
import com.integrixs.backend.api.dto.response.OrchestrationTargetResponse;
import com.integrixs.backend.exception.ResourceNotFoundException;
import com.integrixs.backend.exception.ValidationException;
import com.integrixs.backend.logging.BusinessOperation;
import com.integrixs.data.model.CommunicationAdapter;
import com.integrixs.data.model.IntegrationFlow;
import com.integrixs.data.model.OrchestrationTarget;
import com.integrixs.data.model.User;
import com.integrixs.data.sql.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for managing orchestration targets
 */
@Service
public class OrchestrationTargetService {

    private static final Logger log = LoggerFactory.getLogger(OrchestrationTargetService.class);


    private final OrchestrationTargetSqlRepository orchestrationTargetRepository;
    private final IntegrationFlowSqlRepository integrationFlowRepository;
    private final CommunicationAdapterSqlRepository adapterRepository;
    private final TargetFieldMappingSqlRepository fieldMappingRepository;
    private final UserSqlRepository userRepository;
    private final ObjectMapper objectMapper;

    public OrchestrationTargetService(OrchestrationTargetSqlRepository orchestrationTargetRepository,
                                    IntegrationFlowSqlRepository integrationFlowRepository,
                                    CommunicationAdapterSqlRepository adapterRepository,
                                    TargetFieldMappingSqlRepository fieldMappingRepository,
                                    UserSqlRepository userRepository,
                                    ObjectMapper objectMapper) {
        this.orchestrationTargetRepository = orchestrationTargetRepository;
        this.integrationFlowRepository = integrationFlowRepository;
        this.adapterRepository = adapterRepository;
        this.fieldMappingRepository = fieldMappingRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Get all targets for a flow
     */
    @BusinessOperation(value = "ORCHESTRATION.TARGET.LIST", module = "OrchestrationManagement")
    public List<OrchestrationTargetResponse> getFlowTargets(String flowId) {
        UUID flowUuid = parseUuid(flowId, "flow");
        IntegrationFlow flow = findFlowWithAccess(flowUuid);

        List<OrchestrationTarget> targets = orchestrationTargetRepository.findByFlowIdOrderByExecutionOrder(flowUuid);

        return targets.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get a specific target
     */
    @BusinessOperation(value = "ORCHESTRATION.TARGET.GET", module = "OrchestrationManagement")
    public Optional<OrchestrationTargetResponse> getTarget(String flowId, String targetId) {
        UUID flowUuid = parseUuid(flowId, "flow");
        UUID targetUuid = parseUuid(targetId, "target");

        findFlowWithAccess(flowUuid);

        return orchestrationTargetRepository.findById(targetUuid)
                .filter(target -> target.getFlow().getId().equals(flowUuid))
                .map(this::toResponse);
    }

    /**
     * Add a target to a flow
     */
    @BusinessOperation(value = "ORCHESTRATION.TARGET.CREATE", module = "OrchestrationManagement", logInput = true)
    public OrchestrationTargetResponse addTarget(String flowId, CreateOrchestrationTargetRequest request) {
        UUID flowUuid = parseUuid(flowId, "flow");
        IntegrationFlow flow = findFlowWithWriteAccess(flowUuid);

        // Validate adapter exists and is active
        UUID adapterUuid = parseUuid(request.getTargetAdapterId(), "adapter");
        CommunicationAdapter adapter = adapterRepository.findById(adapterUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Adapter not found: " + request.getTargetAdapterId()));

        if(!adapter.isActive()) {
            throw new ValidationException("Cannot add inactive adapter as target");
        }

        // Check if adapter is already a target
        if(orchestrationTargetRepository.existsByFlowIdAndTargetAdapterId(flowUuid, adapterUuid)) {
            throw new ValidationException("Adapter is already a target for this flow");
        }

        // Determine execution order if not specified
        Integer executionOrder = request.getExecutionOrder();
        if(executionOrder == null) {
            Integer maxOrder = orchestrationTargetRepository.getMaxExecutionOrder(flowUuid);
            executionOrder = (maxOrder != null) ? maxOrder + 1 : 0;
        }

        OrchestrationTarget target = OrchestrationTarget.builder()
                .flow(flow)
                .targetAdapter(adapter)
                .executionOrder(executionOrder)
                .parallel(request.isParallel())
                .routingCondition(request.getRoutingCondition())
                .conditionType(request.getConditionType())
                .awaitResponse(request.isAwaitResponse())
                .timeoutMs(request.getTimeoutMs())
                .errorStrategy(request.getErrorStrategy())
                .active(request.isActive())
                .configuration(convertMapToJson(request.getConfiguration()))
                .description(request.getDescription())
                .build();

        // Set retry policy
        if(request.getRetryPolicy() != null) {
            var retryDto = request.getRetryPolicy();
            target.setRetryPolicy(OrchestrationTarget.RetryPolicy.builder()
                    .maxAttempts(retryDto.getMaxAttempts())
                    .retryDelayMs(retryDto.getRetryDelayMs())
                    .backoffMultiplier(retryDto.getBackoffMultiplier())
                    .maxRetryDelayMs(retryDto.getMaxRetryDelayMs())
                    .retryOnErrors(retryDto.getRetryOnErrors())
                    .build());
        }

        // Set structure IDs if provided
        if(request.getStructureId() != null) {
            target.setStructureId(parseUuid(request.getStructureId(), "structure"));
        }
        if(request.getResponseStructureId() != null) {
            target.setResponseStructureId(parseUuid(request.getResponseStructureId(), "responseStructure"));
        }

        // Set user info
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            target.setCreatedBy(user);
            target.setUpdatedBy(user);
        }

        target = orchestrationTargetRepository.save(target);

        log.info("#2.0# Orchestration target added - Flow: {}, Adapter: {}, Order: {}",
                flow.getName(), adapter.getName(), executionOrder);

        return toResponse(target);
    }

    /**
     * Update a target
     */
    @BusinessOperation(value = "ORCHESTRATION.TARGET.UPDATE", module = "OrchestrationManagement", logInput = true)
    public Optional<OrchestrationTargetResponse> updateTarget(String flowId, String targetId,
                                                            UpdateOrchestrationTargetRequest request) {
        UUID flowUuid = parseUuid(flowId, "flow");
        UUID targetUuid = parseUuid(targetId, "target");

        findFlowWithWriteAccess(flowUuid);

        return orchestrationTargetRepository.findById(targetUuid)
                .filter(target -> target.getFlow().getId().equals(flowUuid))
                .map(target -> {
                    // Update fields
                    if(request.getExecutionOrder() != null) {
                        target.setExecutionOrder(request.getExecutionOrder());
                    }
                    if(request.getParallel() != null) {
                        target.setParallel(request.getParallel());
                    }
                    if(request.getRoutingCondition() != null) {
                        target.setRoutingCondition(request.getRoutingCondition());
                    }
                    if(request.getConditionType() != null) {
                        target.setConditionType(request.getConditionType());
                    }
                    if(request.getAwaitResponse() != null) {
                        target.setAwaitResponse(request.getAwaitResponse());
                    }
                    if(request.getTimeoutMs() != null) {
                        target.setTimeoutMs(request.getTimeoutMs());
                    }
                    if(request.getErrorStrategy() != null) {
                        target.setErrorStrategy(request.getErrorStrategy());
                    }
                    if(request.getConfiguration() != null) {
                        target.setConfiguration(convertMapToJson(request.getConfiguration()));
                    }
                    if(request.getDescription() != null) {
                        target.setDescription(request.getDescription());
                    }

                    // Update retry policy
                    if(request.getRetryPolicy() != null) {
                        var retryDto = request.getRetryPolicy();
                        var currentRetry = target.getRetryPolicy() != null ?
                                target.getRetryPolicy() : new OrchestrationTarget.RetryPolicy();

                        if(retryDto.getMaxAttempts() != null) {
                            currentRetry.setMaxAttempts(retryDto.getMaxAttempts());
                        }
                        if(retryDto.getRetryDelayMs() != null) {
                            currentRetry.setRetryDelayMs(retryDto.getRetryDelayMs());
                        }
                        if(retryDto.getBackoffMultiplier() != null) {
                            currentRetry.setBackoffMultiplier(retryDto.getBackoffMultiplier());
                        }
                        if(retryDto.getMaxRetryDelayMs() != null) {
                            currentRetry.setMaxRetryDelayMs(retryDto.getMaxRetryDelayMs());
                        }
                        if(retryDto.getRetryOnErrors() != null) {
                            currentRetry.setRetryOnErrors(retryDto.getRetryOnErrors());
                        }

                        target.setRetryPolicy(currentRetry);
                    }

                    // Update structure IDs
                    if(request.getStructureId() != null) {
                        target.setStructureId(request.getStructureId().isEmpty() ?
                                null : parseUuid(request.getStructureId(), "structure"));
                    }
                    if(request.getResponseStructureId() != null) {
                        target.setResponseStructureId(request.getResponseStructureId().isEmpty() ?
                                null : parseUuid(request.getResponseStructureId(), "responseStructure"));
                    }

                    // Update user info
                    String username = SecurityContextHolder.getContext().getAuthentication().getName();
                    Optional<User> updateUserOptional = userRepository.findByUsername(username);
                    if (updateUserOptional.isPresent()) {
                        target.setUpdatedBy(updateUserOptional.get());
                    }

                    target = orchestrationTargetRepository.save(target);

                    log.info("#2.0# Orchestration target updated - Target: {}, Flow: {}",
                            targetId, target.getFlow().getName());

                    return toResponse(target);
                });
    }

    /**
     * Remove a target
     */
    @BusinessOperation(value = "ORCHESTRATION.TARGET.DELETE", module = "OrchestrationManagement")
    public boolean removeTarget(String flowId, String targetId) {
        UUID flowUuid = parseUuid(flowId, "flow");
        UUID targetUuid = parseUuid(targetId, "target");

        findFlowWithWriteAccess(flowUuid);

        return orchestrationTargetRepository.findById(targetUuid)
                .filter(target -> target.getFlow().getId().equals(flowUuid))
                .map(target -> {
                    // Delete associated field mappings first
                    fieldMappingRepository.deleteByOrchestrationTargetId(targetUuid);

                    // Delete the target
                    orchestrationTargetRepository.delete(target);

                    // Reorder remaining targets
                    reorderTargetsAfterDeletion(flowUuid, target.getExecutionOrder());

                    log.info("#2.0# Orchestration target removed - Target: {}, Flow: {}",
                            targetId, target.getFlow().getName());

                    return true;
                })
                .orElse(false);
    }

    /**
     * Activate a target
     */
    @BusinessOperation(value = "ORCHESTRATION.TARGET.ACTIVATE", module = "OrchestrationManagement")
    public Optional<OrchestrationTargetResponse> activateTarget(String flowId, String targetId) {
        return updateTargetStatus(flowId, targetId, true);
    }

    /**
     * Deactivate a target
     */
    @BusinessOperation(value = "ORCHESTRATION.TARGET.DEACTIVATE", module = "OrchestrationManagement")
    public Optional<OrchestrationTargetResponse> deactivateTarget(String flowId, String targetId) {
        return updateTargetStatus(flowId, targetId, false);
    }

    /**
     * Reorder targets
     */
    @BusinessOperation(value = "ORCHESTRATION.TARGET.REORDER", module = "OrchestrationManagement", logInput = true)
    public List<OrchestrationTargetResponse> reorderTargets(String flowId, List<TargetOrderRequest> orderRequests) {
        UUID flowUuid = parseUuid(flowId, "flow");
        findFlowWithWriteAccess(flowUuid);

        // Validate all target IDs belong to the flow
        Map<UUID, Integer> orderMap = new HashMap<>();
        for(TargetOrderRequest request : orderRequests) {
            UUID targetUuid = parseUuid(request.getTargetId(), "target");
            orderMap.put(targetUuid, request.getExecutionOrder());
        }

        List<OrchestrationTarget> targets = orchestrationTargetRepository.findByFlowId(flowUuid);

        // Validate all targets are included
        if(targets.size() != orderRequests.size()) {
            throw new ValidationException("All targets must be included in reorder request");
        }

        // Update execution orders
        for(OrchestrationTarget target : targets) {
            Integer newOrder = orderMap.get(target.getId());
            if(newOrder == null) {
                throw new ValidationException("Target not found in reorder request: " + target.getId());
            }
            target.setExecutionOrder(newOrder);
        }

        // Save all targets
        targets = orchestrationTargetRepository.saveAll(targets);

        log.info("#2.0# Orchestration targets reordered - Flow: {}, Count: {}",
                flowId, targets.size());

        return targets.stream()
                .sorted(Comparator.comparing(OrchestrationTarget::getExecutionOrder))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update target status(activate/deactivate)
     */
    private Optional<OrchestrationTargetResponse> updateTargetStatus(String flowId, String targetId, boolean active) {
        UUID flowUuid = parseUuid(flowId, "flow");
        UUID targetUuid = parseUuid(targetId, "target");

        findFlowWithWriteAccess(flowUuid);

        return orchestrationTargetRepository.findById(targetUuid)
                .filter(target -> target.getFlow().getId().equals(flowUuid))
                .map(target -> {
                    target.setActive(active);

                    String username = SecurityContextHolder.getContext().getAuthentication().getName();
                    Optional<User> orderUpdateUserOptional = userRepository.findByUsername(username);
                    if (orderUpdateUserOptional.isPresent()) {
                        target.setUpdatedBy(orderUpdateUserOptional.get());
                    }

                    target = orchestrationTargetRepository.save(target);

                    log.info("#2.0# Orchestration target {} - Target: {}, Flow: {}",
                            active ? "activated" : "deactivated", targetId, target.getFlow().getName());

                    return toResponse(target);
                });
    }

    /**
     * Reorder targets after deletion
     */
    private void reorderTargetsAfterDeletion(UUID flowId, Integer deletedOrder) {
        List<OrchestrationTarget> targets = orchestrationTargetRepository.findByFlowId(flowId);

        for(OrchestrationTarget target : targets) {
            if(target.getExecutionOrder() > deletedOrder) {
                target.setExecutionOrder(target.getExecutionOrder() - 1);
            }
        }

        orchestrationTargetRepository.saveAll(targets);
    }

    /**
     * Find flow and check read access
     */
    private IntegrationFlow findFlowWithAccess(UUID flowId) {
        IntegrationFlow flow = integrationFlowRepository.findById(flowId)
                .orElseThrow(() -> new ResourceNotFoundException("Integration flow not found: " + flowId));

        // Check user has access to the business component
        // TODO: Implement proper access control based on business component membership
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> currentUserOptional = userRepository.findByUsername(username);
        boolean hasAccess = currentUserOptional.isPresent() && flow.getBusinessComponent() != null;

        if(!hasAccess) {
            throw new AccessDeniedException("Access denied to flow: " + flowId);
        }

        return flow;
    }

    /**
     * Find flow and check write access
     */
    private IntegrationFlow findFlowWithWriteAccess(UUID flowId) {
        IntegrationFlow flow = findFlowWithAccess(flowId);

        // Additional check for write access(could be role - based)
        // For now, same as read access

        return flow;
    }

    /**
     * Parse UUID from string
     */
    private UUID parseUuid(String value, String type) {
        try {
            return UUID.fromString(value);
        } catch(IllegalArgumentException e) {
            throw new ValidationException("Invalid " + type + " ID: " + value);
        }
    }

    /**
     * Convert entity to response DTO
     */
    private OrchestrationTargetResponse toResponse(OrchestrationTarget target) {
        // Get mapping count
        long mappingCount = fieldMappingRepository.countByOrchestrationTargetId(target.getId());

        // Build adapter summary
        CommunicationAdapter adapter = target.getTargetAdapter();
        OrchestrationTargetResponse.AdapterSummary adapterSummary = OrchestrationTargetResponse.AdapterSummary.builder()
                .id(adapter.getId().toString())
                .name(adapter.getName())
                .type(adapter.getType().name())
                .mode(adapter.getMode() != null ? adapter.getMode().name() : null)
                .active(adapter.isActive())
                .build();

        // Build retry policy
        OrchestrationTargetResponse.RetryPolicyResponse retryPolicy = null;
        if(target.getRetryPolicy() != null) {
            var rp = target.getRetryPolicy();
            retryPolicy = OrchestrationTargetResponse.RetryPolicyResponse.builder()
                    .maxAttempts(rp.getMaxAttempts())
                    .retryDelayMs(rp.getRetryDelayMs())
                    .backoffMultiplier(rp.getBackoffMultiplier())
                    .maxRetryDelayMs(rp.getMaxRetryDelayMs())
                    .retryOnErrors(rp.getRetryOnErrors())
                    .build();
        }

        return OrchestrationTargetResponse.builder()
                .id(target.getId().toString())
                .flowId(target.getFlow().getId().toString())
                .targetAdapter(adapterSummary)
                .executionOrder(target.getExecutionOrder())
                .parallel(target.isParallel())
                .routingCondition(target.getRoutingCondition())
                .conditionType(target.getConditionType())
                .structureId(target.getStructureId() != null ? target.getStructureId().toString() : null)
                .responseStructureId(target.getResponseStructureId() != null ? target.getResponseStructureId().toString() : null)
                .awaitResponse(target.isAwaitResponse())
                .timeoutMs(target.getTimeoutMs())
                .retryPolicy(retryPolicy)
                .errorStrategy(target.getErrorStrategy())
                .active(target.isActive())
                .configuration(convertJsonToMap(target.getConfiguration()))
                .description(target.getDescription())
                .createdAt(target.getCreatedAt())
                .updatedAt(target.getUpdatedAt())
                .mappingCount(mappingCount)
                .build();
    }

    /**
     * Convert Map to JSON string
     */
    private String convertMapToJson(Map<String, Object> map) {
        if(map == null || map.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(map);
        } catch(JsonProcessingException e) {
            log.warn("Failed to convert map to JSON", e);
            return null;
        }
    }

    /**
     * Convert JSON string to Map
     */
    private Map<String, Object> convertJsonToMap(String json) {
        if(json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, Map.class);
        } catch(JsonProcessingException e) {
            log.warn("Failed to convert JSON to map", e);
            return null;
        }
    }
}
