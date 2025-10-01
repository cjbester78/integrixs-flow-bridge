package com.integrixs.backend.service;

import com.integrixs.backend.dto.dashboard.health.*;
import com.integrixs.data.model.AdapterStatus;
import com.integrixs.data.model.SystemLog;
import com.integrixs.data.sql.repository.AdapterStatusSqlRepository;
import com.integrixs.data.sql.repository.SystemLogSqlRepository;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for monitoring and analyzing adapter health.
 */
@Service
public class AdapterHealthService {

    private static final Logger log = LoggerFactory.getLogger(AdapterHealthService.class);


    private final AdapterStatusSqlRepository adapterStatusRepository;
    private final SystemLogSqlRepository systemLogRepository;
    private final MeterRegistry meterRegistry;

    // Real - time health tracking
    private final Map<String, AdapterHealthMetrics> currentHealthMetrics = new ConcurrentHashMap<>();
    private final Map<String, ConnectionPoolMetrics> connectionPoolMetrics = new ConcurrentHashMap<>();
    private final Map<String, ResourceUsageMetrics> resourceUsageMetrics = new ConcurrentHashMap<>();

    public AdapterHealthService(AdapterStatusSqlRepository adapterStatusRepository,
                                SystemLogSqlRepository systemLogRepository,
                                MeterRegistry meterRegistry) {
        this.adapterStatusRepository = adapterStatusRepository;
        this.systemLogRepository = systemLogRepository;
        this.meterRegistry = meterRegistry;
    }

    /**
     * Get comprehensive health dashboard for all adapters.
     */
    public AdapterHealthDashboard getHealthDashboard() {
        AdapterHealthDashboard dashboard = new AdapterHealthDashboard();
        dashboard.setTimestamp(LocalDateTime.now());

        // Get all adapter statuses
        List<AdapterStatus> statuses = adapterStatusRepository.findAll();
        dashboard.setTotalAdapters(statuses.size());

        // Count by status
        Map<String, Long> statusCounts = statuses.stream()
            .collect(Collectors.groupingBy(AdapterStatus::getStatus, Collectors.counting()));

        dashboard.setHealthyAdapters(statusCounts.getOrDefault("ACTIVE", 0L).intValue());
        dashboard.setUnhealthyAdapters(statusCounts.getOrDefault("ERROR", 0L).intValue());
        dashboard.setWarningAdapters(statusCounts.getOrDefault("WARNING", 0L).intValue());
        dashboard.setInactiveAdapters(statusCounts.getOrDefault("INACTIVE", 0L).intValue());

        // Calculate overall health score(0-100)
        int healthScore = calculateOverallHealthScore(statuses);
        dashboard.setOverallHealthScore(healthScore);

        // Get individual adapter health details
        List<AdapterHealthDetail> healthDetails = statuses.stream()
            .map(this::createHealthDetail)
            .sorted(Comparator.comparingInt(AdapterHealthDetail::getHealthScore))
            .collect(Collectors.toList());
        dashboard.setAdapterHealthDetails(healthDetails);

        // Get health trends
        dashboard.setHealthTrends(calculateHealthTrends());

        // Get critical alerts
        dashboard.setCriticalAlerts(getCriticalAlerts());

        return dashboard;
    }

    /**
     * Get detailed health information for a specific adapter.
     */
    public AdapterHealthDetail getAdapterHealth(String adapterId) {
        UUID adapterUuid = UUID.fromString(adapterId);
        AdapterStatus status = adapterStatusRepository.findById(adapterUuid)
            .orElseThrow(() -> new RuntimeException("Adapter not found: " + adapterId));

        return createDetailedHealthInfo(status);
    }

    /**
     * Get connection pool metrics for an adapter.
     */
    public ConnectionPoolMetrics getConnectionPoolMetrics(String adapterId) {
        return connectionPoolMetrics.computeIfAbsent(adapterId, k -> {
            ConnectionPoolMetrics metrics = new ConnectionPoolMetrics();
            metrics.setAdapterId(adapterId);
            metrics.setMaxConnections(10); // Default
            metrics.setActiveConnections(0);
            metrics.setIdleConnections(0);
            metrics.setWaitingThreads(0);
            metrics.setConnectionUtilization(0.0);
            return metrics;
        });
    }

