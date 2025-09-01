package com.integrixs.data.repository;

import com.integrixs.data.model.SagaStep;
import com.integrixs.data.model.SagaTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SagaStepRepository extends JpaRepository<SagaStep, UUID> {
    
    List<SagaStep> findBySagaTransaction(SagaTransaction sagaTransaction);
    
    List<SagaStep> findBySagaTransactionOrderByStepOrder(SagaTransaction sagaTransaction);
    
    @Query("SELECT ss FROM SagaStep ss WHERE ss.sagaTransaction.id = :transactionId AND ss.status = :status")
    List<SagaStep> findByTransactionIdAndStatus(@Param("transactionId") UUID transactionId, 
                                                @Param("status") SagaStep.StepStatus status);
    
    @Query("SELECT ss FROM SagaStep ss WHERE ss.sagaTransaction.sagaId = :sagaId ORDER BY ss.stepOrder")
    List<SagaStep> findBySagaId(@Param("sagaId") String sagaId);
    
    @Query("SELECT ss FROM SagaStep ss WHERE ss.sagaTransaction.id = :transactionId ORDER BY ss.stepOrder")
    List<SagaStep> findByTransactionIdOrderByStepOrder(@Param("transactionId") UUID transactionId);
}