package com.integrixs.data.sql.repository;

import com.integrixs.data.model.Alert;
import com.integrixs.data.model.Alert.AlertStatus;
import com.integrixs.data.model.Alert.SourceType;
import com.integrixs.data.model.AlertRule;
import com.integrixs.data.model.AlertRule.AlertSeverity;
import com.integrixs.data.sql.mapper.AlertRowMapper;
import com.integrixs.data.sql.core.SqlQueryExecutor;
import com.integrixs.data.sql.core.SqlPaginationHelper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import com.integrixs.data.sql.core.ResultSetMapper;

/**
 * SQL repository implementation for Alert entity.
 * Provides direct SQL operations using native SQL.
 */
@Repository
public class AlertSqlRepository {

    private static final String TABLE_NAME = "alerts";

    private final SqlQueryExecutor sqlQueryExecutor;
    private final AlertRowMapper rowMapper = new AlertRowMapper();

    public AlertSqlRepository(SqlQueryExecutor sqlQueryExecutor) {
        this.sqlQueryExecutor = sqlQueryExecutor;
    }

    public Alert save(Alert entity) {
        if (entity.getId() == null) {
            entity.setId(UUID.randomUUID());
            String sql = "INSERT INTO " + TABLE_NAME + " (id, created_at, updated_at) VALUES (?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
            sqlQueryExecutor.update(sql, entity.getId());
        } else {
            update(entity);
        }
        return entity;
    }

    public Optional<Alert> findById(UUID id) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE id = ?";
        return sqlQueryExecutor.queryForObject(sql, rowMapper, id);
    }

    public List<Alert> findAll() {
        String sql = "SELECT * FROM " + TABLE_NAME + " ORDER BY created_at DESC";
        return sqlQueryExecutor.queryForList(sql, rowMapper);
    }

    public Page<Alert> findAll(Pageable pageable) {
        String countSql = "SELECT COUNT(*) FROM " + TABLE_NAME;
        long total = sqlQueryExecutor.count(countSql);

        String dataSql = "SELECT * FROM " + TABLE_NAME + " ORDER BY created_at DESC LIMIT ? OFFSET ?";
        List<Alert> data = sqlQueryExecutor.queryForList(dataSql, rowMapper,
                                                            pageable.getPageSize(),
                                                            pageable.getOffset());

        return new PageImpl<>(data, pageable, total);
    }

    public Alert update(Alert entity) {
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

    public Optional<Alert> findByAlertId(String alertId) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE alert_id = ?";
        return sqlQueryExecutor.queryForObject(sql, rowMapper, alertId);
    }

    public Page<Alert> findByStatus(AlertStatus status, Pageable pageable) {
        String countSql = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE status = ?";
        long total = sqlQueryExecutor.count(countSql, status.toString());

        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE status = ?";
        sql += SqlPaginationHelper.buildOrderByClause(pageable.getSort());
        sql += SqlPaginationHelper.buildPaginationClause(pageable);

        List<Alert> alerts = sqlQueryExecutor.queryForList(sql, rowMapper, status.toString());
        return new PageImpl<>(alerts, pageable, total);
    }

    public Page<Alert> findBySeverity(AlertSeverity severity, Pageable pageable) {
        String countSql = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE severity = ?";
        long total = sqlQueryExecutor.count(countSql, severity.toString());

        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE severity = ?";
        sql += SqlPaginationHelper.buildOrderByClause(pageable.getSort());
        sql += SqlPaginationHelper.buildPaginationClause(pageable);

        List<Alert> alerts = sqlQueryExecutor.queryForList(sql, rowMapper, severity.toString());
        return new PageImpl<>(alerts, pageable, total);
    }

    public Page<Alert> findBySourceTypeAndSourceId(SourceType sourceType, String sourceId, Pageable pageable) {
        String countSql = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE source_type = ? AND source_id = ?";
        long total = sqlQueryExecutor.count(countSql, sourceType.toString(), sourceId);

        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE source_type = ? AND source_id = ?";
        sql += SqlPaginationHelper.buildOrderByClause(pageable.getSort());
        sql += SqlPaginationHelper.buildPaginationClause(pageable);

        List<Alert> alerts = sqlQueryExecutor.queryForList(sql, rowMapper, sourceType.toString(), sourceId);
        return new PageImpl<>(alerts, pageable, total);
    }

    public Page<Alert> findUnresolvedAlerts(Pageable pageable) {
        String countSql = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE status IN ('NEW', 'ACKNOWLEDGED')";
        long total = sqlQueryExecutor.count(countSql);

        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE status IN ('NEW', 'ACKNOWLEDGED')";
        sql += SqlPaginationHelper.buildOrderByClause(pageable.getSort());
        sql += SqlPaginationHelper.buildPaginationClause(pageable);

        List<Alert> alerts = sqlQueryExecutor.queryForList(sql, rowMapper);
        return new PageImpl<>(alerts, pageable, total);
    }

    public List<Alert> findUnacknowledgedCriticalAlerts() {
        String sql = "SELECT * FROM " + TABLE_NAME +
                    " WHERE status = 'NEW' AND severity = 'CRITICAL' ORDER BY triggered_at DESC";
        return sqlQueryExecutor.queryForList(sql, rowMapper);
    }

    public Map<String, Map<String, Long>> countByStatusAndSeveritySince(LocalDateTime since) {
        String sql = "SELECT status, severity, COUNT(*) as count FROM " + TABLE_NAME +
                    " WHERE triggered_at >= ? GROUP BY status, severity";

        List<Map<String, Object>> results = sqlQueryExecutor.queryForList(sql,
            (rs, rowNum) -> {
                Map<String, Object> map = new HashMap<>();
                map.put("status", rs.getString("status"));
                map.put("severity", rs.getString("severity"));
                map.put("count", rs.getLong("count"));
                return map;
            },
            java.sql.Timestamp.valueOf(since)
        );

        Map<String, Map<String, Long>> stats = new HashMap<>();
        for (Map<String, Object> row : results) {
            String status = (String) row.get("status");
            String severity = (String) row.get("severity");
            Long count = (Long) row.get("count");

            stats.computeIfAbsent(status, k -> new HashMap<>()).put(severity, count);
        }

        return stats;
    }

    public List<Alert> findAlertsNeedingEscalation(LocalDateTime threshold) {
        String sql = "SELECT * FROM " + TABLE_NAME +
                    " WHERE status = 'ACTIVE' AND escalated = false " +
                    " AND triggered_at <= ? ORDER BY triggered_at";
        return sqlQueryExecutor.queryForList(sql, rowMapper, ResultSetMapper.toTimestamp(threshold));
    }

    public List<Alert> findSuppressedAlertsToUnsuppress(LocalDateTime threshold) {
        String sql = "SELECT * FROM " + TABLE_NAME +
                    " WHERE status = 'SUPPRESSED' AND suppressed_until <= ? " +
                    " ORDER BY triggered_at";
        return sqlQueryExecutor.queryForList(sql, rowMapper, ResultSetMapper.toTimestamp(threshold));
    }

    public List<Alert> findRecentSimilarAlerts(AlertRule rule, String sourceId, LocalDateTime since) {
        String sql = "SELECT * FROM " + TABLE_NAME +
                    " WHERE alert_rule_id = ? AND source_id = ? " +
                    " AND triggered_at >= ? ORDER BY triggered_at DESC";
        return sqlQueryExecutor.queryForList(sql, rowMapper,
            rule.getId(), sourceId, ResultSetMapper.toTimestamp(since));
    }
}
