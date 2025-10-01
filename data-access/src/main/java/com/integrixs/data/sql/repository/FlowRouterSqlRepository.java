package com.integrixs.data.sql.repository;

import com.integrixs.data.model.FlowRouter;
import com.integrixs.data.model.IntegrationFlow;
import com.integrixs.data.model.User;
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
import java.util.HashMap;
import java.util.UUID;

/**
 * SQL implementation of FlowRouterRepository using native queries.
 */
@Repository
public class FlowRouterSqlRepository extends BaseSqlRepository<FlowRouter, UUID> {

    private static final String TABLE_NAME = "flow_routers";
    private static final String ID_COLUMN = "id";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Row mapper for FlowRouter entity
     */
    private static final RowMapper<FlowRouter> FLOW_ROUTER_ROW_MAPPER = new RowMapper<FlowRouter>() {
        @Override
        public FlowRouter mapRow(ResultSet rs, int rowNum) throws SQLException {
            FlowRouter router = new FlowRouter();
            router.setId(ResultSetMapper.getUUID(rs, "id"));
            router.setRouterName(ResultSetMapper.getString(rs, "router_name"));
            router.setDescription(ResultSetMapper.getString(rs, "description"));
            router.setInputChannel(ResultSetMapper.getString(rs, "input_channel"));
            router.setDefaultOutputChannel(ResultSetMapper.getString(rs, "default_output_channel"));
            router.setConfiguration(ResultSetMapper.getString(rs, "configuration"));
            router.setEvaluationOrder(ResultSetMapper.getInteger(rs, "evaluation_order"));
            router.setActive(rs.getBoolean("active"));
            // Handle User fields - database stores username as string
            String createdByUsername = ResultSetMapper.getString(rs, "created_by");
            if (createdByUsername != null) {
                User createdBy = new User();
                createdBy.setUsername(createdByUsername);
                router.setCreatedBy(createdBy);
            }

            String updatedByUsername = ResultSetMapper.getString(rs, "updated_by");
            if (updatedByUsername != null) {
                User updatedBy = new User();
                updatedBy.setUsername(updatedByUsername);
                router.setUpdatedBy(updatedBy);
            }
            router.setCreatedAt(ResultSetMapper.getLocalDateTime(rs, "created_at"));
            router.setUpdatedAt(ResultSetMapper.getLocalDateTime(rs, "updated_at"));

            // Handle router type enum
            String routerTypeStr = ResultSetMapper.getString(rs, "router_type");
            if (routerTypeStr != null) {
                router.setRouterType(FlowRouter.RouterType.valueOf(routerTypeStr));
            }

            // Handle flow relationship
            UUID flowId = ResultSetMapper.getUUID(rs, "flow_id");
            if (flowId != null) {
                IntegrationFlow flow = new IntegrationFlow();
                flow.setId(flowId);
                router.setFlow(flow);
            }

            // Handle channel mappings JSON
            String channelMappingsJson = ResultSetMapper.getString(rs, "channel_mappings");
            if (channelMappingsJson != null) {
                try {
                    Map<String, String> mappings = objectMapper.readValue(channelMappingsJson,
                        new TypeReference<Map<String, String>>(){});
                    router.setChannelMappings(mappings);
                } catch (Exception e) {
                    // Log error but continue
                    router.setChannelMappings(new HashMap<>());
                }
            }

            return router;
        }
    };

    public FlowRouterSqlRepository(SqlQueryExecutor sqlQueryExecutor) {
        super(sqlQueryExecutor, TABLE_NAME, ID_COLUMN, FLOW_ROUTER_ROW_MAPPER);
    }

