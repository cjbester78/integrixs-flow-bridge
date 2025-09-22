package com.integrixs.backend.domain.repository;

import com.integrixs.data.model.CommunicationAdapter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Communication adapter repository port - domain layer
 * Acts as a port in hexagonal architecture for communication adapter persistence operations
 */
public interface CommunicationAdapterRepositoryPort {

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
