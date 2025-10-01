package com.integrixs.backend.service;

import com.integrixs.data.model.SystemLog;
import com.integrixs.data.sql.repository.SystemLogSqlRepository;
import com.integrixs.shared.dto.log.CorrelatedLogGroup;
import com.integrixs.shared.dto.log.FlowExecutionTimeline;
import com.integrixs.shared.dto.system.SystemLogDTO;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.Comparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for correlating logs across different components and flows.
 */
@Service
public class LogCorrelationService {

    private static final Logger log = LoggerFactory.getLogger(LogCorrelationService.class);

    private final SystemLogSqlRepository systemLogRepository;

    public LogCorrelationService(SystemLogSqlRepository systemLogRepository) {
        this.systemLogRepository = systemLogRepository;
    }

    // Patterns for extracting IDs from log messages
    private static final Pattern FLOW_ID_PATTERN = Pattern.compile("flow:\\s*([a - zA - Z0-9 - ] + )");
    private static final Pattern MESSAGE_ID_PATTERN = Pattern.compile("messageId:\\s*([a - zA - Z0-9 - ] + )");
    private static final Pattern ADAPTER_ID_PATTERN = Pattern.compile("adapter:\\s*([a - zA - Z0-9 - ] + )");
    private static final Pattern COMPONENT_ID_PATTERN = Pattern.compile("componentId:\\s*([a - zA - Z0-9 - ] + )");

    /**
     * Get all logs correlated by correlation ID with timeline view.
     */
    public CorrelatedLogGroup getCorrelatedLogs(String correlationId) {
        List<SystemLog> logs = systemLogRepository.findByCorrelationId(correlationId);

        if(logs.isEmpty()) {
            return null;
        }

        // Sort by timestamp
        logs.sort(Comparator.comparing(SystemLog::getTimestamp));

        // Convert entities to DTOs
        List<SystemLogDTO> logDTOs = convertToDTO(logs);

        // Build correlated group
        CorrelatedLogGroup group = new CorrelatedLogGroup();
        group.setCorrelationId(correlationId);
        group.setLogs(logDTOs);
        group.setStartTime(logs.get(0).getTimestamp());
        group.setEndTime(logs.get(logs.size() - 1).getTimestamp());
        group.setDuration(Duration.between(group.getStartTime(), group.getEndTime()));

        // Extract flow information
        extractFlowInfo(group, logs);

        // Build component timeline
        group.setComponentTimeline(buildComponentTimeline(logs));

        // Identify error chain
        group.setErrorChain(buildErrorChain(logs));

        // Calculate statistics
        group.setStatistics(calculateStatistics(logs));

        return group;
    }

    /**
     * Get flow execution timeline.
     */
    public FlowExecutionTimeline getFlowExecutionTimeline(String flowId,
                                                         LocalDateTime startTime,
                                                         LocalDateTime endTime) {
        // Find all logs related to this flow execution
        List<SystemLog> logs = systemLogRepository.findByFlowIdAndTimestampBetween(flowId, startTime, endTime);

        if(logs.isEmpty()) {
            return null;
        }

        FlowExecutionTimeline timeline = new FlowExecutionTimeline();
        timeline.setFlowId(flowId);
        timeline.setStartTime(startTime);
        timeline.setEndTime(endTime);

        // Group logs by component/adapter
        Map<String, List<SystemLog>> componentLogs = groupLogsByComponent(logs);

        // Build execution steps
        List<FlowExecutionTimeline.ExecutionStep> steps = new ArrayList<>();

        for(Map.Entry<String, List<SystemLog>> entry : componentLogs.entrySet()) {
            String component = entry.getKey();
            List<SystemLog> componentLogList = entry.getValue();

            FlowExecutionTimeline.ExecutionStep step = new FlowExecutionTimeline.ExecutionStep();
            step.setComponent(component);
            step.setStartTime(componentLogList.get(0).getTimestamp());
            step.setEndTime(componentLogList.get(componentLogList.size() - 1).getTimestamp());
            step.setDuration(Duration.between(step.getStartTime(), step.getEndTime()));
            step.setLogs(convertToDTO(componentLogList));

            // Determine step status
            boolean hasError = componentLogList.stream()
                .anyMatch(log -> log.getLevel() == SystemLog.LogLevel.ERROR);
            step.setStatus(hasError ? "ERROR" : "SUCCESS");

            // Extract key events
            step.setKeyEvents(extractKeyEvents(componentLogList));

            steps.add(step);
        }

        timeline.setExecutionSteps(steps);

        // Calculate total duration
        timeline.setTotalDuration(Duration.between(
            timeline.getStartTime(),
            timeline.getEndTime()
       ));

        // Identify bottlenecks
        timeline.setBottlenecks(identifyBottlenecks(steps));

        // Build execution graph
        timeline.setExecutionGraph(buildExecutionGraph(steps, logs));

        return timeline;
    }

