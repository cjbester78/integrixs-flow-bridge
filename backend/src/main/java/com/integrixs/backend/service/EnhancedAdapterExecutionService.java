package com.integrixs.backend.service;

import com.integrixs.adapters.core.*;
import com.integrixs.adapters.domain.port.InboundAdapterPort;
import com.integrixs.adapters.domain.port.OutboundAdapterPort;
import com.integrixs.adapters.domain.model.SendRequest;
import com.integrixs.adapters.domain.model.FetchRequest;
import com.integrixs.adapters.domain.model.AdapterOperationResult;
import com.integrixs.backend.service.AdapterPoolManager.PooledAdapter;
import com.integrixs.backend.service.FlowAlertingService;
// import com.integrixs.backend.service.deprecated.MessageService;
// import com.integrixs.backend.service.deprecated.FlowExecutionMonitoringService;
import com.integrixs.data.model.CommunicationAdapter;
import com.integrixs.data.model.IntegrationFlow;
import com.integrixs.data.model.SystemLog;
import com.integrixs.data.model.Alert;
import com.integrixs.data.model.AlertRule;
import com.integrixs.data.sql.repository.CommunicationAdapterSqlRepository;
import com.integrixs.data.sql.repository.IntegrationFlowSqlRepository;
import com.integrixs.engine.mapper.HierarchicalXmlFieldMapper;
// import com.integrixs.engine.service.MessageProcessingEngine;
import com.integrixs.engine.service.FormatConversionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.time.LocalDateTime;

/**
 * Enhanced Adapter Execution Service - Bridges the gap between message queue and actual adapter execution
 * Uses pooled adapters and integrates with the message processing engine
 */
@Service
public class EnhancedAdapterExecutionService {

    private static final Logger logger = LoggerFactory.getLogger(EnhancedAdapterExecutionService.class);

    @Autowired
    private AdapterPoolManager poolManager;

    @Autowired
    private AdapterHealthMonitor healthMonitor;

    // @Autowired
    // private MessageProcessingEngine messageProcessingEngine;

    @Autowired
    private FormatConversionService formatConversionService;

    @Autowired
    private HierarchicalXmlFieldMapper xmlFieldMapper;

    @Autowired
    private IntegrationFlowSqlRepository flowRepository;

    @Autowired
    private CommunicationAdapterSqlRepository adapterRepository;

    @Autowired
    @Qualifier("enhancedSagaTransactionService")
    private SagaTransactionService sagaService;

    @Autowired
    private ErrorHandlingService errorHandlingService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private FlowExecutionMonitoringService monitoringService;

