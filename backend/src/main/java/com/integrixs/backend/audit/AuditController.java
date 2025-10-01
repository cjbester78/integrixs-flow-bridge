package com.integrixs.backend.audit;

import com.integrixs.backend.security.RequiresPermission;
import com.integrixs.backend.security.ResourcePermission;
import com.integrixs.data.model.AuditEvent;
import com.integrixs.data.model.AuditTrail;
import com.integrixs.data.sql.repository.AuditEventRepository;
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

        LocalDateTime startDateTime = startTime != null ?
            LocalDateTime.ofInstant(startTime, ZoneId.systemDefault()) : null;
        LocalDateTime endDateTime = endTime != null ?
            LocalDateTime.ofInstant(endTime, ZoneId.systemDefault()) : null;

        Page<AuditEvent> events = auditRepository.searchEvents(
            username, eventType, category, outcome, entityType,
            startDateTime, endDateTime, pageable
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

        // Get all events for the entity and manually paginate
        List<AuditEvent> allEvents = auditRepository.findByEntityTypeAndEntityId(entityType, entityId);

        // Sort by timestamp descending
        allEvents.sort((e1, e2) -> {
            LocalDateTime t1 = e1.getEventTimestamp();
            LocalDateTime t2 = e2.getEventTimestamp();
            if (t1 == null && t2 == null) return 0;
            if (t1 == null) return 1;
            if (t2 == null) return -1;
            return t2.compareTo(t1);
        });

        // Manual pagination
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allEvents.size());
        List<AuditEvent> pageContent = start < allEvents.size() ?
            allEvents.subList(start, end) : new ArrayList<>();

        Page<AuditEvent> events = new org.springframework.data.domain.PageImpl<>(
            pageContent, pageable, allEvents.size());

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

        // Search for security-related event types
        Page<AuditEvent> events = auditRepository.searchEvents(
            null, null, AuditEvent.AuditCategory.AUTHENTICATION, null, null,
            null, null, pageable
        );
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
        LocalDateTime cutoffDateTime = LocalDateTime.ofInstant(cutoffTime, ZoneId.systemDefault());
        auditRepository.deleteByEventTimestampBefore(cutoffDateTime);

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
        LocalDateTime timestamp = event.getEventTimestamp();
        Instant instantTimestamp = timestamp != null ?
            timestamp.atZone(ZoneId.systemDefault()).toInstant() : null;

        return AuditEventDTO.builder()
            .id(event.getId())
            .eventTimestamp(instantTimestamp)
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
            .details(parseDetailsJson(event.getDetails()))
            .build();
    }

    /**
     * Parse details JSON string to Map
     */
    private Map<String, String> parseDetailsJson(String detailsJson) {
        if (detailsJson == null || detailsJson.isEmpty()) {
            return null;
        }
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.core.type.TypeReference<Map<String, String>> typeRef =
                new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>() {};
            return mapper.readValue(detailsJson, typeRef);
        } catch (Exception e) {
            // If parsing fails, return null
            return null;
        }
    }

    /**
     * Audit event DTO
     */
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

        // Private constructor for builder
        private AuditEventDTO() {}

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
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

            public Builder id(UUID id) {
                this.id = id;
                return this;
            }

            public Builder eventTimestamp(Instant eventTimestamp) {
                this.eventTimestamp = eventTimestamp;
                return this;
            }

            public Builder eventType(AuditEvent.AuditEventType eventType) {
                this.eventType = eventType;
                return this;
            }

            public Builder category(AuditEvent.AuditCategory category) {
                this.category = category;
                return this;
            }

            public Builder username(String username) {
                this.username = username;
                return this;
            }

            public Builder ipAddress(String ipAddress) {
                this.ipAddress = ipAddress;
                return this;
            }

            public Builder entityType(String entityType) {
                this.entityType = entityType;
                return this;
            }

            public Builder entityId(String entityId) {
                this.entityId = entityId;
                return this;
            }

            public Builder entityName(String entityName) {
                this.entityName = entityName;
                return this;
            }

            public Builder action(String action) {
                this.action = action;
                return this;
            }

            public Builder outcome(AuditEvent.AuditOutcome outcome) {
                this.outcome = outcome;
                return this;
            }

            public Builder errorMessage(String errorMessage) {
                this.errorMessage = errorMessage;
                return this;
            }

            public Builder durationMs(Long durationMs) {
                this.durationMs = durationMs;
                return this;
            }

            public Builder details(Map<String, String> details) {
                this.details = details;
                return this;
            }

            public AuditEventDTO build() {
                AuditEventDTO dto = new AuditEventDTO();
                dto.id = this.id;
                dto.eventTimestamp = this.eventTimestamp;
                dto.eventType = this.eventType;
                dto.category = this.category;
                dto.username = this.username;
                dto.ipAddress = this.ipAddress;
                dto.entityType = this.entityType;
                dto.entityId = this.entityId;
                dto.entityName = this.entityName;
                dto.action = this.action;
                dto.outcome = this.outcome;
                dto.errorMessage = this.errorMessage;
                dto.durationMs = this.durationMs;
                dto.details = this.details;
                return dto;
            }
        }

        // Getters
        public UUID getId() { return id; }
        public Instant getEventTimestamp() { return eventTimestamp; }
        public AuditEvent.AuditEventType getEventType() { return eventType; }
        public AuditEvent.AuditCategory getCategory() { return category; }
        public String getUsername() { return username; }
        public String getIpAddress() { return ipAddress; }
        public String getEntityType() { return entityType; }
        public String getEntityId() { return entityId; }
        public String getEntityName() { return entityName; }
        public String getAction() { return action; }
        public AuditEvent.AuditOutcome getOutcome() { return outcome; }
        public String getErrorMessage() { return errorMessage; }
        public Long getDurationMs() { return durationMs; }
        public Map<String, String> getDetails() { return details; }

        // Setters
        public void setId(UUID id) { this.id = id; }
        public void setEventTimestamp(Instant eventTimestamp) { this.eventTimestamp = eventTimestamp; }
        public void setEventType(AuditEvent.AuditEventType eventType) { this.eventType = eventType; }
        public void setCategory(AuditEvent.AuditCategory category) { this.category = category; }
        public void setUsername(String username) { this.username = username; }
        public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
        public void setEntityType(String entityType) { this.entityType = entityType; }
        public void setEntityId(String entityId) { this.entityId = entityId; }
        public void setEntityName(String entityName) { this.entityName = entityName; }
        public void setAction(String action) { this.action = action; }
        public void setOutcome(AuditEvent.AuditOutcome outcome) { this.outcome = outcome; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }
        public void setDetails(Map<String, String> details) { this.details = details; }
    }
}
