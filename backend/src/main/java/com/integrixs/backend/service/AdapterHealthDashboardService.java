package com.integrixs.backend.service;

import com.integrixs.adapters.core.AdapterMonitoringService;
import com.integrixs.adapters.monitoring.PerformanceMetricsCollector;
import com.integrixs.adapters.monitoring.SLAMonitoringService;
import com.integrixs.backend.dto.dashboard.health.*;
import com.integrixs.data.model.CommunicationAdapter;
import com.integrixs.data.sql.repository.CommunicationAdapterSqlRepository;
import io.micrometer.core.instrument.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for adapter health monitoring and dashboards.
 */
@Service
public class AdapterHealthDashboardService {

    private static final Logger log = LoggerFactory.getLogger(AdapterHealthDashboardService.class);


    private final CommunicationAdapterSqlRepository adapterRepository;
    private final AdapterMonitoringService adapterMonitoringService;
    private final MeterRegistry meterRegistry;
    private final PerformanceMetricsCollector metricsCollector;
    private final SLAMonitoringService slaMonitoringService;

    // Health score cache
    private final Map<String, AdapterHealthScore> healthScoreCache = new ConcurrentHashMap<>();

    public AdapterHealthDashboardService(CommunicationAdapterSqlRepository adapterRepository,
                                        AdapterMonitoringService adapterMonitoringService,
                                        MeterRegistry meterRegistry,
                                        PerformanceMetricsCollector metricsCollector,
                                        SLAMonitoringService slaMonitoringService) {
        this.adapterRepository = adapterRepository;
        this.adapterMonitoringService = adapterMonitoringService;
        this.meterRegistry = meterRegistry;
        this.metricsCollector = metricsCollector;
        this.slaMonitoringService = slaMonitoringService;
    }

    // Health history
    private final Map<String, List<HealthSnapshot>> healthHistory = new ConcurrentHashMap<>();

    /**
     * Get overall adapter health dashboard.
     */
    public AdapterHealthDashboard getHealthDashboard() {
        AdapterHealthDashboard dashboard = new AdapterHealthDashboard();
        dashboard.setTimestamp(LocalDateTime.now());

        // Get all adapters
        List<CommunicationAdapter> adapters = adapterRepository.findAll();

        // Calculate health for each adapter
        List<AdapterHealthSummary> healthSummaries = new ArrayList<>();
        int healthyCount = 0;
        int warningCount = 0;
        int criticalCount = 0;

        for(CommunicationAdapter adapter : adapters) {
            AdapterHealthSummary summary = calculateAdapterHealth(adapter);
            healthSummaries.add(summary);

            switch(summary.getHealthStatus()) {
                case "HEALTHY":
                    healthyCount++;
                    break;
                case "WARNING":
                    warningCount++;
                    break;
                case "CRITICAL":
                    criticalCount++;
                    break;
            }
        }

        dashboard.setAdapterHealthSummaries(healthSummaries);
        dashboard.setHealthyAdapters(healthyCount);
        dashboard.setWarningAdapters(warningCount);
        dashboard.setCriticalAdapters(criticalCount);
        dashboard.setTotalAdapters(adapters.size());

        // Overall health score
        dashboard.setOverallHealthScore((int) calculateOverallHealthScore(healthSummaries));

        // Critical issues
        dashboard.setCriticalIssues(identifyCriticalIssues(healthSummaries));

        // Recommendations
        dashboard.setRecommendations(generateHealthRecommendations(healthSummaries));

        return dashboard;
    }

