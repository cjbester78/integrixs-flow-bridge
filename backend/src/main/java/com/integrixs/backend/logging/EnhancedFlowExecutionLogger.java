package com.integrixs.backend.logging;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

/**
 * Enhanced logger for flow execution providing detailed operation visibility.
 */
@Slf4j
@Service
public class EnhancedFlowExecutionLogger {
    
    public void logFlowStart(FlowExecutionContext context) {
        // Set MDC context
        MDC.put("flowId", context.getFlowId());
        MDC.put("flowName", context.getFlowName());
        MDC.put("correlationId", context.getCorrelationId());
        MDC.put("messageId", context.getMessageId());
        
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("FLOW.START\n");
        logMessage.append("Flow: ").append(context.getFlowName()).append(" (v").append(context.getFlowVersion()).append(")\n");
        logMessage.append("Flow ID: ").append(context.getFlowId()).append("\n");
        logMessage.append("Source: ").append(context.getSourceSystem()).append(" -> Target: ").append(context.getTargetSystem()).append("\n");
        logMessage.append("Correlation-ID: ").append(context.getCorrelationId()).append("\n");
        logMessage.append("Message-ID: ").append(context.getMessageId()).append("\n");
        logMessage.append("Payload Size: ").append(context.getPayloadSize()).append(" bytes\n");
        logMessage.append("Thread: ").append(Thread.currentThread().getName()).append("\n");
        logMessage.append("Timestamp: ").append(Instant.now());
        
        log.info(logMessage.toString());
    }
    
    public void logFlowComplete(FlowExecutionContext context, long durationMs) {
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("FLOW.COMPLETE\n");
        logMessage.append("Flow: ").append(context.getFlowName()).append(" (v").append(context.getFlowVersion()).append(")\n");
        logMessage.append("Status: SUCCESS\n");
        logMessage.append("Duration: ").append(durationMs).append("ms\n");
        logMessage.append("Correlation-ID: ").append(context.getCorrelationId()).append("\n");
        logMessage.append("Steps Executed: ").append(context.getStepsExecuted()).append("\n");
        logMessage.append("Messages Processed: ").append(context.getMessagesProcessed()).append("\n");
        logMessage.append("Timestamp: ").append(Instant.now());
        
        log.info(logMessage.toString());
        clearMDC();
    }
    
    public void logFlowError(FlowExecutionContext context, Exception error, long durationMs) {
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("FLOW.ERROR\n");
        logMessage.append("Flow: ").append(context.getFlowName()).append(" (v").append(context.getFlowVersion()).append(")\n");
        logMessage.append("Status: FAILED\n");
        logMessage.append("Duration: ").append(durationMs).append("ms\n");
        logMessage.append("Correlation-ID: ").append(context.getCorrelationId()).append("\n");
        logMessage.append("Error Type: ").append(error.getClass().getSimpleName()).append("\n");
        logMessage.append("Error Message: ").append(error.getMessage()).append("\n");
        logMessage.append("Failed Step: ").append(context.getCurrentStep()).append("\n");
        logMessage.append("Timestamp: ").append(Instant.now());
        
        log.error(logMessage.toString(), error);
        clearMDC();
    }
    
    public void logTransformationStep(TransformationContext context) {
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("TRANSFORMATION.EXECUTE\n");
        logMessage.append("Step: ").append(context.getStepNumber()).append(" of ").append(context.getTotalSteps()).append("\n");
        logMessage.append("Name: ").append(context.getTransformationName()).append("\n");
        logMessage.append("Type: ").append(context.getTransformationType()).append("\n");
        logMessage.append("Input-Format: ").append(context.getInputFormat()).append(" -> Output-Format: ").append(context.getOutputFormat()).append("\n");
        logMessage.append("Correlation-ID: ").append(MDC.get("correlationId")).append("\n");
        
        if (context.getConfiguration() != null && !context.getConfiguration().isEmpty()) {
            logMessage.append("Configuration: ").append(context.getConfiguration()).append("\n");
        }
        
        logMessage.append("Timestamp: ").append(Instant.now());
        
        log.info(logMessage.toString());
    }
    
