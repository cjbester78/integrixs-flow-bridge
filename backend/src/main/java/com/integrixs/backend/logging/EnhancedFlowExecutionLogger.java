package com.integrixs.backend.logging;

import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enhanced logger for flow execution providing detailed operation visibility.
 */
@Service
public class EnhancedFlowExecutionLogger {

    private static final Logger log = LoggerFactory.getLogger(EnhancedFlowExecutionLogger.class);


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
        logMessage.append("Correlation - ID: ").append(context.getCorrelationId()).append("\n");
        logMessage.append("Message - ID: ").append(context.getMessageId()).append("\n");
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
        logMessage.append("Correlation - ID: ").append(context.getCorrelationId()).append("\n");
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
        logMessage.append("Correlation - ID: ").append(context.getCorrelationId()).append("\n");
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
        logMessage.append("Input - Format: ").append(context.getInputFormat()).append(" -> Output - Format: ").append(context.getOutputFormat()).append("\n");
        logMessage.append("Correlation - ID: ").append(MDC.get("correlationId")).append("\n");

        if(context.getConfiguration() != null && !context.getConfiguration().isEmpty()) {
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
        logMessage.append("Correlation - ID: ").append(MDC.get("correlationId")).append("\n");

        if("OUTBOUND".equals(context.getDirection())) {
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
        logMessage.append("Correlation - ID: ").append(MDC.get("correlationId"));

        log.info(logMessage.toString());
    }

    public void logResponseMapping(String mappingName, String version, String responseTime) {
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("Executing Response Mapping \"").append(mappingName).append("\" (SWCV ").append(version).append(")\n");
        logMessage.append("Response Time: ").append(responseTime).append("ms\n");
        logMessage.append("Correlation - ID: ").append(MDC.get("correlationId"));

        log.info(logMessage.toString());
    }

    public void logMessageRouting(MessageRoutingContext context) {
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("MESSAGE.ROUTING\n");
        logMessage.append("Router: ").append(context.getRouterName()).append("\n");
        logMessage.append("Condition: ").append(context.getCondition()).append("\n");
        logMessage.append("Selected Route: ").append(context.getSelectedRoute()).append("\n");
        logMessage.append("Target Flow: ").append(context.getTargetFlow()).append("\n");
        logMessage.append("Correlation - ID: ").append(MDC.get("correlationId")).append("\n");
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

        // Private constructor for builder
        private FlowExecutionContext() {}

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
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

            public Builder flowId(String flowId) {
                this.flowId = flowId;
                return this;
            }

            public Builder flowName(String flowName) {
                this.flowName = flowName;
                return this;
            }

            public Builder flowVersion(String flowVersion) {
                this.flowVersion = flowVersion;
                return this;
            }

            public Builder sourceSystem(String sourceSystem) {
                this.sourceSystem = sourceSystem;
                return this;
            }

            public Builder targetSystem(String targetSystem) {
                this.targetSystem = targetSystem;
                return this;
            }

            public Builder correlationId(String correlationId) {
                this.correlationId = correlationId;
                return this;
            }

            public Builder messageId(String messageId) {
                this.messageId = messageId;
                return this;
            }

            public Builder payloadSize(long payloadSize) {
                this.payloadSize = payloadSize;
                return this;
            }

            public Builder currentStep(String currentStep) {
                this.currentStep = currentStep;
                return this;
            }

            public Builder stepsExecuted(int stepsExecuted) {
                this.stepsExecuted = stepsExecuted;
                return this;
            }

            public Builder messagesProcessed(int messagesProcessed) {
                this.messagesProcessed = messagesProcessed;
                return this;
            }

            public FlowExecutionContext build() {
                FlowExecutionContext context = new FlowExecutionContext();
                context.flowId = this.flowId;
                context.flowName = this.flowName;
                context.flowVersion = this.flowVersion;
                context.sourceSystem = this.sourceSystem;
                context.targetSystem = this.targetSystem;
                context.correlationId = this.correlationId;
                context.messageId = this.messageId;
                context.payloadSize = this.payloadSize;
                context.currentStep = this.currentStep;
                context.stepsExecuted = this.stepsExecuted;
                context.messagesProcessed = this.messagesProcessed;
                return context;
            }
        }

        // Getters
        public String getFlowId() { return flowId; }
        public String getFlowName() { return flowName; }
        public String getFlowVersion() { return flowVersion; }
        public String getSourceSystem() { return sourceSystem; }
        public String getTargetSystem() { return targetSystem; }
        public String getCorrelationId() { return correlationId; }
        public String getMessageId() { return messageId; }
        public long getPayloadSize() { return payloadSize; }
        public String getCurrentStep() { return currentStep; }
        public int getStepsExecuted() { return stepsExecuted; }
        public int getMessagesProcessed() { return messagesProcessed; }

        // Setters
        public void setFlowId(String flowId) { this.flowId = flowId; }
        public void setFlowName(String flowName) { this.flowName = flowName; }
        public void setFlowVersion(String flowVersion) { this.flowVersion = flowVersion; }
        public void setSourceSystem(String sourceSystem) { this.sourceSystem = sourceSystem; }
        public void setTargetSystem(String targetSystem) { this.targetSystem = targetSystem; }
        public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
        public void setMessageId(String messageId) { this.messageId = messageId; }
        public void setPayloadSize(long payloadSize) { this.payloadSize = payloadSize; }
        public void setCurrentStep(String currentStep) { this.currentStep = currentStep; }
        public void setStepsExecuted(int stepsExecuted) { this.stepsExecuted = stepsExecuted; }
        public void setMessagesProcessed(int messagesProcessed) { this.messagesProcessed = messagesProcessed; }
    }

