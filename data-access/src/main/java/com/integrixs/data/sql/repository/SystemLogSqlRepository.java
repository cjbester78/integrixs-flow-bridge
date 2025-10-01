package com.integrixs.data.sql.repository;

import com.integrixs.data.model.SystemLog;
import com.integrixs.data.model.SystemLog.LogLevel;
import com.integrixs.data.sql.core.BaseSqlRepository;
import com.integrixs.data.sql.core.ResultSetMapper;
import com.integrixs.data.sql.core.SqlPaginationHelper;
import com.integrixs.data.sql.core.SqlQueryExecutor;
import com.integrixs.shared.dto.log.LogSearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * SQL implementation of SystemLogRepository using native queries.
 */
@Repository("systemLogSqlRepository")
public class SystemLogSqlRepository extends BaseSqlRepository<SystemLog, UUID> {

    private static final String TABLE_NAME = "system_logs";
    private static final String ID_COLUMN = "id";

    /**
     * Row mapper for SystemLog entity
     */
    private static final RowMapper<SystemLog> SYSTEM_LOG_ROW_MAPPER = new RowMapper<SystemLog>() {
        @Override
        public SystemLog mapRow(ResultSet rs, int rowNum) throws SQLException {
            SystemLog log = new SystemLog();
            log.setId(ResultSetMapper.getUUID(rs, "id"));
            log.setTimestamp(ResultSetMapper.getLocalDateTime(rs, "timestamp"));

            String levelStr = ResultSetMapper.getString(rs, "level");
            if (levelStr != null) {
                log.setLevel(LogLevel.valueOf(levelStr));
            }

            log.setMessage(ResultSetMapper.getString(rs, "message"));
            log.setDetails(ResultSetMapper.getString(rs, "details"));
            log.setSource(ResultSetMapper.getString(rs, "source"));
            log.setSourceId(ResultSetMapper.getString(rs, "source_id"));
            log.setSourceName(ResultSetMapper.getString(rs, "source_name"));
            log.setComponent(ResultSetMapper.getString(rs, "component"));
            log.setComponentId(ResultSetMapper.getString(rs, "component_id"));
            log.setDomainType(ResultSetMapper.getString(rs, "domain_type"));
            log.setDomainReferenceId(ResultSetMapper.getString(rs, "domain_reference_id"));
            log.setUserId(ResultSetMapper.getUUID(rs, "user_id"));
            log.setUsername(ResultSetMapper.getString(rs, "username"));
            log.setCategory(ResultSetMapper.getString(rs, "category"));
            log.setIpAddress(ResultSetMapper.getString(rs, "ip_address"));
            log.setUserAgent(ResultSetMapper.getString(rs, "user_agent"));
            log.setCorrelationId(ResultSetMapper.getString(rs, "correlation_id"));
            log.setSessionId(ResultSetMapper.getString(rs, "session_id"));
            log.setStackTrace(ResultSetMapper.getString(rs, "stack_trace"));
            log.setUrl(ResultSetMapper.getString(rs, "url"));
            log.setBrowser(ResultSetMapper.getString(rs, "browser"));
            log.setOs(ResultSetMapper.getString(rs, "os"));
            log.setDeviceType(ResultSetMapper.getString(rs, "device_type"));
            return log;
        }
    };

    public SystemLogSqlRepository(SqlQueryExecutor sqlQueryExecutor) {
        super(sqlQueryExecutor, TABLE_NAME, ID_COLUMN, SYSTEM_LOG_ROW_MAPPER);
    }

    public List<SystemLog> findBySourceAndLevelAndTimestampAfter(String source, LogLevel level, LocalDateTime timestamp) {
        String sql = "SELECT * FROM system_logs WHERE source = ? AND level = ? AND timestamp > ?";
        return sqlQueryExecutor.queryForList(sql, SYSTEM_LOG_ROW_MAPPER, source, level.name(), ResultSetMapper.toTimestamp(timestamp));
    }

