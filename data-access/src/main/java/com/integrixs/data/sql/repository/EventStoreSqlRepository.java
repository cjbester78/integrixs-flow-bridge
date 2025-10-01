package com.integrixs.data.sql.repository;

import com.integrixs.data.model.EventStore;
import com.integrixs.data.sql.core.BaseSqlRepository;
import com.integrixs.data.sql.core.ResultSetMapper;
import com.integrixs.data.sql.core.SqlPaginationHelper;
import com.integrixs.data.sql.core.SqlQueryExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SQL implementation of EventStoreRepository using native queries.
 */
@Repository("eventStoreSqlRepository")
public class EventStoreSqlRepository extends BaseSqlRepository<EventStore, UUID> {

    private static final Logger log = LoggerFactory.getLogger(EventStoreSqlRepository.class);

    private static final String TABLE_NAME = "event_store";
    private static final String ID_COLUMN = "event_id";

    /**
     * Row mapper for EventStore entity
     */
    private static final RowMapper<EventStore> EVENT_STORE_ROW_MAPPER = new RowMapper<EventStore>() {
        @Override
        public EventStore mapRow(ResultSet rs, int rowNum) throws SQLException {
            return EventStore.builder()
                    .eventId(ResultSetMapper.getUUID(rs, "event_id"))
                    .aggregateType(ResultSetMapper.getString(rs, "aggregate_type"))
                    .aggregateId(ResultSetMapper.getUUID(rs, "aggregate_id"))
                    .aggregateVersion(ResultSetMapper.getLong(rs, "aggregate_version"))
                    .eventType(ResultSetMapper.getString(rs, "event_type"))
                    .eventData(ResultSetMapper.getString(rs, "event_data"))
                    .eventMetadata(ResultSetMapper.getString(rs, "event_metadata"))
                    .occurredAt(ResultSetMapper.getLocalDateTime(rs, "occurred_at"))
                    .triggeredBy(ResultSetMapper.getUUID(rs, "triggered_by"))
                    .storedAt(ResultSetMapper.getLocalDateTime(rs, "stored_at"))
                    .correlationId(ResultSetMapper.getUUID(rs, "correlation_id"))
                    .causationId(ResultSetMapper.getUUID(rs, "causation_id"))
                    .build();
        }
    };

    public EventStoreSqlRepository(SqlQueryExecutor sqlQueryExecutor) {
        super(sqlQueryExecutor, TABLE_NAME, ID_COLUMN, EVENT_STORE_ROW_MAPPER);
    }

    /**
     * Finds events by aggregate ID ordered by version
     */
    public List<EventStore> findByAggregateIdOrderByAggregateVersionAsc(UUID aggregateId) {
        String sql = "SELECT * FROM event_store WHERE aggregate_id = ? ORDER BY aggregate_version ASC";
        return sqlQueryExecutor.queryForList(sql, EVENT_STORE_ROW_MAPPER, aggregateId);
    }

    /**
     * Finds events by aggregate ID and type
     */
    public List<EventStore> findByAggregateIdAndAggregateTypeOrderByAggregateVersionAsc(
            UUID aggregateId, String aggregateType) {
        String sql = "SELECT * FROM event_store WHERE aggregate_id = ? AND aggregate_type = ? ORDER BY aggregate_version ASC";
        return sqlQueryExecutor.queryForList(sql, EVENT_STORE_ROW_MAPPER, aggregateId, aggregateType);
    }

    /**
     * Finds events by type within a time range
     */
    public Page<EventStore> findByEventTypeAndOccurredAtBetween(
            String eventType, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        String baseQuery = "SELECT * FROM event_store WHERE event_type = ? AND occurred_at BETWEEN ? AND ?";
        String countQuery = "SELECT COUNT(*) FROM event_store WHERE event_type = ? AND occurred_at BETWEEN ? AND ?";

        String query = SqlPaginationHelper.buildPaginatedQuery(baseQuery, pageable);

        List<EventStore> content = sqlQueryExecutor.queryForList(query, EVENT_STORE_ROW_MAPPER,
                eventType, ResultSetMapper.toTimestamp(startTime), ResultSetMapper.toTimestamp(endTime));

        long total = sqlQueryExecutor.count(countQuery,
                eventType, ResultSetMapper.toTimestamp(startTime), ResultSetMapper.toTimestamp(endTime));

        return SqlPaginationHelper.createPage(content, pageable, total);
    }

    /**
     * Finds events by user within a time range
     */
    public Page<EventStore> findByTriggeredByAndOccurredAtBetween(
            UUID triggeredBy, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        String baseQuery = "SELECT * FROM event_store WHERE triggered_by = ? AND occurred_at BETWEEN ? AND ?";
        String countQuery = "SELECT COUNT(*) FROM event_store WHERE triggered_by = ? AND occurred_at BETWEEN ? AND ?";

        String query = SqlPaginationHelper.buildPaginatedQuery(baseQuery, pageable);

        List<EventStore> content = sqlQueryExecutor.queryForList(query, EVENT_STORE_ROW_MAPPER,
                triggeredBy, ResultSetMapper.toTimestamp(startTime), ResultSetMapper.toTimestamp(endTime));

        long total = sqlQueryExecutor.count(countQuery,
                triggeredBy, ResultSetMapper.toTimestamp(startTime), ResultSetMapper.toTimestamp(endTime));

        return SqlPaginationHelper.createPage(content, pageable, total);
    }

    /**
     * Gets the latest version for an aggregate
     */
    public Long getLatestVersionForAggregate(UUID aggregateId) {
        String sql = "SELECT COALESCE(MAX(aggregate_version), 0) FROM event_store WHERE aggregate_id = ?";
        Long result = sqlQueryExecutor.getJdbcTemplate().queryForObject(sql, Long.class, aggregateId);
        return result != null ? result : 0L;
    }

    /**
     * Finds events by correlation ID
     */
    public List<EventStore> findByCorrelationIdOrderByOccurredAtAsc(UUID correlationId) {
        String sql = "SELECT * FROM event_store WHERE correlation_id = ? ORDER BY occurred_at ASC";
        return sqlQueryExecutor.queryForList(sql, EVENT_STORE_ROW_MAPPER, correlationId);
    }

    @Override
    public EventStore save(EventStore event) {
        if (event.getEventId() == null) {
            event.setEventId(generateId());
        }

        if (event.getStoredAt() == null) {
            event.setStoredAt(LocalDateTime.now());
        }

        String sql = buildInsertSql(
            "event_id", "aggregate_type", "aggregate_id", "aggregate_version",
            "event_type", "event_data", "event_metadata", "occurred_at",
            "triggered_by", "stored_at", "correlation_id", "causation_id"
        );

        sqlQueryExecutor.update(sql,
            event.getEventId(),
            event.getAggregateType(),
            event.getAggregateId(),
            event.getAggregateVersion(),
            event.getEventType(),
            event.getEventData(),
            event.getEventMetadata(),
            ResultSetMapper.toTimestamp(event.getOccurredAt()),
            event.getTriggeredBy(),
            ResultSetMapper.toTimestamp(event.getStoredAt()),
            event.getCorrelationId(),
            event.getCausationId()
        );

        return event;
    }

    @Override
    public EventStore update(EventStore event) {
        // Event store entries are immutable - they should never be updated
        log.error("Attempted to update immutable event store entry with ID: {}", event.getEventId());
        return event; // Return unchanged event
    }
}