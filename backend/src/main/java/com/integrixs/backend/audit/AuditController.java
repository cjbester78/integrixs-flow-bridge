package com.integrixs.backend.audit;

import com.integrixs.backend.security.RequiresPermission;
import com.integrixs.backend.security.ResourcePermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for audit log management
 */
@RestController
@RequestMapping("/api/audit")
@Tag(name = "Audit", description = "Audit log management API")
public class AuditController {

    @Autowired
    private AuditEventRepository auditRepository;

    @Autowired
    private AuditReportService auditReportService;

    /**
     * Search audit events
     */
    @GetMapping("/events")
    @Operation(summary = "Search audit events",
               description = "Search audit events with flexible filtering")
    @ApiResponses( {
        @ApiResponse(responseCode = "200", description = "Events retrieved"),
        @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    })
    @RequiresPermission(ResourcePermission.VIEW_AUDIT_LOGS)
    public Page<AuditEventDTO> searchEvents(
            @Parameter(description = "Username filter")
            @RequestParam(required = false) String username,

            @Parameter(description = "Event type filter")
            @RequestParam(required = false) AuditEvent.AuditEventType eventType,

            @Parameter(description = "Category filter")
            @RequestParam(required = false) AuditEvent.AuditCategory category,

            @Parameter(description = "Outcome filter")
            @RequestParam(required = false) AuditEvent.AuditOutcome outcome,

            @Parameter(description = "Entity type filter")
            @RequestParam(required = false) String entityType,

            @Parameter(description = "Start time filter")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startTime,

            @Parameter(description = "End time filter")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endTime,

            @PageableDefault(sort = "eventTimestamp", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<AuditEvent> events = auditRepository.searchAuditEvents(
            username, eventType, category, outcome, entityType,
            null, // tenantId handled by repository
            startTime, endTime, pageable
       );

        return events.map(this::toDTO);
    }

    /**
     * Get audit events for specific entity
     */
    @GetMapping("/events/entity/ {entityType}/ {entityId}")
    @Operation(summary = "Get entity audit trail",
               description = "Get complete audit trail for a specific entity")
    @RequiresPermission(ResourcePermission.VIEW_AUDIT_LOGS)
    public Page<AuditEventDTO> getEntityAuditTrail(
            @PathVariable String entityType,
            @PathVariable String entityId,
            @PageableDefault(sort = "eventTimestamp", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<AuditEvent> events = auditRepository.findByEntityTypeAndEntityIdOrderByEventTimestampDesc(
            entityType, entityId, pageable
       );

        return events.map(this::toDTO);
    }

    /**
     * Get security events
     */
    @GetMapping("/events/security")
    @Operation(summary = "Get security events",
               description = "Get security - related audit events")
    @RequiresPermission(ResourcePermission.VIEW_SECURITY_LOGS)
    public Page<AuditEventDTO> getSecurityEvents(
            @PageableDefault(sort = "eventTimestamp", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<AuditEvent> events = auditRepository.findSecurityEvents(pageable);
        return events.map(this::toDTO);
    }

    /**
     * Get events by correlation ID
     */
    @GetMapping("/events/correlation/ {correlationId}")
    @Operation(summary = "Get correlated events",
               description = "Get all events related to a correlation ID")
    @RequiresPermission(ResourcePermission.VIEW_AUDIT_LOGS)
    public List<AuditEventDTO> getCorrelatedEvents(@PathVariable String correlationId) {
        List<AuditEvent> events = auditRepository.findByCorrelationIdOrderByEventTimestamp(correlationId);
        return events.stream().map(this::toDTO).collect(Collectors.toList());
    }

    /**
     * Get audit statistics
     */
    @GetMapping("/statistics")
    @Operation(summary = "Get audit statistics",
               description = "Get statistical summary of audit events")
    @RequiresPermission(ResourcePermission.VIEW_AUDIT_LOGS)
    public ResponseEntity<Map<String, Object>> getStatistics(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startTime,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endTime) {

        if(startTime == null) {
            startTime = Instant.now().minusSeconds(86400); // Last 24 hours
        }
        if(endTime == null) {
            endTime = Instant.now();
        }

        Map<String, Object> stats = auditReportService.generateStatistics(startTime, endTime);
        return ResponseEntity.ok(stats);
    }

    /**
     * Export audit events
     */
    @GetMapping(value = "/export", produces = "text/csv")
    @Operation(summary = "Export audit events",
               description = "Export audit events as CSV")
    @RequiresPermission(ResourcePermission.EXPORT_AUDIT_LOGS)
    public ResponseEntity<byte[]> exportEvents(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) AuditEvent.AuditEventType eventType,
            @RequestParam(required = false) AuditEvent.AuditCategory category,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startTime,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endTime) {

        try {
            byte[] csvData = auditReportService.exportToCsv(
                username, eventType, category, startTime, endTime
           );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment",
                String.format("audit_export_%s.csv",
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));

            return ResponseEntity.ok()
                .headers(headers)
                .body(csvData);

        } catch(Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Generate compliance report
     */
    @GetMapping("/reports/compliance")
    @Operation(summary = "Generate compliance report",
               description = "Generate audit compliance report")
    @RequiresPermission(ResourcePermission.GENERATE_REPORTS)
    public ResponseEntity<Map<String, Object>> generateComplianceReport(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startTime,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endTime) {

        Map<String, Object> report = auditReportService.generateComplianceReport(startTime, endTime);
        return ResponseEntity.ok(report);
    }

    /**
     * Cleanup old audit events
     */
    @DeleteMapping("/cleanup")
    @Operation(summary = "Cleanup old audit events",
               description = "Delete audit events older than specified days")
    @RequiresPermission(ResourcePermission.ADMIN_SYSTEM)
    public ResponseEntity<Map<String, Object>> cleanupOldEvents(
            @Parameter(description = "Number of days to retain")
            @RequestParam(defaultValue = "90") int retentionDays) {

        Instant cutoffTime = Instant.now().minusSeconds(retentionDays * 86400L);

        // Count events to be deleted
        long countBefore = auditRepository.count();

        // Delete old events
        auditRepository.deleteByEventTimestampBefore(cutoffTime);

        long countAfter = auditRepository.count();
        long deletedCount = countBefore - countAfter;

        Map<String, Object> result = new HashMap<>();
        result.put("deletedCount", deletedCount);
        result.put("remainingCount", countAfter);
        result.put("cutoffTime", cutoffTime);

        return ResponseEntity.ok(result);
    }

    /**
     * Convert entity to DTO
     */
    private AuditEventDTO toDTO(AuditEvent event) {
        return AuditEventDTO.builder()
            .id(event.getId())
            .eventTimestamp(event.getEventTimestamp())
            .eventType(event.getEventType())
            .category(event.getCategory())
            .username(event.getUsername())
            .ipAddress(event.getIpAddress())
            .entityType(event.getEntityType())
            .entityId(event.getEntityId())
            .entityName(event.getEntityName())
            .action(event.getAction())
            .outcome(event.getOutcome())
            .errorMessage(event.getErrorMessage())
            .durationMs(event.getDurationMs())
            .details(event.getDetails())
            .build();
    }

    /**
     * Audit event DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class AuditEventDTO {
        private UUID id;
        private Instant eventTimestamp;
        private AuditEvent.AuditEventType eventType;
        private AuditEvent.AuditCategory category;
        private String username;
        private String ipAddress;
        private String entityType;
        private String entityId;
        private String entityName;
        private String action;
        private AuditEvent.AuditOutcome outcome;
        private String errorMessage;
        private Long durationMs;
        private Map<String, String> details;
    }
}
