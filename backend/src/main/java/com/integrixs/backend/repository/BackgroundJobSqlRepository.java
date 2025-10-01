package com.integrixs.backend.repository;

import com.integrixs.backend.jobs.BackgroundJob;
import com.integrixs.backend.jobs.JobStatus;
import com.integrixs.data.model.User;
import com.integrixs.data.sql.core.BaseSqlRepository;
import com.integrixs.data.sql.core.ResultSetMapper;
import com.integrixs.data.sql.core.SqlQueryExecutor;
import com.integrixs.data.sql.core.SqlPaginationHelper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * SQL implementation of BackgroundJobRepository
 */
@Repository
public class BackgroundJobSqlRepository extends BaseSqlRepository<BackgroundJob, UUID> implements BackgroundJobRepository {

    private static final String TABLE_NAME = "background_jobs";
    private static final String ID_COLUMN = "id";

    private static final RowMapper<BackgroundJob> JOB_ROW_MAPPER = new RowMapper<BackgroundJob>() {
        @Override
        public BackgroundJob mapRow(ResultSet rs, int rowNum) throws SQLException {
            BackgroundJob job = new BackgroundJob();
            job.setId(ResultSetMapper.getUUID(rs, "id"));
            job.setName(ResultSetMapper.getString(rs, "name"));
            job.setDescription(ResultSetMapper.getString(rs, "description"));
            job.setJobType(ResultSetMapper.getString(rs, "job_type"));

            String statusStr = ResultSetMapper.getString(rs, "status");
            if (statusStr != null) {
                job.setStatus(JobStatus.valueOf(statusStr));
            }

            job.setParameters(ResultSetMapper.getString(rs, "parameters"));
            job.setResult(ResultSetMapper.getString(rs, "result"));
            job.setErrorMessage(ResultSetMapper.getString(rs, "error_message"));
            job.setScheduledAt(ResultSetMapper.getLocalDateTime(rs, "scheduled_at"));
            job.setStartedAt(ResultSetMapper.getLocalDateTime(rs, "started_at"));
            job.setCompletedAt(ResultSetMapper.getLocalDateTime(rs, "completed_at"));
            job.setProgress(rs.getInt("progress"));
            job.setCreatedByUsername(ResultSetMapper.getString(rs, "created_by"));
            job.setRecurring(rs.getBoolean("recurring"));
            job.setCronExpression(ResultSetMapper.getString(rs, "cron_expression"));
            job.setRetryCount(rs.getInt("retry_count"));
            job.setMaxRetries(rs.getInt("max_retries"));
            job.setStackTrace(ResultSetMapper.getString(rs, "stack_trace"));
            job.setTenantId(ResultSetMapper.getUUID(rs, "tenant_id"));
            job.setCreatedAt(ResultSetMapper.getLocalDateTime(rs, "created_at"));
            job.setUpdatedAt(ResultSetMapper.getLocalDateTime(rs, "updated_at"));

            // Handle User fields from BaseEntity
            String createdByUsername = ResultSetMapper.getString(rs, "created_by_username");
            if (createdByUsername != null) {
                User createdByUser = new User();
                createdByUser.setUsername(createdByUsername);
                job.setCreatedBy(createdByUser);
            }

            String updatedByUsername = ResultSetMapper.getString(rs, "updated_by_username");
            if (updatedByUsername != null) {
                User updatedByUser = new User();
                updatedByUser.setUsername(updatedByUsername);
                job.setUpdatedBy(updatedByUser);
            }

            return job;
        }
    };

    public BackgroundJobSqlRepository(SqlQueryExecutor sqlQueryExecutor) {
        super(sqlQueryExecutor, TABLE_NAME, ID_COLUMN, JOB_ROW_MAPPER);
    }