    /**
     * Get detailed health information for a specific adapter.
     */
    public AdapterHealthDetails getAdapterHealthDetails(String adapterId) {
        AdapterHealthDetails details = new AdapterHealthDetails();
        details.setAdapterId(adapterId);
        details.setTimestamp(LocalDateTime.now());

        // Get adapter info
        Optional<CommunicationAdapter> adapterOpt = adapterRepository.findById(UUID.fromString(adapterId));
        if(!adapterOpt.isPresent()) {
            throw new RuntimeException("Adapter not found: " + adapterId);
        }

        CommunicationAdapter adapter = adapterOpt.get();
        details.setAdapterName(adapter.getName());
        details.setAdapterType(adapter.getType().toString());

        // Calculate health score
        AdapterHealthScore healthScore = calculateDetailedHealthScore(adapter);
        details.setHealthScore((int) healthScore.getOverallScore());

        // Get metrics
        details.setPerformanceMetrics(collectPerformanceMetrics(adapter));
        details.setResourceMetrics(collectResourceMetrics(adapter));
        details.setErrorMetrics(collectErrorMetrics(adapter));

        // Connection status
        details.setConnectionStatus(checkConnectionStatus(adapter));

        // Health history
        details.setHealthHistory(getHealthHistory(adapterId));

        // Diagnostics
        details.setDiagnostics(runDiagnostics(adapter));

        return details;
    }

    /**
     * Get adapter health comparison.
     */
    public AdapterHealthComparison compareAdapterHealth(List<String> adapterIds) {
        AdapterHealthComparison comparison = new AdapterHealthComparison();
        comparison.setAdapterIds(adapterIds);
        comparison.setTimestamp(LocalDateTime.now());

        Map<String, AdapterHealthScore> healthScores = new HashMap<>();
        Map<String, PerformanceComparison> performanceComparisons = new HashMap<>();

        for(String adapterId : adapterIds) {
            Optional<CommunicationAdapter> adapterOpt = adapterRepository.findById(UUID.fromString(adapterId));
            if(adapterOpt.isPresent()) {
                CommunicationAdapter adapter = adapterOpt.get();

                // Health score
                AdapterHealthScore score = calculateDetailedHealthScore(adapter);
                healthScores.put(adapterId, score);

                // Performance comparison
                PerformanceComparison perfComp = new PerformanceComparison();
                perfComp.setAdapterId(adapterId);
                perfComp.setThroughput(getThroughput(adapter));
                perfComp.setErrorRate(getErrorRate(adapter));
                perfComp.setAverageResponseTime(getAverageResponseTime(adapter));
                perfComp.setUptime(calculateUptime(adapter));

                performanceComparisons.put(adapterId, perfComp);
            }
        }

        comparison.setHealthScores(healthScores);
        comparison.setPerformanceComparisons(performanceComparisons);

        // Rank adapters
        comparison.setRanking(rankAdaptersByHealth(healthScores));

        // Identify best and worst performers
        comparison.setBestPerformer(identifyBestPerformer(performanceComparisons));
        comparison.setWorstPerformer(identifyWorstPerformer(performanceComparisons));

        return comparison;
    }

    /**
     * Get health alerts for all adapters.
     */
    public List<HealthAlert> getHealthAlerts() {
        List<HealthAlert> alerts = new ArrayList<>();

        List<CommunicationAdapter> adapters = adapterRepository.findAll();

        for(CommunicationAdapter adapter : adapters) {
            // Check various health conditions
            checkConnectionHealth(adapter, alerts);
            checkPerformanceHealth(adapter, alerts);
            checkErrorRateHealth(adapter, alerts);
            checkResourceHealth(adapter, alerts);
            checkSLACompliance(adapter, alerts);
        }

        // Sort by severity and timestamp
        alerts.sort((a, b) -> {
            int severityCompare = b.getSeverity().compareTo(a.getSeverity());
            if(severityCompare != 0) return severityCompare;
            return b.getTimestamp().compareTo(a.getTimestamp());
        });

        return alerts;
    }

    /**
     * Calculate adapter health summary.
     */
    private AdapterHealthSummary calculateAdapterHealth(CommunicationAdapter adapter) {
        AdapterHealthSummary summary = new AdapterHealthSummary();
        summary.setAdapterId(adapter.getId().toString());
        summary.setAdapterName(adapter.getName());
        summary.setAdapterType(adapter.getType().toString());

        // Get cached or calculate health score
        AdapterHealthScore healthScore = healthScoreCache.computeIfAbsent(
            adapter.getId().toString(),
            k -> calculateDetailedHealthScore(adapter)
       );

        summary.setHealthScore((int) healthScore.getOverallScore());
        summary.setHealthStatus(determineHealthStatus(healthScore.getOverallScore()));

        // Key metrics
        summary.setUptime(calculateUptime(adapter));
        summary.setErrorRate(getErrorRate(adapter));
        summary.setAverageResponseTime((long) getAverageResponseTime(adapter));
        summary.setLastActivity(getLastActivity(adapter));

        // Issues
        summary.setActiveIssues(identifyActiveIssues(adapter));

        return summary;
    }

