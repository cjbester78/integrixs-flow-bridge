package com.integrixs.backend.service;

import com.integrixs.data.model.Message;
import com.integrixs.data.sql.repository.MessageSqlRepository;
import com.integrixs.shared.dto.MessageStatsDTO;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

/**
 * Optimized Message Statistics Service using native queries for better performance
 */
@Service
@Primary // Make this the primary implementation
public class OptimizedMessageStatsService extends MessageStatsService {

    private static final Logger log = LoggerFactory.getLogger(OptimizedMessageStatsService.class);

    public OptimizedMessageStatsService(MessageSqlRepository messageRepository) {
        super(messageRepository);
    }

    @Override
    public MessageStatsDTO getMessageStats(Map<String, String> filters) {
        MessageStatsDTO stats = new MessageStatsDTO();

        try {
            // Extract filters
            List<Message.MessageStatus> statusFilter = null;
            LocalDateTime startDate = null;
            LocalDateTime endDate = null;
            String businessComponentId = null;
            UUID flowId = null;

            if(filters != null && !filters.isEmpty()) {
                // Parse status filter
                if(filters.containsKey("status")) {
                    String statusStr = filters.get("status");
                    statusFilter = Arrays.stream(statusStr.split(","))
                        .map(s -> Message.MessageStatus.valueOf(s.toUpperCase()))
                        .toList();
                }

                // Parse date filters
                if(filters.containsKey("startDate")) {
                    startDate = LocalDateTime.parse(filters.get("startDate"));
                }
                if(filters.containsKey("endDate")) {
                    endDate = LocalDateTime.parse(filters.get("endDate"));
                }

                // Business component filter
                if(filters.containsKey("businessComponentId")) {
                    businessComponentId = filters.get("businessComponentId");
                }

                // Flow ID filter
                if(filters.containsKey("flowId")) {
                    flowId = UUID.fromString(filters.get("flowId"));
                }
            }

            // Use filtered count query
            List<Object[]> statusCounts;
            if(businessComponentId != null) {
                // Count by business component
                statusCounts = messageRepository.countByStatusGroupedAndBusinessComponent(businessComponentId);
            } else if(flowId != null) {
                // Count by flow
                statusCounts = messageRepository.countByStatusGroupedAndFlow(flowId);
            } else if(startDate != null && endDate != null) {
                // Count by date range
                statusCounts = messageRepository.countByStatusGroupedAndDateRange(startDate, endDate);
            } else {
                // No filters, use basic query
                statusCounts = messageRepository.countByStatusGrouped();
            }

            Map<Message.MessageStatus, Long> countMap = new HashMap<>();

            for(Object[] row : statusCounts) {
                Message.MessageStatus status = (Message.MessageStatus) row[0];
                Long count = (Long) row[1];
                countMap.put(status, count);
            }

            // Calculate totals
            long total = countMap.values().stream().mapToLong(Long::longValue).sum();
            long processing = countMap.getOrDefault(Message.MessageStatus.PROCESSING, 0L);
            long successful = countMap.getOrDefault(Message.MessageStatus.PROCESSED, 0L) +
                            countMap.getOrDefault(Message.MessageStatus.COMPLETED, 0L);
            long failed = countMap.getOrDefault(Message.MessageStatus.FAILED, 0L) +
                         countMap.getOrDefault(Message.MessageStatus.DEAD_LETTER, 0L);

            // Calculate success rate
            long totalCompleted = successful + failed;
            double successRate = totalCompleted > 0 ? (successful * 100.0 / totalCompleted) : 0;

            // Calculate average processing time with filters
            Double avgProcessingTime = null;
            if(businessComponentId != null) {
                avgProcessingTime = messageRepository.calculateAverageProcessingTimeByBusinessComponent(businessComponentId);
            } else if(flowId != null) {
                avgProcessingTime = messageRepository.calculateAverageProcessingTimeByFlowId(flowId);
            } else if(startDate != null && endDate != null) {
                avgProcessingTime = messageRepository.calculateAverageProcessingTimeByDateRange(startDate, endDate);
            } else {
                avgProcessingTime = messageRepository.calculateAverageProcessingTime();
            }

            stats.setTotal(total);
            stats.setProcessing(processing);
            stats.setSuccessful(successful);
            stats.setFailed(failed);
            stats.setSuccessRate(successRate);
            stats.setAvgProcessingTime(avgProcessingTime != null ? avgProcessingTime : 0.0);

            log.debug("Message stats calculated - Total: {}, Processing: {}, Successful: {}, Failed: {}, " +
                     "Success Rate: {:.2f}%, Avg Processing Time: {:.2f}ms",
                     total, processing, successful, failed, successRate, stats.getAvgProcessingTime());

        } catch(Exception e) {
            log.error("Error calculating message statistics", e);
            // Return zero stats on error
            return stats;
        }

        return stats;
    }

    @Override
    public MessageStatsDTO getStatsForPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        MessageStatsDTO stats = new MessageStatsDTO();

        try {
            // Get counts for the period
            long total = messageRepository.findByReceivedAtBetween(
                startDate, endDate, org.springframework.data.domain.Pageable.unpaged()
           ).getTotalElements();

            // For period stats, we still need to iterate through messages
            // but we can optimize by using database aggregation where possible
            List<Message> messages = messageRepository.findByReceivedAtBetween(
                startDate, endDate, org.springframework.data.domain.Pageable.unpaged()
           ).getContent();

            Map<Message.MessageStatus, Long> statusCounts = new HashMap<>();
            for(Message msg : messages) {
                statusCounts.merge(msg.getStatus(), 1L, Long::sum);
            }

            long processing = statusCounts.getOrDefault(Message.MessageStatus.PROCESSING, 0L);
            long successful = statusCounts.getOrDefault(Message.MessageStatus.PROCESSED, 0L) +
                            statusCounts.getOrDefault(Message.MessageStatus.COMPLETED, 0L);
            long failed = statusCounts.getOrDefault(Message.MessageStatus.FAILED, 0L) +
                         statusCounts.getOrDefault(Message.MessageStatus.DEAD_LETTER, 0L);

            long totalCompleted = successful + failed;
            double successRate = totalCompleted > 0 ? (successful * 100.0 / totalCompleted) : 0;

            // Get average processing time for period
            Double avgProcessingTime = messageRepository.calculateAverageProcessingTimeForPeriod(
                startDate, endDate
           );

            stats.setTotal(total);
            stats.setProcessing(processing);
            stats.setSuccessful(successful);
            stats.setFailed(failed);
            stats.setSuccessRate(successRate);
            stats.setAvgProcessingTime(avgProcessingTime != null ? avgProcessingTime : 0.0);

            log.debug("Period stats calculated for {} to {} - Total: {}",
                     startDate, endDate, total);

        } catch(Exception e) {
            log.error("Error calculating period statistics", e);
        }

        return stats;
    }

    /**
     * Get real - time statistics(for dashboards)
     */
    public MessageStatsDTO getRealtimeStats() {
        // Get stats for the last hour
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        return getStatsForPeriod(oneHourAgo, LocalDateTime.now());
    }

    /**
     * Get statistics breakdown by status
     */
    public Map<String, Long> getStatusBreakdown() {
        Map<String, Long> breakdown = new HashMap<>();

        List<Object[]> statusCounts = messageRepository.countByStatusGrouped();
        for(Object[] row : statusCounts) {
            Message.MessageStatus status = (Message.MessageStatus) row[0];
            Long count = (Long) row[1];
            breakdown.put(status.name(), count);
        }

        return breakdown;
    }
}
