package com.integrixs.backend.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for API rate limiting per user.
 * Uses token bucket algorithm for flexible rate limiting.
 */
@Service
public class RateLimitService {

    private static final Logger log = LoggerFactory.getLogger(RateLimitService.class);


    private final MeterRegistry meterRegistry;

    public RateLimitService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    // Rate limit configurations
    @Value("${rate.limit.default.capacity:100}")
    private long defaultCapacity;

    @Value("${rate.limit.default.refill.tokens:100}")
    private long defaultRefillTokens;

    @Value("${rate.limit.default.refill.period:60}")
    private long defaultRefillPeriod; // seconds

    @Value("${rate.limit.premium.capacity:1000}")
    private long premiumCapacity;

    @Value("${rate.limit.premium.refill.tokens:1000}")
    private long premiumRefillTokens;

    @Value("${rate.limit.api.capacity:10}")
    private long apiKeyCapacity;

    @Value("${rate.limit.api.refill.tokens:10}")
    private long apiKeyRefillTokens;

    @Value("${rate.limit.ip.capacity:50}")
    private long ipCapacity;

    @Value("${rate.limit.ip.refill.tokens:50}")
    private long ipRefillTokens;

    @Value("${rate.limit.ip.refill.period:60}")
    private long ipRefillPeriod;

    @Value("${rate.limit.endpoint.execute.capacity:5}")
    private long endpointExecuteCapacity;

    @Value("${rate.limit.endpoint.execute.refill.tokens:5}")
    private long endpointExecuteRefillTokens;

    @Value("${rate.limit.endpoint.test.capacity:20}")
    private long endpointTestCapacity;

    @Value("${rate.limit.endpoint.test.refill.tokens:20}")
    private long endpointTestRefillTokens;

    @Value("${rate.limit.cleanup.interval:3600000}")
    private long cleanupInterval;

    @Value("${rate.limit.cleanup.max-ip-buckets:10000}")
    private int maxIpBuckets;

    // User buckets
    private final Map<String, UserRateLimit> userBuckets = new ConcurrentHashMap<>();

    // IP - based buckets for anonymous users
    private final Map<String, Bucket> ipBuckets = new ConcurrentHashMap<>();

    // API endpoint - specific limits
    private final Map<String, EndpointRateLimit> endpointLimits = new ConcurrentHashMap<>();

    /**
     * Check if request is allowed for a user.
     */
    public boolean allowRequest(String userId, String userType) {
        UserRateLimit userLimit = userBuckets.computeIfAbsent(userId,
            k -> createUserRateLimit(userId, userType));

        boolean allowed = userLimit.getBucket().tryConsume(1);

        // Record metrics
        Tags tags = Tags.of("user", userId, "type", userType);
        if(allowed) {
            meterRegistry.counter("rate.limit.allowed", tags).increment();
        } else {
            meterRegistry.counter("rate.limit.denied", tags).increment();
            log.warn("Rate limit exceeded for user: {} (type: {})", userId, userType);
        }

        // Update last access
        userLimit.setLastAccess(LocalDateTime.now());
        userLimit.incrementRequestCount();

        return allowed;
    }

    /**
     * Check if request is allowed for an IP address.
     */
    public boolean allowRequestByIp(String ipAddress) {
        Bucket bucket = ipBuckets.computeIfAbsent(ipAddress, k -> createIpBucket());

        boolean allowed = bucket.tryConsume(1);

        // Record metrics
        Tags tags = Tags.of("ip", ipAddress);
        if(allowed) {
            meterRegistry.counter("rate.limit.ip.allowed", tags).increment();
        } else {
            meterRegistry.counter("rate.limit.ip.denied", tags).increment();
            log.warn("Rate limit exceeded for IP: {}", ipAddress);
        }

        return allowed;
    }

    /**
     * Check if request is allowed for a specific API endpoint.
     */
    public boolean allowRequestForEndpoint(String userId, String endpoint, int cost) {
        String key = userId + ":" + endpoint;
        EndpointRateLimit endpointLimit = endpointLimits.computeIfAbsent(key,
            k -> createEndpointRateLimit(endpoint));

        boolean allowed = endpointLimit.getBucket().tryConsume(cost);

        if(!allowed) {
            log.warn("Endpoint rate limit exceeded for user {} on {}", userId, endpoint);
        }

        return allowed;
    }

