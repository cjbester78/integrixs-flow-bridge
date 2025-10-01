package com.integrixs.backend.domain.service;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Domain service for calculating execution metrics
 */
@Service
public class ExecutionMetricsCalculator {

    /**
     * Calculates the duration in milliseconds between start and end time
     */
    public long calculateDuration(LocalDateTime startTime, LocalDateTime endTime) {
        if(startTime == null || endTime == null) {
            return 0L;
        }
        return Duration.between(startTime, endTime).toMillis();
    }

    /**
     * Calculates average execution time from a list of durations
     */
    public double calculateAverageTime(List<Long> executionTimes) {
        if(executionTimes == null || executionTimes.isEmpty()) {
            return 0.0;
        }

        return executionTimes.stream()
            .mapToLong(Long::longValue)
            .average()
            .orElse(0.0);
    }

    /**
     * Calculates success rate percentage
     */
    public double calculateSuccessRate(int successfulExecutions, int totalExecutions) {
        if(totalExecutions == 0) {
            return 0.0;
        }
        return(double) successfulExecutions / totalExecutions * 100;
    }

    /**
     * Checks if execution is considered long - running
     */
    public boolean isLongRunning(LocalDateTime startTime, int thresholdMinutes) {
        if(startTime == null) {
            return false;
        }

        Duration duration = Duration.between(startTime, LocalDateTime.now());
        return duration.toMinutes() > thresholdMinutes;
    }

    /**
     * Updates running average with new value
     */
    public double updateRunningAverage(double currentAverage, int currentCount, long newValue) {
        if(currentCount == 0) {
            return newValue;
        }

        double totalTime = currentAverage * currentCount + newValue;
        return totalTime / (currentCount + 1);
    }
}
