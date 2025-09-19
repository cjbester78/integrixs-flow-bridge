package com.integrixs.backend.api.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Response object for message statistics
 */
public class MessageStatsResponse {

    private long totalMessages;
    private long successfulMessages;
    private long failedMessages;
    private long pendingMessages;
    private long processingMessages;

    private Double avgExecutionTimeMs;
    private Long maxExecutionTimeMs;
    private Long minExecutionTimeMs;

    private Map<String, Long> messagesByStatus;
    private Map<String, Long> messagesByType;
    private Map<String, Long> messagesBySource;
    private Map<String, Long> messagesByTarget;

    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;

    private Long messagesPerHour;
    private Long messagesPerDay;

    private Map<String, Object> additionalStats;

    // Default constructor
    public MessageStatsResponse() {
    }

    public long getTotalMessages() {
        return totalMessages;
    }

    public void setTotalMessages(long totalMessages) {
        this.totalMessages = totalMessages;
    }

    public long getSuccessfulMessages() {
        return successfulMessages;
    }

    public void setSuccessfulMessages(long successfulMessages) {
        this.successfulMessages = successfulMessages;
    }

    public long getFailedMessages() {
        return failedMessages;
    }

    public void setFailedMessages(long failedMessages) {
        this.failedMessages = failedMessages;
    }

    public long getPendingMessages() {
        return pendingMessages;
    }

    public void setPendingMessages(long pendingMessages) {
        this.pendingMessages = pendingMessages;
    }

    public long getProcessingMessages() {
        return processingMessages;
    }

    public void setProcessingMessages(long processingMessages) {
        this.processingMessages = processingMessages;
    }

    public Double getAvgExecutionTimeMs() {
        return avgExecutionTimeMs;
    }

    public void setAvgExecutionTimeMs(Double avgExecutionTimeMs) {
        this.avgExecutionTimeMs = avgExecutionTimeMs;
    }

    public Long getMaxExecutionTimeMs() {
        return maxExecutionTimeMs;
    }

    public void setMaxExecutionTimeMs(Long maxExecutionTimeMs) {
        this.maxExecutionTimeMs = maxExecutionTimeMs;
    }

    public Long getMinExecutionTimeMs() {
        return minExecutionTimeMs;
    }

    public void setMinExecutionTimeMs(Long minExecutionTimeMs) {
        this.minExecutionTimeMs = minExecutionTimeMs;
    }

    public LocalDateTime getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(LocalDateTime periodStart) {
        this.periodStart = periodStart;
    }

    public LocalDateTime getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(LocalDateTime periodEnd) {
        this.periodEnd = periodEnd;
    }

    public Long getMessagesPerHour() {
        return messagesPerHour;
    }

    public void setMessagesPerHour(Long messagesPerHour) {
        this.messagesPerHour = messagesPerHour;
    }

    public Long getMessagesPerDay() {
        return messagesPerDay;
    }

    public void setMessagesPerDay(Long messagesPerDay) {
        this.messagesPerDay = messagesPerDay;
    }

    public Map<String, Long> getMessagesByStatus() {
        return messagesByStatus;
    }

    public void setMessagesByStatus(Map<String, Long> messagesByStatus) {
        this.messagesByStatus = messagesByStatus;
    }

    public Map<String, Long> getMessagesByType() {
        return messagesByType;
    }

    public void setMessagesByType(Map<String, Long> messagesByType) {
        this.messagesByType = messagesByType;
    }

    public Map<String, Long> getMessagesBySource() {
        return messagesBySource;
    }

    public void setMessagesBySource(Map<String, Long> messagesBySource) {
        this.messagesBySource = messagesBySource;
    }

    public Map<String, Long> getMessagesByTarget() {
        return messagesByTarget;
    }

    public void setMessagesByTarget(Map<String, Long> messagesByTarget) {
        this.messagesByTarget = messagesByTarget;
    }

    public Map<String, Object> getAdditionalStats() {
        return additionalStats;
    }

    public void setAdditionalStats(Map<String, Object> additionalStats) {
        this.additionalStats = additionalStats;
    }

    // Builder
    public static MessageStatsResponseBuilder builder() {
        return new MessageStatsResponseBuilder();
    }

