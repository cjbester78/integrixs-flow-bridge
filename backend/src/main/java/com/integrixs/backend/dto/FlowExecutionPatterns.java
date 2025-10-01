package com.integrixs.backend.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO representing flow execution patterns and analytics
 */
public class FlowExecutionPatterns {
    private String flowId;
    private String flowName;
    private LocalDateTime analysisStart;
    private LocalDateTime analysisEnd;

    // Execution patterns
    private Long totalExecutions;
    private Double executionsPerHour;
    private List<HourlyPattern> hourlyPatterns;
    private List<DailyPattern> dailyPatterns;
    private Map<String, Long> executionsByDayOfWeek;

    // Performance patterns
    private Long averageExecutionTime;
    private Long minExecutionTime;
    private Long maxExecutionTime;
    private Map<String, Long> executionTimeDistribution;

    // Success/failure patterns
    private Long successfulExecutions;
    private Long failedExecutions;
    private Double successRate;
    private Map<String, Long> failureReasons;
    private List<ErrorPattern> errorPatterns;

    // Peak usage
    private LocalDateTime peakUsageTime;
    private Long peakExecutionsPerHour;
    private List<PeakPeriod> peakPeriods;

    // Predictions
    private Long predictedNextHourExecutions;
    private Long predictedNextDayExecutions;
    private List<String> anomalies;

    public static class HourlyPattern {
        private Integer hour;
        private Long executionCount;
        private Double averageExecutionTime;
        private Double successRate;

        public Integer getHour() { return hour; }
        public void setHour(Integer hour) { this.hour = hour; }

        public Long getExecutionCount() { return executionCount; }
        public void setExecutionCount(Long executionCount) { this.executionCount = executionCount; }

        public Double getAverageExecutionTime() { return averageExecutionTime; }
        public void setAverageExecutionTime(Double averageExecutionTime) { this.averageExecutionTime = averageExecutionTime; }

        public Double getSuccessRate() { return successRate; }
        public void setSuccessRate(Double successRate) { this.successRate = successRate; }
    }

    public static class DailyPattern {
        private LocalDateTime date;
        private Long executionCount;
        private Double averageExecutionTime;
        private Double successRate;
        private Long errorCount;

        public LocalDateTime getDate() { return date; }
        public void setDate(LocalDateTime date) { this.date = date; }

        public Long getExecutionCount() { return executionCount; }
        public void setExecutionCount(Long executionCount) { this.executionCount = executionCount; }

        public Double getAverageExecutionTime() { return averageExecutionTime; }
        public void setAverageExecutionTime(Double averageExecutionTime) { this.averageExecutionTime = averageExecutionTime; }

        public Double getSuccessRate() { return successRate; }
        public void setSuccessRate(Double successRate) { this.successRate = successRate; }

        public Long getErrorCount() { return errorCount; }
        public void setErrorCount(Long errorCount) { this.errorCount = errorCount; }
    }

    public static class ErrorPattern {
        private String errorType;
        private Long occurrenceCount;
        private LocalDateTime firstOccurrence;
        private LocalDateTime lastOccurrence;
        private Double frequency;
        private String commonCause;

        public String getErrorType() { return errorType; }
        public void setErrorType(String errorType) { this.errorType = errorType; }

        public Long getOccurrenceCount() { return occurrenceCount; }
        public void setOccurrenceCount(Long occurrenceCount) { this.occurrenceCount = occurrenceCount; }

        public LocalDateTime getFirstOccurrence() { return firstOccurrence; }
        public void setFirstOccurrence(LocalDateTime firstOccurrence) { this.firstOccurrence = firstOccurrence; }

        public LocalDateTime getLastOccurrence() { return lastOccurrence; }
        public void setLastOccurrence(LocalDateTime lastOccurrence) { this.lastOccurrence = lastOccurrence; }

        public Double getFrequency() { return frequency; }
        public void setFrequency(Double frequency) { this.frequency = frequency; }

        public String getCommonCause() { return commonCause; }
        public void setCommonCause(String commonCause) { this.commonCause = commonCause; }
    }

    public static class PeakPeriod {
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Long executionCount;
        private String reason;

        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

