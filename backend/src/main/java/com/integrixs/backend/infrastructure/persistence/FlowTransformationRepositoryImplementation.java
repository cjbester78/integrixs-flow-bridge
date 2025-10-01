package com.integrixs.backend.infrastructure.persistence;

import com.integrixs.backend.domain.repository.FlowTransformationDomainRepositoryPort;
import com.integrixs.data.model.FlowTransformation;
import com.integrixs.data.sql.repository.FlowTransformationSqlRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Infrastructure implementation of flow transformation repository
 */
@Repository
public class FlowTransformationRepositoryImplementation implements FlowTransformationDomainRepositoryPort {

    private final FlowTransformationSqlRepository sqlRepository;

    public FlowTransformationRepositoryImplementation(FlowTransformationSqlRepository sqlRepository) {
        this.sqlRepository = sqlRepository;
    }

    @Override
    public Optional<FlowTransformation> findById(UUID id) {
        return sqlRepository.findById(id);
    }

    @Override
    public List<FlowTransformation> findByFlowId(UUID flowId) {
        return sqlRepository.findByFlowId(flowId);
    }

    @Override
    public List<FlowTransformation> findByFlowIdOrderByExecutionOrder(UUID flowId) {
        return sqlRepository.findByFlowIdOrderByExecutionOrder(flowId);
    }

    @Override
    public FlowTransformation save(FlowTransformation transformation) {
        return sqlRepository.save(transformation);
    }

    @Override
    public void deleteById(UUID id) {
        sqlRepository.deleteById(id);
    }

    @Override
    public void deleteByFlowId(UUID flowId) {
        sqlRepository.deleteByFlowId(flowId);
    }

    @Override
    public boolean existsById(UUID id) {
        return sqlRepository.existsById(id);
    }

    @Override
    public long countByFlowId(UUID flowId) {
        return sqlRepository.countByFlowId(flowId);
    }
}
