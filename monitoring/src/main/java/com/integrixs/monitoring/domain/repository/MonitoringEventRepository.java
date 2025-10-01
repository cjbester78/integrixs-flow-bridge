package com.integrixs.monitoring.domain.repository;

import com.integrixs.monitoring.domain.model.MonitoringEvent;
import com.integrixs.monitoring.domain.service.EventLoggingService.EventQueryCriteria;

import java.util.List;
import java.util.Optional;

/**
 * Domain repository interface for monitoring events
 */
public interface MonitoringEventRepository {

    /**
     * Save a monitoring event
     * @param event Event to save
     * @return Saved event
     */
    MonitoringEvent save(MonitoringEvent event);

    /**
     * Save multiple events in batch
     * @param events Events to save
     * @return Saved events
     */
    List<MonitoringEvent> saveAll(List<MonitoringEvent> events);

    /**
     * Find event by ID
     * @param eventId Event ID
     * @return Event if found
     */
    Optional<MonitoringEvent> findById(String eventId);

    /**
     * Find events by correlation ID
     * @param correlationId Correlation ID
     * @return List of events
     */
    List<MonitoringEvent> findByCorrelationId(String correlationId);

    /**
     * Find events by domain reference
     * @param domainType Domain type
     * @param domainReferenceId Domain reference ID
     * @return List of events
     */
    List<MonitoringEvent> findByDomainReference(String domainType, String domainReferenceId);

    /**
     * Query events based on criteria
     * @param criteria Query criteria
     * @return List of matching events
     */
    List<MonitoringEvent> query(EventQueryCriteria criteria);

    /**
     * Count events matching criteria
     * @param criteria Query criteria
     * @return Event count
     */
    long count(EventQueryCriteria criteria);

    /**
     * Delete old events
     * @param retentionDays Number of days to retain
     * @return Number of deleted events
     */
    long deleteOlderThan(int retentionDays);

    /**
     * Get event statistics
     * @param startTime Start time
     * @param endTime End time
     * @return Statistics map
     */
    EventStatistics getStatistics(long startTime, long endTime);

    /**
     * Event statistics
     */
    class EventStatistics {
        private long totalEvents;
        private long errorCount;
        private long warningCount;
        private long infoCount;
        private long debugCount;
        private long criticalCount;

        // Getters and setters
        public long getTotalEvents() { return totalEvents; }
        public void setTotalEvents(long totalEvents) { this.totalEvents = totalEvents; }

        public long getErrorCount() { return errorCount; }
        public void setErrorCount(long errorCount) { this.errorCount = errorCount; }

        public long getWarningCount() { return warningCount; }
        public void setWarningCount(long warningCount) { this.warningCount = warningCount; }

        public long getInfoCount() { return infoCount; }
        public void setInfoCount(long infoCount) { this.infoCount = infoCount; }

        public long getDebugCount() { return debugCount; }
        public void setDebugCount(long debugCount) { this.debugCount = debugCount; }

        public long getCriticalCount() { return criticalCount; }
        public void setCriticalCount(long criticalCount) { this.criticalCount = criticalCount; }
    }
}