    public static class TransformationContext {
        private int stepNumber;
        private int totalSteps;
        private String transformationName;
        private String transformationType;
        private String inputFormat;
        private String outputFormat;
        private Map<String, String> configuration;
        // Private constructor for builder
        private TransformationContext() {}

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private int stepNumber;
            private int totalSteps;
            private String transformationName;
            private String transformationType;
            private String inputFormat;
            private String outputFormat;
            private Map<String, String> configuration;

            public Builder stepNumber(int stepNumber) {
                this.stepNumber = stepNumber;
                return this;
            }

            public Builder totalSteps(int totalSteps) {
                this.totalSteps = totalSteps;
                return this;
            }

            public Builder transformationName(String transformationName) {
                this.transformationName = transformationName;
                return this;
            }

            public Builder transformationType(String transformationType) {
                this.transformationType = transformationType;
                return this;
            }

            public Builder inputFormat(String inputFormat) {
                this.inputFormat = inputFormat;
                return this;
            }

            public Builder outputFormat(String outputFormat) {
                this.outputFormat = outputFormat;
                return this;
            }

            public Builder configuration(Map<String, String> configuration) {
                this.configuration = configuration;
                return this;
            }

            public TransformationContext build() {
                TransformationContext context = new TransformationContext();
                context.stepNumber = this.stepNumber;
                context.totalSteps = this.totalSteps;
                context.transformationName = this.transformationName;
                context.transformationType = this.transformationType;
                context.inputFormat = this.inputFormat;
                context.outputFormat = this.outputFormat;
                context.configuration = this.configuration;
                return context;
            }
        }

        // Getters
        public int getStepNumber() { return stepNumber; }
        public int getTotalSteps() { return totalSteps; }
        public String getTransformationName() { return transformationName; }
        public String getTransformationType() { return transformationType; }
        public String getInputFormat() { return inputFormat; }
        public String getOutputFormat() { return outputFormat; }
        public Map<String, String> getConfiguration() { return configuration; }

