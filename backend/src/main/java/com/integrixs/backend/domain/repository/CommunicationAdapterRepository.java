package com.integrixs.backend.domain.repository;

import com.integrixs.data.model.CommunicationAdapter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository interface for communication adapters
 */
public interface CommunicationAdapterRepository {

    List<CommunicationAdapter> findAll();

    Optional<CommunicationAdapter> findById(UUID id);

    boolean existsById(UUID id);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, UUID excludeId);

    CommunicationAdapter save(CommunicationAdapter adapter);

    void deleteById(UUID id);

    List<CommunicationAdapter> findByActive(boolean active);

    List<CommunicationAdapter> findByBusinessComponentId(UUID businessComponentId);

    List<CommunicationAdapter> findByType(String type);

    List<CommunicationAdapter> findByMode(String mode);

    long countByBusinessComponentId(UUID businessComponentId);
}
