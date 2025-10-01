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
 * SQL implementation of OrchestrationTargetRepository using native queries.
 * Handles complex entity with embedded RetryPolicy and multiple relationships.
 */
@Repository("orchestrationTargetSqlRepository")
public class OrchestrationTargetSqlRepository extends BaseSqlRepository<OrchestrationTarget, UUID> {

    private static final String TABLE_NAME = "orchestration_targets";
    private static final String ID_COLUMN = "id";

    /**
     * Row mapper for OrchestrationTarget entity (without relationships)
     */
    private static final RowMapper<OrchestrationTarget> TARGET_ROW_MAPPER = new RowMapper<OrchestrationTarget>() {
        @Override
        public OrchestrationTarget mapRow(ResultSet rs, int rowNum) throws SQLException {
            OrchestrationTarget target = new OrchestrationTarget();
            target.setId(ResultSetMapper.getUUID(rs, "id"));
            target.setName(ResultSetMapper.getString(rs, "name"));
            target.setExecutionOrder(ResultSetMapper.getInteger(rs, "execution_order"));
            target.setParallel(rs.getBoolean("is_parallel"));
            target.setRoutingCondition(ResultSetMapper.getString(rs, "routing_condition"));

            String conditionTypeStr = ResultSetMapper.getString(rs, "condition_type");
            if (conditionTypeStr != null) {
                target.setConditionType(OrchestrationTarget.ConditionType.valueOf(conditionTypeStr));
            }

            target.setStructureId(ResultSetMapper.getUUID(rs, "structure_id"));
            target.setResponseStructureId(ResultSetMapper.getUUID(rs, "response_structure_id"));
            target.setAwaitResponse(rs.getBoolean("await_response"));
            target.setTimeoutMs(ResultSetMapper.getLong(rs, "timeout_ms"));

            // Map RetryPolicy (embedded fields)
            OrchestrationTarget.RetryPolicy retryPolicy = new OrchestrationTarget.RetryPolicy();
            retryPolicy.setMaxAttempts(ResultSetMapper.getInteger(rs, "max_attempts"));
            retryPolicy.setRetryDelayMs(ResultSetMapper.getLong(rs, "retry_delay_ms"));
            retryPolicy.setBackoffMultiplier(ResultSetMapper.getDouble(rs, "backoff_multiplier"));
            retryPolicy.setMaxRetryDelayMs(ResultSetMapper.getLong(rs, "max_retry_delay_ms"));
            retryPolicy.setRetryOnErrors(ResultSetMapper.getString(rs, "retry_on_errors"));
            target.setRetryPolicy(retryPolicy);

            String errorStrategyStr = ResultSetMapper.getString(rs, "error_strategy");
            if (errorStrategyStr != null) {
                target.setErrorStrategy(OrchestrationTarget.ErrorStrategy.valueOf(errorStrategyStr));
            }

            target.setActive(rs.getBoolean("is_active"));
            target.setStatus(ResultSetMapper.getString(rs, "status"));
            target.setPriority(ResultSetMapper.getInteger(rs, "priority"));
            target.setConfiguration(ResultSetMapper.getString(rs, "configuration"));
            target.setDescription(ResultSetMapper.getString(rs, "description"));
            target.setCreatedAt(ResultSetMapper.getLocalDateTime(rs, "created_at"));
            target.setUpdatedAt(ResultSetMapper.getLocalDateTime(rs, "updated_at"));

            return target;
        }
    };