        // Setters
        public void setStepNumber(int stepNumber) { this.stepNumber = stepNumber; }
        public void setTotalSteps(int totalSteps) { this.totalSteps = totalSteps; }
        public void setTransformationName(String transformationName) { this.transformationName = transformationName; }
        public void setTransformationType(String transformationType) { this.transformationType = transformationType; }
        public void setInputFormat(String inputFormat) { this.inputFormat = inputFormat; }
        public void setOutputFormat(String outputFormat) { this.outputFormat = outputFormat; }
        public void setConfiguration(Map<String, String> configuration) { this.configuration = configuration; }
    }

    public static class AdapterCommunicationContext {
        private String adapterName;
        private String adapterType;
        private String direction; // INBOUND or OUTBOUND
        private String endpoint;
        private String protocol;
        private String sourceSystem;
        private String targetSystem;
        private long payloadSize;

        // Private constructor for builder
        private AdapterCommunicationContext() {}

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String adapterName;
            private String adapterType;
            private String direction;
            private String endpoint;
            private String protocol;
            private String sourceSystem;
            private String targetSystem;
            private long payloadSize;

            public Builder adapterName(String adapterName) {
                this.adapterName = adapterName;
                return this;
            }

            public Builder adapterType(String adapterType) {
                this.adapterType = adapterType;
                return this;
            }

            public Builder direction(String direction) {
                this.direction = direction;
                return this;
            }

            public Builder endpoint(String endpoint) {
                this.endpoint = endpoint;
                return this;
            }

            public Builder protocol(String protocol) {
                this.protocol = protocol;
                return this;
            }

            public Builder sourceSystem(String sourceSystem) {
                this.sourceSystem = sourceSystem;
                return this;
            }

            public Builder targetSystem(String targetSystem) {
                this.targetSystem = targetSystem;
                return this;
            }

            public Builder payloadSize(long payloadSize) {
                this.payloadSize = payloadSize;
                return this;
            }

            public AdapterCommunicationContext build() {
                AdapterCommunicationContext context = new AdapterCommunicationContext();
                context.adapterName = this.adapterName;
                context.adapterType = this.adapterType;
                context.direction = this.direction;
                context.endpoint = this.endpoint;
                context.protocol = this.protocol;
                context.sourceSystem = this.sourceSystem;
                context.targetSystem = this.targetSystem;
                context.payloadSize = this.payloadSize;
                return context;
            }
        }

        // Getters
        public String getAdapterName() { return adapterName; }
        public String getAdapterType() { return adapterType; }
        public String getDirection() { return direction; }
        public String getEndpoint() { return endpoint; }
        public String getProtocol() { return protocol; }
        public String getSourceSystem() { return sourceSystem; }
        public String getTargetSystem() { return targetSystem; }
        public long getPayloadSize() { return payloadSize; }

        // Setters
        public void setAdapterName(String adapterName) { this.adapterName = adapterName; }
        public void setAdapterType(String adapterType) { this.adapterType = adapterType; }
        public void setDirection(String direction) { this.direction = direction; }
        public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
        public void setProtocol(String protocol) { this.protocol = protocol; }
        public void setSourceSystem(String sourceSystem) { this.sourceSystem = sourceSystem; }
        public void setTargetSystem(String targetSystem) { this.targetSystem = targetSystem; }
        public void setPayloadSize(long payloadSize) { this.payloadSize = payloadSize; }
    }

    public static class MessageRoutingContext {
        private String routerName;
        private String condition;
        private String selectedRoute;
        private String targetFlow;
        // Private constructor for builder
        private MessageRoutingContext() {}

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String routerName;
            private String condition;
            private String selectedRoute;
            private String targetFlow;

            public Builder routerName(String routerName) {
                this.routerName = routerName;
                return this;
            }

            public Builder condition(String condition) {
                this.condition = condition;
                return this;
            }

            public Builder selectedRoute(String selectedRoute) {
                this.selectedRoute = selectedRoute;
                return this;
            }

            public Builder targetFlow(String targetFlow) {
                this.targetFlow = targetFlow;
                return this;
            }

            public MessageRoutingContext build() {
                MessageRoutingContext context = new MessageRoutingContext();
                context.routerName = this.routerName;
                context.condition = this.condition;
                context.selectedRoute = this.selectedRoute;
                context.targetFlow = this.targetFlow;
                return context;
            }
        }

        // Getters
        public String getRouterName() { return routerName; }
        public String getCondition() { return condition; }
        public String getSelectedRoute() { return selectedRoute; }
        public String getTargetFlow() { return targetFlow; }

        // Setters
        public void setRouterName(String routerName) { this.routerName = routerName; }
        public void setCondition(String condition) { this.condition = condition; }
        public void setSelectedRoute(String selectedRoute) { this.selectedRoute = selectedRoute; }
        public void setTargetFlow(String targetFlow) { this.targetFlow = targetFlow; }
    }
}
