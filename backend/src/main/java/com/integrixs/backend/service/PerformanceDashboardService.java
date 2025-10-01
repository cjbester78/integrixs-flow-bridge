package com.integrixs.backend.service;

import com.integrixs.adapters.monitoring.PerformanceMetricsCollector;
import com.integrixs.adapters.monitoring.SLAMonitoringService;
import com.integrixs.backend.dto.dashboard.PerformanceSnapshot;
import com.integrixs.backend.dto.dashboard.RealTimeMetrics;
import com.integrixs.backend.dto.dashboard.ComponentPerformance;
import io.micrometer.core.instrument.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for real - time performance monitoring dashboard.
 */
@Service
public class PerformanceDashboardService {

    private static final Logger log = LoggerFactory.getLogger(PerformanceDashboardService.class);


    private final MeterRegistry meterRegistry;
    private final PerformanceMetricsCollector metricsCollector;
    private final SLAMonitoringService slaMonitoringService;

    // Real - time metrics cache
    private final Map<String, RealTimeMetrics> realTimeMetricsCache = new ConcurrentHashMap<>();

    // Historical snapshots(last 24 hours)
    private final List<PerformanceSnapshot> historicalSnapshots = Collections.synchronizedList(new ArrayList<>());

    // Update interval in seconds
    private static final int UPDATE_INTERVAL = 5;

    // Constructor
    @Autowired
    public PerformanceDashboardService(MeterRegistry meterRegistry,
                                     PerformanceMetricsCollector metricsCollector,
                                     SLAMonitoringService slaMonitoringService) {
        this.meterRegistry = meterRegistry;
        this.metricsCollector = metricsCollector;
        this.slaMonitoringService = slaMonitoringService;
    }

    /**
     * Get real - time performance metrics.
     */
    public RealTimeMetrics getRealTimeMetrics() {
        RealTimeMetrics metrics = new RealTimeMetrics();
        metrics.setTimestamp(LocalDateTime.now());

        // System metrics
        metrics.setSystemMetrics(collectSystemMetrics());

        // Component performance
        metrics.setComponentPerformance(collectComponentPerformance());

        // Active operations
        metrics.setActiveOperations(collectActiveOperations());

        // Recent errors
        metrics.setRecentErrors(collectRecentErrors());

        // SLA compliance
        metrics.setSlaCompliance(collectSlaCompliance());

        // Throughput rates
        metrics.setThroughputRates(collectThroughputRates());

        return metrics;
    }

    /**
     * Get performance snapshot for a specific time range.
     */
    public List<PerformanceSnapshot> getHistoricalSnapshots(LocalDateTime startTime, LocalDateTime endTime) {
        return historicalSnapshots.stream()
            .filter(snapshot -> !snapshot.getTimestamp().isBefore(startTime) &&
                               !snapshot.getTimestamp().isAfter(endTime))
            .collect(Collectors.toList());
    }

    /**
     * Get component - specific performance details.
     */
    public ComponentPerformance getComponentPerformance(String componentId) {
        ComponentPerformance performance = new ComponentPerformance();
        performance.setComponentId(componentId);
        performance.setTimestamp(LocalDateTime.now());

        // Collect component - specific metrics
        String metricPrefix = "adapter.operation.duration";

        Collection<Meter> meters = meterRegistry.find(metricPrefix)
            .tag("adapter.type", componentId)
            .meters();

        for(Meter meter : meters) {
            if(meter instanceof io.micrometer.core.instrument.Timer) {
                io.micrometer.core.instrument.Timer timer = (io.micrometer.core.instrument.Timer) meter;

                ComponentPerformance.OperationMetrics opMetrics = new ComponentPerformance.OperationMetrics();
                opMetrics.setOperationName(meter.getId().getTag("operation"));
                opMetrics.setCount(timer.count());
                opMetrics.setMeanDuration(timer.mean(TimeUnit.MILLISECONDS));
                opMetrics.setMaxDuration(timer.max(TimeUnit.MILLISECONDS));

                // Percentiles
                Map<String, Double> percentiles = new HashMap<>();
                percentiles.put("p50", timer.percentile(0.5, TimeUnit.MILLISECONDS));
                percentiles.put("p75", timer.percentile(0.75, TimeUnit.MILLISECONDS));
                percentiles.put("p95", timer.percentile(0.95, TimeUnit.MILLISECONDS));
                percentiles.put("p99", timer.percentile(0.99, TimeUnit.MILLISECONDS));
                opMetrics.setPercentiles(percentiles);

                performance.getOperationMetrics().add(opMetrics);
            }
        }

        // Resource usage
        performance.setResourceUsage(collectComponentResourceUsage(componentId));

        // Error statistics
        performance.setErrorStatistics(collectComponentErrorStats(componentId));

        return performance;
    }

