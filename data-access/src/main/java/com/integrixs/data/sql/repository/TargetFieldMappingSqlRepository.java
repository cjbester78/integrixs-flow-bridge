package com.integrixs.data.sql.repository;

import com.integrixs.data.model.TargetFieldMapping;
import com.integrixs.data.model.OrchestrationTarget;
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
 * SQL implementation of TargetFieldMappingRepository using native queries.
 */
@Repository
public class TargetFieldMappingSqlRepository extends BaseSqlRepository<TargetFieldMapping, UUID> {

    private static final String TABLE_NAME = "target_field_mappings";
    private static final String ID_COLUMN = "id";

    /**
     * Row mapper for TargetFieldMapping entity
     */
    private static final RowMapper<TargetFieldMapping> TARGET_FIELD_MAPPING_ROW_MAPPER = new RowMapper<TargetFieldMapping>() {
        @Override
        public TargetFieldMapping mapRow(ResultSet rs, int rowNum) throws SQLException {
            TargetFieldMapping mapping = new TargetFieldMapping();
            mapping.setId(ResultSetMapper.getUUID(rs, "id"));
            mapping.setSourceFieldPath(ResultSetMapper.getString(rs, "source_field_path"));
            mapping.setTargetFieldPath(ResultSetMapper.getString(rs, "target_field_path"));
            mapping.setTransformationExpression(ResultSetMapper.getString(rs, "transformation_expression"));
            mapping.setConstantValue(ResultSetMapper.getString(rs, "constant_value"));
            mapping.setConditionExpression(ResultSetMapper.getString(rs, "condition_expression"));
            mapping.setDefaultValue(ResultSetMapper.getString(rs, "default_value"));
            mapping.setTargetDataType(ResultSetMapper.getString(rs, "target_data_type"));
            mapping.setRequired(rs.getBoolean("required"));
            mapping.setMappingOrder(rs.getInt("mapping_order"));
            mapping.setVisualFlowData(ResultSetMapper.getString(rs, "visual_flow_data"));
            mapping.setValidationRules(ResultSetMapper.getString(rs, "validation_rules"));
            mapping.setDescription(ResultSetMapper.getString(rs, "description"));
            mapping.setActive(rs.getBoolean("active"));
            mapping.setCreatedAt(ResultSetMapper.getLocalDateTime(rs, "created_at"));
            mapping.setUpdatedAt(ResultSetMapper.getLocalDateTime(rs, "updated_at"));

            // Map enum
            String mappingTypeStr = ResultSetMapper.getString(rs, "mapping_type");
            if (mappingTypeStr != null) {
                mapping.setMappingType(TargetFieldMapping.MappingType.valueOf(mappingTypeStr));
            }

            // Map orchestration target reference
            UUID targetId = ResultSetMapper.getUUID(rs, "orchestration_target_id");
            if (targetId != null) {
                OrchestrationTarget target = new OrchestrationTarget();
                target.setId(targetId);
                mapping.setOrchestrationTarget(target);
            }

            return mapping;
        }
    };

    public TargetFieldMappingSqlRepository(SqlQueryExecutor sqlQueryExecutor) {
        super(sqlQueryExecutor, TABLE_NAME, ID_COLUMN, TARGET_FIELD_MAPPING_ROW_MAPPER);
    }

    public List<TargetFieldMapping> findByOrchestrationTargetId(UUID targetId) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE orchestration_target_id = ? ORDER BY mapping_order";
        return sqlQueryExecutor.queryForList(sql, TARGET_FIELD_MAPPING_ROW_MAPPER, targetId);
    }

    public List<TargetFieldMapping> findByOrchestrationTargetIdAndActiveTrue(UUID targetId) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE orchestration_target_id = ? AND active = true ORDER BY mapping_order";
        return sqlQueryExecutor.queryForList(sql, TARGET_FIELD_MAPPING_ROW_MAPPER, targetId);
    }

    public void deleteByOrchestrationTargetId(UUID targetId) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE orchestration_target_id = ?";
        sqlQueryExecutor.update(sql, targetId);
    }

    @Override
    public TargetFieldMapping save(TargetFieldMapping mapping) {
        if (mapping.getId() == null) {
            mapping.setId(generateId());
        }

        boolean exists = existsById(mapping.getId());

        if (!exists) {
            return insert(mapping);
        } else {
            return update(mapping);
        }
    }

    private TargetFieldMapping insert(TargetFieldMapping mapping) {
        String sql = "INSERT INTO " + TABLE_NAME + " (" +
                     "id, orchestration_target_id, source_field_path, target_field_path, mapping_type, " +
                     "transformation_expression, constant_value, condition_expression, default_value, " +
                     "target_data_type, required, mapping_order, visual_flow_data, validation_rules, " +
                     "description, active, created_at, updated_at" +
                     ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        LocalDateTime now = LocalDateTime.now();
        if (mapping.getCreatedAt() == null) {
            mapping.setCreatedAt(now);
        }
        if (mapping.getUpdatedAt() == null) {
            mapping.setUpdatedAt(now);
        }

        sqlQueryExecutor.update(sql,
            mapping.getId(),
            mapping.getOrchestrationTarget() != null ? mapping.getOrchestrationTarget().getId() : null,
            mapping.getSourceFieldPath(),
            mapping.getTargetFieldPath(),
            mapping.getMappingType() != null ? mapping.getMappingType().toString() : "DIRECT",
            mapping.getTransformationExpression(),
            mapping.getConstantValue(),
            mapping.getConditionExpression(),
            mapping.getDefaultValue(),
            mapping.getTargetDataType(),
            mapping.isRequired(),
            mapping.getMappingOrder() != null ? mapping.getMappingOrder() : 0,
            mapping.getVisualFlowData(),
            mapping.getValidationRules(),
            mapping.getDescription(),
            mapping.isActive(),
            ResultSetMapper.toTimestamp(mapping.getCreatedAt()),
            ResultSetMapper.toTimestamp(mapping.getUpdatedAt())
        );

        return mapping;
    }

    @Override
    public TargetFieldMapping update(TargetFieldMapping mapping) {
        String sql = "UPDATE " + TABLE_NAME + " SET " +
                     "source_field_path = ?, target_field_path = ?, mapping_type = ?, " +
                     "transformation_expression = ?, constant_value = ?, condition_expression = ?, " +
                     "default_value = ?, target_data_type = ?, required = ?, mapping_order = ?, " +
                     "visual_flow_data = ?, validation_rules = ?, description = ?, active = ?, " +
                     "updated_at = ? WHERE id = ?";

        mapping.setUpdatedAt(LocalDateTime.now());

        sqlQueryExecutor.update(sql,
            mapping.getSourceFieldPath(),
            mapping.getTargetFieldPath(),
            mapping.getMappingType() != null ? mapping.getMappingType().toString() : "DIRECT",
            mapping.getTransformationExpression(),
            mapping.getConstantValue(),
            mapping.getConditionExpression(),
            mapping.getDefaultValue(),
            mapping.getTargetDataType(),
            mapping.isRequired(),
            mapping.getMappingOrder() != null ? mapping.getMappingOrder() : 0,
            mapping.getVisualFlowData(),
            mapping.getValidationRules(),
            mapping.getDescription(),
            mapping.isActive(),
            ResultSetMapper.toTimestamp(mapping.getUpdatedAt()),
            mapping.getId()
        );

        return mapping;
    }

    public long countByOrchestrationTargetId(UUID targetId) {
        String sql = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE orchestration_target_id = ?";
        return sqlQueryExecutor.count(sql, targetId);
    }
}