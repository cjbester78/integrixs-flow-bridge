package com.integrixs.backend.performance;

import com.integrixs.data.model.FieldMapping;
import com.integrixs.data.sql.repository.FieldMappingSqlRepository;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cached service for transformation rules.
 * Provides caching for transformation rules and compiled scripts.
 */
@Service
public class CachedTransformationService {

    private static final Logger log = LoggerFactory.getLogger(CachedTransformationService.class);

    private final FieldMappingSqlRepository transformationRuleRepository;
    private final Cache transformationCache;  // Spring Cache abstraction

    // Cache for compiled scripts
    private final ConcurrentHashMap<String, CompiledScript> compiledScripts = new ConcurrentHashMap<>();

    private final ScriptEngineManager scriptEngineManager = new ScriptEngineManager();

    // Constructor
    public CachedTransformationService(FieldMappingSqlRepository transformationRuleRepository,
                                       CacheManager cacheManager) {
        this.transformationRuleRepository = transformationRuleRepository;
        this.transformationCache = cacheManager.getCache("transformationRules");
    }

    /**
     * Get transformation rule by ID with caching.
     */
    @Cacheable(value = "transformationRules", key = "#id.toString()")
    public Optional<FieldMapping> findById(UUID id) {
        log.debug("Loading transformation rule from database: {}", id);
        return transformationRuleRepository.findById(id);
    }

    /**
     * Get transformation rules by flow ID with caching.
     */
    @Cacheable(value = "transformationRules", key = "'flow:' + #flowId.toString()")
    public List<FieldMapping> findByFlowId(UUID flowId) {
        log.debug("Loading transformation rules for flow: {}", flowId);
        return new ArrayList<>();
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

                if (engine instanceof Compilable) {
                    log.debug("Compiling transformation script for rule: {}", ruleId);
                    return ((Compilable) engine).compile(script);
                }

                log.warn("Script engine {} is not compilable", scriptType);
                return null;
            } catch (Exception e) {
                log.error("Failed to compile transformation script for rule: {}", ruleId, e);
                return null;
            }
        });
    }

    /**
     * Save transformation rule and update cache.
     */
    @CachePut(value = "transformationRules", key = "#rule.id.toString()")
    @CacheEvict(value = "transformationRules", key = "'flow:' + #rule.transformation.id.toString()")
    public FieldMapping save(FieldMapping rule) {
        log.debug("Saving transformation rule: {}", rule.getId());

        // Evict compiled script if exists
        if (rule.getJavaFunction() != null) {
            String cacheKey = rule.getId() + ":" + rule.getJavaFunction().hashCode();
            compiledScripts.remove(cacheKey);
        }

        return transformationRuleRepository.save(rule);
    }

    /**
     * Delete transformation rule and evict from cache.
     */
    @CacheEvict(value = "transformationRules", key = "#id.toString()")
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
        if (transformationCache != null) {
            transformationCache.clear(); // uses Spring's Cache abstraction
        }
    }

    /**
     * Get cache statistics.
     */
    public TransformationCacheStats getCacheStats() {
        TransformationCacheStats stats = new TransformationCacheStats();
        stats.setCompiledScriptCount(compiledScripts.size());

        if (transformationCache != null && transformationCache.getNativeCache() instanceof com.github.benmanes.caffeine.cache.Cache) {
            var nativeCache = (com.github.benmanes.caffeine.cache.Cache<?, ?>) transformationCache.getNativeCache();
            stats.setTransformationCacheSize(nativeCache.estimatedSize());
            stats.setTransformationCacheStats(nativeCache.stats());
        }
        return stats;
    }

    /**
     * Preload frequently used transformations.
     */
    public void preloadCache() {
        log.info("Preloading transformation rules into cache");

        List<FieldMapping> activeRules = transformationRuleRepository.findByIsActiveTrue();
        log.info("Found {} active transformation rules", activeRules.size());

        int compiledCount = 0;
        for (FieldMapping rule : activeRules) {
            if (rule.getJavaFunction() != null && !rule.getJavaFunction().isEmpty()) {
                CompiledScript compiled = getCompiledScript(
                        rule.getId().toString(),
                        rule.getJavaFunction(),
                        "javascript"
                );
                if (compiled != null) {
                    compiledCount++;
                }
            }
        }

        log.info("Precompiled {} transformation scripts", compiledCount);
    }

    public static class TransformationCacheStats {
        private int compiledScriptCount;
        private long transformationCacheSize;
        private com.github.benmanes.caffeine.cache.stats.CacheStats transformationCacheStats;

        public int getCompiledScriptCount() { return compiledScriptCount; }
        public void setCompiledScriptCount(int compiledScriptCount) { this.compiledScriptCount = compiledScriptCount; }

        public long getTransformationCacheSize() { return transformationCacheSize; }
        public void setTransformationCacheSize(long transformationCacheSize) { this.transformationCacheSize = transformationCacheSize; }

        public com.github.benmanes.caffeine.cache.stats.CacheStats getTransformationCacheStats() { return transformationCacheStats; }
        public void setTransformationCacheStats(com.github.benmanes.caffeine.cache.stats.CacheStats transformationCacheStats) { this.transformationCacheStats = transformationCacheStats; }
    }
}
