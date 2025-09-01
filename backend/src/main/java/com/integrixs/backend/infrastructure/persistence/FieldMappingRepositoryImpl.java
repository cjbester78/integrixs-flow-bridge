package com.integrixs.backend.infrastructure.persistence;

import com.integrixs.backend.domain.repository.FieldMappingRepository;
import com.integrixs.data.model.FieldMapping;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Infrastructure implementation of FieldMappingRepository
 */
@Repository("domainFieldMappingRepository")
@RequiredArgsConstructor
public class FieldMappingRepositoryImpl implements FieldMappingRepository {
    
    private final com.integrixs.data.repository.FieldMappingRepository jpaRepository;
    
    @Override
    public Optional<FieldMapping> findById(UUID id) {
        return jpaRepository.findById(id);
    }
    
    @Override
    public List<FieldMapping> findByTransformationId(UUID transformationId) {
        return jpaRepository.findByTransformationId(transformationId);
    }
    
    @Override
    public FieldMapping save(FieldMapping fieldMapping) {
        return jpaRepository.save(fieldMapping);
    }
    
    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
    
    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }
    
    @Override
    public long countByTransformationId(UUID transformationId) {
        return jpaRepository.countByTransformationId(transformationId);
    }
}