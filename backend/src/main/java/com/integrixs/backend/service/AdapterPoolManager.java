package com.integrixs.backend.service;

import com.integrixs.adapters.core.*;
import com.integrixs.adapters.factory.AdapterFactoryManager;
import com.integrixs.data.model.CommunicationAdapter;
import com.integrixs.data.sql.repository.CommunicationAdapterSqlRepository;
import com.integrixs.shared.enums.AdapterType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Adapter Pool Manager - Manages pools of adapter instances for efficient resource utilization
 * Provides connection pooling, lifecycle management, and resource limits
 */
@Service
public class AdapterPoolManager {

    private static final Logger logger = LoggerFactory.getLogger(AdapterPoolManager.class);

    @Autowired
    private CommunicationAdapterSqlRepository adapterRepository;

    private AdapterHealthMonitor healthMonitor;

    private final AdapterFactoryManager adapterFactory = AdapterFactoryManager.getInstance();

    // Pool configuration
    private static final int DEFAULT_POOL_SIZE = 5;
    private static final int MAX_POOL_SIZE = 20;
    private static final long IDLE_TIMEOUT_MS = 300000; // 5 minutes
    private static final long VALIDATION_INTERVAL_MS = 60000; // 1 minute

    // Adapter pools by adapter ID
    private final Map<String, AdapterPool> adapterPools = new ConcurrentHashMap<>();

    // Global resource limits
    private final Semaphore globalAdapterLimit = new Semaphore(100); // Max 100 adapters across all pools

    // Pool maintenance executor
    private final ScheduledExecutorService maintenanceExecutor = Executors.newScheduledThreadPool(2);

    // JSON ObjectMapper
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public void setHealthMonitor(AdapterHealthMonitor healthMonitor) {
        this.healthMonitor = healthMonitor;
    }

    @PostConstruct
    public void initialize() {
        logger.info("Initializing AdapterPoolManager");

        // Start pool maintenance tasks
        maintenanceExecutor.scheduleWithFixedDelay(
            this::validatePools,
            VALIDATION_INTERVAL_MS,
            VALIDATION_INTERVAL_MS,
            TimeUnit.MILLISECONDS
       );

        maintenanceExecutor.scheduleWithFixedDelay(
            this::evictIdleConnections,
            IDLE_TIMEOUT_MS,
            IDLE_TIMEOUT_MS,
            TimeUnit.MILLISECONDS
       );
    }