    /**
     * Update connection pool metrics.
     */
    public void updateConnectionPoolMetrics(String adapterId, int active, int idle, int waiting) {
        ConnectionPoolMetrics metrics = getConnectionPoolMetrics(adapterId);
        metrics.setActiveConnections(active);
        metrics.setIdleConnections(idle);
        metrics.setWaitingThreads(waiting);
        metrics.setConnectionUtilization(
            (double) active / metrics.getMaxConnections() * 100
       );
        metrics.setLastUpdated(LocalDateTime.now());

        // Record in Micrometer
        meterRegistry.gauge("adapter.connection.pool.active",
            Collections.singletonList(io.micrometer.core.instrument.Tag.of("adapter", adapterId)),
            active);
        meterRegistry.gauge("adapter.connection.pool.idle",
            Collections.singletonList(io.micrometer.core.instrument.Tag.of("adapter", adapterId)),
            idle);
    }

    /**
     * Get resource usage metrics for an adapter.
     */
    public ResourceUsageMetrics getResourceUsageMetrics(String adapterId) {
        return resourceUsageMetrics.computeIfAbsent(adapterId, k -> {
            ResourceUsageMetrics metrics = new ResourceUsageMetrics();
            metrics.setAdapterId(adapterId);
            metrics.setCpuUsage(0.0);
            metrics.setMemoryUsageMB(0);
            metrics.setThreadCount(0);
            metrics.setFileHandles(0);
            metrics.setNetworkBandwidthKBps(0.0);
            return metrics;
        });
    }

    /**
     * Update resource usage metrics.
     */
    public void updateResourceUsageMetrics(String adapterId, double cpuUsage,
                                         long memoryMB, int threads, int fileHandles,
                                         double bandwidthKBps) {
        ResourceUsageMetrics metrics = getResourceUsageMetrics(adapterId);
        metrics.setCpuUsage(cpuUsage);
        metrics.setMemoryUsageMB(memoryMB);
        metrics.setThreadCount(threads);
        metrics.setFileHandles(fileHandles);
        metrics.setNetworkBandwidthKBps(bandwidthKBps);
        metrics.setLastUpdated(LocalDateTime.now());

        // Record in Micrometer
        meterRegistry.gauge("adapter.resource.cpu",
            Collections.singletonList(io.micrometer.core.instrument.Tag.of("adapter", adapterId)),
            cpuUsage);
        meterRegistry.gauge("adapter.resource.memory",
            Collections.singletonList(io.micrometer.core.instrument.Tag.of("adapter", adapterId)),
            memoryMB);
    }

    /**
     * Perform health check on an adapter.
     */
    public HealthCheckResult performHealthCheck(String adapterId) {
        HealthCheckResult result = new HealthCheckResult();
        result.setAdapterId(adapterId);
        result.setCheckTime(LocalDateTime.now());

        List<HealthCheckItem> checkItems = new ArrayList<>();

        // Check connection
        HealthCheckItem connectionCheck = checkConnection(adapterId);
        checkItems.add(connectionCheck);

        // Check performance
        HealthCheckItem performanceCheck = checkPerformance(adapterId);
        checkItems.add(performanceCheck);

        // Check error rate
        HealthCheckItem errorRateCheck = checkErrorRate(adapterId);
        checkItems.add(errorRateCheck);

        // Check resource usage
        HealthCheckItem resourceCheck = checkResourceUsage(adapterId);
        checkItems.add(resourceCheck);

        // Check response time
        HealthCheckItem responseTimeCheck = checkResponseTime(adapterId);
        checkItems.add(responseTimeCheck);

        result.setCheckItems(checkItems);

        // Calculate overall status
        boolean hasErrors = checkItems.stream().anyMatch(item -> "ERROR".equals(item.getStatus()));
        boolean hasWarnings = checkItems.stream().anyMatch(item -> "WARNING".equals(item.getStatus()));

        if(hasErrors) {
            result.setOverallStatus("UNHEALTHY");
            result.setHealthScore(calculateHealthScoreFromChecks(checkItems));
        } else if(hasWarnings) {
            result.setOverallStatus("WARNING");
            result.setHealthScore(calculateHealthScoreFromChecks(checkItems));
        } else {
            result.setOverallStatus("HEALTHY");
            result.setHealthScore(100);
        }

        // Generate recommendations
        result.setRecommendations(generateHealthRecommendations(checkItems));

        // Update adapter status
        updateAdapterHealthStatus(adapterId, result);

        return result;
    }

    /**
     * Get health history for an adapter.
     */
    public AdapterHealthHistory getHealthHistory(String adapterId, int days) {
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);

        AdapterHealthHistory history = new AdapterHealthHistory();
        history.setAdapterId(adapterId);
        history.setStartTime(startTime);
        history.setEndTime(LocalDateTime.now());

        // Get historical health scores
        List<HealthScorePoint> healthScores = calculateHistoricalHealthScores(adapterId, startTime);
        history.setHealthScores(healthScores);

