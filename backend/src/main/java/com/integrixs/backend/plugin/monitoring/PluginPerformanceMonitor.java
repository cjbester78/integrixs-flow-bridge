package com.integrixs.backend.plugin.monitoring;

import com.integrixs.backend.plugin.api.PluginMessage;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performance monitoring for plugins
 */
@Component
public class PluginPerformanceMonitor {

    private static final Logger log = LoggerFactory.getLogger(PluginPerformanceMonitor.class);


    private final MeterRegistry meterRegistry;

    // Performance metrics storage
    private final Map<String, PluginMetrics> pluginMetrics = new ConcurrentHashMap<>();
    private final Map<String, List<PerformanceSample>> performanceHistory = new ConcurrentHashMap<>();

    // Configuration
    private static final int HISTORY_SIZE = 1000;
    private static final Duration AGGREGATION_WINDOW = Duration.ofMinutes(5);

    // Constructor
    public PluginPerformanceMonitor(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * Record message processing
     */
    public void recordMessageProcessed(String pluginId, String direction, long processingTimeMs, boolean success) {
        PluginMetrics metrics = getOrCreateMetrics(pluginId);

        if(success) {
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

        if(success) {
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

        if(success && responseTimeMs > 0) {
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
        if(memoryUsed > metrics.getPeakMemoryUsage()) {
            metrics.setPeakMemoryUsage(memoryUsed);
        }
        if(cpuUsage > metrics.getPeakCpuUsage()) {
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
        if(metrics == null) {
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
        if(metrics == null) {
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

        synchronized(history) {
            history.add(sample);

            // Trim history if too large
            if(history.size() > HISTORY_SIZE) {
                history.subList(0, history.size() - HISTORY_SIZE).clear();
            }
        }
    }

    private List<PerformanceSample> getRecentSamples(String pluginId, Duration window) {
        List<PerformanceSample> history = performanceHistory.get(pluginId);
        if(history == null) {
            return Collections.emptyList();
        }

        Instant cutoff = Instant.now().minus(window);

        synchronized(history) {
            return history.stream()
                    .filter(sample -> sample.getTimestamp().isAfter(cutoff))
                    .collect(Collectors.toList());
        }
    }

    private PerformanceStatistics calculateStatistics(PluginMetrics metrics, List<PerformanceSample> samples) {
        if(samples.isEmpty()) {
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
        if(sortedValues.isEmpty()) {
            return 0;
        }

        int index = (int) Math.ceil(sortedValues.size() * percentile / 100.0) - 1;
        return sortedValues.get(Math.max(0, Math.min(index, sortedValues.size() - 1)));
    }

    /**
     * Plugin performance metrics
     */
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
            if(processingTime < minProcessingTime) {
                minProcessingTime = processingTime;
            }
            if(processingTime > maxProcessingTime) {
                maxProcessingTime = processingTime;
            }
        }

        // Getters and Setters
        public String getPluginId() {
            return pluginId;
        }

        public AtomicLong getMessagesProcessed() {
            return messagesProcessed;
        }

        public AtomicLong getSuccessfulMessages() {
            return successfulMessages;
        }

        public AtomicLong getFailedMessages() {
            return failedMessages;
        }

        public AtomicLong getTotalProcessingTime() {
            return totalProcessingTime;
        }

        public AtomicLong getErrors() {
            return errors;
        }

        public AtomicLong getSuccessfulConnectionTests() {
            return successfulConnectionTests;
        }

        public AtomicLong getFailedConnectionTests() {
            return failedConnectionTests;
        }

        public long getMinProcessingTime() {
            return minProcessingTime;
        }

        public void setMinProcessingTime(long minProcessingTime) {
            this.minProcessingTime = minProcessingTime;
        }

        public long getMaxProcessingTime() {
            return maxProcessingTime;
        }

        public void setMaxProcessingTime(long maxProcessingTime) {
            this.maxProcessingTime = maxProcessingTime;
        }

        public long getLastMemoryUsage() {
            return lastMemoryUsage;
        }

        public void setLastMemoryUsage(long lastMemoryUsage) {
            this.lastMemoryUsage = lastMemoryUsage;
        }

        public long getPeakMemoryUsage() {
            return peakMemoryUsage;
        }

        public void setPeakMemoryUsage(long peakMemoryUsage) {
            this.peakMemoryUsage = peakMemoryUsage;
        }

        public double getLastCpuUsage() {
            return lastCpuUsage;
        }

        public void setLastCpuUsage(double lastCpuUsage) {
            this.lastCpuUsage = lastCpuUsage;
        }

        public double getPeakCpuUsage() {
            return peakCpuUsage;
        }

        public void setPeakCpuUsage(double peakCpuUsage) {
            this.peakCpuUsage = peakCpuUsage;
        }

        public Map<String, Integer> getErrorCounts() {
            return errorCounts;
        }
    }

    /**
     * Performance sample
     */
        private static class PerformanceSample {
        private final Instant timestamp;
        private final long processingTimeMs;
        private final boolean success;
        private final String direction;

        public PerformanceSample(Instant timestamp, long processingTimeMs, boolean success, String direction) {
            this.timestamp = timestamp;
            this.processingTimeMs = processingTimeMs;
            this.success = success;
            this.direction = direction;
        }

        public Instant getTimestamp() {
            return timestamp;
        }

        public long getProcessingTimeMs() {
            return processingTimeMs;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getDirection() {
            return direction;
        }
    }

    /**
     * Performance statistics
     */
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

        // Getters and Setters
        public long getSampleCount() {
            return sampleCount;
        }

        public void setSampleCount(long sampleCount) {
            this.sampleCount = sampleCount;
        }

        public long getSuccessCount() {
            return successCount;
        }

        public void setSuccessCount(long successCount) {
            this.successCount = successCount;
        }

        public double getSuccessRate() {
            return successRate;
        }

        public void setSuccessRate(double successRate) {
            this.successRate = successRate;
        }

        public double getAverageProcessingTime() {
            return averageProcessingTime;
        }

        public void setAverageProcessingTime(double averageProcessingTime) {
            this.averageProcessingTime = averageProcessingTime;
        }

        public long getMinProcessingTime() {
            return minProcessingTime;
        }

        public void setMinProcessingTime(long minProcessingTime) {
            this.minProcessingTime = minProcessingTime;
        }

        public long getMaxProcessingTime() {
            return maxProcessingTime;
        }

        public void setMaxProcessingTime(long maxProcessingTime) {
            this.maxProcessingTime = maxProcessingTime;
        }

        public long getP50ProcessingTime() {
            return p50ProcessingTime;
        }

        public void setP50ProcessingTime(long p50ProcessingTime) {
            this.p50ProcessingTime = p50ProcessingTime;
        }

        public long getP95ProcessingTime() {
            return p95ProcessingTime;
        }

        public void setP95ProcessingTime(long p95ProcessingTime) {
            this.p95ProcessingTime = p95ProcessingTime;
        }

        public long getP99ProcessingTime() {
            return p99ProcessingTime;
        }

        public void setP99ProcessingTime(long p99ProcessingTime) {
            this.p99ProcessingTime = p99ProcessingTime;
        }

        // Builder pattern
        public static PerformanceStatisticsBuilder builder() {
            return new PerformanceStatisticsBuilder();
        }

        public static class PerformanceStatisticsBuilder {
            private long sampleCount;
            private long successCount;
            private double successRate;
            private double averageProcessingTime;
            private long minProcessingTime;
            private long maxProcessingTime;
            private long p50ProcessingTime;
            private long p95ProcessingTime;
            private long p99ProcessingTime;

            public PerformanceStatisticsBuilder sampleCount(long sampleCount) {
                this.sampleCount = sampleCount;
                return this;
            }

            public PerformanceStatisticsBuilder successCount(long successCount) {
                this.successCount = successCount;
                return this;
            }

            public PerformanceStatisticsBuilder successRate(double successRate) {
                this.successRate = successRate;
                return this;
            }

            public PerformanceStatisticsBuilder averageProcessingTime(double averageProcessingTime) {
                this.averageProcessingTime = averageProcessingTime;
                return this;
            }

            public PerformanceStatisticsBuilder minProcessingTime(long minProcessingTime) {
                this.minProcessingTime = minProcessingTime;
                return this;
            }

            public PerformanceStatisticsBuilder maxProcessingTime(long maxProcessingTime) {
                this.maxProcessingTime = maxProcessingTime;
                return this;
            }

            public PerformanceStatisticsBuilder p50ProcessingTime(long p50ProcessingTime) {
                this.p50ProcessingTime = p50ProcessingTime;
                return this;
            }

            public PerformanceStatisticsBuilder p95ProcessingTime(long p95ProcessingTime) {
                this.p95ProcessingTime = p95ProcessingTime;
                return this;
            }

            public PerformanceStatisticsBuilder p99ProcessingTime(long p99ProcessingTime) {
                this.p99ProcessingTime = p99ProcessingTime;
                return this;
            }

            public PerformanceStatistics build() {
                PerformanceStatistics stats = new PerformanceStatistics();
                stats.sampleCount = this.sampleCount;
                stats.successCount = this.successCount;
                stats.successRate = this.successRate;
                stats.averageProcessingTime = this.averageProcessingTime;
                stats.minProcessingTime = this.minProcessingTime;
                stats.maxProcessingTime = this.maxProcessingTime;
                stats.p50ProcessingTime = this.p50ProcessingTime;
                stats.p95ProcessingTime = this.p95ProcessingTime;
                stats.p99ProcessingTime = this.p99ProcessingTime;
                return stats;
            }
        }
    }

    /**
     * Performance report
     */
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

        // Getters and Setters
        public String getPluginId() {
            return pluginId;
        }

        public void setPluginId(String pluginId) {
            this.pluginId = pluginId;
        }

        public Instant getReportTime() {
            return reportTime;
        }

        public void setReportTime(Instant reportTime) {
            this.reportTime = reportTime;
        }

        public long getTotalMessagesProcessed() {
            return totalMessagesProcessed;
        }

        public void setTotalMessagesProcessed(long totalMessagesProcessed) {
            this.totalMessagesProcessed = totalMessagesProcessed;
        }

        public double getSuccessRate() {
            return successRate;
        }

        public void setSuccessRate(double successRate) {
            this.successRate = successRate;
        }

        public double getAverageProcessingTime() {
            return averageProcessingTime;
        }

        public void setAverageProcessingTime(double averageProcessingTime) {
            this.averageProcessingTime = averageProcessingTime;
        }

        public PerformanceStatistics getLast5Minutes() {
            return last5Minutes;
        }

        public void setLast5Minutes(PerformanceStatistics last5Minutes) {
            this.last5Minutes = last5Minutes;
        }

        public PerformanceStatistics getLastHour() {
            return lastHour;
        }

        public void setLastHour(PerformanceStatistics lastHour) {
            this.lastHour = lastHour;
        }

        public PerformanceStatistics getLast24Hours() {
            return last24Hours;
        }

        public void setLast24Hours(PerformanceStatistics last24Hours) {
            this.last24Hours = last24Hours;
        }

        public Map<String, Integer> getErrorSummary() {
            return errorSummary;
        }

        public void setErrorSummary(Map<String, Integer> errorSummary) {
            this.errorSummary = errorSummary;
        }

        public ResourceUsage getResourceUsage() {
            return resourceUsage;
        }

        public void setResourceUsage(ResourceUsage resourceUsage) {
            this.resourceUsage = resourceUsage;
        }

        // Builder pattern
        public static PerformanceReportBuilder builder() {
            return new PerformanceReportBuilder();
        }

        public static class PerformanceReportBuilder {
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

            public PerformanceReportBuilder pluginId(String pluginId) {
                this.pluginId = pluginId;
                return this;
            }

            public PerformanceReportBuilder reportTime(Instant reportTime) {
                this.reportTime = reportTime;
                return this;
            }

            public PerformanceReportBuilder totalMessagesProcessed(long totalMessagesProcessed) {
                this.totalMessagesProcessed = totalMessagesProcessed;
                return this;
            }

            public PerformanceReportBuilder successRate(double successRate) {
                this.successRate = successRate;
                return this;
            }

            public PerformanceReportBuilder averageProcessingTime(double averageProcessingTime) {
                this.averageProcessingTime = averageProcessingTime;
                return this;
            }

            public PerformanceReportBuilder last5Minutes(PerformanceStatistics last5Minutes) {
                this.last5Minutes = last5Minutes;
                return this;
            }

            public PerformanceReportBuilder lastHour(PerformanceStatistics lastHour) {
                this.lastHour = lastHour;
                return this;
            }

            public PerformanceReportBuilder last24Hours(PerformanceStatistics last24Hours) {
                this.last24Hours = last24Hours;
                return this;
            }

            public PerformanceReportBuilder errorSummary(Map<String, Integer> errorSummary) {
                this.errorSummary = errorSummary;
                return this;
            }

            public PerformanceReportBuilder resourceUsage(ResourceUsage resourceUsage) {
                this.resourceUsage = resourceUsage;
                return this;
            }

            public PerformanceReport build() {
                PerformanceReport report = new PerformanceReport();
                report.pluginId = this.pluginId;
                report.reportTime = this.reportTime;
                report.totalMessagesProcessed = this.totalMessagesProcessed;
                report.successRate = this.successRate;
                report.averageProcessingTime = this.averageProcessingTime;
                report.last5Minutes = this.last5Minutes;
                report.lastHour = this.lastHour;
                report.last24Hours = this.last24Hours;
                report.errorSummary = this.errorSummary;
                report.resourceUsage = this.resourceUsage;
                return report;
            }
        }
    }

    /**
     * Resource usage
     */
            public static class ResourceUsage {
        private long currentMemory;
        private long peakMemory;
        private double currentCpu;
        private double peakCpu;

        // Getters and Setters
        public long getCurrentMemory() {
            return currentMemory;
        }

        public void setCurrentMemory(long currentMemory) {
            this.currentMemory = currentMemory;
        }

        public long getPeakMemory() {
            return peakMemory;
        }

        public void setPeakMemory(long peakMemory) {
            this.peakMemory = peakMemory;
        }

        public double getCurrentCpu() {
            return currentCpu;
        }

        public void setCurrentCpu(double currentCpu) {
            this.currentCpu = currentCpu;
        }

        public double getPeakCpu() {
            return peakCpu;
        }

        public void setPeakCpu(double peakCpu) {
            this.peakCpu = peakCpu;
        }

        // Builder pattern
        public static ResourceUsageBuilder builder() {
            return new ResourceUsageBuilder();
        }

        public static class ResourceUsageBuilder {
            private long currentMemory;
            private long peakMemory;
            private double currentCpu;
            private double peakCpu;

            public ResourceUsageBuilder currentMemory(long currentMemory) {
                this.currentMemory = currentMemory;
                return this;
            }

            public ResourceUsageBuilder peakMemory(long peakMemory) {
                this.peakMemory = peakMemory;
                return this;
            }

            public ResourceUsageBuilder currentCpu(double currentCpu) {
                this.currentCpu = currentCpu;
                return this;
            }

            public ResourceUsageBuilder peakCpu(double peakCpu) {
                this.peakCpu = peakCpu;
                return this;
            }

            public ResourceUsage build() {
                ResourceUsage usage = new ResourceUsage();
                usage.currentMemory = this.currentMemory;
                usage.peakMemory = this.peakMemory;
                usage.currentCpu = this.currentCpu;
                usage.peakCpu = this.peakCpu;
                return usage;
            }
        }
    }
}
