package com.integrixs.backend.performance;

import com.github.benmanes.caffeine.cache.Cache;
import com.integrixs.data.model.TransformationRule;
import com.integrixs.data.repository.TransformationRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cached service for transformation rules.
 * Provides caching for transformation rules and compiled scripts.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CachedTransformationService {

    private final TransformationRuleRepository transformationRuleRepository;
    private final Cache<String, Object> transformationCache;

    // Cache for compiled scripts
    private final ConcurrentHashMap<String, CompiledScript> compiledScripts = new ConcurrentHashMap<>();

    private final ScriptEngineManager scriptEngineManager = new ScriptEngineManager();

    /**
     * Get transformation rule by ID with caching.
     */
    @Cacheable(value = "transformationRules", key = "#id.toString()")
    @Transactional(readOnly = true)
    public Optional<TransformationRule> findById(UUID id) {
        log.debug("Loading transformation rule from database: {}", id);
        return transformationRuleRepository.findById(id);
    }

    /**
     * Get transformation rules by flow ID with caching.
     */
    @Cacheable(value = "transformationRules", key = "'flow:' + #flowId.toString()")
    @Transactional(readOnly = true)
    public List<TransformationRule> findByFlowId(UUID flowId) {
        log.debug("Loading transformation rules for flow: {}", flowId);
        return transformationRuleRepository.findByFlowDefinitionId(flowId);
    }

    /**
     * Get compiled transformation script with caching.
     */
    public CompiledScript getCompiledScript(String ruleId, String script, String scriptType) {
        String cacheKey = ruleId + ":" + script.hashCode();

        return compiledScripts.computeIfAbsent(cacheKey, k -> {
            try {
                ScriptEngine engine = scriptEngineManager.getEngineByName(
                    scriptType != null ? scriptType : "javascript");

                if(engine instanceof Compilable) {
                    log.debug("Compiling transformation script for rule: {}", ruleId);
                    return((Compilable) engine).compile(script);
                }

                log.warn("Script engine {} is not compilable", scriptType);
                return null;
            } catch(Exception e) {
                log.error("Failed to compile transformation script for rule: {}", ruleId, e);
                return null;
            }
        });
    }

    /**
     * Save transformation rule and update cache.
     */
    @CachePut(value = "transformationRules", key = "#rule.id.toString()")
    @CacheEvict(value = "transformationRules", key = "'flow:' + #rule.flowDefinition.id.toString()")
    @Transactional
    public TransformationRule save(TransformationRule rule) {
        log.debug("Saving transformation rule: {}", rule.getName());

        // Evict compiled script if exists
        String cacheKey = rule.getId() + ":" + rule.getTransformationLogic().hashCode();
        compiledScripts.remove(cacheKey);

        return transformationRuleRepository.save(rule);
    }

    /**
     * Delete transformation rule and evict from cache.
     */
    @CacheEvict(value = "transformationRules", key = "#id.toString()")
    @Transactional
    public void deleteById(UUID id) {
        log.debug("Deleting transformation rule: {}", id);

        // Remove all compiled scripts for this rule
        compiledScripts.entrySet().removeIf(entry -> entry.getKey().startsWith(id.toString()));

        transformationRuleRepository.deleteById(id);
    }

    /**
     * Clear transformation caches.
     */
    @CacheEvict(value = "transformationRules", allEntries = true)
    public void clearCache() {
        log.info("Clearing all transformation caches");
        compiledScripts.clear();
        transformationCache.invalidateAll();
    }

    /**
     * Get cache statistics.
     */
    public TransformationCacheStats getCacheStats() {
        TransformationCacheStats stats = new TransformationCacheStats();
        stats.setCompiledScriptCount(compiledScripts.size());
        stats.setTransformationCacheSize(transformationCache.estimatedSize());
        stats.setTransformationCacheStats(transformationCache.stats());
        return stats;
    }

    /**
     * Preload frequently used transformations.
     */
    public void preloadCache() {
        log.info("Preloading transformation rules into cache");

        // Load all active transformation rules
        List<TransformationRule> activeRules = transformationRuleRepository.findByIsActiveTrue();
        log.info("Found {} active transformation rules", activeRules.size());

        // Precompile scripts
        int compiledCount = 0;
        for(TransformationRule rule : activeRules) {
            if(rule.getTransformationType() == TransformationRule.TransformationType.SCRIPT) {
                CompiledScript compiled = getCompiledScript(
                    rule.getId().toString(),
                    rule.getTransformationLogic(),
                    "javascript"
               );
                if(compiled != null) {
                    compiledCount++;
                }
            }
        }

        log.info("Precompiled {} transformation scripts", compiledCount);
    }

    @lombok.Data
    public static class TransformationCacheStats {
        private int compiledScriptCount;
        private long transformationCacheSize;
        private com.github.benmanes.caffeine.cache.stats.CacheStats transformationCacheStats;
    }
}
