package com.integrixs.backend.infrastructure.persistence;

import com.integrixs.backend.domain.repository.FlowTransformationRepositoryPort;
import com.integrixs.data.model.FlowTransformation;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Infrastructure implementation of FlowTransformationRepository
 */
@Repository("domainFlowTransformationRepository")
public class FlowTransformationRepositoryImpl implements FlowTransformationRepositoryPort {

    private final com.integrixs.data.repository.FlowTransformationRepository jpaRepository;
    
    public FlowTransformationRepositoryImpl(com.integrixs.data.repository.FlowTransformationRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

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
