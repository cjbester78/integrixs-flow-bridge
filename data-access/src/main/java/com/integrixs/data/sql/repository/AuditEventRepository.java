package com.integrixs.data.sql.repository;

import com.integrixs.data.model.AuditEvent;
import com.integrixs.data.sql.core.BaseSqlRepository;
import com.integrixs.data.sql.core.ResultSetMapper;
import com.integrixs.data.sql.core.SqlQueryExecutor;
import com.integrixs.data.sql.core.SqlPaginationHelper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * SQL implementation of AuditEventRepository using native queries.
 */
@Repository
public class AuditEventRepository extends BaseSqlRepository<AuditEvent, UUID> {

    private static final String TABLE_NAME = "audit_events";
    private static final String ID_COLUMN = "id";

    /**
     * Row mapper for AuditEvent entity
     */
    private static final RowMapper<AuditEvent> AUDIT_EVENT_ROW_MAPPER = new RowMapper<AuditEvent>() {
        @Override
        public AuditEvent mapRow(ResultSet rs, int rowNum) throws SQLException {
            AuditEvent event = new AuditEvent();
            event.setId(ResultSetMapper.getUUID(rs, "id"));
            event.setUsername(ResultSetMapper.getString(rs, "username"));

            String eventTypeStr = ResultSetMapper.getString(rs, "event_type");
            if (eventTypeStr != null) {
                event.setEventType(AuditEvent.AuditEventType.valueOf(eventTypeStr));
            }

            String categoryStr = ResultSetMapper.getString(rs, "category");
            if (categoryStr != null) {
                event.setCategory(AuditEvent.AuditCategory.valueOf(categoryStr));
            }

            String outcomeStr = ResultSetMapper.getString(rs, "outcome");
            if (outcomeStr != null) {
                event.setOutcome(AuditEvent.AuditOutcome.valueOf(outcomeStr));
            }

            event.setEntityType(ResultSetMapper.getString(rs, "entity_type"));
            event.setEntityId(ResultSetMapper.getString(rs, "entity_id"));
            event.setDetails(ResultSetMapper.getString(rs, "details"));
            event.setIpAddress(ResultSetMapper.getString(rs, "ip_address"));
            event.setUserAgent(ResultSetMapper.getString(rs, "user_agent"));
            event.setTimestamp(ResultSetMapper.getLocalDateTime(rs, "timestamp"));
            event.setCreatedAt(ResultSetMapper.getLocalDateTime(rs, "created_at"));
            event.setAction(ResultSetMapper.getString(rs, "action"));
            event.setErrorMessage(ResultSetMapper.getString(rs, "error_message"));
            event.setCorrelationId(ResultSetMapper.getString(rs, "correlation_id"));
            event.setEntityName(ResultSetMapper.getString(rs, "entity_name"));

            return event;
        }
    };

    public AuditEventRepository(SqlQueryExecutor sqlQueryExecutor) {
        super(sqlQueryExecutor, TABLE_NAME, ID_COLUMN, AUDIT_EVENT_ROW_MAPPER);
    }

    public Page<AuditEvent> searchEvents(String username, AuditEvent.AuditEventType eventType,
                                       AuditEvent.AuditCategory category, AuditEvent.AuditOutcome outcome,
                                       String entityType, LocalDateTime startTime, LocalDateTime endTime,
                                       Pageable pageable) {
        StringBuilder sql = new StringBuilder("SELECT * FROM " + TABLE_NAME + " WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (username != null && !username.isEmpty()) {
            sql.append(" AND username = ?");
            params.add(username);
        }

        if (eventType != null) {
            sql.append(" AND event_type = ?");
            params.add(eventType.toString());
        }

        if (category != null) {
            sql.append(" AND category = ?");
            params.add(category.toString());
        }

        if (outcome != null) {
            sql.append(" AND outcome = ?");
            params.add(outcome.toString());
        }

        if (entityType != null && !entityType.isEmpty()) {
            sql.append(" AND entity_type = ?");
            params.add(entityType);
        }

        if (startTime != null) {
            sql.append(" AND timestamp >= ?");
            params.add(ResultSetMapper.toTimestamp(startTime));
        }

        if (endTime != null) {
            sql.append(" AND timestamp <= ?");
            params.add(ResultSetMapper.toTimestamp(endTime));
        }

        // Count query
        String countSql = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE 1=1" +
                         sql.toString().substring(sql.toString().indexOf(" WHERE 1=1") + 10);
        long total = sqlQueryExecutor.count(countSql, params.toArray());

        // Add pagination
        sql.append(SqlPaginationHelper.buildOrderByClause(pageable.getSort()));
        sql.append(SqlPaginationHelper.buildPaginationClause(pageable));

        List<AuditEvent> events = sqlQueryExecutor.queryForList(sql.toString(), AUDIT_EVENT_ROW_MAPPER, params.toArray());

        return new PageImpl<>(events, pageable, total);
    }