    public List<FlowRouter> findByFlowId(UUID flowId) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE flow_id = ? ORDER BY evaluation_order";
        return sqlQueryExecutor.queryForList(sql, FLOW_ROUTER_ROW_MAPPER, flowId);
    }

    public List<FlowRouter> findByFlowIdAndActiveTrue(UUID flowId) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE flow_id = ? AND active = true ORDER BY evaluation_order";
        return sqlQueryExecutor.queryForList(sql, FLOW_ROUTER_ROW_MAPPER, flowId);
    }

    public List<FlowRouter> findByRouterType(FlowRouter.RouterType routerType) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE router_type = ?";
        return sqlQueryExecutor.queryForList(sql, FLOW_ROUTER_ROW_MAPPER, routerType.name());
    }

    @Override
    public FlowRouter save(FlowRouter router) {
        if (router.getId() == null) {
            router.setId(generateId());
        }

        boolean exists = existsById(router.getId());

        if (!exists) {
            return insert(router);
        } else {
            return update(router);
        }
    }

    private FlowRouter insert(FlowRouter router) {
        String sql = "INSERT INTO " + TABLE_NAME + " (" +
                     "id, router_name, flow_id, router_type, configuration, active, " +
                     "description, input_channel, default_output_channel, channel_mappings, " +
                     "evaluation_order, created_by, updated_by, created_at, updated_at" +
                     ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        LocalDateTime now = LocalDateTime.now();
        if (router.getCreatedAt() == null) {
            router.setCreatedAt(now);
        }
        if (router.getUpdatedAt() == null) {
            router.setUpdatedAt(now);
        }

        String channelMappingsJson = null;
        if (router.getChannelMappings() != null && !router.getChannelMappings().isEmpty()) {
            try {
                channelMappingsJson = objectMapper.writeValueAsString(router.getChannelMappings());
            } catch (Exception e) {
                throw new RuntimeException("Failed to serialize channel mappings", e);
            }
        }

        sqlQueryExecutor.update(sql,
            router.getId(),
            router.getRouterName(),
            router.getFlow() != null ? router.getFlow().getId() : null,
            router.getRouterType() != null ? router.getRouterType().name() : null,
            router.getConfiguration(),
            router.isActive(),
            router.getDescription(),
            router.getInputChannel(),
            router.getDefaultOutputChannel(),
            channelMappingsJson,
            router.getEvaluationOrder() != null ? router.getEvaluationOrder() : 0,
            router.getCreatedBy() != null ? router.getCreatedBy().getUsername() : null,
            router.getUpdatedBy() != null ? router.getUpdatedBy().getUsername() : null,
            ResultSetMapper.toTimestamp(router.getCreatedAt()),
            ResultSetMapper.toTimestamp(router.getUpdatedAt())
        );

        return router;
    }

    @Override
    public FlowRouter update(FlowRouter router) {
        String sql = "UPDATE " + TABLE_NAME + " SET " +
                     "router_name = ?, router_type = ?, configuration = ?, active = ?, " +
                     "description = ?, input_channel = ?, default_output_channel = ?, " +
                     "channel_mappings = ?, evaluation_order = ?, updated_by = ?, updated_at = ? " +
                     "WHERE id = ?";

        router.setUpdatedAt(LocalDateTime.now());

        String channelMappingsJson = null;
        if (router.getChannelMappings() != null && !router.getChannelMappings().isEmpty()) {
            try {
                channelMappingsJson = objectMapper.writeValueAsString(router.getChannelMappings());
            } catch (Exception e) {
                throw new RuntimeException("Failed to serialize channel mappings", e);
            }
        }

        sqlQueryExecutor.update(sql,
            router.getRouterName(),
            router.getRouterType() != null ? router.getRouterType().name() : null,
            router.getConfiguration(),
            router.isActive(),
            router.getDescription(),
            router.getInputChannel(),
            router.getDefaultOutputChannel(),
            channelMappingsJson,
            router.getEvaluationOrder() != null ? router.getEvaluationOrder() : 0,
            router.getUpdatedBy() != null ? router.getUpdatedBy().getUsername() : null,
            ResultSetMapper.toTimestamp(router.getUpdatedAt()),
            router.getId()
        );

        return router;
    }

    public void deleteByFlowId(UUID flowId) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE flow_id = ?";
        sqlQueryExecutor.update(sql, flowId);
    }

    public boolean existsByRouterNameAndFlowId(String routerName, UUID flowId) {
        String sql = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE router_name = ? AND flow_id = ?";
        return sqlQueryExecutor.count(sql, routerName, flowId) > 0;
    }
}