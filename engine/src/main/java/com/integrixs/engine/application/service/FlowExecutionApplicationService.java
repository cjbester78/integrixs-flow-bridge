package com.integrixs.engine.application.service;

import com.integrixs.data.model.FieldMapping;
import com.integrixs.data.model.FlowExecution;
import com.integrixs.data.model.IntegrationFlow;
import com.integrixs.data.sql.repository.FieldMappingSqlRepository;
import com.integrixs.data.sql.repository.FlowExecutionSqlRepository;
import com.integrixs.data.sql.repository.IntegrationFlowSqlRepository;
import com.integrixs.engine.api.dto.FlowExecutionRequestDTO;
import com.integrixs.engine.api.dto.FlowExecutionResponseDTO;
import com.integrixs.engine.domain.model.FlowExecutionContext;
import com.integrixs.engine.domain.model.FlowExecutionResult;
import com.integrixs.engine.domain.service.FlowExecutionService;
import com.integrixs.engine.service.FlowAlertingPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application service for orchestrating flow executions
 */
@Service("engineFlowExecutionApplicationService")
public class FlowExecutionApplicationService {

    private static final Logger log = LoggerFactory.getLogger(FlowExecutionApplicationService.class);


    private final FlowExecutionService flowExecutionService;
    private final IntegrationFlowSqlRepository integrationFlowRepository;
    private final FieldMappingSqlRepository fieldMappingRepository;
    private final FlowExecutionSqlRepository flowExecutionRepository;

    @Autowired(required = false)
    private FlowAlertingPort alertingService;

    public FlowExecutionApplicationService(FlowExecutionService flowExecutionService, IntegrationFlowSqlRepository integrationFlowRepository, FieldMappingSqlRepository fieldMappingRepository, FlowExecutionSqlRepository flowExecutionRepository) {
        this.flowExecutionService = flowExecutionService;
        this.integrationFlowRepository = integrationFlowRepository;
        this.fieldMappingRepository = fieldMappingRepository;
        this.flowExecutionRepository = flowExecutionRepository;
    }

    /**
     * Execute a flow
     * @param request Execution request
     * @return Execution response
     */
    public FlowExecutionResponseDTO executeFlow(FlowExecutionRequestDTO request) {
        LocalDateTime startTime = LocalDateTime.now();
        log.info("Executing flow: {}", request.getFlowId());

        // Create flow execution record
        FlowExecution flowExecution = new FlowExecution();
        flowExecution.setStartTime(startTime);
        flowExecution.setStatus(FlowExecution.ExecutionStatus.IN_PROGRESS);

        try {
            // Load flow
            IntegrationFlow flow = integrationFlowRepository.findById(UUID.fromString(request.getFlowId()))
                    .orElseThrow(() -> new IllegalArgumentException("Flow not found: " + request.getFlowId()));

            flowExecution.setFlow(flow);
            flowExecution = flowExecutionRepository.save(flowExecution);

            // Validate flow
            flowExecutionService.validateFlow(flow);

            // Build context
            FlowExecutionContext context = buildContext(request, flow);

            // Execute flow
            FlowExecutionResult result = flowExecutionService.executeFlow(flow, request.getMessage(), context);

            // Calculate execution time
            long executionTime = Duration.between(startTime, LocalDateTime.now()).toMillis();
            result.setExecutionTimeMs(executionTime);

            // Update flow execution record
            flowExecution.setEndTime(LocalDateTime.now());
            flowExecution.setStatus(FlowExecution.ExecutionStatus.COMPLETED);
            flowExecution.setMessagesProcessed(1);
            flowExecution = flowExecutionRepository.save(flowExecution);

            // Evaluate alerts for successful execution
            if(alertingService != null) {
                alertingService.evaluateFlowAlerts(flowExecution);
            }

            // Convert to DTO
            return convertToResponseDTO(result);

        } catch(Exception e) {
            log.error("Error executing flow {}: {}", request.getFlowId(), e.getMessage(), e);

            long executionTime = Duration.between(startTime, LocalDateTime.now()).toMillis();

            // Update flow execution record for failure
            if(flowExecution != null && flowExecution.getId() != null) {
                flowExecution.setEndTime(LocalDateTime.now());
                flowExecution.setStatus(FlowExecution.ExecutionStatus.FAILED);
                flowExecution.setErrorMessage(e.getMessage());
                flowExecution.setMessagesFailed(1);
                flowExecution = flowExecutionRepository.save(flowExecution);

                // Evaluate alerts for failed execution
                if(alertingService != null) {
                    alertingService.evaluateFlowAlerts(flowExecution);
                }
            }

            return FlowExecutionResponseDTO.builder()
                    .executionId(request.getExecutionId())
                    .flowId(request.getFlowId())
                    .success(false)
                    .errorMessage(e.getMessage())
                    .errorCode("FLOW_EXECUTION_ERROR")
                    .executionTimeMs(executionTime)
                    .build();
        }
    }

