package com.integrixs.adapters.monitoring;

import com.integrixs.adapters.domain.model.AdapterConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Service for monitoring and tracking SLA compliance for adapter operations.
 */
@Service
public class SLAMonitoringService {

    private static final Logger logger = LoggerFactory.getLogger(SLAMonitoringService.class);

    private final PerformanceMetricsCollector metricsCollector;

    // SLA definitions
    private final Map<String, SLADefinition> slaDefinitions = new ConcurrentHashMap<>();

    // SLA compliance tracking
    private final Map<String, SLAComplianceTracker> complianceTrackers = new ConcurrentHashMap<>();

    // Alert callbacks
    private final List<SLAAlertHandler> alertHandlers = new ArrayList<>();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Autowired
    public SLAMonitoringService(PerformanceMetricsCollector metricsCollector) {
        this.metricsCollector = metricsCollector;
        initializeDefaultSLAs();
        startComplianceMonitoring();
    }

    /**
     * Initialize default SLA definitions.
     */
    private void initializeDefaultSLAs() {
        // Connection test SLAs
        defineSLA("testConnection", new SLADefinition(
            "Connection Test SLA",
            5000, // 5 seconds response time
            99.0, // 99% availability
            95.0   // 95% success rate
       ));

        // Data transfer SLAs
        defineSLA("send", new SLADefinition(
            "Send Operation SLA",
            10000, // 10 seconds response time
            99.9, // 99.9% availability
            99.0   // 99% success rate
       ));

        defineSLA("fetch", new SLADefinition(
            "Fetch Operation SLA",
            10000, // 10 seconds response time
            99.9, // 99.9% availability
            99.0   // 99% success rate
       ));

        // Polling SLAs
        defineSLA("poll", new SLADefinition(
            "Polling Operation SLA",
            5000, // 5 seconds response time
            99.5, // 99.5% availability
            98.0   // 98% success rate
       ));

        // Transformation SLAs
        defineSLA("transform", new SLADefinition(
            "Transformation SLA",
            1000, // 1 second response time
            99.99, // 99.99% availability
            99.9   // 99.9% success rate
       ));
    }

    /**
     * Define an SLA for an operation.
     */
    public void defineSLA(String operationName, SLADefinition sla) {
        slaDefinitions.put(operationName, sla);
        metricsCollector.setSLAThreshold(operationName, sla.getMaxResponseTimeMs());
        logger.info("Defined SLA for operation ' {}': {}", operationName, sla);
    }

    /**
     * Track SLA compliance for an operation.
     */
    public void trackOperation(String adapterType, String adapterMode, String operationName,
                             long durationMs, boolean success) {
        String key = createKey(adapterType, adapterMode, operationName);
        SLADefinition sla = slaDefinitions.get(operationName);

        if(sla == null) {
            return; // No SLA defined for this operation
        }

        SLAComplianceTracker tracker = complianceTrackers.computeIfAbsent(key,
            k -> new SLAComplianceTracker(adapterType, adapterMode, operationName, sla));

        tracker.recordOperation(durationMs, success);

        // Check for violations
        checkSLAViolations(tracker);
    }

    /**
     * Get SLA compliance report.
     */
    public SLAComplianceReport getComplianceReport(String adapterType, String adapterMode,
                                                  String operationName) {
        String key = createKey(adapterType, adapterMode, operationName);
        SLAComplianceTracker tracker = complianceTrackers.get(key);

        if(tracker == null) {
            return null;
        }

        return tracker.generateReport();
    }

    /**
     * Get all SLA compliance reports.
     */
    public List<SLAComplianceReport> getAllComplianceReports() {
        return complianceTrackers.values().stream()
            .map(SLAComplianceTracker::generateReport)
            .collect(Collectors.toList());
    }

    /**
     * Register an SLA alert handler.
     */
    public void registerAlertHandler(SLAAlertHandler handler) {
        alertHandlers.add(handler);
    }

