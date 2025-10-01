package com.integrixs.data.sql.repository;

import com.integrixs.data.model.ErrorRecord;
import com.integrixs.data.sql.mapper.ErrorRecordRowMapper;
import com.integrixs.data.sql.core.SqlQueryExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * SQL repository implementation for ErrorRecord entity.
 * Provides direct SQL operations using native SQL.
 */
@Repository
public class ErrorRecordSqlRepository {

    private static final String TABLE_NAME = "error_records";

    private final SqlQueryExecutor sqlQueryExecutor;
    private final ErrorRecordRowMapper rowMapper = new ErrorRecordRowMapper();

    public ErrorRecordSqlRepository(SqlQueryExecutor sqlQueryExecutor) {
        this.sqlQueryExecutor = sqlQueryExecutor;
    }

    public ErrorRecord save(ErrorRecord entity) {
        if (entity.getId() == null) {
            entity.setId(UUID.randomUUID());
            String sql = "INSERT INTO " + TABLE_NAME + " (id, created_at, updated_at) VALUES (?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
            sqlQueryExecutor.update(sql, entity.getId());
        } else {
            update(entity);
        }
        return entity;
    }

    public Optional<ErrorRecord> findById(UUID id) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE id = ?";
        return sqlQueryExecutor.queryForObject(sql, rowMapper, id);
    }

    public List<ErrorRecord> findAll() {
        String sql = "SELECT * FROM " + TABLE_NAME + " ORDER BY created_at DESC";
        return sqlQueryExecutor.queryForList(sql, rowMapper);
    }

    public Page<ErrorRecord> findAll(Pageable pageable) {
        String countSql = "SELECT COUNT(*) FROM " + TABLE_NAME;
        long total = sqlQueryExecutor.count(countSql);

        String dataSql = "SELECT * FROM " + TABLE_NAME + " ORDER BY created_at DESC LIMIT ? OFFSET ?";
        List<ErrorRecord> data = sqlQueryExecutor.queryForList(dataSql, rowMapper,
                                                            pageable.getPageSize(),
                                                            pageable.getOffset());

        return new PageImpl<>(data, pageable, total);
    }

    public ErrorRecord update(ErrorRecord entity) {
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

    public List<ErrorRecord> findByFlowIdAndOccurredAtAfterOrderByOccurredAtDesc(UUID flowId, java.time.LocalDateTime after) {
        String sql = "SELECT * FROM " + TABLE_NAME +
                     " WHERE flow_id = ? AND occurred_at > ? ORDER BY occurred_at DESC";
        return sqlQueryExecutor.queryForList(sql, rowMapper, flowId,
                                           com.integrixs.data.sql.core.ResultSetMapper.toTimestamp(after));
    }
}
