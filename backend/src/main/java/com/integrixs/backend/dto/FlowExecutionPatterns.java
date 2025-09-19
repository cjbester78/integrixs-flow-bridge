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
    }
    
                    public static class DailyPattern {
        private LocalDateTime date;
        private Long executionCount;
        private Double averageExecutionTime;
        private Double successRate;
        private Long errorCount;
    }
    
                    public static class ErrorPattern {
        private String errorType;
        private Long occurrenceCount;
        private LocalDateTime firstOccurrence;
        private LocalDateTime lastOccurrence;
        private Double frequency;
        private String commonCause;
    }
    
                    public static class PeakPeriod {
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Long executionCount;
        private String reason;
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

    public Integer getHour() {
        return hour;
    }

    public void setHour(Integer hour) {
        this.hour = hour;
    }

    public Long getExecutionCount() {
        return executionCount;
    }

    public void setExecutionCount(Long executionCount) {
        this.executionCount = executionCount;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Long getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(Long errorCount) {
        this.errorCount = errorCount;
    }

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    public Long getOccurrenceCount() {
        return occurrenceCount;
    }

    public void setOccurrenceCount(Long occurrenceCount) {
        this.occurrenceCount = occurrenceCount;
    }

    public LocalDateTime getFirstOccurrence() {
        return firstOccurrence;
    }

    public void setFirstOccurrence(LocalDateTime firstOccurrence) {
        this.firstOccurrence = firstOccurrence;
    }

    public LocalDateTime getLastOccurrence() {
        return lastOccurrence;
    }

    public void setLastOccurrence(LocalDateTime lastOccurrence) {
        this.lastOccurrence = lastOccurrence;
    }

    public Double getFrequency() {
        return frequency;
    }

    public void setFrequency(Double frequency) {
        this.frequency = frequency;
    }

    public String getCommonCause() {
        return commonCause;
    }

    public void setCommonCause(String commonCause) {
        this.commonCause = commonCause;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}