        // Get status changes
        List<StatusChangeEvent> statusChanges = getStatusChangeEvents(adapterId, startTime);
        history.setStatusChanges(statusChanges);

        // Get error events
        List<ErrorEvent> errorEvents = getErrorEvents(adapterId, startTime);
        history.setErrorEvents(errorEvents);

        // Calculate statistics
        HealthHistoryStatistics stats = calculateHealthStatistics(healthScores, statusChanges, errorEvents);
        history.setStatistics(stats);

        return history;
    }

    /**
     * Get adapter recovery suggestions.
     */
    public List<RecoverySuggestion> getRecoverySuggestions(String adapterId) {
        List<RecoverySuggestion> suggestions = new ArrayList<>();

        // Analyze recent errors
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        List<SystemLog> recentErrors = systemLogRepository.findBySourceAndLevelAndTimestampAfter(
            adapterId, SystemLog.LogLevel.ERROR, oneHourAgo);

        Map<String, Long> errorCounts = recentErrors.stream()
            .collect(Collectors.groupingBy(SystemLog::getMessage, Collectors.counting()));

        // Generate suggestions based on error patterns
        for(Map.Entry<String, Long> entry : errorCounts.entrySet()) {
            RecoverySuggestion suggestion = generateRecoverySuggestion(entry.getKey(), entry.getValue());
            if(suggestion != null) {
                suggestions.add(suggestion);
            }
        }

        // Add general suggestions based on health metrics
        AdapterHealthMetrics metrics = currentHealthMetrics.get(adapterId);
        if(metrics != null) {
            if(metrics.getErrorRate() > 10) {
                suggestions.add(createSuggestion(
                    "High Error Rate",
                    "Consider implementing circuit breaker pattern",
                    "HIGH",
                    Arrays.asList("Add retry logic", "Implement exponential backoff", "Set up circuit breaker")
               ));
            }

            if(metrics.getAverageResponseTime() > 5000) {
                suggestions.add(createSuggestion(
                    "Slow Response Time",
                    "Optimize adapter performance",
                    "MEDIUM",
                    Arrays.asList("Check connection pooling", "Optimize queries", "Consider caching")
               ));
            }
        }

        return suggestions;
    }

    // Helper methods

    private AdapterHealthDetail createHealthDetail(AdapterStatus status) {
        AdapterHealthDetail detail = new AdapterHealthDetail();
        detail.setAdapterId(status.getId().toString());
        detail.setAdapterName(status.getAdapter().getName());
        detail.setAdapterType(status.getAdapter().getType().toString());
        detail.setStatus(status.getStatus());
        detail.setLastChecked(status.getLastHealthCheck());

        // Calculate health score
        int healthScore = calculateAdapterHealthScore(status);
        detail.setHealthScore(healthScore);

        // Get current metrics
        AdapterHealthMetrics metrics = getOrCalculateHealthMetrics(status.getId().toString());
        detail.setCurrentMetrics(metrics);

        // Get connection pool info
        detail.setConnectionPoolMetrics(getConnectionPoolMetrics(status.getId().toString()));

        // Get resource usage
        detail.setResourceUsageMetrics(getResourceUsageMetrics(status.getId().toString()));

        return detail;
    }

    private AdapterHealthDetail createDetailedHealthInfo(AdapterStatus status) {
        AdapterHealthDetail detail = createHealthDetail(status);

        // Add recent errors
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        List<SystemLog> recentErrors = systemLogRepository.findBySourceAndLevelAndTimestampAfter(
            status.getId().toString(), SystemLog.LogLevel.ERROR, oneHourAgo);

        List<RecentError> errors = recentErrors.stream()
            .map(log -> {
                RecentError error = new RecentError();
                error.setTimestamp(log.getTimestamp());
                error.setMessage(log.getMessage());
                error.setCount(1);
                return error;
            })
            .collect(Collectors.toList());

        detail.setRecentErrors(errors);

        // Add performance trends
        detail.setPerformanceTrends(calculatePerformanceTrends(status.getId().toString()));

        return detail;
    }

    private AdapterHealthMetrics getOrCalculateHealthMetrics(String adapterId) {
        return currentHealthMetrics.computeIfAbsent(adapterId, k -> {
            AdapterHealthMetrics metrics = new AdapterHealthMetrics();
            metrics.setAdapterId(adapterId);

            // Calculate from recent logs
            LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
            List<SystemLog> recentLogs = systemLogRepository.findAll().stream()
                .filter(log -> adapterId.equals(log.getComponentId()) && log.getTimestamp().isAfter(oneHourAgo))
                .collect(Collectors.toList());

            long totalRequests = recentLogs.size();
            long errors = recentLogs.stream().filter(log -> SystemLog.LogLevel.ERROR.equals(log.getLevel())).count();

            metrics.setRequestsPerMinute(totalRequests / 60.0);
            metrics.setErrorRate(totalRequests > 0 ? (double) errors / totalRequests * 100 : 0);
            metrics.setSuccessRate(100 - metrics.getErrorRate());
            metrics.setAverageResponseTime(calculateAverageResponseTime(recentLogs));
            metrics.setUptime(calculateUptime(adapterId));

            return metrics;
        });
    }

    private int calculateAdapterHealthScore(AdapterStatus status) {
        int score = 100;

        // Status - based scoring
        switch(status.getStatus()) {
            case "ERROR":
                score -= 50;
                break;
            case "WARNING":
                score -= 20;
                break;
            case "INACTIVE":
                score -= 30;
                break;
        }

        // Recent errors impact
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        long errorCount = systemLogRepository.countByComponentIdAndLevelAndTimestampAfter(
            status.getId().toString(), SystemLog.LogLevel.ERROR, oneHourAgo);

        if(errorCount > 10) score -= 20;
        else if(errorCount > 5) score -= 10;
        else if(errorCount > 0) score -= 5;

        // Last check time impact
        if(status.getLastHealthCheck() != null) {
            long minutesSinceCheck = ChronoUnit.MINUTES.between(status.getLastHealthCheck(), LocalDateTime.now());
            if(minutesSinceCheck > 60) score -= 10;
            else if(minutesSinceCheck > 30) score -= 5;
        }

        return Math.max(0, score);
    }

    private int calculateOverallHealthScore(List<AdapterStatus> statuses) {
        if(statuses.isEmpty()) return 100;

        double totalScore = statuses.stream()
            .mapToInt(this::calculateAdapterHealthScore)
            .average()
            .orElse(100);

        return(int) Math.round(totalScore);
    }

    private HealthTrends calculateHealthTrends() {
        HealthTrends trends = new HealthTrends();

        // Calculate 24 - hour trends
        LocalDateTime dayAgo = LocalDateTime.now().minusDays(1);
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);

        // Error rate trend
        double currentErrorRate = calculateCurrentErrorRate();
        double dayAgoErrorRate = calculateErrorRateAt(dayAgo);
        trends.setErrorRateTrend(currentErrorRate - dayAgoErrorRate);

        // Response time trend
        double currentResponseTime = calculateCurrentAverageResponseTime();
        double dayAgoResponseTime = calculateResponseTimeAt(dayAgo);
        trends.setResponseTimeTrend(currentResponseTime - dayAgoResponseTime);

        // Availability trend
        double currentAvailability = calculateCurrentAvailability();
        double weekAgoAvailability = calculateAvailabilityAt(weekAgo);
        trends.setAvailabilityTrend(currentAvailability - weekAgoAvailability);

        return trends;
    }

    private List<CriticalAlert> getCriticalAlerts() {
        List<CriticalAlert> alerts = new ArrayList<>();

        // Check each adapter for critical conditions
        List<AdapterStatus> statuses = adapterStatusRepository.findAll();

        for(AdapterStatus status : statuses) {
            // Check for error status
            if("ERROR".equals(status.getStatus())) {
                CriticalAlert alert = new CriticalAlert();
                alert.setAdapterId(status.getId().toString());
                alert.setAdapterName(status.getAdapter().getName());
                alert.setSeverity("CRITICAL");
                alert.setMessage("Adapter is in ERROR state");
                alert.setTimestamp(status.getLastHealthCheck());
                alert.setActionRequired("Investigate and restart adapter");
                alerts.add(alert);
            }

            // Check for high error rate
            AdapterHealthMetrics metrics = currentHealthMetrics.get(status.getId().toString());
            if(metrics != null && metrics.getErrorRate() > 20) {
                CriticalAlert alert = new CriticalAlert();
                alert.setAdapterId(status.getId().toString());
                alert.setAdapterName(status.getAdapter().getName());
                alert.setSeverity("HIGH");
                alert.setMessage(String.format("High error rate: %.1f%%", metrics.getErrorRate()));
                alert.setTimestamp(LocalDateTime.now());
                alert.setActionRequired("Review error logs and configuration");
                alerts.add(alert);
            }

            // Check for connection pool exhaustion
            ConnectionPoolMetrics poolMetrics = connectionPoolMetrics.get(status.getId().toString());
            if(poolMetrics != null && poolMetrics.getConnectionUtilization() > 90) {
                CriticalAlert alert = new CriticalAlert();
                alert.setAdapterId(status.getId().toString());
                alert.setAdapterName(status.getAdapter().getName());
                alert.setSeverity("MEDIUM");
                alert.setMessage("Connection pool near exhaustion");
                alert.setTimestamp(LocalDateTime.now());
                alert.setActionRequired("Increase connection pool size or optimize usage");
                alerts.add(alert);
            }
        }

        return alerts;
    }

    private HealthCheckItem checkConnection(String adapterId) {
        HealthCheckItem item = new HealthCheckItem();
        item.setCheckName("Connection Check");

        AdapterStatus status = adapterStatusRepository.findById(UUID.fromString(adapterId)).orElse(null);
        if(status == null || !"ACTIVE".equals(status.getStatus())) {
            item.setStatus("ERROR");
            item.setMessage("Adapter is not active");
            item.setDetails("Current status: " + (status != null ? status.getStatus() : "NOT_FOUND"));
        } else {
            item.setStatus("OK");
            item.setMessage("Connection is healthy");
            item.setDetails("Last successful check: " + status.getLastHealthCheck());
        }

        return item;
    }

    private HealthCheckItem checkPerformance(String adapterId) {
        HealthCheckItem item = new HealthCheckItem();
        item.setCheckName("Performance Check");

        AdapterHealthMetrics metrics = currentHealthMetrics.get(adapterId);
        if(metrics == null) {
            item.setStatus("WARNING");
            item.setMessage("No performance metrics available");
            return item;
        }

        if(metrics.getAverageResponseTime() > 5000) {
            item.setStatus("WARNING");
            item.setMessage("Slow response time detected");
            item.setDetails(String.format("Average response time: %.0fms", metrics.getAverageResponseTime()));
        } else {
            item.setStatus("OK");
            item.setMessage("Performance is within acceptable range");
            item.setDetails(String.format("Average response time: %.0fms", metrics.getAverageResponseTime()));
        }

        return item;
    }

    private HealthCheckItem checkErrorRate(String adapterId) {
        HealthCheckItem item = new HealthCheckItem();
        item.setCheckName("Error Rate Check");

        AdapterHealthMetrics metrics = currentHealthMetrics.get(adapterId);
        if(metrics == null) {
            item.setStatus("WARNING");
            item.setMessage("No error rate metrics available");
            return item;
        }

        if(metrics.getErrorRate() > 10) {
            item.setStatus("ERROR");
            item.setMessage("High error rate detected");
            item.setDetails(String.format("Current error rate: %.1f%%", metrics.getErrorRate()));
        } else if(metrics.getErrorRate() > 5) {
            item.setStatus("WARNING");
            item.setMessage("Elevated error rate");
            item.setDetails(String.format("Current error rate: %.1f%%", metrics.getErrorRate()));
        } else {
            item.setStatus("OK");
            item.setMessage("Error rate is acceptable");
            item.setDetails(String.format("Current error rate: %.1f%%", metrics.getErrorRate()));
        }

        return item;
    }

    private HealthCheckItem checkResourceUsage(String adapterId) {
        HealthCheckItem item = new HealthCheckItem();
        item.setCheckName("Resource Usage Check");

        ResourceUsageMetrics resources = resourceUsageMetrics.get(adapterId);
        if(resources == null) {
            item.setStatus("WARNING");
            item.setMessage("No resource usage metrics available");
            return item;
        }

        if(resources.getCpuUsage() > 80 || resources.getMemoryUsageMB() > 1024) {
            item.setStatus("WARNING");
            item.setMessage("High resource usage detected");
            item.setDetails(String.format("CPU: %.1f%%, Memory: %dMB",
                resources.getCpuUsage(), resources.getMemoryUsageMB()));
        } else {
            item.setStatus("OK");
            item.setMessage("Resource usage is normal");
            item.setDetails(String.format("CPU: %.1f%%, Memory: %dMB",
                resources.getCpuUsage(), resources.getMemoryUsageMB()));
        }

        return item;
    }

    private HealthCheckItem checkResponseTime(String adapterId) {
        HealthCheckItem item = new HealthCheckItem();
        item.setCheckName("Response Time Check");

        // Get recent response times
        LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(10);
        List<SystemLog> recentLogs = systemLogRepository.findAll().stream()
            .filter(log -> adapterId.equals(log.getComponentId()) && log.getTimestamp().isAfter(tenMinutesAgo))
            .collect(Collectors.toList());

        if(recentLogs.isEmpty()) {
            item.setStatus("WARNING");
            item.setMessage("No recent activity to measure");
            return item;
        }

        // Calculate 95th percentile response time
        List<Long> responseTimes = extractResponseTimes(recentLogs);
        if(responseTimes.isEmpty()) {
            item.setStatus("WARNING");
            item.setMessage("Unable to extract response times");
            return item;
        }

        Collections.sort(responseTimes);
        int index95 = (int) (responseTimes.size() * 0.95);
        long p95ResponseTime = responseTimes.get(Math.min(index95, responseTimes.size() - 1));

        if(p95ResponseTime > 10000) {
            item.setStatus("ERROR");
            item.setMessage("Very high response times detected");
            item.setDetails(String.format("95th percentile: %dms", p95ResponseTime));
        } else if(p95ResponseTime > 5000) {
            item.setStatus("WARNING");
            item.setMessage("Elevated response times");
            item.setDetails(String.format("95th percentile: %dms", p95ResponseTime));
        } else {
            item.setStatus("OK");
            item.setMessage("Response times are good");
            item.setDetails(String.format("95th percentile: %dms", p95ResponseTime));
        }

        return item;
    }

    private int calculateHealthScoreFromChecks(List<HealthCheckItem> checkItems) {
        int totalScore = 100;
        int deduction = 0;

        for(HealthCheckItem item : checkItems) {
            switch(item.getStatus()) {
                case "ERROR":
                    deduction += 20;
                    break;
                case "WARNING":
                    deduction += 10;
                    break;
            }
        }

        return Math.max(0, totalScore - deduction);
    }

    private List<String> generateHealthRecommendations(List<HealthCheckItem> checkItems) {
        List<String> recommendations = new ArrayList<>();

        for(HealthCheckItem item : checkItems) {
            if("ERROR".equals(item.getStatus())) {
                switch(item.getCheckName()) {
                    case "Connection Check":
                        recommendations.add("Restart the adapter and check connection settings");
                        break;
                    case "Error Rate Check":
                        recommendations.add("Review error logs and fix underlying issues");
                        recommendations.add("Consider implementing retry logic");
                        break;
                    case "Response Time Check":
                        recommendations.add("Optimize adapter configuration for better performance");
                        recommendations.add("Check external system performance");
                        break;
                }
            } else if("WARNING".equals(item.getStatus())) {
                switch(item.getCheckName()) {
                    case "Performance Check":
                        recommendations.add("Monitor performance trends closely");
                        break;
                    case "Resource Usage Check":
                        recommendations.add("Consider increasing resource allocation");
                        break;
                }
            }
        }

        return recommendations;
    }

    private void updateAdapterHealthStatus(String adapterId, HealthCheckResult result) {
        AdapterStatus status = adapterStatusRepository.findById(UUID.fromString(adapterId)).orElse(null);
        if(status != null) {
            // Update status based on health check
            if("UNHEALTHY".equals(result.getOverallStatus())) {
                status.setStatus("ERROR");
            } else if("WARNING".equals(result.getOverallStatus())) {
                status.setStatus("WARNING");
            } else {
                status.setStatus("ACTIVE");
            }

            status.setLastHealthCheck(LocalDateTime.now());
            adapterStatusRepository.save(status);
        }
    }

    private List<HealthScorePoint> calculateHistoricalHealthScores(String adapterId, LocalDateTime startTime) {
        List<HealthScorePoint> scores = new ArrayList<>();

        // Calculate hourly health scores
        LocalDateTime current = startTime;
        while(current.isBefore(LocalDateTime.now())) {
            HealthScorePoint point = new HealthScorePoint();
            point.setTimestamp(current);
            point.setScore(calculateHealthScoreAt(adapterId, current));
            scores.add(point);

            current = current.plusHours(1);
        }

        return scores;
    }

    private int calculateHealthScoreAt(String adapterId, LocalDateTime time) {
        // Simplified calculation based on error count in the hour
        LocalDateTime hourStart = time.truncatedTo(ChronoUnit.HOURS);
        LocalDateTime hourEnd = hourStart.plusHours(1);

        long errorCount = systemLogRepository.findAll().stream()
            .filter(log -> adapterId.equals(log.getComponentId()) &&
                    SystemLog.LogLevel.ERROR.equals(log.getLevel()) &&
                    log.getTimestamp().isAfter(hourStart) &&
                    log.getTimestamp().isBefore(hourEnd))
            .count();

        int score = 100;
        if(errorCount > 10) score -= 30;
        else if(errorCount > 5) score -= 20;
        else if(errorCount > 0) score -= 10;

        return Math.max(0, score);
    }

    private List<StatusChangeEvent> getStatusChangeEvents(String adapterId, LocalDateTime startTime) {
        // In a real implementation, this would query a status change history table
        List<StatusChangeEvent> events = new ArrayList<>();

        // For now, return empty list
        return events;
    }

    private List<ErrorEvent> getErrorEvents(String adapterId, LocalDateTime startTime) {
        List<SystemLog> errorLogs = systemLogRepository.findBySourceAndLevelAndTimestampAfter(
            adapterId, SystemLog.LogLevel.ERROR, startTime);

        return errorLogs.stream()
            .map(log -> {
                ErrorEvent event = new ErrorEvent();
                event.setTimestamp(log.getTimestamp());
                event.setErrorType(extractErrorType(log.getMessage()));
                event.setMessage(log.getMessage());
                event.setImpact("MEDIUM"); // Could be calculated based on error type
                return event;
            })
            .collect(Collectors.toList());
    }

    private String extractErrorType(String message) {
        if(message.contains("connection")) return "CONNECTION_ERROR";
        if(message.contains("timeout")) return "TIMEOUT_ERROR";
        if(message.contains("authentication")) return "AUTH_ERROR";
        if(message.contains("validation")) return "VALIDATION_ERROR";
        return "GENERAL_ERROR";
    }

    private HealthHistoryStatistics calculateHealthStatistics(List<HealthScorePoint> scores,
                                                            List<StatusChangeEvent> statusChanges,
                                                            List<ErrorEvent> errors) {
        HealthHistoryStatistics stats = new HealthHistoryStatistics();

        if(!scores.isEmpty()) {
            stats.setAverageHealthScore(
                scores.stream().mapToInt(HealthScorePoint::getScore).average().orElse(0)
           );
            stats.setMinHealthScore(
                scores.stream().mapToInt(HealthScorePoint::getScore).min().orElse(0)
           );
            stats.setMaxHealthScore(
                scores.stream().mapToInt(HealthScorePoint::getScore).max().orElse(100)
           );
        }

        stats.setTotalErrors(errors.size());
        stats.setTotalStatusChanges(statusChanges.size());

        // Calculate uptime percentage
        long totalHours = scores.size();
        long healthyHours = scores.stream().filter(s -> s.getScore() >= 70).count();
        stats.setUptimePercentage(totalHours > 0 ? (double) healthyHours / totalHours * 100 : 100);

        return stats;
    }

    private RecoverySuggestion generateRecoverySuggestion(String errorMessage, long count) {
        if(errorMessage.contains("connection refused")) {
            return createSuggestion(
                "Connection Issues",
                "Target system is refusing connections",
                "HIGH",
                Arrays.asList(
                    "Verify target system is running",
                    "Check firewall settings",
                    "Validate connection parameters"
               )
           );
        } else if(errorMessage.contains("timeout")) {
            return createSuggestion(
                "Timeout Issues",
                "Operations are timing out frequently",
                "MEDIUM",
                Arrays.asList(
                    "Increase timeout values",
                    "Check network latency",
                    "Optimize query/operation performance"
               )
           );
        }

        return null;
    }

    private RecoverySuggestion createSuggestion(String issue, String description,
                                              String priority, List<String> steps) {
        RecoverySuggestion suggestion = new RecoverySuggestion();
        suggestion.setIssue(issue);
        suggestion.setDescription(description);
        suggestion.setPriority(priority);
        suggestion.setSteps(steps);
        suggestion.setEstimatedImpact("Resolving this issue should improve adapter stability");
        return suggestion;
    }

    private double calculateAverageResponseTime(List<SystemLog> logs) {
        List<Long> responseTimes = extractResponseTimes(logs);
        return responseTimes.stream().mapToLong(Long::longValue).average().orElse(0);
    }

    private List<Long> extractResponseTimes(List<SystemLog> logs) {
        // Extract response times from log messages
        // This is a simplified implementation
        return logs.stream()
            .filter(log -> log.getMessage() != null && log.getMessage().contains("completed in"))
            .map(log -> {
                try {
                    String message = log.getMessage();
                    int startIndex = message.indexOf("completed in ") + 13;
                    int endIndex = message.indexOf("ms", startIndex);
                    if(startIndex > 12 && endIndex > startIndex) {
                        return Long.parseLong(message.substring(startIndex, endIndex).trim());
                    }
                } catch(Exception e) {
                    // Ignore parsing errors
                }
                return null;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private double calculateUptime(String adapterId) {
        // Calculate uptime percentage over last 24 hours
        LocalDateTime dayAgo = LocalDateTime.now().minusDays(1);

        // Count total possible checks(assuming checks every 5 minutes)
        long totalChecks = 24 * 12; // 288 checks in 24 hours

        // Count successful checks
        long successfulChecks = systemLogRepository.findAll().stream()
            .filter(log -> adapterId.equals(log.getComponentId()) &&
                    !SystemLog.LogLevel.ERROR.equals(log.getLevel()) &&
                    log.getTimestamp().isAfter(dayAgo))
            .count();

        return Math.min(100, (double) successfulChecks / totalChecks * 100);
    }

    private PerformanceTrend calculatePerformanceTrends(String adapterId) {
        PerformanceTrend trend = new PerformanceTrend();

        // Calculate trends over different time periods
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime hourAgo = now.minusHours(1);
        LocalDateTime dayAgo = now.minusDays(1);
        LocalDateTime weekAgo = now.minusDays(7);

        // Response time trends
        double currentAvgResponse = calculateAverageResponseTimeInPeriod(adapterId, hourAgo, now);
        double dayAvgResponse = calculateAverageResponseTimeInPeriod(adapterId, dayAgo, hourAgo);
        double weekAvgResponse = calculateAverageResponseTimeInPeriod(adapterId, weekAgo, dayAgo);

        trend.setHourlyTrend(currentAvgResponse - dayAvgResponse);
        trend.setDailyTrend(dayAvgResponse - weekAvgResponse);
        trend.setWeeklyTrend(calculateWeeklyTrend(adapterId));

        return trend;
    }

    private double calculateAverageResponseTimeInPeriod(String adapterId, LocalDateTime start, LocalDateTime end) {
        List<SystemLog> logs = systemLogRepository.findAll().stream()
            .filter(log -> adapterId.equals(log.getComponentId()) &&
                    log.getTimestamp().isAfter(start) &&
                    log.getTimestamp().isBefore(end))
            .collect(Collectors.toList());
        return calculateAverageResponseTime(logs);
    }

    private double calculateWeeklyTrend(String adapterId) {
        // Calculate trend over past week
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekAgo = now.minusDays(7);
        LocalDateTime twoWeeksAgo = now.minusDays(14);

        double thisWeek = calculateAverageResponseTimeInPeriod(adapterId, weekAgo, now);
        double lastWeek = calculateAverageResponseTimeInPeriod(adapterId, twoWeeksAgo, weekAgo);

        return thisWeek - lastWeek;
    }

    private double calculateCurrentErrorRate() {
        LocalDateTime hourAgo = LocalDateTime.now().minusHours(1);
        long totalLogs = systemLogRepository.countByTimestampAfter(hourAgo);
        long errorLogs = systemLogRepository.countByLevelAndTimestampAfter(SystemLog.LogLevel.ERROR, hourAgo);

        return totalLogs > 0 ? (double) errorLogs / totalLogs * 100 : 0;
    }

    private double calculateErrorRateAt(LocalDateTime time) {
        LocalDateTime hourBefore = time.minusHours(1);
        long totalLogs = systemLogRepository.count(); // Simplified - would need custom query
        long errorLogs = 0; // Simplified - would need custom query

        return totalLogs > 0 ? (double) errorLogs / totalLogs * 100 : 0;
    }

    private double calculateCurrentAverageResponseTime() {
        LocalDateTime hourAgo = LocalDateTime.now().minusHours(1);
        List<SystemLog> logs = systemLogRepository.findAll().stream()
            .filter(log -> log.getTimestamp().isAfter(hourAgo))
            .collect(Collectors.toList());
        return calculateAverageResponseTime(logs);
    }

    private double calculateResponseTimeAt(LocalDateTime time) {
        LocalDateTime hourBefore = time.minusHours(1);
        List<SystemLog> logs = systemLogRepository.findAll().stream()
            .filter(log -> log.getTimestamp().isAfter(hourBefore) && log.getTimestamp().isBefore(time))
            .collect(Collectors.toList());
        return calculateAverageResponseTime(logs);
    }

    private double calculateCurrentAvailability() {
        return calculateAvailabilityInPeriod(LocalDateTime.now().minusDays(1), LocalDateTime.now());
    }

    private double calculateAvailabilityAt(LocalDateTime time) {
        return calculateAvailabilityInPeriod(time.minusDays(1), time);
    }

    private double calculateAvailabilityInPeriod(LocalDateTime start, LocalDateTime end) {
        // Count adapters that were active during this period
        List<AdapterStatus> statuses = adapterStatusRepository.findAll();
        long totalAdapters = statuses.size();

        if(totalAdapters == 0) return 100;

        long activeAdapters = statuses.stream()
            .filter(s -> "ACTIVE".equals(s.getStatus()))
            .count();

        return(double) activeAdapters / totalAdapters * 100;
    }
}
