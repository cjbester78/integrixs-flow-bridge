package com.integrixs.data.repository;

import com.integrixs.data.model.DeadLetterMessage;
import com.integrixs.data.model.IntegrationFlow;
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
public interface DeadLetterMessageRepository extends JpaRepository<DeadLetterMessage, UUID> {
    
    Page<DeadLetterMessage> findByFlow(IntegrationFlow flow, Pageable pageable);
    
    Page<DeadLetterMessage> findByReprocessedFalse(Pageable pageable);
    
    @Query("SELECT dlm FROM DeadLetterMessage dlm WHERE dlm.queuedAt BETWEEN :startDate AND :endDate")
    Page<DeadLetterMessage> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                           @Param("endDate") LocalDateTime endDate, 
                                           Pageable pageable);
    
    @Query("SELECT dlm FROM DeadLetterMessage dlm WHERE dlm.flow.id = :flowId AND dlm.reprocessed = false")
    List<DeadLetterMessage> findUnprocessedByFlowId(@Param("flowId") Long flowId);
    
    @Query("SELECT dlm FROM DeadLetterMessage dlm WHERE dlm.correlationId = :correlationId")
    List<DeadLetterMessage> findByCorrelationId(@Param("correlationId") String correlationId);
    
    @Query("SELECT COUNT(dlm) FROM DeadLetterMessage dlm WHERE dlm.flow.id = :flowId AND dlm.reprocessed = false")
    Long countUnprocessedByFlowId(@Param("flowId") Long flowId);
    
    List<DeadLetterMessage> findByStatusAndRetryCountLessThanOrderByQueuedAtAsc(DeadLetterMessage.Status status, int maxRetries);
}