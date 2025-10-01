package com.integrixs.data.sql.repository;

import com.integrixs.data.model.MessageStructure;
import com.integrixs.data.sql.mapper.MessageStructureRowMapper;
import com.integrixs.data.sql.core.SqlQueryExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * SQL repository implementation for MessageStructure entity.
 * Provides direct SQL operations using native SQL.
 */
@Repository
public class MessageStructureSqlRepository {

    private static final String TABLE_NAME = "message_structures";

    private final SqlQueryExecutor sqlQueryExecutor;
    private final MessageStructureRowMapper rowMapper = new MessageStructureRowMapper();

    public MessageStructureSqlRepository(SqlQueryExecutor sqlQueryExecutor) {
        this.sqlQueryExecutor = sqlQueryExecutor;
    }

    public MessageStructure save(MessageStructure entity) {
        if (entity.getId() == null) {
            entity.setId(UUID.randomUUID());
            String sql = "INSERT INTO " + TABLE_NAME + " (id, created_at, updated_at) VALUES (?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
            sqlQueryExecutor.update(sql, entity.getId());
        } else {
            update(entity);
        }
        return entity;
    }

    public Optional<MessageStructure> findById(UUID id) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE id = ?";
        return sqlQueryExecutor.queryForObject(sql, rowMapper, id);
    }

    public List<MessageStructure> findAll() {
        String sql = "SELECT * FROM " + TABLE_NAME + " ORDER BY created_at DESC";
        return sqlQueryExecutor.queryForList(sql, rowMapper);
    }

    public Page<MessageStructure> findAll(Pageable pageable) {
        String countSql = "SELECT COUNT(*) FROM " + TABLE_NAME;
        long total = sqlQueryExecutor.count(countSql);

        String dataSql = "SELECT * FROM " + TABLE_NAME + " ORDER BY created_at DESC LIMIT ? OFFSET ?";
        List<MessageStructure> data = sqlQueryExecutor.queryForList(dataSql, rowMapper,
                                                            pageable.getPageSize(),
                                                            pageable.getOffset());

        return new PageImpl<>(data, pageable, total);
    }

    public MessageStructure update(MessageStructure entity) {
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

    public boolean existsByNameAndBusinessComponentIdAndIsActiveTrue(String name, UUID businessComponentId) {
        String sql = "SELECT COUNT(*) FROM " + TABLE_NAME +
                    " WHERE name = ? AND business_component_id = ? AND is_active = true";
        return sqlQueryExecutor.count(sql, name, businessComponentId) > 0;
    }

    public boolean existsByNameAndBusinessComponentIdAndIdNotAndIsActiveTrue(String name, UUID businessComponentId, UUID excludeId) {
        String sql = "SELECT COUNT(*) FROM " + TABLE_NAME +
                    " WHERE name = ? AND business_component_id = ? AND id != ? AND is_active = true";
        return sqlQueryExecutor.count(sql, name, businessComponentId, excludeId) > 0;
    }

    public Optional<MessageStructure> findByIdAndIsActiveTrue(UUID id) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE id = ? AND is_active = true";
        List<MessageStructure> results = sqlQueryExecutor.queryForList(sql, rowMapper, id);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public Page<MessageStructure> searchMessageStructures(UUID businessComponentId, String searchTerm, Pageable pageable) {
        StringBuilder whereSql = new StringBuilder(" WHERE is_active = true");
        java.util.List<Object> params = new java.util.ArrayList<>();

        if (businessComponentId != null) {
            whereSql.append(" AND business_component_id = ?");
            params.add(businessComponentId);
        }

        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            whereSql.append(" AND (name ILIKE ? OR description ILIKE ?)");
            params.add("%" + searchTerm + "%");
            params.add("%" + searchTerm + "%");
        }

        String countSql = "SELECT COUNT(*) FROM " + TABLE_NAME + whereSql.toString();
        long total = sqlQueryExecutor.count(countSql, params.toArray());

        String dataSql = "SELECT * FROM " + TABLE_NAME + whereSql.toString() +
                        " ORDER BY created_at DESC LIMIT ? OFFSET ?";
        params.add(pageable.getPageSize());
        params.add((int)pageable.getOffset());

        List<MessageStructure> data = sqlQueryExecutor.queryForList(dataSql, rowMapper, params.toArray());

        return new PageImpl<>(data, pageable, total);
    }

    public List<MessageStructure> findByBusinessComponentIdAndIsActiveTrueOrderByName(UUID businessComponentId) {
        String sql = "SELECT * FROM " + TABLE_NAME +
                    " WHERE business_component_id = ? AND is_active = true ORDER BY name";
        return sqlQueryExecutor.queryForList(sql, rowMapper, businessComponentId);
    }

    public void delete(MessageStructure structure) {
        deleteById(structure.getId());
    }

    public boolean existsByNameAndIsActiveTrue(String name) {
        String sql = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE name = ? AND is_active = true";
        return sqlQueryExecutor.count(sql, name) > 0;
    }

    public Optional<MessageStructure> findByNameAndIsActiveTrue(String name) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE name = ? AND is_active = true";
        List<MessageStructure> results = sqlQueryExecutor.queryForList(sql, rowMapper, name);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
}