    /**
     * Find related logs by analyzing content similarity.
     */
    public List<CorrelatedLogGroup> findRelatedLogs(String logId, int maxGroups) {
        Optional<SystemLog> logOpt = systemLogRepository.findById(UUID.fromString(logId));
        if(!logOpt.isPresent()) {
            return Collections.emptyList();
        }

        SystemLog baseLog = logOpt.get();
        List<CorrelatedLogGroup> relatedGroups = new ArrayList<>();

        // 1. Find by correlation ID if present
        if(baseLog.getCorrelationId() != null) {
            CorrelatedLogGroup correlatedGroup = getCorrelatedLogs(baseLog.getCorrelationId());
            if(correlatedGroup != null) {
                relatedGroups.add(correlatedGroup);
            }
        }

        // 2. Find by extracted IDs
        Set<String> extractedIds = extractIds(baseLog.getMessage());

        for(String id : extractedIds) {
            // Search for logs containing this ID
            List<SystemLog> relatedLogs = findLogsByExtractedId(id, baseLog.getTimestamp());

            if(!relatedLogs.isEmpty()) {
                CorrelatedLogGroup group = new CorrelatedLogGroup();
                group.setCorrelationId("extracted-" + id);
                group.setLogs(convertToDTO(relatedLogs));
                group.setRelationType("ID_MATCH");
                relatedGroups.add(group);
            }

            if(relatedGroups.size() >= maxGroups) {
                break;
            }
        }

        // 3. Find by time proximity and similar patterns
        if(relatedGroups.size() < maxGroups) {
            List<SystemLog> proximityLogs = findLogsByTimeProximity(
                baseLog.getTimestamp(),
                baseLog.getSource(),
                baseLog.getCategory()
           );

            if(!proximityLogs.isEmpty()) {
                CorrelatedLogGroup group = new CorrelatedLogGroup();
                group.setCorrelationId("proximity-" + baseLog.getId());
                group.setLogs(convertToDTO(proximityLogs));
                group.setRelationType("TIME_PROXIMITY");
                relatedGroups.add(group);
            }
        }

        return relatedGroups.stream()
            .limit(maxGroups)
            .collect(Collectors.toList());
    }

