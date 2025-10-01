package com.integrixs.data.sql.repository;

import com.integrixs.data.model.FlowRoute;
import com.integrixs.data.model.IntegrationFlow;
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
 * SQL implementation of FlowRouteRepository using native queries.
 */
@Repository
public class FlowRouteSqlRepository extends BaseSqlRepository<FlowRoute, UUID> {

    private static final String TABLE_NAME = "flow_routes";
    private static final String ID_COLUMN = "id";

    /**
     * Row mapper for FlowRoute entity
     */
    private static final RowMapper<FlowRoute> FLOW_ROUTE_ROW_MAPPER = new RowMapper<FlowRoute>() {
        @Override
        public FlowRoute mapRow(ResultSet rs, int rowNum) throws SQLException {
            FlowRoute route = new FlowRoute();
            route.setId(ResultSetMapper.getUUID(rs, "id"));
            route.setRouteName(ResultSetMapper.getString(rs, "route_name"));
            route.setDescription(ResultSetMapper.getString(rs, "description"));
            route.setActive(rs.getBoolean("is_active"));
            route.setPriority(rs.getInt("priority"));
            String routeTypeStr = ResultSetMapper.getString(rs, "route_type");
            if (routeTypeStr != null) {
                route.setRouteType(FlowRoute.RouteType.valueOf(routeTypeStr));
            }
            route.setSourceStep(ResultSetMapper.getString(rs, "source_step"));
            route.setTargetStep(ResultSetMapper.getString(rs, "target_step"));
            route.setDescription(ResultSetMapper.getString(rs, "description"));
            route.setConditionOperator(ResultSetMapper.getString(rs, "condition_operator"));
            route.setCreatedAt(ResultSetMapper.getLocalDateTime(rs, "created_at"));
            route.setUpdatedAt(ResultSetMapper.getLocalDateTime(rs, "updated_at"));

            // Map flow reference
            UUID flowId = ResultSetMapper.getUUID(rs, "flow_id");
            if (flowId != null) {
                IntegrationFlow flow = new IntegrationFlow();
                flow.setId(flowId);
                route.setFlow(flow);
            }

            return route;
        }
    };

    public FlowRouteSqlRepository(SqlQueryExecutor sqlQueryExecutor) {
        super(sqlQueryExecutor, TABLE_NAME, ID_COLUMN, FLOW_ROUTE_ROW_MAPPER);
    }

    public List<FlowRoute> findByFlowId(UUID flowId) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE flow_id = ? ORDER BY priority";
        return sqlQueryExecutor.queryForList(sql, FLOW_ROUTE_ROW_MAPPER, flowId);
    }

    public List<FlowRoute> findByFlowIdAndIsActiveTrue(UUID flowId) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE flow_id = ? AND is_active = true ORDER BY priority";
        return sqlQueryExecutor.queryForList(sql, FLOW_ROUTE_ROW_MAPPER, flowId);
    }

    public List<FlowRoute> findByFlowIdAndSourceStep(UUID flowId, String sourceStep) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE flow_id = ? AND source_step = ?";
        return sqlQueryExecutor.queryForList(sql, FLOW_ROUTE_ROW_MAPPER, flowId, sourceStep);
    }

    @Override
    public FlowRoute save(FlowRoute route) {
        if (route.getId() == null) {
            route.setId(generateId());
        }

        boolean exists = existsById(route.getId());

        if (!exists) {
            return insert(route);
        } else {
            return update(route);
        }
    }

    private FlowRoute insert(FlowRoute route) {
        String sql = "INSERT INTO " + TABLE_NAME + " (" +
                     "id, route_name, description, flow_id, source_step, target_step, " +
                     "is_active, priority, route_type, condition_operator, created_at, updated_at" +
                     ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        LocalDateTime now = LocalDateTime.now();
        if (route.getCreatedAt() == null) {
            route.setCreatedAt(now);
        }
        if (route.getUpdatedAt() == null) {
            route.setUpdatedAt(now);
        }

        sqlQueryExecutor.update(sql,
            route.getId(),
            route.getRouteName(),
            route.getDescription(),
            route.getFlow() != null ? route.getFlow().getId() : null,
            route.getSourceStep(),
            route.getTargetStep(),
            route.isActive(),
            route.getPriority(),
            route.getRouteType() != null ? route.getRouteType().toString() : "CONDITIONAL",
            route.getConditionOperator(),
            ResultSetMapper.toTimestamp(route.getCreatedAt()),
            ResultSetMapper.toTimestamp(route.getUpdatedAt())
        );

        return route;
    }

    @Override
    public FlowRoute update(FlowRoute route) {
        String sql = "UPDATE " + TABLE_NAME + " SET " +
                     "route_name = ?, description = ?, source_step = ?, target_step = ?, " +
                     "is_active = ?, priority = ?, route_type = ?, condition_operator = ?, " +
                     "updated_at = ? WHERE id = ?";

        route.setUpdatedAt(LocalDateTime.now());

        sqlQueryExecutor.update(sql,
            route.getRouteName(),
            route.getDescription(),
            route.getSourceStep(),
            route.getTargetStep(),
            route.isActive(),
            route.getPriority(),
            route.getRouteType() != null ? route.getRouteType().toString() : "CONDITIONAL",
            route.getConditionOperator(),
            ResultSetMapper.toTimestamp(route.getUpdatedAt()),
            route.getId()
        );

        return route;
    }

    public void deleteByFlowId(UUID flowId) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE flow_id = ?";
        sqlQueryExecutor.update(sql, flowId);
    }
}