package com.integrixs.data.sql.repository;

import com.integrixs.data.model.RetryPolicy;
import com.integrixs.data.sql.mapper.RetryPolicyRowMapper;
import com.integrixs.data.sql.core.SqlQueryExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * SQL repository implementation for RetryPolicy entity.
 * Provides direct SQL operations using native SQL.
 */
@Repository
public class RetryPolicySqlRepository {

    private static final String TABLE_NAME = "retry_policys";

    private final SqlQueryExecutor sqlQueryExecutor;
    private final RetryPolicyRowMapper rowMapper = new RetryPolicyRowMapper();

    public RetryPolicySqlRepository(SqlQueryExecutor sqlQueryExecutor) {
        this.sqlQueryExecutor = sqlQueryExecutor;
    }

    public RetryPolicy save(RetryPolicy entity) {
        if (entity.getId() == null) {
            entity.setId(UUID.randomUUID());
            String sql = "INSERT INTO " + TABLE_NAME + " (id, created_at, updated_at) VALUES (?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
            sqlQueryExecutor.update(sql, entity.getId());
        } else {
            update(entity);
        }
        return entity;
    }

    public Optional<RetryPolicy> findById(UUID id) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE id = ?";
        return sqlQueryExecutor.queryForObject(sql, rowMapper, id);
    }

    public List<RetryPolicy> findAll() {
        String sql = "SELECT * FROM " + TABLE_NAME + " ORDER BY created_at DESC";
        return sqlQueryExecutor.queryForList(sql, rowMapper);
    }

    public Page<RetryPolicy> findAll(Pageable pageable) {
        String countSql = "SELECT COUNT(*) FROM " + TABLE_NAME;
        long total = sqlQueryExecutor.count(countSql);

        String dataSql = "SELECT * FROM " + TABLE_NAME + " ORDER BY created_at DESC LIMIT ? OFFSET ?";
        List<RetryPolicy> data = sqlQueryExecutor.queryForList(dataSql, rowMapper,
                                                            pageable.getPageSize(),
                                                            pageable.getOffset());

        return new PageImpl<>(data, pageable, total);
    }

    public RetryPolicy update(RetryPolicy entity) {
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

    public Optional<RetryPolicy> findByFlowId(UUID flowId) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE flow_id = ?";
        List<RetryPolicy> results = sqlQueryExecutor.queryForList(sql, rowMapper, flowId);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
}