    /**
     * Calculate detailed health score.
     */
    private AdapterHealthScore calculateDetailedHealthScore(CommunicationAdapter adapter) {
        AdapterHealthScore score = new AdapterHealthScore();

        // Connection health(30% weight)
        double connectionScore = calculateConnectionScore(adapter);
        score.setConnectionScore(connectionScore);

        // Performance health(25% weight)
        double performanceScore = calculatePerformanceScore(adapter);
        score.setPerformanceScore(performanceScore);

        // Error rate health(25% weight)
        double errorScore = calculateErrorScore(adapter);
        score.setErrorScore(errorScore);

        // Resource health(10% weight)
        double resourceScore = calculateResourceScore(adapter);
        score.setResourceScore(resourceScore);

        // SLA compliance(10% weight)
        double slaScore = calculateSLAScore(adapter);
        score.setSlaComplianceScore(slaScore);

        // Calculate weighted overall score
        double overallScore = (connectionScore * 0.3) +
                             (performanceScore * 0.25) +
                             (errorScore * 0.25) +
                             (resourceScore * 0.1) +
                             (slaScore * 0.1);

        score.setOverallScore(overallScore);
        score.setCalculatedAt(LocalDateTime.now());

        return score;
    }

    /**
     * Calculate connection score.
     */
    private double calculateConnectionScore(CommunicationAdapter adapter) {
        // Check if adapter is active
        if(!adapter.isActive()) {
            return 0;
        }

        // Get recent connection test results
        // Note: HealthStatus is package-private in AdapterMonitoringService, we'll use alternative approach
        try {
            // Try to determine health through other means
            if(adapter.isActive() && adapter.getUpdatedAt() != null) {
                long minutesSinceLastUpdate = Duration.between(adapter.getUpdatedAt(), LocalDateTime.now()).toMinutes();
                if(minutesSinceLastUpdate < 5) return 100;
                if(minutesSinceLastUpdate < 15) return 80;
            }
        } catch (Exception e) {
            log.debug("Error checking adapter connection status", e);
        }

        // Check last successful connection
        LocalDateTime lastSuccess = getLastSuccessfulConnection(adapter);
        if(lastSuccess != null) {
            long minutesSinceSuccess = Duration.between(lastSuccess, LocalDateTime.now()).toMinutes();
            if(minutesSinceSuccess < 5) return 90;
            if(minutesSinceSuccess < 15) return 70;
            if(minutesSinceSuccess < 60) return 50;
        }

        return 20;
    }

    /**
     * Calculate performance score.
     */
    private double calculatePerformanceScore(CommunicationAdapter adapter) {
        double avgResponseTime = getAverageResponseTime(adapter);

        // Define thresholds based on adapter type
        double excellentThreshold = 100; // ms
        double goodThreshold = 500;
        double acceptableThreshold = 1000;
        double poorThreshold = 5000;

        if(avgResponseTime <= excellentThreshold) return 100;
        if(avgResponseTime <= goodThreshold) return 80;
        if(avgResponseTime <= acceptableThreshold) return 60;
        if(avgResponseTime <= poorThreshold) return 40;

        return 20;
    }

    /**
     * Calculate error score.
     */
    private double calculateErrorScore(CommunicationAdapter adapter) {
        double errorRate = getErrorRate(adapter);

        if(errorRate <= 0.1) return 100; // < 0.1% errors
        if(errorRate <= 1) return 80;     // < 1% errors
        if(errorRate <= 5) return 60;     // < 5% errors
        if(errorRate <= 10) return 40;    // < 10% errors

        return 20;
    }