    /**
     * Collect system - wide metrics.
     */
    private Map<String, Object> collectSystemMetrics() {
        Map<String, Object> systemMetrics = new HashMap<>();

        // JVM metrics
        Gauge heapGauge = meterRegistry.find("jvm.memory.used")
            .tag("area", "heap")
            .gauge();
        if(heapGauge != null) {
            systemMetrics.put("heapUsedMB", heapGauge.value() / (1024 * 1024));
        }

        Gauge maxHeapGauge = meterRegistry.find("jvm.memory.max")
            .tag("area", "heap")
            .gauge();
        if(maxHeapGauge != null) {
            systemMetrics.put("heapMaxMB", maxHeapGauge.value() / (1024 * 1024));
        }

        // CPU usage
        Gauge cpuGauge = meterRegistry.find("process.cpu.usage").gauge();
        if(cpuGauge != null) {
            systemMetrics.put("cpuUsagePercent", cpuGauge.value() * 100);
        }

        // Thread count
        Gauge threadGauge = meterRegistry.find("jvm.threads.live").gauge();
        if(threadGauge != null) {
            systemMetrics.put("threadCount", threadGauge.value());
        }

        // GC metrics
        Counter gcCounter = meterRegistry.find("jvm.gc.pause").counter();
        if(gcCounter != null) {
            systemMetrics.put("gcCount", gcCounter.count());
        }

        return systemMetrics;
    }

    /**
     * Collect component performance metrics.
     */
    private List<Map<String, Object>> collectComponentPerformance() {
        List<Map<String, Object>> componentMetrics = new ArrayList<>();

        // Get all adapter types
        Set<String> adapterTypes = new HashSet<>();
        meterRegistry.find("adapter.operation.duration")
            .meters()
            .forEach(meter -> {
                String adapterType = meter.getId().getTag("adapter.type");
                if(adapterType != null) {
                    adapterTypes.add(adapterType);
                }
            });

        // Collect metrics for each adapter type
        for(String adapterType : adapterTypes) {
            Map<String, Object> metrics = new HashMap<>();
            metrics.put("component", adapterType);

            // Success rate
            Counter successCounter = meterRegistry.find("adapter.operations.total")
                .tag("adapter.type", adapterType)
                .counter();

            Counter errorCounter = meterRegistry.find("adapter.operations.errors")
                .tag("adapter.type", adapterType)
                .counter();

            if(successCounter != null && errorCounter != null) {
                double total = successCounter.count();
                double errors = errorCounter.count();
                double successRate = total > 0 ? ((total - errors) / total) * 100 : 100;
                metrics.put("successRate", successRate);
                metrics.put("totalOperations", total);
            }

            // Average response time
            io.micrometer.core.instrument.Timer timer = meterRegistry.find("adapter.operation.duration")
                .tag("adapter.type", adapterType)
                .timer();

            if(timer != null) {
                metrics.put("avgResponseTime", timer.mean(TimeUnit.MILLISECONDS));
                metrics.put("maxResponseTime", timer.max(TimeUnit.MILLISECONDS));
            }

            // Active operations
            Gauge activeGauge = meterRegistry.find("adapter.operations.active")
                .tag("adapter.type", adapterType)
                .gauge();

            if(activeGauge != null) {
                metrics.put("activeOperations", activeGauge.value());
            }

            componentMetrics.add(metrics);
        }

        return componentMetrics;
    }

