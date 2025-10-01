package com.integrixs.data.sql.repository;

import com.integrixs.data.model.FlowStructure;
import com.integrixs.data.sql.mapper.FlowStructureRowMapper;
import com.integrixs.data.sql.core.SqlQueryExecutor;
import com.integrixs.data.sql.core.SqlPaginationHelper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * SQL repository implementation for FlowStructure entity.
 * Provides direct SQL operations using native SQL.
 */
@Repository
public class FlowStructureSqlRepository {

    private static final String TABLE_NAME = "flow_structures";

    private final SqlQueryExecutor sqlQueryExecutor;
    private final FlowStructureRowMapper rowMapper = new FlowStructureRowMapper();

    public FlowStructureSqlRepository(SqlQueryExecutor sqlQueryExecutor) {
        this.sqlQueryExecutor = sqlQueryExecutor;
    }

    public FlowStructure save(FlowStructure entity) {
        if (entity.getId() == null) {
            entity.setId(UUID.randomUUID());
            String sql = "INSERT INTO " + TABLE_NAME + " (id, created_at, updated_at) VALUES (?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
            sqlQueryExecutor.update(sql, entity.getId());
        } else {
            update(entity);
        }
        return entity;
    }

    public Optional<FlowStructure> findById(UUID id) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE id = ?";
        return sqlQueryExecutor.queryForObject(sql, rowMapper, id);
    }

    public List<FlowStructure> findAll() {
        String sql = "SELECT * FROM " + TABLE_NAME + " ORDER BY created_at DESC";
        return sqlQueryExecutor.queryForList(sql, rowMapper);
    }

    public Page<FlowStructure> findAll(Pageable pageable) {
        String countSql = "SELECT COUNT(*) FROM " + TABLE_NAME;
        long total = sqlQueryExecutor.count(countSql);

        String dataSql = "SELECT * FROM " + TABLE_NAME + " ORDER BY created_at DESC LIMIT ? OFFSET ?";
        List<FlowStructure> data = sqlQueryExecutor.queryForList(dataSql, rowMapper,
                                                            pageable.getPageSize(),
                                                            pageable.getOffset());

        return new PageImpl<>(data, pageable, total);
    }

    public FlowStructure update(FlowStructure entity) {
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

    public Optional<FlowStructure> findByIdAndIsActiveTrue(UUID id) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE id = ? AND is_active = true";
        return sqlQueryExecutor.queryForObject(sql, rowMapper, id);
    }

    public boolean existsByNameAndBusinessComponentIdAndIdNotAndIsActiveTrue(String name, UUID businessComponentId, UUID excludeId) {
        String sql = "SELECT COUNT(*) FROM " + TABLE_NAME +
                    " WHERE name = ? AND business_component_id = ? AND id != ? AND is_active = true";
        long count = sqlQueryExecutor.count(sql, name, businessComponentId, excludeId);
        return count > 0;
    }

    public Page<FlowStructure> findAllWithFilters(UUID businessComponentId, String search, String type,
                                                  String status, Pageable pageable) {
        StringBuilder whereClause = new StringBuilder(" WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (businessComponentId != null) {
            whereClause.append(" AND business_component_id = ?");
            params.add(businessComponentId);
        }

        if (search != null && !search.trim().isEmpty()) {
            whereClause.append(" AND (name ILIKE ? OR description ILIKE ?)");
            params.add("%" + search + "%");
            params.add("%" + search + "%");
        }

        if (type != null && !type.trim().isEmpty()) {
            whereClause.append(" AND structure_type = ?");
            params.add(type);
        }

        if (status != null && !status.trim().isEmpty()) {
            if ("active".equalsIgnoreCase(status)) {
                whereClause.append(" AND is_active = true");
            } else if ("inactive".equalsIgnoreCase(status)) {
                whereClause.append(" AND is_active = false");
            }
        }

        String countSql = "SELECT COUNT(*) FROM " + TABLE_NAME + whereClause.toString();
        long total = sqlQueryExecutor.count(countSql, params.toArray());

        String sql = "SELECT * FROM " + TABLE_NAME + whereClause.toString();
        sql += SqlPaginationHelper.buildOrderByClause(pageable.getSort());
        sql += SqlPaginationHelper.buildPaginationClause(pageable);

        List<FlowStructure> structures = sqlQueryExecutor.queryForList(sql, rowMapper, params.toArray());

        return new PageImpl<>(structures, pageable, total);
    }

    public List<FlowStructure> findByBusinessComponentIdAndIsActiveTrueOrderByName(UUID businessComponentId) {
        String sql = "SELECT * FROM " + TABLE_NAME +
                    " WHERE business_component_id = ? AND is_active = true ORDER BY name";
        return sqlQueryExecutor.queryForList(sql, rowMapper, businessComponentId);
    }

    public boolean existsByNameAndBusinessComponentIdAndIsActiveTrue(String name, UUID businessComponentId) {
        String sql = "SELECT COUNT(*) FROM " + TABLE_NAME +
                    " WHERE name = ? AND business_component_id = ? AND is_active = true";
        long count = sqlQueryExecutor.count(sql, name, businessComponentId);
        return count > 0;
    }

    public void delete(FlowStructure flowStructure) {
        deleteById(flowStructure.getId());
    }
}