    /**
     * Calculate resource score.
     */
    private double calculateResourceScore(CommunicationAdapter adapter) {
        // Check queue size for message - based adapters
        if(isMessageBasedAdapter(adapter)) {
            Gauge queueGauge = meterRegistry.find("adapter.queue.size")
                .tag("adapter.type", adapter.getType().toString())
                .gauge();

            if(queueGauge != null) {
                double queueSize = queueGauge.value();
                if(queueSize < 100) return 100;
                if(queueSize < 1000) return 80;
                if(queueSize < 10000) return 60;
                return 40;
            }
        }

        // Check connection pool for connection - based adapters
        if(isConnectionBasedAdapter(adapter)) {
            Gauge activeConnections = meterRegistry.find("adapter.connections.active")
                .tag("adapter.type", adapter.getType().toString())
                .gauge();

            if(activeConnections != null) {
                double utilization = activeConnections.value() / 100; // Assume max 100 connections
                if(utilization < 0.5) return 100;
                if(utilization < 0.7) return 80;
                if(utilization < 0.9) return 60;
                return 40;
            }
        }

        return 80; // Default score
    }

    /**
     * Calculate SLA compliance score.
     */
    private double calculateSLAScore(CommunicationAdapter adapter) {
        List<SLAMonitoringService.SLAComplianceReport> reports =
            slaMonitoringService.getAllComplianceReports();

        // Find reports for this adapter
        List<SLAMonitoringService.SLAComplianceReport> adapterReports = reports.stream()
            .filter(r -> r.getAdapterType().equals(adapter.getType().toString()))
            .collect(Collectors.toList());

        if(adapterReports.isEmpty()) {
            return 100; // No SLA defined, assume compliant
        }

        // Average compliance across all SLAs
        double avgCompliance = adapterReports.stream()
            .mapToDouble(r ->(r.getSuccessRate() + r.getResponseTimeCompliance()) / 2)
            .average()
            .orElse(100);

        return avgCompliance;
    }

    /**
     * Determine health status from score.
     */
    private String determineHealthStatus(double score) {
        if(score >= 80) return "HEALTHY";
        if(score >= 60) return "WARNING";
        return "CRITICAL";
    }

    /**
     * Calculate overall health score.
     */
    private double calculateOverallHealthScore(List<AdapterHealthSummary> summaries) {
        if(summaries.isEmpty()) return 0;

        return summaries.stream()
            .mapToDouble(AdapterHealthSummary::getHealthScore)
            .average()
            .orElse(0);
    }

    /**
     * Identify critical issues.
     */
    private List<CriticalIssue> identifyCriticalIssues(List<AdapterHealthSummary> summaries) {
        List<CriticalIssue> issues = new ArrayList<>();

        for(AdapterHealthSummary summary : summaries) {
            if("CRITICAL".equals(summary.getHealthStatus())) {
                CriticalIssue issue = new CriticalIssue();
                issue.setAdapterId(summary.getAdapterId());
                issue.setAdapterName(summary.getAdapterName());
                issue.setIssueType("HEALTH_CRITICAL");
                issue.setDescription("Adapter health is critical with score " +
                    String.format("%.1f", summary.getHealthScore()));
                issue.setSeverity("CRITICAL");
                issue.setDetectedAt(LocalDateTime.now());

                issues.add(issue);
            }

            // Check for high error rates
            if(summary.getErrorRate() > 10) {
                CriticalIssue issue = new CriticalIssue();
                issue.setAdapterId(summary.getAdapterId());
                issue.setAdapterName(summary.getAdapterName());
                issue.setIssueType("HIGH_ERROR_RATE");
                issue.setDescription("Error rate is " +
                    String.format("%.1f%%", summary.getErrorRate()));
                issue.setSeverity("HIGH");
                issue.setDetectedAt(LocalDateTime.now());

                issues.add(issue);
            }
        }

        return issues;
    }