    /**
     * Execute a flow asynchronously
     * @param request Execution request
     * @return Future with execution response
     */
    public CompletableFuture<FlowExecutionResponseDTO> executeFlowAsync(FlowExecutionRequestDTO request) {
        return CompletableFuture.supplyAsync(() -> executeFlow(request));
    }

    /**
     * Process a message through a flow
     * @param flowId Flow ID
     * @param message Input message
     * @return Execution response
     */
    public FlowExecutionResponseDTO processMessage(String flowId, Object message) {
        FlowExecutionRequestDTO request = new FlowExecutionRequestDTO();
        request.setFlowId(flowId);
        request.setMessage(message);
        request.setExecutionId(UUID.randomUUID().toString());

        return executeFlow(request);
    }

    /**
     * Check if a flow is ready for execution
     * @param flowId Flow ID
     * @return true if ready
     */
    public boolean isFlowReady(String flowId) {
        return flowExecutionService.isFlowReady(flowId);
    }

    /**
     * Get field mappings for a flow
     * @param flowId Flow ID
     * @return List of field mappings
     */
    public List<FieldMapping> getFlowFieldMappings(String flowId) {
        return fieldMappingRepository.findByTransformationFlowIdAndIsActiveTrueOrderByTransformationExecutionOrder(
                UUID.fromString(flowId));
    }

    private FlowExecutionContext buildContext(FlowExecutionRequestDTO request, IntegrationFlow flow) {
        return FlowExecutionContext.builder()
                .executionId(request.getExecutionId() != null ? request.getExecutionId() : UUID.randomUUID().toString())
                .flowId(flow.getId().toString())
                .inboundAdapterId(flow.getInboundAdapterId())
                .outboundAdapterId(flow.getOutboundAdapterId())
                .mappingMode(flow.getMappingMode() != null ? flow.getMappingMode().name() : null)
                .parameters(request.getParameters())
                .headers(request.getHeaders())
                .metadata(request.getMetadata())
                .correlationId(request.getCorrelationId())
                .async(request.isAsync())
                .timeout(request.getTimeout())
                .build();
    }

    private FlowExecutionResponseDTO convertToResponseDTO(FlowExecutionResult result) {
        return FlowExecutionResponseDTO.builder()
                .executionId(result.getExecutionId())
                .flowId(result.getFlowId())
                .success(result.isSuccess())
                .processedData(result.getProcessedData())
                .errorMessage(result.getErrorMessage())
                .errorCode(result.getErrorCode())
                .timestamp(result.getTimestamp())
                .executionTimeMs(result.getExecutionTimeMs())
                .metadata(result.getMetadata())
                .warnings(result.getWarnings())
                .inboundAdapterId(result.getInboundAdapterId())
                .outboundAdapterId(result.getOutboundAdapterId())
                .recordsProcessed(result.getRecordsProcessed())
                .build();
    }

    // Builder
    public static FlowExecutionApplicationServiceBuilder builder() {
        return new FlowExecutionApplicationServiceBuilder();
    }

    public static class FlowExecutionApplicationServiceBuilder {
        private FlowExecutionService flowExecutionService;
        private IntegrationFlowSqlRepository integrationFlowRepository;
        private FieldMappingSqlRepository fieldMappingRepository;
        private FlowExecutionSqlRepository flowExecutionRepository;
        private FlowAlertingPort alertingService;

        public FlowExecutionApplicationServiceBuilder flowExecutionService(FlowExecutionService flowExecutionService) {
            this.flowExecutionService = flowExecutionService;
            return this;
        }

        public FlowExecutionApplicationServiceBuilder integrationFlowRepository(IntegrationFlowSqlRepository integrationFlowRepository) {
            this.integrationFlowRepository = integrationFlowRepository;
            return this;
        }

        public FlowExecutionApplicationServiceBuilder fieldMappingRepository(FieldMappingSqlRepository fieldMappingRepository) {
            this.fieldMappingRepository = fieldMappingRepository;
            return this;
        }

        public FlowExecutionApplicationServiceBuilder flowExecutionRepository(FlowExecutionSqlRepository flowExecutionRepository) {
            this.flowExecutionRepository = flowExecutionRepository;
            return this;
        }

        public FlowExecutionApplicationServiceBuilder alertingService(FlowAlertingPort alertingService) {
            this.alertingService = alertingService;
            return this;
        }

        public FlowExecutionApplicationService build() {
            FlowExecutionApplicationService instance = new FlowExecutionApplicationService(
                this.flowExecutionService,
                this.integrationFlowRepository,
                this.fieldMappingRepository,
                this.flowExecutionRepository
            );
            if (this.alertingService != null) {
                instance.alertingService = this.alertingService;
            }
            return instance;
        }
    }
}