    /**
     * Row mapper for OrchestrationTarget with relationships
     */
    private static final RowMapper<OrchestrationTarget> TARGET_WITH_RELATIONSHIPS_ROW_MAPPER = new RowMapper<OrchestrationTarget>() {
        @Override
        public OrchestrationTarget mapRow(ResultSet rs, int rowNum) throws SQLException {
            OrchestrationTarget target = TARGET_ROW_MAPPER.mapRow(rs, rowNum);

            // Map flow (minimal fields)
            UUID flowId = ResultSetMapper.getUUID(rs, "flow_id");
            if (flowId != null) {
                IntegrationFlow flow = new IntegrationFlow();
                flow.setId(flowId);
                target.setFlow(flow);
            }

            // Map target adapter (minimal fields)
            UUID targetAdapterId = ResultSetMapper.getUUID(rs, "target_adapter_id");
            if (targetAdapterId != null) {
                CommunicationAdapter adapter = new CommunicationAdapter();
                adapter.setId(targetAdapterId);
                adapter.setName(ResultSetMapper.getString(rs, "adapter_name"));
                target.setTargetAdapter(adapter);
            }

            // Map target flow (minimal fields)
            UUID targetFlowId = ResultSetMapper.getUUID(rs, "target_flow_id");
            if (targetFlowId != null) {
                IntegrationFlow targetFlow = new IntegrationFlow();
                targetFlow.setId(targetFlowId);
                targetFlow.setName(ResultSetMapper.getString(rs, "target_flow_name"));
                target.setTargetFlow(targetFlow);
            }

            // Map created by user
            UUID createdById = ResultSetMapper.getUUID(rs, "created_by");
            if (createdById != null) {
                User createdBy = new User();
                createdBy.setId(createdById);
                createdBy.setUsername(ResultSetMapper.getString(rs, "created_by_username"));
                createdBy.setEmail(ResultSetMapper.getString(rs, "created_by_email"));
                target.setCreatedBy(createdBy);
            }

            // Map updated by user
            UUID updatedById = ResultSetMapper.getUUID(rs, "updated_by");
            if (updatedById != null) {
                User updatedBy = new User();
                updatedBy.setId(updatedById);
                updatedBy.setUsername(ResultSetMapper.getString(rs, "updated_by_username"));
                updatedBy.setEmail(ResultSetMapper.getString(rs, "updated_by_email"));
                target.setUpdatedBy(updatedBy);
            }

            return target;
        }
    };

    public OrchestrationTargetSqlRepository(SqlQueryExecutor sqlQueryExecutor) {
        super(sqlQueryExecutor, TABLE_NAME, ID_COLUMN, TARGET_ROW_MAPPER);
    }