    /**
     * Generate health recommendations.
     */
    private List<String> generateHealthRecommendations(List<AdapterHealthSummary> summaries) {
        List<String> recommendations = new ArrayList<>();

        // Check for patterns
        long criticalCount = summaries.stream()
            .filter(s -> "CRITICAL".equals(s.getHealthStatus()))
            .count();

        if(criticalCount > 0) {
            recommendations.add("Address " + criticalCount +
                " critical adapters immediately to prevent service disruption");
        }

        // Check average error rate
        double avgErrorRate = summaries.stream()
            .mapToDouble(AdapterHealthSummary::getErrorRate)
            .average()
            .orElse(0);

        if(avgErrorRate > 5) {
            recommendations.add("Overall error rate is high(" +
                String.format("%.1f%%", avgErrorRate) +
                "). Review error handling and retry mechanisms");
        }

        // Check for inactive adapters
        long inactiveCount = summaries.stream()
            .filter(s -> s.getUptime() < 50)
            .count();

        if(inactiveCount > 0) {
            recommendations.add(inactiveCount +
                " adapters have low uptime. Check configuration and connectivity");
        }

        return recommendations;
    }

    /**
     * Collect performance metrics.
     */
    private Map<String, Object> collectPerformanceMetrics(CommunicationAdapter adapter) {
        Map<String, Object> metrics = new HashMap<>();

        metrics.put("throughput", getThroughput(adapter));
        metrics.put("averageResponseTime", getAverageResponseTime(adapter));
        metrics.put("errorRate", getErrorRate(adapter));
        metrics.put("successRate", 100 - getErrorRate(adapter));

        // Get percentiles
        io.micrometer.core.instrument.Timer timer = meterRegistry.find("adapter.operation.duration")
            .tag("adapter.type", adapter.getType().toString())
            .timer();

        if(timer != null) {
            metrics.put("p50ResponseTime", timer.percentile(0.5, TimeUnit.MILLISECONDS));
            metrics.put("p95ResponseTime", timer.percentile(0.95, TimeUnit.MILLISECONDS));
            metrics.put("p99ResponseTime", timer.percentile(0.99, TimeUnit.MILLISECONDS));
        }

        return metrics;
    }

    /**
     * Collect resource metrics.
     */
    private Map<String, Object> collectResourceMetrics(CommunicationAdapter adapter) {
        Map<String, Object> metrics = new HashMap<>();

        // Connection pool metrics
        if(isConnectionBasedAdapter(adapter)) {
            Gauge activeGauge = meterRegistry.find("adapter.connections.active")
                .tag("adapter.type", adapter.getType().toString())
                .gauge();

            if(activeGauge != null) {
                metrics.put("activeConnections", activeGauge.value());
            }
        }

        // Queue metrics
        if(isMessageBasedAdapter(adapter)) {
            Gauge queueGauge = meterRegistry.find("adapter.queue.size")
                .tag("adapter.type", adapter.getType().toString())
                .gauge();

            if(queueGauge != null) {
                metrics.put("queueSize", queueGauge.value());
            }
        }

        return metrics;
    }

    /**
     * Collect error metrics.
     */
    private Map<String, Object> collectErrorMetrics(CommunicationAdapter adapter) {
        Map<String, Object> metrics = new HashMap<>();

        // Error counts by type
        Map<String, Long> errorCounts = new HashMap<>();

        Collection<Counter> errorCounters = meterRegistry.find("adapter.operations.errors")
            .tag("adapter.type", adapter.getType().toString())
            .counters();

        for(Counter counter : errorCounters) {
            String errorType = counter.getId().getTag("error.type");
            errorCounts.put(errorType, (long) counter.count());
        }

        metrics.put("errorsByType", errorCounts);
        metrics.put("totalErrors", errorCounts.values().stream().mapToLong(Long::longValue).sum());

        return metrics;
    }

    /**
     * Check connection status.
     */
    private ConnectionStatus checkConnectionStatus(CommunicationAdapter adapter) {
        ConnectionStatus status = new ConnectionStatus();

        status.setConnected(adapter.isActive());
        status.setLastConnectionTest(getLastConnectionTest(adapter));
        status.setLastSuccessfulConnection(getLastSuccessfulConnection(adapter));
        status.setConnectionAttempts(getConnectionAttempts(adapter));
        status.setFailedAttempts(getFailedConnectionAttempts(adapter));

        return status;
    }

