package com.integrixs.data.sql.repository;

import com.integrixs.data.model.AdapterPayload;
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
 * SQL implementation of AdapterPayloadRepository using native queries.
 */
@Repository("adapterPayloadSqlRepository")
public class AdapterPayloadSqlRepository extends BaseSqlRepository<AdapterPayload, UUID> {

    private static final String TABLE_NAME = "adapter_payloads";
    private static final String ID_COLUMN = "id";

    /**
     * Row mapper for AdapterPayload entity
     */
    private static final RowMapper<AdapterPayload> ADAPTER_PAYLOAD_ROW_MAPPER = new RowMapper<AdapterPayload>() {
        @Override
        public AdapterPayload mapRow(ResultSet rs, int rowNum) throws SQLException {
            return AdapterPayload.builder()
                    .id(ResultSetMapper.getUUID(rs, "id"))
                    .correlationId(ResultSetMapper.getString(rs, "correlation_id"))
                    .adapterId(ResultSetMapper.getUUID(rs, "adapter_id"))
                    .adapterName(ResultSetMapper.getString(rs, "adapter_name"))
                    .adapterType(ResultSetMapper.getString(rs, "adapter_type"))
                    .direction(ResultSetMapper.getString(rs, "direction"))
                    .payloadType(ResultSetMapper.getString(rs, "payload_type"))
                    .messageStructureId(ResultSetMapper.getUUID(rs, "message_structure_id"))
                    .payload(ResultSetMapper.getString(rs, "payload"))
                    .payloadSize(ResultSetMapper.getInteger(rs, "payload_size"))
                    .createdAt(ResultSetMapper.getLocalDateTime(rs, "created_at"))
                    .build();
        }
    };

    public AdapterPayloadSqlRepository(SqlQueryExecutor sqlQueryExecutor) {
        super(sqlQueryExecutor, TABLE_NAME, ID_COLUMN, ADAPTER_PAYLOAD_ROW_MAPPER);
    }

    public List<AdapterPayload> findByCorrelationIdOrderByCreatedAtAsc(String correlationId) {
        String sql = "SELECT * FROM adapter_payloads WHERE correlation_id = ? ORDER BY created_at ASC";
        return sqlQueryExecutor.queryForList(sql, ADAPTER_PAYLOAD_ROW_MAPPER, correlationId);
    }

    public List<AdapterPayload> findByCorrelationIdOrderByCreatedAt(String correlationId) {
        String sql = "SELECT * FROM adapter_payloads WHERE correlation_id = ? ORDER BY created_at";
        return sqlQueryExecutor.queryForList(sql, ADAPTER_PAYLOAD_ROW_MAPPER, correlationId);
    }

    public List<AdapterPayload> findByAdapterIdOrderByCreatedAtDesc(UUID adapterId) {
        String sql = "SELECT * FROM adapter_payloads WHERE adapter_id = ? ORDER BY created_at DESC";
        return sqlQueryExecutor.queryForList(sql, ADAPTER_PAYLOAD_ROW_MAPPER, adapterId);
    }

    public void deleteByCorrelationId(String correlationId) {
        String sql = "DELETE FROM adapter_payloads WHERE correlation_id = ?";
        sqlQueryExecutor.update(sql, correlationId);
    }

    public int deleteByCreatedAtBefore(LocalDateTime cutoffDate) {
        String sql = "DELETE FROM adapter_payloads WHERE created_at < ?";
        return sqlQueryExecutor.update(sql, ResultSetMapper.toTimestamp(cutoffDate));
    }

    public List<AdapterPayload> findByCorrelationId(String correlationId) {
        return findByCorrelationIdOrderByCreatedAtAsc(correlationId);
    }

    @Override
    public AdapterPayload save(AdapterPayload payload) {
        if (payload.getId() == null) {
            payload.setId(generateId());
        }

        if (payload.getCreatedAt() == null) {
            payload.setCreatedAt(LocalDateTime.now());
        }

        // Calculate payload size if not set
        if (payload.getPayload() != null && payload.getPayloadSize() == null) {
            payload.setPayloadSize(payload.getPayload().length());
        }

        String sql = buildInsertSql(
            "id", "correlation_id", "adapter_id", "adapter_name", "adapter_type",
            "direction", "payload_type", "message_structure_id", "payload",
            "payload_size", "created_at"
        );

        sqlQueryExecutor.update(sql,
            payload.getId(),
            payload.getCorrelationId(),
            payload.getAdapterId(),
            payload.getAdapterName(),
            payload.getAdapterType(),
            payload.getDirection(),
            payload.getPayloadType(),
            payload.getMessageStructureId(),
            payload.getPayload(),
            payload.getPayloadSize(),
            ResultSetMapper.toTimestamp(payload.getCreatedAt())
        );

        return payload;
    }

    @Override
    public AdapterPayload update(AdapterPayload payload) {
        // AdapterPayload is typically immutable (audit trail), but providing update for completeness

        // Recalculate payload size if payload changed
        if (payload.getPayload() != null && payload.getPayloadSize() == null) {
            payload.setPayloadSize(payload.getPayload().length());
        }

        String sql = buildUpdateSql(
            "correlation_id", "adapter_id", "adapter_name", "adapter_type",
            "direction", "payload_type", "message_structure_id", "payload",
            "payload_size"
        );

        sqlQueryExecutor.update(sql,
            payload.getCorrelationId(),
            payload.getAdapterId(),
            payload.getAdapterName(),
            payload.getAdapterType(),
            payload.getDirection(),
            payload.getPayloadType(),
            payload.getMessageStructureId(),
            payload.getPayload(),
            payload.getPayloadSize(),
            payload.getId()
        );

        return payload;
    }
}