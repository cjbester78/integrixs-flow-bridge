package com.integrixs.data.sql.core;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Core SQL execution service using Spring JdbcTemplate.
 * Provides common database operations with native SQL.
 */
@Component
public class SqlQueryExecutor {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public SqlQueryExecutor(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    /**
     * Execute a query and return a list of results
     */
    public <T> List<T> queryForList(String sql, RowMapper<T> rowMapper, Object... params) {
        return jdbcTemplate.query(sql, rowMapper, params);
    }

    /**
     * Execute a query and return a single result
     */
    public <T> Optional<T> queryForObject(String sql, RowMapper<T> rowMapper, Object... params) {
        List<T> results = jdbcTemplate.query(sql, rowMapper, params);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    /**
     * Execute a query with named parameters
     */
    public <T> List<T> queryForListWithNamedParams(String sql, Map<String, Object> params, RowMapper<T> rowMapper) {
        return namedParameterJdbcTemplate.query(sql, params, rowMapper);
    }

    /**
     * Execute an update statement
     */
    public int update(String sql, Object... params) {
        return jdbcTemplate.update(sql, params);
    }

    /**
     * Execute an update with named parameters
     */
    public int updateWithNamedParams(String sql, Map<String, Object> params) {
        return namedParameterJdbcTemplate.update(sql, params);
    }

    /**
     * Execute an insert and return generated key
     */
    public Long insert(String sql, Object... params) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        return key != null ? key.longValue() : null;
    }

    /**
     * Execute multiple updates in a batch
     */
    public int[] batchUpdate(String sql, List<Object[]> batchArgs) {
        return jdbcTemplate.batchUpdate(sql, batchArgs);
    }

    /**
     * Count query
     */
    public long count(String sql, Object... params) {
        Long count = jdbcTemplate.queryForObject(sql, Long.class, params);
        return count != null ? count : 0L;
    }

    /**
     * Check if record exists
     */
    public boolean exists(String sql, Object... params) {
        return count(sql, params) > 0;
    }

    /**
     * Get the JdbcTemplate for advanced operations
     */
    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    /**
     * Get the NamedParameterJdbcTemplate for named parameter operations
     */
    public NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
        return namedParameterJdbcTemplate;
    }
}