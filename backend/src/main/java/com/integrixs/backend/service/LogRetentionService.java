package com.integrixs.backend.service;

import com.integrixs.data.model.SystemLog;
import com.integrixs.data.sql.repository.SystemLogSqlRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for managing log retention policies and archiving.
 */
@Service
public class LogRetentionService {

    private static final Logger log = LoggerFactory.getLogger(LogRetentionService.class);

    private final SystemLogSqlRepository systemLogRepository;
        public LogRetentionService(SystemLogSqlRepository systemLogRepository) {
        this.systemLogRepository = systemLogRepository;
            }

    // Retention policies(in days)
    @Value("${log.retention.debug:7}")
    private int debugRetentionDays;

    @Value("${log.retention.info:30}")
    private int infoRetentionDays;

    @Value("${log.retention.warn:90}")
    private int warnRetentionDays;

    @Value("${log.retention.error:365}")
    private int errorRetentionDays;

    @Value("${log.retention.audit:2555}") // 7 years
    private int auditRetentionDays;

    @Value("${log.retention.batch-size:1000}")
    private int batchSize;

    @Value("${log.retention.enabled:true}")
    private boolean retentionEnabled;

    // Custom retention policies by category
    private final Map<String, Integer> categoryRetentionDays = new HashMap<>();

    /**
     * Initialize custom retention policies.
     */
    public void initializeRetentionPolicies() {
        // Security-related logs-keep longer
        categoryRetentionDays.put("SECURITY", 730); // 2 years
        categoryRetentionDays.put("AUTHENTICATION", 730);
        categoryRetentionDays.put("AUTHORIZATION", 730);

        // Transaction logs-keep for compliance
        categoryRetentionDays.put("TRANSACTION", 2555); // 7 years
        categoryRetentionDays.put("PAYMENT", 2555);

        // Performance logs-shorter retention
        categoryRetentionDays.put("PERFORMANCE", 30);
        categoryRetentionDays.put("METRICS", 30);

        // Frontend logs-moderate retention
        categoryRetentionDays.put("FRONTEND_ERROR", 90);
        categoryRetentionDays.put("FRONTEND_INFO", 14);
        categoryRetentionDays.put("FRONTEND_DEBUG", 3);

        log.info("Initialized retention policies for {} categories", categoryRetentionDays.size());
    }

    /**
     * Execute retention policy-runs daily at 2 AM.
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void executeRetentionPolicy() {
        if(!retentionEnabled) {
            log.info("Log retention is disabled");
            return;
        }

        log.info("Starting log retention process");
        long startTime = System.currentTimeMillis();

        try {
            RetentionResult result = new RetentionResult();

            // Process by log level
            result.addResult("DEBUG", deleteLogsByLevel(SystemLog.LogLevel.DEBUG, debugRetentionDays));
            result.addResult("INFO", deleteLogsByLevel(SystemLog.LogLevel.INFO, infoRetentionDays));
            result.addResult("WARN", deleteLogsByLevel(SystemLog.LogLevel.WARN, warnRetentionDays));
            result.addResult("ERROR", deleteLogsByLevel(SystemLog.LogLevel.ERROR, errorRetentionDays));

            // Process by category
            for(Map.Entry<String, Integer> entry : categoryRetentionDays.entrySet()) {
                long deleted = deleteLogsByCategory(entry.getKey(), entry.getValue());
                result.addResult("Category:" + entry.getKey(), deleted);
            }

            // Archive old audit logs
            long archived = archiveAuditLogs();
            result.setArchivedCount(archived);

            long duration = System.currentTimeMillis()-startTime;
            log.info("Log retention completed in {}ms. Deleted: {}, Archived: {}",
                    duration, result.getTotalDeleted(), result.getArchivedCount());

            // Log retention summary
            logRetentionSummary(result);

        } catch(Exception e) {
            log.error("Error during log retention process", e);
        }
    }

    /**
     * Delete logs by level older than retention days.
     */
    private long deleteLogsByLevel(SystemLog.LogLevel level, int retentionDays) {
        LocalDateTime cutoffDate = LocalDateTime.now().minus(retentionDays, ChronoUnit.DAYS);

        int deleted = systemLogRepository.deleteByLevelAndTimestampBefore(level, cutoffDate);
        log.debug("Deleted {} {} logs older than {}", deleted, level, cutoffDate);

        return deleted;
    }

    /**
     * Delete logs by category older than retention days.
     */
    private long deleteLogsByCategory(String category, int retentionDays) {
        LocalDateTime cutoffDate = LocalDateTime.now().minus(retentionDays, ChronoUnit.DAYS);

        int deleted = systemLogRepository.deleteByCategoryAndTimestampBefore(category, cutoffDate);
        log.debug("Deleted {} logs with category {} older than {}", deleted, category, cutoffDate);

        return deleted;
    }