    /**
     * Check for SLA violations and trigger alerts.
     */
    private void checkSLAViolations(SLAComplianceTracker tracker) {
        SLAComplianceReport report = tracker.generateReport();

        List<String> violations = new ArrayList<>();

        // Check response time compliance
        if(report.getResponseTimeCompliance() < tracker.getSla().getMinAvailabilityPercent()) {
            violations.add(String.format("Response time compliance(%.2f%%) below threshold(%.2f%%)",
                report.getResponseTimeCompliance(), tracker.getSla().getMinAvailabilityPercent()));
        }

        // Check success rate compliance
        if(report.getSuccessRate() < tracker.getSla().getMinSuccessRatePercent()) {
            violations.add(String.format("Success rate(%.2f%%) below threshold(%.2f%%)",
                report.getSuccessRate(), tracker.getSla().getMinSuccessRatePercent()));
        }

        // Check availability(based on recent operations)
        double availability = tracker.calculateAvailability();
        if(availability < tracker.getSla().getMinAvailabilityPercent()) {
            violations.add(String.format("Availability(%.2f%%) below threshold(%.2f%%)",
                availability, tracker.getSla().getMinAvailabilityPercent()));
        }

        if(!violations.isEmpty()) {
            SLAViolationEvent event = new SLAViolationEvent(
                tracker.getAdapterType(),
                tracker.getAdapterMode(),
                tracker.getOperationName(),
                violations,
                report
           );

            notifyViolation(event);
        }
    }

    /**
     * Notify alert handlers of SLA violation.
     */
    private void notifyViolation(SLAViolationEvent event) {
        logger.warn("SLA violation detected for {} - {} - {}: {}",
            event.getAdapterType(), event.getAdapterMode(), event.getOperationName(),
            String.join(", ", event.getViolations()));

        for(SLAAlertHandler handler : alertHandlers) {
            try {
                handler.handleSLAViolation(event);
            } catch(Exception e) {
                logger.error("Error notifying SLA violation handler", e);
            }
        }
    }

