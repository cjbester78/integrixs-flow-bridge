package com.integrixs.backend.domain.repository;

import com.integrixs.data.model.FlowTransformation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Flow transformation domain repository port - domain layer
 * Acts as a port in hexagonal architecture for flow transformation persistence operations
 */
public interface FlowTransformationDomainRepositoryPort {

    /**
     * Find transformation by ID
     * @param id The transformation ID
     * @return Optional transformation
     */
    Optional<FlowTransformation> findById(UUID id);

    /**
     * Find all transformations for a flow
     * @param flowId The flow ID
     * @return List of transformations
     */
    List<FlowTransformation> findByFlowId(UUID flowId);

    /**
     * Find transformations by flow ordered by execution order
     * @param flowId The flow ID
     * @return Ordered list of transformations
     */
    List<FlowTransformation> findByFlowIdOrderByExecutionOrder(UUID flowId);

    /**
     * Save a transformation
     * @param transformation The transformation to save
     * @return Saved transformation
     */
    FlowTransformation save(FlowTransformation transformation);

    /**
     * Delete a transformation by ID
     * @param id The transformation ID
     */
    void deleteById(UUID id);

    /**
     * Delete all transformations for a flow
     * @param flowId The flow ID
     */
    void deleteByFlowId(UUID flowId);

    /**
     * Check if transformation exists
     * @param id The transformation ID
     * @return true if exists
     */
    boolean existsById(UUID id);

    /**
     * Count transformations for a flow
     * @param flowId The flow ID
     * @return Count of transformations
     */
    long countByFlowId(UUID flowId);
}
