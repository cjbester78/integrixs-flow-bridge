package com.integrixs.backend.performance;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Cache configuration for frequently accessed data.
 * Uses Caffeine cache for high - performance caching.
 */
// @Configuration  // Disabled - conflicting with CacheConfig
// @EnableCaching
public class CacheConfiguration {

    @Bean
    @Primary
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();

        cacheManager.setCaches(Arrays.asList(
            // Flow definitions cache - rarely changes
            buildCache("flowDefinitions", 100, 1, TimeUnit.HOURS, true),

            // Adapter configurations - changes occasionally
            buildCache("adapterConfigs", 200, 30, TimeUnit.MINUTES, true),

            // Transformation rules - frequently accessed
            buildCache("transformationRules", 500, 15, TimeUnit.MINUTES, true),

            // External API tokens - short - lived
            buildCache("apiTokens", 100, 5, TimeUnit.MINUTES, false),

            // User permissions - session - based
            buildCache("userPermissions", 1000, 30, TimeUnit.MINUTES, true),

            // Monitoring metrics - very short - lived
            buildCache("monitoringMetrics", 500, 1, TimeUnit.MINUTES, false),

            // Schema definitions - rarely changes
            buildCache("schemaDefinitions", 50, 2, TimeUnit.HOURS, true),

            // Query results - configurable TTL
            buildCache("queryResults", 200, 10, TimeUnit.MINUTES, true),

            // Health check results - short - lived
            buildCache("healthChecks", 100, 30, TimeUnit.SECONDS, false),

            // System configurations - rarely changes
            buildCache("systemConfigs", 50, 1, TimeUnit.HOURS, true),

            // IP whitelist cache - frequently checked
            buildCache("ipWhitelist", 1000, 5, TimeUnit.MINUTES, true)
       ));

        return cacheManager;
    }

    /**
     * Flow definition cache with loading cache support.
     */
    @Bean
    public com.github.benmanes.caffeine.cache.Cache<String, Object> flowDefinitionCache() {
        return Caffeine.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(1, TimeUnit.HOURS)
            .expireAfterAccess(30, TimeUnit.MINUTES)
            .recordStats()
            .build();
    }

    /**
     * Adapter configuration cache with automatic refresh.
     */
    @Bean
    public com.github.benmanes.caffeine.cache.Cache<String, Object> adapterConfigCache() {
        return Caffeine.newBuilder()
            .maximumSize(200)
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .refreshAfterWrite(20, TimeUnit.MINUTES)
            .recordStats()
            .build();
    }

    /**
     * Transformation cache for compiled transformations.
     */
    @Bean
    public com.github.benmanes.caffeine.cache.Cache<String, Object> transformationCache() {
        return Caffeine.newBuilder()
            .maximumSize(500)
            .expireAfterWrite(15, TimeUnit.MINUTES)
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .recordStats()
            .build();
    }

    /**
     * Large payload cache with size - based eviction.
     */
    @Bean
    public com.github.benmanes.caffeine.cache.Cache<String, byte[]> payloadCache() {
        return Caffeine.newBuilder()
            .maximumWeight(100_000_000) // 100MB
            .weigher((String key, byte[] value) -> value.length)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .recordStats()
            .build();
    }

    private CaffeineCache buildCache(String name, int maxSize, long duration,
                                    TimeUnit timeUnit, boolean recordStats) {
        Caffeine<Object, Object> caffeineBuilder = Caffeine.newBuilder()
            .maximumSize(maxSize)
            .expireAfterWrite(duration, timeUnit);

        if(recordStats) {
            caffeineBuilder.recordStats();
        }

        return new CaffeineCache(name, caffeineBuilder.build());
    }
}