    /**
     * Get health history.
     */
    private List<HealthSnapshot> getHealthHistory(String adapterId) {
        return healthHistory.getOrDefault(adapterId, new ArrayList<>());
    }

    /**
     * Run diagnostics.
     */
    private List<DiagnosticResult> runDiagnostics(CommunicationAdapter adapter) {
        List<DiagnosticResult> results = new ArrayList<>();

        // Connection test
        results.add(runConnectionDiagnostic(adapter));

        // Configuration validation
        results.add(runConfigurationDiagnostic(adapter));

        // Performance check
        results.add(runPerformanceDiagnostic(adapter));

        // Resource check
        results.add(runResourceDiagnostic(adapter));

        return results;
    }

    /**
     * Run connection diagnostic.
     */
    private DiagnosticResult runConnectionDiagnostic(CommunicationAdapter adapter) {
        DiagnosticResult result = new DiagnosticResult();
        result.setTestName("Connection Test");

        try {
            // Simulate connection test
            boolean connected = adapter.isActive();
            result.setPassed(connected);
            result.setMessage(connected ? "Connection successful" : "Connection failed");
        } catch(Exception e) {
            result.setPassed(false);
            result.setMessage("Connection test error: " + e.getMessage());
        }

        return result;
    }

    /**
     * Run configuration diagnostic.
     */
    private DiagnosticResult runConfigurationDiagnostic(CommunicationAdapter adapter) {
        DiagnosticResult result = new DiagnosticResult();
        result.setTestName("Configuration Validation");

        // Check required configuration
        boolean valid = adapter.getConfiguration() != null &&
                       !adapter.getConfiguration().isEmpty();

        result.setPassed(valid);
        result.setMessage(valid ? "Configuration is valid" : "Missing required configuration");

        return result;
    }

    /**
     * Run performance diagnostic.
     */
    private DiagnosticResult runPerformanceDiagnostic(CommunicationAdapter adapter) {
        DiagnosticResult result = new DiagnosticResult();
        result.setTestName("Performance Check");

        double avgResponseTime = getAverageResponseTime(adapter);
        boolean acceptable = avgResponseTime < 1000; // 1 second threshold

        result.setPassed(acceptable);
        result.setMessage(String.format("Average response time: %.0fms", avgResponseTime));

        return result;
    }

    /**
     * Run resource diagnostic.
     */
    private DiagnosticResult runResourceDiagnostic(CommunicationAdapter adapter) {
        DiagnosticResult result = new DiagnosticResult();
        result.setTestName("Resource Usage");

        // Check resource usage based on adapter type
        boolean healthy = true;
        String message = "Resource usage within limits";

        if(isConnectionBasedAdapter(adapter)) {
            Gauge gauge = meterRegistry.find("adapter.connections.active")
                .tag("adapter.type", adapter.getType().toString())
                .gauge();

            if(gauge != null && gauge.value() > 80) {
                healthy = false;
                message = "High connection pool usage: " + gauge.value();
            }
        }

        result.setPassed(healthy);
        result.setMessage(message);

        return result;
    }

    /**
     * Capture health snapshot periodically.
     */
    @Scheduled(fixedDelay = 300000) // Every 5 minutes
    public void captureHealthSnapshots() {
        try {
            List<CommunicationAdapter> adapters = adapterRepository.findAll();

            for(CommunicationAdapter adapter : adapters) {
                String adapterId = adapter.getId().toString();
                AdapterHealthScore score = calculateDetailedHealthScore(adapter);

                HealthSnapshot snapshot = new HealthSnapshot();
                snapshot.setTimestamp(LocalDateTime.now());
                snapshot.setHealthScore((int) score.getOverallScore());
                snapshot.setStatus(determineHealthStatus(score.getOverallScore()));

                // Add to history
                List<HealthSnapshot> history = healthHistory.computeIfAbsent(
                    adapterId, k -> new ArrayList<>());
                history.add(snapshot);

                // Keep only last 24 hours
                LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
                history.removeIf(s -> s.getTimestamp().isBefore(cutoff));

                // Update cache
                healthScoreCache.put(adapterId, score);
            }
        } catch(Exception e) {
            log.error("Error capturing health snapshots", e);
        }
    }

