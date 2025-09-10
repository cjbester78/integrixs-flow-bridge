package com.integrixs.data.repository;

import com.integrixs.data.model.FlowOrchestrationStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FlowOrchestrationStepRepository extends JpaRepository<FlowOrchestrationStep, UUID> {
    
    /**
     * Find all steps for a flow ordered by execution order
     */
    List<FlowOrchestrationStep> findByFlowIdOrderByExecutionOrderAsc(UUID flowId);
    
    /**
     * Find active steps for a flow
     */
    @Query("SELECT s FROM FlowOrchestrationStep s WHERE s.flow.id = :flowId AND s.isActive = true ORDER BY s.executionOrder ASC")
    List<FlowOrchestrationStep> findActiveStepsByFlowId(@Param("flowId") UUID flowId);
    
    /**
     * Delete all steps for a flow
     */
    @Modifying
    @Query("DELETE FROM FlowOrchestrationStep s WHERE s.flow.id = :flowId")
    void deleteByFlowId(@Param("flowId") UUID flowId);
    
    /**
     * Count steps for a flow
     */
    long countByFlowId(UUID flowId);
    
    /**
     * Get the maximum execution order for a flow
     */
    @Query("SELECT COALESCE(MAX(s.executionOrder), 0) FROM FlowOrchestrationStep s WHERE s.flow.id = :flowId")
    Integer getMaxExecutionOrder(@Param("flowId") UUID flowId);
    
    /**
     * Find steps by type for a flow
     */
    List<FlowOrchestrationStep> findByFlowIdAndStepType(UUID flowId, String stepType);
}