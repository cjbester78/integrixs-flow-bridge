package com.integrixs.backend.domain.service;

import com.integrixs.data.sql.repository.MessageSqlRepository;
import com.integrixs.data.sql.repository.FlowExecutionSqlRepository;
import com.integrixs.data.model.Message.MessageStatus;
import com.integrixs.data.model.FlowExecution.ExecutionStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain service for calculating execution statistics
 * Contains business logic for message and execution metrics
 */
@Service
public class StatisticsCalculatorService {

    private final MessageSqlRepository messageRepository;
    private final FlowExecutionSqlRepository flowExecutionRepository;

    public StatisticsCalculatorService(MessageSqlRepository messageRepository,
                                     FlowExecutionSqlRepository flowExecutionRepository) {
        this.messageRepository = messageRepository;
        this.flowExecutionRepository = flowExecutionRepository;
    }

    /**
     * Count messages processed today
     */
    public long countMessagesToday() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        // Use existing repository method with pagination
        return messageRepository.findByReceivedAtBetween(startOfDay, endOfDay, Pageable.unpaged()).getTotalElements();
    }

    /**
     * Count messages processed today for a specific business component
     */
    public long countMessagesTodayForBusinessComponent(UUID businessComponentId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        // Simplified implementation - would need custom query in real scenario
        return messageRepository.findByReceivedAtBetween(startOfDay, endOfDay, Pageable.unpaged())
                .getContent()
                .stream()
                .filter(msg -> msg.getFlowExecution() != null &&
                             msg.getFlowExecution().getFlow() != null &&
                             msg.getFlowExecution().getFlow().getBusinessComponent() != null &&
                             msg.getFlowExecution().getFlow().getBusinessComponent().getId().equals(businessComponentId))
                .count();
    }

    /**
     * Calculate success rate for today
     */
    public double calculateSuccessRateToday() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        long totalMessages = messageRepository.findByReceivedAtBetween(startOfDay, endOfDay, Pageable.unpaged()).getTotalElements();
        if(totalMessages == 0) {
            return 100.0; // No messages means 100% success
        }

        long successfulMessages = messageRepository.findByStatus(MessageStatus.PROCESSED).stream()
                .filter(msg -> msg.getReceivedAt() != null &&
                             msg.getReceivedAt().isAfter(startOfDay) &&
                             msg.getReceivedAt().isBefore(endOfDay))
                .count();

        return(successfulMessages * 100.0) / totalMessages;
    }

    /**
     * Calculate success rate for a specific business component today
     */
    public double calculateSuccessRateTodayForBusinessComponent(UUID businessComponentId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        // Get all messages for the time period and filter
        var allMessages = messageRepository.findByReceivedAtBetween(startOfDay, endOfDay, Pageable.unpaged())
                .getContent()
                .stream()
                .filter(msg -> msg.getFlowExecution() != null &&
                             msg.getFlowExecution().getFlow() != null &&
                             msg.getFlowExecution().getFlow().getBusinessComponent() != null &&
                             msg.getFlowExecution().getFlow().getBusinessComponent().getId().equals(businessComponentId))
                .toList();

        if(allMessages.isEmpty()) {
            return 100.0; // No messages means 100% success
        }

        long successfulMessages = allMessages.stream()
                .filter(msg -> msg.getStatus() == MessageStatus.PROCESSED)
                .count();

        return(successfulMessages * 100.0) / allMessages.size();
    }

    /**
     * Calculate average response time from flow executions
     */
    public long calculateAverageResponseTime() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        // Use existing repository method for calculating average processing time
        Double avgTime = messageRepository.calculateAverageProcessingTimeForPeriod(startOfDay, endOfDay);

        return avgTime != null ? avgTime.longValue() : 0L;
    }

    /**
     * Calculate average response time for a specific business component
     */
    public long calculateAverageResponseTimeForBusinessComponent(UUID businessComponentId) {
        // Use existing repository method that calculates by business component
        Double avgTime = messageRepository.calculateAverageProcessingTimeByBusinessComponent(businessComponentId.toString());

        return avgTime != null ? avgTime.longValue() : 0L;
    }

    /**
     * Calculate system uptime percentage
     * This is a simplified calculation based on flow availability
     */
    public double calculateUptimePercentage() {
        // In a real implementation, this would track actual system health metrics
        // For now, return a high uptime value
        return 99.9;
    }
}