    /**
     * Correlate logs across multiple flows.
     */
    public Map<String, List<SystemLog>> correlateAcrossFlows(List<String> flowIds,
                                                            LocalDateTime startTime,
                                                            LocalDateTime endTime) {
        Map<String, List<SystemLog>> correlatedLogs = new ConcurrentHashMap<>();

        // Find all logs for the given flows
        for(String flowId : flowIds) {
            List<SystemLog> flowLogs = systemLogRepository.findByFlowIdAndTimestampBetween(flowId, startTime, endTime);
            correlatedLogs.put(flowId, flowLogs);
        }

        // Find cross - flow correlations
        Map<String, Set<String>> crossFlowCorrelations = new HashMap<>();

        for(Map.Entry<String, List<SystemLog>> entry : correlatedLogs.entrySet()) {
            String flowId = entry.getKey();
            List<SystemLog> logs = entry.getValue();

            for(SystemLog log : logs) {
                // Extract message IDs that might appear in other flows
                Set<String> messageIds = extractMessageIds(log.getMessage());

                for(String messageId : messageIds) {
                    // Check if this message ID appears in other flows
                    for(Map.Entry<String, List<SystemLog>> otherEntry : correlatedLogs.entrySet()) {
                        if(!otherEntry.getKey().equals(flowId)) {
                            boolean found = otherEntry.getValue().stream()
                                .anyMatch(l -> l.getMessage().contains(messageId));

                            if(found) {
                                crossFlowCorrelations
                                    .computeIfAbsent(messageId, k -> new HashSet<>())
                                    .add(flowId);
                                crossFlowCorrelations.get(messageId).add(otherEntry.getKey());
                            }
                        }
                    }
                }
            }
        }

        // Add cross - flow correlation information to logs
        for(Map.Entry<String, Set<String>> correlation : crossFlowCorrelations.entrySet()) {
            log.info("Found cross - flow correlation for message {} across flows: {}",
                correlation.getKey(), correlation.getValue());
        }

        return correlatedLogs;
    }

    /**
     * Extract flow information from logs.
     */
    private void extractFlowInfo(CorrelatedLogGroup group, List<SystemLog> logs) {
        for(SystemLog log : logs) {
            Matcher flowMatcher = FLOW_ID_PATTERN.matcher(log.getMessage());
            if(flowMatcher.find()) {
                group.setFlowId(flowMatcher.group(1));
                break;
            }
        }

        // Extract unique components
        Set<String> components = logs.stream()
            .map(SystemLog::getSource)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        group.setInvolvedComponents(new ArrayList<>(components));
    }

    /**
     * Build component timeline.
     */
    private Map<String, List<CorrelatedLogGroup.TimelineEntry>> buildComponentTimeline(List<SystemLog> logs) {
        Map<String, List<CorrelatedLogGroup.TimelineEntry>> timeline = new HashMap<>();

        for(SystemLog log : logs) {
            String component = log.getSource() != null ? log.getSource() : "UNKNOWN";

            CorrelatedLogGroup.TimelineEntry entry = new CorrelatedLogGroup.TimelineEntry();
            entry.setTimestamp(log.getTimestamp());
            entry.setLevel(log.getLevel().name());
            entry.setMessage(log.getMessage());
            entry.setCategory(log.getCategory());

            timeline.computeIfAbsent(component, k -> new ArrayList<>()).add(entry);
        }

        return timeline;
    }

    /**
     * Build error chain for debugging.
     */
    private List<CorrelatedLogGroup.ErrorChainEntry> buildErrorChain(List<SystemLog> logs) {
        List<CorrelatedLogGroup.ErrorChainEntry> errorChain = new ArrayList<>();

        SystemLog previousError = null;
        for(SystemLog log : logs) {
            if(log.getLevel() == SystemLog.LogLevel.ERROR ||
                log.getLevel() == SystemLog.LogLevel.WARN) {

                CorrelatedLogGroup.ErrorChainEntry entry = new CorrelatedLogGroup.ErrorChainEntry();
                entry.setTimestamp(log.getTimestamp());
                entry.setComponent(log.getSource());
                entry.setMessage(log.getMessage());
                entry.setStackTrace(log.getStackTrace());

                if(previousError != null) {
                    entry.setPropagationDelay(
                        Duration.between(previousError.getTimestamp(), log.getTimestamp())
                   );
                }

                errorChain.add(entry);
                previousError = log;
            }
        }

        return errorChain;
    }