    public long countByComponentIdAndTimestampAfter(String componentId, LocalDateTime date) {
        String sql = "SELECT COUNT(*) FROM system_logs WHERE component_id = ? AND timestamp > ?";
        return sqlQueryExecutor.count(sql, componentId, ResultSetMapper.toTimestamp(date));
    }

    public long countByTimestampAfter(LocalDateTime date) {
        String sql = "SELECT COUNT(*) FROM system_logs WHERE timestamp > ?";
        return sqlQueryExecutor.count(sql, ResultSetMapper.toTimestamp(date));
    }

    public long countByComponentIdAndLevelAndTimestampAfter(String componentId, LogLevel level, LocalDateTime date) {
        String sql = "SELECT COUNT(*) FROM system_logs WHERE component_id = ? AND level = ? AND timestamp > ?";
        return sqlQueryExecutor.count(sql, componentId, level.name(), ResultSetMapper.toTimestamp(date));
    }

    public long countByLevelAndTimestampAfter(LogLevel level, LocalDateTime date) {
        String sql = "SELECT COUNT(*) FROM system_logs WHERE level = ? AND timestamp > ?";
        return sqlQueryExecutor.count(sql, level.name(), ResultSetMapper.toTimestamp(date));
    }

    public Page<SystemLog> findByComponentId(String componentId, Pageable pageable) {
        String baseQuery = "SELECT * FROM system_logs WHERE component_id = ?";
        String countQuery = "SELECT COUNT(*) FROM system_logs WHERE component_id = ?";

        // Get total count
        long total = sqlQueryExecutor.count(countQuery, componentId);

        // Get paginated data
        String query = SqlPaginationHelper.buildPaginatedQuery(baseQuery, pageable);
        List<SystemLog> logs = sqlQueryExecutor.queryForList(query, SYSTEM_LOG_ROW_MAPPER, componentId);

        return new PageImpl<>(logs, pageable, total);
    }

    public List<SystemLog> findByCorrelationId(String correlationId) {
        String sql = "SELECT * FROM system_logs WHERE correlation_id = ?";
        return sqlQueryExecutor.queryForList(sql, SYSTEM_LOG_ROW_MAPPER, correlationId);
    }

    public List<SystemLog> findByMessageContainingAndSourceOrderByTimestampDesc(String message, String source) {
        String sql = "SELECT * FROM system_logs WHERE message LIKE ? AND source = ? ORDER BY timestamp DESC";
        return sqlQueryExecutor.queryForList(sql, SYSTEM_LOG_ROW_MAPPER, "%" + message + "%", source);
    }

    public List<SystemLog> findByMessageContainingOrderByTimestampDesc(String message) {
        String sql = "SELECT * FROM system_logs WHERE message LIKE ? ORDER BY timestamp DESC";
        return sqlQueryExecutor.queryForList(sql, SYSTEM_LOG_ROW_MAPPER, "%" + message + "%");
    }

    public List<SystemLog> findByCorrelationIdAndCategoryOrderByTimestampDesc(String correlationId, String category) {
        String sql = "SELECT * FROM system_logs WHERE correlation_id = ? AND category = ? ORDER BY timestamp DESC";
        return sqlQueryExecutor.queryForList(sql, SYSTEM_LOG_ROW_MAPPER, correlationId, category);
    }

    public List<SystemLog> findByCorrelationIdOrderByTimestampDesc(String correlationId) {
        String sql = "SELECT * FROM system_logs WHERE correlation_id = ? ORDER BY timestamp DESC";
        return sqlQueryExecutor.queryForList(sql, SYSTEM_LOG_ROW_MAPPER, correlationId);
    }

    public List<SystemLog> findByCorrelationIdOrderByTimestamp(String correlationId) {
        String sql = "SELECT * FROM system_logs WHERE correlation_id = ? ORDER BY timestamp";
        return sqlQueryExecutor.queryForList(sql, SYSTEM_LOG_ROW_MAPPER, correlationId);
    }