    @Override
    public BackgroundJob save(BackgroundJob job) {
        if (job.getId() == null) {
            job.setId(generateId());
        }

        if (job.getCreatedAt() == null) {
            job.setCreatedAt(LocalDateTime.now());
        }
        job.setUpdatedAt(LocalDateTime.now());

        boolean exists = existsById(job.getId());

        if (!exists) {
            String sql = "INSERT INTO " + TABLE_NAME + " (" +
                        "id, name, description, job_type, status, parameters, result, error_message, " +
                        "scheduled_at, started_at, completed_at, progress, created_by, recurring, " +
                        "cron_expression, retry_count, max_retries, stack_trace, tenant_id, " +
                        "created_at, updated_at, created_by_username, updated_by_username" +
                        ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            sqlQueryExecutor.update(sql,
                job.getId(),
                job.getName(),
                job.getDescription(),
                job.getJobType(),
                job.getStatus() != null ? job.getStatus().name() : null,
                job.getParameters(),
                job.getResult(),
                job.getErrorMessage(),
                ResultSetMapper.toTimestamp(job.getScheduledAt()),
                ResultSetMapper.toTimestamp(job.getStartedAt()),
                ResultSetMapper.toTimestamp(job.getCompletedAt()),
                job.getProgress(),
                job.getCreatedByUsername(),
                job.isRecurring(),
                job.getCronExpression(),
                job.getRetryCount(),
                job.getMaxRetries(),
                job.getStackTrace(),
                job.getTenantId(),
                ResultSetMapper.toTimestamp(job.getCreatedAt()),
                ResultSetMapper.toTimestamp(job.getUpdatedAt()),
                job.getCreatedBy() != null ? job.getCreatedBy().getUsername() : null,
                job.getUpdatedBy() != null ? job.getUpdatedBy().getUsername() : null
            );
        } else {
            update(job);
        }

        return job;
    }

    @Override
    public BackgroundJob update(BackgroundJob job) {
        job.setUpdatedAt(LocalDateTime.now());

        String sql = "UPDATE " + TABLE_NAME + " SET " +
                    "name = ?, description = ?, job_type = ?, status = ?, parameters = ?, " +
                    "result = ?, error_message = ?, scheduled_at = ?, started_at = ?, " +
                    "completed_at = ?, progress = ?, recurring = ?, cron_expression = ?, " +
                    "retry_count = ?, max_retries = ?, stack_trace = ?, updated_at = ?, " +
                    "updated_by_username = ? WHERE id = ?";

        sqlQueryExecutor.update(sql,
            job.getName(),
            job.getDescription(),
            job.getJobType(),
            job.getStatus() != null ? job.getStatus().name() : null,
            job.getParameters(),
            job.getResult(),
            job.getErrorMessage(),
            ResultSetMapper.toTimestamp(job.getScheduledAt()),
            ResultSetMapper.toTimestamp(job.getStartedAt()),
            ResultSetMapper.toTimestamp(job.getCompletedAt()),
            job.getProgress(),
            job.isRecurring(),
            job.getCronExpression(),
            job.getRetryCount(),
            job.getMaxRetries(),
            job.getStackTrace(),
            ResultSetMapper.toTimestamp(job.getUpdatedAt()),
            job.getUpdatedBy() instanceof User ? ((User) job.getUpdatedBy()).getUsername() : null,
            job.getId()
        );

        return job;
    }

    @Override
    public Page<BackgroundJob> findAll(Pageable pageable) {
        String countSql = "SELECT COUNT(*) FROM " + TABLE_NAME;
        long total = sqlQueryExecutor.count(countSql);

        String sql = "SELECT * FROM " + TABLE_NAME;
        sql += SqlPaginationHelper.buildOrderByClause(pageable.getSort());
        sql += SqlPaginationHelper.buildPaginationClause(pageable);

        List<BackgroundJob> jobs = sqlQueryExecutor.queryForList(sql, JOB_ROW_MAPPER);

        return new PageImpl<>(jobs, pageable, total);
    }

