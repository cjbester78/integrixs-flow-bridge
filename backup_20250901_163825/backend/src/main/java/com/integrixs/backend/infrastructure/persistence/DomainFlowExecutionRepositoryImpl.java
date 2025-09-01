package com.integrixs.backend.infrastructure.persistence;

import com.integrixs.backend.domain.repository.DomainFlowExecutionRepository;
import com.integrixs.data.model.FlowExecution;
import com.integrixs.data.model.FlowExecution.ExecutionStatus;
import com.integrixs.data.repository.FlowExecutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of DomainFlowExecutionRepository using JPA
 * Bridges between domain repository interface and JPA repository
 */
@Repository("domainFlowExecutionRepositoryImpl")
@RequiredArgsConstructor
public class DomainFlowExecutionRepositoryImpl implements DomainFlowExecutionRepository {
    
    private final FlowExecutionRepository jpaRepository;
    
    @Override
    public List<FlowExecution> findAll() {
        return jpaRepository.findAll();
    }
    
    @Override
    public Optional<FlowExecution> findById(UUID id) {
        return jpaRepository.findById(id);
    }
    
    @Override
    public FlowExecution save(FlowExecution execution) {
        return jpaRepository.save(execution);
    }
    
    @Override
    public Double getAverageExecutionTimeForDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        // This would need a custom query in the JPA repository
        // For now, return a mock value
        return 250.0;
    }
    
    @Override
    public Double getAverageExecutionTimeForBusinessComponentAndDateRange(UUID businessComponentId, 
                                                                         LocalDateTime startDate, 
                                                                         LocalDateTime endDate) {
        // This would need a custom query that joins with flow and adapter tables
        // For now, return a mock value
        return 275.0;
    }
    
    @Override
    public long countByStatusAndDateRange(ExecutionStatus status, LocalDateTime startDate, LocalDateTime endDate) {
        return jpaRepository.findByDateRange(startDate, endDate, PageRequest.of(0, Integer.MAX_VALUE))
            .stream()
            .filter(e -> e.getStatus() == status)
            .count();
    }
}