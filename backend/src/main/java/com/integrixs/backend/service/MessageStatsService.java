package com.integrixs.backend.service;

import com.integrixs.data.model.Message;
import com.integrixs.data.sql.repository.MessageSqlRepository;
import com.integrixs.shared.dto.MessageStatsDTO;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service for message statistics
 */
public abstract class MessageStatsService {

    protected final MessageSqlRepository messageRepository;

    protected MessageStatsService(MessageSqlRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public MessageStatsDTO getMessageStats(Map<String, String> filters) {
        MessageStatsDTO stats = new MessageStatsDTO();

        // Count messages by status
        long total = messageRepository.count();
        long processing = countByStatus(Message.MessageStatus.PROCESSING);
        long successful = countByStatuses(Arrays.asList(
            Message.MessageStatus.PROCESSED,
            Message.MessageStatus.COMPLETED
       ));
        long failed = countByStatuses(Arrays.asList(
            Message.MessageStatus.FAILED,
            Message.MessageStatus.DEAD_LETTER
       ));

        // Calculate success rate
        long totalCompleted = successful + failed;
        double successRate = totalCompleted > 0 ? (successful * 100.0 / totalCompleted) : 0;

        // Calculate average processing time
        double avgProcessingTime = calculateAverageProcessingTime(filters);

        stats.setTotal(total);
        stats.setProcessing(processing);
        stats.setSuccessful(successful);
        stats.setFailed(failed);
        stats.setSuccessRate(successRate);
        stats.setAvgProcessingTime(avgProcessingTime);

        return stats;
    }

    /**
     * Count messages by specific status
     */
    private long countByStatus(Message.MessageStatus status) {
        return messageRepository.findByStatus(status).size();
    }

    /**
     * Count messages by multiple statuses
     */
    private long countByStatuses(List<Message.MessageStatus> statuses) {
        return statuses.stream()
            .mapToLong(this::countByStatus)
            .sum();
    }

    /**
     * Calculate average processing time for completed messages
     */
    private double calculateAverageProcessingTime(Map<String, String> filters) {
        List<Message> completedMessages;

        // Apply filters if provided
        if(filters != null && filters.containsKey("flowId")) {
            UUID flowId = UUID.fromString(filters.get("flowId"));
            completedMessages = messageRepository.findByStatusInOrderByPriorityDescCreatedAtAsc(
                Arrays.asList(
                    Message.MessageStatus.PROCESSED,
                    Message.MessageStatus.COMPLETED
               )
           ).stream()
            .filter(m -> m.getFlow() != null && m.getFlow().getId().equals(flowId))
            .toList();
        } else {
            completedMessages = messageRepository.findByStatusInOrderByPriorityDescCreatedAtAsc(
                Arrays.asList(
                    Message.MessageStatus.PROCESSED,
                    Message.MessageStatus.COMPLETED
               )
           );
        }

        if(completedMessages.isEmpty()) {
            return 0;
        }

        // Calculate processing times
        double totalProcessingTime = completedMessages.stream()
            .filter(m -> m.getReceivedAt() != null &&
                       (m.getCompletedAt() != null || m.getProcessedAt() != null))
            .mapToDouble(m -> {
                LocalDateTime startTime = m.getReceivedAt();
                LocalDateTime endTime = m.getCompletedAt() != null ?
                    m.getCompletedAt() : m.getProcessedAt();

                if(endTime != null) {
                    Duration duration = Duration.between(startTime, endTime);
                    return duration.toMillis();
                }
                return 0;
            })
            .sum();

        long countWithTimes = completedMessages.stream()
            .filter(m -> m.getReceivedAt() != null &&
                       (m.getCompletedAt() != null || m.getProcessedAt() != null))
            .count();

        return countWithTimes > 0 ? totalProcessingTime / countWithTimes : 0;
    }

    /**
     * Get statistics for a specific flow
     */
    public MessageStatsDTO getFlowStats(UUID flowId) {
        Map<String, String> filters = Map.of("flowId", flowId.toString());
        return getMessageStats(filters);
    }

    /**
     * Get statistics for a specific time period
     */
    public MessageStatsDTO getStatsForPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        MessageStatsDTO stats = new MessageStatsDTO();

        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
        List<Message> messages = messageRepository.findByReceivedAtBetween(
            startDate, endDate, pageable).getContent();

        long total = messages.size();
        long processing = messages.stream()
            .filter(m -> m.getStatus() == Message.MessageStatus.PROCESSING)
            .count();
        long successful = messages.stream()
            .filter(m -> m.getStatus() == Message.MessageStatus.PROCESSED ||
                        m.getStatus() == Message.MessageStatus.COMPLETED)
            .count();
        long failed = messages.stream()
            .filter(m -> m.getStatus() == Message.MessageStatus.FAILED ||
                        m.getStatus() == Message.MessageStatus.DEAD_LETTER)
            .count();

        long totalCompleted = successful + failed;
        double successRate = totalCompleted > 0 ? (successful * 100.0 / totalCompleted) : 0;

        // Calculate average processing time for messages in period
        double avgProcessingTime = messages.stream()
            .filter(m ->(m.getStatus() == Message.MessageStatus.PROCESSED ||
                         m.getStatus() == Message.MessageStatus.COMPLETED) &&
                        m.getReceivedAt() != null &&
                        (m.getCompletedAt() != null || m.getProcessedAt() != null))
            .mapToDouble(m -> {
                LocalDateTime endTime = m.getCompletedAt() != null ?
                    m.getCompletedAt() : m.getProcessedAt();
                Duration duration = Duration.between(m.getReceivedAt(), endTime);
                return duration.toMillis();
            })
            .average()
            .orElse(0);

        stats.setTotal(total);
        stats.setProcessing(processing);
        stats.setSuccessful(successful);
        stats.setFailed(failed);
        stats.setSuccessRate(successRate);
        stats.setAvgProcessingTime(avgProcessingTime);

        return stats;
    }
}
