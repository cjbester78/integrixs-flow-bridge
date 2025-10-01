package com.integrixs.data.sql.repository;

import com.integrixs.data.model.*;
import com.integrixs.data.sql.core.BaseSqlRepository;
import com.integrixs.data.sql.core.ResultSetMapper;
import com.integrixs.data.sql.core.SqlQueryExecutor;
import com.integrixs.shared.enums.TransformationType;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * SQL implementation of FlowTransformationRepository using native queries.
 * Handles FlowTransformation entity with relationships to IntegrationFlow and Users.
 */
@Repository("flowTransformationSqlRepository")
public class FlowTransformationSqlRepository extends BaseSqlRepository<FlowTransformation, UUID> {

    private static final String TABLE_NAME = "flow_transformations";
    private static final String ID_COLUMN = "id";

    /**
     * Row mapper for FlowTransformation entity (without relationships)
     */
    private static final RowMapper<FlowTransformation> TRANSFORMATION_ROW_MAPPER = new RowMapper<FlowTransformation>() {
        @Override
        public FlowTransformation mapRow(ResultSet rs, int rowNum) throws SQLException {
            FlowTransformation transformation = new FlowTransformation();
            transformation.setId(ResultSetMapper.getUUID(rs, "id"));

            String typeStr = ResultSetMapper.getString(rs, "type");
            if (typeStr != null) {
                transformation.setType(FlowTransformation.TransformationType.valueOf(typeStr));
            }

            transformation.setConfiguration(ResultSetMapper.getString(rs, "configuration"));
            transformation.setExecutionOrder(rs.getInt("execution_order"));
            transformation.setActive(rs.getBoolean("is_active"));
            transformation.setName(ResultSetMapper.getString(rs, "name"));
            transformation.setDescription(ResultSetMapper.getString(rs, "description"));
            transformation.setCreatedAt(ResultSetMapper.getLocalDateTime(rs, "created_at"));
            transformation.setUpdatedAt(ResultSetMapper.getLocalDateTime(rs, "updated_at"));

            return transformation;
        }
    };

    /**
     * Row mapper for FlowTransformation with relationships
     */
    private static final RowMapper<FlowTransformation> TRANSFORMATION_WITH_RELATIONSHIPS_ROW_MAPPER = new RowMapper<FlowTransformation>() {
        @Override
        public FlowTransformation mapRow(ResultSet rs, int rowNum) throws SQLException {
            FlowTransformation transformation = TRANSFORMATION_ROW_MAPPER.mapRow(rs, rowNum);

            // Map created by user
            UUID createdById = ResultSetMapper.getUUID(rs, "created_by");
            if (createdById != null) {
                User createdBy = new User();
                createdBy.setId(createdById);
                createdBy.setUsername(ResultSetMapper.getString(rs, "created_by_username"));
                createdBy.setEmail(ResultSetMapper.getString(rs, "created_by_email"));
                transformation.setCreatedBy(createdBy);
            }

            // Map updated by user
            UUID updatedById = ResultSetMapper.getUUID(rs, "updated_by");
            if (updatedById != null) {
                User updatedBy = new User();
                updatedBy.setId(updatedById);
                updatedBy.setUsername(ResultSetMapper.getString(rs, "updated_by_username"));
                updatedBy.setEmail(ResultSetMapper.getString(rs, "updated_by_email"));
                transformation.setUpdatedBy(updatedBy);
            }

            // Map flow (minimal fields only)
            UUID flowId = ResultSetMapper.getUUID(rs, "flow_id");
            if (flowId != null) {
                IntegrationFlow flow = new IntegrationFlow();
                flow.setId(flowId);
                transformation.setFlow(flow);
            }

            return transformation;
        }
    };

    public FlowTransformationSqlRepository(SqlQueryExecutor sqlQueryExecutor) {
        super(sqlQueryExecutor, TABLE_NAME, ID_COLUMN, TRANSFORMATION_ROW_MAPPER);
    }

