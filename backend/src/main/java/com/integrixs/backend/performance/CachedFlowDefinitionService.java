package com.integrixs.backend.performance;

import com.integrixs.data.model.FlowDefinition;
import com.integrixs.data.repository.FlowDefinitionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Cached service layer for flow definitions.
 * Provides caching for frequently accessed flow data.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CachedFlowDefinitionService {

    private final FlowDefinitionRepository flowDefinitionRepository;

    /**
     * Get flow definition by ID with caching.
     */
    @Cacheable(value = "flowDefinitions", key = "#id.toString()")
    @Transactional(readOnly = true)
    public Optional<FlowDefinition> findById(UUID id) {
        log.debug("Loading flow definition from database: {}", id);
        return flowDefinitionRepository.findById(id);
    }

    /**
     * Get flow definition by name with caching.
     */
    @Cacheable(value = "flowDefinitions", key = "'name:' + #name")
    @Transactional(readOnly = true)
    public Optional<FlowDefinition> findByName(String name) {
        log.debug("Loading flow definition by name from database: {}", name);
        return flowDefinitionRepository.findByName(name);
    }

    /**
     * Get all active flow definitions with caching.
     */
    @Cacheable(value = "flowDefinitions", key = "'active:all'")
    @Transactional(readOnly = true)
    public List<FlowDefinition> findAllActive() {
        log.debug("Loading all active flow definitions from database");
        return flowDefinitionRepository.findByIsActiveTrue();
    }

    /**
     * Save flow definition and update cache.
     */
    @CachePut(value = "flowDefinitions", key = "#flowDefinition.id.toString()")
    @CacheEvict(value = "flowDefinitions", key = "'active:all'")
    @Transactional
    public FlowDefinition save(FlowDefinition flowDefinition) {
        log.debug("Saving flow definition: {}", flowDefinition.getName());
        FlowDefinition saved = flowDefinitionRepository.save(flowDefinition);

        // Also update the name - based cache
        if(saved.getName() != null) {
            updateNameCache(saved);
        }

        return saved;
    }

    /**
     * Delete flow definition and evict from cache.
     */
    @CacheEvict(value = "flowDefinitions", allEntries = true)
    @Transactional
    public void deleteById(UUID id) {
        log.debug("Deleting flow definition: {}", id);
        flowDefinitionRepository.deleteById(id);
    }

    /**
     * Update flow definition status with cache eviction.
     */
    @CacheEvict(value = "flowDefinitions", key = "#id.toString()")
    @Transactional
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
    public Optional<FlowDefinition> refreshCache(UUID id) {
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
        List<FlowDefinition> activeFlows = findAllActive();
        log.info("Preloaded {} active flow definitions", activeFlows.size());

        // Load individual flows to populate ID - based cache
        for(FlowDefinition flow : activeFlows) {
            findById(flow.getId());
        }
    }

    @CachePut(value = "flowDefinitions", key = "'name:' + #flow.name")
    private FlowDefinition updateNameCache(FlowDefinition flow) {
        return flow;
    }
}
