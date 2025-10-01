package com.integrixs.data.sql.repository;

import com.integrixs.data.model.FlowOrchestrationStep;
import com.integrixs.data.model.IntegrationFlow;
import com.integrixs.data.sql.core.BaseSqlRepository;
import com.integrixs.data.sql.core.ResultSetMapper;
import com.integrixs.data.sql.core.SqlQueryExecutor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * SQL implementation of FlowOrchestrationStepRepository using native queries.
 */
@Repository
public class FlowOrchestrationStepSqlRepository extends BaseSqlRepository<FlowOrchestrationStep, UUID> {

    private static final String TABLE_NAME = "flow_orchestration_steps";
    private static final String ID_COLUMN = "id";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Row mapper for FlowOrchestrationStep entity
     */
    private static final RowMapper<FlowOrchestrationStep> ORCHESTRATION_STEP_ROW_MAPPER = new RowMapper<FlowOrchestrationStep>() {
        @Override
        public FlowOrchestrationStep mapRow(ResultSet rs, int rowNum) throws SQLException {
            FlowOrchestrationStep step = new FlowOrchestrationStep();
            step.setId(ResultSetMapper.getUUID(rs, "id"));
            step.setStepType(ResultSetMapper.getString(rs, "step_type"));
            step.setStepName(ResultSetMapper.getString(rs, "step_name"));
            step.setDescription(ResultSetMapper.getString(rs, "description"));
            step.setExecutionOrder(ResultSetMapper.getInteger(rs, "execution_order"));
            step.setConditionExpression(ResultSetMapper.getString(rs, "condition_expression"));
            step.setIsConditional(rs.getBoolean("is_conditional"));
            step.setIsActive(rs.getBoolean("is_active"));
            step.setTimeoutSeconds(ResultSetMapper.getInteger(rs, "timeout_seconds"));
            step.setRetryAttempts(ResultSetMapper.getInteger(rs, "retry_attempts"));
            step.setRetryDelaySeconds(ResultSetMapper.getInteger(rs, "retry_delay_seconds"));
            step.setTargetAdapterId(ResultSetMapper.getUUID(rs, "target_adapter_id"));
            step.setTargetFlowStructureId(ResultSetMapper.getUUID(rs, "target_flow_structure_id"));
            step.setTransformationId(ResultSetMapper.getUUID(rs, "transformation_id"));
            step.setCreatedAt(ResultSetMapper.getLocalDateTime(rs, "created_at"));
            step.setUpdatedAt(ResultSetMapper.getLocalDateTime(rs, "updated_at"));

            // Handle flow relationship
            UUID flowId = ResultSetMapper.getUUID(rs, "flow_id");
            if (flowId != null) {
                IntegrationFlow flow = new IntegrationFlow();
                flow.setId(flowId);
                step.setFlow(flow);
            }

            // Handle configuration JSON
            String configJson = ResultSetMapper.getString(rs, "configuration");
            if (configJson != null) {
                try {
                    step.setConfiguration(objectMapper.readValue(configJson, new TypeReference<Map<String, Object>>(){}));
                } catch (Exception e) {
                    // Log error but continue
                }
            }

            return step;
        }
    };

    public FlowOrchestrationStepSqlRepository(SqlQueryExecutor sqlQueryExecutor) {
        super(sqlQueryExecutor, TABLE_NAME, ID_COLUMN, ORCHESTRATION_STEP_ROW_MAPPER);
    }

