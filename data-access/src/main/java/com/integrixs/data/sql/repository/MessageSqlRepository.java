package com.integrixs.data.sql.repository;

import com.integrixs.data.model.*;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * SQL implementation of MessageRepository using native queries.
 * Handles large message payloads and relationships to Flow and FlowExecution.
 */
@Repository("messageSqlRepository")
public class MessageSqlRepository extends BaseSqlRepository<Message, UUID> {

    private static final String TABLE_NAME = "messages";
    private static final String ID_COLUMN = "id";

    /**
     * Row mapper for Message entity (without relationships)
     */
    private static final RowMapper<Message> MESSAGE_ROW_MAPPER = new RowMapper<Message>() {
        @Override
        public Message mapRow(ResultSet rs, int rowNum) throws SQLException {
            Message message = new Message();
            message.setId(ResultSetMapper.getUUID(rs, "id"));
            message.setMessageId(ResultSetMapper.getString(rs, "message_id"));

            String statusStr = ResultSetMapper.getString(rs, "status");
            if (statusStr != null) {
                message.setStatus(Message.MessageStatus.valueOf(statusStr));
            }

            message.setSourceSystem(ResultSetMapper.getString(rs, "source_system"));
            message.setTargetSystem(ResultSetMapper.getString(rs, "target_system"));
            message.setMessageType(ResultSetMapper.getString(rs, "message_type"));
            message.setContentType(ResultSetMapper.getString(rs, "content_type"));
            message.setMessageContent(ResultSetMapper.getString(rs, "message_content"));
            message.setHeaders(ResultSetMapper.getString(rs, "headers"));
            message.setProperties(ResultSetMapper.getString(rs, "properties"));
            message.setReceivedAt(ResultSetMapper.getLocalDateTime(rs, "received_at"));
            message.setProcessedAt(ResultSetMapper.getLocalDateTime(rs, "processed_at"));
            message.setCompletedAt(ResultSetMapper.getLocalDateTime(rs, "completed_at"));
            message.setErrorMessage(ResultSetMapper.getString(rs, "error_message"));
            message.setRetryCount(ResultSetMapper.getInteger(rs, "retry_count"));
            message.setCorrelationId(ResultSetMapper.getString(rs, "correlation_id"));
            message.setPriority(ResultSetMapper.getInteger(rs, "priority"));
            message.setCreatedAt(ResultSetMapper.getLocalDateTime(rs, "created_at"));
            message.setUpdatedAt(ResultSetMapper.getLocalDateTime(rs, "updated_at"));

            return message;
        }
    };

    /**
     * Row mapper for Message with relationships
     */
    private static final RowMapper<Message> MESSAGE_WITH_RELATIONSHIPS_ROW_MAPPER = new RowMapper<Message>() {
        @Override
        public Message mapRow(ResultSet rs, int rowNum) throws SQLException {
            Message message = MESSAGE_ROW_MAPPER.mapRow(rs, rowNum);

            // Map flow (minimal fields)
            UUID flowId = ResultSetMapper.getUUID(rs, "flow_id");
            if (flowId != null) {
                IntegrationFlow flow = new IntegrationFlow();
                flow.setId(flowId);
                flow.setName(ResultSetMapper.getString(rs, "flow_name"));
                message.setFlow(flow);
            }

            // Map flow execution (minimal fields)
            UUID flowExecutionId = ResultSetMapper.getUUID(rs, "flow_execution_id");
            if (flowExecutionId != null) {
                FlowExecution flowExecution = new FlowExecution();
                flowExecution.setId(flowExecutionId);
                // Note: execution_id field doesn't exist in database schema
                // Using the flow_execution_id as identifier instead
                message.setFlowExecution(flowExecution);
            }

            // Map created by user
            UUID createdById = ResultSetMapper.getUUID(rs, "created_by");
            if (createdById != null) {
                User createdBy = new User();
                createdBy.setId(createdById);
                createdBy.setUsername(ResultSetMapper.getString(rs, "created_by_username"));
                createdBy.setEmail(ResultSetMapper.getString(rs, "created_by_email"));
                message.setCreatedBy(createdBy);
            }

            // Map updated by user
            UUID updatedById = ResultSetMapper.getUUID(rs, "updated_by");
            if (updatedById != null) {
                User updatedBy = new User();
                updatedBy.setId(updatedById);
                updatedBy.setUsername(ResultSetMapper.getString(rs, "updated_by_username"));
                updatedBy.setEmail(ResultSetMapper.getString(rs, "updated_by_email"));
                message.setUpdatedBy(updatedBy);
            }

            return message;
        }
    };

