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
     * Calculate average processing time using Java instead of database - specific functions
     */
    Double calculateAverageProcessingTimeJpa();

    /**
     * Calculate average processing time by flow ID using Java
     */
    Double calculateAverageProcessingTimeByFlowIdJpa(UUID flowId);

    /**
     * Calculate average processing time by business component using Java
     */
    Double calculateAverageProcessingTimeByBusinessComponentJpa(String businessComponentId);

    /**
     * Calculate average processing time by date range using Java
     */
    Double calculateAverageProcessingTimeByDateRangeJpa(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Get grouped status counts with filters using Criteria API
     */
    Map<Message.MessageStatus, Long> getStatusCountsWithFilters(Map<String, Object> filters);
}
