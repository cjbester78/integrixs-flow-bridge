package com.integrixs.data.sql.repository;

import com.integrixs.data.model.User;
import com.integrixs.data.model.UserSession;
import com.integrixs.data.sql.core.BaseSqlRepository;
import com.integrixs.data.sql.core.ResultSetMapper;
import com.integrixs.data.sql.core.SqlQueryExecutor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * SQL implementation of UserSessionRepository using native queries.
 */
@Repository
public class UserSessionSqlRepository extends BaseSqlRepository<UserSession, UUID> {

    private static final String TABLE_NAME = "user_sessions";
    private static final String ID_COLUMN = "id";

    /**
     * Row mapper for UserSession entity
     */
    private static final RowMapper<UserSession> USER_SESSION_ROW_MAPPER = new RowMapper<UserSession>() {
        @Override
        public UserSession mapRow(ResultSet rs, int rowNum) throws SQLException {
            UserSession session = new UserSession();
            session.setId(ResultSetMapper.getUUID(rs, "id"));
            session.setRefreshToken(ResultSetMapper.getString(rs, "refresh_token"));
            session.setExpiresAt(ResultSetMapper.getLocalDateTime(rs, "expires_at"));
            session.setIpAddress(ResultSetMapper.getString(rs, "ip_address"));
            session.setUserAgent(ResultSetMapper.getString(rs, "user_agent"));
            session.setCreatedAt(ResultSetMapper.getLocalDateTime(rs, "created_at"));
            session.setLastUsedAt(ResultSetMapper.getLocalDateTime(rs, "last_used_at"));

            // Map user reference
            UUID userId = ResultSetMapper.getUUID(rs, "user_id");
            if (userId != null) {
                User user = new User();
                user.setId(userId);
                session.setUser(user);
            }

            return session;
        }
    };

    public UserSessionSqlRepository(SqlQueryExecutor sqlQueryExecutor) {
        super(sqlQueryExecutor, TABLE_NAME, ID_COLUMN, USER_SESSION_ROW_MAPPER);
    }

    public List<UserSession> findByUserAndExpiresAtAfter(User user, LocalDateTime expiryTime) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE user_id = ? AND expires_at > ?";
        return sqlQueryExecutor.queryForList(sql, USER_SESSION_ROW_MAPPER, user.getId(), ResultSetMapper.toTimestamp(expiryTime));
    }

    public Optional<UserSession> findByRefreshToken(String refreshToken) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE refresh_token = ?";
        List<UserSession> results = sqlQueryExecutor.queryForList(sql, USER_SESSION_ROW_MAPPER, refreshToken);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public void deleteByUser(User user) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE user_id = ?";
        sqlQueryExecutor.update(sql, user.getId());
    }

    public int updateLastActivity(UUID sessionId, LocalDateTime lastUsedAt) {
        String sql = "UPDATE " + TABLE_NAME + " SET last_used_at = ? WHERE id = ?";
        return sqlQueryExecutor.update(sql, ResultSetMapper.toTimestamp(lastUsedAt), sessionId);
    }

    @Override
    public UserSession save(UserSession session) {
        if (session.getId() == null) {
            session.setId(generateId());
        }

        boolean exists = existsById(session.getId());

        if (!exists) {
            return insert(session);
        } else {
            return update(session);
        }
    }

    private UserSession insert(UserSession session) {
        String sql = "INSERT INTO " + TABLE_NAME + " (" +
                     "id, user_id, refresh_token, expires_at, ip_address, user_agent, " +
                     "created_at, last_used_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        if (session.getCreatedAt() == null) {
            session.setCreatedAt(LocalDateTime.now());
        }
        if (session.getLastUsedAt() == null) {
            session.setLastUsedAt(session.getCreatedAt());
        }

        sqlQueryExecutor.update(sql,
            session.getId(),
            session.getUser() != null ? session.getUser().getId() : null,
            session.getRefreshToken(),
            ResultSetMapper.toTimestamp(session.getExpiresAt()),
            session.getIpAddress(),
            session.getUserAgent(),
            ResultSetMapper.toTimestamp(session.getCreatedAt()),
            ResultSetMapper.toTimestamp(session.getLastUsedAt())
        );

        return session;
    }

    @Override
    public UserSession update(UserSession session) {
        String sql = "UPDATE " + TABLE_NAME + " SET " +
                     "expires_at = ?, ip_address = ?, user_agent = ?, last_used_at = ? " +
                     "WHERE id = ?";

        session.setLastUsedAt(LocalDateTime.now());

        sqlQueryExecutor.update(sql,
            ResultSetMapper.toTimestamp(session.getExpiresAt()),
            session.getIpAddress(),
            session.getUserAgent(),
            ResultSetMapper.toTimestamp(session.getLastUsedAt()),
            session.getId()
        );

        return session;
    }

    public void deleteExpiredSessions() {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE expires_at < ?";
        sqlQueryExecutor.update(sql, ResultSetMapper.toTimestamp(LocalDateTime.now()));
    }

    public void deleteByExpiresAtBefore(LocalDateTime expiresAt) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE expires_at < ?";
        sqlQueryExecutor.update(sql, ResultSetMapper.toTimestamp(expiresAt));
    }
}