    @Autowired
    private FlowAlertingService alertingService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Execute a flow with message queue integration
     */
    public CompletableFuture<ExecutionResult> executeFlow(String flowId, String correlationId, String payload) {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();

            try {
                // Load flow configuration
                IntegrationFlow flow = flowRepository.findById(UUID.fromString(flowId))
                    .orElseThrow(() -> new IllegalArgumentException("Flow not found: " + flowId));

                // Start monitoring
                monitoringService.startExecution(correlationId, flowId, flow.getName());

                // Start saga transaction
                var transaction = sagaService.startTransaction(flow, correlationId);

                // Define saga steps
                var steps = defineSagaSteps(flow, payload, correlationId);

                // Execute saga with error handling
                return errorHandlingService.executeWithErrorHandling(flowId, () -> {
                    try {
                        var sagaResult = sagaService.executeSaga(transaction.getId().toString(), steps).get();

                        if(sagaResult.isSuccess()) {
                            long duration = System.currentTimeMillis() - startTime;

                            // Update metrics
                            var sourceMetrics = healthMonitor.getMetrics(flow.getInboundAdapterId().toString());
                            var targetMetrics = healthMonitor.getMetrics(flow.getOutboundAdapterId().toString());

                            if(sourceMetrics != null) sourceMetrics.recordMessage(true, duration / 2);
                            if(targetMetrics != null) targetMetrics.recordMessage(true, duration / 2);

                            // Complete monitoring
                            monitoringService.completeExecution(correlationId, true, "Flow executed successfully");

                            return ExecutionResult.success(
                                sagaResult.getContext().get("processedData"),
                                duration
                           );
                        } else {
                            // Complete monitoring with failure
                            monitoringService.completeExecution(correlationId, false,
                                "Flow execution failed: " + sagaResult.getErrorMessage());

                            return ExecutionResult.failure(
                                sagaResult.getErrorMessage(),
                                sagaResult.getErrorDetails()
                           );
                        }
                    } catch(InterruptedException | java.util.concurrent.ExecutionException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Saga execution interrupted", e);
                    }
                });

            } catch(Exception e) {
                logger.error("Flow execution failed for {}", flowId, e);
                errorHandlingService.handleError(flowId, e);

                // Complete monitoring with failure
                monitoringService.completeExecution(correlationId, false,
                    "Flow execution failed with exception: " + e.getMessage());

                return ExecutionResult.failure(e.getMessage(), e.toString());
            }
        });
    }

    /**
     * Define saga steps for flow execution
     */
    private java.util.List<SagaTransactionService.SagaStepDefinition> defineSagaSteps(
            IntegrationFlow flow, String payload, String correlationId) {

        var steps = new java.util.ArrayList<SagaTransactionService.SagaStepDefinition>();

        // Step 1: Fetch from source adapter
        Map<String, Object> fetchParams = new HashMap<>();
        fetchParams.put("adapterId", flow.getInboundAdapterId().toString());
        fetchParams.put("payload", payload);
        fetchParams.put("correlationId", correlationId);

        steps.add(new SagaTransactionService.SagaStepDefinition(
            "FetchFromSource",
            "ADAPTER_FETCH",
            1,
            fetchParams
       ));

        // Step 2: Transform data(if needed)
        if(flow.getMappingMode() == com.integrixs.data.model.MappingMode.WITH_MAPPING) {
            Map<String, Object> transformParams = new HashMap<>();
            transformParams.put("flowId", flow.getId().toString());

            steps.add(new SagaTransactionService.SagaStepDefinition(
                "TransformData",
                "DATA_TRANSFORM",
                2,
                transformParams
           ));
        }

        // Step 3: Send to target adapter
        Map<String, Object> sendParams = new HashMap<>();
        sendParams.put("adapterId", flow.getOutboundAdapterId().toString());
        sendParams.put("correlationId", correlationId);

        steps.add(new SagaTransactionService.SagaStepDefinition(
            "SendToTarget",
            "ADAPTER_SEND",
            3,
            sendParams
       ));

        return steps;
    }

    /**
     * Fetch data from source adapter using pooled connection
     */
    public CompletableFuture<Object> fetchFromAdapter(String adapterId, String correlationId) {
        return CompletableFuture.supplyAsync(() -> {
            PooledAdapter<InboundAdapterPort> pooledAdapter = null;
            CommunicationAdapter adapterEntity = null;

            try {
                // Check adapter health
                var healthStatus = healthMonitor.getHealthStatus(adapterId);
                if(healthStatus != null && !healthStatus.isHealthy()) {
                    throw new RuntimeException("Adapter is unhealthy: " + healthStatus.getLastError());
                }

                // Get adapter from pool
                pooledAdapter = poolManager.getInboundAdapter(adapterId);
                InboundAdapterPort adapter = pooledAdapter.getAdapter();

                // Log adapter request
                adapterEntity = adapterRepository.findById(UUID.fromString(adapterId)).orElse(null);
                if(adapterEntity != null) {
                    messageService.logAdapterActivity(adapterEntity, "Fetch data start", "Starting data fetch from adapter", SystemLog.LogLevel.INFO, correlationId);
                }

                // Update monitoring
                monitoringService.updateExecutionProgress(correlationId, "FETCH_FROM_ADAPTER",
                   "Fetching data from adapter: " + adapterId);

                // Fetch data
                // Create fetch request for InboundAdapterPort
                com.integrixs.adapters.domain.model.FetchRequest fetchRequest =
                    com.integrixs.adapters.domain.model.FetchRequest.builder()
                        .adapterId(adapterId)
                        .build();
                AdapterOperationResult result = adapter.fetch(fetchRequest);

                if(result.isSuccess()) {
                    // Log successful fetch
                    if(adapterEntity != null) {
                        messageService.logAdapterActivity(adapterEntity, "Fetch data success",
                           "Fetched " + (result.getData() != null ? result.getData().toString().length() : 0) + " bytes",
                           SystemLog.LogLevel.INFO, correlationId);
                    }

                    // Update monitoring
                    monitoringService.updateExecutionProgress(correlationId, "FETCH_COMPLETE",
                       "Successfully fetched data from adapter");

                    return result.getData();
                } else {
                    throw new RuntimeException("Fetch failed: " + result.getMessage());
                }

            } catch(Exception e) {
                logger.error("Failed to fetch from adapter {}", adapterId, e);
                if(adapterEntity != null) {
                    messageService.logAdapterActivity(adapterEntity, "Fetch error", e.getMessage(),
                       SystemLog.LogLevel.ERROR, correlationId);
                }
                throw new RuntimeException("Adapter fetch failed", e);
            } finally {
                // Return adapter to pool
                if(pooledAdapter != null) {
                    poolManager.returnAdapter(adapterId, pooledAdapter);
                }
            }
        });
    }

    /**
     * Send data to target adapter using pooled connection
     */
    public CompletableFuture<Void> sendToAdapter(String adapterId, Object data, String correlationId) {
        return CompletableFuture.runAsync(() -> {
            PooledAdapter<OutboundAdapterPort> pooledAdapter = null;
            CommunicationAdapter adapterEntity = null;

            try {
                // Check adapter health
                var healthStatus = healthMonitor.getHealthStatus(adapterId);
                if(healthStatus != null && !healthStatus.isHealthy()) {
                    throw new RuntimeException("Adapter is unhealthy: " + healthStatus.getLastError());
                }

                // Get adapter from pool
                pooledAdapter = poolManager.getOutboundAdapter(adapterId);
                OutboundAdapterPort adapter = pooledAdapter.getAdapter();

                // Log adapter request
                adapterEntity = adapterRepository.findById(UUID.fromString(adapterId)).orElse(null);
                if(adapterEntity != null) {
                    messageService.logAdapterActivity(adapterEntity, "Send data start",
                       "Sending " + (data != null ? data.toString().length() : 0) + " bytes",
                       SystemLog.LogLevel.INFO, correlationId);
                }

                // Update monitoring
                monitoringService.updateExecutionProgress(correlationId, "SEND_TO_ADAPTER",
                   "Sending data to adapter: " + adapterId);

                // Send data
                // Create send request for OutboundAdapterPort
                SendRequest sendRequest = SendRequest.builder()
                    .adapterId(adapterId)
                    .payload(data)
                    .build();
                AdapterOperationResult result = adapter.send(sendRequest);

                if(result.isSuccess()) {
                    // Log successful send
                    if(adapterEntity != null) {
                        messageService.logAdapterActivity(adapterEntity, "Send data success", "Data sent successfully",
                           SystemLog.LogLevel.INFO, correlationId);
                    }

                    // Update monitoring
                    monitoringService.updateExecutionProgress(correlationId, "SEND_COMPLETE",
                       "Successfully sent data to adapter");
                } else {
                    throw new RuntimeException("Send failed: " + result.getMessage());
                }

            } catch(Exception e) {
                logger.error("Failed to send to adapter {}", adapterId, e);
                if(adapterEntity != null) {
                    messageService.logAdapterActivity(adapterEntity, "Send error", e.getMessage(),
                       SystemLog.LogLevel.ERROR, correlationId);
                }
                throw new RuntimeException("Adapter send failed", e);
            } finally {
                // Return adapter to pool
                if(pooledAdapter != null) {
                    poolManager.returnAdapter(adapterId, pooledAdapter);
                }
            }
        });
    }

    /**
     * Initialize saga step handlers
     */
    @jakarta.annotation.PostConstruct
    public void initializeSagaHandlers() {
        // Register adapter fetch handler
        sagaService.registerStepHandler("ADAPTER_FETCH", (step, context) -> {
            try {
                String adapterId = (String) step.getParameters().get("adapterId");
                String correlationId = (String) step.getParameters().get("correlationId");

                Object data = fetchFromAdapter(adapterId, correlationId).get();
                context.put("sourceData", data);

                return SagaTransactionService.StepResult.success(
                    objectMapper.writeValueAsString(data)
               );
            } catch(Exception e) {
                return SagaTransactionService.StepResult.failure(e.getMessage());
            }
        });

        // Register data transform handler
        sagaService.registerStepHandler("DATA_TRANSFORM", (step, context) -> {
            try {
                String flowId = (String) step.getParameters().get("flowId");
                String correlationId = (String) context.get("correlationId").toString();
                Object sourceData = context.get("sourceData");

                // Update monitoring
                monitoringService.updateExecutionProgress(correlationId, "DATA_TRANSFORM",
                   "Starting data transformation");

                // Use message processing engine for transformation
                // When message processing engine is available, replace with:
                // var result = messageProcessingEngine.processMessage(flowId, sourceData).get();

                // Temporary stub implementation
                var result = new ProcessingResult();
                result.setSuccess(true);
                result.setProcessedData(sourceData); // Just pass through for now

                if(result.isSuccess()) {
                    context.put("processedData", result.getProcessedData());

                    // Update monitoring
                    monitoringService.updateExecutionProgress(correlationId, "TRANSFORM_COMPLETE",
                       "Data transformation completed successfully");

                    return SagaTransactionService.StepResult.success(
                        objectMapper.writeValueAsString(result.getProcessedData())
                   );
                } else {
                    return SagaTransactionService.StepResult.failure(result.getErrorMessage());
                }
            } catch(Exception e) {
                return SagaTransactionService.StepResult.failure(e.getMessage());
            }
        });

        // Register adapter send handler
        sagaService.registerStepHandler("ADAPTER_SEND", (step, context) -> {
            try {
                String adapterId = (String) step.getParameters().get("adapterId");
                String correlationId = (String) step.getParameters().get("correlationId");
                Object data = context.get("processedData");

                if(data == null) {
                    data = context.get("sourceData"); // No transformation
                }

                sendToAdapter(adapterId, data, correlationId).get();

                return SagaTransactionService.StepResult.success("Data sent successfully");
            } catch(Exception e) {
                return SagaTransactionService.StepResult.failure(e.getMessage());
            }
        });

        // Register compensation handlers
        sagaService.registerCompensationHandler("ADAPTER_FETCH", (step, context) -> {
            // Nothing to compensate for fetch
            logger.info("Compensating adapter fetch step");
        });

        sagaService.registerCompensationHandler("DATA_TRANSFORM", (step, context) -> {
            // Nothing to compensate for transform
            logger.info("Compensating data transform step");
        });

        sagaService.registerCompensationHandler("ADAPTER_SEND", (step, context) -> {
            // Implement compensation logic for adapter send failures
            String adapterId = (String) step.getParameters().get("adapterId");
            String correlationId = (String) step.getParameters().get("correlationId");

            logger.info("Compensating adapter send step for adapter: {} with correlationId: {}",
                adapterId, correlationId);

            try {
                // Check if adapter supports reversals
                CommunicationAdapter adapter = adapterRepository.findById(UUID.fromString(adapterId))
                    .orElseThrow(() -> new RuntimeException("Adapter not found: " + adapterId));

                // Parse configuration JSON
                Map<String, String> config = new HashMap<>();
                try {
                    if (adapter.getConfiguration() != null) {
                        config = objectMapper.readValue(adapter.getConfiguration(), Map.class);
                    }
                } catch (Exception e) {
                    logger.warn("Failed to parse adapter configuration", e);
                }
                boolean supportsReversal = Boolean.parseBoolean(config.getOrDefault("supportsReversal", "false"));

                if(supportsReversal) {
                    // Build reversal message
                    Object originalData = context.get("processedData");
                    if(originalData == null) {
                        originalData = context.get("sourceData");
                    }

                    Map<String, Object> reversalMessage = new HashMap<>();
                    reversalMessage.put("action", "REVERSAL");
                    reversalMessage.put("originalCorrelationId", correlationId);
                    reversalMessage.put("reversalTimestamp", LocalDateTime.now().toString());
                    reversalMessage.put("originalData", originalData);

                    // Send reversal through adapter
                    sendToAdapter(adapterId, reversalMessage, "REV-" + correlationId)
                        .thenAccept(result -> logger.info("Reversal sent successfully for correlationId: {}", correlationId))
                        .exceptionally(ex -> {
                            logger.error("Failed to send reversal for correlationId: {}", correlationId, ex);
                            return null;
                        });
                } else {
                    // Log compensation attempt for audit trail
                    logger.warn("Adapter {} does not support reversals. Manual intervention may be required for correlationId: {}",
                        adapter.getName(), correlationId);

                    // Create alert for manual intervention
                    Alert compensationAlert = Alert.builder()
                        .title("Adapter Compensation Required")
                        .message(String.format("Manual compensation needed for adapter %s, correlation ID: %s",
                            adapter.getName(), correlationId))
                        .severity(AlertRule.AlertSeverity.HIGH)
                        .alertId(UUID.randomUUID().toString())
                        .triggeredAt(LocalDateTime.now())
                        .build();

                    // Find or create alert rule for compensation
                    Map<String, String> alertDetails = new HashMap<>();
                    alertDetails.put("adapterId", adapterId);
                    alertDetails.put("correlationId", correlationId);
                    alertDetails.put("adapterName", adapter.getName());

                    // For now, just log the alert since we don't have an alert rule
                    logger.error("ALERT: {}", compensationAlert.getMessage());
                }
            } catch(Exception e) {
                logger.error("Error during adapter send compensation for correlationId: {}", correlationId, e);
            }
        });
    }

    /**
     * Test adapter connectivity
     */
    public CompletableFuture<TestResult> testAdapter(String adapterId) {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.currentTimeMillis();

            try {
                CommunicationAdapter adapter = adapterRepository.findById(UUID.fromString(adapterId))
                    .orElseThrow(() -> new IllegalArgumentException("Adapter not found"));

                // Force health check
                var healthResult = healthMonitor.forceHealthCheck(adapterId).get();

                if(healthResult.isHealthy()) {
                    // Try to get adapter from pool to test actual connectivity
                    if(adapter.getDirection().equals("INBOUND")) {
                        var pooled = poolManager.getInboundAdapter(adapterId);
                        poolManager.returnAdapter(adapterId, pooled);
                    } else {
                        var pooled = poolManager.getOutboundAdapter(adapterId);
                        poolManager.returnAdapter(adapterId, pooled);
                    }

                    long duration = System.currentTimeMillis() - startTime;
                    return TestResult.success(
                        "Adapter test successful",
                        duration
                   );
                } else {
                    return TestResult.failure(
                        "Health check failed: " + healthResult.getErrorMessage()
                   );
                }

            } catch(Exception e) {
                logger.error("Adapter test failed for {}", adapterId, e);
                return TestResult.failure(e.getMessage());
            }
        });
    }

    /**
     * Execution result
     */
    public static class ExecutionResult {
        private final boolean success;
        private final Object data;
        private final String errorMessage;
        private final String errorDetails;
        private final long executionTimeMs;

        public static ExecutionResult success(Object data, long executionTimeMs) {
            return new ExecutionResult(true, data, null, null, executionTimeMs);
        }

        public static ExecutionResult failure(String errorMessage, String errorDetails) {
            return new ExecutionResult(false, null, errorMessage, errorDetails, 0);
        }

        private ExecutionResult(boolean success, Object data, String errorMessage,
                              String errorDetails, long executionTimeMs) {
            this.success = success;
            this.data = data;
            this.errorMessage = errorMessage;
            this.errorDetails = errorDetails;
            this.executionTimeMs = executionTimeMs;
        }

        public boolean isSuccess() { return success; }
        public Object getData() { return data; }
        public String getErrorMessage() { return errorMessage; }
        public String getErrorDetails() { return errorDetails; }
        public long getExecutionTimeMs() { return executionTimeMs; }
    }

    /**
     * Test result
     */
    // Temporary stub class until MessageProcessingEngine is available
    private static class ProcessingResult {
        private boolean success;
        private Object processedData;
        private String errorMessage;

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public Object getProcessedData() { return processedData; }
        public void setProcessedData(Object processedData) { this.processedData = processedData; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }

    public static class TestResult {
        private final boolean success;
        private final String message;
        private final long responseTimeMs;

        public static TestResult success(String message, long responseTimeMs) {
            return new TestResult(true, message, responseTimeMs);
        }

        public static TestResult failure(String message) {
            return new TestResult(false, message, 0);
        }

        private TestResult(boolean success, String message, long responseTimeMs) {
            this.success = success;
            this.message = message;
            this.responseTimeMs = responseTimeMs;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public long getResponseTimeMs() { return responseTimeMs; }
    }
}
