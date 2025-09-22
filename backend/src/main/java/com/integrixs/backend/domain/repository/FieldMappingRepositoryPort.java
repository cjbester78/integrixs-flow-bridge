package com.integrixs.backend.domain.repository;

import com.integrixs.data.model.FieldMapping;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository interface for field mappings
 */
public interface FieldMappingRepositoryPort {

    Optional<FieldMapping> findById(UUID id);

    List<FieldMapping> findByTransformationId(UUID transformationId);

    FieldMapping save(FieldMapping fieldMapping);

    void deleteById(UUID id);

    boolean existsById(UUID id);

    long countByTransformationId(UUID transformationId);
}
