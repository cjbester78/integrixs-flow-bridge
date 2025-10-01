package com.integrixs.backend.controller;

import com.integrixs.backend.jobs.BackgroundJob;
import com.integrixs.backend.jobs.JobExecutionService;
import com.integrixs.backend.jobs.JobStatus;
import com.integrixs.backend.repository.BackgroundJobRepository;
import com.integrixs.backend.security.RequiresPermission;
import com.integrixs.backend.security.ResourcePermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Controller for background job management
 */
@RestController
@RequestMapping("/api/jobs")
@Tag(name = "Background Jobs", description = "Background job management API")
public class BackgroundJobController {

    private static final Logger logger = LoggerFactory.getLogger(BackgroundJobController.class);

    @Autowired
    private JobExecutionService jobExecutionService;

    @Autowired
    private BackgroundJobRepository jobRepository;

    /**
     * Get job by ID
     */
    @GetMapping("/ {jobId}")
    @Operation(summary = "Get job details", description = "Gets details of a specific background job")
    public ResponseEntity<?> getJob(@PathVariable UUID jobId) {
        return jobExecutionService.getJob(jobId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * List jobs with pagination
     */
    @GetMapping
    @Operation(summary = "List jobs", description = "Lists background jobs with pagination and filtering")
    @RequiresPermission(ResourcePermission.MONITOR_FLOWS)
    public Page<BackgroundJob> listJobs(
            @RequestParam(required = false) JobStatus status,
            @RequestParam(required = false) String jobType,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        if(status != null && jobType != null) {
            // For combined filter, we need to fetch all and filter in memory
            // In production, this should be a custom SQL query
            Page<BackgroundJob> allJobs = jobRepository.findByStatus(status, pageable);
            List<BackgroundJob> filtered = allJobs.getContent().stream()
                .filter(job -> jobType.equals(job.getJobType()))
                .toList();
            return new PageImpl<>(filtered, pageable, filtered.size());
        } else if(status != null) {
            return jobRepository.findByStatus(status, pageable);
        } else if(jobType != null) {
            return jobRepository.findByJobType(jobType, pageable);
        } else {
            return jobRepository.findAll(pageable);
        }
    }

    /**
     * Get job statistics
     */
    @GetMapping("/statistics")
    @Operation(summary = "Get job statistics", description = "Gets statistics about background jobs")
    @RequiresPermission(ResourcePermission.MONITOR_METRICS)
    public ResponseEntity<Map<String, Object>> getStatistics() {
        return ResponseEntity.ok(jobExecutionService.getJobStatistics());
    }

    /**
     * Cancel a job
     */
    @PostMapping("/ {jobId}/cancel")
    @Operation(summary = "Cancel job", description = "Cancels a pending or running job")
    public ResponseEntity<?> cancelJob(@PathVariable UUID jobId) {
        boolean cancelled = jobExecutionService.cancelJob(jobId);
        if(cancelled) {
            return ResponseEntity.ok(Map.of("message", "Job cancelled successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Job cannot be cancelled"));
        }
    }

    /**
     * Retry a failed job
     */
    @PostMapping("/ {jobId}/retry")
    @Operation(summary = "Retry job", description = "Retries a failed job")
    public ResponseEntity<?> retryJob(@PathVariable UUID jobId) {
        return jobExecutionService.getJob(jobId)
            .map(job -> {
                if(job.getStatus() != JobStatus.FAILED) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Only failed jobs can be retried"));
                }

                // Reset job status for retry
                job.setStatus(JobStatus.PENDING);
                job.setErrorMessage(null);
                job.setStackTrace(null);
                jobRepository.save(job);

                // Trigger processing
                jobExecutionService.processNextJobs();

                return ResponseEntity.ok(Map.of("message", "Job scheduled for retry"));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Delete completed jobs
     */
    @DeleteMapping("/cleanup")
    @Operation(summary = "Cleanup jobs", description = "Deletes old completed jobs")
    @RequiresPermission(ResourcePermission.ADMIN_SYSTEM)
    public ResponseEntity<?> cleanupJobs(
            @RequestParam(defaultValue = "30") int olderThanDays) {

        jobExecutionService.cleanupOldJobs();
        return ResponseEntity.ok(Map.of("message", "Cleanup initiated"));
    }
}