    public static class MessageStatsResponseBuilder {
        private long totalMessages;
        private long successfulMessages;
        private long failedMessages;
        private long pendingMessages;
        private long processingMessages;
        private Double avgExecutionTimeMs;
        private Long maxExecutionTimeMs;
        private Long minExecutionTimeMs;
        private Map<String, Long> messagesByStatus;
        private Map<String, Long> messagesByType;
        private Map<String, Long> messagesBySource;
        private Map<String, Long> messagesByTarget;
        private LocalDateTime periodStart;
        private LocalDateTime periodEnd;
        private Long messagesPerHour;
        private Long messagesPerDay;
        private Map<String, Object> additionalStats;

        public MessageStatsResponseBuilder totalMessages(long totalMessages) {
            this.totalMessages = totalMessages;
            return this;
        }

        public MessageStatsResponseBuilder successfulMessages(long successfulMessages) {
            this.successfulMessages = successfulMessages;
            return this;
        }

        public MessageStatsResponseBuilder failedMessages(long failedMessages) {
            this.failedMessages = failedMessages;
            return this;
        }

        public MessageStatsResponseBuilder pendingMessages(long pendingMessages) {
            this.pendingMessages = pendingMessages;
            return this;
        }

        public MessageStatsResponseBuilder processingMessages(long processingMessages) {
            this.processingMessages = processingMessages;
            return this;
        }

        public MessageStatsResponseBuilder avgExecutionTimeMs(Double avgExecutionTimeMs) {
            this.avgExecutionTimeMs = avgExecutionTimeMs;
            return this;
        }

        public MessageStatsResponseBuilder maxExecutionTimeMs(Long maxExecutionTimeMs) {
            this.maxExecutionTimeMs = maxExecutionTimeMs;
            return this;
        }

        public MessageStatsResponseBuilder minExecutionTimeMs(Long minExecutionTimeMs) {
            this.minExecutionTimeMs = minExecutionTimeMs;
            return this;
        }

        public MessageStatsResponseBuilder messagesByStatus(Map<String, Long> messagesByStatus) {
            this.messagesByStatus = messagesByStatus;
            return this;
        }

        public MessageStatsResponseBuilder messagesByType(Map<String, Long> messagesByType) {
            this.messagesByType = messagesByType;
            return this;
        }

        public MessageStatsResponseBuilder messagesBySource(Map<String, Long> messagesBySource) {
            this.messagesBySource = messagesBySource;
            return this;
        }

        public MessageStatsResponseBuilder messagesByTarget(Map<String, Long> messagesByTarget) {
            this.messagesByTarget = messagesByTarget;
            return this;
        }

        public MessageStatsResponseBuilder periodStart(LocalDateTime periodStart) {
            this.periodStart = periodStart;
            return this;
        }

        public MessageStatsResponseBuilder periodEnd(LocalDateTime periodEnd) {
            this.periodEnd = periodEnd;
            return this;
        }

        public MessageStatsResponseBuilder messagesPerHour(Long messagesPerHour) {
            this.messagesPerHour = messagesPerHour;
            return this;
        }

        public MessageStatsResponseBuilder messagesPerDay(Long messagesPerDay) {
            this.messagesPerDay = messagesPerDay;
            return this;
        }

        public MessageStatsResponseBuilder additionalStats(Map<String, Object> additionalStats) {
            this.additionalStats = additionalStats;
            return this;
        }

        public MessageStatsResponse build() {
            MessageStatsResponse response = new MessageStatsResponse();
            response.totalMessages = this.totalMessages;
            response.successfulMessages = this.successfulMessages;
            response.failedMessages = this.failedMessages;
            response.pendingMessages = this.pendingMessages;
            response.processingMessages = this.processingMessages;
            response.avgExecutionTimeMs = this.avgExecutionTimeMs;
            response.maxExecutionTimeMs = this.maxExecutionTimeMs;
            response.minExecutionTimeMs = this.minExecutionTimeMs;
            response.messagesByStatus = this.messagesByStatus;
            response.messagesByType = this.messagesByType;
            response.messagesBySource = this.messagesBySource;
            response.messagesByTarget = this.messagesByTarget;
            response.periodStart = this.periodStart;
            response.periodEnd = this.periodEnd;
            response.messagesPerHour = this.messagesPerHour;
            response.messagesPerDay = this.messagesPerDay;
            response.additionalStats = this.additionalStats;
            return response;
        }
    }
}
