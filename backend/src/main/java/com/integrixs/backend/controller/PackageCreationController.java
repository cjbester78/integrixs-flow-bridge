package com.integrixs.backend.controller;

import com.integrixs.backend.dto.PackageCreationRequest;
import com.integrixs.backend.dto.PackageCreationResult;
import com.integrixs.backend.service.TransactionalPackageCreationService;
import com.integrixs.backend.jobs.BackgroundJob;
import com.integrixs.backend.jobs.JobExecutionService;
import com.integrixs.backend.jobs.executors.PackageCreationJobExecutor;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Controller for transactional package creation
 */
@RestController
@RequestMapping("/api/packages")
@Tag(name = "Package Creation", description = "Transactional package creation API")
@Validated
public class PackageCreationController {

    private static final Logger logger = LoggerFactory.getLogger(PackageCreationController.class);

    @Autowired
    private TransactionalPackageCreationService packageCreationService;

    @Autowired
    private JobExecutionService jobExecutionService;

    @Autowired
    private ObjectMapper objectMapper;

    // In - memory storage for package creation status(in production, use Redis or database)
    private final Map<UUID, PackageCreationResult> creationStatus = new ConcurrentHashMap<>();

    /**
     * Create a new integration package
     */
    @PostMapping("/create")
    @Operation(summary = "Create integration package",
               description = "Creates a complete integration package with all components in a single transaction")
    @ApiResponses( {
        @ApiResponse(responseCode = "201", description = "Package created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "500", description = "Package creation failed")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public ResponseEntity<?> createPackage(@Valid @RequestBody PackageCreationRequest request) {
        try {
            logger.info("Creating integration package: {}", request.getFlowName());

            // Set user ID from security context
            // In a real implementation, get from SecurityContextHolder
            if(request.getUserId() == null) {
                request.setUserId(UUID.randomUUID()); // Placeholder
            }

            // Create package
            PackageCreationResult result = packageCreationService.createPackage(request);

            // Store status for polling
            creationStatus.put(result.getCorrelationId(), result);

            // Return result
            if(result.isSuccess()) {
                return ResponseEntity.status(HttpStatus.CREATED).body(result);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
            }

        } catch(TransactionalPackageCreationService.PackageCreationException e) {
            logger.error("Package creation failed", e);
            PackageCreationResult result = PackageCreationResult.failure(
                e.getMessage(), e.getContext()
           );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        } catch(Exception e) {
            logger.error("Unexpected error during package creation", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Package creation failed");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Create a new integration package asynchronously
     */
    @PostMapping("/create - async")
    @Operation(summary = "Create integration package asynchronously",
               description = "Creates a complete integration package as a background job")
    @ApiResponses( {
        @ApiResponse(responseCode = "202", description = "Package creation job accepted"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "500", description = "Job submission failed")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public ResponseEntity<?> createPackageAsync(@Valid @RequestBody PackageCreationRequest request) {
        try {
            logger.info("Creating integration package asynchronously: {}", request.getFlowName());

            // Set user ID from security context
            if(request.getUserId() == null) {
                request.setUserId(UUID.randomUUID()); // Placeholder
            }

            // Create job parameters
            Map<String, String> parameters = PackageCreationJobExecutor.createJobParameters(request, objectMapper);

            // Submit job
            BackgroundJob job = jobExecutionService.submitJob(
                PackageCreationJobExecutor.JOB_TYPE,
                parameters,
                request.getUserId(),
                request.getTenantId()
           );

            // Return job information
            Map<String, Object> response = new HashMap<>();
            response.put("jobId", job.getId());
            response.put("status", job.getStatus());
            response.put("message", "Package creation job submitted successfully");
            response.put("estimatedDuration", 60000); // 1 minute estimate

            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);

        } catch(Exception e) {
            logger.error("Failed to submit package creation job", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to submit job");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Create package with manual compensation(for testing rollback)
     */
    @PostMapping("/create - with - compensation")
    @Operation(summary = "Create package with compensation",
               description = "Creates a package with manual compensation for testing rollback scenarios")
    @ApiResponses( {
        @ApiResponse(responseCode = "201", description = "Package created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "500", description = "Package creation failed but compensated")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public ResponseEntity<?> createPackageWithCompensation(@Valid @RequestBody PackageCreationRequest request) {
        try {
            logger.info("Creating integration package with compensation: {}", request.getFlowName());

            // Set user ID from security context
            if(request.getUserId() == null) {
                request.setUserId(UUID.randomUUID()); // Placeholder
            }

            // Create package with compensation
            PackageCreationResult result = packageCreationService.createPackageWithCompensation(request);

            // Store status
            creationStatus.put(result.getCorrelationId(), result);

            // Return result
            if(result.isSuccess()) {
                return ResponseEntity.status(HttpStatus.CREATED).body(result);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
            }

        } catch(Exception e) {
            logger.error("Unexpected error during package creation", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Package creation failed");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Get package creation status
     */
    @GetMapping("/status/ {correlationId}")
    @Operation(summary = "Get package creation status",
               description = "Gets the current status of a package creation operation")
    @ApiResponses( {
        @ApiResponse(responseCode = "200", description = "Status retrieved"),
        @ApiResponse(responseCode = "404", description = "Status not found")
    })
    public ResponseEntity<?> getStatus(
            @Parameter(description = "Correlation ID of the package creation operation")
            @PathVariable UUID correlationId) {

        PackageCreationResult result = creationStatus.get(correlationId);
        if(result != null) {
            return ResponseEntity.ok(result);
        } else {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Status not found");
            error.put("correlationId", correlationId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    /**
     * Validate package creation request
     */
    @PostMapping("/validate")
    @Operation(summary = "Validate package request",
               description = "Validates a package creation request without creating the package")
    @ApiResponses( {
        @ApiResponse(responseCode = "200", description = "Request is valid"),
        @ApiResponse(responseCode = "400", description = "Request is invalid")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public ResponseEntity<?> validatePackage(@Valid @RequestBody PackageCreationRequest request) {
        try {
            // Perform validation checks
            Map<String, Object> validation = new HashMap<>();
            validation.put("valid", true);
            validation.put("flowName", request.getFlowName());

            // Check source adapter
            if(request.getSourceAdapter() == null &&
                (request.getTargetAdapters() == null || request.getTargetAdapters().isEmpty())) {
                validation.put("valid", false);
                validation.put("error", "At least one adapter must be configured");
                return ResponseEntity.badRequest().body(validation);
            }

            // Check transformations
            if(request.getTransformations() == null || request.getTransformations().isEmpty()) {
                validation.put("warning", "No transformations configured");
            }

            // Check structures
            if(request.getSourceStructure() == null && request.getTargetStructures().isEmpty()) {
                validation.put("warning", "No message structures defined");
            }

            validation.put("message", "Package request is valid");
            return ResponseEntity.ok(validation);

        } catch(Exception e) {
            logger.error("Error validating package request", e);
            Map<String, Object> error = new HashMap<>();
            error.put("valid", false);
            error.put("error", "Validation failed");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Get package creation statistics
     */
    @GetMapping("/statistics")
    @Operation(summary = "Get package creation statistics",
               description = "Gets statistics about package creation operations")
    @ApiResponses( {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved")
    })
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<?> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        int total = creationStatus.size();
        long successful = creationStatus.values().stream()
            .filter(PackageCreationResult::isSuccess)
            .count();
        long failed = total - successful;

        double avgDuration = creationStatus.values().stream()
            .filter(r -> r.getDurationMillis() != null)
            .mapToLong(PackageCreationResult::getDurationMillis)
            .average()
            .orElse(0.0);

        stats.put("totalOperations", total);
        stats.put("successfulOperations", successful);
        stats.put("failedOperations", failed);
        stats.put("successRate", total > 0 ? (successful * 100.0 / total) : 0.0);
        stats.put("averageDurationMs", avgDuration);

        return ResponseEntity.ok(stats);
    }
}
