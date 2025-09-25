package com.integrixs.backend.infrastructure.persistence;

import com.integrixs.backend.domain.repository.CommunicationAdapterRepositoryPort;
import com.integrixs.data.model.CommunicationAdapter;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of CommunicationAdapterSqlRepository using SQL
 * Bridges between domain repository interface and SQL repository
 */
@Repository("domainCommunicationAdapterRepository")
public class CommunicationAdapterRepositoryImpl implements CommunicationAdapterRepositoryPort {

    private final com.integrixs.data.sql.repository.CommunicationAdapterSqlRepository sqlRepository;

    public CommunicationAdapterRepositoryImpl(com.integrixs.data.sql.repository.CommunicationAdapterSqlRepository sqlRepository) {
        this.sqlRepository = sqlRepository;
    }

    @Override
    public List<CommunicationAdapter> findAll() {
        return sqlRepository.findAll();
    }

    @Override
    public Optional<CommunicationAdapter> findById(UUID id) {
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
        // Custom implementation for this method
        return sqlRepository.findAll().stream()
                .anyMatch(adapter -> adapter.getName().equals(name) && !adapter.getId().equals(excludeId));
    }

    @Override
    public CommunicationAdapter save(CommunicationAdapter adapter) {
        return sqlRepository.save(adapter);
    }

    @Override
    public void deleteById(UUID id) {
        sqlRepository.deleteById(id);
    }

    @Override
    public List<CommunicationAdapter> findByActive(boolean active) {
        return sqlRepository.findByIsActiveTrue();
    }

    @Override
    public List<CommunicationAdapter> findByBusinessComponentId(UUID businessComponentId) {
        return sqlRepository.findByBusinessComponent_Id(businessComponentId);
    }

    @Override
    public List<CommunicationAdapter> findByType(String type) {
        return sqlRepository.findAll().stream()
                .filter(adapter -> adapter.getType().name().equals(type))
                .collect(Collectors.toList());
    }

    @Override
    public List<CommunicationAdapter> findByMode(String mode) {
        return sqlRepository.findAll().stream()
                .filter(adapter -> adapter.getMode().name().equals(mode))
                .collect(Collectors.toList());
    }

    @Override
    public long countByBusinessComponentId(UUID businessComponentId) {
        return sqlRepository.findByBusinessComponent_Id(businessComponentId).size();
    }
}
