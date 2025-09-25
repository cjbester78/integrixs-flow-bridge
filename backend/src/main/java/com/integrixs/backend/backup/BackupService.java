package com.integrixs.backend.backup;

import com.integrixs.data.model.AuditEvent;
import com.integrixs.backend.audit.AuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

/**
 * Service for automated backup operations
 */
@Service
public class BackupService {

    private static final Logger logger = LoggerFactory.getLogger(BackupService.class);
    private static final DateTimeFormatter TIMESTAMP_FORMAT =
        DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    @Autowired
    private DataSource dataSource;

    @Autowired
    private AuditService auditService;

    @Autowired(required = false)
    private CloudStorageService cloudStorageService;

    @Value("${backup.local.path:/backup}")
    private String localBackupPath;

    @Value("${backup.retention.days:30}")
    private int retentionDays;

    @Value("${backup.database.enabled:true}")
    private boolean databaseBackupEnabled;

    @Value("${backup.files.enabled:true}")
    private boolean filesBackupEnabled;

    @Value("${backup.cloud.enabled:false}")
    private boolean cloudBackupEnabled;

    @Value("${backup.encryption.enabled:true}")
    private boolean encryptionEnabled;

    /**
     * Scheduled database backup-runs daily at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void performScheduledDatabaseBackup() {
        if(!databaseBackupEnabled) {
            return;
        }

        logger.info("Starting scheduled database backup");

        try {
            BackupResult result = backupDatabase();

            if(result.isSuccess()) {
                logger.info("Database backup completed successfully: {}", result.getBackupPath());

                // Upload to cloud if enabled
                if(cloudBackupEnabled && cloudStorageService != null) {
                    uploadToCloud(result.getBackupPath());
                }

                // Clean old backups
                cleanOldBackups("database");
            }

            // Log audit event
            auditService.logSecurityEvent(
                AuditEvent.AuditEventType.SYSTEM_START,
                "Database backup completed",
                Map.of(
                    "backupPath", result.getBackupPath(),
                    "size", String.valueOf(result.getSize()),
                    "duration", String.valueOf(result.getDurationMs())
               )
           );

        } catch(Exception e) {
            logger.error("Database backup failed", e);

            auditService.logSecurityEvent(
                AuditEvent.AuditEventType.MAINTENANCE_PERFORMED,
                "Database backup failed: " + e.getMessage(),
                Map.of("error", e.getMessage(), "type", "backup_failure")
           );

            sendAlert("Database backup failed: " + e.getMessage());
        }
    }

    /**
     * Perform database backup
     */
    public BackupResult backupDatabase() throws Exception {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String backupName = String.format("db_backup_%s.sql.gz", timestamp);
        Path backupPath = Paths.get(localBackupPath, "database", backupName);

        // Ensure directory exists
        Files.createDirectories(backupPath.getParent());

        long startTime = System.currentTimeMillis();

        // Perform backup using pg_dump
        ProcessBuilder pb = new ProcessBuilder(
            "pg_dump",
            "--host = localhost",
            "--username = postgres",
            "--no-password",
            "--verbose",
            "--format = plain",
            "--no-owner",
            "--no-acl",
            "integrixs"
       );

        pb.environment().put("PGPASSWORD", getDbPassword());

        Process process = pb.start();

        // Compress output
        try(InputStream input = process.getInputStream();
             GZIPOutputStream gzipOutput = new GZIPOutputStream(
                 new FileOutputStream(backupPath.toFile()))) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            while((bytesRead = input.read(buffer)) != -1) {
                gzipOutput.write(buffer, 0, bytesRead);
            }
        }

        int exitCode = process.waitFor();
        if(exitCode != 0) {
            throw new RuntimeException("pg_dump failed with exit code: " + exitCode);
        }

        long size = Files.size(backupPath);
        long duration = System.currentTimeMillis()-startTime;

        // Verify backup
        verifyDatabaseBackup(backupPath);

