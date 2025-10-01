package com.integrixs.data.sql.repository;

import com.integrixs.data.model.FlowExecution;
import com.integrixs.data.sql.mapper.FlowExecutionRowMapper;
import com.integrixs.data.sql.core.SqlQueryExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * SQL repository implementation for FlowExecution entity.
 * Provides direct SQL operations using native SQL.
 */
@Repository
public class FlowExecutionSqlRepository {

    private static final String TABLE_NAME = "flow_executions";

    private final SqlQueryExecutor sqlQueryExecutor;
    private final FlowExecutionRowMapper rowMapper = new FlowExecutionRowMapper();

    public FlowExecutionSqlRepository(SqlQueryExecutor sqlQueryExecutor) {
        this.sqlQueryExecutor = sqlQueryExecutor;
    }

    public FlowExecution save(FlowExecution entity) {
        if (entity.getId() == null) {
            entity.setId(UUID.randomUUID());
            String sql = "INSERT INTO " + TABLE_NAME + " (id, created_at, updated_at) VALUES (?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
            sqlQueryExecutor.update(sql, entity.getId());
        } else {
            update(entity);
        }
        return entity;
    }

    public Optional<FlowExecution> findById(UUID id) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE id = ?";
        return sqlQueryExecutor.queryForObject(sql, rowMapper, id);
    }

    public List<FlowExecution> findAll() {
        String sql = "SELECT * FROM " + TABLE_NAME + " ORDER BY created_at DESC";
        return sqlQueryExecutor.queryForList(sql, rowMapper);
    }

    public Page<FlowExecution> findAll(Pageable pageable) {
        String countSql = "SELECT COUNT(*) FROM " + TABLE_NAME;
        long total = sqlQueryExecutor.count(countSql);

        String dataSql = "SELECT * FROM " + TABLE_NAME + " ORDER BY created_at DESC LIMIT ? OFFSET ?";
        List<FlowExecution> data = sqlQueryExecutor.queryForList(dataSql, rowMapper,
                                                            pageable.getPageSize(),
                                                            pageable.getOffset());

        return new PageImpl<>(data, pageable, total);
    }

    public FlowExecution update(FlowExecution entity) {
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

    public Optional<FlowExecution> findByMessageId(UUID messageId) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE message_id = ?";
        List<FlowExecution> results = sqlQueryExecutor.queryForList(sql, rowMapper, messageId);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public Page<FlowExecution> findByStartedAtBetween(java.time.LocalDateTime start, java.time.LocalDateTime end, Pageable pageable) {
        String countSql = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE started_at BETWEEN ? AND ?";
        long total = sqlQueryExecutor.count(countSql,
                                          com.integrixs.data.sql.core.ResultSetMapper.toTimestamp(start),
                                          com.integrixs.data.sql.core.ResultSetMapper.toTimestamp(end));

        String dataSql = "SELECT * FROM " + TABLE_NAME + " WHERE started_at BETWEEN ? AND ? ORDER BY started_at DESC LIMIT ? OFFSET ?";
        List<FlowExecution> data = sqlQueryExecutor.queryForList(dataSql, rowMapper,
                                                                com.integrixs.data.sql.core.ResultSetMapper.toTimestamp(start),
                                                                com.integrixs.data.sql.core.ResultSetMapper.toTimestamp(end),
                                                                pageable.getPageSize(),
                                                                pageable.getOffset());

        return new PageImpl<>(data, pageable, total);
    }
}
