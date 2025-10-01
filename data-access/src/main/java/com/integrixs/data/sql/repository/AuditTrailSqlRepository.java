package com.integrixs.data.sql.repository;

import com.integrixs.data.model.AuditTrail;
import com.integrixs.data.sql.mapper.AuditTrailRowMapper;
import com.integrixs.data.sql.core.SqlQueryExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDateTime;
import java.util.ArrayList;
import com.integrixs.data.sql.core.ResultSetMapper;

/**
 * SQL repository implementation for AuditTrail entity.
 * Provides direct SQL operations using native SQL.
 */
@Repository
public class AuditTrailSqlRepository {

    private static final String TABLE_NAME = "audit_trails";

    private final SqlQueryExecutor sqlQueryExecutor;
    private final AuditTrailRowMapper rowMapper = new AuditTrailRowMapper();

    public AuditTrailSqlRepository(SqlQueryExecutor sqlQueryExecutor) {
        this.sqlQueryExecutor = sqlQueryExecutor;
    }

    public AuditTrail save(AuditTrail entity) {
        if (entity.getId() == null) {
            entity.setId(UUID.randomUUID());
            String sql = "INSERT INTO " + TABLE_NAME + " (id, created_at, updated_at) VALUES (?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
            sqlQueryExecutor.update(sql, entity.getId());
        } else {
            update(entity);
        }
        return entity;
    }

    public Optional<AuditTrail> findById(UUID id) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE id = ?";
        return sqlQueryExecutor.queryForObject(sql, rowMapper, id);
    }

    public List<AuditTrail> findAll() {
        String sql = "SELECT * FROM " + TABLE_NAME + " ORDER BY created_at DESC";
        return sqlQueryExecutor.queryForList(sql, rowMapper);
    }

    public Page<AuditTrail> findAll(Pageable pageable) {
        String countSql = "SELECT COUNT(*) FROM " + TABLE_NAME;
        long total = sqlQueryExecutor.count(countSql);

        String dataSql = "SELECT * FROM " + TABLE_NAME + " ORDER BY created_at DESC LIMIT ? OFFSET ?";
        List<AuditTrail> data = sqlQueryExecutor.queryForList(dataSql, rowMapper,
                                                            pageable.getPageSize(),
                                                            pageable.getOffset());

        return new PageImpl<>(data, pageable, total);
    }

    public AuditTrail update(AuditTrail entity) {
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

    public List<AuditTrail> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, String entityId) {
        String sql = "SELECT * FROM " + TABLE_NAME +
                    " WHERE entity_type = ? AND entity_id = ? ORDER BY created_at DESC";
        return sqlQueryExecutor.queryForList(sql, rowMapper, entityType, entityId);
    }

    public Page<AuditTrail> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable) {
        String countSql = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE user_id = ?";
        long total = sqlQueryExecutor.count(countSql, userId);

        String dataSql = "SELECT * FROM " + TABLE_NAME +
                        " WHERE user_id = ? ORDER BY created_at DESC LIMIT ? OFFSET ?";
        List<AuditTrail> data = sqlQueryExecutor.queryForList(dataSql, rowMapper,
                                                            userId,
                                                            pageable.getPageSize(),
                                                            pageable.getOffset());

        return new PageImpl<>(data, pageable, total);
    }

    public Page<AuditTrail> findByAuditActionOrderByCreatedAtDesc(AuditTrail.AuditAction action, Pageable pageable) {
        String countSql = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE audit_action = ?";
        long total = sqlQueryExecutor.count(countSql, action.toString());

        String dataSql = "SELECT * FROM " + TABLE_NAME +
                        " WHERE audit_action = ? ORDER BY created_at DESC LIMIT ? OFFSET ?";
        List<AuditTrail> data = sqlQueryExecutor.queryForList(dataSql, rowMapper,
                                                            action.toString(),
                                                            pageable.getPageSize(),
                                                            pageable.getOffset());

        return new PageImpl<>(data, pageable, total);
    }

    public Page<AuditTrail> searchAuditTrail(String entityType, AuditTrail.AuditAction action,
                                           UUID userId, LocalDateTime startDate,
                                           LocalDateTime endDate, Pageable pageable) {
        StringBuilder whereClauses = new StringBuilder(" WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (entityType != null) {
            whereClauses.append(" AND entity_type = ?");
            params.add(entityType);
        }

        if (action != null) {
            whereClauses.append(" AND audit_action = ?");
            params.add(action.toString());
        }

        if (userId != null) {
            whereClauses.append(" AND user_id = ?");
            params.add(userId);
        }

        if (startDate != null) {
            whereClauses.append(" AND created_at >= ?");
            params.add(ResultSetMapper.toTimestamp(startDate));
        }

        if (endDate != null) {
            whereClauses.append(" AND created_at <= ?");
            params.add(ResultSetMapper.toTimestamp(endDate));
        }

        String countSql = "SELECT COUNT(*) FROM " + TABLE_NAME + whereClauses.toString();
        long total = sqlQueryExecutor.count(countSql, params.toArray());

        String dataSql = "SELECT * FROM " + TABLE_NAME + whereClauses.toString() +
                        " ORDER BY created_at DESC LIMIT ? OFFSET ?";
        params.add(pageable.getPageSize());
        params.add(pageable.getOffset());

        List<AuditTrail> data = sqlQueryExecutor.queryForList(dataSql, rowMapper, params.toArray());

        return new PageImpl<>(data, pageable, total);
    }
}
