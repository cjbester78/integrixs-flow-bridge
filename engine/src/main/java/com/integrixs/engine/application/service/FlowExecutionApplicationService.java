package com.integrixs.engine.application.service;

import com.integrixs.data.model.FieldMapping;
import com.integrixs.data.model.FlowExecution;
import com.integrixs.data.model.IntegrationFlow;
import com.integrixs.data.repository.FieldMappingRepository;
import com.integrixs.data.repository.FlowExecutionRepository;
import com.integrixs.data.repository.IntegrationFlowRepository;
import com.integrixs.engine.api.dto.FlowExecutionRequestDTO;
import com.integrixs.engine.api.dto.FlowExecutionResponseDTO;
import com.integrixs.engine.domain.model.FlowExecutionContext;
import com.integrixs.engine.domain.model.FlowExecutionResult;
import com.integrixs.engine.domain.service.FlowExecutionService;
import com.integrixs.backend.service.AlertingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Application service for orchestrating flow executions
 */
@Slf4j
@Service("engineFlowExecutionApplicationService")
@RequiredArgsConstructor
public class FlowExecutionApplicationService {

    private final FlowExecutionService flowExecutionService;
    private final IntegrationFlowRepository integrationFlowRepository;
    private final FieldMappingRepository fieldMappingRepository;
    private final FlowExecutionRepository flowExecutionRepository;

    @Autowired(required = false)
    private AlertingService alertingService;

    /**
     * Execute a flow
     * @param request Execution request
     * @return Execution response
     */
    @Transactional
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

            flowExecution.setIntegrationFlow(flow);
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
            flowExecution.setSuccessCount(1);
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
                flowExecution.setErrorCount(1);
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
}