    /**
     * Start periodic compliance monitoring.
     */
    private void startComplianceMonitoring() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                checkAllCompliance();
            } catch(Exception e) {
                logger.error("Error during compliance monitoring", e);
            }
        }, 1, 1, TimeUnit.MINUTES);
    }

    /**
     * Check compliance for all tracked operations.
     */
    private void checkAllCompliance() {
        for(SLAComplianceTracker tracker : complianceTrackers.values()) {
            checkSLAViolations(tracker);
        }
    }

    /**
     * Create a tracking key.
     */
    private String createKey(String adapterType, String adapterMode, String operationName) {
        return String.format("%s-%s-%s", adapterType, adapterMode, operationName);
    }

    /**
     * Shutdown the monitoring service.
     */
    public void shutdown() {
        scheduler.shutdown();
        try {
            if(!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch(InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * SLA definition class.
     */
    public static class SLADefinition {
        private final String name;
        private final long maxResponseTimeMs;
        private final double minAvailabilityPercent;
        private final double minSuccessRatePercent;

        public SLADefinition(String name, long maxResponseTimeMs,
                           double minAvailabilityPercent, double minSuccessRatePercent) {
            this.name = name;
            this.maxResponseTimeMs = maxResponseTimeMs;
            this.minAvailabilityPercent = minAvailabilityPercent;
            this.minSuccessRatePercent = minSuccessRatePercent;
        }

        // Getters
        public String getName() { return name; }
        public long getMaxResponseTimeMs() { return maxResponseTimeMs; }
        public double getMinAvailabilityPercent() { return minAvailabilityPercent; }
        public double getMinSuccessRatePercent() { return minSuccessRatePercent; }

        @Override
        public String toString() {
            return String.format("SLA[%s: maxResponse = %dms, minAvailability = %.1f%%, minSuccess = %.1f%%]",
                name, maxResponseTimeMs, minAvailabilityPercent, minSuccessRatePercent);
        }
    }

    /**
     * SLA compliance tracker.
     */
    private static class SLAComplianceTracker {
        private final String adapterType;
        private final String adapterMode;
        private final String operationName;
        private final SLADefinition sla;

        private final List<OperationRecord> operations = Collections.synchronizedList(new ArrayList<>());
        private final long windowSizeMs = 3600000; // 1 hour window

        public SLAComplianceTracker(String adapterType, String adapterMode,
                                   String operationName, SLADefinition sla) {
            this.adapterType = adapterType;
            this.adapterMode = adapterMode;
            this.operationName = operationName;
            this.sla = sla;
        }

        public void recordOperation(long durationMs, boolean success) {
            operations.add(new OperationRecord(durationMs, success));
            cleanupOldRecords();
        }

        public SLAComplianceReport generateReport() {
            List<OperationRecord> currentOps = getRecentOperations();

            if(currentOps.isEmpty()) {
                return new SLAComplianceReport(adapterType, adapterMode, operationName,
                    0, 100.0, 100.0, 100.0, 0.0, 0.0);
            }

            long totalOps = currentOps.size();
            long successfulOps = currentOps.stream().filter(op -> op.success).count();
            long compliantOps = currentOps.stream()
                .filter(op -> op.durationMs <= sla.maxResponseTimeMs)
                .count();

            double avgResponseTime = currentOps.stream()
                .mapToLong(op -> op.durationMs)
                .average()
                .orElse(0.0);

            double maxResponseTime = currentOps.stream()
                .mapToLong(op -> op.durationMs)
                .max()
                .orElse(0);

            double successRate = (double) successfulOps / totalOps * 100;
            double responseTimeCompliance = (double) compliantOps / totalOps * 100;
            double availability = calculateAvailability();

            return new SLAComplianceReport(
                adapterType, adapterMode, operationName,
                totalOps, successRate, responseTimeCompliance, availability,
                avgResponseTime, maxResponseTime
           );
        }

        public double calculateAvailability() {
            // Simple availability calculation based on recent operations
            List<OperationRecord> recentOps = getRecentOperations();
            if(recentOps.isEmpty()) {
                return 100.0;
            }

            // Check for extended periods without operations(potential downtime)
            long expectedOpsPerHour = 60; // Assume at least 1 operation per minute
            long actualOps = recentOps.size();

            return Math.min(100.0, (double) actualOps / expectedOpsPerHour * 100);
        }

        private List<OperationRecord> getRecentOperations() {
            long cutoff = System.currentTimeMillis() - windowSizeMs;
            return operations.stream()
                .filter(op -> op.timestamp > cutoff)
                .collect(Collectors.toList());
        }

        private void cleanupOldRecords() {
            long cutoff = System.currentTimeMillis() - windowSizeMs;
            operations.removeIf(op -> op.timestamp <= cutoff);
        }

        // Getters
        public String getAdapterType() { return adapterType; }
        public String getAdapterMode() { return adapterMode; }
        public String getOperationName() { return operationName; }
        public SLADefinition getSla() { return sla; }
    }

    /**
     * Operation record.
     */
    private static class OperationRecord {
        private final long timestamp;
        private final long durationMs;
        private final boolean success;

        public OperationRecord(long durationMs, boolean success) {
            this.timestamp = System.currentTimeMillis();
            this.durationMs = durationMs;
            this.success = success;
        }
    }

    /**
     * SLA compliance report.
     */
    public static class SLAComplianceReport {
        private final String adapterType;
        private final String adapterMode;
        private final String operationName;
        private final long totalOperations;
        private final double successRate;
        private final double responseTimeCompliance;
        private final double availability;
        private final double avgResponseTimeMs;
        private final double maxResponseTimeMs;
        private final LocalDateTime reportTime;

        public SLAComplianceReport(String adapterType, String adapterMode, String operationName,
                                 long totalOperations, double successRate,
                                 double responseTimeCompliance, double availability,
                                 double avgResponseTimeMs, double maxResponseTimeMs) {
            this.adapterType = adapterType;
            this.adapterMode = adapterMode;
            this.operationName = operationName;
            this.totalOperations = totalOperations;
            this.successRate = successRate;
            this.responseTimeCompliance = responseTimeCompliance;
            this.availability = availability;
            this.avgResponseTimeMs = avgResponseTimeMs;
            this.maxResponseTimeMs = maxResponseTimeMs;
            this.reportTime = LocalDateTime.now();
        }

        // Getters
        public String getAdapterType() { return adapterType; }
        public String getAdapterMode() { return adapterMode; }
        public String getOperationName() { return operationName; }
        public long getTotalOperations() { return totalOperations; }
        public double getSuccessRate() { return successRate; }
        public double getResponseTimeCompliance() { return responseTimeCompliance; }
        public double getAvailability() { return availability; }
        public double getAvgResponseTimeMs() { return avgResponseTimeMs; }
        public double getMaxResponseTimeMs() { return maxResponseTimeMs; }
        public LocalDateTime getReportTime() { return reportTime; }
    }

    /**
     * SLA violation event.
     */
    public static class SLAViolationEvent {
        private final String adapterType;
        private final String adapterMode;
        private final String operationName;
        private final List<String> violations;
        private final SLAComplianceReport report;
        private final LocalDateTime timestamp;

        public SLAViolationEvent(String adapterType, String adapterMode, String operationName,
                               List<String> violations, SLAComplianceReport report) {
            this.adapterType = adapterType;
            this.adapterMode = adapterMode;
            this.operationName = operationName;
            this.violations = violations;
            this.report = report;
            this.timestamp = LocalDateTime.now();
        }

        // Getters
        public String getAdapterType() { return adapterType; }
        public String getAdapterMode() { return adapterMode; }
        public String getOperationName() { return operationName; }
        public List<String> getViolations() { return violations; }
        public SLAComplianceReport getReport() { return report; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }

    /**
     * Interface for SLA alert handlers.
     */
    public interface SLAAlertHandler {
        void handleSLAViolation(SLAViolationEvent event);
    }
}