    @Override
    public Optional<OrchestrationTarget> findById(UUID id) {
        String sql = buildSelectWithJoins() + " WHERE ot.id = ?";

        List<OrchestrationTarget> results = sqlQueryExecutor.queryForList(sql, TARGET_WITH_RELATIONSHIPS_ROW_MAPPER, id);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public List<OrchestrationTarget> findAll() {
        String sql = buildSelectWithJoins() + " ORDER BY ot.flow_id, ot.execution_order";
        return sqlQueryExecutor.queryForList(sql, TARGET_WITH_RELATIONSHIPS_ROW_MAPPER);
    }

    public List<OrchestrationTarget> findByFlowId(UUID flowId) {
        String sql = buildSelectWithJoins() + " WHERE ot.flow_id = ? ORDER BY ot.execution_order, ot.priority DESC";
        return sqlQueryExecutor.queryForList(sql, TARGET_WITH_RELATIONSHIPS_ROW_MAPPER, flowId);
    }

    public List<OrchestrationTarget> findActiveByFlowId(UUID flowId) {
        String sql = buildSelectWithJoins() + " WHERE ot.flow_id = ? AND ot.is_active = true ORDER BY ot.execution_order, ot.priority DESC";
        return sqlQueryExecutor.queryForList(sql, TARGET_WITH_RELATIONSHIPS_ROW_MAPPER, flowId);
    }

    public List<OrchestrationTarget> findByTargetAdapterId(UUID adapterId) {
        String sql = buildSelectWithJoins() + " WHERE ot.target_adapter_id = ? ORDER BY ot.name";
        return sqlQueryExecutor.queryForList(sql, TARGET_WITH_RELATIONSHIPS_ROW_MAPPER, adapterId);
    }

    public List<OrchestrationTarget> findByTargetFlowId(UUID targetFlowId) {
        String sql = buildSelectWithJoins() + " WHERE ot.target_flow_id = ? ORDER BY ot.name";
        return sqlQueryExecutor.queryForList(sql, TARGET_WITH_RELATIONSHIPS_ROW_MAPPER, targetFlowId);
    }

    @Override
    public OrchestrationTarget save(OrchestrationTarget target) {
        if (target.getId() == null) {
            target.setId(generateId());
        }

        boolean exists = existsById(target.getId());

        if (!exists) {
            return insert(target);
        } else {
            return update(target);
        }
    }

    private OrchestrationTarget insert(OrchestrationTarget target) {
        String sql = "INSERT INTO " + TABLE_NAME + " (" +
                     "id, name, flow_id, target_adapter_id, target_flow_id, execution_order, " +
                     "is_parallel, routing_condition, condition_type, structure_id, " +
                     "response_structure_id, await_response, timeout_ms, " +
                     "max_attempts, retry_delay_ms, backoff_multiplier, max_retry_delay_ms, retry_on_errors, " +
                     "error_strategy, is_active, status, priority, configuration, description, " +
                     "created_at, updated_at, created_by, updated_by" +
                     ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        LocalDateTime now = LocalDateTime.now();
        if (target.getCreatedAt() == null) {
            target.setCreatedAt(now);
        }
        if (target.getUpdatedAt() == null) {
            target.setUpdatedAt(now);
        }

        OrchestrationTarget.RetryPolicy retryPolicy = target.getRetryPolicy();
        if (retryPolicy == null) {
            retryPolicy = new OrchestrationTarget.RetryPolicy();
        }

        sqlQueryExecutor.update(sql,
            target.getId(),
            target.getName(),
            target.getFlow() != null ? target.getFlow().getId() : null,
            target.getTargetAdapter() != null ? target.getTargetAdapter().getId() : null,
            target.getTargetFlow() != null ? target.getTargetFlow().getId() : null,
            target.getExecutionOrder(),
            target.isParallel(),
            target.getRoutingCondition(),
            target.getConditionType() != null ? target.getConditionType().toString() : "ALWAYS",
            target.getStructureId(),
            target.getResponseStructureId(),
            target.isAwaitResponse(),
            target.getTimeoutMs(),
            retryPolicy.getMaxAttempts(),
            retryPolicy.getRetryDelayMs(),
            retryPolicy.getBackoffMultiplier(),
            retryPolicy.getMaxRetryDelayMs(),
            retryPolicy.getRetryOnErrors(),
            target.getErrorStrategy() != null ? target.getErrorStrategy().toString() : "FAIL_FLOW",
            target.isActive(),
            target.getStatus(),
            target.getPriority(),
            target.getConfiguration(),
            target.getDescription(),
            ResultSetMapper.toTimestamp(target.getCreatedAt()),
            ResultSetMapper.toTimestamp(target.getUpdatedAt()),
            target.getCreatedBy() != null ? target.getCreatedBy().getId() : null,
            target.getUpdatedBy() != null ? target.getUpdatedBy().getId() : null
        );

        return target;
    }

    @Override
    public OrchestrationTarget update(OrchestrationTarget target) {
        String sql = "UPDATE " + TABLE_NAME + " SET " +
                     "name = ?, flow_id = ?, target_adapter_id = ?, target_flow_id = ?, " +
                     "execution_order = ?, is_parallel = ?, routing_condition = ?, " +
                     "condition_type = ?, structure_id = ?, response_structure_id = ?, " +
                     "await_response = ?, timeout_ms = ?, " +
                     "max_attempts = ?, retry_delay_ms = ?, backoff_multiplier = ?, " +
                     "max_retry_delay_ms = ?, retry_on_errors = ?, " +
                     "error_strategy = ?, is_active = ?, status = ?, priority = ?, " +
                     "configuration = ?, description = ?, updated_at = ?, updated_by = ? " +
                     "WHERE id = ?";

        target.setUpdatedAt(LocalDateTime.now());

        OrchestrationTarget.RetryPolicy retryPolicy = target.getRetryPolicy();
        if (retryPolicy == null) {
            retryPolicy = new OrchestrationTarget.RetryPolicy();
        }

        sqlQueryExecutor.update(sql,
            target.getName(),
            target.getFlow() != null ? target.getFlow().getId() : null,
            target.getTargetAdapter() != null ? target.getTargetAdapter().getId() : null,
            target.getTargetFlow() != null ? target.getTargetFlow().getId() : null,
            target.getExecutionOrder(),
            target.isParallel(),
            target.getRoutingCondition(),
            target.getConditionType() != null ? target.getConditionType().toString() : "ALWAYS",
            target.getStructureId(),
            target.getResponseStructureId(),
            target.isAwaitResponse(),
            target.getTimeoutMs(),
            retryPolicy.getMaxAttempts(),
            retryPolicy.getRetryDelayMs(),
            retryPolicy.getBackoffMultiplier(),
            retryPolicy.getMaxRetryDelayMs(),
            retryPolicy.getRetryOnErrors(),
            target.getErrorStrategy() != null ? target.getErrorStrategy().toString() : "FAIL_FLOW",
            target.isActive(),
            target.getStatus(),
            target.getPriority(),
            target.getConfiguration(),
            target.getDescription(),
            ResultSetMapper.toTimestamp(target.getUpdatedAt()),
            target.getUpdatedBy() != null ? target.getUpdatedBy().getId() : null,
            target.getId()
        );

        return target;
    }

    /**
     * Delete all orchestration targets for a specific flow
     */
    public void deleteByFlowId(UUID flowId) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE flow_id = ?";
        sqlQueryExecutor.update(sql, flowId);
    }

