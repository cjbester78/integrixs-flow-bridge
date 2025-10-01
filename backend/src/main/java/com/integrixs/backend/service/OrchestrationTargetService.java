package com.integrixs.backend.service;

import com.integrixs.data.model.OrchestrationTarget;
import com.integrixs.data.model.IntegrationFlow;
import com.integrixs.data.model.CommunicationAdapter;
import com.integrixs.data.sql.repository.OrchestrationTargetSqlRepository;
import com.integrixs.data.sql.repository.IntegrationFlowSqlRepository;
import com.integrixs.data.sql.repository.CommunicationAdapterSqlRepository;
import com.integrixs.backend.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing orchestration targets
 * Handles the configuration and execution of orchestration flows
 */
@Service("orchestrationTargetServiceImpl")
public class OrchestrationTargetService {

    private static final Logger log = LoggerFactory.getLogger(OrchestrationTargetService.class);

    private final OrchestrationTargetSqlRepository orchestrationTargetRepository;
    private final IntegrationFlowSqlRepository integrationFlowRepository;
    private final CommunicationAdapterSqlRepository communicationAdapterRepository;

    public OrchestrationTargetService(OrchestrationTargetSqlRepository orchestrationTargetRepository,
                                     IntegrationFlowSqlRepository integrationFlowRepository,
                                     CommunicationAdapterSqlRepository communicationAdapterRepository) {
        this.orchestrationTargetRepository = orchestrationTargetRepository;
        this.integrationFlowRepository = integrationFlowRepository;
        this.communicationAdapterRepository = communicationAdapterRepository;
    }

    /**
     * Create a new orchestration target
     */
    public OrchestrationTarget createTarget(OrchestrationTarget target) {
        log.info("Creating orchestration target for adapter: {}",
            target.getTargetAdapter() != null ? target.getTargetAdapter().getName() : "Unknown");

        // Validate references
        validateTarget(target);

        target.setCreatedAt(LocalDateTime.now());
        target.setUpdatedAt(LocalDateTime.now());

        return orchestrationTargetRepository.save(target);
    }

    /**
     * Get all orchestration targets
     */
    public List<OrchestrationTarget> getAllTargets() {
        return orchestrationTargetRepository.findAll();
    }

    /**
     * Get orchestration target by ID
     */
    public OrchestrationTarget getTargetById(UUID id) {
        return orchestrationTargetRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Orchestration target not found: " + id));
    }

    /**
     * Get targets for a specific flow
     */
    public List<OrchestrationTarget> getTargetsForFlow(UUID flowId) {
        return orchestrationTargetRepository.findByFlowId(flowId);
    }

    /**
     * Get targets by adapter
     */
    public List<OrchestrationTarget> getTargetsByAdapter(UUID adapterId) {
        // Use query annotation method we added
        return orchestrationTargetRepository.findByTargetAdapterId(adapterId);
    }

    /**
     * Update orchestration target
     */
    public OrchestrationTarget updateTarget(UUID id, OrchestrationTarget targetUpdate) {
        log.info("Updating orchestration target: {}", id);

        OrchestrationTarget existing = getTargetById(id);

        // Update fields
        if (targetUpdate.getDescription() != null) {
            existing.setDescription(targetUpdate.getDescription());
        }
        if (targetUpdate.getTargetAdapter() != null) {
            validateAdapterExists(targetUpdate.getTargetAdapter().getId());
            existing.setTargetAdapter(targetUpdate.getTargetAdapter());
        }
        if (targetUpdate.getRoutingCondition() != null) {
            existing.setRoutingCondition(targetUpdate.getRoutingCondition());
        }
        if (targetUpdate.getExecutionOrder() != null) {
            existing.setExecutionOrder(targetUpdate.getExecutionOrder());
        }
        if (targetUpdate.getErrorStrategy() != null) {
            existing.setErrorStrategy(targetUpdate.getErrorStrategy());
        }

        existing.setUpdatedAt(LocalDateTime.now());

        return orchestrationTargetRepository.save(existing);
    }

