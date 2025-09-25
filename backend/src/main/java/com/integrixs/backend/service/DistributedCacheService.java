package com.integrixs.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Service for distributed caching operations using Redis
 */
@Service
@ConditionalOnProperty(name = "spring.redis.enabled", havingValue = "true", matchIfMissing = false)
public class DistributedCacheService {

    private static final Logger logger = LoggerFactory.getLogger(DistributedCacheService.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final ValueOperations<String, Object> valueOps;
    private final HashOperations<String, String, Object> hashOps;
    private final SetOperations<String, Object> setOps;
    private final ZSetOperations<String, Object> zSetOps;

    public DistributedCacheService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.valueOps = redisTemplate.opsForValue();
        this.hashOps = redisTemplate.opsForHash();
        this.setOps = redisTemplate.opsForSet();
        this.zSetOps = redisTemplate.opsForZSet();
    }

    // ========== Key - Value Operations ==========

    /**
     * Set a value with TTL
     */
    public void set(String key, Object value, Duration ttl) {
        try {
            valueOps.set(key, value, ttl);
            logger.debug("Cached value for key: {}", key);
        } catch(Exception e) {
            logger.error("Error caching value for key: {}", key, e);
        }
    }

    /**
     * Set a value without TTL
     */
    public void set(String key, Object value) {
        try {
            valueOps.set(key, value);
            logger.debug("Cached value for key: {}", key);
        } catch(Exception e) {
            logger.error("Error caching value for key: {}", key, e);
        }
    }

    /**
     * Get a value
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        try {
            Object value = valueOps.get(key);
            if(value != null && type.isInstance(value)) {
                return(T) value;
            }
            return null;
        } catch(Exception e) {
            logger.error("Error retrieving value for key: {}", key, e);
            return null;
        }
    }

    /**
     * Get multiple values
     */
    public List<Object> multiGet(Collection<String> keys) {
        try {
            return valueOps.multiGet(keys);
        } catch(Exception e) {
            logger.error("Error retrieving multiple values", e);
            return List.of();
        }
    }

