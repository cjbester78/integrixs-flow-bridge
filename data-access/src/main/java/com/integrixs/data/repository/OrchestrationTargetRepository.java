package com.integrixs.data.repository;

import com.integrixs.data.model.OrchestrationTarget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for orchestration target operations
 */
@Repository
public interface OrchestrationTargetRepository extends JpaRepository<OrchestrationTarget, UUID> {

    /**
     * Find all targets for a specific flow, ordered by execution order
     */
    @Query("SELECT ot FROM OrchestrationTarget ot " +
           "LEFT JOIN FETCH ot.targetAdapter " +
           "WHERE ot.flow.id = :flowId " +
           "ORDER BY ot.executionOrder ASC")
    List<OrchestrationTarget> findByFlowIdOrderByExecutionOrder(@Param("flowId") UUID flowId);

    /**
     * Find all active targets for a flow
     */
    @Query("SELECT ot FROM OrchestrationTarget ot " +
           "LEFT JOIN FETCH ot.targetAdapter " +
           "WHERE ot.flow.id = :flowId AND ot.active = true " +
           "ORDER BY ot.executionOrder ASC")
    List<OrchestrationTarget> findActiveByFlowId(@Param("flowId") UUID flowId);

    /**
     * Find targets by flow and execution order
     */
    List<OrchestrationTarget> findByFlowIdAndExecutionOrder(UUID flowId, Integer executionOrder);

    /**
     * Find parallel targets at a specific execution order
     */
    @Query("SELECT ot FROM OrchestrationTarget ot " +
           "WHERE ot.flow.id = :flowId " +
           "AND ot.executionOrder = :order " +
           "AND ot.parallel = true")
    List<OrchestrationTarget> findParallelTargetsAtOrder(@Param("flowId") UUID flowId,
                                                         @Param("order") Integer order);

    /**
     * Check if a target adapter is already used in a flow
     */
    boolean existsByFlowIdAndTargetAdapterId(UUID flowId, UUID targetAdapterId);

    /**
     * Count targets for a flow
     */
    long countByFlowId(UUID flowId);

    /**
     * Find the maximum execution order for a flow
     */
    @Query("SELECT COALESCE(MAX(ot.executionOrder), -1) FROM OrchestrationTarget ot " +
           "WHERE ot.flow.id = :flowId")
    Integer findMaxExecutionOrderByFlowId(@Param("flowId") UUID flowId);

    /**
     * Get the maximum execution order for a flow
     */
    @Query("SELECT MAX(ot.executionOrder) FROM OrchestrationTarget ot " +
           "WHERE ot.flow.id = :flowId")
    Optional<Integer> getMaxExecutionOrder(@Param("flowId") UUID flowId);

    /**
     * Find all targets for a flow(without ordering)
     */
    List<OrchestrationTarget> findByFlowId(UUID flowId);

    /**
     * Find targets with specific condition type
     */
    List<OrchestrationTarget> findByFlowIdAndConditionType(UUID flowId,
                                                           OrchestrationTarget.ConditionType conditionType);

    /**
     * Delete all targets for a flow
     */
    void deleteByFlowId(UUID flowId);

    /**
     * Find targets by target adapter ID
     */
    @Query("SELECT ot FROM OrchestrationTarget ot WHERE ot.targetAdapter.id = :adapterId")
    List<OrchestrationTarget> findByTargetAdapterId(@Param("adapterId") UUID adapterId);

    /**
     * Find targets that await response
     */
    @Query("SELECT ot FROM OrchestrationTarget ot " +
           "WHERE ot.flow.id = :flowId AND ot.awaitResponse = true")
    List<OrchestrationTarget> findTargetsAwaitingResponse(@Param("flowId") UUID flowId);

    /**
     * Update execution order for reordering
     */
    @Query("UPDATE OrchestrationTarget ot SET ot.executionOrder = :newOrder " +
           "WHERE ot.id = :targetId")
    void updateExecutionOrder(@Param("targetId") UUID targetId,
                             @Param("newOrder") Integer newOrder);
}
