package com.integrixs.data.repository;

import com.integrixs.data.model.SagaTransaction;
import com.integrixs.data.model.IntegrationFlow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SagaTransactionRepository extends JpaRepository<SagaTransaction, UUID> {

    Optional<SagaTransaction> findBySagaId(String sagaId);

    List<SagaTransaction> findByFlow(IntegrationFlow flow);

    List<SagaTransaction> findByStatus(SagaTransaction.SagaStatus status);

    @Query("SELECT st FROM SagaTransaction st WHERE st.status IN :statuses")
    List<SagaTransaction> findByStatusIn(@Param("statuses") List<SagaTransaction.SagaStatus> statuses);

    @Query("SELECT st FROM SagaTransaction st WHERE st.startedAt < :cutoffTime AND st.status = :status")
    List<SagaTransaction> findStaleSagas(@Param("cutoffTime") LocalDateTime cutoffTime,
                                        @Param("status") SagaTransaction.SagaStatus status);

    @Query("SELECT st FROM SagaTransaction st WHERE st.flow.id = :flowId ORDER BY st.startedAt DESC")
    List<SagaTransaction> findRecentByFlowId(@Param("flowId") Long flowId);
}
