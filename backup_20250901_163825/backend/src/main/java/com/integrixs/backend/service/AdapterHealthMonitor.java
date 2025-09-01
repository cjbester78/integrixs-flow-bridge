package com.integrixs.backend.service;

import com.integrixs.data.model.AdapterHealthRecord;
import com.integrixs.data.model.CommunicationAdapter;
import com.integrixs.data.repository.AdapterHealthRecordRepository;
import com.integrixs.data.repository.CommunicationAdapterRepository;
import com.integrixs.shared.enums.AdapterType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Adapter Health Monitor - Monitors the health status of all adapters
 * Performs periodic health checks, tracks metrics, and alerts on failures
 */
@Service
public class AdapterHealthMonitor {
    
    private static final Logger logger = LoggerFactory.getLogger(AdapterHealthMonitor.class);
    
    @Autowired
    private CommunicationAdapterRepository adapterRepository;
    
    @Autowired
    private AdapterHealthRecordRepository healthRecordRepository;
    
    
    private AdapterPoolManager poolManager;
    
    @Value("${integrix.health.check.interval:30000}")
    private long healthCheckIntervalMs;
    
    @Value("${integrix.health.check.timeout:10000}")
    private long healthCheckTimeoutMs;
    
    @Value("${integrix.health.failure.threshold:3}")
    private int failureThreshold;
    
    // Health check executor
    private ScheduledExecutorService healthCheckExecutor;
    private ExecutorService checkExecutor;
    
    // Health status tracking
    private final Map<String, AdapterHealthStatus> healthStatusMap = new ConcurrentHashMap<>();
    
    // Metrics tracking
    private final Map<String, AdapterMetrics> metricsMap = new ConcurrentHashMap<>();
    
    @Autowired
    public void setPoolManager(@Lazy AdapterPoolManager poolManager) {
        this.poolManager = poolManager;
    }
    
    @PostConstruct
    public void initialize() {
        logger.info("Initializing AdapterHealthMonitor");
        
        healthCheckExecutor = Executors.newScheduledThreadPool(2);
        checkExecutor = Executors.newFixedThreadPool(10);
        
        // Load all adapters
        loadAdapters();
        
        // Start health check scheduler
        healthCheckExecutor.scheduleWithFixedDelay(
            this::performHealthChecks,
            healthCheckIntervalMs,
            healthCheckIntervalMs,
            TimeUnit.MILLISECONDS
        );
        
        // Start metrics collection
        healthCheckExecutor.scheduleWithFixedDelay(
            this::collectMetrics,
            60000, // Every minute
            60000,
            TimeUnit.MILLISECONDS
        );
    }
    
    /**
     * Load all active adapters for monitoring
     */
    private void loadAdapters() {
        List<CommunicationAdapter> adapters = adapterRepository.findByIsActiveTrue();
        
        for (CommunicationAdapter adapter : adapters) {
            healthStatusMap.put(
                adapter.getId().toString(),
                new AdapterHealthStatus(adapter.getId().toString(), adapter.getName())
            );
            
            metricsMap.put(
                adapter.getId().toString(),
                new AdapterMetrics(adapter.getId().toString())
            );
        }
        
        logger.info("Loaded {} adapters for health monitoring", adapters.size());
    }
    