    @Override
    public Page<BackgroundJob> findByStatus(JobStatus status, Pageable pageable) {
        String countSql = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE status = ?";
        long total = sqlQueryExecutor.count(countSql, status.name());

        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE status = ?";
        sql += SqlPaginationHelper.buildOrderByClause(pageable.getSort());
        sql += SqlPaginationHelper.buildPaginationClause(pageable);

        List<BackgroundJob> jobs = sqlQueryExecutor.queryForList(sql, JOB_ROW_MAPPER, status.name());

        return new PageImpl<>(jobs, pageable, total);
    }

    @Override
    public Page<BackgroundJob> findByCreatedBy(String createdBy, Pageable pageable) {
        String countSql = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE created_by = ?";
        long total = sqlQueryExecutor.count(countSql, createdBy);

        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE created_by = ?";
        sql += SqlPaginationHelper.buildOrderByClause(pageable.getSort());
        sql += SqlPaginationHelper.buildPaginationClause(pageable);

        List<BackgroundJob> jobs = sqlQueryExecutor.queryForList(sql, JOB_ROW_MAPPER, createdBy);

        return new PageImpl<>(jobs, pageable, total);
    }

    @Override
    public Page<BackgroundJob> findByJobType(String jobType, Pageable pageable) {
        String countSql = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE job_type = ?";
        long total = sqlQueryExecutor.count(countSql, jobType);

        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE job_type = ?";
        sql += SqlPaginationHelper.buildOrderByClause(pageable.getSort());
        sql += SqlPaginationHelper.buildPaginationClause(pageable);

        List<BackgroundJob> jobs = sqlQueryExecutor.queryForList(sql, JOB_ROW_MAPPER, jobType);

        return new PageImpl<>(jobs, pageable, total);
    }

    @Override
    public List<BackgroundJob> findByStatusAndScheduledAtBefore(JobStatus status, LocalDateTime dateTime) {
        String sql = "SELECT * FROM " + TABLE_NAME +
                    " WHERE status = ? AND scheduled_at < ? ORDER BY scheduled_at ASC";
        return sqlQueryExecutor.queryForList(sql, JOB_ROW_MAPPER,
            status.name(), ResultSetMapper.toTimestamp(dateTime));
    }

    @Override
    public List<BackgroundJob> findByStatusInAndStartedAtBefore(List<JobStatus> statuses, LocalDateTime dateTime) {
        if (statuses == null || statuses.isEmpty()) {
            return List.of();
        }

        StringBuilder sql = new StringBuilder("SELECT * FROM " + TABLE_NAME + " WHERE status IN (");
        for (int i = 0; i < statuses.size(); i++) {
            sql.append("?");
            if (i < statuses.size() - 1) {
                sql.append(",");
            }
        }
        sql.append(") AND started_at < ? ORDER BY started_at ASC");

        Object[] params = new Object[statuses.size() + 1];
        for (int i = 0; i < statuses.size(); i++) {
            params[i] = statuses.get(i).name();
        }
        params[statuses.size()] = ResultSetMapper.toTimestamp(dateTime);

        return sqlQueryExecutor.queryForList(sql.toString(), JOB_ROW_MAPPER, params);
    }

    @Override
    public List<BackgroundJob> findByRecurringTrue() {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE recurring = true";
        return sqlQueryExecutor.queryForList(sql, JOB_ROW_MAPPER);
    }

    @Override
    public Optional<BackgroundJob> findFirstByStatusOrderByScheduledAtAsc(JobStatus status) {
        String sql = "SELECT * FROM " + TABLE_NAME +
                    " WHERE status = ? ORDER BY scheduled_at ASC LIMIT 1";
        return sqlQueryExecutor.queryForObject(sql, JOB_ROW_MAPPER, status.name());
    }

    @Override
    public long countByStatus(JobStatus status) {
        String sql = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE status = ?";
        return sqlQueryExecutor.count(sql, status.name());
    }

