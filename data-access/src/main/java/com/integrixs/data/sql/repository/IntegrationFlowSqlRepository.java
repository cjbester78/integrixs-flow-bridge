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
 * SQL implementation of IntegrationFlowRepository using native queries.
 */
@Repository("integrationFlowSqlRepository")
public class IntegrationFlowSqlRepository extends BaseSqlRepository<IntegrationFlow, UUID> {

    private static final String TABLE_NAME = "integration_flows";
    private static final String ID_COLUMN = "id";

    /**
     * Row mapper for IntegrationFlow entity (without relationships)
     */
    private static final RowMapper<IntegrationFlow> FLOW_ROW_MAPPER = new RowMapper<IntegrationFlow>() {
        @Override
        public IntegrationFlow mapRow(ResultSet rs, int rowNum) throws SQLException {
            IntegrationFlow flow = new IntegrationFlow();
            flow.setId(ResultSetMapper.getUUID(rs, "id"));
            flow.setName(ResultSetMapper.getString(rs, "name"));
            flow.setDescription(ResultSetMapper.getString(rs, "description"));
            flow.setTenantId(ResultSetMapper.getUUID(rs, "tenant_id"));
            flow.setInboundAdapterId(ResultSetMapper.getUUID(rs, "source_adapter_id"));
            flow.setOutboundAdapterId(ResultSetMapper.getUUID(rs, "target_adapter_id"));
            flow.setSourceFlowStructureId(ResultSetMapper.getUUID(rs, "source_flow_structure_id"));
            flow.setTargetFlowStructureId(ResultSetMapper.getUUID(rs, "target_flow_structure_id"));

            String statusStr = ResultSetMapper.getString(rs, "status");
            if (statusStr != null) {
                flow.setStatus(FlowStatus.valueOf(statusStr));
            }

            flow.setActive(rs.getBoolean("is_active"));

            String mappingModeStr = ResultSetMapper.getString(rs, "mapping_mode");
            if (mappingModeStr != null) {
                flow.setMappingMode(MappingMode.valueOf(mappingModeStr));
            }

            flow.setSkipXmlConversion(rs.getBoolean("skip_xml_conversion"));
            flow.setVersion(ResultSetMapper.getString(rs, "version"));

            String flowTypeStr = ResultSetMapper.getString(rs, "flow_type");
            if (flowTypeStr != null) {
                flow.setFlowType(FlowType.valueOf(flowTypeStr));
            }

            flow.setDeployedAt(ResultSetMapper.getLocalDateTime(rs, "deployed_at"));
            flow.setDeployedBy(ResultSetMapper.getUUID(rs, "deployed_by"));
            flow.setDeploymentEndpoint(ResultSetMapper.getString(rs, "deployment_endpoint"));
            flow.setDeploymentMetadata(ResultSetMapper.getString(rs, "deployment_metadata"));
            flow.setCreatedAt(ResultSetMapper.getLocalDateTime(rs, "created_at"));
            flow.setUpdatedAt(ResultSetMapper.getLocalDateTime(rs, "updated_at"));
            flow.setLastExecutionAt(ResultSetMapper.getLocalDateTime(rs, "last_execution_at"));
            flow.setExecutionCount(rs.getInt("execution_count"));
            flow.setSuccessCount(rs.getInt("success_count"));
            flow.setErrorCount(rs.getInt("error_count"));

            return flow;
        }
    };

    /**
     * Row mapper for IntegrationFlow with relationships
     */
    private static final RowMapper<IntegrationFlow> FLOW_WITH_RELATIONSHIPS_ROW_MAPPER = new RowMapper<IntegrationFlow>() {
        @Override
        public IntegrationFlow mapRow(ResultSet rs, int rowNum) throws SQLException {
            IntegrationFlow flow = FLOW_ROW_MAPPER.mapRow(rs, rowNum);

            // Map business component
            UUID businessComponentId = ResultSetMapper.getUUID(rs, "business_component_id");
            if (businessComponentId != null) {
                BusinessComponent bc = new BusinessComponent();
                bc.setId(businessComponentId);
                bc.setName(ResultSetMapper.getString(rs, "bc_name"));
                bc.setDescription(ResultSetMapper.getString(rs, "bc_description"));
                flow.setBusinessComponent(bc);
            }

            // Map created by user
            UUID createdById = ResultSetMapper.getUUID(rs, "created_by");
            if (createdById != null) {
                User createdBy = new User();
                createdBy.setId(createdById);
                createdBy.setUsername(ResultSetMapper.getString(rs, "created_by_username"));
                createdBy.setEmail(ResultSetMapper.getString(rs, "created_by_email"));
                flow.setCreatedBy(createdBy);
            }

            // Map updated by user
            UUID updatedById = ResultSetMapper.getUUID(rs, "updated_by");
            if (updatedById != null) {
                User updatedBy = new User();
                updatedBy.setId(updatedById);
                updatedBy.setUsername(ResultSetMapper.getString(rs, "updated_by_username"));
                updatedBy.setEmail(ResultSetMapper.getString(rs, "updated_by_email"));
                flow.setUpdatedBy(updatedBy);
            }

            return flow;
        }
    };

