package com.integrixs.engine.application.service;

import com.integrixs.data.model.IntegrationFlow;
import com.integrixs.data.sql.repository.IntegrationFlowSqlRepository;
import com.integrixs.engine.api.dto.WorkflowExecutionRequestDTO;
import com.integrixs.engine.api.dto.WorkflowExecutionResponseDTO;
import com.integrixs.engine.domain.model.FlowExecutionContext;
import com.integrixs.engine.domain.model.WorkflowContext;
import com.integrixs.engine.domain.service.FlowExecutionService;
import com.integrixs.engine.domain.service.WorkflowOrchestrationService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application - level orchestrator that coordinates complex workflows
 * Bridges between API layer and domain services
 */
@Service
public class ApplicationOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(ApplicationOrchestrator.class);


    private final WorkflowOrchestrationService workflowOrchestrationService;
    private final FlowExecutionService flowExecutionService;
    private final IntegrationFlowSqlRepository integrationFlowRepository;
    private final AdapterExecutionApplicationService adapterExecutionApplicationService;
    private final FlowExecutionApplicationService flowExecutionApplicationService;

    // Track active workflows
    private final Map<String, WorkflowContext> activeWorkflows = new ConcurrentHashMap<>();

    public ApplicationOrchestrator(WorkflowOrchestrationService workflowOrchestrationService, FlowExecutionService flowExecutionService, IntegrationFlowSqlRepository integrationFlowRepository, AdapterExecutionApplicationService adapterExecutionApplicationService, FlowExecutionApplicationService flowExecutionApplicationService) {
        this.workflowOrchestrationService = workflowOrchestrationService;
        this.flowExecutionService = flowExecutionService;
        this.integrationFlowRepository = integrationFlowRepository;
        this.adapterExecutionApplicationService = adapterExecutionApplicationService;
        this.flowExecutionApplicationService = flowExecutionApplicationService;
    }

    /**
     * Execute a complete workflow
     * @param request Workflow execution request
     * @return Workflow execution response
     */
    public WorkflowExecutionResponseDTO executeWorkflow(WorkflowExecutionRequestDTO request) {
        log.info("Executing workflow for flow: {}", request.getFlowId());

        try {
            // Load flow
            IntegrationFlow flow = integrationFlowRepository.findById(UUID.fromString(request.getFlowId()))
                    .orElseThrow(() -> new IllegalArgumentException("Flow not found: " + request.getFlowId()));

            // Validate workflow
            workflowOrchestrationService.validateWorkflow(flow);

            // Execute workflow
            WorkflowContext context = workflowOrchestrationService.executeWorkflow(flow, request.getInputData());

            // Track active workflow
            activeWorkflows.put(context.getWorkflowId(), context);

            // Convert to response DTO
            return convertToResponseDTO(context);

        } catch(Exception e) {
            log.error("Error executing workflow for flow {}: {}", request.getFlowId(), e.getMessage(), e);
            return createErrorResponse(request, e);
        } finally {
            // Clean up completed workflows
            cleanupCompletedWorkflows();
        }
    }

    /**
     * Execute a workflow asynchronously
     * @param request Workflow execution request
     * @return Future with workflow response
     */
    public CompletableFuture<WorkflowExecutionResponseDTO> executeWorkflowAsync(WorkflowExecutionRequestDTO request) {
        return CompletableFuture.supplyAsync(() -> executeWorkflow(request));
    }

    /**
     * Execute a simple flow(adapter to adapter)
     * @param flowId Flow ID
     * @param inputData Input data
     * @return Workflow response
     */
    public WorkflowExecutionResponseDTO executeSimpleFlow(String flowId, Object inputData) {
        log.info("Executing simple flow: {}", flowId);

        try {
            // Use flow execution service directly for simple flows
            var flowResponse = flowExecutionApplicationService.processMessage(flowId, inputData);

            // Convert to workflow response
            return WorkflowExecutionResponseDTO.builder()
                    .workflowId(flowResponse.getExecutionId())
                    .flowId(flowId)
                    .success(flowResponse.isSuccess())
                    .outputData(flowResponse.getProcessedData())
                    .errorMessage(flowResponse.getErrorMessage())
                    .executionTimeMs(flowResponse.getExecutionTimeMs())
                    .state(flowResponse.isSuccess() ?
                        WorkflowContext.WorkflowState.COMPLETED.name() :
                        WorkflowContext.WorkflowState.FAILED.name())
                    .build();

        } catch(Exception e) {
            log.error("Error executing simple flow {}: {}", flowId, e.getMessage(), e);
            var request = new WorkflowExecutionRequestDTO();
            request.setFlowId(flowId);
            return createErrorResponse(request, e);
        }
    }

    /**
     * Get workflow status
     * @param workflowId Workflow ID
     * @return Workflow status
     */
    public WorkflowExecutionResponseDTO getWorkflowStatus(String workflowId) {
        log.debug("Getting status for workflow: {}", workflowId);

        WorkflowContext context = activeWorkflows.get(workflowId);
        if(context == null) {
            context = workflowOrchestrationService.getWorkflowStatus(workflowId);
        }

        if(context == null) {
            throw new IllegalArgumentException("Workflow not found: " + workflowId);
        }

        return convertToResponseDTO(context);
    }

    /**
     * Cancel a workflow
     * @param workflowId Workflow ID
     * @return true if cancelled
     */
    public boolean cancelWorkflow(String workflowId) {
        log.info("Cancelling workflow: {}", workflowId);

        boolean cancelled = workflowOrchestrationService.cancelWorkflow(workflowId);
        if(cancelled) {
            activeWorkflows.remove(workflowId);
        }

        return cancelled;
    }

    /**
     * Suspend a workflow
     * @param workflowId Workflow ID
     * @return true if suspended
     */
    public boolean suspendWorkflow(String workflowId) {
        log.info("Suspending workflow: {}", workflowId);
        return workflowOrchestrationService.suspendWorkflow(workflowId);
    }

    /**
     * Resume a workflow
     * @param workflowId Workflow ID
     * @return true if resumed
     */
    public boolean resumeWorkflow(String workflowId) {
        log.info("Resuming workflow: {}", workflowId);
        return workflowOrchestrationService.resumeWorkflow(workflowId);
    }

    /**
     * Check adapter health before workflow execution
     * @param flowId Flow ID
     * @return Health check result
     */
    public Map<String, Object> checkWorkflowHealth(String flowId) {
        try {
            IntegrationFlow flow = integrationFlowRepository.findById(UUID.fromString(flowId))
                    .orElseThrow(() -> new IllegalArgumentException("Flow not found: " + flowId));

            boolean sourceHealthy = adapterExecutionApplicationService.isAdapterHealthy(
                    flow.getInboundAdapterId().toString());
            boolean targetHealthy = adapterExecutionApplicationService.isAdapterHealthy(
                    flow.getOutboundAdapterId().toString());

            return Map.of(
                "flowId", flowId,
                "inboundAdapterHealthy", sourceHealthy,
                "outboundAdapterHealthy", targetHealthy,
                "overallHealth", sourceHealthy && targetHealthy,
                "timestamp", System.currentTimeMillis()
           );

        } catch(Exception e) {
            return Map.of(
                "flowId", flowId,
                "error", e.getMessage(),
                "overallHealth", false,
                "timestamp", System.currentTimeMillis()
           );
        }
    }

    private WorkflowExecutionResponseDTO convertToResponseDTO(WorkflowContext context) {
        return WorkflowExecutionResponseDTO.builder()
                .workflowId(context.getWorkflowId())
                .flowId(context.getFlowId())
                .executionId(context.getExecutionId())
                .state(context.getState().name())
                .success(context.getState() == WorkflowContext.WorkflowState.COMPLETED)
                .steps(context.getSteps())
                .currentStep(context.getCurrentStep())
                .globalVariables(context.getGlobalVariables())
                .metadata(context.getMetadata())
                .correlationId(context.getCorrelationId())
                .startTime(context.getStartTime())
                .endTime(context.getEndTime())
                .executionTimeMs(context.getDuration())
                .initiatedBy(context.getInitiatedBy())
                .build();
    }

    private WorkflowExecutionResponseDTO createErrorResponse(WorkflowExecutionRequestDTO request, Exception e) {
        return WorkflowExecutionResponseDTO.builder()
                .workflowId(UUID.randomUUID().toString())
                .flowId(request.getFlowId())
                .success(false)
                .state(WorkflowContext.WorkflowState.FAILED.name())
                .errorMessage(e.getMessage())
                .build();
    }

    private void cleanupCompletedWorkflows() {
        activeWorkflows.entrySet().removeIf(entry -> {
            WorkflowContext context = entry.getValue();
            return context.getState() == WorkflowContext.WorkflowState.COMPLETED ||
                   context.getState() == WorkflowContext.WorkflowState.FAILED ||
                   context.getState() == WorkflowContext.WorkflowState.CANCELLED;
        });
    }

    // Builder
    public static ApplicationOrchestratorBuilder builder() {
        return new ApplicationOrchestratorBuilder();
    }

    public static class ApplicationOrchestratorBuilder {
        private WorkflowOrchestrationService workflowOrchestrationService;
        private FlowExecutionService flowExecutionService;
        private IntegrationFlowSqlRepository integrationFlowRepository;
        private AdapterExecutionApplicationService adapterExecutionApplicationService;
        private FlowExecutionApplicationService flowExecutionApplicationService;

        public ApplicationOrchestratorBuilder workflowOrchestrationService(WorkflowOrchestrationService workflowOrchestrationService) {
            this.workflowOrchestrationService = workflowOrchestrationService;
            return this;
        }

        public ApplicationOrchestratorBuilder flowExecutionService(FlowExecutionService flowExecutionService) {
            this.flowExecutionService = flowExecutionService;
            return this;
        }

        public ApplicationOrchestratorBuilder integrationFlowRepository(IntegrationFlowSqlRepository integrationFlowRepository) {
            this.integrationFlowRepository = integrationFlowRepository;
            return this;
        }

        public ApplicationOrchestratorBuilder adapterExecutionApplicationService(AdapterExecutionApplicationService adapterExecutionApplicationService) {
            this.adapterExecutionApplicationService = adapterExecutionApplicationService;
            return this;
        }

        public ApplicationOrchestratorBuilder flowExecutionApplicationService(FlowExecutionApplicationService flowExecutionApplicationService) {
            this.flowExecutionApplicationService = flowExecutionApplicationService;
            return this;
        }

        public ApplicationOrchestrator build() {
            return new ApplicationOrchestrator(
                this.workflowOrchestrationService,
                this.flowExecutionService,
                this.integrationFlowRepository,
                this.adapterExecutionApplicationService,
                this.flowExecutionApplicationService
            );
        }
    }
}
