package com.integrixs.data.repository;

import com.integrixs.data.model.TargetFieldMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for target - specific field mapping operations
 */
@Repository
public interface TargetFieldMappingRepository extends JpaRepository<TargetFieldMapping, UUID> {

    /**
     * Find all mappings for a specific orchestration target
     */
    @Query("SELECT tfm FROM TargetFieldMapping tfm " +
           "WHERE tfm.orchestrationTarget.id = :targetId " +
           "ORDER BY tfm.mappingOrder ASC")
    List<TargetFieldMapping> findByOrchestrationTargetId(@Param("targetId") UUID targetId);

    /**
     * Find active mappings for a target
     */
    @Query("SELECT tfm FROM TargetFieldMapping tfm " +
           "WHERE tfm.orchestrationTarget.id = :targetId " +
           "AND tfm.active = true " +
           "ORDER BY tfm.mappingOrder ASC")
    List<TargetFieldMapping> findActiveByOrchestrationTargetId(@Param("targetId") UUID targetId);

    /**
     * Find mappings for a flow across all targets
     */
    @Query("SELECT tfm FROM TargetFieldMapping tfm " +
           "JOIN tfm.orchestrationTarget ot " +
           "WHERE ot.flow.id = :flowId " +
           "ORDER BY ot.executionOrder ASC, tfm.mappingOrder ASC")
    List<TargetFieldMapping> findByFlowId(@Param("flowId") UUID flowId);

    /**
     * Find mappings by source field path for a target
     */
    List<TargetFieldMapping> findByOrchestrationTargetIdAndSourceFieldPath(
        UUID targetId, String sourceFieldPath);

    /**
     * Find mappings by target field path for a target
     */
    List<TargetFieldMapping> findByOrchestrationTargetIdAndTargetFieldPath(
        UUID targetId, String targetFieldPath);

    /**
     * Delete all mappings for a target
     */
    void deleteByOrchestrationTargetId(UUID targetId);

    /**
     * Count mappings for a target
     */
    long countByOrchestrationTargetId(UUID targetId);

    /**
     * Find required mappings for a target
     */
    @Query("SELECT tfm FROM TargetFieldMapping tfm " +
           "WHERE tfm.orchestrationTarget.id = :targetId " +
           "AND tfm.required = true")
    List<TargetFieldMapping> findRequiredMappingsByTargetId(@Param("targetId") UUID targetId);

    /**
     * Check if a target field is already mapped
     */
    boolean existsByOrchestrationTargetIdAndTargetFieldPath(UUID targetId, String targetFieldPath);
}