    /**
     * Count orchestration targets by flow
     */
    public long countByFlowId(UUID flowId) {
        String sql = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE flow_id = ?";
        return sqlQueryExecutor.count(sql, flowId);
    }

    /**
     * Find targets by condition type
     */
    public List<OrchestrationTarget> findByConditionType(OrchestrationTarget.ConditionType conditionType) {
        String sql = buildSelectWithJoins() + " WHERE ot.condition_type = ? ORDER BY ot.name";
        return sqlQueryExecutor.queryForList(sql, TARGET_WITH_RELATIONSHIPS_ROW_MAPPER, conditionType.toString());
    }

    /**
     * Find targets that execute in parallel
     */
    public List<OrchestrationTarget> findParallelTargetsByFlowId(UUID flowId) {
        String sql = buildSelectWithJoins() + " WHERE ot.flow_id = ? AND ot.is_parallel = true ORDER BY ot.execution_order";
        return sqlQueryExecutor.queryForList(sql, TARGET_WITH_RELATIONSHIPS_ROW_MAPPER, flowId);
    }

    /**
     * Update execution order for a target
     */
    public void updateExecutionOrder(UUID targetId, int newOrder) {
        String sql = "UPDATE " + TABLE_NAME + " SET execution_order = ?, updated_at = ? WHERE id = ?";
        sqlQueryExecutor.update(sql, newOrder, ResultSetMapper.toTimestamp(LocalDateTime.now()), targetId);
    }

    /**
     * Build SELECT query with all JOINs
     */
    private String buildSelectWithJoins() {
        return "SELECT ot.*, " +
               "ca.name as adapter_name, " +
               "tf.name as target_flow_name, " +
               "cu.username as created_by_username, cu.email as created_by_email, " +
               "uu.username as updated_by_username, uu.email as updated_by_email " +
               "FROM " + TABLE_NAME + " ot " +
               "LEFT JOIN communication_adapters ca ON ot.target_adapter_id = ca.id " +
               "LEFT JOIN integration_flows tf ON ot.target_flow_id = tf.id " +
               "LEFT JOIN users cu ON ot.created_by = cu.id " +
               "LEFT JOIN users uu ON ot.updated_by = uu.id";
    }

    /**
     * Check if target exists by name within a flow
     */
    public boolean existsByFlowIdAndName(UUID flowId, String name) {
        String sql = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE flow_id = ? AND name = ?";
        long count = sqlQueryExecutor.count(sql, flowId, name);
        return count > 0;
    }

