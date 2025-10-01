package com.integrixs.backend.jobs;

import com.integrixs.backend.repository.BackgroundJobRepository;
import com.integrixs.backend.security.TenantContext;
import com.integrixs.backend.websocket.JobProgressWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrixs.backend.jobs.BackgroundJob;
import com.integrixs.data.model.User;
import com.integrixs.backend.repository.BackgroundJobSqlRepository;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.Arrays;

/**
 * Service for executing background jobs
 */
@Service
public class JobExecutionService {

    private static final Logger logger = LoggerFactory.getLogger(JobExecutionService.class);

    @Autowired
    private BackgroundJobRepository jobRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired(required = false)
    private JobProgressWebSocketHandler webSocketHandler;

    @Value("${jobs.executor.threads:10}")
    private int executorThreads;

    @Value("${jobs.executor.queue-size:100}")
    private int queueSize;

    @Value("${jobs.executor.stuck-job-timeout:3600000}") // 1 hour
    private long stuckJobTimeout;

    @Value("${jobs.executor.cleanup-age-days:30}")
    private int cleanupAgeDays;

    private ExecutorService executorService;
    private ScheduledExecutorService scheduledExecutor;
    private Map<String, JobExecutor> jobExecutors = new ConcurrentHashMap<>();
    private Map<UUID, Future<?>> runningJobs = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        // Initialize thread pool
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
            executorThreads,
            executorThreads,
            60L, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(queueSize),
            new ThreadFactory() {
                private int counter = 0;
                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r, "job-executor-" + counter++);
                    thread.setDaemon(true);
                    return thread;
                }
            },
            new ThreadPoolExecutor.CallerRunsPolicy()
       );
        executor.allowCoreThreadTimeOut(true);
        this.executorService = executor;

        // Initialize scheduled executor for maintenance tasks
        this.scheduledExecutor = Executors.newScheduledThreadPool(2);

        // Discover and register job executors
        discoverJobExecutors();
    }

    @PreDestroy
    public void shutdown() {
        logger.info("Shutting down job execution service");
        executorService.shutdown();
        scheduledExecutor.shutdown();
        try {
            if(!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
            if(!scheduledExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                scheduledExecutor.shutdownNow();
            }
        } catch(InterruptedException e) {
            executorService.shutdownNow();
            scheduledExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Submit a new job for execution
     */
    public BackgroundJob submitJob(String jobType, Map<String, String> parameters, UUID userId, UUID tenantId) {
        // Validate job executor exists
        JobExecutor executor = jobExecutors.get(jobType);
        if(executor == null) {
            throw new IllegalArgumentException("No executor found for job type: " + jobType);
        }

        // Validate parameters
        executor.validateParameters(parameters);

        // Create job entity
        BackgroundJob job = new BackgroundJob();
        job.setJobType(jobType);
        job.setParameters(convertMapToJson(parameters));
        if (userId != null) {
            User user = new User();
            user.setId(userId);
            job.setCreatedBy(user);
        }
        job.setTenantId(tenantId);
        job.setStatus(JobStatus.PENDING);

        // Save job
        job = jobRepository.save(job);

        logger.info("Submitted job {} of type {}", job.getId(), jobType);

        // Trigger job processing
        processNextJobs();

        return job;
    }

    /**
     * Submit a scheduled job
     */
    public BackgroundJob submitScheduledJob(String jobType, Map<String, String> parameters,
                                          LocalDateTime scheduledAt, UUID userId, UUID tenantId) {
        BackgroundJob job = submitJob(jobType, parameters, userId, tenantId);
        job.setScheduledAt(scheduledAt);
        return jobRepository.save(job);
    }

    /**
     * Cancel a job
     */
    public boolean cancelJob(UUID jobId) {
        Optional<BackgroundJob> jobOpt = jobRepository.findById(jobId);
        if(!jobOpt.isPresent()) {
            return false;
        }

        BackgroundJob job = jobOpt.get();

        // Check if job can be cancelled
        if(job.getStatus().isTerminal()) {
            return false;
        }

        // If running, try to interrupt
        if(job.getStatus() == JobStatus.RUNNING) {
            Future<?> future = runningJobs.get(jobId);
            if(future != null) {
                future.cancel(true);
            }
        }

        // Update status
        job.setStatus(JobStatus.CANCELLED);
        job.setCompletedAt(LocalDateTime.now());
        jobRepository.save(job);

        // Notify via WebSocket
        notifyJobUpdate(job);

        return true;
    }

    /**
     * Get job status
     */
    public Optional<BackgroundJob> getJob(UUID jobId) {
        return jobRepository.findById(jobId);
    }

    /**
     * Process pending jobs
     */
    @Scheduled(fixedDelay = 5000) // Run every 5 seconds
    public void processNextJobs() {
        try {
            // Find pending jobs
            Page<BackgroundJob> pendingJobsPage = jobRepository.findPendingJobs(
                LocalDateTime.now(),
                PageRequest.of(0, executorThreads * 2)
           );
            List<BackgroundJob> pendingJobs = pendingJobsPage.getContent();

            for(BackgroundJob job : pendingJobs) {
                if(runningJobs.size() >= executorThreads) {
                    break; // Thread pool is full
                }

                // Try to claim the job
                int updated = jobRepository.updateJobStatus(job.getId(), JobStatus.PENDING, JobStatus.RUNNING);
                if(updated > 0) {
                    // Successfully claimed the job
                    executeJob(job);
                }
            }
        } catch(Exception e) {
            logger.error("Error processing pending jobs", e);
        }
    }

    /**
     * Execute a job asynchronously
     */
    @Async
    public void executeJob(BackgroundJob job) {
        Future<?> future = executorService.submit(() -> {
            try {
                // Set tenant context
                if(job.getTenantId() != null) {
                    TenantContext.setCurrentTenant(job.getTenantId());
                }

                logger.info("Starting execution of job {}", job.getId());

                // Update job start time
                job.setStartedAt(LocalDateTime.now());
                job.setStatus(JobStatus.RUNNING);
                jobRepository.save(job);

                // Get executor
                JobExecutor executor = jobExecutors.get(job.getJobType());
                if(executor == null) {
                    throw new IllegalStateException("No executor found for job type: " + job.getJobType());
                }

                // Create progress callback
                JobExecutor.ProgressCallback progressCallback = (progress, step) -> {
                    job.setProgress(progress);
                    job.setCurrentStep(step);
                    jobRepository.updateJobProgress(job.getId(), progress, step);
                    notifyJobUpdate(job);
                };

                // Execute job
                Map<String, String> results = executor.execute(job, progressCallback);

                // Update job completion
                job.setStatus(JobStatus.COMPLETED);
                job.setProgress(100);
                job.setCompletedAt(LocalDateTime.now());
                job.setResults(results);
                jobRepository.save(job);

                logger.info("Job {} completed successfully", job.getId());

            } catch(Exception e) {
                logger.error("Job {} failed", job.getId(), e);
                handleJobFailure(job, e);
            } finally {
                // Clean up
                runningJobs.remove(job.getId());
                TenantContext.clear();
                notifyJobUpdate(job);
            }
        });

        runningJobs.put(job.getId(), future);
    }

    /**
     * Handle job failure
     */
    private void handleJobFailure(BackgroundJob job, Exception error) {
        try {
            job.setErrorMessage(error.getMessage());
            job.setStackTrace(getStackTrace(error));

            JobExecutor executor = jobExecutors.get(job.getJobType());

            if(executor != null && executor.isRetryable() && job.canRetry()) {
                // Schedule retry
                job.setStatus(JobStatus.RETRYING);
                job.incrementRetryCount();

                long retryDelay = executor.getRetryDelay(job.getRetryCount());
                job.setScheduledAt(LocalDateTime.now().plusNanos(retryDelay * 1_000_000));

                logger.info("Job {} will be retried in {} ms", job.getId(), retryDelay);
            } else {
                // Mark as failed
                job.setStatus(JobStatus.FAILED);
                job.setCompletedAt(LocalDateTime.now());
            }

            jobRepository.save(job);

        } catch(Exception e) {
            logger.error("Error handling job failure", e);
        }
    }

    /**
     * Clean up stuck jobs
     */
    @Scheduled(fixedDelay = 300000) // Run every 5 minutes
    public void cleanupStuckJobs() {
        try {
            LocalDateTime cutoffTime = LocalDateTime.now().minusNanos(stuckJobTimeout * 1_000_000);
            List<BackgroundJob> stuckJobs = jobRepository.findStuckJobs(cutoffTime);

            for(BackgroundJob job : stuckJobs) {
                logger.warn("Found stuck job: {}", job.getId());
                job.setStatus(JobStatus.FAILED);
                job.setErrorMessage("Job execution timed out");
                job.setCompletedAt(LocalDateTime.now());
                jobRepository.save(job);
            }

            if(!stuckJobs.isEmpty()) {
                logger.info("Cleaned up {} stuck jobs", stuckJobs.size());
            }
        } catch(Exception e) {
            logger.error("Error cleaning up stuck jobs", e);
        }
    }

    /**
     * Clean up old completed jobs
     */
    @Scheduled(cron = "0 0 2 * * ?") // Run daily at 2 AM
    public void cleanupOldJobs() {
        try {
            LocalDateTime cutoffTime = LocalDateTime.now().minusDays(cleanupAgeDays);
            int deleted = jobRepository.cleanupOldJobs(cutoffTime);
            if(deleted > 0) {
                logger.info("Cleaned up {} old jobs", deleted);
            }
        } catch(Exception e) {
            logger.error("Error cleaning up old jobs", e);
        }
    }

    /**
     * Discover and register job executors from Spring context
     */
    private void discoverJobExecutors() {
        Map<String, JobExecutor> executors = applicationContext.getBeansOfType(JobExecutor.class);
        for(JobExecutor executor : executors.values()) {
            jobExecutors.put(executor.getJobType(), executor);
            logger.info("Registered job executor for type: {}", executor.getJobType());
        }
    }

    /**
     * Notify job update via WebSocket
     */
    private void notifyJobUpdate(BackgroundJob job) {
        if(webSocketHandler != null) {
            try {
                webSocketHandler.sendJobUpdate(job);
            } catch(Exception e) {
                logger.error("Error sending job update via WebSocket", e);
            }
        }
    }

    /**
     * Get stack trace as string
     */
    private String getStackTrace(Exception e) {
        return Arrays.stream(e.getStackTrace())
                .map(StackTraceElement::toString)
                .collect(Collectors.joining("\n\t", 
                    e.getClass().getName() + ": " + e.getMessage() + "\n\t", ""));
    }

    /**
     * Get job statistics
     */
    public Map<String, Object> getJobStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // Get counts by status
        for(JobStatus status : JobStatus.values()) {
            long count = jobRepository.countByStatus(status);
            stats.put("count_" + status.name().toLowerCase(), count);
        }

        // Get total count
        stats.put("total_count", jobRepository.count());
        stats.put("running_jobs", runningJobs.size());
        stats.put("executor_threads", executorThreads);

        return stats;
    }

    /**
     * Convert Map to JSON string
     */
    private String convertMapToJson(Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            logger.error("Error converting map to JSON", e);
            return null;
        }
    }

    /**
     * Check stuck jobs and update their status
     */
    @Scheduled(fixedDelay = 300000) // Every 5 minutes
    public void checkStuckJobs() {
        try {
            LocalDateTime stuckThreshold = LocalDateTime.now().minus(stuckJobTimeout, java.time.temporal.ChronoUnit.MILLIS);
            List<BackgroundJob> stuckJobs = jobRepository.findByStatusInAndStartedAtBefore(
                Arrays.asList(JobStatus.RUNNING),
                stuckThreshold
            );

            for (BackgroundJob job : stuckJobs) {
                logger.warn("Marking job {} as failed due to timeout", job.getId());
                job.setStatus(JobStatus.FAILED);
                job.setErrorMessage("Job timed out after " + (stuckJobTimeout / 1000) + " seconds");
                job.setCompletedAt(LocalDateTime.now());
                jobRepository.save(job);

                // Remove from running jobs
                runningJobs.remove(job.getId());
            }
        } catch (Exception e) {
            logger.error("Error checking stuck jobs", e);
        }
    }

}
