package com.integrixs.backend.infrastructure.persistence;

import com.integrixs.backend.domain.repository.BusinessComponentRepository;
import com.integrixs.data.model.BusinessComponent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of BusinessComponentRepository using JPA
 * Bridges between domain repository interface and JPA repository
 */
@Repository("domainBusinessComponentRepository")
@RequiredArgsConstructor
public class BusinessComponentRepositoryImpl implements BusinessComponentRepository {

    private final com.integrixs.data.repository.BusinessComponentRepository jpaRepository;
    private final com.integrixs.data.repository.CommunicationAdapterRepository adapterRepository;

    @Override
    public List<BusinessComponent> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public Optional<BusinessComponent> findById(UUID id) {
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
    public BusinessComponent save(BusinessComponent component) {
        return jpaRepository.save(component);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public long countAssociatedAdapters(UUID componentId) {
        return adapterRepository.countByBusinessComponent_Id(componentId);
    }
}
