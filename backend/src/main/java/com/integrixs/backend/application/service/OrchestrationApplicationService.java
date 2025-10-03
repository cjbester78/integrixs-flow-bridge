package com.integrixs.backend.application.service;

import com.integrixs.backend.api.dto.OrchestrationDTO;
import com.integrixs.backend.api.dto.OrchestrationExecutionDTO;
import com.integrixs.backend.api.dto.ValidationResultDTO;
import com.integrixs.backend.domain.model.OrchestrationExecution;
import com.integrixs.backend.domain.service.OrchestrationManagementService;
import com.integrixs.backend.infrastructure.orchestration.OrchestrationExecutor;
import com.integrixs.backend.service.TransformationExecutionService;
import com.integrixs.backend.statemachine.FlowExecutionStates;
import com.integrixs.backend.statemachine.FlowExecutionEvents;
import com.integrixs.data.model.IntegrationFlow;
import com.integrixs.data.sql.repository.IntegrationFlowSqlRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application service for orchestrating integration flows
 */
@Service
public class OrchestrationApplicationService {

    private static final Logger log = LoggerFactory.getLogger(OrchestrationApplicationService.class);


    private final OrchestrationManagementService orchestrationManagementService;
    private final OrchestrationExecutor orchestrationExecutor;
    private final IntegrationFlowSqlRepository integrationFlowRepository;
    private final TransformationExecutionService transformationService;
    private final StateMachineFactory<FlowExecutionStates, FlowExecutionEvents> stateMachineFactory;

    private final Map<String, OrchestrationExecution> activeExecutions = new ConcurrentHashMap<>();
    private final Map<String, StateMachine<FlowExecutionStates, FlowExecutionEvents>> activeStateMachines = new ConcurrentHashMap<>();

    public OrchestrationApplicationService(OrchestrationManagementService orchestrationManagementService,
                                         OrchestrationExecutor orchestrationExecutor,
                                         IntegrationFlowSqlRepository integrationFlowRepository,
                                         TransformationExecutionService transformationService,
                                         StateMachineFactory<FlowExecutionStates, FlowExecutionEvents> stateMachineFactory) {
        this.orchestrationManagementService = orchestrationManagementService;
        this.orchestrationExecutor = orchestrationExecutor;
        this.integrationFlowRepository = integrationFlowRepository;
        this.transformationService = transformationService;
        this.stateMachineFactory = stateMachineFactory;
    }

    /**
     * Execute an orchestration flow synchronously
     */
    public OrchestrationDTO executeOrchestrationFlow(String flowId, Object inputData) {
        try {
            // Validate input
            orchestrationManagementService.validateInputData(inputData);

            // Get and validate flow
            IntegrationFlow flow = integrationFlowRepository.findById(UUID.fromString(flowId))
                    .orElseThrow(() -> new IllegalArgumentException("Flow not found: " + flowId));

            orchestrationManagementService.validateOrchestrationFlow(flow);

            // Create execution
            OrchestrationExecution execution = createExecution(flow, inputData);
            activeExecutions.put(execution.getExecutionId(), execution);

            // Execute workflow using state machine
            return executeWorkflowWithStateMachine(execution, flow);

        } catch(Exception e) {
            log.error("Orchestration execution failed for flow {}", flowId, e);
            throw new RuntimeException("Orchestration execution failed: " + e.getMessage(), e);
        }
    }

    /**
     * Execute an orchestration flow asynchronously
     */
    @Async
    public CompletableFuture<OrchestrationDTO> executeOrchestrationFlowAsync(String flowId, Object inputData) {
        return CompletableFuture.supplyAsync(() -> executeOrchestrationFlow(flowId, inputData));
    }

    /**
     * Get current execution status
     */
    public Optional<OrchestrationExecutionDTO> getExecutionStatus(String executionId) {
        return Optional.ofNullable(activeExecutions.get(executionId))
                .map(this::convertToExecutionDTO);
    }

    /**
     * Cancel an active orchestration execution
     */
    public boolean cancelExecution(String executionId) {
        OrchestrationExecution execution = activeExecutions.get(executionId);
        if(execution != null && orchestrationManagementService.canCancelExecution(execution.getStatus())) {
            execution.updateStatus("CANCELLED");
            execution.addLog("Execution cancelled by user");
            return true;
        }
        return false;
    }