    public List<SystemLog> findByCategoryOrderByTimestampDesc(String category) {
        String sql = "SELECT * FROM system_logs WHERE category = ? ORDER BY timestamp DESC";
        return sqlQueryExecutor.queryForList(sql, SYSTEM_LOG_ROW_MAPPER, category);
    }

    public Page<SystemLog> findAllByOrderByTimestampDesc(Pageable pageable) {
        String baseQuery = "SELECT * FROM system_logs";
        String countQuery = "SELECT COUNT(*) FROM system_logs";

        String query = baseQuery + " ORDER BY timestamp DESC" + SqlPaginationHelper.buildPaginationClause(pageable);

        List<SystemLog> content = sqlQueryExecutor.queryForList(query, SYSTEM_LOG_ROW_MAPPER);
        long total = sqlQueryExecutor.count(countQuery);

        return SqlPaginationHelper.createPage(content, pageable, total);
    }

    public int deleteByTimestampBefore(LocalDateTime cutoffDate) {
        String sql = "DELETE FROM system_logs WHERE timestamp < ?";
        return sqlQueryExecutor.update(sql, ResultSetMapper.toTimestamp(cutoffDate));
    }

    @Override
    public SystemLog save(SystemLog log) {
        if (log.getId() == null) {
            log.setId(generateId());
        }

        if (log.getTimestamp() == null) {
            log.setTimestamp(LocalDateTime.now());
        }

        String sql = buildInsertSql(
            "id", "timestamp", "level", "message", "details",
            "source", "source_id", "source_name", "component", "component_id",
            "domain_type", "domain_reference_id", "user_id", "username",
            "category", "ip_address", "user_agent", "correlation_id", "session_id",
            "stack_trace", "url", "browser", "os", "device_type"
        );

        sqlQueryExecutor.update(sql,
            log.getId(),
            ResultSetMapper.toTimestamp(log.getTimestamp()),
            log.getLevel() != null ? log.getLevel().name() : null,
            log.getMessage(),
            log.getDetails(),
            log.getSource(),
            log.getSourceId(),
            log.getSourceName(),
            log.getComponent(),
            log.getComponentId(),
            log.getDomainType(),
            log.getDomainReferenceId(),
            log.getUserId(),
            log.getUsername(),
            log.getCategory(),
            log.getIpAddress(),
            log.getUserAgent(),
            log.getCorrelationId(),
            log.getSessionId(),
            log.getStackTrace(),
            log.getUrl(),
            log.getBrowser(),
            log.getOs(),
            log.getDeviceType()
        );

        return log;
    }

    @Override
    public SystemLog update(SystemLog log) {
        // SystemLog is typically not updated, but we provide this for completeness
        String sql = buildUpdateSql(
            "timestamp", "level", "message", "details",
            "source", "source_id", "source_name", "component", "component_id",
            "domain_type", "domain_reference_id", "user_id", "username",
            "category", "ip_address", "user_agent", "correlation_id", "session_id",
            "stack_trace", "url", "browser", "os", "device_type"
        );

        sqlQueryExecutor.update(sql,
            ResultSetMapper.toTimestamp(log.getTimestamp()),
            log.getLevel() != null ? log.getLevel().name() : null,
            log.getMessage(),
            log.getDetails(),
            log.getSource(),
            log.getSourceId(),
            log.getSourceName(),
            log.getComponent(),
            log.getComponentId(),
            log.getDomainType(),
            log.getDomainReferenceId(),
            log.getUserId(),
            log.getUsername(),
            log.getCategory(),
            log.getIpAddress(),
            log.getUserAgent(),
            log.getCorrelationId(),
            log.getSessionId(),
            log.getStackTrace(),
            log.getUrl(),
            log.getBrowser(),
            log.getOs(),
            log.getDeviceType(),
            log.getId()
        );

        return log;
    }