    private final FlowTransformationSqlRepository transformationRepository;
    private final OrchestrationTargetSqlRepository orchestrationTargetRepository;

    public IntegrationFlowSqlRepository(SqlQueryExecutor sqlQueryExecutor,
                                       FlowTransformationSqlRepository transformationRepository,
                                       OrchestrationTargetSqlRepository orchestrationTargetRepository) {
        super(sqlQueryExecutor, TABLE_NAME, ID_COLUMN, FLOW_ROW_MAPPER);
        this.transformationRepository = transformationRepository;
        this.orchestrationTargetRepository = orchestrationTargetRepository;
    }

    @Override
    public Optional<IntegrationFlow> findById(UUID id) {
        String sql = buildSelectWithJoins() + " WHERE ifl.id = ?";

        List<IntegrationFlow> results = sqlQueryExecutor.queryForList(sql, FLOW_WITH_RELATIONSHIPS_ROW_MAPPER, id);
        if (results.isEmpty()) {
            return Optional.empty();
        }

        IntegrationFlow flow = results.get(0);
        loadCollections(flow);
        return Optional.of(flow);
    }

    @Override
    public List<IntegrationFlow> findAll() {
        String sql = buildSelectWithJoins() + " ORDER BY ifl.name";
        List<IntegrationFlow> flows = sqlQueryExecutor.queryForList(sql, FLOW_WITH_RELATIONSHIPS_ROW_MAPPER);

        for (IntegrationFlow flow : flows) {
            loadCollections(flow);
        }

        return flows;
    }

    public Page<IntegrationFlow> findAll(Pageable pageable) {
        String baseQuery = buildSelectWithJoins();
        String countQuery = "SELECT COUNT(*) FROM " + TABLE_NAME;

        long total = sqlQueryExecutor.count(countQuery);
        String paginatedQuery = baseQuery + SqlPaginationHelper.buildOrderByClause(pageable.getSort()) + SqlPaginationHelper.buildPaginationClause(pageable);
        List<IntegrationFlow> flows = sqlQueryExecutor.queryForList(paginatedQuery, FLOW_WITH_RELATIONSHIPS_ROW_MAPPER);

        for (IntegrationFlow flow : flows) {
            loadCollections(flow);
        }

        return new PageImpl<>(flows, pageable, total);
    }

    public Optional<IntegrationFlow> findByName(String name) {
        String sql = buildSelectWithJoins() + " WHERE ifl.name = ?";

        List<IntegrationFlow> results = sqlQueryExecutor.queryForList(sql, FLOW_WITH_RELATIONSHIPS_ROW_MAPPER, name);
        if (results.isEmpty()) {
            return Optional.empty();
        }

        IntegrationFlow flow = results.get(0);
        loadCollections(flow);
        return Optional.of(flow);
    }

    public List<IntegrationFlow> findByBusinessComponentId(UUID businessComponentId) {
        String sql = buildSelectWithJoins() + " WHERE ifl.business_component_id = ? ORDER BY ifl.name";
        List<IntegrationFlow> flows = sqlQueryExecutor.queryForList(sql, FLOW_WITH_RELATIONSHIPS_ROW_MAPPER, businessComponentId);

        for (IntegrationFlow flow : flows) {
            loadCollections(flow);
        }

        return flows;
    }

    public List<IntegrationFlow> findByStatus(FlowStatus status) {
        String sql = buildSelectWithJoins() + " WHERE ifl.status = ? ORDER BY ifl.name";
        List<IntegrationFlow> flows = sqlQueryExecutor.queryForList(sql, FLOW_WITH_RELATIONSHIPS_ROW_MAPPER, status.toString());

        for (IntegrationFlow flow : flows) {
            loadCollections(flow);
        }

        return flows;
    }

    public List<IntegrationFlow> findActiveFlows() {
        String sql = buildSelectWithJoins() + " WHERE ifl.is_active = true ORDER BY ifl.name";
        List<IntegrationFlow> flows = sqlQueryExecutor.queryForList(sql, FLOW_WITH_RELATIONSHIPS_ROW_MAPPER);

        for (IntegrationFlow flow : flows) {
            loadCollections(flow);
        }

        return flows;
    }