    /**
     * Delete a key
     */
    public boolean delete(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.delete(key));
        } catch(Exception e) {
            logger.error("Error deleting key: {}", key, e);
            return false;
        }
    }

    /**
     * Delete multiple keys
     */
    public long deleteAll(Collection<String> keys) {
        try {
            Long count = redisTemplate.delete(keys);
            return count != null ? count : 0;
        } catch(Exception e) {
            logger.error("Error deleting multiple keys", e);
            return 0;
        }
    }

    /**
     * Check if key exists
     */
    public boolean exists(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch(Exception e) {
            logger.error("Error checking key existence: {}", key, e);
            return false;
        }
    }

    /**
     * Set expiration time for a key
     */
    public boolean expire(String key, Duration ttl) {
        try {
            return Boolean.TRUE.equals(redisTemplate.expire(key, ttl));
        } catch(Exception e) {
            logger.error("Error setting expiration for key: {}", key, e);
            return false;
        }
    }

    // ========== Hash Operations ==========

    /**
     * Set hash field
     */
    public void hashSet(String key, String field, Object value) {
        try {
            hashOps.put(key, field, value);
            logger.debug("Set hash field {} in key: {}", field, key);
        } catch(Exception e) {
            logger.error("Error setting hash field {} in key: {}", field, key, e);
        }
    }

    /**
     * Set multiple hash fields
     */
    public void hashSetAll(String key, Map<String, Object> fields) {
        try {
            hashOps.putAll(key, fields);
            logger.debug("Set {} hash fields in key: {}", fields.size(), key);
        } catch(Exception e) {
            logger.error("Error setting hash fields in key: {}", key, e);
        }
    }

    /**
     * Get hash field
     */
    @SuppressWarnings("unchecked")
    public <T> T hashGet(String key, String field, Class<T> type) {
        try {
            Object value = hashOps.get(key, field);
            if(value != null && type.isInstance(value)) {
                return(T) value;
            }
            return null;
        } catch(Exception e) {
            logger.error("Error getting hash field {} from key: {}", field, key, e);
            return null;
        }
    }

    /**
     * Get all hash fields
     */
    public Map<String, Object> hashGetAll(String key) {
        try {
            return hashOps.entries(key);
        } catch(Exception e) {
            logger.error("Error getting all hash fields from key: {}", key, e);
            return Map.of();
        }
    }

    /**
     * Delete hash fields
     */
    public long hashDelete(String key, String... fields) {
        try {
            return hashOps.delete(key, (Object[]) fields);
        } catch(Exception e) {
            logger.error("Error deleting hash fields from key: {}", key, e);
            return 0;
        }
    }

    // ========== Set Operations ==========

    /**
     * Add to set
     */
    public long setAdd(String key, Object... values) {
        try {
            Long count = setOps.add(key, values);
            return count != null ? count : 0;
        } catch(Exception e) {
            logger.error("Error adding to set: {}", key, e);
            return 0;
        }
    }

    /**
     * Remove from set
     */
    public long setRemove(String key, Object... values) {
        try {
            Long count = setOps.remove(key, values);
            return count != null ? count : 0;
        } catch(Exception e) {
            logger.error("Error removing from set: {}", key, e);
            return 0;
        }
    }

    /**
     * Check if member exists in set
     */
    public boolean setIsMember(String key, Object value) {
        try {
            return Boolean.TRUE.equals(setOps.isMember(key, value));
        } catch(Exception e) {
            logger.error("Error checking set membership: {}", key, e);
            return false;
        }
    }

    /**
     * Get all set members
     */
    public Set<Object> setMembers(String key) {
        try {
            return setOps.members(key);
        } catch(Exception e) {
            logger.error("Error getting set members: {}", key, e);
            return Set.of();
        }
    }

    // ========== Sorted Set Operations ==========

    /**
     * Add to sorted set
     */
    public boolean zSetAdd(String key, Object value, double score) {
        try {
            return Boolean.TRUE.equals(zSetOps.add(key, value, score));
        } catch(Exception e) {
            logger.error("Error adding to sorted set: {}", key, e);
            return false;
        }
    }

    /**
     * Get range from sorted set
     */
    public Set<Object> zSetRange(String key, long start, long end) {
        try {
            return zSetOps.range(key, start, end);
        } catch(Exception e) {
            logger.error("Error getting sorted set range: {}", key, e);
            return Set.of();
        }
    }

    /**
     * Remove range by score
     */
    public long zSetRemoveRangeByScore(String key, double min, double max) {
        try {
            Long count = zSetOps.removeRangeByScore(key, min, max);
            return count != null ? count : 0;
        } catch(Exception e) {
            logger.error("Error removing sorted set range: {}", key, e);
            return 0;
        }
    }

    // ========== Atomic Operations ==========

    /**
     * Increment value
     */
    public long increment(String key, long delta) {
        try {
            Long value = valueOps.increment(key, delta);
            return value != null ? value : 0;
        } catch(Exception e) {
            logger.error("Error incrementing key: {}", key, e);
            return 0;
        }
    }

    /**
     * Decrement value
     */
    public long decrement(String key, long delta) {
        try {
            Long value = valueOps.increment(key, -delta);
            return value != null ? value : 0;
        } catch(Exception e) {
            logger.error("Error decrementing key: {}", key, e);
            return 0;
        }
    }

    // ========== Pattern Operations ==========

    /**
     * Find keys by pattern
     */
    public Set<String> keys(String pattern) {
        try {
            return redisTemplate.keys(pattern);
        } catch(Exception e) {
            logger.error("Error finding keys by pattern: {}", pattern, e);
            return Set.of();
        }
    }

    /**
     * Clear all keys with pattern
     */
    public long clearPattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if(keys != null && !keys.isEmpty()) {
                Long count = redisTemplate.delete(keys);
                return count != null ? count : 0;
            }
            return 0;
        } catch(Exception e) {
            logger.error("Error clearing keys by pattern: {}", pattern, e);
            return 0;
        }
    }

    // ========== Distributed Lock Operations ==========

    /**
     * Try to acquire a distributed lock
     */
    public boolean tryLock(String lockKey, String lockValue, Duration timeout) {
        try {
            Boolean acquired = valueOps.setIfAbsent(lockKey, lockValue, timeout);
            return Boolean.TRUE.equals(acquired);
        } catch(Exception e) {
            logger.error("Error acquiring lock: {}", lockKey, e);
            return false;
        }
    }

    /**
     * Release a distributed lock
     */
    public boolean releaseLock(String lockKey, String lockValue) {
        try {
            Object currentValue = valueOps.get(lockKey);
            if(lockValue.equals(currentValue)) {
                return Boolean.TRUE.equals(redisTemplate.delete(lockKey));
            }
            return false;
        } catch(Exception e) {
            logger.error("Error releasing lock: {}", lockKey, e);
            return false;
        }
    }

    /**
     * Execute with distributed lock
     */
    public <T> T executeWithLock(String lockKey, Duration lockTimeout,
                                 DistributedOperation<T> operation) {
        String lockValue = generateLockValue();
        boolean acquired = tryLock(lockKey, lockValue, lockTimeout);

        if(!acquired) {
            logger.warn("Failed to acquire lock: {}", lockKey);
            return null;
        }

        try {
            return operation.execute();
        } catch (Exception e) {
            logger.error("Error executing operation with lock: {}", lockKey, e);
            throw new RuntimeException("Failed to execute operation with lock", e);
        } finally {
            releaseLock(lockKey, lockValue);
        }
    }

    /**
     * Generate unique lock value
     */
    private String generateLockValue() {
        return Thread.currentThread().getName() + "-" + System.nanoTime();
    }

    /**
     * Functional interface for distributed operations
     */
    @FunctionalInterface
    public interface DistributedOperation<T> {
        T execute() throws Exception;
    }
}
