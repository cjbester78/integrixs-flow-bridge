package com.integrixs.backend.domain.service;

import com.integrixs.data.model.Message;
import com.integrixs.data.model.SystemLog;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Domain service for calculating message statistics
 */
@Service
public class MessageStatisticsService {

    /**
     * Calculates message counts by status
     */

    private static final Logger log = LoggerFactory.getLogger(MessageStatisticsService.class);

    public Map<String, Long> calculateMessagesByStatus(List<SystemLog> logs) {
        return logs.stream()
                .filter(log -> log.getDetails() != null && log.getDetails().contains("status"))
                .collect(Collectors.groupingBy(
                    log -> extractStatus(log.getDetails()),
                    Collectors.counting()
               ));
    }

    /**
     * Calculates message counts by type
     */
    public Map<String, Long> calculateMessagesByType(List<SystemLog> logs) {
        return logs.stream()
                .filter(log -> log.getDomainType() != null)
                .collect(Collectors.groupingBy(
                    SystemLog::getDomainType,
                    Collectors.counting()
               ));
    }

    /**
     * Calculates average execution time from logs
     */
    public Double calculateAverageExecutionTime(List<SystemLog> logs) {
        List<Long> executionTimes = extractExecutionTimes(logs);

        if(executionTimes.isEmpty()) {
            return 0.0;
        }

        return executionTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);
    }

    /**
     * Calculates message throughput
     */
    public Long calculateMessagesPerHour(List<SystemLog> logs, LocalDateTime from, LocalDateTime to) {
        if(logs.isEmpty() || from == null || to == null) {
            return 0L;
        }

        long hours = ChronoUnit.HOURS.between(from, to);
        if(hours == 0) hours = 1; // Avoid division by zero

        return(long) logs.size() / hours;
    }

    /**
     * Calculates message throughput per day
     */
    public Long calculateMessagesPerDay(List<SystemLog> logs, LocalDateTime from, LocalDateTime to) {
        if(logs.isEmpty() || from == null || to == null) {
            return 0L;
        }

        long days = ChronoUnit.DAYS.between(from, to);
        if(days == 0) days = 1; // Avoid division by zero

        return(long) logs.size() / days;
    }

    /**
     * Groups messages by source system
     */
    public Map<String, Long> calculateMessagesBySource(List<SystemLog> logs) {
        return logs.stream()
                .filter(log -> log.getDetails() != null && log.getDetails().contains("source"))
                .collect(Collectors.groupingBy(
                    log -> extractField(log.getDetails(), "source"),
                    Collectors.counting()
               ));
    }

    /**
     * Groups messages by target system
     */
    public Map<String, Long> calculateMessagesByTarget(List<SystemLog> logs) {
        return logs.stream()
                .filter(log -> log.getDetails() != null && log.getDetails().contains("target"))
                .collect(Collectors.groupingBy(
                    log -> extractField(log.getDetails(), "target"),
                    Collectors.counting()
               ));
    }

    /**
     * Calculates min, max execution times
     */
    public Map<String, Long> calculateExecutionTimeStats(List<SystemLog> logs) {
        List<Long> executionTimes = extractExecutionTimes(logs);

        Map<String, Long> stats = new HashMap<>();

        if(!executionTimes.isEmpty()) {
            stats.put("min", Collections.min(executionTimes));
            stats.put("max", Collections.max(executionTimes));
            stats.put("avg", (long) executionTimes.stream()
                    .mapToLong(Long::longValue)
                    .average()
                    .orElse(0.0));
        } else {
            stats.put("min", 0L);
            stats.put("max", 0L);
            stats.put("avg", 0L);
        }

        return stats;
    }

    private String extractStatus(String details) {
        // Simple extraction - in real implementation would use proper JSON parsing
        if(details.contains("COMPLETED")) return "COMPLETED";
        if(details.contains("FAILED")) return "FAILED";
        if(details.contains("PROCESSING")) return "PROCESSING";
        if(details.contains("PENDING")) return "PENDING";
        return "UNKNOWN";
    }

    private String extractField(String details, String fieldName) {
        // Simple extraction - in real implementation would use proper JSON parsing
        int index = details.indexOf(fieldName + ":");
        if(index == -1) return "UNKNOWN";

        int start = index + fieldName.length() + 1;
        int end = details.indexOf(",", start);
        if(end == -1) end = details.indexOf("}", start);
        if(end == -1) end = details.length();

        return details.substring(start, end).trim().replace("\"", "");
    }

    private List<Long> extractExecutionTimes(List<SystemLog> logs) {
        return logs.stream()
                .filter(log -> log.getDetails() != null && log.getDetails().contains("executionTime"))
                .map(log -> {
                    try {
                        String timeStr = extractField(log.getDetails(), "executionTime");
                        return Long.parseLong(timeStr.replaceAll("[^0-9]", ""));
                    } catch(Exception e) {
                        return 0L;
                    }
                })
                .filter(time -> time > 0)
                .collect(Collectors.toList());
    }
}