    /**
     * Collect active operations.
     */
    private List<Map<String, Object>> collectActiveOperations() {
        List<Map<String, Object>> activeOps = new ArrayList<>();

        Collection<Gauge> activeGauges = meterRegistry.find("adapter.operations.active").gauges();

        for(Gauge gauge : activeGauges) {
            if(gauge.value() > 0) {
                Map<String, Object> op = new HashMap<>();
                op.put("adapterType", gauge.getId().getTag("adapter.type"));
                op.put("adapterMode", gauge.getId().getTag("adapter.mode"));
                op.put("operation", gauge.getId().getTag("operation"));
                op.put("count", gauge.value());
                activeOps.add(op);
            }
        }

        return activeOps;
    }

    /**
     * Collect recent errors.
     */
    private List<Map<String, Object>> collectRecentErrors() {
        List<Map<String, Object>> recentErrors = new ArrayList<>();

        // Get error counters
        Collection<Counter> errorCounters = meterRegistry.find("adapter.operations.errors").counters();

        for(Counter counter : errorCounters) {
            Map<String, Object> error = new HashMap<>();
            error.put("adapterType", counter.getId().getTag("adapter.type"));
            error.put("operation", counter.getId().getTag("operation"));
            error.put("errorType", counter.getId().getTag("error.type"));
            error.put("count", counter.count());

            recentErrors.add(error);
        }

        // Sort by count descending and limit to top 10
        return recentErrors.stream()
            .sorted((a, b) -> Double.compare((Double) b.get("count"), (Double) a.get("count")))
            .limit(10)
            .collect(Collectors.toList());
    }

    /**
     * Collect SLA compliance metrics.
     */
    private Map<String, Object> collectSlaCompliance() {
        Map<String, Object> slaMetrics = new HashMap<>();

        // Get all SLA reports
        List<SLAMonitoringService.SLAComplianceReport> reports =
            slaMonitoringService.getAllComplianceReports();

        if(!reports.isEmpty()) {
            // Overall compliance
            double avgSuccessRate = reports.stream()
                .mapToDouble(SLAMonitoringService.SLAComplianceReport::getSuccessRate)
                .average()
                .orElse(100.0);

            double avgResponseCompliance = reports.stream()
                .mapToDouble(SLAMonitoringService.SLAComplianceReport::getResponseTimeCompliance)
                .average()
                .orElse(100.0);

            slaMetrics.put("overallSuccessRate", avgSuccessRate);
            slaMetrics.put("overallResponseCompliance", avgResponseCompliance);

            // Violations
            Counter violationCounter = meterRegistry.find("adapter.sla.violations").counter();
            if(violationCounter != null) {
                slaMetrics.put("totalViolations", violationCounter.count());
            }

            // Per - operation compliance
            List<Map<String, Object>> operationCompliance = new ArrayList<>();
            for(SLAMonitoringService.SLAComplianceReport report : reports) {
                Map<String, Object> opCompliance = new HashMap<>();
                opCompliance.put("operation", report.getOperationName());
                opCompliance.put("successRate", report.getSuccessRate());
                opCompliance.put("responseCompliance", report.getResponseTimeCompliance());
                opCompliance.put("avgResponseTime", report.getAvgResponseTimeMs());
                operationCompliance.add(opCompliance);
            }
            slaMetrics.put("operationCompliance", operationCompliance);
        }

        return slaMetrics;
    }

    /**
     * Collect throughput rates.
     */
    private Map<String, Double> collectThroughputRates() {
        Map<String, Double> throughputRates = new HashMap<>();

        // Calculate rates for different time windows
        Collection<Counter> counters = meterRegistry.find("adapter.operations.total").counters();

        for(Counter counter : counters) {
            String key = counter.getId().getTag("adapter.type") + "-" +
                        counter.getId().getTag("operation");

            // Simple rate calculation(would need time - series data for accurate rates)
            double rate = counter.count() / 60.0; // Operations per minute
            throughputRates.put(key, rate);
        }

        return throughputRates;
    }

    /**
     * Collect component resource usage.
     */
    private Map<String, Object> collectComponentResourceUsage(String componentId) {
        Map<String, Object> resourceUsage = new HashMap<>();

        // Connection pool metrics
        Gauge activeConnections = meterRegistry.find("adapter.connections.active")
            .tag("adapter.type", componentId)
            .gauge();

        if(activeConnections != null) {
            resourceUsage.put("activeConnections", activeConnections.value());
        }

        // Queue metrics
        Gauge queueSize = meterRegistry.find("adapter.queue.size")
            .tag("adapter.type", componentId)
            .gauge();

        if(queueSize != null) {
            resourceUsage.put("queueSize", queueSize.value());
        }

        // Memory usage(component - specific if available)
        resourceUsage.put("estimatedMemoryMB", estimateComponentMemoryUsage(componentId));

        return resourceUsage;
    }