    /**
     * Find targets awaiting response
     */
    public List<OrchestrationTarget> findByFlowIdAndAwaitResponseTrue(UUID flowId) {
        String sql = buildSelectWithJoins() + " WHERE ot.flow_id = ? AND ot.await_response = true ORDER BY ot.execution_order";
        return sqlQueryExecutor.queryForList(sql, TARGET_WITH_RELATIONSHIPS_ROW_MAPPER, flowId);
    }

    /**
     * Find targets by flow ID ordered by execution order
     */
    public List<OrchestrationTarget> findByFlowIdOrderByExecutionOrder(UUID flowId) {
        String sql = buildSelectWithJoins() + " WHERE ot.flow_id = ? ORDER BY ot.execution_order";
        return sqlQueryExecutor.queryForList(sql, TARGET_WITH_RELATIONSHIPS_ROW_MAPPER, flowId);
    }

    /**
     * Check if target exists
     */
    public boolean existsByFlowIdAndTargetAdapterId(UUID flowId, UUID targetAdapterId) {
        String sql = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE flow_id = ? AND target_adapter_id = ?";
        long count = sqlQueryExecutor.count(sql, flowId, targetAdapterId);
        return count > 0;
    }

    /**
     * Get max execution order for a flow
     */
    public Integer getMaxExecutionOrder(UUID flowId) {
        String sql = "SELECT MAX(execution_order) FROM " + TABLE_NAME + " WHERE flow_id = ?";
        return sqlQueryExecutor.queryForObject(sql, (rs, rowNum) -> rs.getObject(1) != null ? rs.getInt(1) : null, flowId)
                .orElse(null);
    }

    /**
     * Delete by entity
     */
    public void delete(OrchestrationTarget target) {
        deleteById(target.getId());
    }

    /**
     * Save all targets
     */
    public List<OrchestrationTarget> saveAll(List<OrchestrationTarget> targets) {
        List<OrchestrationTarget> saved = new ArrayList<>();
        for (OrchestrationTarget target : targets) {
            saved.add(save(target));
        }
        return saved;
    }

    /**
     * Find by status
     */
    public List<OrchestrationTarget> findByStatus(String status) {
        String sql = buildSelectWithJoins() + " WHERE ot.status = ? ORDER BY ot.priority DESC";
        return sqlQueryExecutor.queryForList(sql, TARGET_WITH_RELATIONSHIPS_ROW_MAPPER, status);
    }

    /**
     * Find by source adapter ID
     */
    public List<OrchestrationTarget> findBySourceAdapterId(UUID sourceAdapterId) {
        String sql = buildSelectWithJoins() + " WHERE ot.source_adapter_id = ? ORDER BY ot.execution_order";
        return sqlQueryExecutor.queryForList(sql, TARGET_WITH_RELATIONSHIPS_ROW_MAPPER, sourceAdapterId);
    }

    /**
     * Helper method to load related entities for a target
     */
    private void loadRelatedEntities(OrchestrationTarget target) {
        // This method is for compatibility - the joins already load the basic info
    }

    /**
     * Find by flow ID with pagination
     */
    public Page<OrchestrationTarget> findByFlowIdOrderByExecutionOrderAsc(UUID flowId, Pageable pageable) {
        String baseQuery = buildSelectWithJoins() + " WHERE ot.flow_id = ?";
        String countQuery = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE flow_id = ?";

        // Get total count
        long total = sqlQueryExecutor.count(countQuery, flowId);

        // Build final query with ordering and pagination
        String orderBy = " ORDER BY ot.execution_order ASC";
        String paginationClause = SqlPaginationHelper.buildPaginationClause(pageable);
        String finalQuery = baseQuery + orderBy + paginationClause;

        List<OrchestrationTarget> targets = sqlQueryExecutor.queryForList(finalQuery, TARGET_WITH_RELATIONSHIPS_ROW_MAPPER, flowId);

        return new PageImpl<>(targets, pageable, total);
    }

    /**
     * Find by flow with pagination
     */
    public Page<OrchestrationTarget> findByFlowOrderByExecutionOrderAsc(IntegrationFlow flow, Pageable pageable) {
        return findByFlowIdOrderByExecutionOrderAsc(flow.getId(), pageable);
    }
}