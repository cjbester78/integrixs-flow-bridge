package com.integrixs.monitoring.infrastructure.persistence;

import com.integrixs.monitoring.domain.model.MonitoringEvent;
import com.integrixs.monitoring.domain.repository.MonitoringEventRepository;
import com.integrixs.monitoring.domain.service.EventLoggingService.EventQueryCriteria;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In - memory implementation of monitoring event repository
 */
@Repository
public class InMemoryMonitoringEventRepository implements MonitoringEventRepository {

    private final Map<String, MonitoringEvent> storage = new ConcurrentHashMap<>();

    @Override
    public MonitoringEvent save(MonitoringEvent event) {
        storage.put(event.getEventId(), event);
        return event;
    }

    @Override
    public List<MonitoringEvent> saveAll(List<MonitoringEvent> events) {
        events.forEach(event -> storage.put(event.getEventId(), event));
        return events;
    }

    @Override
    public Optional<MonitoringEvent> findById(String eventId) {
        return Optional.ofNullable(storage.get(eventId));
    }

    @Override
    public List<MonitoringEvent> findByCorrelationId(String correlationId) {
        return storage.values().stream()
                .filter(event -> correlationId.equals(event.getCorrelationId()))
                .sorted(Comparator.comparing(MonitoringEvent::getTimestamp))
                .collect(Collectors.toList());
    }

    @Override
    public List<MonitoringEvent> findByDomainReference(String domainType, String domainReferenceId) {
        return storage.values().stream()
                .filter(event -> domainType.equals(event.getDomainType()) &&
                               domainReferenceId.equals(event.getDomainReferenceId()))
                .sorted(Comparator.comparing(MonitoringEvent::getTimestamp).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<MonitoringEvent> query(EventQueryCriteria criteria) {
        return storage.values().stream()
                .filter(event -> matchesCriteria(event, criteria))
                .sorted(Comparator.comparing(MonitoringEvent::getTimestamp).reversed())
                .limit(criteria.getLimit() != null ? criteria.getLimit() : 1000)
                .collect(Collectors.toList());
    }

    @Override
    public long count(EventQueryCriteria criteria) {
        return storage.values().stream()
                .filter(event -> matchesCriteria(event, criteria))
                .count();
    }

    @Override
    public long deleteOlderThan(int retentionDays) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);
        List<String> toDelete = storage.entrySet().stream()
                .filter(entry -> entry.getValue().getTimestamp().isBefore(cutoff))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        toDelete.forEach(storage::remove);
        return toDelete.size();
    }

    @Override
    public EventStatistics getStatistics(long startTime, long endTime) {
        LocalDateTime start = LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(startTime), ZoneId.systemDefault());
        LocalDateTime end = LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(endTime), ZoneId.systemDefault());

        List<MonitoringEvent> eventsInRange = storage.values().stream()
                .filter(event -> !event.getTimestamp().isBefore(start) &&
                               !event.getTimestamp().isAfter(end))
                .collect(Collectors.toList());

        EventStatistics stats = new EventStatistics();
        stats.setTotalEvents(eventsInRange.size());

        eventsInRange.forEach(event -> {
            switch(event.getLevel()) {
                case CRITICAL:
                    stats.setCriticalCount(stats.getCriticalCount() + 1);
                    break;
                case ERROR:
                    stats.setErrorCount(stats.getErrorCount() + 1);
                    break;
                case WARNING:
                    stats.setWarningCount(stats.getWarningCount() + 1);
                    break;
                case INFO:
                    stats.setInfoCount(stats.getInfoCount() + 1);
                    break;
                case DEBUG:
                    stats.setDebugCount(stats.getDebugCount() + 1);
                    break;
            }
        });

        return stats;
    }

    private boolean matchesCriteria(MonitoringEvent event, EventQueryCriteria criteria) {
        if(criteria.getEventType() != null && event.getEventType() != criteria.getEventType()) {
            return false;
        }

        if(criteria.getMinLevel() != null && event.getLevel().ordinal() < criteria.getMinLevel().ordinal()) {
            return false;
        }

        if(criteria.getSource() != null && !criteria.getSource().equals(event.getSource())) {
            return false;
        }

        if(criteria.getUserId() != null && !criteria.getUserId().equals(event.getUserId())) {
            return false;
        }

        if(criteria.getDomainType() != null && !criteria.getDomainType().equals(event.getDomainType())) {
            return false;
        }

        if(criteria.getDomainReferenceId() != null &&
            !criteria.getDomainReferenceId().equals(event.getDomainReferenceId())) {
            return false;
        }

        if(criteria.getCorrelationId() != null &&
            !criteria.getCorrelationId().equals(event.getCorrelationId())) {
            return false;
        }

        if(criteria.getStartTime() != null) {
            LocalDateTime start = LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochMilli(criteria.getStartTime()), ZoneId.systemDefault());
            if(event.getTimestamp().isBefore(start)) {
                return false;
            }
        }

        if(criteria.getEndTime() != null) {
            LocalDateTime end = LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochMilli(criteria.getEndTime()), ZoneId.systemDefault());
            if(event.getTimestamp().isAfter(end)) {
                return false;
            }
        }

        return true;
    }
}
