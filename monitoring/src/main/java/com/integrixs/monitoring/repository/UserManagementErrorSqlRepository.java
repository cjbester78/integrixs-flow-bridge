package com.integrixs.monitoring.repository;

import com.integrixs.data.model.SystemLog;
import com.integrixs.monitoring.model.UserManagementError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * SQL implementation of UserManagementErrorRepository using native queries.
 */
@Repository
public class UserManagementErrorSqlRepository implements UserManagementErrorRepository {

    private static final String TABLE_NAME = "user_management_errors";
    private static final String ID_COLUMN = "id";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Row mapper for UserManagementError entity
     */
    private static final RowMapper<UserManagementError> ERROR_ROW_MAPPER = new RowMapper<UserManagementError>() {
        @Override
        public UserManagementError mapRow(ResultSet rs, int rowNum) throws SQLException {
            UserManagementError error = new UserManagementError();
            error.setId(UUID.fromString(rs.getString("id")));
            error.setAction(rs.getString("action"));
            error.setDescription(rs.getString("description"));
            error.setPayload(rs.getString("payload"));

            Timestamp createdAt = rs.getTimestamp("created_at");
            error.setCreatedAt(createdAt != null ? createdAt.toLocalDateTime() : null);

            // Map the SystemLog reference
            String logIdStr = rs.getString("log_id");
            UUID logId = logIdStr != null ? UUID.fromString(logIdStr) : null;
            if (logId != null) {
                SystemLog log = new SystemLog();
                log.setId(logId);
                error.setLog(log);
            }

            return error;
        }
    };

    public UserManagementErrorSqlRepository() {
        // JdbcTemplate will be autowired
    }

    @Override
    public UserManagementError save(UserManagementError error) {
        if (error.getId() == null) {
            error.setId(UUID.randomUUID());
        }

        if (error.getCreatedAt() == null) {
            error.setCreatedAt(LocalDateTime.now());
        }

        String sql = "INSERT INTO " + TABLE_NAME + " (id, action, description, payload, log_id, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?) " +
                     "ON CONFLICT (id) DO UPDATE SET " +
                     "action = EXCLUDED.action, " +
                     "description = EXCLUDED.description, " +
                     "payload = EXCLUDED.payload, " +
                     "log_id = EXCLUDED.log_id";

        jdbcTemplate.update(sql,
            error.getId(),
            error.getAction(),
            error.getDescription(),
            error.getPayload(),
            error.getLog() != null ? error.getLog().getId() : null,
            Timestamp.valueOf(error.getCreatedAt())
        );

        return error;
    }

    @Override
    public Optional<UserManagementError> findById(UUID id) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE " + ID_COLUMN + " = ?";
        List<UserManagementError> results = jdbcTemplate.query(sql, ERROR_ROW_MAPPER, id);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public List<UserManagementError> findAll() {
        String sql = "SELECT * FROM " + TABLE_NAME + " ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, ERROR_ROW_MAPPER);
    }

    @Override
    public List<UserManagementError> findByAction(String action) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE action = ? ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, ERROR_ROW_MAPPER, action);
    }

    @Override
    public void deleteById(UUID id) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE " + ID_COLUMN + " = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM " + TABLE_NAME;
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0L;
    }
}