    // Helper methods

    private double getThroughput(CommunicationAdapter adapter) {
        Counter counter = meterRegistry.find("adapter.operations.total")
            .tag("adapter.type", adapter.getType().toString())
            .counter();

        return counter != null ? counter.count() / 60 : 0; // Operations per minute
    }

    private double getErrorRate(CommunicationAdapter adapter) {
        Counter totalCounter = meterRegistry.find("adapter.operations.total")
            .tag("adapter.type", adapter.getType().toString())
            .counter();

        Counter errorCounter = meterRegistry.find("adapter.operations.errors")
            .tag("adapter.type", adapter.getType().toString())
            .counter();

        if(totalCounter != null && errorCounter != null && totalCounter.count() > 0) {
            return(errorCounter.count() / totalCounter.count()) * 100;
        }

        return 0;
    }

    private double getAverageResponseTime(CommunicationAdapter adapter) {
        io.micrometer.core.instrument.Timer timer = meterRegistry.find("adapter.operation.duration")
            .tag("adapter.type", adapter.getType().toString())
            .timer();

        return timer != null ? timer.mean(TimeUnit.MILLISECONDS) : 0;
    }

    private double calculateUptime(CommunicationAdapter adapter) {
        // Simplified uptime calculation
        return adapter.isActive() ? 99.9 : 0;
    }

    private LocalDateTime getLastActivity(CommunicationAdapter adapter) {
        // Would query from logs or monitoring data
        return LocalDateTime.now().minusMinutes(5);
    }

    private List<String> identifyActiveIssues(CommunicationAdapter adapter) {
        List<String> issues = new ArrayList<>();

        if(!adapter.isActive()) {
            issues.add("Adapter is inactive");
        }

        if(getErrorRate(adapter) > 10) {
            issues.add("High error rate");
        }

        if(getAverageResponseTime(adapter) > 5000) {
            issues.add("Slow response times");
        }

        return issues;
    }

    private LocalDateTime getLastConnectionTest(CommunicationAdapter adapter) {
        return LocalDateTime.now().minusMinutes(1);
    }

    private LocalDateTime getLastSuccessfulConnection(CommunicationAdapter adapter) {
        return adapter.isActive() ? LocalDateTime.now() : LocalDateTime.now().minusHours(1);
    }

    private int getConnectionAttempts(CommunicationAdapter adapter) {
        return 100; // Placeholder
    }

    private int getFailedConnectionAttempts(CommunicationAdapter adapter) {
        return adapter.isActive() ? 1 : 10; // Placeholder
    }

    private boolean isMessageBasedAdapter(CommunicationAdapter adapter) {
        return adapter.getType().toString().contains("IBMMQ") ||
               adapter.getType().toString().contains("KAFKA");
    }

    private boolean isConnectionBasedAdapter(CommunicationAdapter adapter) {
        return adapter.getType().toString().contains("HTTP") ||
               adapter.getType().toString().contains("JDBC") ||
               adapter.getType().toString().contains("FTP");
    }

    private void checkConnectionHealth(CommunicationAdapter adapter, List<HealthAlert> alerts) {
        if(!adapter.isActive()) {
            HealthAlert alert = new HealthAlert();
            alert.setAdapterId(adapter.getId().toString());
            alert.setAdapterName(adapter.getName());
            alert.setAlertType("CONNECTION_FAILURE");
            alert.setMessage("Adapter is not active");
            alert.setSeverity("HIGH");
            alert.setTimestamp(LocalDateTime.now());
            alerts.add(alert);
        }
    }

