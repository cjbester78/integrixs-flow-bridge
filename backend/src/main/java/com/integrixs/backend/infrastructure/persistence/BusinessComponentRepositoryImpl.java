package com.integrixs.backend.infrastructure.persistence;

import com.integrixs.backend.domain.repository.BusinessComponentRepositoryPort;
import com.integrixs.data.model.BusinessComponent;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of BusinessComponentSqlRepository using native SQL
 * Bridges between domain repository interface and SQL repository
 */
@Repository("domainBusinessComponentRepository")
public class BusinessComponentRepositoryImpl implements BusinessComponentRepositoryPort {

    private final com.integrixs.data.sql.repository.BusinessComponentSqlRepository sqlRepository;
    private final com.integrixs.data.sql.repository.CommunicationAdapterSqlRepository adapterRepository;

    public BusinessComponentRepositoryImpl(com.integrixs.data.sql.repository.BusinessComponentSqlRepository sqlRepository,
                                          com.integrixs.data.sql.repository.CommunicationAdapterSqlRepository adapterRepository) {
        this.sqlRepository = sqlRepository;
        this.adapterRepository = adapterRepository;
    }

    @Override
    public List<BusinessComponent> findAll() {
        return sqlRepository.findAll();
    }

    @Override
    public Optional<BusinessComponent> findById(UUID id) {
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
    public BusinessComponent save(BusinessComponent component) {
        return sqlRepository.save(component);
    }

    @Override
    public void deleteById(UUID id) {
        sqlRepository.deleteById(id);
    }

    @Override
    public long countAssociatedAdapters(UUID componentId) {
        return adapterRepository.countByBusinessComponent_Id(componentId);
    }
}