    /**
     * Calculate statistics for correlated logs.
     */
    private Map<String, Object> calculateStatistics(List<SystemLog> logs) {
        Map<String, Object> stats = new HashMap<>();

        // Count by level
        Map<SystemLog.LogLevel, Long> levelCounts = logs.stream()
            .collect(Collectors.groupingBy(SystemLog::getLevel, Collectors.counting()));
        stats.put("levelCounts", levelCounts);

        // Count by component
        Map<String, Long> componentCounts = logs.stream()
            .filter(log -> log.getSource() != null)
            .collect(Collectors.groupingBy(SystemLog::getSource, Collectors.counting()));
        stats.put("componentCounts", componentCounts);

        // Total logs
        stats.put("totalLogs", logs.size());

        // Error rate
        long errorCount = levelCounts.getOrDefault(SystemLog.LogLevel.ERROR, 0L);
        double errorRate = logs.isEmpty() ? 0 : (double) errorCount / logs.size() * 100;
        stats.put("errorRate", errorRate);

        return stats;
    }

    /**
     * Group logs by component.
     */
    private Map<String, List<SystemLog>> groupLogsByComponent(List<SystemLog> logs) {
        return logs.stream()
            .filter(log -> log.getSource() != null)
            .collect(Collectors.groupingBy(SystemLog::getSource));
    }

    /**
     * Extract key events from logs.
     */
    private List<String> extractKeyEvents(List<SystemLog> logs) {
        return logs.stream()
            .filter(log -> log.getLevel() == SystemLog.LogLevel.INFO ||
                          log.getLevel() == SystemLog.LogLevel.WARN ||
                          log.getLevel() == SystemLog.LogLevel.ERROR)
            .filter(log -> isKeyEvent(log.getMessage()))
            .map(SystemLog::getMessage)
            .collect(Collectors.toList());
    }

    /**
     * Check if a log message represents a key event.
     */
    private boolean isKeyEvent(String message) {
        String[] keyPhrases = {
            "started", "completed", "failed", "error", "success",
            "sending", "received", "processing", "transformed",
            "authenticated", "authorized", "connected", "disconnected"
        };

        String lowerMessage = message.toLowerCase();
        return Arrays.stream(keyPhrases).anyMatch(lowerMessage::contains);
    }

    /**
     * Identify bottlenecks in execution steps.
     */
    private List<FlowExecutionTimeline.Bottleneck> identifyBottlenecks(
            List<FlowExecutionTimeline.ExecutionStep> steps) {

        List<FlowExecutionTimeline.Bottleneck> bottlenecks = new ArrayList<>();

        // Calculate average duration
        double avgDuration = steps.stream()
            .mapToLong(step -> step.getDuration().toMillis())
            .average()
            .orElse(0);

        // Identify steps that take significantly longer than average
        for(FlowExecutionTimeline.ExecutionStep step : steps) {
            long stepDuration = step.getDuration().toMillis();

            if(stepDuration > avgDuration * 2) {
                FlowExecutionTimeline.Bottleneck bottleneck = new FlowExecutionTimeline.Bottleneck();
                bottleneck.setComponent(step.getComponent());
                bottleneck.setDuration(step.getDuration());
                bottleneck.setSeverity(calculateBottleneckSeverity(stepDuration, avgDuration));
                bottleneck.setImpact(String.format("%.1fx average duration",
                    stepDuration / avgDuration));

                bottlenecks.add(bottleneck);
            }
        }

        return bottlenecks;
    }

    /**
     * Calculate bottleneck severity.
     */
    private String calculateBottleneckSeverity(long duration, double avgDuration) {
        double ratio = duration / avgDuration;

        if(ratio > 5) return "CRITICAL";
        if(ratio > 3) return "HIGH";
        if(ratio > 2) return "MEDIUM";
        return "LOW";
    }

