package com.integrixs.data.sql.repository;

import com.integrixs.data.model.RouteCondition;
import com.integrixs.data.model.FlowRoute;
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
 * SQL implementation of RouteConditionRepository using native queries.
 */
@Repository
public class RouteConditionSqlRepository extends BaseSqlRepository<RouteCondition, UUID> {

    private static final String TABLE_NAME = "route_conditions";
    private static final String ID_COLUMN = "id";

    /**
     * Row mapper for RouteCondition entity
     */
    private static final RowMapper<RouteCondition> ROUTE_CONDITION_ROW_MAPPER = new RowMapper<RouteCondition>() {
        @Override
        public RouteCondition mapRow(ResultSet rs, int rowNum) throws SQLException {
            RouteCondition condition = new RouteCondition();
            condition.setId(ResultSetMapper.getUUID(rs, "id"));
            condition.setFieldPath(ResultSetMapper.getString(rs, "field_path"));
            condition.setExpectedValue(ResultSetMapper.getString(rs, "expected_value"));
            condition.setSourcePath(ResultSetMapper.getString(rs, "source_path"));
            condition.setOrder(rs.getInt("order_sequence"));
            condition.setActive(rs.getBoolean("is_active"));
            condition.setCreatedAt(ResultSetMapper.getLocalDateTime(rs, "created_at"));
            condition.setUpdatedAt(ResultSetMapper.getLocalDateTime(rs, "updated_at"));

            // Map enums
            String conditionTypeStr = ResultSetMapper.getString(rs, "condition_type");
            if (conditionTypeStr != null) {
                condition.setConditionType(RouteCondition.ConditionType.valueOf(conditionTypeStr));
            }

            String operatorStr = ResultSetMapper.getString(rs, "operator");
            if (operatorStr != null) {
                condition.setOperator(RouteCondition.Operator.valueOf(operatorStr));
            }

            String logicalOperatorStr = ResultSetMapper.getString(rs, "logical_operator");
            if (logicalOperatorStr != null) {
                condition.setLogicalOperator(RouteCondition.LogicalOperator.valueOf(logicalOperatorStr));
            }

            String sourceTypeStr = ResultSetMapper.getString(rs, "source_type");
            if (sourceTypeStr != null) {
                condition.setSourceType(RouteCondition.SourceType.valueOf(sourceTypeStr));
            }

            // Map flow route reference
            UUID flowRouteId = ResultSetMapper.getUUID(rs, "flow_route_id");
            if (flowRouteId != null) {
                FlowRoute flowRoute = new FlowRoute();
                flowRoute.setId(flowRouteId);
                condition.setFlowRoute(flowRoute);
            }

            return condition;
        }
    };

    public RouteConditionSqlRepository(SqlQueryExecutor sqlQueryExecutor) {
        super(sqlQueryExecutor, TABLE_NAME, ID_COLUMN, ROUTE_CONDITION_ROW_MAPPER);
    }

    public List<RouteCondition> findByFlowRouteId(UUID flowRouteId) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE flow_route_id = ? ORDER BY order_sequence";
        return sqlQueryExecutor.queryForList(sql, ROUTE_CONDITION_ROW_MAPPER, flowRouteId);
    }

    public List<RouteCondition> findByFlowRouteIdAndIsActiveTrue(UUID flowRouteId) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE flow_route_id = ? AND is_active = true ORDER BY order_sequence";
        return sqlQueryExecutor.queryForList(sql, ROUTE_CONDITION_ROW_MAPPER, flowRouteId);
    }

    public void deleteByFlowRouteId(UUID flowRouteId) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE flow_route_id = ?";
        sqlQueryExecutor.update(sql, flowRouteId);
    }

    @Override
    public RouteCondition save(RouteCondition condition) {
        if (condition.getId() == null) {
            condition.setId(generateId());
        }

        boolean exists = existsById(condition.getId());

        if (!exists) {
            return insert(condition);
        } else {
            return update(condition);
        }
    }

    private RouteCondition insert(RouteCondition condition) {
        String sql = "INSERT INTO " + TABLE_NAME + " (" +
                     "id, flow_route_id, condition_type, field_path, operator, expected_value, " +
                     "logical_operator, order_sequence, is_active, source_type, source_path, created_at, updated_at" +
                     ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        LocalDateTime now = LocalDateTime.now();
        if (condition.getCreatedAt() == null) {
            condition.setCreatedAt(now);
        }
        if (condition.getUpdatedAt() == null) {
            condition.setUpdatedAt(now);
        }

        sqlQueryExecutor.update(sql,
            condition.getId(),
            condition.getFlowRoute() != null ? condition.getFlowRoute().getId() : null,
            condition.getConditionType() != null ? condition.getConditionType().toString() : null,
            condition.getFieldPath(),
            condition.getOperator() != null ? condition.getOperator().toString() : null,
            condition.getExpectedValue(),
            condition.getLogicalOperator() != null ? condition.getLogicalOperator().toString() : "AND",
            condition.getOrder() != null ? condition.getOrder() : 0,
            condition.isActive(),
            condition.getSourceType() != null ? condition.getSourceType().toString() : "VARIABLE",
            condition.getSourcePath(),
            ResultSetMapper.toTimestamp(condition.getCreatedAt()),
            ResultSetMapper.toTimestamp(condition.getUpdatedAt())
        );

        return condition;
    }

    @Override
    public RouteCondition update(RouteCondition condition) {
        String sql = "UPDATE " + TABLE_NAME + " SET " +
                     "condition_type = ?, field_path = ?, operator = ?, expected_value = ?, " +
                     "logical_operator = ?, order_sequence = ?, is_active = ?, source_type = ?, " +
                     "source_path = ?, updated_at = ? WHERE id = ?";

        condition.setUpdatedAt(LocalDateTime.now());

        sqlQueryExecutor.update(sql,
            condition.getConditionType() != null ? condition.getConditionType().toString() : null,
            condition.getFieldPath(),
            condition.getOperator() != null ? condition.getOperator().toString() : null,
            condition.getExpectedValue(),
            condition.getLogicalOperator() != null ? condition.getLogicalOperator().toString() : "AND",
            condition.getOrder() != null ? condition.getOrder() : 0,
            condition.isActive(),
            condition.getSourceType() != null ? condition.getSourceType().toString() : "VARIABLE",
            condition.getSourcePath(),
            ResultSetMapper.toTimestamp(condition.getUpdatedAt()),
            condition.getId()
        );

        return condition;
    }

    public long countByFlowRouteId(UUID flowRouteId) {
        String sql = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE flow_route_id = ?";
        return sqlQueryExecutor.count(sql, flowRouteId);
    }
}