package com.integrixs.backend.performance;

import com.integrixs.data.model.IntegrationFlow;
import com.integrixs.data.sql.repository.IntegrationFlowSqlRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cached service layer for flow definitions.
 * Provides caching for frequently accessed flow data.
 */
@Service
public class CachedFlowDefinitionService {

    private static final Logger log = LoggerFactory.getLogger(CachedFlowDefinitionService.class);


    private final IntegrationFlowSqlRepository flowDefinitionRepository;

    public CachedFlowDefinitionService(IntegrationFlowSqlRepository flowDefinitionRepository) {
        this.flowDefinitionRepository = flowDefinitionRepository;
    }

    /**
     * Get flow definition by ID with caching.
     */
    @Cacheable(value = "flowDefinitions", key = "#id.toString()")
    public Optional<IntegrationFlow> findById(UUID id) {
        log.debug("Loading flow definition from database: {}", id);
        return flowDefinitionRepository.findById(id);
    }

    /**
     * Check if flow exists by name with caching.
     */
    @Cacheable(value = "flowDefinitions", key = "'exists:' + #name")
    public boolean existsByName(String name) {
        log.debug("Checking if flow exists by name: {}", name);
        return flowDefinitionRepository.existsByName(name);
    }

    /**
     * Get all active flow definitions with caching.
     */
    @Cacheable(value = "flowDefinitions", key = "'active:all'")
    public List<IntegrationFlow> findAllActive() {
        log.debug("Loading all active flow definitions from database");
        return flowDefinitionRepository.findByIsActive(true);
    }

    /**
     * Save flow definition and update cache.
     */
    @CachePut(value = "flowDefinitions", key = "#flow.id.toString()")
    @CacheEvict(value = "flowDefinitions", key = "'active:all'")
    public IntegrationFlow save(IntegrationFlow flow) {
        log.debug("Saving flow definition: {}", flow.getName());
        return flowDefinitionRepository.save(flow);
    }

    /**
     * Delete flow definition and evict from cache.
     */
    @CacheEvict(value = "flowDefinitions", allEntries = true)
    public void deleteById(UUID id) {
        log.debug("Deleting flow definition: {}", id);
        flowDefinitionRepository.deleteById(id);
    }

    /**
     * Update flow definition status with cache eviction.
     */
    @CacheEvict(value = "flowDefinitions", key = "#id.toString()")
    public void updateStatus(UUID id, boolean isActive) {
        log.debug("Updating flow definition status: {} -> {}", id, isActive);
        flowDefinitionRepository.findById(id).ifPresent(flow -> {
            flow.setActive(isActive);
            flowDefinitionRepository.save(flow);
        });
    }

    /**
     * Refresh cache for a specific flow.
     */
    @CachePut(value = "flowDefinitions", key = "#id.toString()")
    public Optional<IntegrationFlow> refreshCache(UUID id) {
        log.debug("Refreshing cache for flow definition: {}", id);
        return flowDefinitionRepository.findById(id);
    }

    /**
     * Clear all flow definition caches.
     */
    @CacheEvict(value = "flowDefinitions", allEntries = true)
    public void clearCache() {
        log.info("Clearing all flow definition caches");
    }

    /**
     * Preload frequently accessed flows into cache.
     */
    public void preloadCache() {
        log.info("Preloading flow definitions into cache");

        // Load all active flows
        List<IntegrationFlow> activeFlows = findAllActive();
        log.info("Preloaded {} active flow definitions", activeFlows.size());

        // Load individual flows to populate ID - based cache
        for(IntegrationFlow flow : activeFlows) {
            findById(flow.getId());
        }
    }

    // Removed updateNameCache as IntegrationFlowSqlRepository doesn't support findByName
}