    @Override
    public Optional<FlowTransformation> findById(UUID id) {
        String sql = buildSelectWithJoins() + " WHERE ft.id = ?";

        List<FlowTransformation> results = sqlQueryExecutor.queryForList(sql, TRANSFORMATION_WITH_RELATIONSHIPS_ROW_MAPPER, id);
        if (results.isEmpty()) {
            return Optional.empty();
        }

        FlowTransformation transformation = results.get(0);
        // Note: FieldMappings would be loaded separately if needed
        return Optional.of(transformation);
    }

    @Override
    public List<FlowTransformation> findAll() {
        String sql = buildSelectWithJoins() + " ORDER BY ft.flow_id, ft.execution_order";
        return sqlQueryExecutor.queryForList(sql, TRANSFORMATION_WITH_RELATIONSHIPS_ROW_MAPPER);
    }

    public List<FlowTransformation> findByFlowId(UUID flowId) {
        String sql = buildSelectWithJoins() + " WHERE ft.flow_id = ? ORDER BY ft.execution_order";
        return sqlQueryExecutor.queryForList(sql, TRANSFORMATION_WITH_RELATIONSHIPS_ROW_MAPPER, flowId);
    }

    public List<FlowTransformation> findByType(TransformationType type) {
        String sql = buildSelectWithJoins() + " WHERE ft.type = ? ORDER BY ft.name";
        return sqlQueryExecutor.queryForList(sql, TRANSFORMATION_WITH_RELATIONSHIPS_ROW_MAPPER, type.toString());
    }

    public List<FlowTransformation> findActiveByFlowId(UUID flowId) {
        String sql = buildSelectWithJoins() + " WHERE ft.flow_id = ? AND ft.is_active = true ORDER BY ft.execution_order";
        return sqlQueryExecutor.queryForList(sql, TRANSFORMATION_WITH_RELATIONSHIPS_ROW_MAPPER, flowId);
    }

    @Override
    public FlowTransformation save(FlowTransformation transformation) {
        if (transformation.getId() == null) {
            transformation.setId(generateId());
        }

        boolean exists = existsById(transformation.getId());

        if (!exists) {
            return insert(transformation);
        } else {
            return update(transformation);
        }
    }

    private FlowTransformation insert(FlowTransformation transformation) {
        String sql = "INSERT INTO " + TABLE_NAME + " (" +
                     "id, flow_id, type, configuration, execution_order, is_active, " +
                     "name, description, created_at, updated_at, created_by, updated_by" +
                     ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        LocalDateTime now = LocalDateTime.now();
        if (transformation.getCreatedAt() == null) {
            transformation.setCreatedAt(now);
        }
        if (transformation.getUpdatedAt() == null) {
            transformation.setUpdatedAt(now);
        }

        sqlQueryExecutor.update(sql,
            transformation.getId(),
            transformation.getFlow() != null ? transformation.getFlow().getId() : null,
            transformation.getType() != null ? transformation.getType().toString() : null,
            transformation.getConfiguration(),
            transformation.getExecutionOrder(),
            transformation.isActive(),
            transformation.getName(),
            transformation.getDescription(),
            ResultSetMapper.toTimestamp(transformation.getCreatedAt()),
            ResultSetMapper.toTimestamp(transformation.getUpdatedAt()),
            transformation.getCreatedBy() != null ? transformation.getCreatedBy().getId() : null,
            transformation.getUpdatedBy() != null ? transformation.getUpdatedBy().getId() : null
        );

        return transformation;
    }

    @Override
    public FlowTransformation update(FlowTransformation transformation) {
        String sql = "UPDATE " + TABLE_NAME + " SET " +
                     "flow_id = ?, type = ?, configuration = ?, execution_order = ?, " +
                     "is_active = ?, name = ?, description = ?, updated_at = ?, updated_by = ? " +
                     "WHERE id = ?";

        transformation.setUpdatedAt(LocalDateTime.now());

        sqlQueryExecutor.update(sql,
            transformation.getFlow() != null ? transformation.getFlow().getId() : null,
            transformation.getType() != null ? transformation.getType().toString() : null,
            transformation.getConfiguration(),
            transformation.getExecutionOrder(),
            transformation.isActive(),
            transformation.getName(),
            transformation.getDescription(),
            ResultSetMapper.toTimestamp(transformation.getUpdatedAt()),
            transformation.getUpdatedBy() != null ? transformation.getUpdatedBy().getId() : null,
            transformation.getId()
        );

        return transformation;
    }

