package com.integrixs.backend.infrastructure.persistence;

import com.integrixs.backend.domain.repository.IntegrationFlowRepositoryPort;
import com.integrixs.data.model.IntegrationFlow;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of IntegrationFlowSqlRepository using native SQL
 * Bridges between domain repository interface and SQL repository
 */
@Repository("domainIntegrationFlowRepository")
public class IntegrationFlowRepositoryImpl implements IntegrationFlowRepositoryPort {

    private final com.integrixs.data.sql.repository.IntegrationFlowSqlRepository sqlRepository;

    public IntegrationFlowRepositoryImpl(com.integrixs.data.sql.repository.IntegrationFlowSqlRepository sqlRepository) {
        this.sqlRepository = sqlRepository;
    }

    @Override
    public List<IntegrationFlow> findAll() {
        return sqlRepository.findAll();
    }

    @Override
    public Optional<IntegrationFlow> findById(UUID id) {
        return sqlRepository.findById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return sqlRepository.existsById(id);
    }

    @Override
    public boolean existsByName(String name) {
        return sqlRepository.existsByName(name);
    }

    @Override
    public boolean existsByNameAndIdNot(String name, UUID excludeId) {
        return sqlRepository.existsByNameAndIdNot(name, excludeId);
    }

    @Override
    public IntegrationFlow save(IntegrationFlow flow) {
        return sqlRepository.save(flow);
    }

    @Override
    public void deleteById(UUID id) {
        sqlRepository.deleteById(id);
    }

    @Override
    public List<IntegrationFlow> findByIsActive(boolean active) {
        return sqlRepository.findByIsActive(active);
    }

    @Override
    public List<IntegrationFlow> findBySourceAdapterId(UUID inboundAdapterId) {
        return sqlRepository.findBySourceAdapterId(inboundAdapterId);
    }

    @Override
    public List<IntegrationFlow> findByTargetAdapterId(UUID outboundAdapterId) {
        return sqlRepository.findByTargetAdapterId(outboundAdapterId);
    }

    @Override
    public void flush() {
        sqlRepository.flush();
    }
}