    /**
     * Find pending jobs ready for execution
     */
    @Override
    public Page<BackgroundJob> findPendingJobs(LocalDateTime beforeTime, Pageable pageable) {
        String countSql = "SELECT COUNT(*) FROM " + TABLE_NAME +
                         " WHERE status = ? AND (scheduled_at IS NULL OR scheduled_at <= ?)";
        long total = sqlQueryExecutor.count(countSql, JobStatus.PENDING.name(), ResultSetMapper.toTimestamp(beforeTime));

        String sql = "SELECT * FROM " + TABLE_NAME +
                    " WHERE status = ? AND (scheduled_at IS NULL OR scheduled_at <= ?)";
        sql += SqlPaginationHelper.buildOrderByClause(pageable.getSort());
        sql += SqlPaginationHelper.buildPaginationClause(pageable);

        List<BackgroundJob> jobs = sqlQueryExecutor.queryForList(sql, JOB_ROW_MAPPER,
            JobStatus.PENDING.name(), ResultSetMapper.toTimestamp(beforeTime));

        return new PageImpl<>(jobs, pageable, total);
    }

    /**
     * Update job status atomically
     */
    @Override
    public int updateJobStatus(UUID jobId, JobStatus fromStatus, JobStatus toStatus) {
        String sql = "UPDATE " + TABLE_NAME + " SET status = ?, updated_at = CURRENT_TIMESTAMP " +
                    "WHERE id = ? AND status = ?";
        return sqlQueryExecutor.update(sql, toStatus.name(), jobId, fromStatus.name());
    }

    /**
     * Find jobs by status and completed before date
     */
    @Override
    public Page<BackgroundJob> findByStatusInAndCompletedAtBefore(List<JobStatus> statuses, LocalDateTime dateTime, Pageable pageable) {
        if (statuses == null || statuses.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        StringBuilder sql = new StringBuilder("SELECT * FROM " + TABLE_NAME + " WHERE status IN (");
        StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE status IN (");

        for (int i = 0; i < statuses.size(); i++) {
            sql.append("?");
            countSql.append("?");
            if (i < statuses.size() - 1) {
                sql.append(",");
                countSql.append(",");
            }
        }
        sql.append(") AND completed_at < ?");
        countSql.append(") AND completed_at < ?");

        Object[] params = new Object[statuses.size() + 1];
        for (int i = 0; i < statuses.size(); i++) {
            params[i] = statuses.get(i).name();
        }
        params[statuses.size()] = ResultSetMapper.toTimestamp(dateTime);

        long total = sqlQueryExecutor.count(countSql.toString(), params);

        sql.append(SqlPaginationHelper.buildOrderByClause(pageable.getSort()));
        sql.append(SqlPaginationHelper.buildPaginationClause(pageable));

        List<BackgroundJob> jobs = sqlQueryExecutor.queryForList(sql.toString(), JOB_ROW_MAPPER, params);

        return new PageImpl<>(jobs, pageable, total);
    }

    /**
     * Update job progress
     */
    @Override
    public int updateJobProgress(UUID jobId, int progress, String currentStep) {
        String sql = "UPDATE " + TABLE_NAME + " SET progress = ?, current_step = ?, updated_at = CURRENT_TIMESTAMP " +
                    "WHERE id = ?";
        return sqlQueryExecutor.update(sql, progress, currentStep, jobId);
    }

    /**
     * Find stuck jobs
     */
    @Override
    public List<BackgroundJob> findStuckJobs(LocalDateTime stuckThreshold) {
        // This is the same as findByStatusInAndStartedAtBefore with RUNNING status
        return findByStatusInAndStartedAtBefore(List.of(JobStatus.RUNNING), stuckThreshold);
    }

    /**
     * Clean up old jobs
     */
    @Override
    public int cleanupOldJobs(LocalDateTime cutoffDate) {
        String sql = "DELETE FROM " + TABLE_NAME +
                    " WHERE status IN ('COMPLETED', 'FAILED', 'CANCELLED') AND completed_at < ?";
        return sqlQueryExecutor.update(sql, ResultSetMapper.toTimestamp(cutoffDate));
    }
}