package com.integrixs.data.sql.repository;

import com.integrixs.data.model.FieldMapping;
import com.integrixs.data.sql.mapper.FieldMappingRowMapper;
import com.integrixs.data.sql.core.SqlQueryExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * SQL repository implementation for FieldMapping entity.
 * Provides direct SQL operations using native SQL.
 */
@Repository
public class FieldMappingSqlRepository {

    private static final String TABLE_NAME = "field_mappings";

    private final SqlQueryExecutor sqlQueryExecutor;
    private final FieldMappingRowMapper rowMapper = new FieldMappingRowMapper();

    public FieldMappingSqlRepository(SqlQueryExecutor sqlQueryExecutor) {
        this.sqlQueryExecutor = sqlQueryExecutor;
    }

    public FieldMapping save(FieldMapping entity) {
        if (entity.getId() == null) {
            entity.setId(UUID.randomUUID());
            String sql = "INSERT INTO " + TABLE_NAME + " (id, created_at, updated_at) VALUES (?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
            sqlQueryExecutor.update(sql, entity.getId());
        } else {
            update(entity);
        }
        return entity;
    }

    public Optional<FieldMapping> findById(UUID id) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE id = ?";
        return sqlQueryExecutor.queryForObject(sql, rowMapper, id);
    }

    public List<FieldMapping> findAll() {
        String sql = "SELECT * FROM " + TABLE_NAME + " ORDER BY created_at DESC";
        return sqlQueryExecutor.queryForList(sql, rowMapper);
    }

    public Page<FieldMapping> findAll(Pageable pageable) {
        String countSql = "SELECT COUNT(*) FROM " + TABLE_NAME;
        long total = sqlQueryExecutor.count(countSql);

        String dataSql = "SELECT * FROM " + TABLE_NAME + " ORDER BY created_at DESC LIMIT ? OFFSET ?";
        List<FieldMapping> data = sqlQueryExecutor.queryForList(dataSql, rowMapper,
                                                            pageable.getPageSize(),
                                                            pageable.getOffset());

        return new PageImpl<>(data, pageable, total);
    }

    public FieldMapping update(FieldMapping entity) {
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

    public List<FieldMapping> findByTransformationFlowIdAndIsActiveTrueOrderByTransformationExecutionOrder(UUID flowId) {
        String sql = "SELECT fm.* FROM " + TABLE_NAME + " fm " +
                     "JOIN flow_transformations ft ON fm.transformation_id = ft.id " +
                     "WHERE ft.flow_id = ? AND fm.is_active = true " +
                     "ORDER BY ft.execution_order, fm.target_field";
        return sqlQueryExecutor.queryForList(sql, rowMapper, flowId);
    }

    public List<FieldMapping> findByTransformationId(UUID transformationId) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE transformation_id = ? ORDER BY target_field";
        return sqlQueryExecutor.queryForList(sql, rowMapper, transformationId);
    }

    public long countByTransformationId(UUID transformationId) {
        String sql = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE transformation_id = ?";
        return sqlQueryExecutor.count(sql, transformationId);
    }

    public void deleteByTransformationId(UUID transformationId) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE transformation_id = ?";
        sqlQueryExecutor.update(sql, transformationId);
    }

    public List<FieldMapping> findByIsActiveTrue() {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE is_active = true ORDER BY created_at DESC";
        return sqlQueryExecutor.queryForList(sql, rowMapper);
    }

    public boolean existsById(UUID id) {
        String sql = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE id = ?";
        return sqlQueryExecutor.count(sql, id) > 0;
    }
}