    /**
     * Perform health checks on all adapters
     */
    @Scheduled(fixedDelayString = "${integrix.health.check.interval:30000}")
    public void performHealthChecks() {
        logger.debug("Starting health checks for {} adapters", healthStatusMap.size());
        
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        
        for (String adapterId : healthStatusMap.keySet()) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                checkAdapterHealth(adapterId);
            }, checkExecutor);
            
            futures.add(future);
        }
        
        // Wait for all checks to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .orTimeout(healthCheckTimeoutMs * 2, TimeUnit.MILLISECONDS)
            .exceptionally(ex -> {
                logger.error("Health check batch failed", ex);
                return null;
            });
    }
    
    /**
     * Check health of a single adapter
     */
    private void checkAdapterHealth(String adapterId) {
        AdapterHealthStatus status = healthStatusMap.get(adapterId);
        if (status == null) return;
        
        try {
            CommunicationAdapter adapter = adapterRepository.findById(UUID.fromString(adapterId))
                .orElse(null);
            
            if (adapter == null || !adapter.isActive()) {
                status.markInactive();
                return;
            }
            
            // Perform health check based on adapter type
            HealthCheckResult result = performHealthCheck(adapter);
            
            // Update status
            if (result.isHealthy()) {
                status.markHealthy(result.getResponseTime());
                recordHealthCheck(adapter, true, result.getResponseTime(), null);
            } else {
                status.markUnhealthy(result.getErrorMessage());
                recordHealthCheck(adapter, false, result.getResponseTime(), result.getErrorMessage());
                
                // Check if failure threshold exceeded
                if (status.getConsecutiveFailures() >= failureThreshold) {
                    handleAdapterFailure(adapter, status);
                }
            }
            
        } catch (Exception e) {
            logger.error("Error checking health for adapter {}", adapterId, e);
            status.markUnhealthy(e.getMessage());
        }
    }
    
    /**
     * Perform actual health check for adapter
     */
    private HealthCheckResult performHealthCheck(CommunicationAdapter adapter) {
        long startTime = System.currentTimeMillis();
        
        try {
            switch (adapter.getType()) {
                case HTTP:
                case REST:
                    return checkHttpHealth(adapter);
                case JDBC:
                    return checkDatabaseHealth(adapter);
                case FILE:
                    return checkFileSystemHealth(adapter);
                case FTP:
                case SFTP:
                    return checkFtpHealth(adapter);
                case JMS:
                    return checkJmsHealth(adapter);
                case SOAP:
                    return checkSoapHealth(adapter);
                default:
                    // Generic health check
                    return checkGenericHealth(adapter);
            }
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            return HealthCheckResult.unhealthy(e.getMessage(), responseTime);
        }
    }
    
    /**
     * Check HTTP/REST adapter health
     */
    private HealthCheckResult checkHttpHealth(CommunicationAdapter adapter) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Parse adapter configuration to get endpoint
            Map<String, Object> config = parseAdapterConfig(adapter);
            String endpoint = (String) config.get("endpoint");
            
            if (endpoint == null) {
                return HealthCheckResult.unhealthy("No endpoint configured", 0);
            }
            
            // TODO: Perform actual HTTP health check
            // For now, simulate health check
            Thread.sleep(100); // Simulate network call
            
            long responseTime = System.currentTimeMillis() - startTime;
            return HealthCheckResult.healthy(responseTime);
            
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            return HealthCheckResult.unhealthy(e.getMessage(), responseTime);
        }
    }
    
    /**
     * Check database adapter health
     */
    private HealthCheckResult checkDatabaseHealth(CommunicationAdapter adapter) {
        long startTime = System.currentTimeMillis();
        
        try {
            // TODO: Perform actual database connectivity check
            // For now, simulate health check
            Thread.sleep(50); // Simulate DB query
            
            long responseTime = System.currentTimeMillis() - startTime;
            return HealthCheckResult.healthy(responseTime);
            
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            return HealthCheckResult.unhealthy(e.getMessage(), responseTime);
        }
    }
    
    /**
     * Check file system adapter health
     */
    private HealthCheckResult checkFileSystemHealth(CommunicationAdapter adapter) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Parse configuration to get directory
            Map<String, Object> config = parseAdapterConfig(adapter);
            String directory = (String) config.get("directory");
            
            if (directory == null) {
                return HealthCheckResult.unhealthy("No directory configured", 0);
            }
            
            // TODO: Check if directory exists and is accessible
            // For now, simulate health check
            Thread.sleep(20); // Simulate file system check
            
            long responseTime = System.currentTimeMillis() - startTime;
            return HealthCheckResult.healthy(responseTime);
            
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            return HealthCheckResult.unhealthy(e.getMessage(), responseTime);
        }
    }
    
    /**
     * Check FTP/SFTP adapter health
     */
    private HealthCheckResult checkFtpHealth(CommunicationAdapter adapter) {
        long startTime = System.currentTimeMillis();
        
        try {
            // TODO: Perform actual FTP connectivity check
            // For now, simulate health check
            Thread.sleep(150); // Simulate FTP connection
            
            long responseTime = System.currentTimeMillis() - startTime;
            return HealthCheckResult.healthy(responseTime);
            
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            return HealthCheckResult.unhealthy(e.getMessage(), responseTime);
        }
    }
    
    /**
     * Check JMS adapter health
     */
    private HealthCheckResult checkJmsHealth(CommunicationAdapter adapter) {
        long startTime = System.currentTimeMillis();
        
        try {
            // TODO: Perform actual JMS connectivity check
            // For now, simulate health check
            Thread.sleep(100); // Simulate JMS connection
            
            long responseTime = System.currentTimeMillis() - startTime;
            return HealthCheckResult.healthy(responseTime);
            
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            return HealthCheckResult.unhealthy(e.getMessage(), responseTime);
        }
    }
    
    /**
     * Check SOAP adapter health
     */
    private HealthCheckResult checkSoapHealth(CommunicationAdapter adapter) {
        long startTime = System.currentTimeMillis();
        
        try {
            // TODO: Perform actual SOAP service check
            // For now, simulate health check
            Thread.sleep(200); // Simulate SOAP call
            
            long responseTime = System.currentTimeMillis() - startTime;
            return HealthCheckResult.healthy(responseTime);
            
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            return HealthCheckResult.unhealthy(e.getMessage(), responseTime);
        }
    }
    
    /**
     * Generic health check
     */
    private HealthCheckResult checkGenericHealth(CommunicationAdapter adapter) {
        // Basic check - just verify adapter can be created
        try {
            // Try to get adapter from pool
            var poolStats = poolManager.getPoolStatistics(adapter.getId().toString());
            
            if (poolStats != null && poolStats.getTotalActive() > 0) {
                return HealthCheckResult.healthy(10);
            }
            
            return HealthCheckResult.healthy(50);
            
        } catch (Exception e) {
            return HealthCheckResult.unhealthy(e.getMessage(), 0);
        }
    }
    
    /**
     * Handle adapter failure
     */
    private void handleAdapterFailure(CommunicationAdapter adapter, AdapterHealthStatus status) {
        logger.error("Adapter {} has failed {} consecutive health checks", 
            adapter.getName(), status.getConsecutiveFailures());
        
        // Send notification
        logger.error("Adapter health failure notification - adapter: {} ({}), error: {}", 
            adapter.getName(), adapter.getId(), status.getLastError());
        
        // Mark adapter as unhealthy in database
        updateAdapterHealthStatus(adapter.getId().toString(), false);
    }
    
    /**
     * Record health check result
     */
    @Transactional
    private void recordHealthCheck(CommunicationAdapter adapter, boolean healthy, 
                                  long responseTime, String error) {
        try {
            AdapterHealthRecord record = new AdapterHealthRecord();
            record.setAdapterId(adapter.getId());
            record.setHealthy(healthy);
            record.setResponseTimeMs(responseTime);
            record.setErrorMessage(error);
            record.setCheckTime(LocalDateTime.now());
            
            healthRecordRepository.save(record);
            
        } catch (Exception e) {
            logger.error("Failed to record health check for adapter {}", adapter.getId(), e);
        }
    }
    
    /**
     * Update adapter health status in database
     */
    @Transactional
    private void updateAdapterHealthStatus(String adapterId, boolean healthy) {
        try {
            adapterRepository.findById(UUID.fromString(adapterId)).ifPresent(adapter -> {
                adapter.setHealthy(healthy);
                adapter.setLastHealthCheck(LocalDateTime.now());
                adapterRepository.save(adapter);
            });
        } catch (Exception e) {
            logger.error("Failed to update adapter health status", e);
        }
    }
    
    /**
     * Collect adapter metrics
     */
    private void collectMetrics() {
        for (Map.Entry<String, AdapterMetrics> entry : metricsMap.entrySet()) {
            String adapterId = entry.getKey();
            AdapterMetrics metrics = entry.getValue();
            
            // Get pool statistics
            var poolStats = poolManager.getPoolStatistics(adapterId);
            if (poolStats != null) {
                metrics.updatePoolMetrics(
                    poolStats.getTotalActive(),
                    poolStats.getTotalPooled()
                );
            }
        }
    }
    
    /**
     * Get health status for an adapter
     */
    public AdapterHealthStatus getHealthStatus(String adapterId) {
        return healthStatusMap.get(adapterId);
    }
    
    /**
     * Get health status for all adapters
     */
    public Map<String, AdapterHealthStatus> getAllHealthStatus() {
        return new HashMap<>(healthStatusMap);
    }
    
    /**
     * Get metrics for an adapter
     */
    public AdapterMetrics getMetrics(String adapterId) {
        return metricsMap.get(adapterId);
    }
    
    /**
     * Force health check for specific adapter
     */
    public CompletableFuture<HealthCheckResult> forceHealthCheck(String adapterId) {
        return CompletableFuture.supplyAsync(() -> {
            CommunicationAdapter adapter = adapterRepository.findById(UUID.fromString(adapterId))
                .orElseThrow(() -> new IllegalArgumentException("Adapter not found"));
            
            return performHealthCheck(adapter);
        }, checkExecutor);
    }
    
    /**
     * Parse adapter configuration JSON
     */
    private Map<String, Object> parseAdapterConfig(CommunicationAdapter adapter) {
        // TODO: Implement JSON parsing
        return new HashMap<>();
    }
    
    /**
     * Health check result
     */
    public static class HealthCheckResult {
        private final boolean healthy;
        private final String errorMessage;
        private final long responseTime;
        
        public static HealthCheckResult healthy(long responseTime) {
            return new HealthCheckResult(true, null, responseTime);
        }
        
        public static HealthCheckResult unhealthy(String error, long responseTime) {
            return new HealthCheckResult(false, error, responseTime);
        }
        
        private HealthCheckResult(boolean healthy, String errorMessage, long responseTime) {
            this.healthy = healthy;
            this.errorMessage = errorMessage;
            this.responseTime = responseTime;
        }
        
        public boolean isHealthy() { return healthy; }
        public String getErrorMessage() { return errorMessage; }
        public long getResponseTime() { return responseTime; }
    }
    
    /**
     * Adapter health status
     */
    public static class AdapterHealthStatus {
        private final String adapterId;
        private final String adapterName;
        private volatile boolean healthy = true;
        private volatile boolean active = true;
        private volatile LocalDateTime lastCheckTime;
        private volatile String lastError;
        private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
        private final AtomicLong totalChecks = new AtomicLong(0);
        private final AtomicLong failedChecks = new AtomicLong(0);
        private final AtomicLong totalResponseTime = new AtomicLong(0);
        
        public AdapterHealthStatus(String adapterId, String adapterName) {
            this.adapterId = adapterId;
            this.adapterName = adapterName;
            this.lastCheckTime = LocalDateTime.now();
        }
        
        public void markHealthy(long responseTime) {
            this.healthy = true;
            this.lastError = null;
            this.consecutiveFailures.set(0);
            this.lastCheckTime = LocalDateTime.now();
            this.totalChecks.incrementAndGet();
            this.totalResponseTime.addAndGet(responseTime);
        }
        
        public void markUnhealthy(String error) {
            this.healthy = false;
            this.lastError = error;
            this.consecutiveFailures.incrementAndGet();
            this.lastCheckTime = LocalDateTime.now();
            this.totalChecks.incrementAndGet();
            this.failedChecks.incrementAndGet();
        }
        
        public void markInactive() {
            this.active = false;
            this.healthy = false;
        }
        
        public String getAdapterId() { return adapterId; }
        public String getAdapterName() { return adapterName; }
        public boolean isHealthy() { return healthy; }
        public boolean isActive() { return active; }
        public LocalDateTime getLastCheckTime() { return lastCheckTime; }
        public String getLastError() { return lastError; }
        public int getConsecutiveFailures() { return consecutiveFailures.get(); }
        public long getTotalChecks() { return totalChecks.get(); }
        public long getFailedChecks() { return failedChecks.get(); }
        public double getAverageResponseTime() {
            long checks = totalChecks.get() - failedChecks.get();
            return checks > 0 ? (double) totalResponseTime.get() / checks : 0;
        }
        public double getUptime() {
            long total = totalChecks.get();
            return total > 0 ? ((double) (total - failedChecks.get()) / total) * 100 : 100;
        }
    }
    
    /**
     * Adapter metrics
     */
    public static class AdapterMetrics {
        private final String adapterId;
        private final AtomicLong messagesProcessed = new AtomicLong(0);
        private final AtomicLong messagesFailed = new AtomicLong(0);
        private final AtomicLong totalProcessingTime = new AtomicLong(0);
        private volatile int activeConnections = 0;
        private volatile int pooledConnections = 0;
        private volatile LocalDateTime lastUpdated;
        
        public AdapterMetrics(String adapterId) {
            this.adapterId = adapterId;
            this.lastUpdated = LocalDateTime.now();
        }
        
        public void recordMessage(boolean success, long processingTime) {
            if (success) {
                messagesProcessed.incrementAndGet();
            } else {
                messagesFailed.incrementAndGet();
            }
            totalProcessingTime.addAndGet(processingTime);
            lastUpdated = LocalDateTime.now();
        }
        
        public void updatePoolMetrics(int active, int pooled) {
            this.activeConnections = active;
            this.pooledConnections = pooled;
            this.lastUpdated = LocalDateTime.now();
        }
        
        public String getAdapterId() { return adapterId; }
        public long getMessagesProcessed() { return messagesProcessed.get(); }
        public long getMessagesFailed() { return messagesFailed.get(); }
        public int getActiveConnections() { return activeConnections; }
        public int getPooledConnections() { return pooledConnections; }
        public LocalDateTime getLastUpdated() { return lastUpdated; }
        
        public double getAverageProcessingTime() {
            long total = messagesProcessed.get() + messagesFailed.get();
            return total > 0 ? (double) totalProcessingTime.get() / total : 0;
        }
        
        public double getSuccessRate() {
            long total = messagesProcessed.get() + messagesFailed.get();
            return total > 0 ? ((double) messagesProcessed.get() / total) * 100 : 100;
        }
    }
}