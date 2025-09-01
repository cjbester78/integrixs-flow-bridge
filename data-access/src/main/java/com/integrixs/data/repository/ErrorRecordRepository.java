package com.integrixs.data.repository;

import com.integrixs.data.model.ErrorRecord;
import com.integrixs.data.model.IntegrationFlow;
import com.integrixs.data.model.FlowExecution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ErrorRecordRepository extends JpaRepository<ErrorRecord, UUID> {
    
    List<ErrorRecord> findByFlow(IntegrationFlow flow);
    
    Page<ErrorRecord> findByFlowExecution(FlowExecution flowExecution, Pageable pageable);
    
    List<ErrorRecord> findByResolvedFalse();
    
    @Query("SELECT er FROM ErrorRecord er WHERE er.occurredAt BETWEEN :startDate AND :endDate")
    Page<ErrorRecord> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                     @Param("endDate") LocalDateTime endDate, 
                                     Pageable pageable);
    
    @Query("SELECT er FROM ErrorRecord er WHERE er.flow.id = :flowId AND er.resolved = false")
    List<ErrorRecord> findUnresolvedByFlowId(@Param("flowId") UUID flowId);
    
    @Query("SELECT er FROM ErrorRecord er WHERE er.severity = :severity AND er.resolved = false")
    List<ErrorRecord> findUnresolvedBySeverity(@Param("severity") ErrorRecord.ErrorSeverity severity);
    
    @Query("SELECT COUNT(er) FROM ErrorRecord er WHERE er.flow.id = :flowId AND er.errorType = :errorType")
    Long countByFlowIdAndErrorType(@Param("flowId") UUID flowId, @Param("errorType") ErrorRecord.ErrorType errorType);
    
    @Query("SELECT er FROM ErrorRecord er WHERE er.flow.id = :flowId AND er.occurredAt > :since ORDER BY er.occurredAt DESC")
    List<ErrorRecord> findByFlowIdAndOccurredAtAfterOrderByOccurredAtDesc(@Param("flowId") UUID flowId, @Param("since") LocalDateTime since);
}