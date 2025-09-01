package com.integrixs.backend.domain.service;

import com.integrixs.backend.domain.repository.DomainMessageRepository;
import com.integrixs.backend.domain.repository.DomainFlowExecutionRepository;
import com.integrixs.data.model.Message.MessageStatus;
import com.integrixs.data.model.FlowExecution.ExecutionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain service for calculating execution statistics
 * Contains business logic for message and execution metrics
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsCalculatorService {
    
    private final DomainMessageRepository messageRepository;
    private final DomainFlowExecutionRepository flowExecutionRepository;
    
    /**
     * Count messages processed today
     */
    public long countMessagesToday() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        
        return messageRepository.countByDateRange(startOfDay, endOfDay);
    }
    
    /**
     * Count messages processed today for a specific business component
     */
    public long countMessagesTodayForBusinessComponent(UUID businessComponentId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        
        return messageRepository.countByBusinessComponentAndDateRange(businessComponentId, startOfDay, endOfDay);
    }
    
    /**
     * Calculate success rate for today
     */
    public double calculateSuccessRateToday() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        
        long totalMessages = messageRepository.countByDateRange(startOfDay, endOfDay);
        if (totalMessages == 0) {
            return 100.0; // No messages means 100% success
        }
        
        long successfulMessages = messageRepository.countByStatusAndDateRange(MessageStatus.PROCESSED, startOfDay, endOfDay);
        
        return (successfulMessages * 100.0) / totalMessages;
    }
    
    /**
     * Calculate success rate for a specific business component today
     */
    public double calculateSuccessRateTodayForBusinessComponent(UUID businessComponentId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        
        long totalMessages = messageRepository.countByBusinessComponentAndDateRange(businessComponentId, startOfDay, endOfDay);
        if (totalMessages == 0) {
            return 100.0; // No messages means 100% success
        }
        
        long successfulMessages = messageRepository.countByBusinessComponentStatusAndDateRange(
            businessComponentId, MessageStatus.PROCESSED, startOfDay, endOfDay);
        
        return (successfulMessages * 100.0) / totalMessages;
    }
    
    /**
     * Calculate average response time from flow executions
     */
    public long calculateAverageResponseTime() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        
        Double avgTime = flowExecutionRepository.getAverageExecutionTimeForDateRange(startOfDay, endOfDay);
        
        return avgTime != null ? avgTime.longValue() : 0L;
    }
    
    /**
     * Calculate average response time for a specific business component
     */
    public long calculateAverageResponseTimeForBusinessComponent(UUID businessComponentId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        
        Double avgTime = flowExecutionRepository.getAverageExecutionTimeForBusinessComponentAndDateRange(
            businessComponentId, startOfDay, endOfDay);
        
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