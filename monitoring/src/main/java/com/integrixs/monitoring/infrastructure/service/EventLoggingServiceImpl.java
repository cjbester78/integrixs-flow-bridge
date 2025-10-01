package com.integrixs.monitoring.infrastructure.service;

import com.integrixs.monitoring.domain.model.MonitoringEvent;
import com.integrixs.monitoring.domain.repository.MonitoringEventRepository;
import com.integrixs.monitoring.domain.service.EventLoggingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Infrastructure implementation of event logging service
 */
@Service
public class EventLoggingServiceImpl implements EventLoggingService {

    private static final Logger log = LoggerFactory.getLogger(EventLoggingServiceImpl.class);
    private final MonitoringEventRepository eventRepository;

    public EventLoggingServiceImpl(MonitoringEventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    public void logEvent(MonitoringEvent event) {
        try {
            eventRepository.save(event);
            log.debug("Event logged: {} - {}", event.getEventType(), event.getMessage());
        } catch(Exception e) {
            log.error("Error logging event: {}", e.getMessage(), e);
        }
    }

    @Override
    public void logEventsBatch(List<MonitoringEvent> events) {
        try {
            eventRepository.saveAll(events);
            log.debug("Batch logged {} events", events.size());
        } catch(Exception e) {
            log.error("Error batch logging events: {}", e.getMessage(), e);
        }
    }

    @Override
    public void logSystemEvent(MonitoringEvent.EventLevel level, String source, String message, Object metadata) {
        MonitoringEvent event = MonitoringEvent.builder()
                .eventType(MonitoringEvent.EventType.SYSTEM_LOG)
                .level(level)
                .source(source)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();

        if(metadata != null) {
            event.addMetadata("details", metadata);
        }

        logEvent(event);
    }

    @Override
    public void logFlowExecution(String flowId, String flowName, String status, long executionTime, String correlationId) {
        MonitoringEvent event = MonitoringEvent.builder()
                .eventType(MonitoringEvent.EventType.FLOW_EXECUTION)
                .level(MonitoringEvent.EventLevel.INFO)
                .source("FlowExecutionEngine")
                .message("Flow execution completed: " + flowName)
                .correlationId(correlationId)
                .domainType("Flow")
                .domainReferenceId(flowId)
                .build();

        event.addMetadata("flowName", flowName);
        event.addMetadata("status", status);
        event.addMetadata("executionTime", executionTime);

        logEvent(event);
    }

    @Override
    public void logAdapterOperation(String adapterId, String operation, boolean success, long duration, int recordsProcessed) {
        MonitoringEvent event = MonitoringEvent.builder()
                .eventType(MonitoringEvent.EventType.ADAPTER_OPERATION)
                .level(success ? MonitoringEvent.EventLevel.INFO : MonitoringEvent.EventLevel.ERROR)
                .source("AdapterFramework")
                .message("Adapter operation: " + operation)
                .domainType("Adapter")
                .domainReferenceId(adapterId)
                .build();

        event.addMetadata("operation", operation);
        event.addMetadata("success", success);
        event.addMetadata("duration", duration);
        event.addMetadata("recordsProcessed", recordsProcessed);

        logEvent(event);
    }

    @Override
    public void logUserActivity(String userId, String activity, String resource, String result) {
        MonitoringEvent event = MonitoringEvent.builder()
                .eventType(MonitoringEvent.EventType.USER_ACTIVITY)
                .level(MonitoringEvent.EventLevel.INFO)
                .source("UserActivityTracker")
                .message("User activity: " + activity)
                .userId(userId)
                .domainType("User")
                .domainReferenceId(userId)
                .build();

        event.addMetadata("activity", activity);
        event.addMetadata("resource", resource);
        event.addMetadata("result", result);

        logEvent(event);
    }

    @Override
    public void logError(String source, String message, Throwable exception, String domainType, String domainReferenceId) {
        MonitoringEvent event = MonitoringEvent.builder()
                .eventType(MonitoringEvent.EventType.ERROR)
                .level(MonitoringEvent.EventLevel.ERROR)
                .source(source)
                .message(message)
                .domainType(domainType)
                .domainReferenceId(domainReferenceId)
                .build();

        if(exception != null) {
            event.setStackTrace(getStackTraceAsString(exception));
            event.addMetadata("exceptionType", exception.getClass().getName());
            event.addMetadata("exceptionMessage", exception.getMessage());
        }

        logEvent(event);
    }

    @Override
    public void logSecurityEvent(String eventType, String userId, String resource, String outcome, Object details) {
        MonitoringEvent event = MonitoringEvent.builder()
                .eventType(MonitoringEvent.EventType.SECURITY)
                .level(MonitoringEvent.EventLevel.WARNING)
                .source("SecurityFramework")
                .message("Security event: " + eventType)
                .userId(userId)
                .build();

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("securityEventType", eventType);
        metadata.put("resource", resource);
        metadata.put("outcome", outcome);
        if(details != null) {
            metadata.put("details", details);
        }
        event.setMetadata(metadata);

        logEvent(event);
    }

    @Override
    public List<MonitoringEvent> queryEvents(EventQueryCriteria criteria) {
        return eventRepository.query(criteria);
    }

    private String getStackTraceAsString(Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        sb.append(throwable.getClass().getName()).append(": ").append(throwable.getMessage()).append("\n");

        StackTraceElement[] stackTrace = throwable.getStackTrace();
        int maxLines = Math.min(stackTrace.length, 50); // Limit stack trace length

        for(int i = 0; i < maxLines; i++) {
            sb.append("\tat ").append(stackTrace[i]).append("\n");
        }

        if(stackTrace.length > maxLines) {
            sb.append("\t... ").append(stackTrace.length - maxLines).append(" more\n");
        }

        if(throwable.getCause() != null) {
            sb.append("Caused by: ").append(getStackTraceAsString(throwable.getCause()));
        }

        return sb.toString();
    }
}