    public List<IntegrationFlow> findByTenantId(UUID tenantId) {
        String sql = buildSelectWithJoins() + " WHERE ifl.tenant_id = ? ORDER BY ifl.name";
        List<IntegrationFlow> flows = sqlQueryExecutor.queryForList(sql, FLOW_WITH_RELATIONSHIPS_ROW_MAPPER, tenantId);

        for (IntegrationFlow flow : flows) {
            loadCollections(flow);
        }

        return flows;
    }

    public List<IntegrationFlow> findByInboundAdapterId(UUID adapterId) {
        String sql = buildSelectWithJoins() + " WHERE ifl.source_adapter_id = ? ORDER BY ifl.name";
        List<IntegrationFlow> flows = sqlQueryExecutor.queryForList(sql, FLOW_WITH_RELATIONSHIPS_ROW_MAPPER, adapterId);

        for (IntegrationFlow flow : flows) {
            loadCollections(flow);
        }

        return flows;
    }

    public List<IntegrationFlow> findByOutboundAdapterId(UUID adapterId) {
        String sql = buildSelectWithJoins() + " WHERE ifl.target_adapter_id = ? ORDER BY ifl.name";
        List<IntegrationFlow> flows = sqlQueryExecutor.queryForList(sql, FLOW_WITH_RELATIONSHIPS_ROW_MAPPER, adapterId);

        for (IntegrationFlow flow : flows) {
            loadCollections(flow);
        }

        return flows;
    }

    public List<IntegrationFlow> findByFlowType(FlowType flowType) {
        String sql = buildSelectWithJoins() + " WHERE ifl.flow_type = ? ORDER BY ifl.name";
        List<IntegrationFlow> flows = sqlQueryExecutor.queryForList(sql, FLOW_WITH_RELATIONSHIPS_ROW_MAPPER, flowType.toString());

        for (IntegrationFlow flow : flows) {
            loadCollections(flow);
        }

        return flows;
    }

    @Override
    public IntegrationFlow save(IntegrationFlow flow) {
        if (flow.getId() == null) {
            flow.setId(generateId());
        }

        boolean exists = existsById(flow.getId());

        if (!exists) {
            flow = insert(flow);
        } else {
            flow = update(flow);
        }

        // Save collections
        saveCollections(flow);

        return flow;
    }

    private IntegrationFlow insert(IntegrationFlow flow) {
        String sql = "INSERT INTO " + TABLE_NAME + " (" +
                     "id, name, description, tenant_id, source_adapter_id, target_adapter_id, " +
                     "source_flow_structure_id, target_flow_structure_id, status, is_active, " +
                     "mapping_mode, skip_xml_conversion, version, flow_type, deployed_at, " +
                     "deployed_by, deployment_endpoint, deployment_metadata, created_at, " +
                     "updated_at, created_by, updated_by, business_component_id, " +
                     "last_execution_at, execution_count, success_count, error_count" +
                     ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        LocalDateTime now = LocalDateTime.now();
        if (flow.getCreatedAt() == null) {
            flow.setCreatedAt(now);
        }
        if (flow.getUpdatedAt() == null) {
            flow.setUpdatedAt(now);
        }

        sqlQueryExecutor.update(sql,
            flow.getId(),
            flow.getName(),
            flow.getDescription(),
            flow.getTenantId(),
            flow.getInboundAdapterId(),
            flow.getOutboundAdapterId(),
            flow.getSourceFlowStructureId(),
            flow.getTargetFlowStructureId(),
            flow.getStatus() != null ? flow.getStatus().toString() : "DRAFT",
            flow.isActive(),
            flow.getMappingMode() != null ? flow.getMappingMode().toString() : "WITH_MAPPING",
            flow.isSkipXmlConversion(),
            flow.getVersion(),
            flow.getFlowType() != null ? flow.getFlowType().toString() : "DIRECT_MAPPING",
            ResultSetMapper.toTimestamp(flow.getDeployedAt()),
            flow.getDeployedBy(),
            flow.getDeploymentEndpoint(),
            flow.getDeploymentMetadata(),
            ResultSetMapper.toTimestamp(flow.getCreatedAt()),
            ResultSetMapper.toTimestamp(flow.getUpdatedAt()),
            flow.getCreatedBy() != null ? flow.getCreatedBy().getId() : null,
            flow.getUpdatedBy() != null ? flow.getUpdatedBy().getId() : null,
            flow.getBusinessComponent() != null ? flow.getBusinessComponent().getId() : null,
            ResultSetMapper.toTimestamp(flow.getLastExecutionAt()),
            flow.getExecutionCount(),
            flow.getSuccessCount(),
            flow.getErrorCount()
        );

        return flow;
    }

