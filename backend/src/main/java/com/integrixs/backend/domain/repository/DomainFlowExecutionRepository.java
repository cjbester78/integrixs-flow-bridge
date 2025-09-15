package com.integrixs.backend.domain.repository;

import com.integrixs.data.model.FlowExecution;
import com.integrixs.data.model.FlowExecution.ExecutionStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository interface for flow executions
 */
public interface DomainFlowExecutionRepository {

    List<FlowExecution> findAll();

    Optional<FlowExecution> findById(UUID id);

    FlowExecution save(FlowExecution execution);

    Double getAverageExecutionTimeForDateRange(LocalDateTime startDate, LocalDateTime endDate);

    Double getAverageExecutionTimeForBusinessComponentAndDateRange(UUID businessComponentId,
                                                                  LocalDateTime startDate,
                                                                  LocalDateTime endDate);

    long countByStatusAndDateRange(ExecutionStatus status, LocalDateTime startDate, LocalDateTime endDate);
}