    public MessageSqlRepository(SqlQueryExecutor sqlQueryExecutor) {
        super(sqlQueryExecutor, TABLE_NAME, ID_COLUMN, MESSAGE_ROW_MAPPER);
    }

    @Override
    public Optional<Message> findById(UUID id) {
        String sql = buildSelectWithJoins() + " WHERE m.id = ?";

        List<Message> results = sqlQueryExecutor.queryForList(sql, MESSAGE_WITH_RELATIONSHIPS_ROW_MAPPER, id);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public List<Message> findAll() {
        String sql = buildSelectWithJoins() + " ORDER BY m.received_at DESC";
        return sqlQueryExecutor.queryForList(sql, MESSAGE_WITH_RELATIONSHIPS_ROW_MAPPER);
    }

    public Page<Message> findAll(Pageable pageable) {
        String baseQuery = buildSelectWithJoins();
        String countQuery = "SELECT COUNT(*) FROM " + TABLE_NAME;

        long total = sqlQueryExecutor.count(countQuery);
        String paginatedQuery = baseQuery + SqlPaginationHelper.buildOrderByClause(pageable.getSort()) + SqlPaginationHelper.buildPaginationClause(pageable);
        List<Message> messages = sqlQueryExecutor.queryForList(paginatedQuery, MESSAGE_WITH_RELATIONSHIPS_ROW_MAPPER);

        return new PageImpl<>(messages, pageable, total);
    }

    public Optional<Message> findByMessageId(String messageId) {
        String sql = buildSelectWithJoins() + " WHERE m.message_id = ?";

        List<Message> results = sqlQueryExecutor.queryForList(sql, MESSAGE_WITH_RELATIONSHIPS_ROW_MAPPER, messageId);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public List<Message> findByStatus(Message.MessageStatus status) {
        String sql = buildSelectWithJoins() + " WHERE m.status = ? ORDER BY m.priority DESC, m.received_at ASC";
        return sqlQueryExecutor.queryForList(sql, MESSAGE_WITH_RELATIONSHIPS_ROW_MAPPER, status.toString());
    }

    public List<Message> findByFlowId(UUID flowId) {
        String sql = buildSelectWithJoins() + " WHERE m.flow_id = ? ORDER BY m.received_at DESC";
        return sqlQueryExecutor.queryForList(sql, MESSAGE_WITH_RELATIONSHIPS_ROW_MAPPER, flowId);
    }

    public List<Message> findByFlowExecutionId(UUID flowExecutionId) {
        String sql = buildSelectWithJoins() + " WHERE m.flow_execution_id = ? ORDER BY m.received_at ASC";
        return sqlQueryExecutor.queryForList(sql, MESSAGE_WITH_RELATIONSHIPS_ROW_MAPPER, flowExecutionId);
    }

    public List<Message> findByCorrelationId(String correlationId) {
        String sql = buildSelectWithJoins() + " WHERE m.correlation_id = ? ORDER BY m.received_at ASC";
        return sqlQueryExecutor.queryForList(sql, MESSAGE_WITH_RELATIONSHIPS_ROW_MAPPER, correlationId);
    }

    public List<Message> findPendingMessagesByPriority(int limit) {
        String sql = buildSelectWithJoins() +
                     " WHERE m.status IN ('PENDING', 'QUEUED', 'RETRY') " +
                     "ORDER BY m.priority DESC, m.received_at ASC LIMIT ?";
        return sqlQueryExecutor.queryForList(sql, MESSAGE_WITH_RELATIONSHIPS_ROW_MAPPER, limit);
    }

    public List<Message> findFailedMessages(LocalDateTime since) {
        String sql = buildSelectWithJoins() +
                     " WHERE m.status = 'FAILED' AND m.updated_at >= ? " +
                     "ORDER BY m.updated_at DESC";
        return sqlQueryExecutor.queryForList(sql, MESSAGE_WITH_RELATIONSHIPS_ROW_MAPPER,
                                           ResultSetMapper.toTimestamp(since));
    }

    public List<Message> findBySourceSystem(String sourceSystem) {
        String sql = buildSelectWithJoins() + " WHERE m.source_system = ? ORDER BY m.received_at DESC";
        return sqlQueryExecutor.queryForList(sql, MESSAGE_WITH_RELATIONSHIPS_ROW_MAPPER, sourceSystem);
    }

    public List<Message> findByTargetSystem(String targetSystem) {
        String sql = buildSelectWithJoins() + " WHERE m.target_system = ? ORDER BY m.received_at DESC";
        return sqlQueryExecutor.queryForList(sql, MESSAGE_WITH_RELATIONSHIPS_ROW_MAPPER, targetSystem);
    }

    @Override
    public Message save(Message message) {
        if (message.getId() == null) {
            message.setId(generateId());
        }

        boolean exists = existsById(message.getId());

        if (!exists) {
            return insert(message);
        } else {
            return update(message);
        }
    }

    private Message insert(Message message) {
        String sql = "INSERT INTO " + TABLE_NAME + " (" +
                     "id, message_id, flow_id, flow_execution_id, status, " +
                     "source_system, target_system, message_type, content_type, " +
                     "message_content, headers, properties, received_at, processed_at, " +
                     "completed_at, error_message, retry_count, correlation_id, priority, " +
                     "created_at, updated_at, created_by, updated_by" +
                     ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        LocalDateTime now = LocalDateTime.now();
        if (message.getCreatedAt() == null) {
            message.setCreatedAt(now);
        }
        if (message.getUpdatedAt() == null) {
            message.setUpdatedAt(now);
        }
        if (message.getReceivedAt() == null) {
            message.setReceivedAt(now);
        }

        sqlQueryExecutor.update(sql,
            message.getId(),
            message.getMessageId(),
            message.getFlow() != null ? message.getFlow().getId() : null,
            message.getFlowExecution() != null ? message.getFlowExecution().getId() : null,
            message.getStatus() != null ? message.getStatus().toString() : "RECEIVED",
            message.getSourceSystem(),
            message.getTargetSystem(),
            message.getMessageType(),
            message.getContentType(),
            message.getMessageContent(),
            message.getHeaders(),
            message.getProperties(),
            ResultSetMapper.toTimestamp(message.getReceivedAt()),
            ResultSetMapper.toTimestamp(message.getProcessedAt()),
            ResultSetMapper.toTimestamp(message.getCompletedAt()),
            message.getErrorMessage(),
            message.getRetryCount(),
            message.getCorrelationId(),
            message.getPriority(),
            ResultSetMapper.toTimestamp(message.getCreatedAt()),
            ResultSetMapper.toTimestamp(message.getUpdatedAt()),
            message.getCreatedBy() != null ? message.getCreatedBy().getId() : null,
            message.getUpdatedBy() != null ? message.getUpdatedBy().getId() : null
        );

        return message;
    }

    @Override
    public Message update(Message message) {
        String sql = "UPDATE " + TABLE_NAME + " SET " +
                     "message_id = ?, flow_id = ?, flow_execution_id = ?, status = ?, " +
                     "source_system = ?, target_system = ?, message_type = ?, content_type = ?, " +
                     "message_content = ?, headers = ?, properties = ?, processed_at = ?, " +
                     "completed_at = ?, error_message = ?, retry_count = ?, correlation_id = ?, " +
                     "priority = ?, updated_at = ?, updated_by = ? " +
                     "WHERE id = ?";

        message.setUpdatedAt(LocalDateTime.now());

        sqlQueryExecutor.update(sql,
            message.getMessageId(),
            message.getFlow() != null ? message.getFlow().getId() : null,
            message.getFlowExecution() != null ? message.getFlowExecution().getId() : null,
            message.getStatus() != null ? message.getStatus().toString() : "RECEIVED",
            message.getSourceSystem(),
            message.getTargetSystem(),
            message.getMessageType(),
            message.getContentType(),
            message.getMessageContent(),
            message.getHeaders(),
            message.getProperties(),
            ResultSetMapper.toTimestamp(message.getProcessedAt()),
            ResultSetMapper.toTimestamp(message.getCompletedAt()),
            message.getErrorMessage(),
            message.getRetryCount(),
            message.getCorrelationId(),
            message.getPriority(),
            ResultSetMapper.toTimestamp(message.getUpdatedAt()),
            message.getUpdatedBy() != null ? message.getUpdatedBy().getId() : null,
            message.getId()
        );

        return message;
    }

    /**
     * Update message status
     */
    public int updateStatus(UUID messageId, Message.MessageStatus status) {
        String sql = "UPDATE " + TABLE_NAME + " SET status = ?, updated_at = ? WHERE id = ?";
        return sqlQueryExecutor.update(sql, status.toString(),
                                     ResultSetMapper.toTimestamp(LocalDateTime.now()), messageId);
    }

    /**
     * Update message status with error
     */
    public int updateStatusWithError(UUID messageId, Message.MessageStatus status, String errorMessage) {
        String sql = "UPDATE " + TABLE_NAME + " SET status = ?, error_message = ?, " +
                     "retry_count = retry_count + 1, updated_at = ? WHERE id = ?";
        return sqlQueryExecutor.update(sql, status.toString(), errorMessage,
                                     ResultSetMapper.toTimestamp(LocalDateTime.now()), messageId);
    }

    /**
     * Mark message as processed
     */
    public int markAsProcessed(UUID messageId) {
        LocalDateTime now = LocalDateTime.now();
        String sql = "UPDATE " + TABLE_NAME + " SET status = ?, processed_at = ?, updated_at = ? WHERE id = ?";
        return sqlQueryExecutor.update(sql, Message.MessageStatus.PROCESSED.toString(),
                                     ResultSetMapper.toTimestamp(now),
                                     ResultSetMapper.toTimestamp(now), messageId);
    }

    /**
     * Mark message as completed
     */
    public int markAsCompleted(UUID messageId) {
        LocalDateTime now = LocalDateTime.now();
        String sql = "UPDATE " + TABLE_NAME + " SET status = ?, completed_at = ?, updated_at = ? WHERE id = ?";
        return sqlQueryExecutor.update(sql, Message.MessageStatus.COMPLETED.toString(),
                                     ResultSetMapper.toTimestamp(now),
                                     ResultSetMapper.toTimestamp(now), messageId);
    }

    /**
     * Count messages by status
     */
    public long countByStatus(Message.MessageStatus status) {
        String sql = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE status = ?";
        return sqlQueryExecutor.count(sql, status.toString());
    }

    /**
     * Count messages by flow
     */
    public long countByFlowId(UUID flowId) {
        String sql = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE flow_id = ?";
        return sqlQueryExecutor.count(sql, flowId);
    }

    /**
     * Delete old messages
     */
    public int deleteOldMessages(LocalDateTime before) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE completed_at < ? AND status = ?";
        return sqlQueryExecutor.update(sql, ResultSetMapper.toTimestamp(before),
                                     Message.MessageStatus.COMPLETED.toString());
    }

    /**
     * Find messages for retry
     */
    public List<Message> findMessagesForRetry(int maxRetryCount) {
        String sql = buildSelectWithJoins() +
                     " WHERE m.status = 'RETRY' AND m.retry_count < ? " +
                     "ORDER BY m.priority DESC, m.retry_count ASC, m.received_at ASC";
        return sqlQueryExecutor.queryForList(sql, MESSAGE_WITH_RELATIONSHIPS_ROW_MAPPER, maxRetryCount);
    }

    /**
     * Build SELECT query with all JOINs
     */
    private String buildSelectWithJoins() {
        return "SELECT m.*, " +
               "f.name as flow_name, " +
               "fe.id as flow_execution_id, " +
               "cu.username as created_by_username, cu.email as created_by_email, " +
               "uu.username as updated_by_username, uu.email as updated_by_email " +
               "FROM " + TABLE_NAME + " m " +
               "LEFT JOIN integration_flows f ON m.flow_id = f.id " +
               "LEFT JOIN flow_executions fe ON m.flow_execution_id = fe.id " +
               "LEFT JOIN users cu ON m.created_by = cu.id " +
               "LEFT JOIN users uu ON m.updated_by = uu.id";
    }

    /**
     * Check if message exists by messageId
     */
    public boolean existsByMessageId(String messageId) {
        String sql = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE message_id = ?";
        long count = sqlQueryExecutor.count(sql, messageId);
        return count > 0;
    }

    /**
     * Find messages by status ordered by priority and received time
     */
    public List<Message> findByStatusOrderByPriorityAndReceivedAt(Message.MessageStatus status) {
        String sql = buildSelectWithJoins() +
                     " WHERE m.status = ? ORDER BY m.priority DESC, m.received_at ASC";
        return sqlQueryExecutor.queryForList(sql, MESSAGE_WITH_RELATIONSHIPS_ROW_MAPPER, status.toString());
    }

    /**
     * Find messages by date range
     */
    public List<Message> findByReceivedAtBetween(LocalDateTime start, LocalDateTime end) {
        String sql = buildSelectWithJoins() +
                     " WHERE m.received_at >= ? AND m.received_at <= ? " +
                     "ORDER BY m.received_at DESC";
        return sqlQueryExecutor.queryForList(sql, MESSAGE_WITH_RELATIONSHIPS_ROW_MAPPER,
                                           ResultSetMapper.toTimestamp(start),
                                           ResultSetMapper.toTimestamp(end));
    }

    /**
     * Find messages by date range with pagination
     */
    public Page<Message> findByReceivedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable) {
        String baseSql = buildSelectWithJoins() +
                        " WHERE m.received_at >= ? AND m.received_at <= ? ";

        // Count query
        String countSql = "SELECT COUNT(*) FROM messages m WHERE m.received_at >= ? AND m.received_at <= ?";
        long total = sqlQueryExecutor.count(countSql,
                                          ResultSetMapper.toTimestamp(start),
                                          ResultSetMapper.toTimestamp(end));

        // Data query with pagination
        String sql = baseSql + SqlPaginationHelper.buildOrderByClause(pageable.getSort()) +
                    SqlPaginationHelper.buildPaginationClause(pageable);

        List<Message> messages = sqlQueryExecutor.queryForList(sql, MESSAGE_WITH_RELATIONSHIPS_ROW_MAPPER,
                                                             ResultSetMapper.toTimestamp(start),
                                                             ResultSetMapper.toTimestamp(end));

        return new PageImpl<>(messages, pageable, total);
    }