    /**
     * Search logs with complex criteria
     */
    public Page<SystemLog> searchLogs(LogSearchCriteria criteria, Pageable pageable) {
        StringBuilder queryBuilder = new StringBuilder("SELECT * FROM system_logs");
        StringBuilder countQueryBuilder = new StringBuilder("SELECT COUNT(*) FROM system_logs");
        List<Object> params = new ArrayList<>();

        String whereClause = buildWhereClause(criteria, params);
        if (!whereClause.isEmpty()) {
            queryBuilder.append(" ").append(whereClause);
            countQueryBuilder.append(" ").append(whereClause);
        }

        // Add ordering
        String orderBy = " ORDER BY timestamp DESC";
        if (pageable.getSort() != null && pageable.getSort().isSorted()) {
            orderBy = SqlPaginationHelper.buildOrderByClause(pageable.getSort());
        }
        queryBuilder.append(orderBy);

        // Add pagination
        queryBuilder.append(SqlPaginationHelper.buildPaginationClause(pageable));

        // Execute queries
        List<SystemLog> content = sqlQueryExecutor.queryForList(queryBuilder.toString(), SYSTEM_LOG_ROW_MAPPER, params.toArray());
        long total = sqlQueryExecutor.count(countQueryBuilder.toString(), params.toArray());

        return SqlPaginationHelper.createPage(content, pageable, total);
    }

    /**
     * Find logs with multiple filters
     */
    public List<SystemLog> findWithFilters(String level, String source, String userId,
                                          String domainType, String domainReferenceId,
                                          LocalDateTime from, LocalDateTime to) {
        StringBuilder sql = new StringBuilder("SELECT * FROM system_logs WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (level != null) {
            sql.append(" AND level = ?");
            params.add(level);
        }

        if (source != null) {
            sql.append(" AND source = ?");
            params.add(source);
        }

        if (userId != null) {
            sql.append(" AND user_id = ?");
            params.add(UUID.fromString(userId));
        }

        if (domainType != null) {
            sql.append(" AND domain_type = ?");
            params.add(domainType);
        }

        if (domainReferenceId != null) {
            sql.append(" AND domain_reference_id = ?");
            params.add(domainReferenceId);
        }

        if (from != null) {
            sql.append(" AND timestamp >= ?");
            params.add(ResultSetMapper.toTimestamp(from));
        }

        if (to != null) {
            sql.append(" AND timestamp <= ?");
            params.add(ResultSetMapper.toTimestamp(to));
        }

        sql.append(" ORDER BY timestamp DESC");

        return sqlQueryExecutor.queryForList(sql.toString(), SYSTEM_LOG_ROW_MAPPER, params.toArray());
    }

    /**
     * Find logs by flow ID and time range
     */
    public List<SystemLog> findByFlowIdAndTimestampBetween(String flowId, LocalDateTime startTime, LocalDateTime endTime) {
        String sql = "SELECT * FROM system_logs WHERE message LIKE ? AND timestamp BETWEEN ? AND ? ORDER BY timestamp";
        return sqlQueryExecutor.queryForList(sql, SYSTEM_LOG_ROW_MAPPER,
            "%flow: " + flowId + "%",
            ResultSetMapper.toTimestamp(startTime),
            ResultSetMapper.toTimestamp(endTime));
    }

    /**
     * Find logs in time window around a given timestamp
     */
    public List<SystemLog> findByTimestampBetween(LocalDateTime startTime, LocalDateTime endTime, String source, String category) {
        StringBuilder sql = new StringBuilder("SELECT * FROM system_logs WHERE timestamp BETWEEN ? AND ?");
        List<Object> params = new ArrayList<>();
        params.add(ResultSetMapper.toTimestamp(startTime));
        params.add(ResultSetMapper.toTimestamp(endTime));

        if (source != null) {
            sql.append(" AND source = ?");
            params.add(source);
        }

        if (category != null) {
            sql.append(" AND category = ?");
            params.add(category);
        }

        sql.append(" ORDER BY timestamp");

        return sqlQueryExecutor.queryForList(sql.toString(), SYSTEM_LOG_ROW_MAPPER, params.toArray());
    }

