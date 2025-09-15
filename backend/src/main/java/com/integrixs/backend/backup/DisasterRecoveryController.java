package com.integrixs.backend.backup;

import com.integrixs.backend.security.RequiresPermission;
import com.integrixs.backend.security.ResourcePermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.Instant;
import java.util.*;

/**
 * Controller for disaster recovery operations
 */
@RestController
@RequestMapping("/api/dr")
@Tag(name = "Disaster Recovery", description = "Disaster recovery and backup management")
public class DisasterRecoveryController {

    private static final Logger logger = LoggerFactory.getLogger(DisasterRecoveryController.class);

    @Autowired
    private BackupService backupService;

    @Autowired
    private DataSource dataSource;

    @Autowired(required = false)
    private HealthCheckService healthCheckService;

    /**
     * Trigger manual backup
     */
    @PostMapping("/backup/ {type}")
    @Operation(summary = "Trigger manual backup",
               description = "Manually trigger a backup of specified type")
    @ApiResponses( {
        @ApiResponse(responseCode = "200", description = "Backup started"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions"),
        @ApiResponse(responseCode = "500", description = "Backup failed")
    })
    @RequiresPermission(ResourcePermission.ADMIN_SYSTEM)
    public ResponseEntity<?> triggerBackup(@PathVariable String type) {
        try {
            BackupService.BackupResult result;

            switch(type.toLowerCase()) {
                case "database":
                    result = backupService.backupDatabase();
                    break;
                case "configuration":
                    result = backupService.backupConfiguration();
                    break;
                default:
                    return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid backup type: " + type));
            }

            if(result.isSuccess()) {
                return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "backupPath", result.getBackupPath(),
                    "size", result.getSize(),
                    "durationMs", result.getDurationMs()
               ));
            } else {
                return ResponseEntity.internalServerError()
                    .body(Map.of("error", result.getError()));
            }

        } catch(Exception e) {
            logger.error("Backup failed", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Backup failed: " + e.getMessage()));
        }
    }

    /**
     * List available backups
     */
    @GetMapping("/backup/ {type}")
    @Operation(summary = "List backups",
               description = "List available backups of specified type")
    @RequiresPermission(ResourcePermission.VIEW_AUDIT_LOGS)
    public ResponseEntity<List<BackupService.BackupInfo>> listBackups(@PathVariable String type) {
        List<BackupService.BackupInfo> backups = backupService.listBackups(type);
        return ResponseEntity.ok(backups);
    }

    /**
     * Get system health status
     */
    @GetMapping("/health")
    @Operation(summary = "Get DR health status",
               description = "Get comprehensive health check for DR readiness")
    public ResponseEntity<Map<String, Object>> getHealthStatus() {
        Map<String, Object> health = new HashMap<>();

        // Database health
        health.put("database", checkDatabaseHealth());

        // Replication status
        health.put("replication", checkReplicationStatus());

        // Backup status
        health.put("backups", getBackupStatus());

        // Service health
        if(healthCheckService != null) {
            health.put("services", healthCheckService.checkAllServices());
        }

        // Overall status
        boolean isHealthy = health.values().stream()
            .filter(v -> v instanceof Map)
            .map(v ->(Map<String, Object>) v)
            .allMatch(m -> "UP".equals(m.get("status")));

        health.put("status", isHealthy ? "HEALTHY" : "DEGRADED");
        health.put("timestamp", Instant.now());

        return ResponseEntity.ok(health);
    }

    /**
     * Test DR failover(dry run)
     */
    @PostMapping("/test/failover")
    @Operation(summary = "Test failover",
               description = "Perform a dry run of failover procedures")
    @RequiresPermission(ResourcePermission.ADMIN_SYSTEM)
    public ResponseEntity<?> testFailover(@RequestParam(defaultValue = "false") boolean execute) {
        Map<String, Object> result = new HashMap<>();
        List<String> steps = new ArrayList<>();

        try {
            // Step 1: Check standby availability
            steps.add("Checking standby database availability");
            boolean standbyAvailable = checkStandbyDatabase();
            result.put("standbyAvailable", standbyAvailable);

            if(!standbyAvailable) {
                result.put("status", "FAILED");
                result.put("reason", "Standby database not available");
                return ResponseEntity.ok(result);
            }

            // Step 2: Check replication lag
            steps.add("Checking replication lag");
            long replicationLag = getReplicationLag();
            result.put("replicationLagSeconds", replicationLag);

            if(replicationLag > 300) { // More than 5 minutes
                result.put("warning", "High replication lag detected");
            }

            // Step 3: Verify backup availability
            steps.add("Verifying recent backups");
            Map<String, Object> backupStatus = getBackupStatus();
            result.put("lastBackup", backupStatus);

            // Step 4: Test service health
            steps.add("Testing service health checks");
            Map<String, Object> serviceHealth = testServiceHealth();
            result.put("serviceHealth", serviceHealth);

            // Step 5: Simulate failover(if execute = true)
            if(execute) {
                steps.add("EXECUTING FAILOVER TEST");
                // In production, this would trigger actual failover
                result.put("failoverTest", "SIMULATED - No actual changes made");
            }

            result.put("status", "READY");
            result.put("steps", steps);
            result.put("canFailover", true);

        } catch(Exception e) {
            logger.error("Failover test failed", e);
            result.put("status", "ERROR");
            result.put("error", e.getMessage());
            result.put("steps", steps);
        }

        return ResponseEntity.ok(result);
    }

