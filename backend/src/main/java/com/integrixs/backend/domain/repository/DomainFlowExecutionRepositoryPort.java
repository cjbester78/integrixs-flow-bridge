package com.integrixs.backend.domain.repository;

import com.integrixs.data.model.FlowExecution;
import com.integrixs.data.model.FlowExecution.ExecutionStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Flow execution repository port - domain layer
 * Acts as a port in hexagonal architecture for flow execution persistence operations
 */
public interface DomainFlowExecutionRepositoryPort {

    List<FlowExecution> findAll();

    Optional<FlowExecution> findById(UUID id);

    FlowExecution save(FlowExecution execution);

    Double getAverageExecutionTimeForDateRange(LocalDateTime startDate, LocalDateTime endDate);

    Double getAverageExecutionTimeForBusinessComponentAndDateRange(UUID businessComponentId,
                                                                  LocalDateTime startDate,
                                                                  LocalDateTime endDate);

    long countByStatusAndDateRange(ExecutionStatus status, LocalDateTime startDate, LocalDateTime endDate);
}
