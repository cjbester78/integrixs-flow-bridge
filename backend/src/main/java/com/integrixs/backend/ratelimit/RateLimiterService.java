package com.integrixs.backend.ratelimit;

import com.integrixs.backend.service.DistributedCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

/**
 * Service for API rate limiting using token bucket algorithm
 */
@Service
public class RateLimiterService implements com.integrixs.shared.services.RateLimiterService {

    private static final Logger logger = LoggerFactory.getLogger(RateLimiterService.class);

    @Autowired(required = false)
    private DistributedCacheService cacheService;

    @Value("${api.ratelimit.default.capacity:100}")
    private int defaultCapacity;

    @Value("${api.ratelimit.default.refill-rate:10}")
    private int defaultRefillRate;

    @Value("${api.ratelimit.default.refill-period:60}")
    private int defaultRefillPeriod;

    // Local cache for when Redis is not available
    private final Map<String, TokenBucket> localBuckets = new ConcurrentHashMap<>();

    /**
     * Check if request is allowed based on rate limit
     */
    public RateLimitResult checkRateLimit(String key, RateLimitConfig config) {
        if(config == null) {
            config = getDefaultConfig();
        }

        if(cacheService != null) {
            return checkDistributedRateLimit(key, config);
        } else {
            return checkLocalRateLimit(key, config);
        }
    }

    /**
     * Check rate limit for specific user
     */
    public RateLimitResult checkUserRateLimit(String userId, String resource) {
        String key = "ratelimit:user:" + userId + ":" + resource;
        RateLimitConfig config = getUserRateLimitConfig(userId, resource);
        return checkRateLimit(key, config);
    }

    /**
     * Check rate limit for IP address
     */
    public RateLimitResult checkIpRateLimit(String ipAddress, String endpoint) {
        String key = "ratelimit:ip:" + ipAddress + ":" + endpoint;
        RateLimitConfig config = getIpRateLimitConfig(endpoint);
        return checkRateLimit(key, config);
    }

    /**
     * Check rate limit for API key
     */
    public RateLimitResult checkApiKeyRateLimit(String apiKey) {
        String key = "ratelimit:apikey:" + apiKey;
        RateLimitConfig config = getApiKeyRateLimitConfig(apiKey);
        return checkRateLimit(key, config);
    }

    /**
     * Reset rate limit for a key
     */
    public void resetRateLimit(String key) {
        if(cacheService != null) {
            cacheService.delete(key);
        } else {
            localBuckets.remove(key);
        }
        logger.info("Reset rate limit for key: {}", key);
    }

    /**
     * Get current token count for a key
     */
    public int getAvailableTokens(String key) {
        if(cacheService != null) {
            TokenBucket bucket = cacheService.get(key, TokenBucket.class);
            if(bucket != null) {
                bucket.refill();
                return(int) bucket.getTokens();
            }
        } else {
            TokenBucket bucket = localBuckets.get(key);
            if(bucket != null) {
                bucket.refill();
                return(int) bucket.getTokens();
            }
        }
        return defaultCapacity;
    }

    /**
     * Check distributed rate limit using Redis
     */
    private RateLimitResult checkDistributedRateLimit(String key, RateLimitConfig config) {
        String lockKey = key + ":lock";

        return cacheService.executeWithLock(lockKey, Duration.ofMillis(100), () -> {
            TokenBucket bucket = cacheService.get(key, TokenBucket.class);

            if(bucket == null) {
                bucket = new TokenBucket(config);
            }

            bucket.refill();
            boolean allowed = bucket.tryConsume();

            // Update bucket in cache
            cacheService.set(key, bucket, Duration.ofMinutes(5));

            return new RateLimitResult(
                allowed,
                bucket.getTokens(),
                config.getCapacity(),
                bucket.getNextRefillTime()
           );
        });
    }

    /**
     * Check local rate limit
     */
    private RateLimitResult checkLocalRateLimit(String key, RateLimitConfig config) {
        TokenBucket bucket = localBuckets.computeIfAbsent(key, k -> new TokenBucket(config));

        synchronized(bucket) {
            bucket.refill();
            boolean allowed = bucket.tryConsume();

            return new RateLimitResult(
                allowed,
                bucket.getTokens(),
                config.getCapacity(),
                bucket.getNextRefillTime()
           );
        }
    }

    /**
     * Get rate limit configuration for user
     */
    private RateLimitConfig getUserRateLimitConfig(String userId, String resource) {
        // In production, load from database or configuration
        // For now, return enhanced limits for authenticated users
        return RateLimitConfig.builder()
            .capacity(200)
            .refillTokens(20)
            .refillPeriod(60)
            .refillUnit(TimeUnit.SECONDS)
            .build();
    }

    /**
     * Get rate limit configuration for IP
     */
    private RateLimitConfig getIpRateLimitConfig(String endpoint) {
        // Different limits for different endpoints
        if(endpoint.startsWith("/api/auth")) {
            return RateLimitConfig.builder()
                .capacity(10)
                .refillTokens(1)
                .refillPeriod(60)
                .refillUnit(TimeUnit.SECONDS)
                .build();
        }

        return getDefaultConfig();
    }

    /**
     * Get rate limit configuration for API key
     */
    private RateLimitConfig getApiKeyRateLimitConfig(String apiKey) {
        // In production, load from API key configuration
        return RateLimitConfig.builder()
            .capacity(1000)
            .refillTokens(100)
            .refillPeriod(60)
            .refillUnit(TimeUnit.SECONDS)
            .build();
    }

    /**
     * Get default rate limit configuration
     */
    private RateLimitConfig getDefaultConfig() {
        return RateLimitConfig.builder()
            .capacity(defaultCapacity)
            .refillTokens(defaultRefillRate)
            .refillPeriod(defaultRefillPeriod)
            .refillUnit(TimeUnit.SECONDS)
            .build();
    }

