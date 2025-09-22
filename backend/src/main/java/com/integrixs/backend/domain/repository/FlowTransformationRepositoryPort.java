package com.integrixs.backend.domain.repository;

import com.integrixs.data.model.FlowTransformation;

import java.util.Optional;
import java.util.UUID;

/**
 * Flow transformation repository port - domain layer
 * Acts as a port in hexagonal architecture for flow transformation persistence operations
 */
public interface FlowTransformationRepositoryPort {

    Optional<FlowTransformation> findById(UUID id);

    FlowTransformation save(FlowTransformation transformation);

    boolean existsById(UUID id);
}
