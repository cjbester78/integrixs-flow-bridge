package com.integrixs.engine.infrastructure.persistence;

import com.integrixs.engine.domain.model.WorkflowEvent;
import com.integrixs.engine.domain.repository.WorkflowEventRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Infrastructure implementation of WorkflowEventRepository
 * Currently uses in - memory storage, can be replaced with database persistence
 */
@Repository
public class WorkflowEventRepositoryImpl implements WorkflowEventRepository {

    // In - memory storage for events

    private static final Logger log = LoggerFactory.getLogger(WorkflowEventRepositoryImpl.class);

    private final Map<String, WorkflowEvent> eventStore = new ConcurrentHashMap<>();
    private final Map<String, List<String>> workflowEventIndex = new ConcurrentHashMap<>();
    private final Map<String, List<String>> flowEventIndex = new ConcurrentHashMap<>();

    @Override
    public WorkflowEvent save(WorkflowEvent event) {
        if(event.getEventId() == null) {
            event.setEventId(UUID.randomUUID().toString());
        }

        log.debug("Saving workflow event: {} for workflow: {}", event.getEventType(), event.getWorkflowId());
        eventStore.put(event.getEventId(), event);

        // Update indexes
        workflowEventIndex.computeIfAbsent(event.getWorkflowId(), k -> new ArrayList<>())
                .add(event.getEventId());

        if(event.getFlowId() != null) {
            flowEventIndex.computeIfAbsent(event.getFlowId(), k -> new ArrayList<>())
                    .add(event.getEventId());
        }

        return event;
    }

    @Override
    public List<WorkflowEvent> findByWorkflowId(String workflowId) {
        List<String> eventIds = workflowEventIndex.get(workflowId);
        if(eventIds == null || eventIds.isEmpty()) {
            return Collections.emptyList();
        }

        return eventIds.stream()
                .map(eventStore::get)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(WorkflowEvent::getTimestamp))
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkflowEvent> findByFlowId(String flowId) {
        List<String> eventIds = flowEventIndex.get(flowId);
        if(eventIds == null || eventIds.isEmpty()) {
            return Collections.emptyList();
        }

        return eventIds.stream()
                .map(eventStore::get)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(WorkflowEvent::getTimestamp))
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkflowEvent> findByEventType(WorkflowEvent.EventType eventType) {
        return eventStore.values().stream()
                .filter(event -> eventType.equals(event.getEventType()))
                .sorted(Comparator.comparing(WorkflowEvent::getTimestamp))
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkflowEvent> findByTimestampBetween(LocalDateTime startTime, LocalDateTime endTime) {
        return eventStore.values().stream()
                .filter(event -> {
                    LocalDateTime timestamp = event.getTimestamp();
                    return timestamp != null &&
                           !timestamp.isBefore(startTime) &&
                           !timestamp.isAfter(endTime);
                })
                .sorted(Comparator.comparing(WorkflowEvent::getTimestamp))
                .collect(Collectors.toList());
    }

    @Override
    public List<WorkflowEvent> findByWorkflowIdAndEventType(String workflowId, WorkflowEvent.EventType eventType) {
        return findByWorkflowId(workflowId).stream()
                .filter(event -> eventType.equals(event.getEventType()))
                .collect(Collectors.toList());
    }

    @Override
    public int deleteByTimestampBefore(LocalDateTime beforeDate) {
        List<String> toDelete = eventStore.values().stream()
                .filter(event -> event.getTimestamp().isBefore(beforeDate))
                .map(WorkflowEvent::getEventId)
                .collect(Collectors.toList());

        toDelete.forEach(eventId -> {
            WorkflowEvent event = eventStore.remove(eventId);
            if(event != null) {
                // Update indexes
                List<String> workflowEvents = workflowEventIndex.get(event.getWorkflowId());
                if(workflowEvents != null) {
                    workflowEvents.remove(eventId);
                }

                if(event.getFlowId() != null) {
                    List<String> flowEvents = flowEventIndex.get(event.getFlowId());
                    if(flowEvents != null) {
                        flowEvents.remove(eventId);
                    }
                }
            }
        });

        log.info("Deleted {} events before {}", toDelete.size(), beforeDate);
        return toDelete.size();
    }

    @Override
    public long countByWorkflowId(String workflowId) {
        List<String> eventIds = workflowEventIndex.get(workflowId);
        return eventIds != null ? eventIds.size() : 0;
    }

    /**
     * Clear all events(for testing purposes)
     */
    public void clearAll() {
        eventStore.clear();
        workflowEventIndex.clear();
        flowEventIndex.clear();
    }
}