    public List<AuditEvent> findByUsername(String username) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE username = ? ORDER BY timestamp DESC";
        return sqlQueryExecutor.queryForList(sql, AUDIT_EVENT_ROW_MAPPER, username);
    }

    public List<AuditEvent> findByEntityTypeAndEntityId(String entityType, String entityId) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE entity_type = ? AND entity_id = ? ORDER BY timestamp DESC";
        return sqlQueryExecutor.queryForList(sql, AUDIT_EVENT_ROW_MAPPER, entityType, entityId);
    }

    public List<AuditEvent> findByTimestampBetween(LocalDateTime start, LocalDateTime end) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE timestamp BETWEEN ? AND ? ORDER BY timestamp DESC";
        return sqlQueryExecutor.queryForList(sql, AUDIT_EVENT_ROW_MAPPER,
                                           ResultSetMapper.toTimestamp(start),
                                           ResultSetMapper.toTimestamp(end));
    }

    public List<AuditEvent> findByCorrelationIdOrderByEventTimestamp(String correlationId) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE correlation_id = ? ORDER BY timestamp DESC";
        return sqlQueryExecutor.queryForList(sql, AUDIT_EVENT_ROW_MAPPER, correlationId);
    }

    public void deleteByEventTimestampBefore(LocalDateTime cutoffTime) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE timestamp < ?";
        sqlQueryExecutor.update(sql, ResultSetMapper.toTimestamp(cutoffTime));
    }

    public Page<AuditEvent> findByEventTimestampBetweenOrderByEventTimestampDesc(
            Instant startTime, Instant endTime, Pageable pageable) {
        LocalDateTime start = LocalDateTime.ofInstant(startTime, ZoneId.systemDefault());
        LocalDateTime end = LocalDateTime.ofInstant(endTime, ZoneId.systemDefault());

        String countSql = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE timestamp BETWEEN ? AND ?";
        long total = sqlQueryExecutor.count(countSql,
            ResultSetMapper.toTimestamp(start), ResultSetMapper.toTimestamp(end));

        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE timestamp BETWEEN ? AND ? ORDER BY timestamp DESC";
        sql += " LIMIT " + pageable.getPageSize() + " OFFSET " + pageable.getOffset();

        List<AuditEvent> content = sqlQueryExecutor.queryForList(sql, AUDIT_EVENT_ROW_MAPPER,
            ResultSetMapper.toTimestamp(start), ResultSetMapper.toTimestamp(end));

        return new PageImpl<>(content, pageable, total);
    }

    public List<Object[]> countEventsByType(Instant startTime, Instant endTime) {
        LocalDateTime start = LocalDateTime.ofInstant(startTime, ZoneId.systemDefault());
        LocalDateTime end = LocalDateTime.ofInstant(endTime, ZoneId.systemDefault());

        String sql = "SELECT event_type, COUNT(*) FROM " + TABLE_NAME +
                    " WHERE timestamp BETWEEN ? AND ? GROUP BY event_type";

        return sqlQueryExecutor.queryForList(sql, (rs, rowNum) -> new Object[] {
            rs.getString(1), rs.getLong(2)
        }, ResultSetMapper.toTimestamp(start), ResultSetMapper.toTimestamp(end));
    }

    public Page<AuditEvent> findByOutcomeInOrderByEventTimestampDesc(
            List<AuditEvent.AuditOutcome> outcomes, Instant startTime, Instant endTime, Pageable pageable) {
        LocalDateTime start = LocalDateTime.ofInstant(startTime, ZoneId.systemDefault());
        LocalDateTime end = LocalDateTime.ofInstant(endTime, ZoneId.systemDefault());

        String outcomeList = outcomes.stream()
            .map(o -> "'" + o.toString() + "'")
            .collect(Collectors.joining(","));

        String countSql = "SELECT COUNT(*) FROM " + TABLE_NAME +
                         " WHERE outcome IN (" + outcomeList + ") AND timestamp BETWEEN ? AND ?";
        long total = sqlQueryExecutor.count(countSql,
            ResultSetMapper.toTimestamp(start), ResultSetMapper.toTimestamp(end));

        String sql = "SELECT * FROM " + TABLE_NAME +
                    " WHERE outcome IN (" + outcomeList + ") AND timestamp BETWEEN ? AND ? ORDER BY timestamp DESC";
        sql += " LIMIT " + pageable.getPageSize() + " OFFSET " + pageable.getOffset();

        List<AuditEvent> content = sqlQueryExecutor.queryForList(sql, AUDIT_EVENT_ROW_MAPPER,
            ResultSetMapper.toTimestamp(start), ResultSetMapper.toTimestamp(end));

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public AuditEvent save(AuditEvent event) {
        if (event.getId() == null) {
            event.setId(generateId());
        }

        boolean exists = existsById(event.getId());

        if (!exists) {
            return insert(event);
        } else {
            return update(event);
        }
    }

    private AuditEvent insert(AuditEvent event) {
        String sql = "INSERT INTO " + TABLE_NAME + " (" +
                     "id, username, event_type, category, outcome, entity_type, entity_id, " +
                     "details, ip_address, user_agent, timestamp, created_at, " +
                     "action, error_message, correlation_id, entity_name" +
                     ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        if (event.getTimestamp() == null) {
            event.setTimestamp(LocalDateTime.now());
        }
        if (event.getCreatedAt() == null) {
            event.setCreatedAt(LocalDateTime.now());
        }

        sqlQueryExecutor.update(sql,
            event.getId(),
            event.getUsername(),
            event.getEventType() != null ? event.getEventType().toString() : null,
            event.getCategory() != null ? event.getCategory().toString() : null,
            event.getOutcome() != null ? event.getOutcome().toString() : null,
            event.getEntityType(),
            event.getEntityId(),
            event.getDetails(),
            event.getIpAddress(),
            event.getUserAgent(),
            ResultSetMapper.toTimestamp(event.getTimestamp()),
            ResultSetMapper.toTimestamp(event.getCreatedAt()),
            event.getAction(),
            event.getErrorMessage(),
            event.getCorrelationId(),
            event.getEntityName()
        );

        return event;
    }

    @Override
    public AuditEvent update(AuditEvent event) {
        // Audit events are typically immutable, so update might not be needed
        return event;
    }

    public void deleteOlderThan(LocalDateTime cutoffDate) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE timestamp < ?";
        sqlQueryExecutor.update(sql, ResultSetMapper.toTimestamp(cutoffDate));
    }

    public Page<AuditEvent> findByUsernameAndTimestampBetween(String username, Instant start, Instant end, Pageable pageable) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE username = ? AND timestamp BETWEEN ? AND ?";

        // Convert Instant to LocalDateTime for database query
        LocalDateTime startTime = start != null ? LocalDateTime.ofInstant(start, java.time.ZoneId.systemDefault()) : null;
        LocalDateTime endTime = end != null ? LocalDateTime.ofInstant(end, java.time.ZoneId.systemDefault()) : null;

        // Count query
        String countSql = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE username = ? AND timestamp BETWEEN ? AND ?";
        long total = sqlQueryExecutor.count(countSql, username,
                                          ResultSetMapper.toTimestamp(startTime),
                                          ResultSetMapper.toTimestamp(endTime));

        // Add pagination
        sql += SqlPaginationHelper.buildOrderByClause(pageable.getSort());
        sql += SqlPaginationHelper.buildPaginationClause(pageable);

        List<AuditEvent> events = sqlQueryExecutor.queryForList(sql, AUDIT_EVENT_ROW_MAPPER,
                                                               username,
                                                               ResultSetMapper.toTimestamp(startTime),
                                                               ResultSetMapper.toTimestamp(endTime));

        return new PageImpl<>(events, pageable, total);
    }
}