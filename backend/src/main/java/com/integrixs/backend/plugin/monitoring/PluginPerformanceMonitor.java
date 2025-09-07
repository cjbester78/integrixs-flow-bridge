package com.integrixs.backend.plugin.monitoring;

import com.integrixs.backend.plugin.api.PluginMessage;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Performance monitoring for plugins
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PluginPerformanceMonitor {
    
    private final MeterRegistry meterRegistry;
    
    // Performance metrics storage
    private final Map<String, PluginMetrics> pluginMetrics = new ConcurrentHashMap<>();
    private final Map<String, List<PerformanceSample>> performanceHistory = new ConcurrentHashMap<>();
    
    // Configuration
    private static final int HISTORY_SIZE = 1000;
    private static final Duration AGGREGATION_WINDOW = Duration.ofMinutes(5);
    
    /**
     * Record message processing
     */
    public void recordMessageProcessed(String pluginId, String direction, long processingTimeMs, boolean success) {
        PluginMetrics metrics = getOrCreateMetrics(pluginId);
        
        if (success) {
            metrics.getMessagesProcessed().incrementAndGet();
            metrics.getSuccessfulMessages().incrementAndGet();
        } else {
            metrics.getMessagesProcessed().incrementAndGet();
            metrics.getFailedMessages().incrementAndGet();
        }
        
        metrics.getTotalProcessingTime().addAndGet(processingTimeMs);
        
        // Update min/max processing time
        metrics.updateMinMaxProcessingTime(processingTimeMs);
        
        // Record in Micrometer
        Counter.builder("plugin.messages.processed")
                .tag("plugin", pluginId)
                .tag("direction", direction)
                .tag("status", success ? "success" : "failure")
                .register(meterRegistry)
                .increment();
        
        Timer.builder("plugin.message.processing.time")
                .tag("plugin", pluginId)
                .tag("direction", direction)
                .register(meterRegistry)
                .record(processingTimeMs, TimeUnit.MILLISECONDS);
        
        // Add to performance history
        addPerformanceSample(pluginId, new PerformanceSample(
                Instant.now(),
                processingTimeMs,
                success,
                direction
        ));
    }
    
    /**
     * Record connection test result
     */
    public void recordConnectionTest(String pluginId, String direction, boolean success, long responseTimeMs) {
        PluginMetrics metrics = getOrCreateMetrics(pluginId);
        
        if (success) {
            metrics.getSuccessfulConnectionTests().incrementAndGet();
        } else {
            metrics.getFailedConnectionTests().incrementAndGet();
        }
        
        Counter.builder("plugin.connection.tests")
                .tag("plugin", pluginId)
                .tag("direction", direction)
                .tag("status", success ? "success" : "failure")
                .register(meterRegistry)
                .increment();
        
        if (success && responseTimeMs > 0) {
            Timer.builder("plugin.connection.test.time")
                    .tag("plugin", pluginId)
                    .tag("direction", direction)
                    .register(meterRegistry)
                    .record(responseTimeMs, TimeUnit.MILLISECONDS);
        }
    }
    
    /**
     * Record error
     */
    public void recordError(String pluginId, String errorType, String errorMessage) {
        PluginMetrics metrics = getOrCreateMetrics(pluginId);
        metrics.getErrors().incrementAndGet();
        
        // Track error types
        metrics.getErrorCounts().compute(errorType, (k, v) -> v == null ? 1 : v + 1);
        
        Counter.builder("plugin.errors")
                .tag("plugin", pluginId)
                .tag("type", errorType)
                .register(meterRegistry)
                .increment();
        
        log.warn("Plugin error - ID: {}, Type: {}, Message: {}", pluginId, errorType, errorMessage);
    }
    
    /**
     * Record resource usage
     */
    public void recordResourceUsage(String pluginId, long memoryUsed, double cpuUsage) {
        PluginMetrics metrics = getOrCreateMetrics(pluginId);
        metrics.setLastMemoryUsage(memoryUsed);
        metrics.setLastCpuUsage(cpuUsage);
        
        // Update peak values
        if (memoryUsed > metrics.getPeakMemoryUsage()) {
            metrics.setPeakMemoryUsage(memoryUsed);
        }
        if (cpuUsage > metrics.getPeakCpuUsage()) {
            metrics.setPeakCpuUsage(cpuUsage);
        }
        
        meterRegistry.gauge("plugin.memory.usage", 
                Collections.singletonList(io.micrometer.core.instrument.Tag.of("plugin", pluginId)), 
                memoryUsed);
        
        meterRegistry.gauge("plugin.cpu.usage", 
                Collections.singletonList(io.micrometer.core.instrument.Tag.of("plugin", pluginId)), 
                cpuUsage);
    }
    
    /**
     * Get performance metrics for a plugin
     */
    public PluginMetrics getMetrics(String pluginId) {
        return pluginMetrics.get(pluginId);
    }
    
    /**
     * Get all plugin metrics
     */
    public Map<String, PluginMetrics> getAllMetrics() {
        return new HashMap<>(pluginMetrics);
    }
    
    /**
     * Get performance statistics
     */
    public PerformanceStatistics getStatistics(String pluginId, Duration window) {
        PluginMetrics metrics = pluginMetrics.get(pluginId);
        if (metrics == null) {
            return PerformanceStatistics.empty();
        }
        
        List<PerformanceSample> samples = getRecentSamples(pluginId, window);
        
        return calculateStatistics(metrics, samples);
    }
    
    /**
     * Get performance report
     */
    public PerformanceReport generateReport(String pluginId) {
        PluginMetrics metrics = pluginMetrics.get(pluginId);
        if (metrics == null) {
            return PerformanceReport.builder()
                    .pluginId(pluginId)
                    .reportTime(Instant.now())
                    .build();
        }
        
        // Calculate various time windows
        PerformanceStatistics last5Minutes = getStatistics(pluginId, Duration.ofMinutes(5));
        PerformanceStatistics lastHour = getStatistics(pluginId, Duration.ofHours(1));
        PerformanceStatistics last24Hours = getStatistics(pluginId, Duration.ofHours(24));
        
        return PerformanceReport.builder()
                .pluginId(pluginId)
                .reportTime(Instant.now())
                .totalMessagesProcessed(metrics.getMessagesProcessed().get())
                .successRate(metrics.getSuccessRate())
                .averageProcessingTime(metrics.getAverageProcessingTime())
                .last5Minutes(last5Minutes)
                .lastHour(lastHour)
                .last24Hours(last24Hours)
                .errorSummary(metrics.getErrorCounts())
                .resourceUsage(ResourceUsage.builder()
                        .currentMemory(metrics.getLastMemoryUsage())
                        .peakMemory(metrics.getPeakMemoryUsage())
                        .currentCpu(metrics.getLastCpuUsage())
                        .peakCpu(metrics.getPeakCpuUsage())
                        .build())
                .build();
    }
    
    /**
     * Reset metrics for a plugin
     */
    public void resetMetrics(String pluginId) {
        pluginMetrics.remove(pluginId);
        performanceHistory.remove(pluginId);
        log.info("Reset metrics for plugin: {}", pluginId);
    }
    
    private PluginMetrics getOrCreateMetrics(String pluginId) {
        return pluginMetrics.computeIfAbsent(pluginId, k -> new PluginMetrics(pluginId));
    }
    
    private void addPerformanceSample(String pluginId, PerformanceSample sample) {
        List<PerformanceSample> history = performanceHistory.computeIfAbsent(pluginId, k -> new ArrayList<>());
        
        synchronized (history) {
            history.add(sample);
            
            // Trim history if too large
            if (history.size() > HISTORY_SIZE) {
                history.subList(0, history.size() - HISTORY_SIZE).clear();
            }
        }
    }
    
    private List<PerformanceSample> getRecentSamples(String pluginId, Duration window) {
        List<PerformanceSample> history = performanceHistory.get(pluginId);
        if (history == null) {
            return Collections.emptyList();
        }
        
        Instant cutoff = Instant.now().minus(window);
        
        synchronized (history) {
            return history.stream()
                    .filter(sample -> sample.getTimestamp().isAfter(cutoff))
                    .collect(Collectors.toList());
        }
    }
    
    private PerformanceStatistics calculateStatistics(PluginMetrics metrics, List<PerformanceSample> samples) {
        if (samples.isEmpty()) {
            return PerformanceStatistics.empty();
        }
        
        long successCount = samples.stream().filter(PerformanceSample::isSuccess).count();
        double avgProcessingTime = samples.stream()
                .mapToLong(PerformanceSample::getProcessingTimeMs)
                .average()
                .orElse(0.0);
        
        long minTime = samples.stream()
                .mapToLong(PerformanceSample::getProcessingTimeMs)
                .min()
                .orElse(0);
        
        long maxTime = samples.stream()
                .mapToLong(PerformanceSample::getProcessingTimeMs)
                .max()
                .orElse(0);
        
        // Calculate percentiles
        List<Long> sortedTimes = samples.stream()
                .map(PerformanceSample::getProcessingTimeMs)
                .sorted()
                .collect(Collectors.toList());
        
        long p50 = getPercentile(sortedTimes, 50);
        long p95 = getPercentile(sortedTimes, 95);
        long p99 = getPercentile(sortedTimes, 99);
        
        return PerformanceStatistics.builder()
                .sampleCount(samples.size())
                .successCount(successCount)
                .successRate((double) successCount / samples.size() * 100)
                .averageProcessingTime(avgProcessingTime)
                .minProcessingTime(minTime)
                .maxProcessingTime(maxTime)
                .p50ProcessingTime(p50)
                .p95ProcessingTime(p95)
                .p99ProcessingTime(p99)
                .build();
    }
    
    private long getPercentile(List<Long> sortedValues, int percentile) {
        if (sortedValues.isEmpty()) {
            return 0;
        }
        
        int index = (int) Math.ceil(sortedValues.size() * percentile / 100.0) - 1;
        return sortedValues.get(Math.max(0, Math.min(index, sortedValues.size() - 1)));
    }
    
    /**
     * Plugin performance metrics
     */
    @Data
    public static class PluginMetrics {
        private final String pluginId;
        private final AtomicLong messagesProcessed = new AtomicLong();
        private final AtomicLong successfulMessages = new AtomicLong();
        private final AtomicLong failedMessages = new AtomicLong();
        private final AtomicLong totalProcessingTime = new AtomicLong();
        private final AtomicLong errors = new AtomicLong();
        private final AtomicLong successfulConnectionTests = new AtomicLong();
        private final AtomicLong failedConnectionTests = new AtomicLong();
        
        private volatile long minProcessingTime = Long.MAX_VALUE;
        private volatile long maxProcessingTime = 0;
        private volatile long lastMemoryUsage = 0;
        private volatile long peakMemoryUsage = 0;
        private volatile double lastCpuUsage = 0.0;
        private volatile double peakCpuUsage = 0.0;
        
        private final Map<String, Integer> errorCounts = new ConcurrentHashMap<>();
        
        public PluginMetrics(String pluginId) {
            this.pluginId = pluginId;
        }
        
        public double getSuccessRate() {
            long total = messagesProcessed.get();
            return total > 0 ? (double) successfulMessages.get() / total * 100 : 0;
        }
        
        public double getAverageProcessingTime() {
            long count = messagesProcessed.get();
            return count > 0 ? (double) totalProcessingTime.get() / count : 0;
        }
        
        public void updateMinMaxProcessingTime(long processingTime) {
            if (processingTime < minProcessingTime) {
                minProcessingTime = processingTime;
            }
            if (processingTime > maxProcessingTime) {
                maxProcessingTime = processingTime;
            }
        }
    }
    
    /**
     * Performance sample
     */
    @Data
    private static class PerformanceSample {
        private final Instant timestamp;
        private final long processingTimeMs;
        private final boolean success;
        private final String direction;
    }
    
    /**
     * Performance statistics
     */
    @Data
    @Builder
    public static class PerformanceStatistics {
        private long sampleCount;
        private long successCount;
        private double successRate;
        private double averageProcessingTime;
        private long minProcessingTime;
        private long maxProcessingTime;
        private long p50ProcessingTime;
        private long p95ProcessingTime;
        private long p99ProcessingTime;
        
        public static PerformanceStatistics empty() {
            return PerformanceStatistics.builder().build();
        }
    }
    
    /**
     * Performance report
     */
    @Data
    @Builder
    public static class PerformanceReport {
        private String pluginId;
        private Instant reportTime;
        private long totalMessagesProcessed;
        private double successRate;
        private double averageProcessingTime;
        private PerformanceStatistics last5Minutes;
        private PerformanceStatistics lastHour;
        private PerformanceStatistics last24Hours;
        private Map<String, Integer> errorSummary;
        private ResourceUsage resourceUsage;
    }
    
    /**
     * Resource usage
     */
    @Data
    @Builder
    public static class ResourceUsage {
        private long currentMemory;
        private long peakMemory;
        private double currentCpu;
        private double peakCpu;
    }
}