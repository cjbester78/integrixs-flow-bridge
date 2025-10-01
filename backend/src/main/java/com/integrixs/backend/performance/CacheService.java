package com.integrixs.backend.performance;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for managing and monitoring caches.
 * Provides cache statistics, eviction policies, and warming capabilities.
 */
@Service
public class CacheService {

    private static final Logger log = LoggerFactory.getLogger(CacheService.class);


    private final CacheManager cacheManager;
    private final MeterRegistry meterRegistry;
    private final Map<String, Cache<?, ?>> caffeineeCaches;

    // Cache hit/miss tracking
    private final Map<String, AtomicLong> cacheHits = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> cacheMisses = new ConcurrentHashMap<>();

    public CacheService(CacheManager cacheManager, MeterRegistry meterRegistry) {
        this.cacheManager = cacheManager;
        this.meterRegistry = meterRegistry;
        this.caffeineeCaches = new ConcurrentHashMap<>();
    }

    @PostConstruct
    public void init() {
        // Register cache metrics
        Collection<String> cacheNames = cacheManager.getCacheNames();
        for(String cacheName : cacheNames) {
            cacheHits.put(cacheName, new AtomicLong(0));
            cacheMisses.put(cacheName, new AtomicLong(0));

            // Register gauges for monitoring
            Tags tags = Tags.of("cache", cacheName);
            meterRegistry.gauge("cache.hits", tags, cacheHits.get(cacheName));
            meterRegistry.gauge("cache.misses", tags, cacheMisses.get(cacheName));
        }
    }

    /**
     * Get cache statistics for all caches.
     */
    public Map<String, CacheStatistics> getAllCacheStatistics() {
        Map<String, CacheStatistics> allStats = new HashMap<>();

        for(Map.Entry<String, Cache<?, ?>> entry : caffeineeCaches.entrySet()) {
            String cacheName = entry.getKey();
            Cache<?, ?> cache = entry.getValue();

            CacheStatistics stats = getCacheStatistics(cacheName, cache);
            allStats.put(cacheName, stats);
        }

        return allStats;
    }

    /**
     * Get statistics for a specific cache.
     */
    public CacheStatistics getCacheStatistics(String cacheName) {
        Cache<?, ?> cache = caffeineeCaches.get(cacheName);
        if(cache == null) {
            throw new IllegalArgumentException("Cache not found: " + cacheName);
        }

        return getCacheStatistics(cacheName, cache);
    }

    private CacheStatistics getCacheStatistics(String cacheName, Cache<?, ?> cache) {
        CacheStatistics statistics = new CacheStatistics();
        statistics.setCacheName(cacheName);

        // Get Caffeine stats if available
        CacheStats caffeineStats = cache.stats();
        statistics.setHitCount(caffeineStats.hitCount());
        statistics.setMissCount(caffeineStats.missCount());
        statistics.setLoadSuccessCount(caffeineStats.loadSuccessCount());
        statistics.setLoadFailureCount(caffeineStats.loadFailureCount());
        statistics.setTotalLoadTime(caffeineStats.totalLoadTime());
        statistics.setEvictionCount(caffeineStats.evictionCount());
        statistics.setEvictionWeight(caffeineStats.evictionWeight());

        // Calculate rates
        long requestCount = caffeineStats.requestCount();
        if(requestCount > 0) {
            statistics.setHitRate(caffeineStats.hitRate());
            statistics.setMissRate(caffeineStats.missRate());
        }

        // Estimate size
        statistics.setEstimatedSize(cache.estimatedSize());

        return statistics;
    }

    /**
     * Clear a specific cache.
     */
    public void clearCache(String cacheName) {
        org.springframework.cache.Cache cache = cacheManager.getCache(cacheName);
        if(cache != null) {
            cache.clear();
            log.info("Cleared cache: {}", cacheName);
        }

        Cache<?, ?> caffeineCache = caffeineeCaches.get(cacheName);
        if(caffeineCache != null) {
            caffeineCache.invalidateAll();
        }
    }

    /**
     * Clear all caches.
     */
    public void clearAllCaches() {
        for(String cacheName : cacheManager.getCacheNames()) {
            clearCache(cacheName);
        }
        log.info("Cleared all caches");
    }

    /**
     * Evict specific entry from cache.
     */
    public void evictCacheEntry(String cacheName, Object key) {
        org.springframework.cache.Cache cache = cacheManager.getCache(cacheName);
        if(cache != null) {
            cache.evict(key);
            log.debug("Evicted key {} from cache {}", key, cacheName);
        }
    }

    /**
     * Warm up caches with frequently accessed data.
     */
    public void warmUpCaches() {
        log.info("Starting cache warm - up...");

        // Warm up flow definitions cache
        warmUpFlowDefinitionsCache();

        // Warm up adapter configurations cache
        warmUpAdapterConfigCache();

        // Warm up transformation rules cache
        warmUpTransformationCache();

        log.info("Cache warm - up completed");
    }

