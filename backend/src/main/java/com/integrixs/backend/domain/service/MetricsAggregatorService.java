package com.integrixs.backend.domain.service;

import com.integrixs.backend.domain.repository.IntegrationFlowRepository;
import com.integrixs.backend.domain.repository.CommunicationAdapterRepository;
import com.integrixs.data.model.IntegrationFlow;
import com.integrixs.data.model.CommunicationAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Domain service for aggregating integration metrics
 * Contains business logic for calculating flow and adapter statistics
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MetricsAggregatorService {

    private final IntegrationFlowRepository flowRepository;
    private final CommunicationAdapterRepository adapterRepository;

    /**
     * Count active integration flows
     */
    public int countActiveFlows() {
        List<IntegrationFlow> activeFlows = flowRepository.findByIsActive(true);
        return activeFlows.size();
    }

    /**
     * Count active flows for a specific business component
     */
    public int countActiveFlowsByBusinessComponent(UUID businessComponentId) {
        List<IntegrationFlow> activeFlows = flowRepository.findByIsActive(true);

        return(int) activeFlows.stream()
            .filter(flow -> isFlowAssociatedWithBusinessComponent(flow, businessComponentId))
            .count();
    }

    /**
     * Count total flows including inactive ones
     */
    public int countTotalFlows() {
        return flowRepository.findAll().size();
    }

    /**
     * Count flows with errors(based on error threshold)
     */
    public int countFlowsWithErrors(long errorThreshold) {
        // In a real implementation, this would query flow execution statistics
        // For now, return 0 as we don't have error tracking in the domain model
        log.debug("Counting flows with error count > {}", errorThreshold);
        return 0;
    }

    /**
     * Check if a flow is associated with a specific business component
     */
    private boolean isFlowAssociatedWithBusinessComponent(IntegrationFlow flow, UUID businessComponentId) {
        // Check source adapter
        boolean sourceMatch = adapterRepository.findById(flow.getInboundAdapterId())
            .map(adapter -> {
                if(adapter.getBusinessComponent() != null) {
                    return businessComponentId.equals(adapter.getBusinessComponent().getId());
                }
                return false;
            })
            .orElse(false);

        // Check target adapter
        boolean targetMatch = adapterRepository.findById(flow.getOutboundAdapterId())
            .map(adapter -> {
                if(adapter.getBusinessComponent() != null) {
                    return businessComponentId.equals(adapter.getBusinessComponent().getId());
                }
                return false;
            })
            .orElse(false);

        return sourceMatch || targetMatch;
    }

    /**
     * Calculate average processing time across all flows
     * This would typically query execution metrics
     */
    public long calculateAverageProcessingTime() {
        // Mock implementation - in reality would query execution metrics
        return 250L; // milliseconds
    }

    /**
     * Calculate average processing time for a business component
     */
    public long calculateAverageProcessingTimeForComponent(UUID businessComponentId) {
        // Mock implementation - in reality would query execution metrics
        return 275L; // milliseconds
    }
}
