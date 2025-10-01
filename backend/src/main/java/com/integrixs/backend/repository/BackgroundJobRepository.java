package com.integrixs.backend.repository;

import com.integrixs.backend.jobs.BackgroundJob;
import com.integrixs.backend.jobs.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for BackgroundJob entities
 */
@Repository
public interface BackgroundJobRepository {

    BackgroundJob save(BackgroundJob job);

    Optional<BackgroundJob> findById(UUID id);

    Page<BackgroundJob> findAll(Pageable pageable);

    Page<BackgroundJob> findByStatus(JobStatus status, Pageable pageable);

    Page<BackgroundJob> findByCreatedBy(String createdBy, Pageable pageable);

    Page<BackgroundJob> findByJobType(String jobType, Pageable pageable);

    List<BackgroundJob> findByStatusAndScheduledAtBefore(JobStatus status, LocalDateTime dateTime);

    List<BackgroundJob> findByStatusInAndStartedAtBefore(List<JobStatus> statuses, LocalDateTime dateTime);

    List<BackgroundJob> findByRecurringTrue();

    Optional<BackgroundJob> findFirstByStatusOrderByScheduledAtAsc(JobStatus status);

    void deleteById(UUID id);

    boolean existsById(UUID id);

    long countByStatus(JobStatus status);

    long count();

    Page<BackgroundJob> findPendingJobs(LocalDateTime beforeTime, Pageable pageable);

    int updateJobStatus(UUID jobId, JobStatus fromStatus, JobStatus toStatus);

    Page<BackgroundJob> findByStatusInAndCompletedAtBefore(List<JobStatus> statuses, LocalDateTime dateTime, Pageable pageable);

    int updateJobProgress(UUID jobId, int progress, String currentStep);

    List<BackgroundJob> findStuckJobs(LocalDateTime stuckThreshold);

    int cleanupOldJobs(LocalDateTime cutoffDate);
}