    /**
     * Delete all transformations for a specific flow
     */
    public void deleteByFlowId(UUID flowId) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE flow_id = ?";
        sqlQueryExecutor.update(sql, flowId);
    }

    /**
     * Count transformations by flow
     */
    public long countByFlowId(UUID flowId) {
        String sql = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE flow_id = ?";
        return sqlQueryExecutor.count(sql, flowId);
    }

    /**
     * Update execution order for transformations
     */
    public void updateExecutionOrder(UUID transformationId, int newOrder) {
        String sql = "UPDATE " + TABLE_NAME + " SET execution_order = ? WHERE id = ?";
        sqlQueryExecutor.update(sql, newOrder, transformationId);
    }

    /**
     * Build SELECT query with all JOINs
     */
    private String buildSelectWithJoins() {
        return "SELECT ft.*, " +
               "cu.username as created_by_username, cu.email as created_by_email, " +
               "uu.username as updated_by_username, uu.email as updated_by_email " +
               "FROM " + TABLE_NAME + " ft " +
               "LEFT JOIN users cu ON ft.created_by = cu.id " +
               "LEFT JOIN users uu ON ft.updated_by = uu.id";
    }

    public boolean existsByFlowIdAndName(UUID flowId, String name) {
        String sql = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE flow_id = ? AND name = ?";
        long count = sqlQueryExecutor.count(sql, flowId, name);
        return count > 0;
    }

    /**
     * Find transformations by execution order range
     */
    public List<FlowTransformation> findByFlowIdAndExecutionOrderBetween(UUID flowId, int startOrder, int endOrder) {
        String sql = buildSelectWithJoins() +
                     " WHERE ft.flow_id = ? AND ft.execution_order >= ? AND ft.execution_order <= ? " +
                     "ORDER BY ft.execution_order";
        return sqlQueryExecutor.queryForList(sql, TRANSFORMATION_WITH_RELATIONSHIPS_ROW_MAPPER, flowId, startOrder, endOrder);
    }

    /**
     * Find transformations by flow ID ordered by execution order
     */
    public List<FlowTransformation> findByFlowIdOrderByExecutionOrder(UUID flowId) {
        String sql = buildSelectWithJoins() +
                     " WHERE ft.flow_id = ? ORDER BY ft.execution_order";
        return sqlQueryExecutor.queryForList(sql, TRANSFORMATION_WITH_RELATIONSHIPS_ROW_MAPPER, flowId);
    }

    /**
     * Find transformation with field mappings by ID
     */
    public Optional<FlowTransformation> findWithFieldMappingsById(UUID id) {
        // First get the transformation
        Optional<FlowTransformation> transformationOpt = findById(id);

        if (transformationOpt.isPresent()) {
            FlowTransformation transformation = transformationOpt.get();
            // Load field mappings
            String sql = "SELECT * FROM field_mappings WHERE transformation_id = ? ORDER BY mapping_order";
            List<FieldMapping> mappings = sqlQueryExecutor.queryForList(sql,
                (rs, rowNum) -> {
                    FieldMapping mapping = new FieldMapping();
                    mapping.setId(ResultSetMapper.getUUID(rs, "id"));
                    mapping.setSourceXPath(ResultSetMapper.getString(rs, "source_xpath"));
                    mapping.setTargetXPath(ResultSetMapper.getString(rs, "target_xpath"));
                    mapping.setMappingRule(ResultSetMapper.getString(rs, "mapping_rule"));
                    String mappingType = ResultSetMapper.getString(rs, "mapping_type");
                    if (mappingType != null) {
                        mapping.setMappingType(FieldMapping.MappingType.valueOf(mappingType));
                    }
                    mapping.setMappingOrder(ResultSetMapper.getInteger(rs, "mapping_order"));
                    mapping.setIsActive(rs.getBoolean("is_active"));
                    mapping.setCreatedAt(ResultSetMapper.getLocalDateTime(rs, "created_at"));
                    mapping.setUpdatedAt(ResultSetMapper.getLocalDateTime(rs, "updated_at"));
                    mapping.setTransformation(transformation);
                    return mapping;
                }, id);
            transformation.setFieldMappings(mappings);
        }

        return transformationOpt;
    }
}