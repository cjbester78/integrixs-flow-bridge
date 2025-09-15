package com.integrixs.backend.repository;

import com.integrixs.backend.jobs.BackgroundJob;
import com.integrixs.backend.jobs.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for background jobs
 */
@Repository
public interface BackgroundJobRepository extends JpaRepository<BackgroundJob, UUID>, JpaSpecificationExecutor<BackgroundJob> {

    /**
     * Find jobs by status
     */
    List<BackgroundJob> findByStatus(JobStatus status);

    /**
     * Find jobs by status with pagination
     */
    Page<BackgroundJob> findByStatus(JobStatus status, Pageable pageable);

    /**
     * Find jobs by tenant
     */
    Page<BackgroundJob> findByTenantId(UUID tenantId, Pageable pageable);

    /**
     * Find jobs by user
     */
    Page<BackgroundJob> findByCreatedBy(UUID userId, Pageable pageable);

    /**
     * Find jobs by type and status
     */
    List<BackgroundJob> findByJobTypeAndStatus(String jobType, JobStatus status);

    /**
     * Find pending jobs ordered by priority and creation time
     */
    @Query("SELECT j FROM BackgroundJob j WHERE j.status = 'PENDING' " +
           "AND(j.scheduledAt IS NULL OR j.scheduledAt <= :now) " +
           "ORDER BY j.priority ASC, j.createdAt ASC")
    List<BackgroundJob> findPendingJobs(@Param("now") LocalDateTime now, Pageable pageable);

    /**
     * Find stuck jobs(running for too long)
     */
    @Query("SELECT j FROM BackgroundJob j WHERE j.status = 'RUNNING' " +
           "AND j.startedAt < :cutoffTime")
    List<BackgroundJob> findStuckJobs(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Update job status atomically
     */
    @Modifying
    @Query("UPDATE BackgroundJob j SET j.status = :newStatus, j.version = j.version + 1 " +
           "WHERE j.id = :jobId AND j.status = :expectedStatus")
    int updateJobStatus(@Param("jobId") UUID jobId,
                       @Param("expectedStatus") JobStatus expectedStatus,
                       @Param("newStatus") JobStatus newStatus);

    /**
     * Update job progress
     */
    @Modifying
    @Query("UPDATE BackgroundJob j SET j.progress = :progress, j.currentStep = :currentStep, " +
           "j.version = j.version + 1 WHERE j.id = :jobId")
    int updateJobProgress(@Param("jobId") UUID jobId,
                         @Param("progress") Integer progress,
                         @Param("currentStep") String currentStep);

    /**
     * Clean up old completed jobs
     */
    @Modifying
    @Query("DELETE FROM BackgroundJob j WHERE j.status IN('COMPLETED', 'FAILED', 'CANCELLED') " +
           "AND j.completedAt < :cutoffTime")
    int cleanupOldJobs(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Count jobs by status and tenant
     */
    @Query("SELECT COUNT(j) FROM BackgroundJob j WHERE j.tenantId = :tenantId AND j.status = :status")
    long countByTenantIdAndStatus(@Param("tenantId") UUID tenantId, @Param("status") JobStatus status);

    /**
     * Get job statistics by type
     */
    @Query("SELECT j.jobType as type, j.status as status, COUNT(j) as count " +
           "FROM BackgroundJob j GROUP BY j.jobType, j.status")
    List<JobStatistics> getJobStatistics();

    /**
     * Lock job for processing(pessimistic locking)
     */
    @Query("SELECT j FROM BackgroundJob j WHERE j.id = :jobId")
    @javax.persistence.LockModeType(javax.persistence.LockModeType.PESSIMISTIC_WRITE)
    Optional<BackgroundJob> findByIdForUpdate(@Param("jobId") UUID jobId);

    /**
     * Interface for job statistics projection
     */
    interface JobStatistics {
        String getType();
        JobStatus getStatus();
        Long getCount();
    }
}