    /**
     * Calculate average processing time for messages in a time period
     */
    public Double calculateAverageProcessingTimeForPeriod(LocalDateTime start, LocalDateTime end) {
        String sql = "SELECT AVG(EXTRACT(EPOCH FROM (processed_at - received_at))) AS avg_time " +
                    "FROM messages WHERE received_at >= ? AND received_at <= ? " +
                    "AND processed_at IS NOT NULL";

        return sqlQueryExecutor.queryForObject(sql, (rs, rowNum) -> rs.getDouble("avg_time"),
                                             ResultSetMapper.toTimestamp(start),
                                             ResultSetMapper.toTimestamp(end))
                              .orElse(0.0);
    }

    /**
     * Calculate average processing time by business component
     */
    public Double calculateAverageProcessingTimeByBusinessComponent(String businessComponentId) {
        String sql = "SELECT AVG(EXTRACT(EPOCH FROM (m.processed_at - m.received_at))) AS avg_time " +
                    "FROM messages m " +
                    "JOIN flow_executions fe ON m.flow_execution_id = fe.id " +
                    "JOIN integration_flows f ON fe.flow_id = f.id " +
                    "WHERE f.business_component_id = ? AND m.processed_at IS NOT NULL";

        return sqlQueryExecutor.queryForObject(sql, (rs, rowNum) -> rs.getDouble("avg_time"),
                                             UUID.fromString(businessComponentId))
                              .orElse(0.0);
    }

