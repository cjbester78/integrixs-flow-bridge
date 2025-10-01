package com.integrixs.data.sql.repository;

import com.integrixs.data.model.DeadLetterMessage;
import com.integrixs.data.sql.mapper.DeadLetterMessageRowMapper;
import com.integrixs.data.sql.core.SqlQueryExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * SQL repository implementation for DeadLetterMessage entity.
 * Provides direct SQL operations using native SQL.
 */
@Repository
public class DeadLetterMessageSqlRepository {

    private static final String TABLE_NAME = "dead_letter_messages";

    private final SqlQueryExecutor sqlQueryExecutor;
    private final DeadLetterMessageRowMapper rowMapper = new DeadLetterMessageRowMapper();

    public DeadLetterMessageSqlRepository(SqlQueryExecutor sqlQueryExecutor) {
        this.sqlQueryExecutor = sqlQueryExecutor;
    }

    public DeadLetterMessage save(DeadLetterMessage entity) {
        if (entity.getId() == null) {
            entity.setId(UUID.randomUUID());
            String sql = "INSERT INTO " + TABLE_NAME + " (id, created_at, updated_at) VALUES (?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
            sqlQueryExecutor.update(sql, entity.getId());
        } else {
            update(entity);
        }
        return entity;
    }

    public Optional<DeadLetterMessage> findById(UUID id) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE id = ?";
        return sqlQueryExecutor.queryForObject(sql, rowMapper, id);
    }

    public List<DeadLetterMessage> findAll() {
        String sql = "SELECT * FROM " + TABLE_NAME + " ORDER BY created_at DESC";
        return sqlQueryExecutor.queryForList(sql, rowMapper);
    }

    public Page<DeadLetterMessage> findAll(Pageable pageable) {
        String countSql = "SELECT COUNT(*) FROM " + TABLE_NAME;
        long total = sqlQueryExecutor.count(countSql);

        String dataSql = "SELECT * FROM " + TABLE_NAME + " ORDER BY created_at DESC LIMIT ? OFFSET ?";
        List<DeadLetterMessage> data = sqlQueryExecutor.queryForList(dataSql, rowMapper,
                                                            pageable.getPageSize(),
                                                            pageable.getOffset());

        return new PageImpl<>(data, pageable, total);
    }

    public DeadLetterMessage update(DeadLetterMessage entity) {
        String sql = "UPDATE " + TABLE_NAME + " SET updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        sqlQueryExecutor.update(sql, entity.getId());
        return entity;
    }

    public void deleteById(UUID id) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE id = ?";
        sqlQueryExecutor.update(sql, id);
    }

    public long count() {
        String sql = "SELECT COUNT(*) FROM " + TABLE_NAME;
        return sqlQueryExecutor.count(sql);
    }

    public List<DeadLetterMessage> findByStatusAndRetryCountLessThanOrderByQueuedAtAsc(DeadLetterMessage.Status status, int maxRetryCount) {
        String sql = "SELECT * FROM " + TABLE_NAME +
                     " WHERE status = ? AND retry_count < ? ORDER BY queued_at ASC";
        return sqlQueryExecutor.queryForList(sql, rowMapper, status.toString(), maxRetryCount);
    }

    public Page<DeadLetterMessage> findByStatusAndRetryCountLessThan(DeadLetterMessage.Status status, int maxRetryCount, Pageable pageable) {
        String whereSql = " WHERE status = ? AND retry_count < ?";
        String countSql = "SELECT COUNT(*) FROM " + TABLE_NAME + whereSql;
        long total = sqlQueryExecutor.count(countSql, status.toString(), maxRetryCount);

        String dataSql = "SELECT * FROM " + TABLE_NAME + whereSql +
                        " ORDER BY queued_at ASC LIMIT ? OFFSET ?";
        List<DeadLetterMessage> data = sqlQueryExecutor.queryForList(dataSql, rowMapper,
                                                                     status.toString(),
                                                                     maxRetryCount,
                                                                     pageable.getPageSize(),
                                                                     pageable.getOffset());

        return new PageImpl<>(data, pageable, total);
    }

    public Optional<DeadLetterMessage> findByMessageId(String messageId) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE message_id = ?";
        List<DeadLetterMessage> results = sqlQueryExecutor.queryForList(sql, rowMapper, messageId);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public List<DeadLetterMessage> findByFlowIdAndQueuedAtBetween(UUID flowId, java.time.LocalDateTime start, java.time.LocalDateTime end) {
        String sql = "SELECT * FROM " + TABLE_NAME +
                     " WHERE flow_id = ? AND queued_at BETWEEN ? AND ? ORDER BY queued_at DESC";
        return sqlQueryExecutor.queryForList(sql, rowMapper, flowId,
                                           com.integrixs.data.sql.core.ResultSetMapper.toTimestamp(start),
                                           com.integrixs.data.sql.core.ResultSetMapper.toTimestamp(end));
    }

    public List<DeadLetterMessage> findByQueuedAtBetween(java.time.LocalDateTime start, java.time.LocalDateTime end) {
        String sql = "SELECT * FROM " + TABLE_NAME +
                     " WHERE queued_at BETWEEN ? AND ? ORDER BY queued_at DESC";
        return sqlQueryExecutor.queryForList(sql, rowMapper,
                                           com.integrixs.data.sql.core.ResultSetMapper.toTimestamp(start),
                                           com.integrixs.data.sql.core.ResultSetMapper.toTimestamp(end));
    }

    public int deleteByQueuedAtBeforeAndStatus(java.time.LocalDateTime cutoff, DeadLetterMessage.Status status) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE queued_at < ? AND status = ?";
        return sqlQueryExecutor.update(sql,
                                      com.integrixs.data.sql.core.ResultSetMapper.toTimestamp(cutoff),
                                      status.toString());
    }
}
