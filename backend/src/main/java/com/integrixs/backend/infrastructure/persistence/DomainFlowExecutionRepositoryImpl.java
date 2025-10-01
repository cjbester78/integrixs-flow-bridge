package com.integrixs.backend.infrastructure.persistence;

import com.integrixs.backend.domain.repository.DomainFlowExecutionRepositoryPort;
import com.integrixs.data.model.FlowExecution;
import com.integrixs.data.model.FlowExecution.ExecutionStatus;
import com.integrixs.data.sql.repository.FlowExecutionSqlRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of DomainFlowExecutionRepository using SQL
 * Bridges between domain repository interface and SQL repository
 */
@Repository("domainFlowExecutionRepositoryImpl")
public class DomainFlowExecutionRepositoryImpl implements DomainFlowExecutionRepositoryPort {

    private final FlowExecutionSqlRepository sqlRepository;

    public DomainFlowExecutionRepositoryImpl(FlowExecutionSqlRepository sqlRepository) {
        this.sqlRepository = sqlRepository;
    }

    @Override
    public List<FlowExecution> findAll() {
        return sqlRepository.findAll();
    }

    @Override
    public Optional<FlowExecution> findById(UUID id) {
        return sqlRepository.findById(id);
    }

    @Override
    public FlowExecution save(FlowExecution execution) {
        return sqlRepository.save(execution);
    }

    @Override
    public Double getAverageExecutionTimeForDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        // Custom implementation needed
        // For now, return a mock value
        return 250.0;
    }

    @Override
    public Double getAverageExecutionTimeForBusinessComponentAndDateRange(UUID businessComponentId,
                                                                         LocalDateTime startDate,
                                                                         LocalDateTime endDate) {
        // Custom implementation needed with joins
        // For now, return a mock value
        return 275.0;
    }

    @Override
    public long countByStatusAndDateRange(ExecutionStatus status, LocalDateTime startDate, LocalDateTime endDate) {
        return sqlRepository.findByStartedAtBetween(startDate, endDate, PageRequest.of(0, Integer.MAX_VALUE))
            .stream()
            .filter(e -> e.getStatus() == status)
            .count();
    }
}
