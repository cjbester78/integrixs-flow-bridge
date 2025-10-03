package com.integrixs.backend.camunda;

import com.integrixs.backend.service.BackendAdapterExecutor;
import com.integrixs.backend.service.TransformationExecutionService;
import com.integrixs.backend.application.service.OrchestrationTargetService;
import com.integrixs.data.model.CommunicationAdapter;
import com.integrixs.data.model.OrchestrationTarget;
import com.integrixs.data.sql.repository.CommunicationAdapterSqlRepository;
import com.integrixs.data.sql.repository.OrchestrationTargetSqlRepository;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * External task client service for handling Camunda external tasks
 * This provides better scalability and reliability than polling directly
 */
@Service
@ConditionalOnProperty(name = "camunda.bpm.enabled", havingValue = "true", matchIfMissing = false)
public class ExternalTaskClientService implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(ExternalTaskClientService.class);

    @Value("${camunda.bpm.client.base-url:http://localhost:8080/engine-rest}")
    private String baseUrl;

    @Value("${camunda.bpm.client.worker-id:integrix-worker}")
    private String workerId;

    @Value("${camunda.bpm.client.max-tasks:10}")
    private int maxTasks;

    @Value("${camunda.bpm.client.lock-duration:60000}")
    private long lockDuration;

    @Value("${camunda.bpm.client.async-response-timeout:30000}")
    private long asyncResponseTimeout;

    @Autowired
    private TransformationExecutionService transformationService;

    @Autowired
    private BackendAdapterExecutor adapterExecutionService;

    @Autowired
    private OrchestrationTargetService orchestrationTargetService;

    @Autowired
    private CommunicationAdapterSqlRepository adapterRepository;

    @Autowired
    private OrchestrationTargetSqlRepository orchestrationTargetRepository;

    private final Map<String, ExternalTaskClient> clients = new ConcurrentHashMap<>();

    @Override
    public void run(String... args) {
        logger.info("Starting Camunda external task clients");

        // Create transformation task client
        createTransformationTaskClient();

        // Create adapter task client
        createAdapterTaskClient();

        // Create orchestration task client
        createOrchestrationTaskClient();

        // Create routing task client
        createRoutingTaskClient();

        // Create generic service task client
        createGenericServiceTaskClient();
    }

    /**
     * Create client for transformation tasks
     */
    private void createTransformationTaskClient() {
        ExternalTaskClient client = ExternalTaskClient.create()
            .baseUrl(baseUrl)
            .workerId(workerId + "-transformation")
            .maxTasks(maxTasks)
            .lockDuration(lockDuration)
            .asyncResponseTimeout(asyncResponseTimeout)
            .build();

        client.subscribe("integrix-transformation")
            .handler(new TransformationTaskHandler())
            .open();

        clients.put("transformation", client);
        logger.info("Transformation task client started");
    }

    /**
     * Create client for adapter tasks
     */
    private void createAdapterTaskClient() {
        ExternalTaskClient client = ExternalTaskClient.create()
            .baseUrl(baseUrl)
            .workerId(workerId + "-adapter")
            .maxTasks(maxTasks)
            .lockDuration(lockDuration)
            .asyncResponseTimeout(asyncResponseTimeout)
            .build();

        client.subscribe("integrix-adapter")
            .handler(new AdapterTaskHandler())
            .open();

        clients.put("adapter", client);
        logger.info("Adapter task client started");
    }

    /**
     * Create client for orchestration tasks
     */
    private void createOrchestrationTaskClient() {
        ExternalTaskClient client = ExternalTaskClient.create()
            .baseUrl(baseUrl)
            .workerId(workerId + "-orchestration")
            .maxTasks(maxTasks)
            .lockDuration(lockDuration)
            .asyncResponseTimeout(asyncResponseTimeout)
            .build();

        client.subscribe("integrix-orchestration")
            .handler(new OrchestrationTaskHandler())
            .open();

        clients.put("orchestration", client);
        logger.info("Orchestration task client started");
    }

    /**
     * Create client for routing tasks
     */
    private void createRoutingTaskClient() {
        ExternalTaskClient client = ExternalTaskClient.create()
            .baseUrl(baseUrl)
            .workerId(workerId + "-routing")
            .maxTasks(maxTasks)
            .lockDuration(lockDuration)
            .asyncResponseTimeout(asyncResponseTimeout)
            .build();

        client.subscribe("integrix-routing")
            .handler(new RoutingTaskHandler())
            .open();

        clients.put("routing", client);
        logger.info("Routing task client started");
    }

    /**
     * Create client for generic service tasks
     */
    private void createGenericServiceTaskClient() {
        ExternalTaskClient client = ExternalTaskClient.create()
            .baseUrl(baseUrl)
            .workerId(workerId + "-generic")
            .maxTasks(maxTasks)
            .lockDuration(lockDuration)
            .asyncResponseTimeout(asyncResponseTimeout)
            .build();

        // Subscribe to multiple generic topics
        String[] genericTopics = {
            "integrix-service",
            "integrix-enrichment",
            "integrix-validation",
            "integrix-logging"
        };

        for(String topic : genericTopics) {
            client.subscribe(topic)
                .handler(new GenericTaskHandler())
                .open();
        }

        clients.put("generic", client);
        logger.info("Generic service task client started");
    }

    @PreDestroy
    public void shutdown() {
        logger.info("Shutting down external task clients");
        clients.values().forEach(ExternalTaskClient::stop);
    }

    /**
     * Handler for transformation tasks
     */
    private class TransformationTaskHandler implements ExternalTaskHandler {
        @Override
        public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {
            logger.debug("Executing transformation task: {}", externalTask.getActivityId());

            try {
                // Get transformation ID
                String transformationId = externalTask.getVariable("transformationId");
                if(transformationId == null) {
                    transformationId = externalTask.getActivityId();
                }

                // Get input data
                Object inputData = externalTask.getVariable("currentData");
                if(inputData == null) {
                    inputData = externalTask.getAllVariables();
                }

                // Execute transformation
                var result = transformationService.executeTransformation(transformationId, inputData);

                if(result.isSuccess()) {
                    // Complete task with result
                    Map<String, Object> variables = new HashMap<>();
                    variables.put("currentData", result.getData());
                    variables.put("transformationResult", result.getData());
                    variables.put("transformationSuccess", true);

                    externalTaskService.complete(externalTask, variables);
                    logger.debug("Transformation task completed successfully");
                } else {
                    // Handle failure
                    externalTaskService.handleFailure(
                        externalTask,
                        result.getMessage(),
                        result.getStackTrace(),
                        3,
                        Duration.ofSeconds(30).toMillis()
                   );
                }
            } catch(Exception e) {
                logger.error("Error executing transformation task", e);
                externalTaskService.handleFailure(
                    externalTask,
                    e.getMessage(),
                    getStackTrace(e),
                    0,
                    0L
               );
            }
        }
    }

    /**
     * Handler for adapter tasks
     */
    private class AdapterTaskHandler implements ExternalTaskHandler {
        @Override
        public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {
            logger.debug("Executing adapter task: {}", externalTask.getActivityId());

            try {
                // Get adapter ID
                String adapterId = externalTask.getVariable("adapterId");
                if(adapterId == null) {
                    throw new RuntimeException("No adapter ID specified for adapter task");
                }

                // Get message data
                Object messageData = externalTask.getVariable("currentData");
                Map<String, Object> context = externalTask.getAllVariables();

                // Fetch adapter from repository
                CommunicationAdapter adapter = adapterRepository.findById(UUID.fromString(adapterId))
                    .orElseThrow(() -> new RuntimeException("Adapter not found: " + adapterId));

                // Execute adapter
                String result = adapterExecutionService.executeAdapter(
                    adapter,
                    messageData != null ? messageData.toString() : context.toString(),
                    context
               );

                // Adapter execution returns the result directly as string
                Map<String, Object> variables = new HashMap<>();
                variables.put("currentData", result);
                variables.put("adapterResult", result);
                variables.put("adapterSuccess", true);

                externalTaskService.complete(externalTask, variables);
                logger.debug("Adapter task completed successfully");
            } catch(Exception e) {
                logger.error("Error executing adapter task", e);
                externalTaskService.handleFailure(
                    externalTask,
                    e.getMessage(),
                    getStackTrace(e),
                    0,
                    0L
               );
            }
        }
    }

    /**
     * Handler for orchestration tasks
     */
    private class OrchestrationTaskHandler implements ExternalTaskHandler {
        @Override
        public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {
            logger.debug("Executing orchestration task: {}", externalTask.getActivityId());

            try {
                // Get flow ID
                String flowId = externalTask.getVariable("flowId");
                if(flowId == null) {
                    throw new RuntimeException("No flow ID specified for orchestration task");
                }

                // Get orchestration targets
                List<OrchestrationTarget> targets = orchestrationTargetRepository.findByFlowId(UUID.fromString(flowId));

                // Get message data
                Object messageData = externalTask.getVariable("currentData");
                Map<String, Object> context = externalTask.getAllVariables();

                Map<String, Object> results = new HashMap<>();
                List<String> failures = new ArrayList<>();

                // Execute each target
                for(OrchestrationTarget target : targets) {
                    if(!target.isActive()) {
                        continue;
                    }

                    try {
                        // Execute adapter for target
                        String result = adapterExecutionService.executeAdapter(
                            target.getTargetAdapter(),
                            messageData != null ? messageData.toString() : context.toString(),
                            context
                       );

                        results.put("target_" + target.getId() + "_result", result);
                    } catch(Exception e) {
                        failures.add(target.getTargetAdapter().getName() + ": " + e.getMessage());
                    }
                }

                // Complete task with results
                Map<String, Object> variables = new HashMap<>();
                variables.putAll(results);
                variables.put("orchestrationSuccess", failures.isEmpty());
                if(!failures.isEmpty()) {
                    variables.put("orchestrationFailures", failures);
                }

                externalTaskService.complete(externalTask, variables);
                logger.debug("Orchestration task completed with {} targets executed", results.size());

            } catch(Exception e) {
                logger.error("Error executing orchestration task", e);
                externalTaskService.handleFailure(
                    externalTask,
                    e.getMessage(),
                    getStackTrace(e),
                    0,
                    0L
               );
            }
        }
    }

    /**
     * Handler for routing tasks
     */
    private class RoutingTaskHandler implements ExternalTaskHandler {
        @Override
        public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {
            logger.debug("Executing routing task: {}", externalTask.getActivityId());

            try {
                // Get routing rules
                String routingRules = externalTask.getVariable("routingRules");
                Object messageData = externalTask.getVariable("currentData");

                // Simple routing logic(in production would use ConditionEvaluationService)
                String selectedRoute = evaluateRouting(routingRules, messageData, externalTask.getAllVariables());

                // Complete task with selected route
                Map<String, Object> variables = new HashMap<>();
                variables.put("selectedRoute", selectedRoute);
                variables.put("routingSuccess", true);

                externalTaskService.complete(externalTask, variables);
                logger.debug("Routing task completed, selected route: {}", selectedRoute);

            } catch(Exception e) {
                logger.error("Error executing routing task", e);
                externalTaskService.handleFailure(
                    externalTask,
                    e.getMessage(),
                    getStackTrace(e),
                    0,
                    0L
               );
            }
        }

        private String evaluateRouting(String rules, Object data, Map<String, Object> variables) {
            // Simple implementation-in production would parse and evaluate rules
            if(rules == null || rules.isEmpty()) {
                return "default";
            }

            // Check for specific conditions in variables
            if(variables.containsKey("priority") && "high".equals(variables.get("priority"))) {
                return "express";
            }

            if(variables.containsKey("errorCount") && (Integer) variables.get("errorCount") > 3) {
                return "error";
            }

            return "default";
        }
    }

    /**
     * Handler for generic service tasks
     */
    private class GenericTaskHandler implements ExternalTaskHandler {
        @Override
        public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {
            logger.debug("Executing generic task: {} ( {})", externalTask.getActivityId(), externalTask.getTopicName());

            try {
                // Add timestamp
                Map<String, Object> variables = new HashMap<>();
                variables.put("taskExecutionTime_" + externalTask.getActivityId(), System.currentTimeMillis());

                // Process based on topic
                String topic = externalTask.getTopicName();
                switch(topic) {
                    case "integrix-enrichment":
                        handleEnrichment(externalTask, variables);
                        break;
                    case "integrix-validation":
                        handleValidation(externalTask, variables);
                        break;
                    case "integrix-logging":
                        handleLogging(externalTask, variables);
                        break;
                    default:
                        // Generic processing
                        Object currentData = externalTask.getVariable("currentData");
                        if(currentData != null) {
                            variables.put("currentData", currentData);
                        }
                }

                // Complete task
                externalTaskService.complete(externalTask, variables);
                logger.debug("Generic task completed successfully");

            } catch(Exception e) {
                logger.error("Error executing generic task", e);
                externalTaskService.handleFailure(
                    externalTask,
                    e.getMessage(),
                    getStackTrace(e),
                    1,
                    Duration.ofMinutes(5).toMillis()
               );
            }
        }

        private void handleEnrichment(ExternalTask task, Map<String, Object> variables) {
            // Add enrichment data
            Map<String, Object> enrichmentData = new HashMap<>();
            enrichmentData.put("enrichedAt", new Date());
            enrichmentData.put("enrichmentSource", "integrix");
            variables.put("enrichmentData", enrichmentData);
        }

        private void handleValidation(ExternalTask task, Map<String, Object> variables) {
            // Perform validation
            Object data = task.getVariable("currentData");
            boolean isValid = data != null && !data.toString().isEmpty();
            variables.put("validationResult", isValid);
            variables.put("validationErrors", isValid ? Collections.emptyList() : Arrays.asList("Data is empty"));
        }

        private void handleLogging(ExternalTask task, Map<String, Object> variables) {
            // Log process data
            logger.info("Process logging-Instance: {}, Activity: {}, Variables: {}",
                task.getProcessInstanceId(),
                task.getActivityId(),
                task.getAllVariables().keySet()
           );
            variables.put("logged", true);
        }
    }

    /**
     * Get stack trace as string
     */
    private String getStackTrace(Exception e) {
        StringBuilder sb = new StringBuilder();
        sb.append(e.toString()).append("\n");
        for(StackTraceElement element : e.getStackTrace()) {
            sb.append("\tat ").append(element.toString()).append("\n");
            if(sb.length() > 4000) { // Limit size
                sb.append("\t... truncated");
                break;
            }
        }
        return sb.toString();
    }
}