    @Override
    public IntegrationFlow update(IntegrationFlow flow) {
        String sql = "UPDATE " + TABLE_NAME + " SET " +
                     "name = ?, description = ?, tenant_id = ?, source_adapter_id = ?, " +
                     "target_adapter_id = ?, source_flow_structure_id = ?, target_flow_structure_id = ?, " +
                     "status = ?, is_active = ?, mapping_mode = ?, skip_xml_conversion = ?, " +
                     "version = ?, flow_type = ?, deployed_at = ?, deployed_by = ?, " +
                     "deployment_endpoint = ?, deployment_metadata = ?, updated_at = ?, " +
                     "updated_by = ?, business_component_id = ?, last_execution_at = ?, " +
                     "execution_count = ?, success_count = ?, error_count = ? " +
                     "WHERE id = ?";

        flow.setUpdatedAt(LocalDateTime.now());

        sqlQueryExecutor.update(sql,
            flow.getName(),
            flow.getDescription(),
            flow.getTenantId(),
            flow.getInboundAdapterId(),
            flow.getOutboundAdapterId(),
            flow.getSourceFlowStructureId(),
            flow.getTargetFlowStructureId(),
            flow.getStatus() != null ? flow.getStatus().toString() : "DRAFT",
            flow.isActive(),
            flow.getMappingMode() != null ? flow.getMappingMode().toString() : "WITH_MAPPING",
            flow.isSkipXmlConversion(),
            flow.getVersion(),
            flow.getFlowType() != null ? flow.getFlowType().toString() : "DIRECT_MAPPING",
            ResultSetMapper.toTimestamp(flow.getDeployedAt()),
            flow.getDeployedBy(),
            flow.getDeploymentEndpoint(),
            flow.getDeploymentMetadata(),
            ResultSetMapper.toTimestamp(flow.getUpdatedAt()),
            flow.getUpdatedBy() != null ? flow.getUpdatedBy().getId() : null,
            flow.getBusinessComponent() != null ? flow.getBusinessComponent().getId() : null,
            ResultSetMapper.toTimestamp(flow.getLastExecutionAt()),
            flow.getExecutionCount(),
            flow.getSuccessCount(),
            flow.getErrorCount(),
            flow.getId()
        );

        return flow;
    }

    /**
     * Build SELECT query with all JOINs
     */
    private String buildSelectWithJoins() {
        return "SELECT ifl.*, " +
               "bc.name as bc_name, bc.description as bc_description, " +
               "cu.username as created_by_username, cu.email as created_by_email, " +
               "uu.username as updated_by_username, uu.email as updated_by_email " +
               "FROM " + TABLE_NAME + " ifl " +
               "LEFT JOIN business_components bc ON ifl.business_component_id = bc.id " +
               "LEFT JOIN users cu ON ifl.created_by = cu.id " +
               "LEFT JOIN users uu ON ifl.updated_by = uu.id";
    }

    /**
     * Load collections (transformations and orchestration targets)
     */
    private void loadCollections(IntegrationFlow flow) {
        // Load transformations
        List<FlowTransformation> transformations = transformationRepository.findByFlowId(flow.getId());
        flow.setTransformations(transformations);

        // Load orchestration targets
        List<OrchestrationTarget> targets = orchestrationTargetRepository.findByFlowId(flow.getId());
        flow.setOrchestrationTargets(targets);
    }

    /**
     * Save collections (transformations and orchestration targets)
     */
    private void saveCollections(IntegrationFlow flow) {
        // Save transformations
        if (flow.getTransformations() != null) {
            transformationRepository.deleteByFlowId(flow.getId());
            for (FlowTransformation transformation : flow.getTransformations()) {
                transformation.setFlow(flow);
                transformationRepository.save(transformation);
            }
        }

        // Save orchestration targets
        if (flow.getOrchestrationTargets() != null) {
            orchestrationTargetRepository.deleteByFlowId(flow.getId());
            for (OrchestrationTarget target : flow.getOrchestrationTargets()) {
                target.setFlow(flow);
                orchestrationTargetRepository.save(target);
            }
        }
    }

    public boolean existsByName(String name) {
        String sql = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE name = ?";
        long count = sqlQueryExecutor.count(sql, name);
        return count > 0;
    }