    private void warmUpFlowDefinitionsCache() {
        // Implementation would load frequently used flow definitions
        log.debug("Warming up flow definitions cache");
    }

    private void warmUpAdapterConfigCache() {
        // Implementation would load active adapter configurations
        log.debug("Warming up adapter configuration cache");
    }

    private void warmUpTransformationCache() {
        // Implementation would pre - compile frequently used transformations
        log.debug("Warming up transformation cache");
    }

    /**
     * Monitor cache effectiveness and log warnings.
     */
    @Scheduled(fixedDelay = 300000) // Every 5 minutes
    public void monitorCacheEffectiveness() {
        for(Map.Entry<String, Cache<?, ?>> entry : caffeineeCaches.entrySet()) {
            String cacheName = entry.getKey();
            Cache<?, ?> cache = entry.getValue();

            CacheStats stats = cache.stats();
            double hitRate = stats.hitRate();

            if(hitRate < 0.5 && stats.requestCount() > 100) {
                log.warn("Low cache hit rate for {}: {:.2f}% (requests: {})",
                        cacheName, hitRate * 100, stats.requestCount());
            }

            if(stats.evictionCount() > cache.estimatedSize()) {
                log.warn("High eviction rate for cache {}: {} evictions, {} current size",
                        cacheName, stats.evictionCount(), cache.estimatedSize());
            }
        }
    }

    /**
     * Get cache recommendations based on usage patterns.
     */
    public CacheRecommendations getRecommendations() {
        CacheRecommendations recommendations = new CacheRecommendations();

        for(Map.Entry<String, Cache<?, ?>> entry : caffeineeCaches.entrySet()) {
            String cacheName = entry.getKey();
            Cache<?, ?> cache = entry.getValue();
            CacheStats stats = cache.stats();

            // Low hit rate recommendation
            if(stats.hitRate() < 0.3 && stats.requestCount() > 100) {
                recommendations.addRecommendation(cacheName,
                    "Consider increasing cache size or TTL due to low hit rate");
            }

            // High eviction rate recommendation
            if(stats.evictionCount() > stats.loadCount() * 2) {
                recommendations.addRecommendation(cacheName,
                    "High eviction rate detected - consider increasing cache size");
            }

            // Unused cache recommendation
            if(stats.requestCount() < 10) {
                recommendations.addRecommendation(cacheName,
                    "Cache is rarely used - consider removing or consolidating");
            }
        }

        return recommendations;
    }

        public static class CacheStatistics {
        private String cacheName;
        private long hitCount;
        private long missCount;
        private long loadSuccessCount;
        private long loadFailureCount;
        private long totalLoadTime;
        private long evictionCount;
        private long evictionWeight;
        private double hitRate;
        private double missRate;
        private long estimatedSize;

        public double getAverageLoadPenalty() {
            long totalLoadCount = loadSuccessCount + loadFailureCount;
            return totalLoadCount > 0 ? (double) totalLoadTime / totalLoadCount : 0;
        }

        // Getters and setters
        public String getCacheName() { return cacheName; }
        public void setCacheName(String cacheName) { this.cacheName = cacheName; }

        public long getHitCount() { return hitCount; }
        public void setHitCount(long hitCount) { this.hitCount = hitCount; }

        public long getMissCount() { return missCount; }
        public void setMissCount(long missCount) { this.missCount = missCount; }

        public long getLoadSuccessCount() { return loadSuccessCount; }
        public void setLoadSuccessCount(long loadSuccessCount) { this.loadSuccessCount = loadSuccessCount; }

        public long getLoadFailureCount() { return loadFailureCount; }
        public void setLoadFailureCount(long loadFailureCount) { this.loadFailureCount = loadFailureCount; }

        public long getTotalLoadTime() { return totalLoadTime; }
        public void setTotalLoadTime(long totalLoadTime) { this.totalLoadTime = totalLoadTime; }

        public long getEvictionCount() { return evictionCount; }
        public void setEvictionCount(long evictionCount) { this.evictionCount = evictionCount; }

        public long getEvictionWeight() { return evictionWeight; }
        public void setEvictionWeight(long evictionWeight) { this.evictionWeight = evictionWeight; }

        public double getHitRate() { return hitRate; }
        public void setHitRate(double hitRate) { this.hitRate = hitRate; }

        public double getMissRate() { return missRate; }
        public void setMissRate(double missRate) { this.missRate = missRate; }

        public long getEstimatedSize() { return estimatedSize; }
        public void setEstimatedSize(long estimatedSize) { this.estimatedSize = estimatedSize; }
    }

        public static class CacheRecommendations {
        private final Map<String, List<String>> recommendations = new HashMap<>();

        public void addRecommendation(String cacheName, String recommendation) {
            recommendations.computeIfAbsent(cacheName, k -> new ArrayList<>())
                .add(recommendation);
        }

        public boolean hasRecommendations() {
            return !recommendations.isEmpty();
        }
    }
}
