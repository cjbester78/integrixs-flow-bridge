package com.integrixs.monitoring.domain.service;

import com.integrixs.monitoring.domain.model.MonitoringEvent;

import java.util.List;

/**
 * Domain service interface for event logging
 */
public interface EventLoggingService {

    /**
     * Log a monitoring event
     * @param event The event to log
     */
    void logEvent(MonitoringEvent event);

    /**
     * Log multiple events in batch
     * @param events List of events to log
     */
    void logEventsBatch(List<MonitoringEvent> events);

    /**
     * Log system event
     * @param level Event level
     * @param source Event source
     * @param message Event message
     * @param metadata Additional metadata
     */
    void logSystemEvent(MonitoringEvent.EventLevel level, String source, String message, Object metadata);

    /**
     * Log flow execution event
     * @param flowId Flow ID
     * @param flowName Flow name
     * @param status Execution status
     * @param executionTime Execution duration in milliseconds
     * @param correlationId Correlation ID
     */
    void logFlowExecution(String flowId, String flowName, String status, long executionTime, String correlationId);

    /**
     * Log adapter operation
     * @param adapterId Adapter ID
     * @param operation Operation performed
     * @param success Whether operation succeeded
     * @param duration Operation duration
     * @param recordsProcessed Number of records processed
     */
    void logAdapterOperation(String adapterId, String operation, boolean success, long duration, int recordsProcessed);

    /**
     * Log user activity
     * @param userId User ID
     * @param activity Activity performed
     * @param resource Resource accessed
     * @param result Activity result
     */
    void logUserActivity(String userId, String activity, String resource, String result);

    /**
     * Log error event
     * @param source Error source
     * @param message Error message
     * @param exception Exception that occurred
     * @param domainType Domain type
     * @param domainReferenceId Domain reference ID
     */
    void logError(String source, String message, Throwable exception, String domainType, String domainReferenceId);

    /**
     * Log security event
     * @param eventType Security event type
     * @param userId User involved
     * @param resource Resource accessed
     * @param outcome Event outcome
     * @param details Additional details
     */
    void logSecurityEvent(String eventType, String userId, String resource, String outcome, Object details);

    /**
     * Query events
     * @param criteria Search criteria
     * @return List of matching events
     */
    List<MonitoringEvent> queryEvents(EventQueryCriteria criteria);

    /**
     * Event query criteria
     */
    class EventQueryCriteria {
        private MonitoringEvent.EventType eventType;
        private MonitoringEvent.EventLevel minLevel;
        private String source;
        private String userId;
        private String domainType;
        private String domainReferenceId;
        private String correlationId;
        private Long startTime;
        private Long endTime;
        private Integer limit;

        // Getters and setters
        public MonitoringEvent.EventType getEventType() { return eventType; }
        public void setEventType(MonitoringEvent.EventType eventType) { this.eventType = eventType; }

        public MonitoringEvent.EventLevel getMinLevel() { return minLevel; }
        public void setMinLevel(MonitoringEvent.EventLevel minLevel) { this.minLevel = minLevel; }

        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public String getDomainType() { return domainType; }
        public void setDomainType(String domainType) { this.domainType = domainType; }

        public String getDomainReferenceId() { return domainReferenceId; }
        public void setDomainReferenceId(String domainReferenceId) { this.domainReferenceId = domainReferenceId; }

        public String getCorrelationId() { return correlationId; }
        public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

        public Long getStartTime() { return startTime; }
        public void setStartTime(Long startTime) { this.startTime = startTime; }

        public Long getEndTime() { return endTime; }
        public void setEndTime(Long endTime) { this.endTime = endTime; }

        public Integer getLimit() { return limit; }
        public void setLimit(Integer limit) { this.limit = limit; }
    }
}
