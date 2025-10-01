package com.integrixs.data.sql.repository;

import com.integrixs.data.model.AdapterHealthRecord;
import com.integrixs.data.sql.core.BaseSqlRepository;
import com.integrixs.data.sql.core.ResultSetMapper;
import com.integrixs.data.sql.core.SqlQueryExecutor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * SQL implementation of AdapterHealthRecordRepository using native queries.
 */
@Repository
public class AdapterHealthRecordSqlRepository extends BaseSqlRepository<AdapterHealthRecord, UUID> {

    private static final String TABLE_NAME = "adapter_health_records";
    private static final String ID_COLUMN = "id";

    /**
     * Row mapper for AdapterHealthRecord entity
     */
    private static final RowMapper<AdapterHealthRecord> ADAPTER_HEALTH_RECORD_ROW_MAPPER = new RowMapper<AdapterHealthRecord>() {
        @Override
        public AdapterHealthRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
            AdapterHealthRecord record = new AdapterHealthRecord();
            record.setId(ResultSetMapper.getUUID(rs, "id"));
            record.setAdapterId(ResultSetMapper.getUUID(rs, "adapter_id"));
            record.setCheckedAt(ResultSetMapper.getLocalDateTime(rs, "checked_at"));

            String healthStatusStr = ResultSetMapper.getString(rs, "health_status");
            if (healthStatusStr != null) {
                record.setHealthStatus(AdapterHealthRecord.HealthStatus.valueOf(healthStatusStr));
            }

            record.setResponseTimeMs(ResultSetMapper.getLong(rs, "response_time_ms"));
            record.setAvailableConnections(ResultSetMapper.getInteger(rs, "available_connections"));
            record.setActiveConnections(ResultSetMapper.getInteger(rs, "active_connections"));
            record.setErrorCount(ResultSetMapper.getInteger(rs, "error_count"));
            record.setSuccessCount(ResultSetMapper.getInteger(rs, "success_count"));
            record.setCpuUsage(ResultSetMapper.getDouble(rs, "cpu_usage"));
            record.setMemoryUsage(ResultSetMapper.getDouble(rs, "memory_usage"));
            record.setLastErrorMessage(ResultSetMapper.getString(rs, "last_error_message"));
            record.setHealthDetails(ResultSetMapper.getString(rs, "health_details"));
            record.setAvailable(rs.getBoolean("available"));
            record.setCreatedAt(ResultSetMapper.getLocalDateTime(rs, "created_at"));
            record.setUpdatedAt(ResultSetMapper.getLocalDateTime(rs, "updated_at"));

            return record;
        }
    };

    public AdapterHealthRecordSqlRepository(SqlQueryExecutor sqlQueryExecutor) {
        super(sqlQueryExecutor, TABLE_NAME, ID_COLUMN, ADAPTER_HEALTH_RECORD_ROW_MAPPER);
    }

    public List<AdapterHealthRecord> findByAdapterIdOrderByTimestampDesc(UUID adapterId) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE adapter_id = ? ORDER BY timestamp DESC";
        return sqlQueryExecutor.queryForList(sql, ADAPTER_HEALTH_RECORD_ROW_MAPPER, adapterId);
    }

    public List<AdapterHealthRecord> findByAdapterIdAndTimestampBetween(UUID adapterId, LocalDateTime start, LocalDateTime end) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE adapter_id = ? AND timestamp BETWEEN ? AND ? ORDER BY timestamp";
        return sqlQueryExecutor.queryForList(sql, ADAPTER_HEALTH_RECORD_ROW_MAPPER,
            adapterId,
            ResultSetMapper.toTimestamp(start),
            ResultSetMapper.toTimestamp(end));
    }

    public List<AdapterHealthRecord> findByHealthStatusAndTimestampAfter(String healthStatus, LocalDateTime timestamp) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE health_status = ? AND timestamp > ? ORDER BY timestamp";
        return sqlQueryExecutor.queryForList(sql, ADAPTER_HEALTH_RECORD_ROW_MAPPER,
            healthStatus,
            ResultSetMapper.toTimestamp(timestamp));
    }

    @Override
    public AdapterHealthRecord save(AdapterHealthRecord record) {
        if (record.getId() == null) {
            record.setId(generateId());
        }

        LocalDateTime now = LocalDateTime.now();
        if (record.getCheckedAt() == null) {
            record.setCheckedAt(now);
        }
        if (record.getCreatedAt() == null) {
            record.setCreatedAt(now);
        }
        if (record.getUpdatedAt() == null) {
            record.setUpdatedAt(now);
        }

        String sql = "INSERT INTO " + TABLE_NAME + " (" +
                     "id, adapter_id, health_status, checked_at, response_time_ms, " +
                     "available_connections, active_connections, error_count, success_count, " +
                     "cpu_usage, memory_usage, last_error_message, health_details, " +
                     "available, created_at, updated_at" +
                     ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        UUID adapterId = record.getAdapter() != null ? record.getAdapter().getId() : null;

        sqlQueryExecutor.update(sql,
            record.getId(),
            adapterId,
            record.getHealthStatus() != null ? record.getHealthStatus().name() : null,
            ResultSetMapper.toTimestamp(record.getCheckedAt()),
            record.getResponseTimeMs(),
            record.getAvailableConnections(),
            record.getActiveConnections(),
            record.getErrorCount(),
            record.getSuccessCount(),
            record.getCpuUsage(),
            record.getMemoryUsage(),
            record.getLastErrorMessage(),
            record.getHealthDetails(),
            record.isAvailable(),
            ResultSetMapper.toTimestamp(record.getCreatedAt()),
            ResultSetMapper.toTimestamp(record.getUpdatedAt())
        );

        return record;
    }

    @Override
    public AdapterHealthRecord update(AdapterHealthRecord record) {
        // Health records are typically immutable, but providing for completeness
        String sql = "UPDATE " + TABLE_NAME + " SET " +
                     "health_status = ?, response_time_ms = ?, available_connections = ?, " +
                     "active_connections = ?, error_count = ?, success_count = ?, " +
                     "cpu_usage = ?, memory_usage = ?, last_error_message = ?, " +
                     "health_details = ?, available = ?, updated_at = ? " +
                     "WHERE id = ?";

        record.setUpdatedAt(LocalDateTime.now());

        sqlQueryExecutor.update(sql,
            record.getHealthStatus() != null ? record.getHealthStatus().name() : null,
            record.getResponseTimeMs(),
            record.getAvailableConnections(),
            record.getActiveConnections(),
            record.getErrorCount(),
            record.getSuccessCount(),
            record.getCpuUsage(),
            record.getMemoryUsage(),
            record.getLastErrorMessage(),
            record.getHealthDetails(),
            record.isAvailable(),
            ResultSetMapper.toTimestamp(record.getUpdatedAt()),
            record.getId()
        );

        return record;
    }

    public void deleteByAdapterIdAndCheckedAtBefore(UUID adapterId, LocalDateTime checkedAt) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE adapter_id = ? AND checked_at < ?";
        sqlQueryExecutor.update(sql, adapterId, ResultSetMapper.toTimestamp(checkedAt));
    }
}