    public int updateExecutionStats(UUID flowId, boolean success) {
        String sql = "UPDATE " + TABLE_NAME + " SET " +
                     "execution_count = execution_count + 1, " +
                     (success ? "success_count = success_count + 1, " : "error_count = error_count + 1, ") +
                     "last_execution_at = ? " +
                     "WHERE id = ?";

        return sqlQueryExecutor.update(sql, ResultSetMapper.toTimestamp(LocalDateTime.now()), flowId);
    }

    public int updateDeploymentInfo(UUID flowId, LocalDateTime deployedAt, UUID deployedBy,
                                   String endpoint, String metadata) {
        String sql = "UPDATE " + TABLE_NAME + " SET " +
                     "deployed_at = ?, deployed_by = ?, deployment_endpoint = ?, " +
                     "deployment_metadata = ?, status = ? " +
                     "WHERE id = ?";

        return sqlQueryExecutor.update(sql,
            ResultSetMapper.toTimestamp(deployedAt),
            deployedBy,
            endpoint,
            metadata,
            FlowStatus.ACTIVE.toString(),
            flowId
        );
    }

    public long countByBusinessComponentId(UUID businessComponentId) {
        String sql = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE business_component_id = ?";
        return sqlQueryExecutor.count(sql, businessComponentId);
    }

    public long countActiveFlows() {
        String sql = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE is_active = true";
        return sqlQueryExecutor.count(sql);
    }


    public boolean existsByNameAndIdNot(String name, UUID excludeId) {
        String sql = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE name = ? AND id != ?";
        return sqlQueryExecutor.count(sql, name, excludeId) > 0;
    }

    public List<IntegrationFlow> findByIsActive(boolean isActive) {
        String sql = buildSelectWithJoins() + " WHERE ifl.is_active = ?";
        return sqlQueryExecutor.queryForList(sql, FLOW_WITH_RELATIONSHIPS_ROW_MAPPER, isActive);
    }

    public long countByTenantId(UUID tenantId) {
        // TODO: Implement tenant filtering when tenant_id column is added
        // For now, return total count
        return count();
    }

    public List<IntegrationFlow> findAllById(java.util.Set<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return new java.util.ArrayList<>();
        }

        String placeholders = ids.stream()
            .map(id -> "?")
            .collect(java.util.stream.Collectors.joining(","));

        String sql = buildSelectWithJoins() + " WHERE ifl.id IN (" + placeholders + ")";
        return sqlQueryExecutor.queryForList(sql, FLOW_WITH_RELATIONSHIPS_ROW_MAPPER, ids.toArray());
    }

    public long countBySourceFlowStructureIdAndIsActiveTrue(UUID sourceFlowStructureId) {
        String sql = "SELECT COUNT(*) FROM " + TABLE_NAME +
                    " WHERE source_flow_structure_id = ? AND is_active = true";
        return sqlQueryExecutor.count(sql, sourceFlowStructureId);
    }

    public long countByTargetFlowStructureIdAndIsActiveTrue(UUID targetFlowStructureId) {
        String sql = "SELECT COUNT(*) FROM " + TABLE_NAME +
                    " WHERE target_flow_structure_id = ? AND is_active = true";
        return sqlQueryExecutor.count(sql, targetFlowStructureId);
    }

    public List<IntegrationFlow> findBySourceAdapterId(UUID adapterId) {
        String sql = buildSelectWithJoins() + " WHERE ifl.source_adapter_id = ?";
        return sqlQueryExecutor.queryForList(sql, FLOW_WITH_RELATIONSHIPS_ROW_MAPPER, adapterId);
    }

    public List<IntegrationFlow> findByTargetAdapterId(UUID adapterId) {
        String sql = buildSelectWithJoins() + " WHERE ifl.target_adapter_id = ?";
        return sqlQueryExecutor.queryForList(sql, FLOW_WITH_RELATIONSHIPS_ROW_MAPPER, adapterId);
    }

    public void flush() {
        // No-op for SQL repository - no buffering to flush
    }

    public List<IntegrationFlow> findByStatusAndIsActiveTrueOrderByName(FlowStatus status) {
        String sql = buildSelectWithJoins() + " WHERE ifl.status = ? AND ifl.is_active = true ORDER BY ifl.name";
        return sqlQueryExecutor.queryForList(sql, FLOW_WITH_RELATIONSHIPS_ROW_MAPPER, status.toString());
    }

    public List<IntegrationFlow> findByDeploymentEndpointContainingAndStatus(String endpoint, FlowStatus status) {
        String sql = buildSelectWithJoins() + " WHERE ifl.deployment_endpoint LIKE ? AND ifl.status = ? ORDER BY ifl.name";
        return sqlQueryExecutor.queryForList(sql, FLOW_WITH_RELATIONSHIPS_ROW_MAPPER, "%" + endpoint + "%", status.toString());
    }
}