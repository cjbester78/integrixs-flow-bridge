package com.integrixs.backend.service;

import com.integrixs.data.model.*;
import com.integrixs.data.sql.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for monitoring flow execution progress and providing real - time updates
 */
@Service("flowMonitoringService")
public class FlowExecutionMonitoringService {

    private static final Logger log = LoggerFactory.getLogger(FlowExecutionMonitoringService.class);


    private final SystemLogSqlRepository systemLogRepository;
    private final IntegrationFlowSqlRepository flowRepository;

    // In - memory cache for active executions
    private final Map<String, ExecutionProgress> activeExecutions = new ConcurrentHashMap<>();

    public FlowExecutionMonitoringService(SystemLogSqlRepository systemLogRepository,
                                         IntegrationFlowSqlRepository flowRepository) {
        this.systemLogRepository = systemLogRepository;
        this.flowRepository = flowRepository;
    }

    /**
     * Start monitoring a flow execution
     */
    public void startExecution(String correlationId, String flowId, String flowName) {
        ExecutionProgress progress = new ExecutionProgress(correlationId, flowId, flowName);
        activeExecutions.put(correlationId, progress);

        // Log to system log
        SystemLog logEntry = SystemLog.builder()
            .timestamp(LocalDateTime.now())
            .level(SystemLog.LogLevel.INFO)
            .message("Flow execution started")
            .details("Flow: " + flowName)
            .source("FLOW_MONITORING")
            .sourceId(flowId)
            .sourceName(flowName)
            .component("FlowExecutionMonitoringService")
            .domainType("IntegrationFlow")
            .domainReferenceId(flowId)
            .correlationId(correlationId)
            .category("FLOW_MONITORING")
            .build();

        systemLogRepository.save(logEntry);
        log.info("[ {}] Started monitoring flow execution: {}", correlationId, flowName);
    }

    /**
     * Update execution progress
     */
    public void updateExecutionProgress(String correlationId, String stage, String message) {
        ExecutionProgress progress = activeExecutions.get(correlationId);
        if(progress == null) {
            log.warn("No active execution found for correlationId: {}", correlationId);
            return;
        }

        progress.updateStage(stage, message);

        // Log to system log
        SystemLog logEntry = SystemLog.builder()
            .timestamp(LocalDateTime.now())
            .level(SystemLog.LogLevel.INFO)
            .message("Execution progress: " + stage)
            .details(message)
            .source("FLOW_MONITORING")
            .sourceId(progress.getFlowId())
            .sourceName(progress.getFlowName())
            .component("FlowExecutionMonitoringService")
            .domainType("IntegrationFlow")
            .domainReferenceId(progress.getFlowId())
            .correlationId(correlationId)
            .category("FLOW_MONITORING")
            .build();

        systemLogRepository.save(logEntry);
        log.info("[ {}] Progress update - Stage: {}, Message: {}", correlationId, stage, message);
    }

    /**
     * Complete execution monitoring
     */
    public void completeExecution(String correlationId, boolean success, String message) {
        ExecutionProgress progress = activeExecutions.remove(correlationId);
        if(progress == null) {
            log.warn("No active execution found for correlationId: {}", correlationId);
            return;
        }

        progress.complete(success);

        // Log completion
        SystemLog logEntry = SystemLog.builder()
            .timestamp(LocalDateTime.now())
            .level(success ? SystemLog.LogLevel.INFO : SystemLog.LogLevel.ERROR)
            .message("Flow execution " + (success ? "completed successfully" : "failed"))
            .details(message)
            .source("FLOW_MONITORING")
            .sourceId(progress.getFlowId())
            .sourceName(progress.getFlowName())
            .component("FlowExecutionMonitoringService")
            .domainType("IntegrationFlow")
            .domainReferenceId(progress.getFlowId())
            .correlationId(correlationId)
            .category("FLOW_MONITORING")
            .build();

        systemLogRepository.save(logEntry);

        log.info("[ {}] Execution {} - Duration: {}ms",
            correlationId,
            success ? "succeeded" : "failed",
            progress.getDuration());
    }

    /**
     * Get current execution progress
     */
    public ExecutionProgress getExecutionProgress(String correlationId) {
        return activeExecutions.get(correlationId);
    }

    /**
     * Get all active executions
     */
    public Collection<ExecutionProgress> getActiveExecutions() {
        return new ArrayList<>(activeExecutions.values());
    }

    /**
     * Get execution history from logs
     */
    public List<SystemLog> getExecutionHistory(String correlationId) {
        return systemLogRepository.findByCorrelationIdOrderByTimestamp(correlationId);
    }

    /**
     * Clean up stale executions(older than 1 hour)
     */
    public void cleanupStaleExecutions() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(1);

        activeExecutions.entrySet().removeIf(entry -> {
            ExecutionProgress progress = entry.getValue();
            if(progress.getStartTime().isBefore(cutoff) && !progress.isCompleted()) {
                log.warn("Removing stale execution: {}", entry.getKey());
                completeExecution(entry.getKey(), false, "Execution timed out");
                return true;
            }
            return false;
        });
    }

    /**
     * Execution progress tracking class
     */
    public static class ExecutionProgress {
        private final String correlationId;
        private final String flowId;
        private final String flowName;
        private final LocalDateTime startTime;
        private LocalDateTime endTime;
        private String currentStage;
        private String currentMessage;
        private final List<StageProgress> stages;
        private boolean completed;
        private boolean success;

        public ExecutionProgress(String correlationId, String flowId, String flowName) {
            this.correlationId = correlationId;
            this.flowId = flowId;
            this.flowName = flowName;
            this.startTime = LocalDateTime.now();
            this.stages = new ArrayList<>();
            this.completed = false;
        }

        public void updateStage(String stage, String message) {
            this.currentStage = stage;
            this.currentMessage = message;
            stages.add(new StageProgress(stage, message, LocalDateTime.now()));
        }

        public void complete(boolean success) {
            this.completed = true;
            this.success = success;
            this.endTime = LocalDateTime.now();
        }

        public long getDuration() {
            LocalDateTime end = endTime != null ? endTime : LocalDateTime.now();
            return java.time.Duration.between(startTime, end).toMillis();
        }

        // Getters
        public String getCorrelationId() { return correlationId; }
        public String getFlowId() { return flowId; }
        public String getFlowName() { return flowName; }
        public LocalDateTime getStartTime() { return startTime; }
        public LocalDateTime getEndTime() { return endTime; }
        public String getCurrentStage() { return currentStage; }
        public String getCurrentMessage() { return currentMessage; }
        public List<StageProgress> getStages() { return new ArrayList<>(stages); }
        public boolean isCompleted() { return completed; }
        public boolean isSuccess() { return success; }
    }

    /**
     * Stage progress tracking
     */
    public static class StageProgress {
        private final String stage;
        private final String message;
        private final LocalDateTime timestamp;

        public StageProgress(String stage, String message, LocalDateTime timestamp) {
            this.stage = stage;
            this.message = message;
            this.timestamp = timestamp;
        }

        // Getters
        public String getStage() { return stage; }
        public String getMessage() { return message; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }
}