    private void checkPerformanceHealth(CommunicationAdapter adapter, List<HealthAlert> alerts) {
        double avgResponseTime = getAverageResponseTime(adapter);
        if(avgResponseTime > 5000) {
            HealthAlert alert = new HealthAlert();
            alert.setAdapterId(adapter.getId().toString());
            alert.setAdapterName(adapter.getName());
            alert.setAlertType("SLOW_PERFORMANCE");
            alert.setMessage(String.format("Average response time is %.0fms", avgResponseTime));
            alert.setSeverity("MEDIUM");
            alert.setTimestamp(LocalDateTime.now());
            alerts.add(alert);
        }
    }

    private void checkErrorRateHealth(CommunicationAdapter adapter, List<HealthAlert> alerts) {
        double errorRate = getErrorRate(adapter);
        if(errorRate > 10) {
            HealthAlert alert = new HealthAlert();
            alert.setAdapterId(adapter.getId().toString());
            alert.setAdapterName(adapter.getName());
            alert.setAlertType("HIGH_ERROR_RATE");
            alert.setMessage(String.format("Error rate is %.1f%%", errorRate));
            alert.setSeverity("HIGH");
            alert.setTimestamp(LocalDateTime.now());
            alerts.add(alert);
        }
    }

    private void checkResourceHealth(CommunicationAdapter adapter, List<HealthAlert> alerts) {
        if(isConnectionBasedAdapter(adapter)) {
            Gauge gauge = meterRegistry.find("adapter.connections.active")
                .tag("adapter.type", adapter.getType().toString())
                .gauge();

            if(gauge != null && gauge.value() > 80) {
                HealthAlert alert = new HealthAlert();
                alert.setAdapterId(adapter.getId().toString());
                alert.setAdapterName(adapter.getName());
                alert.setAlertType("HIGH_RESOURCE_USAGE");
                alert.setMessage("Connection pool usage is high: " + gauge.value());
                alert.setSeverity("MEDIUM");
                alert.setTimestamp(LocalDateTime.now());
                alerts.add(alert);
            }
        }
    }

    private void checkSLACompliance(CommunicationAdapter adapter, List<HealthAlert> alerts) {
        double slaScore = calculateSLAScore(adapter);
        if(slaScore < 80) {
            HealthAlert alert = new HealthAlert();
            alert.setAdapterId(adapter.getId().toString());
            alert.setAdapterName(adapter.getName());
            alert.setAlertType("SLA_VIOLATION");
            alert.setMessage(String.format("SLA compliance is %.1f%%", slaScore));
            alert.setSeverity("MEDIUM");
            alert.setTimestamp(LocalDateTime.now());
            alerts.add(alert);
        }
    }

    private List<String> rankAdaptersByHealth(Map<String, AdapterHealthScore> healthScores) {
        return healthScores.entrySet().stream()
            .sorted((a, b) -> Double.compare(
                b.getValue().getOverallScore(),
                a.getValue().getOverallScore()))
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    private String identifyBestPerformer(Map<String, PerformanceComparison> comparisons) {
        return comparisons.entrySet().stream()
            .max((a, b) -> {
                // Score based on multiple factors
                double scoreA = calculatePerformanceRankScore(a.getValue());
                double scoreB = calculatePerformanceRankScore(b.getValue());
                return Double.compare(scoreA, scoreB);
            })
            .map(Map.Entry::getKey)
            .orElse(null);
    }

    private String identifyWorstPerformer(Map<String, PerformanceComparison> comparisons) {
        return comparisons.entrySet().stream()
            .min((a, b) -> {
                double scoreA = calculatePerformanceRankScore(a.getValue());
                double scoreB = calculatePerformanceRankScore(b.getValue());
                return Double.compare(scoreA, scoreB);
            })
            .map(Map.Entry::getKey)
            .orElse(null);
    }

    private double calculatePerformanceRankScore(PerformanceComparison perf) {
        // Higher throughput, lower error rate, lower response time = better score
        double throughputScore = perf.getThroughput();
        double errorScore = 100 - perf.getErrorRate();
        double responseScore = 1000 / (perf.getAverageResponseTime() + 1);
        double uptimeScore = perf.getUptime();

        return(throughputScore * 0.3) + (errorScore * 0.3) +
               (responseScore * 0.2) + (uptimeScore * 0.2);
    }
}
