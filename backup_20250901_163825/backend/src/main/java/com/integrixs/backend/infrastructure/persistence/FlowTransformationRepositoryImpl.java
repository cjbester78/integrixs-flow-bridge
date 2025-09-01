package com.integrixs.backend.infrastructure.persistence;

import com.integrixs.backend.domain.repository.FlowTransformationRepository;
import com.integrixs.data.model.FlowTransformation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Infrastructure implementation of FlowTransformationRepository
 */
@Repository("domainFlowTransformationRepository")
@RequiredArgsConstructor
public class FlowTransformationRepositoryImpl implements FlowTransformationRepository {
    
    private final com.integrixs.data.repository.FlowTransformationRepository jpaRepository;
    
    @Override
    public Optional<FlowTransformation> findById(UUID id) {
        return jpaRepository.findById(id);
    }
    
    @Override
    public FlowTransformation save(FlowTransformation transformation) {
        return jpaRepository.save(transformation);
    }
    
    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }
}