    /**
     * Get remaining tokens for a user.
     */
    public long getRemainingTokens(String userId) {
        UserRateLimit userLimit = userBuckets.get(userId);
        if(userLimit == null) {
            return defaultCapacity;
        }
        return userLimit.getBucket().getAvailableTokens();
    }

    /**
     * Get rate limit status for a user.
     */
    public RateLimitStatus getRateLimitStatus(String userId) {
        UserRateLimit userLimit = userBuckets.get(userId);
        if(userLimit == null) {
            return RateLimitStatus.builder()
                .userId(userId)
                .limit(defaultCapacity)
                .remaining(defaultCapacity)
                .resetTime(LocalDateTime.now().plusSeconds(defaultRefillPeriod))
                .build();
        }

        Bucket bucket = userLimit.getBucket();
        return RateLimitStatus.builder()
            .userId(userId)
            .limit(userLimit.getCapacity())
            .remaining(bucket.getAvailableTokens())
            .resetTime(LocalDateTime.now().plusSeconds(defaultRefillPeriod))
            .requestCount(userLimit.getRequestCount())
            .lastAccess(userLimit.getLastAccess())
            .build();
    }

    /**
     * Reset rate limit for a user.
     */
    public void resetUserLimit(String userId) {
        UserRateLimit userLimit = userBuckets.get(userId);
        if(userLimit != null) {
            userLimit.reset();
            log.info("Reset rate limit for user: {}", userId);
        }
    }

    /**
     * Update rate limit for a user.
     */
    public void updateUserLimit(String userId, long capacity, long refillTokens, long refillPeriod) {
        UserRateLimit userLimit = userBuckets.get(userId);
        if(userLimit != null) {
            Bandwidth bandwidth = Bandwidth.classic(capacity,
                Refill.intervally(refillTokens, Duration.ofSeconds(refillPeriod)));
            userLimit.setBucket(Bucket.builder().addLimit(bandwidth).build());
            userLimit.setCapacity(capacity);
            log.info("Updated rate limit for user {}: capacity = {}, refill = {}/ {}",
                userId, capacity, refillTokens, refillPeriod);
        }
    }