    /**
     * Calculate level facets
     */
    public Map<String, Long> calculateLevelFacets(LogSearchCriteria criteria) {
        StringBuilder sql = new StringBuilder("SELECT level, COUNT(*) as count FROM system_logs");
        List<Object> params = new ArrayList<>();

        String whereClause = buildWhereClause(criteria, params);
        if (!whereClause.isEmpty()) {
            sql.append(" ").append(whereClause);
        }

        sql.append(" GROUP BY level");

        Map<String, Long> facets = new HashMap<>();
        List<Map<String, Object>> results = sqlQueryExecutor.queryForList(sql.toString(),
            (rs, rowNum) -> {
                Map<String, Object> row = new HashMap<>();
                row.put("level", rs.getString("level"));
                row.put("count", rs.getLong("count"));
                return row;
            }, params.toArray());

        for (Map<String, Object> row : results) {
            facets.put((String) row.get("level"), (Long) row.get("count"));
        }

        return facets;
    }

    /**
     * Calculate source facets (top 10)
     */
    public Map<String, Long> calculateSourceFacets(LogSearchCriteria criteria) {
        StringBuilder sql = new StringBuilder("SELECT source, COUNT(*) as count FROM system_logs");
        List<Object> params = new ArrayList<>();

        String whereClause = buildWhereClause(criteria, params);
        if (!whereClause.isEmpty()) {
            sql.append(" ").append(whereClause);
        }

        sql.append(" GROUP BY source ORDER BY count DESC LIMIT 10");

        Map<String, Long> facets = new HashMap<>();
        List<Map<String, Object>> results = sqlQueryExecutor.queryForList(sql.toString(),
            (rs, rowNum) -> {
                Map<String, Object> row = new HashMap<>();
                row.put("source", rs.getString("source"));
                row.put("count", rs.getLong("count"));
                return row;
            }, params.toArray());

        for (Map<String, Object> row : results) {
            facets.put((String) row.get("source"), (Long) row.get("count"));
        }

        return facets;
    }

    /**
     * Calculate category facets (top 10)
     */
    public Map<String, Long> calculateCategoryFacets(LogSearchCriteria criteria) {
        StringBuilder sql = new StringBuilder("SELECT category, COUNT(*) as count FROM system_logs WHERE category IS NOT NULL");
        List<Object> params = new ArrayList<>();

        String whereClause = buildWhereClause(criteria, params);
        if (!whereClause.isEmpty()) {
            sql.append(" AND ").append(whereClause.substring(6)); // Remove "WHERE "
        }

        sql.append(" GROUP BY category ORDER BY count DESC LIMIT 10");

        Map<String, Long> facets = new HashMap<>();
        List<Map<String, Object>> results = sqlQueryExecutor.queryForList(sql.toString(),
            (rs, rowNum) -> {
                Map<String, Object> row = new HashMap<>();
                row.put("category", rs.getString("category"));
                row.put("count", rs.getLong("count"));
                return row;
            }, params.toArray());

        for (Map<String, Object> row : results) {
            facets.put((String) row.get("category"), (Long) row.get("count"));
        }

        return facets;
    }