    /**
     * Collect component error statistics.
     */
    private Map<String, Object> collectComponentErrorStats(String componentId) {
        Map<String, Object> errorStats = new HashMap<>();

        // Error types and counts
        Map<String, Long> errorTypes = new HashMap<>();

        Collection<Counter> errorCounters = meterRegistry.find("adapter.operations.errors")
            .tag("adapter.type", componentId)
            .counters();

        for(Counter counter : errorCounters) {
            String errorType = counter.getId().getTag("error.type");
            errorTypes.put(errorType, (long) counter.count());
        }

        errorStats.put("errorTypes", errorTypes);
        errorStats.put("totalErrors", errorTypes.values().stream().mapToLong(Long::longValue).sum());

        return errorStats;
    }

    /**
     * Estimate component memory usage.
     */
    private double estimateComponentMemoryUsage(String componentId) {
        // This is a rough estimate based on operations
        io.micrometer.core.instrument.Timer timer = meterRegistry.find("adapter.operation.duration")
            .tag("adapter.type", componentId)
            .timer();

        if(timer != null) {
            // Estimate based on operation count
            return timer.count() * 0.001; // 1KB per operation(very rough estimate)
        }

        return 0.0;
    }

    /**
     * Capture performance snapshot periodically.
     */
    @Scheduled(fixedDelay = 60000) // Every minute
    public void capturePerformanceSnapshot() {
        try {
            PerformanceSnapshot snapshot = new PerformanceSnapshot();
            snapshot.setTimestamp(LocalDateTime.now());

            // System metrics
            snapshot.setCpuUsage(getCpuUsage());
            snapshot.setMemoryUsage(getMemoryUsage());
            snapshot.setThreadCount(getThreadCount());

            // Throughput
            snapshot.setTotalThroughput(getTotalThroughput());

            // Error rate
            snapshot.setErrorRate(getOverallErrorRate());

            // Component summaries
            snapshot.setComponentSummaries(getComponentSummaries());

            // Add to historical snapshots
            historicalSnapshots.add(snapshot);

            // Keep only last 24 hours
            LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
            historicalSnapshots.removeIf(s -> s.getTimestamp().isBefore(cutoff));

        } catch(Exception e) {
            log.error("Error capturing performance snapshot", e);
        }
    }

    /**
     * Get CPU usage.
     */
    private double getCpuUsage() {
        Gauge gauge = meterRegistry.find("process.cpu.usage").gauge();
        return gauge != null ? gauge.value() * 100 : 0.0;
    }

    /**
     * Get memory usage.
     */
    private double getMemoryUsage() {
        Gauge usedGauge = meterRegistry.find("jvm.memory.used").tag("area", "heap").gauge();
        Gauge maxGauge = meterRegistry.find("jvm.memory.max").tag("area", "heap").gauge();

        if(usedGauge != null && maxGauge != null && maxGauge.value() > 0) {
            return(usedGauge.value() / maxGauge.value()) * 100;
        }

        return 0.0;
    }

    /**
     * Get thread count.
     */
    private int getThreadCount() {
        Gauge gauge = meterRegistry.find("jvm.threads.live").gauge();
        return gauge != null ? (int) gauge.value() : 0;
    }

    /**
     * Get total throughput.
     */
    private double getTotalThroughput() {
        return meterRegistry.find("adapter.operations.total")
            .counters()
            .stream()
            .mapToDouble(Counter::count)
            .sum();
    }

    /**
     * Get overall error rate.
     */
    private double getOverallErrorRate() {
        double totalOps = getTotalThroughput();
        double totalErrors = meterRegistry.find("adapter.operations.errors")
            .counters()
            .stream()
            .mapToDouble(Counter::count)
            .sum();

        return totalOps > 0 ? (totalErrors / totalOps) * 100 : 0.0;
    }

    /**
     * Get component summaries.
     */
    private List<Map<String, Object>> getComponentSummaries() {
        // Similar to collectComponentPerformance but more condensed
        return collectComponentPerformance();
    }
}