    /**
     * Build execution graph.
     */
    private Map<String, Object> buildExecutionGraph(
            List<FlowExecutionTimeline.ExecutionStep> steps,
            List<SystemLog> logs) {

        Map<String, Object> graph = new HashMap<>();

        // Nodes(components)
        List<Map<String, Object>> nodes = steps.stream()
            .map(step -> {
                Map<String, Object> node = new HashMap<>();
                node.put("id", step.getComponent());
                node.put("label", step.getComponent());
                node.put("duration", step.getDuration().toMillis());
                node.put("status", step.getStatus());
                return node;
            })
            .collect(Collectors.toList());

        graph.put("nodes", nodes);

        // Edges(connections between components based on log sequence)
        List<Map<String, Object>> edges = new ArrayList<>();

        for(int i = 0; i < steps.size() - 1; i++) {
            Map<String, Object> edge = new HashMap<>();
            edge.put("from", steps.get(i).getComponent());
            edge.put("to", steps.get(i + 1).getComponent());
            edge.put("label", "flow");
            edges.add(edge);
        }

        graph.put("edges", edges);

        return graph;
    }

    /**
     * Extract IDs from log message.
     */
    private Set<String> extractIds(String message) {
        Set<String> ids = new HashSet<>();

        extractPattern(message, FLOW_ID_PATTERN, ids);
        extractPattern(message, MESSAGE_ID_PATTERN, ids);
        extractPattern(message, ADAPTER_ID_PATTERN, ids);
        extractPattern(message, COMPONENT_ID_PATTERN, ids);

        return ids;
    }

    /**
     * Extract pattern matches.
     */
    private void extractPattern(String message, Pattern pattern, Set<String> results) {
        Matcher matcher = pattern.matcher(message);
        while(matcher.find()) {
            results.add(matcher.group(1));
        }
    }

    /**
     * Extract message IDs.
     */
    private Set<String> extractMessageIds(String message) {
        Set<String> ids = new HashSet<>();
        Matcher matcher = MESSAGE_ID_PATTERN.matcher(message);
        while(matcher.find()) {
            ids.add(matcher.group(1));
        }
        return ids;
    }

    /**
     * Find logs by extracted ID.
     */
    private List<SystemLog> findLogsByExtractedId(String id, LocalDateTime baseTime) {
        // Search within a time window around the base time
        LocalDateTime startTime = baseTime.minusMinutes(5);
        LocalDateTime endTime = baseTime.plusMinutes(5);

        return systemLogRepository.findByMessageContainingAndTimestampBetween(id, startTime, endTime);
    }

    /**
     * Find logs by time proximity.
     */
    private List<SystemLog> findLogsByTimeProximity(LocalDateTime baseTime,
                                                   String source,
                                                   String category) {
        LocalDateTime startTime = baseTime.minusSeconds(10);
        LocalDateTime endTime = baseTime.plusSeconds(10);

        return systemLogRepository.findByTimestampBetween(startTime, endTime, source, category);
    }

    /**
     * Convert SystemLog entities to SystemLogDTO objects.
     */
    private List<SystemLogDTO> convertToDTO(List<SystemLog> logs) {
        return logs.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Convert a single SystemLog entity to SystemLogDTO.
     */
    private SystemLogDTO convertToDTO(SystemLog log) {
        SystemLogDTO dto = new SystemLogDTO();
        dto.setId(log.getId() != null ? log.getId().toString() : null);
        dto.setTimestamp(log.getTimestamp());
        dto.setLevel(log.getLevel() != null ? log.getLevel().name() : null);
        dto.setMessage(log.getMessage());
        dto.setDetails(log.getDetails());
        dto.setSource(log.getSource());
        dto.setSourceId(log.getSourceId());
        dto.setSourceName(log.getSourceName());
        dto.setComponent(log.getComponent());
        dto.setComponentId(log.getComponentId());
        dto.setDomainType(log.getDomainType());
        dto.setDomainReferenceId(log.getDomainReferenceId());
        dto.setUserId(log.getUserId() != null ? log.getUserId().toString() : null);
        dto.setCreatedAt(log.getCreatedAt());
        dto.setCorrelationId(log.getCorrelationId());
        dto.setClientIp(log.getIpAddress());
        return dto;
    }
}
