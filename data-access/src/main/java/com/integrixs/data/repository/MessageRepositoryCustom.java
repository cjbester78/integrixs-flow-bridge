package com.integrixs.data.repository;

import com.integrixs.data.model.Message;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Custom repository interface for Message entity with complex queries
 */
public interface MessageRepositoryCustom {

    /**
     * Calculate average processing time using native SQL
     */
    Double calculateAverageProcessingTime();

    /**
     * Calculate average processing time by flow ID using native SQL
     */
    Double calculateAverageProcessingTimeByFlowId(UUID flowId);

    /**
     * Calculate average processing time by business component using native SQL
     */
    Double calculateAverageProcessingTimeByBusinessComponent(String businessComponentId);

    /**
     * Calculate average processing time by date range using native SQL
     */
    Double calculateAverageProcessingTimeByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Get grouped status counts with filters using native SQL
     */
    Map<Message.MessageStatus, Long> getStatusCountsWithFilters(Map<String, Object> filters);
}