    /**
     * Delete orchestration target
     */
    public void deleteTarget(UUID id) {
        log.info("Deleting orchestration target: {}", id);

        OrchestrationTarget target = getTargetById(id);
        orchestrationTargetRepository.delete(target);
    }

    /**
     * Enable or disable a target
     */
    public OrchestrationTarget setTargetStatus(UUID id, boolean enabled) {
        OrchestrationTarget target = getTargetById(id);
        target.setStatus(enabled ? "ACTIVE" : "INACTIVE");
        target.setUpdatedAt(LocalDateTime.now());
        return orchestrationTargetRepository.save(target);
    }

    /**
     * Get active targets for orchestration
     */
    public List<OrchestrationTarget> getActiveTargets() {
        return orchestrationTargetRepository.findByStatus("ACTIVE");
    }

    /**
     * Execute orchestration for a message
     */
    public void executeOrchestration(UUID sourceAdapterId, String messageContent, Map<String, Object> metadata) {
        log.debug("Executing orchestration for adapter: {}", sourceAdapterId);

        // Find applicable targets
        List<OrchestrationTarget> targets = orchestrationTargetRepository.findBySourceAdapterId(sourceAdapterId)
            .stream()
            .filter(t -> "ACTIVE".equals(t.getStatus()))
            .sorted((a, b) -> Integer.compare(b.getPriority(), a.getPriority()))
            .collect(Collectors.toList());

        for (OrchestrationTarget target : targets) {
            try {
                if (evaluateRoutingRules(target, messageContent, metadata)) {
                    executeTarget(target, messageContent, metadata);
                }
            } catch (Exception e) {
                handleTargetError(target, e);
            }
        }
    }

    /**
     * Validate target configuration
     */
    private void validateTarget(OrchestrationTarget target) {
        if (target.getName() == null || target.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Target name is required");
        }

        // Validate either flow or adapter is set
        if (target.getTargetFlow() == null && target.getTargetAdapter() == null) {
            throw new IllegalArgumentException("Either target flow or adapter must be specified");
        }

        if (target.getTargetFlow() != null) {
            validateFlowExists(target.getTargetFlow().getId());
        }

        if (target.getTargetAdapter() != null) {
            validateAdapterExists(target.getTargetAdapter().getId());
        }
    }

    /**
     * Validate flow exists
     */
    private void validateFlowExists(UUID flowId) {
        if (!integrationFlowRepository.existsById(flowId)) {
            throw new ResourceNotFoundException("Integration flow not found: " + flowId);
        }
    }

    /**
     * Validate adapter exists
     */
    private void validateAdapterExists(UUID adapterId) {
        if (!communicationAdapterRepository.existsById(adapterId)) {
            throw new ResourceNotFoundException("Communication adapter not found: " + adapterId);
        }
    }

    /**
     * Evaluate routing rules for a target
     */
    private boolean evaluateRoutingRules(OrchestrationTarget target, String messageContent, Map<String, Object> metadata) {
        // TODO: Implement routing rule evaluation
        // For now, return true if no rules are defined
        if (target.getRoutingCondition() == null || target.getRoutingCondition().isEmpty()) {
            return true;
        }

        // Implement rule evaluation logic here
        return true;
    }

    /**
     * Execute a specific target
     */
    private void executeTarget(OrchestrationTarget target, String messageContent, Map<String, Object> metadata) {
        log.debug("Executing target: {} for adapter: {}", target.getId(),
                  target.getTargetAdapter() != null ? target.getTargetAdapter().getName() : "Unknown");

        // TODO: Implement actual target execution
        // This would involve calling the appropriate flow or adapter
    }

    /**
     * Handle errors during target execution
     */
    private void handleTargetError(OrchestrationTarget target, Exception error) {
        log.error("Error executing target {}: {}", target.getId(), error.getMessage());

        // TODO: Implement error handling based on target configuration
        if (target.getErrorStrategy() != null) {
            // Handle based on error handling strategy
        }
    }
}