package com.integrixs.backend.domain.service;

import com.integrixs.data.model.IntegrationFlow;
import com.integrixs.data.sql.repository.IntegrationFlowSqlRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Domain service for flow validation logic
 */
@Service
public class FlowValidationService {

    private final IntegrationFlowSqlRepository flowRepository;

    public FlowValidationService(IntegrationFlowSqlRepository flowRepository) {
        this.flowRepository = flowRepository;
    }

    /**
     * Validates that flow name is unique
     */
    public void validateFlowNameUniqueness(String name, UUID excludeId) {
        boolean exists = excludeId == null ?
            flowRepository.existsByName(name) :
            flowRepository.existsByNameAndIdNot(name, excludeId);

        if(exists) {
            throw new IllegalArgumentException("A flow with the name '" + name + "' already exists");
        }
    }

    /**
     * Validates flow constraints
     */
    public void validateFlow(IntegrationFlow flow) {
        if(flow.getName() == null || flow.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Flow name cannot be empty");
        }

        if(flow.getInboundAdapterId() == null && flow.getOutboundAdapterId() == null) {
            throw new IllegalArgumentException("At least one adapter must be configured");
        }
    }

    /**
     * Validates if flow can be activated
     */
    public void validateFlowActivation(IntegrationFlow flow) {
        if(flow.getInboundAdapterId() == null || flow.getOutboundAdapterId() == null) {
            throw new IllegalStateException("Both source and target adapters must be configured before activation");
        }
    }
}
