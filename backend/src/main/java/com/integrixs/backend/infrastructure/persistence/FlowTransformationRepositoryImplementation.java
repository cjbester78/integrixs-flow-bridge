package com.integrixs.backend.infrastructure.persistence;

import com.integrixs.backend.domain.repository.FlowTransformationDomainRepository;
import com.integrixs.data.model.FlowTransformation;
import com.integrixs.data.repository.FlowTransformationRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Infrastructure implementation of flow transformation repository
 */
@Repository
public class FlowTransformationRepositoryImplementation implements FlowTransformationDomainRepository {

    private final FlowTransformationRepository jpaRepository;

    @Override
    public Optional<FlowTransformation> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<FlowTransformation> findByFlowId(UUID flowId) {
        return jpaRepository.findByFlowId(flowId);
    }

    @Override
    public List<FlowTransformation> findByFlowIdOrderByExecutionOrder(UUID flowId) {
        return jpaRepository.findByFlowIdOrderByExecutionOrder(flowId);
    }

    @Override
    public FlowTransformation save(FlowTransformation transformation) {
        return jpaRepository.save(transformation);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public void deleteByFlowId(UUID flowId) {
        jpaRepository.deleteByFlowId(flowId);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public long countByFlowId(UUID flowId) {
        return jpaRepository.countByFlowId(flowId);
    }
}