    public List<FlowOrchestrationStep> findByFlowIdOrderByExecutionOrder(UUID flowId) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE flow_id = ? ORDER BY execution_order";
        return sqlQueryExecutor.queryForList(sql, ORCHESTRATION_STEP_ROW_MAPPER, flowId);
    }

    public List<FlowOrchestrationStep> findByFlowIdAndIsActiveTrue(UUID flowId) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE flow_id = ? AND is_active = true ORDER BY execution_order";
        return sqlQueryExecutor.queryForList(sql, ORCHESTRATION_STEP_ROW_MAPPER, flowId);
    }

    @Override
    public FlowOrchestrationStep save(FlowOrchestrationStep step) {
        if (step.getId() == null) {
            step.setId(generateId());
        }

        boolean exists = existsById(step.getId());

        if (!exists) {
            return insert(step);
        } else {
            return update(step);
        }
    }

    private FlowOrchestrationStep insert(FlowOrchestrationStep step) {
        String sql = "INSERT INTO " + TABLE_NAME + " (" +
                     "id, flow_id, step_type, step_name, description, execution_order, " +
                     "configuration, condition_expression, is_conditional, is_active, " +
                     "timeout_seconds, retry_attempts, retry_delay_seconds, " +
                     "target_adapter_id, target_flow_structure_id, transformation_id, " +
                     "created_at, updated_at" +
                     ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        LocalDateTime now = LocalDateTime.now();
        if (step.getCreatedAt() == null) {
            step.setCreatedAt(now);
        }
        if (step.getUpdatedAt() == null) {
            step.setUpdatedAt(now);
        }

        String configJson = null;
        if (step.getConfiguration() != null) {
            try {
                configJson = objectMapper.writeValueAsString(step.getConfiguration());
            } catch (Exception e) {
                throw new RuntimeException("Failed to serialize configuration", e);
            }
        }

        sqlQueryExecutor.update(sql,
            step.getId(),
            step.getFlow() != null ? step.getFlow().getId() : null,
            step.getStepType(),
            step.getStepName(),
            step.getDescription(),
            step.getExecutionOrder(),
            configJson,
            step.getConditionExpression(),
            step.getIsConditional() != null ? step.getIsConditional() : false,
            step.getIsActive() != null ? step.getIsActive() : true,
            step.getTimeoutSeconds(),
            step.getRetryAttempts(),
            step.getRetryDelaySeconds(),
            step.getTargetAdapterId(),
            step.getTargetFlowStructureId(),
            step.getTransformationId(),
            ResultSetMapper.toTimestamp(step.getCreatedAt()),
            ResultSetMapper.toTimestamp(step.getUpdatedAt())
        );

        return step;
    }

    @Override
    public FlowOrchestrationStep update(FlowOrchestrationStep step) {
        String sql = "UPDATE " + TABLE_NAME + " SET " +
                     "step_type = ?, step_name = ?, description = ?, execution_order = ?, " +
                     "configuration = ?, condition_expression = ?, is_conditional = ?, is_active = ?, " +
                     "timeout_seconds = ?, retry_attempts = ?, retry_delay_seconds = ?, " +
                     "target_adapter_id = ?, target_flow_structure_id = ?, transformation_id = ?, " +
                     "updated_at = ? WHERE id = ?";

        step.setUpdatedAt(LocalDateTime.now());

        String configJson = null;
        if (step.getConfiguration() != null) {
            try {
                configJson = objectMapper.writeValueAsString(step.getConfiguration());
            } catch (Exception e) {
                throw new RuntimeException("Failed to serialize configuration", e);
            }
        }

        sqlQueryExecutor.update(sql,
            step.getStepType(),
            step.getStepName(),
            step.getDescription(),
            step.getExecutionOrder(),
            configJson,
            step.getConditionExpression(),
            step.getIsConditional() != null ? step.getIsConditional() : false,
            step.getIsActive() != null ? step.getIsActive() : true,
            step.getTimeoutSeconds(),
            step.getRetryAttempts(),
            step.getRetryDelaySeconds(),
            step.getTargetAdapterId(),
            step.getTargetFlowStructureId(),
            step.getTransformationId(),
            ResultSetMapper.toTimestamp(step.getUpdatedAt()),
            step.getId()
        );

        return step;
    }

    public void deleteByFlowId(UUID flowId) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE flow_id = ?";
        sqlQueryExecutor.update(sql, flowId);
    }
}