    /**
     * Build WHERE clause from search criteria
     */
    private String buildWhereClause(LogSearchCriteria criteria, List<Object> params) {
        List<String> conditions = new ArrayList<>();

        // Time range
        if (criteria.getStartTime() != null) {
            conditions.add("timestamp >= ?");
            params.add(ResultSetMapper.toTimestamp(criteria.getStartTime()));
        }

        if (criteria.getEndTime() != null) {
            conditions.add("timestamp <= ?");
            params.add(ResultSetMapper.toTimestamp(criteria.getEndTime()));
        }

        // Levels
        if (criteria.getLevels() != null && !criteria.getLevels().isEmpty()) {
            List<String> levelPlaceholders = new ArrayList<>();
            for (String level : criteria.getLevels()) {
                levelPlaceholders.add("?");
                params.add(level);
            }
            conditions.add("level IN (" + String.join(",", levelPlaceholders) + ")");
        }

        // Sources
        if (criteria.getSources() != null && !criteria.getSources().isEmpty()) {
            List<String> sourcePlaceholders = new ArrayList<>();
            for (String source : criteria.getSources()) {
                sourcePlaceholders.add("?");
                params.add(source);
            }
            conditions.add("source IN (" + String.join(",", sourcePlaceholders) + ")");
        }

        // Categories
        if (criteria.getCategories() != null && !criteria.getCategories().isEmpty()) {
            List<String> categoryPlaceholders = new ArrayList<>();
            for (String category : criteria.getCategories()) {
                categoryPlaceholders.add("?");
                params.add(category);
            }
            conditions.add("category IN (" + String.join(",", categoryPlaceholders) + ")");
        }

        // Component IDs
        if (criteria.getComponentIds() != null && !criteria.getComponentIds().isEmpty()) {
            List<String> componentPlaceholders = new ArrayList<>();
            for (String componentId : criteria.getComponentIds()) {
                componentPlaceholders.add("?");
                params.add(componentId);
            }
            conditions.add("component_id IN (" + String.join(",", componentPlaceholders) + ")");
        }

        // User IDs
        if (criteria.getUserIds() != null && !criteria.getUserIds().isEmpty()) {
            List<String> userPlaceholders = new ArrayList<>();
            for (String userId : criteria.getUserIds()) {
                userPlaceholders.add("?");
                params.add(userId);
            }
            conditions.add("user_id IN (" + String.join(",", userPlaceholders) + ")");
        }

        // Correlation ID
        if (criteria.getCorrelationId() != null && !criteria.getCorrelationId().isEmpty()) {
            conditions.add("correlation_id = ?");
            params.add(criteria.getCorrelationId());
        }

        // Text search
        if (criteria.getSearchText() != null && !criteria.getSearchText().isEmpty()) {
            conditions.add("(LOWER(message) LIKE ? OR LOWER(details) LIKE ? OR LOWER(stack_trace) LIKE ?)");
            String searchPattern = "%" + criteria.getSearchText().toLowerCase() + "%";
            params.add(searchPattern);
            params.add(searchPattern);
            params.add(searchPattern);
        }

        // Regex pattern (PostgreSQL specific)
        if (criteria.getRegexPattern() != null && !criteria.getRegexPattern().isEmpty()) {
            conditions.add("message ~ ?");
            params.add(criteria.getRegexPattern());
        }

        // IP address
        if (criteria.getIpAddress() != null && !criteria.getIpAddress().isEmpty()) {
            conditions.add("ip_address = ?");
            params.add(criteria.getIpAddress());
        }

        // Exclude patterns
        if (criteria.getExcludePatterns() != null && !criteria.getExcludePatterns().isEmpty()) {
            for (String pattern : criteria.getExcludePatterns()) {
                conditions.add("message NOT LIKE ?");
                params.add("%" + pattern + "%");
            }
        }

        return conditions.isEmpty() ? "" : "WHERE " + String.join(" AND ", conditions);
    }

    /**
     * Find flow execution logs within a time range
     */
    public List<SystemLog> findFlowExecutionLogs(LocalDateTime startTime, LocalDateTime endTime, List<String> flowIds) {
        StringBuilder sql = new StringBuilder("SELECT * FROM system_logs WHERE timestamp BETWEEN ? AND ? AND message LIKE ?");
        List<Object> params = new ArrayList<>();
        params.add(ResultSetMapper.toTimestamp(startTime));
        params.add(ResultSetMapper.toTimestamp(endTime));
        params.add("%flow execution%");

        if (flowIds != null && !flowIds.isEmpty()) {
            sql.append(" AND (");
            List<String> flowConditions = new ArrayList<>();
            for (String flowId : flowIds) {
                flowConditions.add("message LIKE ?");
                params.add("%flow: " + flowId + "%");
            }
            sql.append(String.join(" OR ", flowConditions));
            sql.append(")");
        }

        sql.append(" ORDER BY timestamp");

        return sqlQueryExecutor.queryForList(sql.toString(), SYSTEM_LOG_ROW_MAPPER, params.toArray());
    }

