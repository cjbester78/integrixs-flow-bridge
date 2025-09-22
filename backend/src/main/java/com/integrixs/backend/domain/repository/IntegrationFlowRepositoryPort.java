package com.integrixs.backend.domain.repository;

import com.integrixs.data.model.IntegrationFlow;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Integration flow repository port - domain layer
 * Acts as a port in hexagonal architecture for integration flow persistence operations
 */
public interface IntegrationFlowRepositoryPort {

    List<IntegrationFlow> findAll();

    Optional<IntegrationFlow> findById(UUID id);

    boolean existsById(UUID id);

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, UUID excludeId);

    IntegrationFlow save(IntegrationFlow flow);

    void deleteById(UUID id);

    List<IntegrationFlow> findByIsActive(boolean active);

    List<IntegrationFlow> findBySourceAdapterId(UUID inboundAdapterId);

    List<IntegrationFlow> findByTargetAdapterId(UUID outboundAdapterId);

    void flush();
}