    /**
     * Token bucket implementation for rate limiting
     */
    public static class TokenBucket {
        private double tokens;
        private final int capacity;
        private final int refillTokens;
        private final long refillPeriodMillis;
        private long lastRefillTimestamp;

        public TokenBucket(RateLimitConfig config) {
            this.capacity = config.getCapacity();
            this.tokens = capacity;
            this.refillTokens = config.getRefillTokens();
            this.refillPeriodMillis = config.getRefillUnit().toMillis(config.getRefillPeriod());
            this.lastRefillTimestamp = System.currentTimeMillis();
        }

        public synchronized void refill() {
            long now = System.currentTimeMillis();
            long timeSinceLastRefill = now-lastRefillTimestamp;

            if(timeSinceLastRefill >= refillPeriodMillis) {
                long periodsElapsed = timeSinceLastRefill / refillPeriodMillis;
                double tokensToAdd = periodsElapsed * refillTokens;
                tokens = Math.min(capacity, tokens + tokensToAdd);
                lastRefillTimestamp = now-(timeSinceLastRefill % refillPeriodMillis);
            }
        }

        public synchronized boolean tryConsume() {
            if(tokens >= 1) {
                tokens--;
                return true;
            }
            return false;
        }

        public synchronized boolean tryConsume(int count) {
            if(tokens >= count) {
                tokens -= count;
                return true;
            }
            return false;
        }

        public double getTokens() {
            return tokens;
        }

        public long getNextRefillTime() {
            return lastRefillTimestamp + refillPeriodMillis;
        }
    }

    /**
     * Rate limit result
     */
    public static class RateLimitResult {
        private final boolean allowed;
        private final double remainingTokens;
        private final int limit;
        private final long resetTime;

        public RateLimitResult(boolean allowed, double remainingTokens, int limit, long resetTime) {
            this.allowed = allowed;
            this.remainingTokens = remainingTokens;
            this.limit = limit;
            this.resetTime = resetTime;
        }

        public boolean isAllowed() {
            return allowed;
        }

        public int getRemainingRequests() {
            return(int) remainingTokens;
        }

        public int getLimit() {
            return limit;
        }

        public long getResetTime() {
            return resetTime;
        }

        public long getRetryAfter() {
            if(allowed) {
                return 0;
            }
            return Math.max(0, resetTime-System.currentTimeMillis()) / 1000;
        }
    }

    /**
     * Rate limit configuration
     */
    public static class RateLimitConfig {
        private final int capacity;
        private final int refillTokens;
        private final int refillPeriod;
        private final TimeUnit refillUnit;

        private RateLimitConfig(Builder builder) {
            this.capacity = builder.capacity;
            this.refillTokens = builder.refillTokens;
            this.refillPeriod = builder.refillPeriod;
            this.refillUnit = builder.refillUnit;
        }

        public static Builder builder() {
            return new Builder();
        }

        public int getCapacity() {
            return capacity;
        }

        public int getRefillTokens() {
            return refillTokens;
        }

        public int getRefillPeriod() {
            return refillPeriod;
        }

        public TimeUnit getRefillUnit() {
            return refillUnit;
        }

        public static class Builder {
            private int capacity = 100;
            private int refillTokens = 10;
            private int refillPeriod = 60;
            private TimeUnit refillUnit = TimeUnit.SECONDS;

            public Builder capacity(int capacity) {
                this.capacity = capacity;
                return this;
            }

            public Builder refillTokens(int refillTokens) {
                this.refillTokens = refillTokens;
                return this;
            }

            public Builder refillPeriod(int refillPeriod) {
                this.refillPeriod = refillPeriod;
                return this;
            }

            public Builder refillUnit(TimeUnit refillUnit) {
                this.refillUnit = refillUnit;
                return this;
            }

            public RateLimitConfig build() {
                return new RateLimitConfig(this);
            }
        }
    }

    // Implementation of com.integrixs.shared.services.RateLimiterService interface methods

    @Override
    public <T> T executeWithRateLimit(String rateLimiterName, Callable<T> callable) throws Exception {
        RateLimitResult result = checkRateLimit("api:" + rateLimiterName, null);
        if (!result.isAllowed()) {
            throw new RuntimeException("Rate limit exceeded for: " + rateLimiterName);
        }
        return callable.call();
    }

    @Override
    public <T> CompletableFuture<T> executeWithRateLimitAsync(String rateLimiterName, Callable<T> callable) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return executeWithRateLimit(rateLimiterName, callable);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public boolean tryAcquirePermission(String rateLimiterName) {
        RateLimitResult result = checkRateLimit("api:" + rateLimiterName, null);
        return result.isAllowed();
    }

    @Override
    public boolean tryAcquirePermission(String rateLimiterName, long timeoutMs) {
        // For simplicity, ignore timeout for now and just check current status
        return tryAcquirePermission(rateLimiterName);
    }

    @Override
    public com.integrixs.shared.services.RateLimiterService.RateLimitResult getRateLimitStatus(String rateLimiterName) {
        RateLimitResult backendResult = checkRateLimit("api:" + rateLimiterName, null);
        return new com.integrixs.shared.services.RateLimiterService.RateLimitResult(
            backendResult.isAllowed(),
            backendResult.getRetryAfter() * 1000, // Convert to milliseconds
            backendResult.getRemainingRequests()
        );
    }

    @Override
    public void acquire(String rateLimiterName, int permits) {
        // For simplicity, just check if allowed for the requested permits
        for (int i = 0; i < permits; i++) {
            if (!tryAcquirePermission(rateLimiterName)) {
                throw new RuntimeException("Could not acquire " + permits + " permits for " + rateLimiterName);
            }
        }
    }
}
