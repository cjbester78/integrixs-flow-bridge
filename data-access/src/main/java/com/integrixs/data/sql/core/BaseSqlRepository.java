package com.integrixs.data.sql.core;

import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Abstract base repository for native SQL operations.
 * Provides common CRUD operations for entities.
 *
 * @param <T> Entity type
 * @param <ID> ID type (typically UUID)
 */
public abstract class BaseSqlRepository<T, ID> {

    protected final SqlQueryExecutor sqlQueryExecutor;
    protected final String tableName;
    protected final String idColumn;
    protected final RowMapper<T> rowMapper;

    public BaseSqlRepository(SqlQueryExecutor sqlQueryExecutor, String tableName, String idColumn, RowMapper<T> rowMapper) {
        this.sqlQueryExecutor = sqlQueryExecutor;
        this.tableName = tableName;
        this.idColumn = idColumn;
        this.rowMapper = rowMapper;
    }

    /**
     * Find entity by ID
     */
    public Optional<T> findById(ID id) {
        String sql = String.format("SELECT * FROM %s WHERE %s = ?", tableName, idColumn);
        return sqlQueryExecutor.queryForObject(sql, rowMapper, id);
    }

    /**
     * Find all entities
     */
    public List<T> findAll() {
        String sql = String.format("SELECT * FROM %s", tableName);
        return sqlQueryExecutor.queryForList(sql, rowMapper);
    }

    /**
     * Find all entities with pagination
     */
    public List<T> findAll(int offset, int limit) {
        String sql = String.format("SELECT * FROM %s LIMIT ? OFFSET ?", tableName);
        return sqlQueryExecutor.queryForList(sql, rowMapper, limit, offset);
    }

    /**
     * Count all entities
     */
    public long count() {
        String sql = String.format("SELECT COUNT(*) FROM %s", tableName);
        return sqlQueryExecutor.count(sql);
    }

    /**
     * Check if entity exists by ID
     */
    public boolean existsById(ID id) {
        String sql = String.format("SELECT COUNT(*) FROM %s WHERE %s = ?", tableName, idColumn);
        return sqlQueryExecutor.exists(sql, id);
    }

    /**
     * Delete entity by ID
     */
    public void deleteById(ID id) {
        String sql = String.format("DELETE FROM %s WHERE %s = ?", tableName, idColumn);
        sqlQueryExecutor.update(sql, id);
    }

    /**
     * Delete all entities
     */
    public void deleteAll() {
        String sql = String.format("DELETE FROM %s", tableName);
        sqlQueryExecutor.update(sql);
    }

    /**
     * Save entity - must be implemented by subclasses
     */
    public abstract T save(T entity);

    /**
     * Update entity - must be implemented by subclasses
     */
    public abstract T update(T entity);

    /**
     * Generate new UUID if needed
     */
    protected UUID generateId() {
        return UUID.randomUUID();
    }

    /**
     * Helper method to build INSERT SQL
     */
    protected String buildInsertSql(String... columns) {
        StringBuilder sql = new StringBuilder("INSERT INTO ").append(tableName).append(" (");
        StringBuilder values = new StringBuilder(" VALUES (");

        for (int i = 0; i < columns.length; i++) {
            sql.append(columns[i]);
            values.append("?");
            if (i < columns.length - 1) {
                sql.append(", ");
                values.append(", ");
            }
        }

        sql.append(")").append(values).append(")");
        return sql.toString();
    }

    /**
     * Helper method to build UPDATE SQL
     */
    protected String buildUpdateSql(String... columns) {
        StringBuilder sql = new StringBuilder("UPDATE ").append(tableName).append(" SET ");

        for (int i = 0; i < columns.length; i++) {
            sql.append(columns[i]).append(" = ?");
            if (i < columns.length - 1) {
                sql.append(", ");
            }
        }

        sql.append(" WHERE ").append(idColumn).append(" = ?");
        return sql.toString();
    }
}