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

    private final com.integrixs.data.sql.repository.FlowTransformationSqlRepository sqlRepository;

    public FlowTransformationRepositoryImpl(com.integrixs.data.sql.repository.FlowTransformationSqlRepository sqlRepository) {
        this.sqlRepository = sqlRepository;
    }

    @Override
    public Optional<FlowTransformation> findById(UUID id) {
        return sqlRepository.findById(id);
    }

    @Override
    public FlowTransformation save(FlowTransformation transformation) {
        return sqlRepository.save(transformation);
    }

    @Override
    public boolean existsById(UUID id) {
        return sqlRepository.existsById(id);
    }
}