    /**
     * Find messages by status ordered by priority and created date
     */
    public List<Message> findByStatusInOrderByPriorityDescCreatedAtAsc(List<Message.MessageStatus> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return new ArrayList<>();
        }

        String statusList = statuses.stream()
            .map(s -> "'" + s.toString() + "'")
            .collect(java.util.stream.Collectors.joining(","));

        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE status IN (" + statusList + ") " +
                    "ORDER BY priority DESC, created_at ASC";

        return sqlQueryExecutor.queryForList(sql, MESSAGE_ROW_MAPPER);
    }

    /**
     * Count by status grouped by business component
     */
    public List<Object[]> countByStatusGroupedAndBusinessComponent(String businessComponentId) {
        String sql = "SELECT m.status, COUNT(*) FROM messages m " +
                    "JOIN flow_executions fe ON m.flow_execution_id = fe.id " +
                    "JOIN integration_flows f ON fe.flow_id = f.id " +
                    "WHERE f.business_component_id = ? " +
                    "GROUP BY m.status";

        return sqlQueryExecutor.queryForList(sql, (rs, rowNum) -> new Object[] {
            rs.getString(1), rs.getLong(2)
        }, UUID.fromString(businessComponentId));
    }

    /**
     * Count by status grouped by flow
     */
    public List<Object[]> countByStatusGroupedAndFlow(UUID flowId) {
        String sql = "SELECT m.status, COUNT(*) FROM messages m " +
                    "JOIN flow_executions fe ON m.flow_execution_id = fe.id " +
                    "WHERE fe.flow_id = ? " +
                    "GROUP BY m.status";

        return sqlQueryExecutor.queryForList(sql, (rs, rowNum) -> new Object[] {
            rs.getString(1), rs.getLong(2)
        }, flowId);
    }

    /**
     * Count by status grouped with date range
     */
    public List<Object[]> countByStatusGroupedAndDateRange(LocalDateTime start, LocalDateTime end) {
        String sql = "SELECT status, COUNT(*) FROM " + TABLE_NAME +
                    " WHERE created_at BETWEEN ? AND ? " +
                    "GROUP BY status";

        return sqlQueryExecutor.queryForList(sql, (rs, rowNum) -> new Object[] {
            rs.getString(1), rs.getLong(2)
        }, ResultSetMapper.toTimestamp(start), ResultSetMapper.toTimestamp(end));
    }

    /**
     * Count by status grouped
     */
    public List<Object[]> countByStatusGrouped() {
        String sql = "SELECT status, COUNT(*) FROM " + TABLE_NAME + " GROUP BY status";

        return sqlQueryExecutor.queryForList(sql, (rs, rowNum) -> new Object[] {
            rs.getString(1), rs.getLong(2)
        });
    }

    /**
     * Calculate average processing time by flow ID
     */
    public Double calculateAverageProcessingTimeByFlowId(UUID flowId) {
        String sql = "SELECT AVG(EXTRACT(EPOCH FROM (m.processed_at - m.received_at))) AS avg_time " +
                    "FROM messages m " +
                    "JOIN flow_executions fe ON m.flow_execution_id = fe.id " +
                    "WHERE fe.flow_id = ? AND m.processed_at IS NOT NULL";

        return sqlQueryExecutor.queryForObject(sql, (rs, rowNum) -> rs.getDouble("avg_time"), flowId)
                              .orElse(0.0);
    }

    /**
     * Calculate average processing time with date range
     */
    public Double calculateAverageProcessingTimeByDateRange(LocalDateTime start, LocalDateTime end) {
        String sql = "SELECT AVG(EXTRACT(EPOCH FROM (processed_at - received_at))) AS avg_time " +
                    "FROM " + TABLE_NAME +
                    " WHERE processed_at IS NOT NULL AND created_at BETWEEN ? AND ?";

        return sqlQueryExecutor.queryForObject(sql, (rs, rowNum) -> rs.getDouble("avg_time"),
                                             ResultSetMapper.toTimestamp(start),
                                             ResultSetMapper.toTimestamp(end))
                              .orElse(0.0);
    }

    /**
     * Calculate average processing time overall
     */
    public Double calculateAverageProcessingTime() {
        String sql = "SELECT AVG(EXTRACT(EPOCH FROM (processed_at - received_at))) AS avg_time " +
                    "FROM " + TABLE_NAME +
                    " WHERE processed_at IS NOT NULL";

        return sqlQueryExecutor.queryForObject(sql, (rs, rowNum) -> rs.getDouble("avg_time"))
                              .orElse(0.0);
    }
}