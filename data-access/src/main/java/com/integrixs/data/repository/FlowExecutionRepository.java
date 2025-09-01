package com.integrixs.data.repository;

import com.integrixs.data.model.FlowExecution;
import com.integrixs.data.model.IntegrationFlow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FlowExecutionRepository extends JpaRepository<FlowExecution, UUID> {
    
    Optional<FlowExecution> findByExecutionId(String executionId);
    
    Page<FlowExecution> findByFlow(IntegrationFlow flow, Pageable pageable);
    
    List<FlowExecution> findByStatus(FlowExecution.ExecutionStatus status);
    
    Page<FlowExecution> findByFlowIdOrderByStartedAtDesc(UUID flowId, Pageable pageable);
    
    Page<FlowExecution> findByStartedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    @Query("SELECT fe FROM FlowExecution fe WHERE fe.status IN :statuses AND fe.startedAt < :cutoffTime")
    List<FlowExecution> findStaleExecutions(@Param("statuses") List<FlowExecution.ExecutionStatus> statuses,
                                           @Param("cutoffTime") LocalDateTime cutoffTime);
    
    Long countByFlowIdAndStatus(UUID flowId, FlowExecution.ExecutionStatus status);
    
    @Query("SELECT AVG(fe.executionTimeMs) FROM FlowExecution fe WHERE fe.flow.id = :flowId AND fe.status = 'COMPLETED'")
    Double getAverageExecutionTime(@Param("flowId") UUID flowId);
    
    @Query("SELECT fe FROM FlowExecution fe JOIN fe.flow f JOIN Message m ON m.flow = f WHERE m.id = :messageId")
    Optional<FlowExecution> findByMessageId(@Param("messageId") UUID messageId);
}