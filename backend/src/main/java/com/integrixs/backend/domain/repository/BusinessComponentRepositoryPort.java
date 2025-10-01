package com.integrixs.backend.domain.repository;

import com.integrixs.data.model.BusinessComponent;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository interface for business components
 */
public interface BusinessComponentRepositoryPort {

    List<BusinessComponent> findAll();

    Optional<BusinessComponent> findById(UUID id);

    boolean existsById(UUID id);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, UUID excludeId);

    BusinessComponent save(BusinessComponent component);

    void deleteById(UUID id);

    long countAssociatedAdapters(UUID componentId);
}
