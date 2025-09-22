package com.integrixs.backend.infrastructure.persistence;

import com.integrixs.backend.domain.repository.IntegrationFlowRepositoryPort;
import com.integrixs.data.model.IntegrationFlow;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of IntegrationFlowRepository using JPA
 * Bridges between domain repository interface and JPA repository
 */
@Repository("domainIntegrationFlowRepository")
public class IntegrationFlowRepositoryImpl implements IntegrationFlowRepositoryPort {

    private final com.integrixs.data.repository.IntegrationFlowRepository jpaRepository;
    
    public IntegrationFlowRepositoryImpl(com.integrixs.data.repository.IntegrationFlowRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<IntegrationFlow> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public Optional<IntegrationFlow> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public boolean existsByName(String name) {
        return jpaRepository.existsByName(name);
    }

    @Override
    public boolean existsByNameAndIdNot(String name, UUID excludeId) {
        return jpaRepository.existsByNameAndIdNot(name, excludeId);
    }

    @Override
    public IntegrationFlow save(IntegrationFlow flow) {
        return jpaRepository.save(flow);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public List<IntegrationFlow> findByIsActive(boolean active) {
        return jpaRepository.findByIsActive(active);
    }

    @Override
    public List<IntegrationFlow> findBySourceAdapterId(UUID inboundAdapterId) {
        return jpaRepository.findBySourceAdapterId(inboundAdapterId);
    }

    @Override
    public List<IntegrationFlow> findByTargetAdapterId(UUID outboundAdapterId) {
        return jpaRepository.findByTargetAdapterId(outboundAdapterId);
    }

    @Override
    public void flush() {
        jpaRepository.flush();
    }
}
