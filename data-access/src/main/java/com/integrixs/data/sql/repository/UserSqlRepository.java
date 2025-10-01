package com.integrixs.data.sql.repository;

import com.integrixs.data.model.User;
import com.integrixs.data.repository.UserRepository;
import com.integrixs.data.sql.core.BaseSqlRepository;
import com.integrixs.data.sql.core.ResultSetMapper;
import com.integrixs.data.sql.core.SqlQueryExecutor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * SQL implementation of UserRepository using native queries.
 * This repository provides all user data access operations using JDBC.
 */
@Repository("userSqlRepository")
public class UserSqlRepository extends BaseSqlRepository<User, UUID> implements UserRepository {

    private static final String TABLE_NAME = "users";
    private static final String ID_COLUMN = "id";

    /**
     * Row mapper for User entity
     */
    private static final RowMapper<User> USER_ROW_MAPPER = new RowMapper<User>() {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User();
            user.setId(ResultSetMapper.getUUID(rs, "id"));
            user.setUsername(ResultSetMapper.getString(rs, "username"));
            user.setEmail(ResultSetMapper.getString(rs, "email"));
            user.setPasswordHash(ResultSetMapper.getString(rs, "password_hash"));
            user.setFirstName(ResultSetMapper.getString(rs, "first_name"));
            user.setLastName(ResultSetMapper.getString(rs, "last_name"));
            user.setRoleId(ResultSetMapper.getUUID(rs, "role_id"));
            user.setRole(ResultSetMapper.getString(rs, "role"));
            user.setStatus(ResultSetMapper.getString(rs, "status"));
            user.setPermissions(ResultSetMapper.getString(rs, "permissions"));
            user.setTenantId(ResultSetMapper.getUUID(rs, "tenant_id"));
            user.setCreatedAt(ResultSetMapper.getLocalDateTime(rs, "created_at"));
            user.setUpdatedAt(ResultSetMapper.getLocalDateTime(rs, "updated_at"));
            user.setLastLoginAt(ResultSetMapper.getLocalDateTime(rs, "last_login_at"));
            user.setPasswordResetToken(ResultSetMapper.getString(rs, "password_reset_token"));
            user.setPasswordResetExpiresAt(ResultSetMapper.getLocalDateTime(rs, "password_reset_expires_at"));
            return user;
        }
    };

    public UserSqlRepository(SqlQueryExecutor sqlQueryExecutor) {
        super(sqlQueryExecutor, TABLE_NAME, ID_COLUMN, USER_ROW_MAPPER);
    }

    /**
     * Find user by username
     */
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        return sqlQueryExecutor.queryForObject(sql, USER_ROW_MAPPER, username);
    }

    /**
     * Find user by email
     */
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        return sqlQueryExecutor.queryForObject(sql, USER_ROW_MAPPER, email);
    }

    /**
     * Check if username exists
     */
    public boolean existsByUsername(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        return sqlQueryExecutor.exists(sql, username);
    }

    /**
     * Check if email exists
     */
    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        return sqlQueryExecutor.exists(sql, email);
    }

    /**
     * Count users by role
     */
    public long countByRole(String role) {
        String sql = "SELECT COUNT(*) FROM users WHERE role = ?";
        return sqlQueryExecutor.count(sql, role);
    }

    /**
     * Find users by tenant ID
     */
    public List<User> findByTenantId(UUID tenantId) {
        String sql = "SELECT * FROM users WHERE tenant_id = ?";
        return sqlQueryExecutor.queryForList(sql, USER_ROW_MAPPER, tenantId);
    }

    /**
     * Count users by tenant ID
     */
    public long countByTenantId(UUID tenantId) {
        String sql = "SELECT COUNT(*) FROM users WHERE tenant_id = ?";
        return sqlQueryExecutor.count(sql, tenantId);
    }

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            user.setId(generateId());
        }

        // Set timestamps
        LocalDateTime now = LocalDateTime.now();
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(now);
        }
        if (user.getUpdatedAt() == null) {
            user.setUpdatedAt(now);
        }

        String sql = buildInsertSql(
            "id", "username", "email", "password_hash", "first_name", "last_name",
            "role_id", "role", "status", "permissions", "tenant_id",
            "created_at", "updated_at", "last_login_at",
            "password_reset_token", "password_reset_expires_at"
        );

        sqlQueryExecutor.update(sql,
            user.getId(), user.getUsername(), user.getEmail(), user.getPasswordHash(),
            user.getFirstName(), user.getLastName(), user.getRoleId(), user.getRole(),
            user.getStatus(), user.getPermissions(), user.getTenantId(),
            ResultSetMapper.toTimestamp(user.getCreatedAt()),
            ResultSetMapper.toTimestamp(user.getUpdatedAt()),
            ResultSetMapper.toTimestamp(user.getLastLoginAt()),
            user.getPasswordResetToken(),
            ResultSetMapper.toTimestamp(user.getPasswordResetExpiresAt())
        );

        return user;
    }

    @Override
    public User update(User user) {
        // Update timestamp
        user.setUpdatedAt(LocalDateTime.now());

        String sql = buildUpdateSql(
            "username", "email", "password_hash", "first_name", "last_name",
            "role_id", "role", "status", "permissions", "tenant_id",
            "updated_at", "last_login_at",
            "password_reset_token", "password_reset_expires_at"
        );

        sqlQueryExecutor.update(sql,
            user.getUsername(), user.getEmail(), user.getPasswordHash(),
            user.getFirstName(), user.getLastName(), user.getRoleId(), user.getRole(),
            user.getStatus(), user.getPermissions(), user.getTenantId(),
            ResultSetMapper.toTimestamp(user.getUpdatedAt()),
            ResultSetMapper.toTimestamp(user.getLastLoginAt()),
            user.getPasswordResetToken(),
            ResultSetMapper.toTimestamp(user.getPasswordResetExpiresAt()),
            user.getId()
        );

        return user;
    }

    /**
     * Update last login timestamp
     */
    public void updateLastLogin(UUID userId) {
        String sql = "UPDATE users SET last_login_at = ? WHERE id = ?";
        sqlQueryExecutor.update(sql, ResultSetMapper.toTimestamp(LocalDateTime.now()), userId);
    }

    /**
     * Find users by role and status
     */
    public List<User> findByRoleAndStatus(String role, String status) {
        String sql = "SELECT * FROM users WHERE role = ? AND status = ?";
        return sqlQueryExecutor.queryForList(sql, USER_ROW_MAPPER, role, status);
    }

    /**
     * Find active users
     */
    public List<User> findActiveUsers() {
        String sql = "SELECT * FROM users WHERE status = 'active' ORDER BY username";
        return sqlQueryExecutor.queryForList(sql, USER_ROW_MAPPER);
    }

    /**
     * Find all users with pagination
     */
    public Page<User> findAll(Pageable pageable) {
        String countSql = "SELECT COUNT(*) FROM " + TABLE_NAME;
        long total = sqlQueryExecutor.count(countSql);

        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM " + TABLE_NAME);

        // Add sorting if specified
        if (pageable.getSort().isSorted()) {
            sqlBuilder.append(" ORDER BY ");
            pageable.getSort().forEach(order -> {
                sqlBuilder.append(order.getProperty()).append(" ").append(order.getDirection().name()).append(", ");
            });
            // Remove trailing comma
            sqlBuilder.setLength(sqlBuilder.length() - 2);
        } else {
            sqlBuilder.append(" ORDER BY username");
        }

        sqlBuilder.append(" LIMIT ").append(pageable.getPageSize()).append(" OFFSET ").append(pageable.getOffset());

        List<User> content = sqlQueryExecutor.queryForList(sqlBuilder.toString(), USER_ROW_MAPPER);
        return new PageImpl<>(content, pageable, total);
    }

    /**
     * Delete a user
     */
    public void delete(User user) {
        deleteById(user.getId());
    }

    /**
     * Delete users by tenant ID
     */
    public void deleteByTenantId(UUID tenantId) {
        String sql = "DELETE FROM users WHERE tenant_id = ?";
        sqlQueryExecutor.update(sql, tenantId);
    }

    /**
     * Find users by status
     */
    public List<User> findByStatus(String status) {
        String sql = "SELECT * FROM users WHERE status = ? ORDER BY username";
        return sqlQueryExecutor.queryForList(sql, USER_ROW_MAPPER, status);
    }

    /**
     * Find users by role
     */
    public List<User> findByRole(String role) {
        String sql = "SELECT * FROM users WHERE role = ? ORDER BY username";
        return sqlQueryExecutor.queryForList(sql, USER_ROW_MAPPER, role);
    }

    /**
     * Find users by status and role with pagination
     */
    public Page<User> findByStatusAndRole(String status, String role, Pageable pageable) {
        String countSql = "SELECT COUNT(*) FROM users WHERE status = ? AND role = ?";
        long total = sqlQueryExecutor.count(countSql, status, role);

        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM users WHERE status = ? AND role = ?");

        // Add sorting if specified
        if (pageable.getSort().isSorted()) {
            sqlBuilder.append(" ORDER BY ");
            pageable.getSort().forEach(order -> {
                sqlBuilder.append(order.getProperty()).append(" ").append(order.getDirection().name()).append(", ");
            });
            // Remove trailing comma
            sqlBuilder.setLength(sqlBuilder.length() - 2);
        } else {
            sqlBuilder.append(" ORDER BY username");
        }

        sqlBuilder.append(" LIMIT ").append(pageable.getPageSize()).append(" OFFSET ").append(pageable.getOffset());

        List<User> content = sqlQueryExecutor.queryForList(sqlBuilder.toString(), USER_ROW_MAPPER, status, role);
        return new PageImpl<>(content, pageable, total);
    }

    /**
     * Count users by status
     */
    public long countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM users WHERE status = ?";
        return sqlQueryExecutor.count(sql, status);
    }
}