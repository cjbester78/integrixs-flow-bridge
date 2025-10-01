package com.integrixs.backend.infrastructure.persistence;

import com.integrixs.backend.domain.repository.FieldMappingRepositoryPort;
import com.integrixs.data.model.FieldMapping;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Infrastructure implementation of FieldMappingRepository
 */
@Repository("domainFieldMappingRepository")
public class FieldMappingRepositoryImpl implements FieldMappingRepositoryPort {

    private final com.integrixs.data.sql.repository.FieldMappingSqlRepository sqlRepository;

    public FieldMappingRepositoryImpl(com.integrixs.data.sql.repository.FieldMappingSqlRepository sqlRepository) {
        this.sqlRepository = sqlRepository;
    }

    @Override
    public Optional<FieldMapping> findById(UUID id) {
        return sqlRepository.findById(id);
    }

    @Override
    public List<FieldMapping> findByTransformationId(UUID transformationId) {
        return sqlRepository.findByTransformationId(transformationId);
    }

    @Override
    public FieldMapping save(FieldMapping fieldMapping) {
        return sqlRepository.save(fieldMapping);
    }

    @Override
    public void deleteById(UUID id) {
        sqlRepository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return sqlRepository.existsById(id);
    }

    @Override
    public long countByTransformationId(UUID transformationId) {
        return sqlRepository.countByTransformationId(transformationId);
    }
}
