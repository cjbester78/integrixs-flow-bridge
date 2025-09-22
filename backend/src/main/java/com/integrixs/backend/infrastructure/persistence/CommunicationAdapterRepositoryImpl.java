package com.integrixs.backend.infrastructure.persistence;

import com.integrixs.backend.domain.repository.CommunicationAdapterRepositoryPort;
import com.integrixs.data.model.CommunicationAdapter;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of CommunicationAdapterRepository using JPA
 * Bridges between domain repository interface and JPA repository
 */
@Repository("domainCommunicationAdapterRepository")
public class CommunicationAdapterRepositoryImpl implements CommunicationAdapterRepositoryPort {

    private final com.integrixs.data.repository.CommunicationAdapterRepository jpaRepository;

    public CommunicationAdapterRepositoryImpl(com.integrixs.data.repository.CommunicationAdapterRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<CommunicationAdapter> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public Optional<CommunicationAdapter> findById(UUID id) {
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
        // Since this method doesn't exist in JPA repo, we need to implement it
        return jpaRepository.findAll().stream()
                .anyMatch(adapter -> adapter.getName().equals(name) && !adapter.getId().equals(excludeId));
    }

    @Override
    public CommunicationAdapter save(CommunicationAdapter adapter) {
        return jpaRepository.save(adapter);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public List<CommunicationAdapter> findByActive(boolean active) {
        return jpaRepository.findByIsActiveTrue();
    }

    @Override
    public List<CommunicationAdapter> findByBusinessComponentId(UUID businessComponentId) {
        return jpaRepository.findByBusinessComponent_Id(businessComponentId);
    }

    @Override
    public List<CommunicationAdapter> findByType(String type) {
        return jpaRepository.findAll().stream()
                .filter(adapter -> adapter.getType().name().equals(type))
                .collect(Collectors.toList());
    }

    @Override
    public List<CommunicationAdapter> findByMode(String mode) {
        return jpaRepository.findAll().stream()
                .filter(adapter -> adapter.getMode().name().equals(mode))
                .collect(Collectors.toList());
    }

    @Override
    public long countByBusinessComponentId(UUID businessComponentId) {
        return jpaRepository.findByBusinessComponent_Id(businessComponentId).size();
    }
}