    public void logAdapterCommunication(AdapterCommunicationContext context) {
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("ADAPTER.").append(context.getDirection().toUpperCase()).append("\n");
        logMessage.append("Adapter: ").append(context.getAdapterName()).append("\n");
        logMessage.append("Type: ").append(context.getAdapterType()).append("\n");
        logMessage.append("Endpoint: ").append(context.getEndpoint()).append("\n");
        logMessage.append("Protocol: ").append(context.getProtocol()).append("\n");
        logMessage.append("Correlation-ID: ").append(MDC.get("correlationId")).append("\n");
        
        if ("OUTBOUND".equals(context.getDirection())) {
            logMessage.append("Target System: ").append(context.getTargetSystem()).append("\n");
        } else {
            logMessage.append("Source System: ").append(context.getSourceSystem()).append("\n");
        }
        
        logMessage.append("Payload Size: ").append(context.getPayloadSize()).append(" bytes\n");
        logMessage.append("Timestamp: ").append(Instant.now());
        
        log.info(logMessage.toString());
    }
    
    public void logRequestMapping(String mappingName, String version, String sourceFormat, String targetFormat) {
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("Executing Request Mapping \"").append(mappingName).append("\" (SWCV ").append(version).append(")\n");
        logMessage.append("Source Format: ").append(sourceFormat).append("\n");
        logMessage.append("Target Format: ").append(targetFormat).append("\n");
        logMessage.append("Correlation-ID: ").append(MDC.get("correlationId"));
        
        log.info(logMessage.toString());
    }
    
    public void logResponseMapping(String mappingName, String version, String responseTime) {
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("Executing Response Mapping \"").append(mappingName).append("\" (SWCV ").append(version).append(")\n");
        logMessage.append("Response Time: ").append(responseTime).append("ms\n");
        logMessage.append("Correlation-ID: ").append(MDC.get("correlationId"));
        
        log.info(logMessage.toString());
    }
    
    public void logMessageRouting(MessageRoutingContext context) {
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("MESSAGE.ROUTING\n");
        logMessage.append("Router: ").append(context.getRouterName()).append("\n");
        logMessage.append("Condition: ").append(context.getCondition()).append("\n");
        logMessage.append("Selected Route: ").append(context.getSelectedRoute()).append("\n");
        logMessage.append("Target Flow: ").append(context.getTargetFlow()).append("\n");
        logMessage.append("Correlation-ID: ").append(MDC.get("correlationId")).append("\n");
        logMessage.append("Timestamp: ").append(Instant.now());
        
        log.info(logMessage.toString());
    }
    
    public void logPerformanceMetrics(String operationName, Map<String, Object> metrics) {
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("PERFORMANCE.METRICS\n");
        logMessage.append("Operation: ").append(operationName).append("\n");
        
        metrics.forEach((key, value) -> 
            logMessage.append(key).append(": ").append(value).append("\n")
        );
        
        logMessage.append("Timestamp: ").append(Instant.now());
        
        log.debug(logMessage.toString());
    }
    
    private void clearMDC() {
        MDC.remove("flowId");
        MDC.remove("flowName");
        MDC.remove("correlationId");
        MDC.remove("messageId");
    }
    
    /**
     * Context classes for structured logging
     */
    @lombok.Data
    @lombok.Builder
    public static class FlowExecutionContext {
        private String flowId;
        private String flowName;
        private String flowVersion;
        private String sourceSystem;
        private String targetSystem;
        private String correlationId;
        private String messageId;
        private long payloadSize;
        private String currentStep;
        private int stepsExecuted;
        private int messagesProcessed;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class TransformationContext {
        private int stepNumber;
        private int totalSteps;
        private String transformationName;
        private String transformationType;
        private String inputFormat;
        private String outputFormat;
        private Map<String, String> configuration;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class AdapterCommunicationContext {
        private String adapterName;
        private String adapterType;
        private String direction; // INBOUND or OUTBOUND
        private String endpoint;
        private String protocol;
        private String sourceSystem;
        private String targetSystem;
        private long payloadSize;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class MessageRoutingContext {
        private String routerName;
        private String condition;
        private String selectedRoute;
        private String targetFlow;
    }
}