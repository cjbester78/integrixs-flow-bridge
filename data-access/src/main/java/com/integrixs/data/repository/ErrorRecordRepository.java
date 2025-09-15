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

    Page<ErrorRecord> findByOccurredAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    List<ErrorRecord> findByFlowIdAndResolvedFalse(UUID flowId);

    List<ErrorRecord> findBySeverityAndResolvedFalse(ErrorRecord.ErrorSeverity severity);

    Long countByFlowIdAndErrorType(UUID flowId, ErrorRecord.ErrorType errorType);

    List<ErrorRecord> findByFlowIdAndOccurredAtAfterOrderByOccurredAtDesc(UUID flowId, LocalDateTime since);
}