    /**
     * Find logs by message containing text and within time range
     */
    public List<SystemLog> findByMessageContainingAndTimestampBetween(String text, LocalDateTime startTime, LocalDateTime endTime) {
        String sql = "SELECT * FROM system_logs WHERE message LIKE ? AND timestamp BETWEEN ? AND ? ORDER BY timestamp";
        return sqlQueryExecutor.queryForList(sql, SYSTEM_LOG_ROW_MAPPER,
            "%" + text + "%",
            ResultSetMapper.toTimestamp(startTime),
            ResultSetMapper.toTimestamp(endTime));
    }

    /**
     * Find all logs with pagination
     */
    public Page<SystemLog> findAll(Pageable pageable) {
        // Count query
        String countSql = "SELECT COUNT(*) FROM " + TABLE_NAME;
        long total = sqlQueryExecutor.count(countSql);

        // Data query
        String sql = "SELECT * FROM " + TABLE_NAME;
        sql += SqlPaginationHelper.buildOrderByClause(pageable.getSort());
        sql += SqlPaginationHelper.buildPaginationClause(pageable);

        List<SystemLog> logs = sqlQueryExecutor.queryForList(sql, SYSTEM_LOG_ROW_MAPPER);

        return new PageImpl<>(logs, pageable, total);
    }

    /**
     * Find logs by level and timestamp between
     */
    public List<SystemLog> findByLevelAndTimestampBetween(SystemLog.LogLevel level, LocalDateTime start, LocalDateTime end) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE level = ? AND timestamp BETWEEN ? AND ? ORDER BY timestamp DESC";
        return sqlQueryExecutor.queryForList(sql, SYSTEM_LOG_ROW_MAPPER,
            level.toString(),
            ResultSetMapper.toTimestamp(start),
            ResultSetMapper.toTimestamp(end));
    }

    /**
     * Delete logs by level and timestamp before
     */
    public int deleteByLevelAndTimestampBefore(SystemLog.LogLevel level, LocalDateTime cutoffDate) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE level = ? AND timestamp < ?";
        return sqlQueryExecutor.update(sql, level.toString(), ResultSetMapper.toTimestamp(cutoffDate));
    }

    /**
     * Delete logs by category and timestamp before
     */
    public int deleteByCategoryAndTimestampBefore(String category, LocalDateTime cutoffDate) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE category = ? AND timestamp < ?";
        return sqlQueryExecutor.update(sql, category, ResultSetMapper.toTimestamp(cutoffDate));
    }

    /**
     * Find audit logs for archive
     */
    public List<SystemLog> findAuditLogsForArchive(LocalDateTime cutoffDate, int batchSize) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE " +
                    "(category IN('AUDIT', 'SECURITY', 'TRANSACTION', 'COMPLIANCE') " +
                    "OR level = 'AUDIT') " +
                    "AND timestamp < ? " +
                    "ORDER BY timestamp " +
                    "LIMIT ?";
        return sqlQueryExecutor.queryForList(sql, SYSTEM_LOG_ROW_MAPPER,
            ResultSetMapper.toTimestamp(cutoffDate), batchSize);
    }

    /**
     * Delete logs by IDs
     */
    public void deleteByIds(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        String placeholders = ids.stream()
            .map(id -> "?")
            .collect(java.util.stream.Collectors.joining(","));

        String sql = "DELETE FROM " + TABLE_NAME + " WHERE id IN (" + placeholders + ")";
        sqlQueryExecutor.update(sql, ids.toArray());
    }

    /**
     * Find logs by timestamp between and correlation ID not null
     */
    public List<SystemLog> findByTimestampBetweenAndCorrelationIdNotNull(LocalDateTime start, LocalDateTime end) {
        String sql = "SELECT * FROM " + TABLE_NAME +
                    " WHERE timestamp BETWEEN ? AND ? AND correlation_id IS NOT NULL " +
                    " ORDER BY timestamp DESC";
        return sqlQueryExecutor.queryForList(sql, SYSTEM_LOG_ROW_MAPPER,
            ResultSetMapper.toTimestamp(start),
            ResultSetMapper.toTimestamp(end));
    }

}