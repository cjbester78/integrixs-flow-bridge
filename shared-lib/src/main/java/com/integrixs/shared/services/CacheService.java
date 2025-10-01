package com.integrixs.shared.services;

import java.util.concurrent.TimeUnit;

/**
 * Service for caching data
 */
public interface CacheService {

    /**
     * Get a value from the cache
     * @param key The cache key
     * @param type The expected type of the cached value
     * @return The cached value or null if not found
     */
    <T> T get(String key, Class<T> type);

    /**
     * Put a value in the cache
     * @param key The cache key
     * @param value The value to cache
     */
    void put(String key, Object value);

    /**
     * Put a value in the cache with TTL
     * @param key The cache key
     * @param value The value to cache
     * @param ttl Time to live
     * @param timeUnit The time unit for TTL
     */
    void put(String key, Object value, long ttl, TimeUnit timeUnit);

    /**
     * Remove a value from the cache
     * @param key The cache key
     * @return True if the value was removed, false if not found
     */
    boolean evict(String key);

    /**
     * Clear all cached values
     */
    void evictAll();

    /**
     * Check if a key exists in the cache
     * @param key The cache key
     * @return True if the key exists, false otherwise
     */
    boolean exists(String key);

    /**
     * Get or compute a value if not present
     * @param key The cache key
     * @param type The expected type
     * @param supplier Supplier to compute value if not present
     * @return The cached or computed value
     */
    <T> T getOrCompute(String key, Class<T> type, java.util.function.Supplier<T> supplier);
}
