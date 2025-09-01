package com.integrixs.data.repository;

import com.integrixs.data.model.RetryPolicy;
import com.integrixs.data.model.IntegrationFlow;
import com.integrixs.data.model.ErrorRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RetryPolicyRepository extends JpaRepository<RetryPolicy, UUID> {
    
    Optional<RetryPolicy> findByPolicyName(String policyName);
    
    List<RetryPolicy> findByFlow(IntegrationFlow flow);
    
    List<RetryPolicy> findByActiveTrue();
    
    @Query("SELECT rp FROM RetryPolicy rp WHERE rp.flow.id = :flowId AND rp.errorType = :errorType AND rp.active = true")
    Optional<RetryPolicy> findActiveByFlowIdAndErrorType(@Param("flowId") UUID flowId, 
                                                         @Param("errorType") ErrorRecord.ErrorType errorType);
    
    @Query("SELECT rp FROM RetryPolicy rp WHERE rp.flow IS NULL AND rp.errorType = :errorType AND rp.active = true")
    Optional<RetryPolicy> findGlobalPolicyByErrorType(@Param("errorType") ErrorRecord.ErrorType errorType);
    
    @Query("SELECT rp FROM RetryPolicy rp WHERE rp.flow.id = :flowId AND rp.active = true")
    Optional<RetryPolicy> findByFlowId(@Param("flowId") UUID flowId);
}