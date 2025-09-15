package com.integrixs.backend.camunda;

import com.integrixs.backend.service.AdapterExecutionService;
import com.integrixs.backend.service.TransformationExecutionService;
import com.integrixs.backend.service.OrchestrationTargetService;
import com.integrixs.data.model.OrchestrationTarget;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Camunda delegate for executing Integrix service tasks
 * This delegate handles transformation, adapter calls, and orchestration routing
 */
@Component("integrixServiceTask")
public class IntegrixServiceTaskDelegate implements JavaDelegate {

    private static final Logger logger = LoggerFactory.getLogger(IntegrixServiceTaskDelegate.class);

    @Autowired
    private TransformationExecutionService transformationService;

    @Autowired
    private AdapterExecutionService adapterExecutionService;

    @Autowired
    private OrchestrationTargetService orchestrationTargetService;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String activityId = execution.getCurrentActivityId();
        String activityName = execution.getCurrentActivityName();
        logger.info("Executing Integrix service task: {} ( {})", activityName, activityId);

        try {
            // Get task type from activity ID or process variables
            String taskType = (String) execution.getVariable("taskType");
            if(taskType == null) {
                taskType = inferTaskType(activityId, activityName);
            }

            // Execute based on task type
            switch(taskType) {
                case "transformation":
                    executeTransformation(execution);
                    break;

                case "adapter":
                    executeAdapterCall(execution);
                    break;

                case "routing":
                    executeRouting(execution);
                    break;

                case "orchestration":
                    executeOrchestration(execution);
                    break;

                default:
                    logger.warn("Unknown task type: {}, executing as generic service task", taskType);
                    executeGenericTask(execution);
            }

            // Set success flag
            execution.setVariable("taskResult_" + activityId, "SUCCESS");

        } catch(Exception e) {
            logger.error("Failed to execute service task: " + activityName, e);

            // Set failure information
            execution.setVariable("taskResult_" + activityId, "FAILED");
            execution.setVariable("taskError_" + activityId, e.getMessage());

            // Re - throw to trigger error handling
            throw e;
        }
    }

    /**
     * Execute transformation task
     */
    private void executeTransformation(DelegateExecution execution) throws Exception {
        logger.info("Executing transformation task");

        // Get transformation configuration
        String transformationId = (String) execution.getVariable("transformationId");
        Object inputData = execution.getVariable("currentData");

        if(transformationId == null) {
            transformationId = execution.getCurrentActivityId();
        }

        // Execute transformation
        var result = transformationService.executeTransformation(
            transformationId,
            inputData != null ? inputData : execution.getVariables()
       );

        if(result.isSuccess()) {
            execution.setVariable("currentData", result.getData());
            execution.setVariable("transformationResult", result.getData());
            logger.info("Transformation completed successfully");
        } else {
            throw new RuntimeException("Transformation failed: " + result.getMessage());
        }
    }

    /**
     * Execute adapter call
     */
    private void executeAdapterCall(DelegateExecution execution) throws Exception {
        logger.info("Executing adapter call");

        // Get adapter configuration
        String adapterId = (String) execution.getVariable("adapterId");
        Object messageData = execution.getVariable("currentData");
        Map<String, Object> context = execution.getVariables();

        if(adapterId == null) {
            throw new RuntimeException("No adapter ID specified for adapter call");
        }

        // Execute adapter
        var result = adapterExecutionService.executeAdapter(
            UUID.fromString(adapterId),
            messageData != null ? messageData : context,
            context
       );

        if(result.isSuccess()) {
            execution.setVariable("currentData", result.getData());
            execution.setVariable("adapterResult", result.getData());
            logger.info("Adapter call completed successfully");
        } else {
            throw new RuntimeException("Adapter execution failed: " + result.getError());
        }
    }

    /**
     * Execute routing decision
     */
    private void executeRouting(DelegateExecution execution) throws Exception {
        logger.info("Executing routing task");

        // Get routing configuration
        String routingRules = (String) execution.getVariable("routingRules");
        Object messageData = execution.getVariable("currentData");

        // Evaluate routing conditions
        String selectedRoute = evaluateRoutingRules(routingRules, messageData, execution.getVariables());

        // Set the selected route
        execution.setVariable("selectedRoute", selectedRoute);
        logger.info("Routing decision made: {}", selectedRoute);
    }

    /**
     * Execute orchestration with multiple targets
     */
    private void executeOrchestration(DelegateExecution execution) throws Exception {
        logger.info("Executing orchestration task");

        // Get flow ID
        String flowId = (String) execution.getVariable("flowId");
        if(flowId == null) {
            throw new RuntimeException("No flow ID specified for orchestration");
        }

        // Get orchestration targets
        List<OrchestrationTarget> targets = orchestrationTargetService.getTargetsByFlowId(UUID.fromString(flowId));

        // Execute each target based on configuration
        for(OrchestrationTarget target : targets) {
            if(!target.isActive()) {
                logger.info("Skipping inactive target: {}", target.getId());
                continue;
            }

            // Check routing condition
            if(!evaluateTargetCondition(target, execution)) {
                logger.info("Target {} condition not met, skipping", target.getId());
                continue;
            }

            // Execute target
            try {
                executeOrchestrationTarget(target, execution);
            } catch(Exception e) {
                handleTargetError(target, e, execution);
            }
        }
    }

    /**
     * Execute a single orchestration target
     */
    private void executeOrchestrationTarget(OrchestrationTarget target, DelegateExecution execution) throws Exception {
        logger.info("Executing orchestration target: {}", target.getId());

        // Get message data
        Object messageData = execution.getVariable("currentData");
        Map<String, Object> context = execution.getVariables();

        // Execute adapter
        var result = adapterExecutionService.executeAdapter(
            target.getTargetAdapter().getId(),
            messageData != null ? messageData : context,
            context
       );

        if(result.isSuccess()) {
            execution.setVariable("target_" + target.getId() + "_result", result.getData());
            logger.info("Target {} executed successfully", target.getId());
        } else {
            throw new RuntimeException("Target execution failed: " + result.getError());
        }
    }

    /**
     * Handle error for a specific target
     */
    private void handleTargetError(OrchestrationTarget target, Exception error, DelegateExecution execution) throws Exception {
        logger.error("Error executing target: " + target.getId(), error);

        String errorStrategy = target.getErrorStrategy();
        if(errorStrategy == null) {
            errorStrategy = "FAIL_FAST";
        }

        switch(errorStrategy) {
            case "CONTINUE":
                // Log error and continue
                execution.setVariable("target_" + target.getId() + "_error", error.getMessage());
                break;

            case "COMPENSATE":
                // Trigger compensation
                execution.setVariable("compensationRequired", true);
                execution.setVariable("compensationTargetId", target.getId().toString());
                throw error;

            case "RETRY":
                // Will be handled by Camunda retry mechanism
                throw error;

            case "FAIL_FAST":
            default:
                // Fail immediately
                throw error;
        }
    }

    /**
     * Execute generic service task
     */
    private void executeGenericTask(DelegateExecution execution) {
        logger.info("Executing generic service task");

        // Add timestamp
        execution.setVariable("taskExecutionTime_" + execution.getCurrentActivityId(), System.currentTimeMillis());

        // Generic processing
        Object inputData = execution.getVariable("currentData");
        if(inputData != null) {
            // Pass through the data
            execution.setVariable("currentData", inputData);
        }
    }

    /**
     * Infer task type from activity ID or name
     */
    private String inferTaskType(String activityId, String activityName) {
        if(activityId == null && activityName == null) {
            return "generic";
        }

        String combined = (activityId + " " + activityName).toLowerCase();

        if(combined.contains("transform")) {
            return "transformation";
        } else if(combined.contains("adapter") || combined.contains("call")) {
            return "adapter";
        } else if(combined.contains("route") || combined.contains("routing")) {
            return "routing";
        } else if(combined.contains("orchestrat")) {
            return "orchestration";
        } else {
            return "generic";
        }
    }

    /**
     * Evaluate routing rules
     */
    private String evaluateRoutingRules(String rules, Object data, Map<String, Object> variables) {
        // Simple implementation - in production would use the ConditionEvaluationService
        if(rules == null || rules.isEmpty()) {
            return "default";
        }

        // For now, return the first route
        return "default";
    }

    /**
     * Evaluate target condition
     */
    private boolean evaluateTargetCondition(OrchestrationTarget target, DelegateExecution execution) {
        if(target.getRoutingCondition() == null || target.getRoutingCondition().isEmpty()) {
            return true; // No condition means always execute
        }

        // Simple evaluation - in production would use ConditionEvaluationService
        return true;
    }
}