    /**
     * Validate orchestration flow configuration
     */
    public ValidationResultDTO validateOrchestrationFlow(String flowId) {
        ValidationResultDTO result = new ValidationResultDTO();

        try {
            IntegrationFlow flow = integrationFlowRepository.findById(UUID.fromString(flowId))
                    .orElseThrow(() -> new IllegalArgumentException("Flow not found: " + flowId));

            orchestrationManagementService.validateOrchestrationFlow(flow);
            result.setValid(true);
            result.addInfo("Flow validation successful");

        } catch(IllegalStateException e) {
            result.setValid(false);
            result.addError(e.getMessage());
        } catch(Exception e) {
            result.setValid(false);
            result.addError("Validation failed: " + e.getMessage());
        }

        return result;
    }

    /**
     * Get orchestration execution history
     */
    public List<OrchestrationExecutionDTO> getExecutionHistory(String flowId, int limit) {
        return activeExecutions.values().stream()
                .filter(execution -> execution.getFlowId().equals(flowId))
                .sorted((a, b) -> {
                    LocalDateTime aTime = a.getStartTime() != null ? a.getStartTime() : LocalDateTime.MIN;
                    LocalDateTime bTime = b.getStartTime() != null ? b.getStartTime() : LocalDateTime.MIN;
                    return bTime.compareTo(aTime);
                })
                .limit(limit)
                .map(this::convertToExecutionDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all active executions
     */
    public List<OrchestrationExecutionDTO> getActiveExecutions() {
        return activeExecutions.values().stream()
                .filter(OrchestrationExecution::isInProgress)
                .map(this::convertToExecutionDTO)
                .collect(Collectors.toList());
    }

    private OrchestrationExecution createExecution(IntegrationFlow flow, Object inputData) {
        OrchestrationExecution execution = new OrchestrationExecution();
        execution.setExecutionId(orchestrationManagementService.generateExecutionId());
        execution.setFlowId(flow.getId().toString());
        execution.setFlowName(flow.getName());
        execution.updateStatus("PENDING");
        execution.setInputData(inputData);
        execution.addLog("Orchestration execution created");

        return execution;
    }

    /**
     * Execute workflow using Spring State Machine to replace Camunda task execution
     */
    private OrchestrationDTO executeWorkflowWithStateMachine(OrchestrationExecution execution, IntegrationFlow flow) {
        try {
            // Create and configure state machine for this execution
            StateMachine<FlowExecutionStates, FlowExecutionEvents> stateMachine = stateMachineFactory.getStateMachine();
            activeStateMachines.put(execution.getExecutionId(), stateMachine);
            
            // Configure state machine with execution context
            stateMachine.getExtendedState().getVariables().put("execution", execution);
            stateMachine.getExtendedState().getVariables().put("flow", flow);
            stateMachine.getExtendedState().getVariables().put("orchestrationExecutor", orchestrationExecutor);
            stateMachine.getExtendedState().getVariables().put("transformationService", transformationService);
            
            // Start state machine
            stateMachine.start();
            
            // Trigger flow start event
            boolean eventSent = stateMachine.sendEvent(FlowExecutionEvents.START_FLOW);
            if (!eventSent) {
                return createErrorResult(execution, "Failed to start flow execution state machine");
            }
            
            // Wait for completion or monitor async execution
            FlowExecutionStates finalState = stateMachine.getState().getId();
            
            // Clean up state machine
            activeStateMachines.remove(execution.getExecutionId());
            stateMachine.stop();
            
            if (finalState == FlowExecutionStates.COMPLETED) {
                return createSuccessResult(execution);
            } else if (finalState == FlowExecutionStates.FAILED) {
                return createErrorResult(execution, "Flow execution failed in state machine");
            } else {
                return createErrorResult(execution, "Flow execution ended in unexpected state: " + finalState);
            }
            
        } catch(Exception e) {
            log.error("State machine workflow execution failed", e);
            execution.updateStatus("FAILED");
            execution.addLog("State machine execution failed: " + e.getMessage());
            return createErrorResult(execution, "State machine workflow execution failed: " + e.getMessage());
        }
    }

    private OrchestrationDTO executeWorkflow(OrchestrationExecution execution, IntegrationFlow flow) {
        try {
            execution.updateStatus("RUNNING");
            execution.addLog("Beginning workflow execution");

            // Step 1: Initialize process
            execution.setCurrentStep("INITIALIZE");
            execution.addLog(orchestrationManagementService.formatLogMessage("Process initialized"));

            // Step 2: Initialize adapters
            execution.setCurrentStep("INITIALIZE_ADAPTERS");
            if(!orchestrationExecutor.initializeAdapters(execution, flow)) {
                return createErrorResult(execution, "Failed to initialize adapters");
            }

            // Step 3: Fetch data from source adapter(if needed)
            if(execution.getInputData() == null) {
                execution.setCurrentStep("FETCH_SOURCE_DATA");
                String inboundAdapterId = flow.getInboundAdapterId().toString();
                if(!orchestrationExecutor.fetchFromSourceAdapter(execution, inboundAdapterId)) {
                    return createErrorResult(execution, "Failed to fetch data from source adapter");
                }
            }

            // Step 4: Execute transformations
            execution.setCurrentStep("EXECUTE_TRANSFORMATIONS");
            if(!orchestrationExecutor.executeTransformations(execution, flow.getId().toString())) {
                return createErrorResult(execution, "Failed to execute transformations");
            }

            // Step 5: Send to target adapter
            execution.setCurrentStep("PROCESS_TARGETS");
            String outboundAdapterId = flow.getOutboundAdapterId().toString();
            if(!orchestrationExecutor.sendToTargetAdapter(execution, outboundAdapterId)) {
                return createErrorResult(execution, "Failed to send data to target adapter");
            }

            // Step 6: Complete process
            execution.setCurrentStep("COMPLETE");
            execution.updateStatus("COMPLETED");
            execution.addLog("Orchestration execution completed successfully");

            return createSuccessResult(execution);

        } catch(Exception e) {
            log.error("Workflow execution failed", e);
            execution.updateStatus("FAILED");
            execution.addLog("Execution failed: " + e.getMessage());
            return createErrorResult(execution, "Workflow execution failed: " + e.getMessage());
        }
    }

    private OrchestrationDTO createSuccessResult(OrchestrationExecution execution) {
        OrchestrationDTO dto = new OrchestrationDTO();
        dto.setSuccess(true);
        dto.setExecutionId(execution.getExecutionId());
        dto.setData(execution.getOutputData());
        dto.setLogs(execution.getLogs());
        dto.setDuration(orchestrationManagementService.calculateExecutionDuration(
                execution.getStartTime(), execution.getEndTime()));
        return dto;
    }

    private OrchestrationDTO createErrorResult(OrchestrationExecution execution, String message) {
        execution.updateStatus("FAILED");
        execution.addLog(message);

        OrchestrationDTO dto = new OrchestrationDTO();
        dto.setSuccess(false);
        dto.setExecutionId(execution.getExecutionId());
        dto.setMessage(message);
        dto.setLogs(execution.getLogs());
        dto.setDuration(orchestrationManagementService.calculateExecutionDuration(
                execution.getStartTime(), execution.getEndTime()));
        return dto;
    }

    private OrchestrationExecutionDTO convertToExecutionDTO(OrchestrationExecution execution) {
        OrchestrationExecutionDTO dto = new OrchestrationExecutionDTO();
        dto.setExecutionId(execution.getExecutionId());
        dto.setFlowId(execution.getFlowId());
        dto.setFlowName(execution.getFlowName());
        dto.setStatus(execution.getStatus());
        dto.setCurrentStep(execution.getCurrentStep());
        dto.setCurrentStepDisplay(orchestrationManagementService.getStepDisplayName(execution.getCurrentStep()));
        dto.setStartTime(execution.getStartTime());
        dto.setEndTime(execution.getEndTime());
        dto.setDuration(orchestrationManagementService.calculateExecutionDuration(
                execution.getStartTime(), execution.getEndTime()));
        dto.setInputData(execution.getInputData());
        dto.setOutputData(execution.getOutputData());
        dto.setLogs(execution.getLogs());
        dto.setMetadata(execution.getMetadata());
        dto.setInProgress(execution.isInProgress());
        dto.setComplete(execution.isComplete());
        return dto;
    }
}