        public Long getExecutionCount() { return executionCount; }
        public void setExecutionCount(Long executionCount) { this.executionCount = executionCount; }

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    // Default constructor
    public FlowExecutionPatterns() {
    }

    public String getFlowId() {
        return flowId;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    public String getFlowName() {
        return flowName;
    }

    public void setFlowName(String flowName) {
        this.flowName = flowName;
    }

    public LocalDateTime getAnalysisStart() {
        return analysisStart;
    }

    public void setAnalysisStart(LocalDateTime analysisStart) {
        this.analysisStart = analysisStart;
    }

    public LocalDateTime getAnalysisEnd() {
        return analysisEnd;
    }

    public void setAnalysisEnd(LocalDateTime analysisEnd) {
        this.analysisEnd = analysisEnd;
    }

    public Long getTotalExecutions() {
        return totalExecutions;
    }

    public void setTotalExecutions(Long totalExecutions) {
        this.totalExecutions = totalExecutions;
    }

    public Double getExecutionsPerHour() {
        return executionsPerHour;
    }

    public void setExecutionsPerHour(Double executionsPerHour) {
        this.executionsPerHour = executionsPerHour;
    }

    public List<HourlyPattern> getHourlyPatterns() {
        return hourlyPatterns;
    }

    public void setHourlyPatterns(List<HourlyPattern> hourlyPatterns) {
        this.hourlyPatterns = hourlyPatterns;
    }

    public List<DailyPattern> getDailyPatterns() {
        return dailyPatterns;
    }

    public void setDailyPatterns(List<DailyPattern> dailyPatterns) {
        this.dailyPatterns = dailyPatterns;
    }

    public Long getAverageExecutionTime() {
        return averageExecutionTime;
    }

    public void setAverageExecutionTime(Long averageExecutionTime) {
        this.averageExecutionTime = averageExecutionTime;
    }

    public Long getMinExecutionTime() {
        return minExecutionTime;
    }

    public void setMinExecutionTime(Long minExecutionTime) {
        this.minExecutionTime = minExecutionTime;
    }

    public Long getMaxExecutionTime() {
        return maxExecutionTime;
    }

    public void setMaxExecutionTime(Long maxExecutionTime) {
        this.maxExecutionTime = maxExecutionTime;
    }

    public Long getSuccessfulExecutions() {
        return successfulExecutions;
    }

    public void setSuccessfulExecutions(Long successfulExecutions) {
        this.successfulExecutions = successfulExecutions;
    }

    public Long getFailedExecutions() {
        return failedExecutions;
    }

    public void setFailedExecutions(Long failedExecutions) {
        this.failedExecutions = failedExecutions;
    }

    public Double getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(Double successRate) {
        this.successRate = successRate;
    }

    public List<ErrorPattern> getErrorPatterns() {
        return errorPatterns;
    }

    public void setErrorPatterns(List<ErrorPattern> errorPatterns) {
        this.errorPatterns = errorPatterns;
    }

    public LocalDateTime getPeakUsageTime() {
        return peakUsageTime;
    }

    public void setPeakUsageTime(LocalDateTime peakUsageTime) {
        this.peakUsageTime = peakUsageTime;
    }

    public Long getPeakExecutionsPerHour() {
        return peakExecutionsPerHour;
    }

    public void setPeakExecutionsPerHour(Long peakExecutionsPerHour) {
        this.peakExecutionsPerHour = peakExecutionsPerHour;
    }

    public List<PeakPeriod> getPeakPeriods() {
        return peakPeriods;
    }

    public void setPeakPeriods(List<PeakPeriod> peakPeriods) {
        this.peakPeriods = peakPeriods;
    }

    public Long getPredictedNextHourExecutions() {
        return predictedNextHourExecutions;
    }

    public void setPredictedNextHourExecutions(Long predictedNextHourExecutions) {
        this.predictedNextHourExecutions = predictedNextHourExecutions;
    }

    public Long getPredictedNextDayExecutions() {
        return predictedNextDayExecutions;
    }

    public void setPredictedNextDayExecutions(Long predictedNextDayExecutions) {
        this.predictedNextDayExecutions = predictedNextDayExecutions;
    }

    public List<String> getAnomalies() {
        return anomalies;
    }

    public void setAnomalies(List<String> anomalies) {
        this.anomalies = anomalies;
    }

    // Missing methods required by FlowExecutionHeatmapService
    public void setAnalysisPeriodDays(int days) {
        // This method is used to indicate the analysis period in days
        // The actual period is determined by analysisStart and analysisEnd
    }

    public Map<Integer, Long> getHourlyPattern() {
        // Convert hourlyPatterns list to a map for compatibility
        Map<Integer, Long> hourlyMap = new java.util.HashMap<>();
        if (hourlyPatterns != null) {
            for (HourlyPattern pattern : hourlyPatterns) {
                if (pattern.getHour() != null && pattern.getExecutionCount() != null) {
                    hourlyMap.put(pattern.getHour(), pattern.getExecutionCount());
                }
            }
        }
        return hourlyMap;
    }

    public void setHourlyPattern(Map<Integer, Long> hourlyPattern) {
        // Convert map to list of HourlyPattern objects
        this.hourlyPatterns = new java.util.ArrayList<>();
        if (hourlyPattern != null) {
            for (Map.Entry<Integer, Long> entry : hourlyPattern.entrySet()) {
                HourlyPattern pattern = new HourlyPattern();
                pattern.setHour(entry.getKey());
                pattern.setExecutionCount(entry.getValue());
                this.hourlyPatterns.add(pattern);
            }
        }
    }

    public Map<LocalDateTime, Long> getDailyPattern() {
        // Convert dailyPatterns list to a map for compatibility
        Map<LocalDateTime, Long> dailyMap = new java.util.HashMap<>();
        if (dailyPatterns != null) {
            for (DailyPattern pattern : dailyPatterns) {
                if (pattern.getDate() != null && pattern.getExecutionCount() != null) {
                    dailyMap.put(pattern.getDate(), pattern.getExecutionCount());
                }
            }
        }
        return dailyMap;
    }

    public void setDailyPattern(Map<LocalDateTime, Long> dailyPattern) {
        // Convert map to list of DailyPattern objects
        this.dailyPatterns = new java.util.ArrayList<>();
        if (dailyPattern != null) {
            for (Map.Entry<LocalDateTime, Long> entry : dailyPattern.entrySet()) {
                DailyPattern pattern = new DailyPattern();
                pattern.setDate(entry.getKey());
                pattern.setExecutionCount(entry.getValue());
                this.dailyPatterns.add(pattern);
            }
        }
    }

    public Map<String, Long> getWeeklyPattern() {
        // This is executionsByDayOfWeek
        return executionsByDayOfWeek;
    }

    public void setWeeklyPattern(Map<String, Long> weeklyPattern) {
        this.executionsByDayOfWeek = weeklyPattern;
    }

    public Map<String, Long> getExecutionsByDayOfWeek() {
        return executionsByDayOfWeek;
    }

    public void setExecutionsByDayOfWeek(Map<String, Long> executionsByDayOfWeek) {
        this.executionsByDayOfWeek = executionsByDayOfWeek;
    }

    public List<Map<String, Object>> getExecutionClusters() {
        // Return empty list for now - this needs proper implementation
        return new java.util.ArrayList<>();
    }

    public void setExecutionClusters(List<Map<String, Object>> clusters) {
        // Store clusters - this needs proper implementation
    }

    public Map<String, Double> getExecutionVelocity() {
        // Return empty map for now - this needs proper implementation
        return new java.util.HashMap<>();
    }

    public void setExecutionVelocity(Map<String, Double> velocity) {
        // Store velocity - this needs proper implementation
    }

    public List<Map<String, Object>> getAnomalousPatterns() {
        // Return empty list for now - this needs proper implementation
        return new java.util.ArrayList<>();
    }

    public void setAnomalousPatterns(List<Map<String, Object>> patterns) {
        // Store anomalous patterns - this needs proper implementation
    }

    public Map<String, Long> getExecutionTimeDistribution() {
        return executionTimeDistribution;
    }

    public void setExecutionTimeDistribution(Map<String, Long> executionTimeDistribution) {
        this.executionTimeDistribution = executionTimeDistribution;
    }

    public Map<String, Long> getFailureReasons() {
        return failureReasons;
    }

    public void setFailureReasons(Map<String, Long> failureReasons) {
        this.failureReasons = failureReasons;
    }

}