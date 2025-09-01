package com.integrixs.backend.domain.repository;

import com.integrixs.data.model.IntegrationFlow;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository interface for integration flows
 */
public interface IntegrationFlowRepository {
    
    List<IntegrationFlow> findAll();
    
    Optional<IntegrationFlow> findById(UUID id);
    
    boolean existsById(UUID id);
    
    boolean existsByName(String name);
    
    boolean existsByNameAndIdNot(String name, UUID excludeId);
    
    IntegrationFlow save(IntegrationFlow flow);
    
    void deleteById(UUID id);
    
    List<IntegrationFlow> findByIsActive(boolean active);
    
    List<IntegrationFlow> findBySourceAdapterId(UUID sourceAdapterId);
    
    List<IntegrationFlow> findByTargetAdapterId(UUID targetAdapterId);
    
    void flush();
}