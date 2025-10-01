package com.integrixs.data.sql.repository;

import com.integrixs.data.model.AdapterStatus;
import com.integrixs.data.model.CommunicationAdapter;
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
 * SQL implementation of AdapterStatusRepository using native queries.
 */
@Repository
public class AdapterStatusSqlRepository extends BaseSqlRepository<AdapterStatus, UUID> {

    private static final String TABLE_NAME = "adapter_status";
    private static final String ID_COLUMN = "id";

    /**
     * Row mapper for AdapterStatus entity
     */
    private static final RowMapper<AdapterStatus> ADAPTER_STATUS_ROW_MAPPER = new RowMapper<AdapterStatus>() {
        @Override
        public AdapterStatus mapRow(ResultSet rs, int rowNum) throws SQLException {
            AdapterStatus status = new AdapterStatus();
            status.setId(ResultSetMapper.getUUID(rs, "id"));
            status.setStatus(ResultSetMapper.getString(rs, "status"));
            status.setHealthScore(ResultSetMapper.getInteger(rs, "health_score"));
            status.setLastActivity(ResultSetMapper.getLocalDateTime(rs, "last_activity"));
            status.setLastError(ResultSetMapper.getLocalDateTime(rs, "last_error"));
            status.setLastErrorMessage(ResultSetMapper.getString(rs, "last_error_message"));
            status.setTotalMessagesProcessed(ResultSetMapper.getLong(rs, "total_messages_processed"));
            status.setMessagesProcessedToday(ResultSetMapper.getLong(rs, "messages_processed_today"));
            status.setErrorCountToday(ResultSetMapper.getLong(rs, "error_count_today"));
            status.setAverageResponseTime(ResultSetMapper.getLong(rs, "average_response_time"));
            status.setIsConnected(ResultSetMapper.getBoolean(rs, "is_connected"));
            status.setConnectionDetails(ResultSetMapper.getString(rs, "connection_details"));
            status.setLastHealthCheck(ResultSetMapper.getLocalDateTime(rs, "last_health_check"));
            status.setNextHealthCheck(ResultSetMapper.getLocalDateTime(rs, "next_health_check"));
            status.setMetadata(ResultSetMapper.getString(rs, "metadata"));
            status.setCreatedAt(ResultSetMapper.getLocalDateTime(rs, "created_at"));
            status.setUpdatedAt(ResultSetMapper.getLocalDateTime(rs, "updated_at"));

            // Handle adapter relationship
            CommunicationAdapter adapter = new CommunicationAdapter();
            adapter.setId(ResultSetMapper.getUUID(rs, "adapter_id"));
            status.setAdapter(adapter);

            return status;
        }
    };

    public AdapterStatusSqlRepository(SqlQueryExecutor sqlQueryExecutor) {
        super(sqlQueryExecutor, TABLE_NAME, ID_COLUMN, ADAPTER_STATUS_ROW_MAPPER);
    }

    public Optional<AdapterStatus> findByAdapterId(UUID adapterId) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE adapter_id = ?";
        return sqlQueryExecutor.queryForObject(sql, ADAPTER_STATUS_ROW_MAPPER, adapterId);
    }

    public List<AdapterStatus> findByStatus(String status) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE status = ?";
        return sqlQueryExecutor.queryForList(sql, ADAPTER_STATUS_ROW_MAPPER, status);
    }

    public List<AdapterStatus> findByLastActivityBefore(LocalDateTime timestamp) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE last_activity < ?";
        return sqlQueryExecutor.queryForList(sql, ADAPTER_STATUS_ROW_MAPPER, ResultSetMapper.toTimestamp(timestamp));
    }

    @Override
    public AdapterStatus save(AdapterStatus status) {
        if (status.getId() == null) {
            status.setId(generateId());
        }

        boolean exists = existsById(status.getId());

        if (!exists) {
            return insert(status);
        } else {
            return update(status);
        }
    }

    private AdapterStatus insert(AdapterStatus status) {
        String sql = "INSERT INTO " + TABLE_NAME + " (" +
                     "id, adapter_id, status, health_score, last_activity, last_error, " +
                     "last_error_message, total_messages_processed, messages_processed_today, " +
                     "error_count_today, average_response_time, is_connected, connection_details, " +
                     "last_health_check, next_health_check, metadata, created_at, updated_at" +
                     ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        LocalDateTime now = LocalDateTime.now();
        if (status.getCreatedAt() == null) {
            status.setCreatedAt(now);
        }
        if (status.getUpdatedAt() == null) {
            status.setUpdatedAt(now);
        }

        sqlQueryExecutor.update(sql,
            status.getId(),
            status.getAdapter() != null ? status.getAdapter().getId() : null,
            status.getStatus(),
            status.getHealthScore(),
            ResultSetMapper.toTimestamp(status.getLastActivity()),
            ResultSetMapper.toTimestamp(status.getLastError()),
            status.getLastErrorMessage(),
            status.getTotalMessagesProcessed(),
            status.getMessagesProcessedToday(),
            status.getErrorCountToday(),
            status.getAverageResponseTime(),
            status.getIsConnected(),
            status.getConnectionDetails(),
            ResultSetMapper.toTimestamp(status.getLastHealthCheck()),
            ResultSetMapper.toTimestamp(status.getNextHealthCheck()),
            status.getMetadata(),
            ResultSetMapper.toTimestamp(status.getCreatedAt()),
            ResultSetMapper.toTimestamp(status.getUpdatedAt())
        );

        return status;
    }

    @Override
    public AdapterStatus update(AdapterStatus status) {
        String sql = "UPDATE " + TABLE_NAME + " SET " +
                     "adapter_id = ?, status = ?, health_score = ?, last_activity = ?, " +
                     "last_error = ?, last_error_message = ?, total_messages_processed = ?, " +
                     "messages_processed_today = ?, error_count_today = ?, average_response_time = ?, " +
                     "is_connected = ?, connection_details = ?, last_health_check = ?, " +
                     "next_health_check = ?, metadata = ?, updated_at = ? " +
                     "WHERE id = ?";

        status.setUpdatedAt(LocalDateTime.now());

        sqlQueryExecutor.update(sql,
            status.getAdapter() != null ? status.getAdapter().getId() : null,
            status.getStatus(),
            status.getHealthScore(),
            ResultSetMapper.toTimestamp(status.getLastActivity()),
            ResultSetMapper.toTimestamp(status.getLastError()),
            status.getLastErrorMessage(),
            status.getTotalMessagesProcessed(),
            status.getMessagesProcessedToday(),
            status.getErrorCountToday(),
            status.getAverageResponseTime(),
            status.getIsConnected(),
            status.getConnectionDetails(),
            ResultSetMapper.toTimestamp(status.getLastHealthCheck()),
            ResultSetMapper.toTimestamp(status.getNextHealthCheck()),
            status.getMetadata(),
            ResultSetMapper.toTimestamp(status.getUpdatedAt()),
            status.getId()
        );

        return status;
    }

    public void deleteByAdapterId(UUID adapterId) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE adapter_id = ?";
        sqlQueryExecutor.update(sql, adapterId);
    }
}