        return BackupResult.success(backupPath.toString(), size, duration);
    }

    /**
     * Backup application files
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void performScheduledFileBackup() {
        if(!filesBackupEnabled) {
            return;
        }

        logger.info("Starting scheduled file backup");

        try {
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
            String backupName = String.format("files_backup_%s.tar.gz", timestamp);
            Path backupPath = Paths.get(localBackupPath, "files", backupName);

            // Ensure directory exists
            Files.createDirectories(backupPath.getParent());

            // Create tar.gz archive
            ProcessBuilder pb = new ProcessBuilder(
                "tar",
                "-czf",
                backupPath.toString(),
                "-C", "/data/integrixs",
                "uploads",
                "configurations",
                "templates"
           );

            Process process = pb.start();
            int exitCode = process.waitFor();

            if(exitCode != 0) {
                throw new RuntimeException("tar failed with exit code: " + exitCode);
            }

            long size = Files.size(backupPath);

            logger.info("File backup completed: {} ( {} bytes)", backupPath, size);

            // Upload to cloud if enabled
            if(cloudBackupEnabled && cloudStorageService != null) {
                uploadToCloud(backupPath.toString());
            }

            // Clean old backups
            cleanOldBackups("files");

        } catch(Exception e) {
            logger.error("File backup failed", e);
            sendAlert("File backup failed: " + e.getMessage());
        }
    }

    /**
     * Perform configuration backup
     */
    public BackupResult backupConfiguration() throws Exception {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String backupName = String.format("config_backup_%s.tar.gz", timestamp);
        Path backupPath = Paths.get(localBackupPath, "config", backupName);

        // Ensure directory exists
        Files.createDirectories(backupPath.getParent());

        long startTime = System.currentTimeMillis();

        // Export database configuration
        exportDatabaseConfig(backupPath.getParent());

        // Create archive
        ProcessBuilder pb = new ProcessBuilder(
            "tar",
            "-czf",
            backupPath.toString(),
            "-C", "/etc/integrixs",
            "."
       );

        Process process = pb.start();
        int exitCode = process.waitFor();

        if(exitCode != 0) {
            throw new RuntimeException("Configuration backup failed");
        }

        long size = Files.size(backupPath);
        long duration = System.currentTimeMillis()-startTime;

        return BackupResult.success(backupPath.toString(), size, duration);
    }

    /**
     * Export database configuration
     */
    private void exportDatabaseConfig(Path outputDir) throws Exception {
        Path configFile = outputDir.resolve("db_config.json");

        Map<String, Object> dbConfig = new HashMap<>();

        try(Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            // Export adapter configurations
            ResultSet rs = stmt.executeQuery(
                "SELECT id, name, type, configuration FROM adapters"
           );

            List<Map<String, Object>> adapters = new ArrayList<>();
            while(rs.next()) {
                Map<String, Object> adapter = new HashMap<>();
                adapter.put("id", rs.getString("id"));
                adapter.put("name", rs.getString("name"));
                adapter.put("type", rs.getString("type"));
                adapter.put("configuration", rs.getString("configuration"));
                adapters.add(adapter);
            }
            dbConfig.put("adapters", adapters);

            // Export flow configurations
            rs = stmt.executeQuery(
                "SELECT id, name, configuration FROM integration_flows"
           );

            List<Map<String, Object>> flows = new ArrayList<>();
            while(rs.next()) {
                Map<String, Object> flow = new HashMap<>();
                flow.put("id", rs.getString("id"));
                flow.put("name", rs.getString("name"));
                flow.put("configuration", rs.getString("configuration"));
                flows.add(flow);
            }
            dbConfig.put("flows", flows);
        }

        // Write to file
        Files.writeString(configFile, toJson(dbConfig));
    }

    /**
     * Verify database backup integrity
     */
    private void verifyDatabaseBackup(Path backupPath) throws Exception {
        // Basic verification-check if file is readable and has content
        if(!Files.exists(backupPath) || Files.size(backupPath) < 1000) {
            throw new RuntimeException("Backup file is invalid or too small");
        }

        // Could add more sophisticated verification here
        logger.debug("Backup verification passed for: {}", backupPath);
    }

    /**
     * Upload backup to cloud storage
     */
    private CompletableFuture<Void> uploadToCloud(String localPath) {
        return CompletableFuture.runAsync(() -> {
            try {
                logger.info("Uploading backup to cloud: {}", localPath);
                cloudStorageService.uploadFile(localPath);
                logger.info("Cloud upload completed for: {}", localPath);
            } catch(Exception e) {
                logger.error("Cloud upload failed for: {}", localPath, e);
            }
        });
    }

    /**
     * Clean old backup files
     */
    private void cleanOldBackups(String type) {
        try {
            Path backupDir = Paths.get(localBackupPath, type);
            if(!Files.exists(backupDir)) {
                return;
            }

            LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);

            Files.walk(backupDir)
                .filter(Files::isRegularFile)
                .filter(path -> {
                    try {
                        LocalDateTime fileTime = LocalDateTime.ofInstant(
                            Files.getLastModifiedTime(path).toInstant(),
                            java.time.ZoneId.systemDefault()
                       );
                        return fileTime.isBefore(cutoff);
                    } catch(Exception e) {
                        return false;
                    }
                })
                .forEach(path -> {
                    try {
                        Files.delete(path);
                        logger.info("Deleted old backup: {}", path);
                    } catch(Exception e) {
                        logger.warn("Failed to delete old backup: {}", path, e);
                    }
                });

        } catch(Exception e) {
            logger.error("Error cleaning old backups", e);
        }
    }

    /**
     * Get database password from environment or config
     */
    private String getDbPassword() {
        // In production, retrieve from secure storage
        return System.getenv("DB_PASSWORD");
    }

    /**
     * Send alert notification
     */
    private void sendAlert(String message) {
        // Implementation would send email/SMS/Slack notification
        logger.error("ALERT: {}", message);
    }

    /**
     * Convert object to JSON
     */
    private String toJson(Object obj) throws Exception {
        // Simple JSON conversion-use Jackson in production
        return obj.toString();
    }

    /**
     * List available backups
     */
    public List<BackupInfo> listBackups(String type) {
        try {
            Path backupDir = Paths.get(localBackupPath, type);
            if(!Files.exists(backupDir)) {
                return Collections.emptyList();
            }

            return Files.walk(backupDir)
                .filter(Files::isRegularFile)
                .map(path -> {
                    try {
                        return new BackupInfo(
                            path.getFileName().toString(),
                            path.toString(),
                            Files.size(path),
                            Files.getLastModifiedTime(path).toInstant()
                       );
                    } catch(Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(BackupInfo::getCreatedAt).reversed())
                .collect(Collectors.toList());

        } catch(Exception e) {
            logger.error("Error listing backups", e);
            return Collections.emptyList();
        }
    }

    /**
     * Backup result
     */
    public static class BackupResult {
        private final boolean success;
        private final String backupPath;
        private final long size;
        private final long durationMs;
        private final String error;

        private BackupResult(boolean success, String backupPath, long size,
                           long durationMs, String error) {
            this.success = success;
            this.backupPath = backupPath;
            this.size = size;
            this.durationMs = durationMs;
            this.error = error;
        }

        public static BackupResult success(String path, long size, long duration) {
            return new BackupResult(true, path, size, duration, null);
        }

        public static BackupResult failure(String error) {
            return new BackupResult(false, null, 0, 0, error);
        }

        // Getters
        public boolean isSuccess() { return success; }
        public String getBackupPath() { return backupPath; }
        public long getSize() { return size; }
        public long getDurationMs() { return durationMs; }
        public String getError() { return error; }
    }

    /**
     * Backup information
     */
    public static class BackupInfo {
        private final String name;
        private final String path;
        private final long size;
        private final java.time.Instant createdAt;

        public BackupInfo(String name, String path, long size, java.time.Instant createdAt) {
            this.name = name;
            this.path = path;
            this.size = size;
            this.createdAt = createdAt;
        }

        // Getters
        public String getName() { return name; }
        public String getPath() { return path; }
        public long getSize() { return size; }
        public java.time.Instant getCreatedAt() { return createdAt; }
    }

    /**
     * Cloud storage service interface
     */
    public interface CloudStorageService {
        void uploadFile(String localPath) throws Exception;
        void downloadFile(String remotePath, String localPath) throws Exception;
        List<String> listFiles(String prefix) throws Exception;
    }
}
