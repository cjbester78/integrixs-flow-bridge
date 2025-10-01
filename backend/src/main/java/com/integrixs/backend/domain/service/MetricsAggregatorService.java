package com.integrixs.backend.domain.service;

import com.integrixs.data.sql.repository.IntegrationFlowSqlRepository;
import com.integrixs.data.sql.repository.CommunicationAdapterSqlRepository;
import com.integrixs.data.model.IntegrationFlow;
import com.integrixs.data.model.CommunicationAdapter;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Domain service for aggregating integration metrics
 * Contains business logic for calculating flow and adapter statistics
 */
@Service
public class MetricsAggregatorService {

    private static final Logger log = LoggerFactory.getLogger(MetricsAggregatorService.class);


    private final IntegrationFlowSqlRepository flowRepository;
    private final CommunicationAdapterSqlRepository adapterRepository;

    public MetricsAggregatorService(IntegrationFlowSqlRepository flowRepository,
                                  CommunicationAdapterSqlRepository adapterRepository) {
        this.flowRepository = flowRepository;
        this.adapterRepository = adapterRepository;
    }

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
        boolean sourceMatch = false;
        if (flow.getInboundAdapterId() != null) {
            sourceMatch = adapterRepository.findById(flow.getInboundAdapterId())
                .map(adapter -> {
                    if(adapter.getBusinessComponent() != null) {
                        return businessComponentId.equals(adapter.getBusinessComponent().getId());
                    }
                    return false;
                })
                .orElse(false);
        }

        // Check target adapter
        boolean targetMatch = false;
        if (flow.getOutboundAdapterId() != null) {
            targetMatch = adapterRepository.findById(flow.getOutboundAdapterId())
                .map(adapter -> {
                    if(adapter.getBusinessComponent() != null) {
                        return businessComponentId.equals(adapter.getBusinessComponent().getId());
                    }
                    return false;
                })
                .orElse(false);
        }

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