    /**
     * Clean up inactive buckets.
     */
    @Scheduled(fixedDelayString = "${rate.limit.cleanup.interval:3600000}") // Every hour
    public void cleanupInactiveBuckets() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);

        // Clean user buckets
        userBuckets.entrySet().removeIf(entry -> {
            UserRateLimit limit = entry.getValue();
            return limit.getLastAccess() != null && limit.getLastAccess().isBefore(cutoff);
        });

        // IP buckets don't track last access, so clear based on size
        if(ipBuckets.size() > maxIpBuckets) {
            ipBuckets.clear();
            log.info("Cleared IP rate limit cache");
        }

        log.debug("Cleaned up inactive rate limit buckets");
    }

    /**
     * Get all active rate limits.
     */
    public Map<String, RateLimitStatus> getAllRateLimits() {
        Map<String, RateLimitStatus> statuses = new HashMap<>();

        for(Map.Entry<String, UserRateLimit> entry : userBuckets.entrySet()) {
            statuses.put(entry.getKey(), getRateLimitStatus(entry.getKey()));
        }

        return statuses;
    }

    private UserRateLimit createUserRateLimit(String userId, String userType) {
        long capacity;
        long refillTokens;

        switch(userType.toUpperCase()) {
            case "PREMIUM":
            case "ADMIN":
                capacity = premiumCapacity;
                refillTokens = premiumRefillTokens;
                break;

            case "API":
                capacity = apiKeyCapacity;
                refillTokens = apiKeyRefillTokens;
                break;

            default:
                capacity = defaultCapacity;
                refillTokens = defaultRefillTokens;
        }

        Bandwidth bandwidth = Bandwidth.classic(capacity,
            Refill.intervally(refillTokens, Duration.ofSeconds(defaultRefillPeriod)));

        UserRateLimit userLimit = new UserRateLimit();
        userLimit.setUserId(userId);
        userLimit.setUserType(userType);
        userLimit.setBucket(Bucket.builder().addLimit(bandwidth).build());
        userLimit.setCapacity(capacity);
        userLimit.setCreatedAt(LocalDateTime.now());

        log.debug("Created rate limit for user {} (type: {}) with capacity {}",
            userId, userType, capacity);

        return userLimit;
    }

    private Bucket createIpBucket() {
        Bandwidth bandwidth = Bandwidth.classic(ipCapacity,
            Refill.intervally(ipRefillTokens, Duration.ofSeconds(ipRefillPeriod)));
        return Bucket.builder().addLimit(bandwidth).build();
    }

    private EndpointRateLimit createEndpointRateLimit(String endpoint) {
        // Configure based on endpoint sensitivity
        long capacity = defaultCapacity;
        long refillTokens = defaultRefillTokens;

        if(endpoint.contains("/api/v1/flows/execute")) {
            capacity = endpointExecuteCapacity;
            refillTokens = endpointExecuteRefillTokens;
        } else if(endpoint.contains("/api/v1/adapters/test")) {
            capacity = endpointTestCapacity;
            refillTokens = endpointTestRefillTokens;
        }

        Bandwidth bandwidth = Bandwidth.classic(capacity,
            Refill.intervally(refillTokens, Duration.ofMinutes(1)));

        EndpointRateLimit endpointLimit = new EndpointRateLimit();
        endpointLimit.setEndpoint(endpoint);
        endpointLimit.setBucket(Bucket.builder().addLimit(bandwidth).build());

        return endpointLimit;
    }

    private static class UserRateLimit {
        private String userId;
        private String userType;
        private Bucket bucket;
        private long capacity;
        private LocalDateTime createdAt;
        private LocalDateTime lastAccess;
        private long requestCount;

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getUserType() {
            return userType;
        }

        public void setUserType(String userType) {
            this.userType = userType;
        }

        public Bucket getBucket() {
            return bucket;
        }

        public void setBucket(Bucket bucket) {
            this.bucket = bucket;
        }

        public long getCapacity() {
            return capacity;
        }

        public void setCapacity(long capacity) {
            this.capacity = capacity;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }

        public LocalDateTime getLastAccess() {
            return lastAccess;
        }

        public void setLastAccess(LocalDateTime lastAccess) {
            this.lastAccess = lastAccess;
        }

        public long getRequestCount() {
            return requestCount;
        }

        public void setRequestCount(long requestCount) {
            this.requestCount = requestCount;
        }

        public void incrementRequestCount() {
            this.requestCount++;
        }

        public void reset() {
            Bandwidth bandwidth = Bandwidth.classic(capacity,
                Refill.intervally(capacity, Duration.ofMinutes(1)));
            this.bucket = Bucket.builder().addLimit(bandwidth).build();
            this.requestCount = 0;
        }
    }

    private static class EndpointRateLimit {
        private String endpoint;
        private Bucket bucket;

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public Bucket getBucket() {
            return bucket;
        }

        public void setBucket(Bucket bucket) {
            this.bucket = bucket;
        }
    }

    public static class RateLimitStatus {
        private String userId;
        private long limit;
        private long remaining;
        private LocalDateTime resetTime;
        private long requestCount;
        private LocalDateTime lastAccess;

        // Private constructor for builder
        private RateLimitStatus() {}

        // Builder pattern
        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String userId;
            private long limit;
            private long remaining;
            private LocalDateTime resetTime;
            private long requestCount;
            private LocalDateTime lastAccess;

            public Builder userId(String userId) {
                this.userId = userId;
                return this;
            }

            public Builder limit(long limit) {
                this.limit = limit;
                return this;
            }

            public Builder remaining(long remaining) {
                this.remaining = remaining;
                return this;
            }

            public Builder resetTime(LocalDateTime resetTime) {
                this.resetTime = resetTime;
                return this;
            }

            public Builder requestCount(long requestCount) {
                this.requestCount = requestCount;
                return this;
            }

            public Builder lastAccess(LocalDateTime lastAccess) {
                this.lastAccess = lastAccess;
                return this;
            }

            public RateLimitStatus build() {
                RateLimitStatus status = new RateLimitStatus();
                status.userId = this.userId;
                status.limit = this.limit;
                status.remaining = this.remaining;
                status.resetTime = this.resetTime;
                status.requestCount = this.requestCount;
                status.lastAccess = this.lastAccess;
                return status;
            }
        }

        // Getters
        public String getUserId() {
            return userId;
        }

        public long getLimit() {
            return limit;
        }

        public long getRemaining() {
            return remaining;
        }

        public LocalDateTime getResetTime() {
            return resetTime;
        }

        public long getRequestCount() {
            return requestCount;
        }

        public LocalDateTime getLastAccess() {
            return lastAccess;
        }
    }
}
