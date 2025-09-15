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

    Page<DeadLetterMessage> findByQueuedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    List<DeadLetterMessage> findByFlowIdAndReprocessedFalse(Long flowId);

    List<DeadLetterMessage> findByCorrelationId(String correlationId);

    Long countByFlowIdAndReprocessedFalse(Long flowId);

    List<DeadLetterMessage> findByStatusAndRetryCountLessThanOrderByQueuedAtAsc(DeadLetterMessage.Status status, int maxRetries);
}