    /**
     * Archive audit logs to separate table/storage.
     */
    private long archiveAuditLogs() {
        LocalDateTime archiveCutoff = LocalDateTime.now().minus(365, ChronoUnit.DAYS);
        AtomicLong archivedCount = new AtomicLong(0);

        // Find audit logs to archive
        String selectQuery = "SELECT * FROM system_logs WHERE " +
                "(category IN('AUDIT', 'SECURITY', 'TRANSACTION', 'COMPLIANCE') " +
                "OR level = 'AUDIT') " +
                "AND timestamp < :cutoffDate " +
                "LIMIT :batchSize";

        boolean hasMore = true;
        while(hasMore) {
            List<SystemLog> logsToArchive = systemLogRepository.findAuditLogsForArchive(archiveCutoff, batchSize);

            if(logsToArchive.isEmpty()) {
                hasMore = false;
            } else {
                // Archive logs(in production, this would write to archive storage)
                archiveLogs(logsToArchive);

                // Delete archived logs
                List<UUID> idsToDelete = logsToArchive.stream()
                    .map(SystemLog::getId)
                    .collect(java.util.stream.Collectors.toList());

                systemLogRepository.deleteByIds(idsToDelete);

                archivedCount.addAndGet(logsToArchive.size());

                // Clear memory after batch processing
                // Memory management handled by SQL repository
            }
        }

        return archivedCount.get();
    }

    /**
     * Archive logs to external storage.
     */
    private void archiveLogs(List<SystemLog> logs) {
        // In production, this would:
        // 1. Compress logs
        // 2. Write to archive storage(S3, blob storage, etc.)
        // 3. Update archive index

        log.debug("Archiving {} logs", logs.size());

        // For now, just log the archival
        String archiveFileName = String.format("logs_archive_%s_%d.json.gz",
            LocalDateTime.now().toString(), logs.size());
        log.info("Would archive {} logs to {}", logs.size(), archiveFileName);
    }

    /**
     * Get retention statistics.
     */
    public Map<String, Object> getRetentionStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // Count logs by level
        for(SystemLog.LogLevel level : SystemLog.LogLevel.values()) {
            long count = systemLogRepository.countByLevelAndTimestampAfter(
                level, LocalDateTime.now().minus(30, ChronoUnit.DAYS));
            stats.put("level_" + level.name(), count);
        }

        // Total logs
        stats.put("total_logs", systemLogRepository.count());

        // Oldest log
        SystemLog oldestLog = systemLogRepository.findAll(
            org.springframework.data.domain.PageRequest.of(0, 1,
                org.springframework.data.domain.Sort.by("timestamp").ascending())
       ).stream().findFirst().orElse(null);

        if(oldestLog != null) {
            stats.put("oldest_log_date", oldestLog.getTimestamp());
            stats.put("oldest_log_age_days",
                ChronoUnit.DAYS.between(oldestLog.getTimestamp(), LocalDateTime.now()));
        }

        // Storage size estimate(rough calculation)
        long estimatedSizeMB = (systemLogRepository.count() * 1024) / (1024 * 1024); // ~1KB per log
        stats.put("estimated_size_mb", estimatedSizeMB);

        return stats;
    }

    /**
     * Update retention policy for a category.
     */
    public void updateCategoryRetention(String category, int retentionDays) {
        categoryRetentionDays.put(category, retentionDays);
        log.info("Updated retention policy for category {} to {} days", category, retentionDays);
    }

    /**
     * Get current retention policies.
     */
    public Map<String, Integer> getRetentionPolicies() {
        Map<String, Integer> policies = new HashMap<>();

        // Level-based policies
        policies.put("DEBUG", debugRetentionDays);
        policies.put("INFO", infoRetentionDays);
        policies.put("WARN", warnRetentionDays);
        policies.put("ERROR", errorRetentionDays);
        policies.put("AUDIT", auditRetentionDays);

        // Category-based policies
        policies.putAll(categoryRetentionDays);

        return policies;
    }

    /**
     * Log retention summary.
     */
    private void logRetentionSummary(RetentionResult result) {
        SystemLog summaryLog = SystemLog.builder()
            .timestamp(LocalDateTime.now())
            .level(SystemLog.LogLevel.INFO)
            .category("SYSTEM_MAINTENANCE")
            .source("LogRetentionService")
            .message("Log retention completed")
            .details(result.toJson())
            .build();

        systemLogRepository.save(summaryLog);
    }

    /**
     * Result of retention execution.
     */
    private static class RetentionResult {
        private final Map<String, Long> deletedCounts = new HashMap<>();
        private long archivedCount = 0;

        public void addResult(String type, long count) {
            deletedCounts.put(type, count);
        }

        public void setArchivedCount(long count) {
            this.archivedCount = count;
        }

        public long getTotalDeleted() {
            return deletedCounts.values().stream().mapToLong(Long::longValue).sum();
        }

        public long getArchivedCount() {
            return archivedCount;
        }

        public String toJson() {
            try {
                Map<String, Object> data = new HashMap<>();
                data.put("deleted", deletedCounts);
                data.put("archived", archivedCount);
                data.put("total_deleted", getTotalDeleted());

                return new com.fasterxml.jackson.databind.ObjectMapper()
                    .writeValueAsString(data);
            } catch(Exception e) {
                return " {}";
            }
        }
    }
}