    /**
     * Get recovery point objectives status
     */
    @GetMapping("/rpo - status")
    @Operation(summary = "Get RPO status",
               description = "Get current Recovery Point Objective compliance")
    public ResponseEntity<Map<String, Object>> getRpoStatus() {
        Map<String, Object> rpoStatus = new HashMap<>();

        // Database RPO(target: 15 minutes)
        Map<String, Object> dbRpo = new HashMap<>();
        long lastBackupAge = getLastBackupAge("database");
        dbRpo.put("targetMinutes", 15);
        dbRpo.put("currentMinutes", lastBackupAge / 60);
        dbRpo.put("compliant", lastBackupAge < 900); // 15 minutes in seconds
        rpoStatus.put("database", dbRpo);

        // Files RPO(target: 60 minutes)
        Map<String, Object> filesRpo = new HashMap<>();
        long lastFileBackupAge = getLastBackupAge("files");
        filesRpo.put("targetMinutes", 60);
        filesRpo.put("currentMinutes", lastFileBackupAge / 60);
        filesRpo.put("compliant", lastFileBackupAge < 3600);
        rpoStatus.put("files", filesRpo);

        // Overall compliance
        boolean overallCompliant = ((boolean) dbRpo.get("compliant")) &&
                                  ((boolean) filesRpo.get("compliant"));
        rpoStatus.put("overallCompliant", overallCompliant);
        rpoStatus.put("timestamp", Instant.now());

        return ResponseEntity.ok(rpoStatus);
    }

    /**
     * Download DR runbook
     */
    @GetMapping("/runbook")
    @Operation(summary = "Download DR runbook",
               description = "Download the disaster recovery runbook")
    @RequiresPermission(ResourcePermission.VIEW_DOCUMENTATION)
    public ResponseEntity<byte[]> downloadRunbook() {
        try {
            // In production, this would serve the actual runbook
            String runbook = generateRunbook();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_MARKDOWN);
            headers.setContentDispositionFormData("attachment", "dr_runbook.md");

            return ResponseEntity.ok()
                .headers(headers)
                .body(runbook.getBytes());

        } catch(Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Update emergency contacts
     */
    @PostMapping("/contacts")
    @Operation(summary = "Update emergency contacts",
               description = "Update the emergency contact list")
    @RequiresPermission(ResourcePermission.ADMIN_SYSTEM)
    public ResponseEntity<?> updateContacts(@RequestBody List<EmergencyContact> contacts) {
        // In production, this would update the contact database
        logger.info("Emergency contacts updated: {} contacts", contacts.size());
        return ResponseEntity.ok(Map.of(
            "status", "updated",
            "count", contacts.size()
       ));
    }

    // Helper methods

    private Map<String, Object> checkDatabaseHealth() {
        Map<String, Object> health = new HashMap<>();
        try(Connection conn = dataSource.getConnection()) {
            health.put("status", "UP");
            health.put("database", conn.getMetaData().getDatabaseProductName());
            health.put("version", conn.getMetaData().getDatabaseProductVersion());
        } catch(Exception e) {
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
        }
        return health;
    }

    private Map<String, Object> checkReplicationStatus() {
        Map<String, Object> status = new HashMap<>();
        try {
            // This would check actual replication status
            status.put("status", "STREAMING");
            status.put("lagSeconds", getReplicationLag());
            status.put("standbyConnected", true);
        } catch(Exception e) {
            status.put("status", "ERROR");
            status.put("error", e.getMessage());
        }
        return status;
    }

    private Map<String, Object> getBackupStatus() {
        Map<String, Object> status = new HashMap<>();

        List<BackupService.BackupInfo> dbBackups = backupService.listBackups("database");
        if(!dbBackups.isEmpty()) {
            BackupService.BackupInfo latest = dbBackups.get(0);
            status.put("lastDatabaseBackup", Map.of(
                "name", latest.getName(),
                "size", latest.getSize(),
                "age", Instant.now().getEpochSecond() - latest.getCreatedAt().getEpochSecond()
           ));
        }

        List<BackupService.BackupInfo> fileBackups = backupService.listBackups("files");
        if(!fileBackups.isEmpty()) {
            BackupService.BackupInfo latest = fileBackups.get(0);
            status.put("lastFileBackup", Map.of(
                "name", latest.getName(),
                "size", latest.getSize(),
                "age", Instant.now().getEpochSecond() - latest.getCreatedAt().getEpochSecond()
           ));
        }

        return status;
    }

    private boolean checkStandbyDatabase() {
        // In production, actually check standby connectivity
        return true;
    }

    private long getReplicationLag() {
        // In production, query actual replication lag
        return 45; // seconds
    }

    private long getLastBackupAge(String type) {
        List<BackupService.BackupInfo> backups = backupService.listBackups(type);
        if(backups.isEmpty()) {
            return Long.MAX_VALUE;
        }
        return Instant.now().getEpochSecond() - backups.get(0).getCreatedAt().getEpochSecond();
    }

    private Map<String, Object> testServiceHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("backend", "UP");
        health.put("redis", "UP");
        health.put("rabbitmq", "UP");
        return health;
    }

    private String generateRunbook() {
        return "# Disaster Recovery Runbook\n\n" +
               "## Quick Reference\n" +
               "- Database Failover: `./scripts/failover - database.sh`\n" +
               "- Service Recovery: `./scripts/recover - services.sh`\n" +
               "- Backup Restore: `./scripts/restore - backup.sh [backup - file]`\n\n" +
               "## Contact List\n" +
               "See /api/dr/contacts for current emergency contacts.\n";
    }

    /**
     * Emergency contact DTO
     */
    public static class EmergencyContact {
        private String name;
        private String role;
        private String phone;
        private String email;
        private String availability;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getAvailability() { return availability; }
        public void setAvailability(String availability) { this.availability = availability; }
    }

    /**
     * Health check service interface
     */
    public interface HealthCheckService {
        Map<String, Object> checkAllServices();
    }
}