    @PreDestroy
    public void shutdown() {
        logger.info("Shutting down AdapterPoolManager");

        maintenanceExecutor.shutdown();
        try {
            if(!maintenanceExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                maintenanceExecutor.shutdownNow();
            }
        } catch(InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Shutdown all pools
        adapterPools.values().forEach(AdapterPool::shutdown);
        adapterPools.clear();
    }

    /**
     * Get a inbound adapter from the pool
     */
    public PooledAdapter<com.integrixs.adapters.domain.port.InboundAdapterPort> getInboundAdapter(String adapterId) throws Exception {
        CommunicationAdapter adapter = getAdapterConfig(adapterId);
        AdapterPool pool = getOrCreatePool(adapterId, adapter);

        return pool.borrowSender();
    }

    /**
     * Get a outbound adapter from the pool
     */
    public PooledAdapter<com.integrixs.adapters.domain.port.OutboundAdapterPort> getOutboundAdapter(String adapterId) throws Exception {
        CommunicationAdapter adapter = getAdapterConfig(adapterId);
        AdapterPool pool = getOrCreatePool(adapterId, adapter);

        return pool.borrowReceiver();
    }

    /**
     * Return an adapter to the pool
     */
    public void returnAdapter(String adapterId, PooledAdapter<?> pooledAdapter) {
        AdapterPool pool = adapterPools.get(adapterId);
        if(pool != null) {
            pool.returnAdapter(pooledAdapter);
        } else {
            // Pool doesn't exist, destroy the adapter
            pooledAdapter.destroy();
        }
    }

    /**
     * Get or create an adapter pool
     */
    private AdapterPool getOrCreatePool(String adapterId, CommunicationAdapter adapter) {
        return adapterPools.computeIfAbsent(adapterId, id -> {
            logger.info("Creating new adapter pool for: {}", adapter.getName());
            return new AdapterPool(adapterId, adapter);
        });
    }

    /**
     * Get adapter configuration
     */
    private CommunicationAdapter getAdapterConfig(String adapterId) {
        return adapterRepository.findById(UUID.fromString(adapterId))
            .orElseThrow(() -> new IllegalArgumentException("Adapter not found: " + adapterId));
    }

    /**
     * Validate all pools
     */
    private void validatePools() {
        for(AdapterPool pool : adapterPools.values()) {
            try {
                pool.validate();
            } catch(Exception e) {
                logger.error("Error validating pool for adapter {}", pool.adapterId, e);
            }
        }
    }

    /**
     * Evict idle connections from all pools
     */
    private void evictIdleConnections() {
        for(AdapterPool pool : adapterPools.values()) {
            try {
                pool.evictIdle();
            } catch(Exception e) {
                logger.error("Error evicting idle connections for adapter {}", pool.adapterId, e);
            }
        }
    }

    /**
     * Get pool statistics
     */
    public PoolStatistics getPoolStatistics(String adapterId) {
        AdapterPool pool = adapterPools.get(adapterId);
        return pool != null ? pool.getStatistics() : null;
    }

    /**
     * Get all pool statistics
     */
    public Map<String, PoolStatistics> getAllPoolStatistics() {
        Map<String, PoolStatistics> stats = new HashMap<>();
        for(Map.Entry<String, AdapterPool> entry : adapterPools.entrySet()) {
            stats.put(entry.getKey(), entry.getValue().getStatistics());
        }
        return stats;
    }

    /**
     * Inner class representing an adapter pool
     */
    private class AdapterPool {
        private final String adapterId;
        private final CommunicationAdapter adapterConfig;
        private final BlockingQueue<PooledAdapter<com.integrixs.adapters.domain.port.InboundAdapterPort>> senderPool;
        private final BlockingQueue<PooledAdapter<com.integrixs.adapters.domain.port.OutboundAdapterPort>> receiverPool;
        private final AtomicInteger activeSenders = new AtomicInteger(0);
        private final AtomicInteger activeReceivers = new AtomicInteger(0);
        private final ReadWriteLock lock = new ReentrantReadWriteLock();
        private volatile boolean shutdown = false;

        public AdapterPool(String adapterId, CommunicationAdapter adapterConfig) {
            this.adapterId = adapterId;
            this.adapterConfig = adapterConfig;

            int poolSize = determinePoolSize(adapterConfig);
            this.senderPool = new LinkedBlockingQueue<>(poolSize);
            this.receiverPool = new LinkedBlockingQueue<>(poolSize);
        }

        public PooledAdapter<com.integrixs.adapters.domain.port.InboundAdapterPort> borrowSender() throws Exception {
            if(shutdown) {
                throw new IllegalStateException("Pool is shutdown");
            }

            PooledAdapter<com.integrixs.adapters.domain.port.InboundAdapterPort> pooled = senderPool.poll();

            if(pooled == null) {
                // Try to create a new adapter
                lock.readLock().lock();
                try {
                    if(activeSenders.get() < getMaxPoolSize()) {
                        pooled = createInboundAdapter();
                    }
                } finally {
                    lock.readLock().unlock();
                }

                if(pooled == null) {
                    // Wait for an adapter to become available
                    pooled = senderPool.poll(5, TimeUnit.SECONDS);
                    if(pooled == null) {
                        throw new TimeoutException("No inbound adapter available in pool");
                    }
                }
            }

            // Validate before returning
            if(!pooled.validate()) {
                pooled.destroy();
                return borrowSender(); // Recursive call to get another
            }

            activeSenders.incrementAndGet();
            pooled.markBorrowed();
            return pooled;
        }

        public PooledAdapter<com.integrixs.adapters.domain.port.OutboundAdapterPort> borrowReceiver() throws Exception {
            if(shutdown) {
                throw new IllegalStateException("Pool is shutdown");
            }

            PooledAdapter<com.integrixs.adapters.domain.port.OutboundAdapterPort> pooled = receiverPool.poll();

            if(pooled == null) {
                // Try to create a new adapter
                lock.readLock().lock();
                try {
                    if(activeReceivers.get() < getMaxPoolSize()) {
                        pooled = createOutboundAdapter();
                    }
                } finally {
                    lock.readLock().unlock();
                }

                if(pooled == null) {
                    // Wait for an adapter to become available
                    pooled = receiverPool.poll(5, TimeUnit.SECONDS);
                    if(pooled == null) {
                        throw new TimeoutException("No outbound adapter available in pool");
                    }
                }
            }

            // Validate before returning
            if(!pooled.validate()) {
                pooled.destroy();
                return borrowReceiver(); // Recursive call to get another
            }

            activeReceivers.incrementAndGet();
            pooled.markBorrowed();
            return pooled;
        }

        public void returnAdapter(PooledAdapter<?> pooledAdapter) {
            if(shutdown || !pooledAdapter.isReusable()) {
                pooledAdapter.destroy();
                return;
            }

            pooledAdapter.markReturned();

            if(pooledAdapter.getAdapter() instanceof com.integrixs.adapters.domain.port.InboundAdapterPort) {
                activeSenders.decrementAndGet();
                senderPool.offer((PooledAdapter<com.integrixs.adapters.domain.port.InboundAdapterPort>) pooledAdapter);
            } else if(pooledAdapter.getAdapter() instanceof com.integrixs.adapters.domain.port.OutboundAdapterPort) {
                activeReceivers.decrementAndGet();
                receiverPool.offer((PooledAdapter<com.integrixs.adapters.domain.port.OutboundAdapterPort>) pooledAdapter);
            }
        }

        private PooledAdapter<com.integrixs.adapters.domain.port.InboundAdapterPort> createInboundAdapter() throws Exception {
            if(!globalAdapterLimit.tryAcquire()) {
                throw new RuntimeException("Global adapter limit reached");
            }

            try {
                Object config = buildAdapterConfiguration(adapterConfig);
                com.integrixs.adapters.domain.port.InboundAdapterPort adapter = adapterFactory.createSender(
                    mapToAdapterType(adapterConfig.getType()),
                    config
               );
                // Create AdapterConfiguration for initialization
                com.integrixs.adapters.domain.model.AdapterConfiguration initConfig =
                    com.integrixs.adapters.domain.model.AdapterConfiguration.builder()
                        .adapterType(mapToAdapterType(adapterConfig.getType()))
                        .adapterMode(adapterConfig.getMode())
                        .connectionProperties((Map<String, Object>) config)
                        .build();

                adapter.initialize(initConfig);

                return new PooledAdapter<>(adapter, adapterId, true);
            } catch(Exception e) {
                globalAdapterLimit.release();
                throw e;
            }
        }

        private PooledAdapter<com.integrixs.adapters.domain.port.OutboundAdapterPort> createOutboundAdapter() throws Exception {
            if(!globalAdapterLimit.tryAcquire()) {
                throw new RuntimeException("Global adapter limit reached");
            }

            try {
                Object config = buildAdapterConfiguration(adapterConfig);
                com.integrixs.adapters.domain.port.OutboundAdapterPort adapter = adapterFactory.createReceiver(
                    mapToAdapterType(adapterConfig.getType()),
                    config
               );
                // Create AdapterConfiguration for initialization
                com.integrixs.adapters.domain.model.AdapterConfiguration initConfig =
                    com.integrixs.adapters.domain.model.AdapterConfiguration.builder()
                        .adapterType(mapToAdapterType(adapterConfig.getType()))
                        .adapterMode(adapterConfig.getMode())
                        .connectionProperties((Map<String, Object>) config)
                        .build();

                adapter.initialize(initConfig);

                return new PooledAdapter<>(adapter, adapterId, false);
            } catch(Exception e) {
                globalAdapterLimit.release();
                throw e;
            }
        }

        public void validate() {
            List<PooledAdapter<com.integrixs.adapters.domain.port.InboundAdapterPort>> invalidSenders = new ArrayList<>();
            List<PooledAdapter<com.integrixs.adapters.domain.port.OutboundAdapterPort>> invalidReceivers = new ArrayList<>();

            // Validate sender pool
            senderPool.forEach(pooled -> {
                if(!pooled.validate()) {
                    invalidSenders.add(pooled);
                }
            });

            // Validate receiver pool
            receiverPool.forEach(pooled -> {
                if(!pooled.validate()) {
                    invalidReceivers.add(pooled);
                }
            });

            // Remove invalid adapters
            invalidSenders.forEach(pooled -> {
                senderPool.remove(pooled);
                pooled.destroy();
            });

            invalidReceivers.forEach(pooled -> {
                receiverPool.remove(pooled);
                pooled.destroy();
            });

            if(!invalidSenders.isEmpty() || !invalidReceivers.isEmpty()) {
                logger.warn("Removed {} invalid sender and {} invalid outbound adapters from pool {}",
                    invalidSenders.size(), invalidReceivers.size(), adapterId);
            }
        }

        public void evictIdle() {
            long now = System.currentTimeMillis();

            // Evict idle senders
            senderPool.removeIf(pooled -> {
                if(now - pooled.getLastUsedTime() > IDLE_TIMEOUT_MS) {
                    pooled.destroy();
                    return true;
                }
                return false;
            });

            // Evict idle receivers
            receiverPool.removeIf(pooled -> {
                if(now - pooled.getLastUsedTime() > IDLE_TIMEOUT_MS) {
                    pooled.destroy();
                    return true;
                }
                return false;
            });
        }

        public void shutdown() {
            lock.writeLock().lock();
            try {
                shutdown = true;

                // Destroy all pooled adapters
                while(!senderPool.isEmpty()) {
                    PooledAdapter<com.integrixs.adapters.domain.port.InboundAdapterPort> pooled = senderPool.poll();
                    if(pooled != null) {
                        pooled.destroy();
                    }
                }

                while(!receiverPool.isEmpty()) {
                    PooledAdapter<com.integrixs.adapters.domain.port.OutboundAdapterPort> pooled = receiverPool.poll();
                    if(pooled != null) {
                        pooled.destroy();
                    }
                }
            } finally {
                lock.writeLock().unlock();
            }
        }

        public PoolStatistics getStatistics() {
            return new PoolStatistics(
                adapterId,
                adapterConfig.getName(),
                senderPool.size(),
                receiverPool.size(),
                activeSenders.get(),
                activeReceivers.get(),
                getMaxPoolSize()
           );
        }

        private int determinePoolSize(CommunicationAdapter adapter) {
            // Determine pool size based on adapter type
            return switch(adapter.getType()) {
                case JDBC -> 10; // Database connections need larger pool
                case HTTP, REST -> 15; // HTTP can handle more concurrent connections
                case FILE, FTP, SFTP -> 5; // File operations are usually limited
                case IBMMQ -> 8; // IBM MQ has moderate concurrency
                default -> DEFAULT_POOL_SIZE;
            };
        }

        private int getMaxPoolSize() {
            return Math.min(determinePoolSize(adapterConfig) * 2, MAX_POOL_SIZE);
        }
    }

    /**
     * Pooled adapter wrapper
     */
    public static class PooledAdapter<T> {
        private final T adapter;
        private final String adapterId;
        private final boolean isSender;
        private long lastUsedTime;
        private long borrowedTime;
        private boolean borrowed;

        public PooledAdapter(T adapter, String adapterId, boolean isSender) {
            this.adapter = adapter;
            this.adapterId = adapterId;
            this.isSender = isSender;
            this.lastUsedTime = System.currentTimeMillis();
            this.borrowed = false;
        }

        public T getAdapter() {
            return adapter;
        }

        public void markBorrowed() {
            this.borrowed = true;
            this.borrowedTime = System.currentTimeMillis();
        }

        public void markReturned() {
            this.borrowed = false;
            this.lastUsedTime = System.currentTimeMillis();
        }

        public boolean validate() {
            try {
                // Basic validation - adapter should still be initialized
                return adapter != null; // In real implementation, would call adapter.isHealthy()
            } catch(Exception e) {
                return false;
            }
        }

        public boolean isReusable() {
            // Some adapters might not be reusable after certain operations
            return true; // For now, assume all are reusable
        }

        public void destroy() {
            try {
                if(adapter instanceof com.integrixs.adapters.domain.port.AdapterPort) {
                    ((com.integrixs.adapters.domain.port.AdapterPort) adapter).shutdown();
                }
            } catch(Exception e) {
                logger.error("Error destroying adapter", e);
            }
        }

        public long getLastUsedTime() {
            return lastUsedTime;
        }

        public boolean isBorrowed() {
            return borrowed;
        }
    }

    /**
     * Pool statistics
     */
    public static class PoolStatistics {
        private final String adapterId;
        private final String adapterName;
        private final int pooledSenders;
        private final int pooledReceivers;
        private final int activeSenders;
        private final int activeReceivers;
        private final int maxPoolSize;

        public PoolStatistics(String adapterId, String adapterName, int pooledSenders,
                            int pooledReceivers, int activeSenders, int activeReceivers,
                            int maxPoolSize) {
            this.adapterId = adapterId;
            this.adapterName = adapterName;
            this.pooledSenders = pooledSenders;
            this.pooledReceivers = pooledReceivers;
            this.activeSenders = activeSenders;
            this.activeReceivers = activeReceivers;
            this.maxPoolSize = maxPoolSize;
        }

        public String getAdapterId() { return adapterId; }
        public String getAdapterName() { return adapterName; }
        public int getPooledSenders() { return pooledSenders; }
        public int getPooledReceivers() { return pooledReceivers; }
        public int getActiveSenders() { return activeSenders; }
        public int getActiveReceivers() { return activeReceivers; }
        public int getMaxPoolSize() { return maxPoolSize; }
        public int getTotalActive() { return activeSenders + activeReceivers; }
        public int getTotalPooled() { return pooledSenders + pooledReceivers; }
    }

    // Helper methods

    private Object buildAdapterConfiguration(CommunicationAdapter adapter) {
        // Parse the JSON configuration and create appropriate config objects
        Map<String, Object> config = new HashMap<>();

        try {
            // Parse the JSON configuration if present
            if(adapter.getConfiguration() != null && !adapter.getConfiguration().isEmpty()) {
                config = objectMapper.readValue(adapter.getConfiguration(), Map.class);
            }

            // Add standard fields from adapter entity
            config.put("adapterId", adapter.getId().toString());
            config.put("adapterName", adapter.getName());
            config.put("adapterType", adapter.getType().toString());

            // Add type - specific configurations
            switch(adapter.getType()) {
                case HTTP:
                case REST:
                    // Ensure required HTTP fields
                    config.putIfAbsent("endpoint", config.getOrDefault("url", ""));
                    config.putIfAbsent("method", "POST");
                    config.putIfAbsent("timeout", 30000);
                    config.putIfAbsent("contentType", "application/json");
                    break;

                case SOAP:
                    // Ensure required SOAP fields
                    config.putIfAbsent("wsdlUrl", config.getOrDefault("url", ""));
                    config.putIfAbsent("soapAction", "");
                    config.putIfAbsent("soapVersion", "1.1");
                    break;

                case FILE:
                case FTP:
                case SFTP:
                    // Ensure required file transfer fields
                    config.putIfAbsent("directory", config.getOrDefault("path", "/"));
                    config.putIfAbsent("filePattern", "*");
                    config.putIfAbsent("pollInterval", 60000);
                    break;

                case JDBC:
                    // Ensure required database fields
                    config.putIfAbsent("connectionUrl", config.getOrDefault("url", ""));
                    config.putIfAbsent("driverClass", config.getOrDefault("driver", ""));
                    config.putIfAbsent("poolSize", 5);
                    break;

                case IBMMQ:
                case RABBITMQ:
                case AMQP:
                    // Ensure required messaging fields
                    config.putIfAbsent("queueName", config.getOrDefault("queue", ""));
                    config.putIfAbsent("connectionFactory", config.getOrDefault("factory", ""));
                    break;

                default:
                    // Generic configuration
                    logger.debug("Using generic configuration for adapter type: {}", adapter.getType());
            }

            // Add common fields
            config.putIfAbsent("retryCount", 3);
            config.putIfAbsent("retryDelay", 1000);
            config.putIfAbsent("enabled", true);

        } catch(Exception e) {
            logger.error("Error building adapter configuration for adapter {}: {}",
                adapter.getName(), e.getMessage());
        }

        return config;
    }

    private com.integrixs.adapters.domain.model.AdapterConfiguration.AdapterTypeEnum mapToAdapterType(AdapterType sharedType) {
        return com.integrixs.adapters.domain.model.AdapterConfiguration.AdapterTypeEnum.valueOf(sharedType.name());
    }
}
