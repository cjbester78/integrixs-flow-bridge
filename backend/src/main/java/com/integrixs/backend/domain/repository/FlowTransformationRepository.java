package com.integrixs.backend.domain.repository;

import com.integrixs.data.model.FlowTransformation;

import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository interface for flow transformations
 */
public interface FlowTransformationRepository {

    Optional<FlowTransformation> findById(UUID id);

    FlowTransformation save(FlowTransformation transformation);

    boolean existsById(UUID id);
}
