package com.integrixs.data.sql.repository;

import com.integrixs.adapters.domain.model.AdapterConfiguration;
import com.integrixs.data.model.*;
import com.integrixs.data.sql.core.BaseSqlRepository;
import com.integrixs.data.sql.core.ResultSetMapper;
import com.integrixs.data.sql.core.SqlQueryExecutor;
import com.integrixs.data.sql.core.SqlPaginationHelper;
import com.integrixs.shared.enums.AdapterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * SQL implementation of CommunicationAdapterRepository using native queries.
 */
@Repository("communicationAdapterSqlRepository")
public class CommunicationAdapterSqlRepository extends BaseSqlRepository<CommunicationAdapter, UUID> {

    private static final String TABLE_NAME = "communication_adapters";
    private static final String ID_COLUMN = "id";

    /**
     * Row mapper for CommunicationAdapter entity (without relationships)
     */
    private static final RowMapper<CommunicationAdapter> ADAPTER_ROW_MAPPER = new RowMapper<CommunicationAdapter>() {
        @Override
        public CommunicationAdapter mapRow(ResultSet rs, int rowNum) throws SQLException {
            CommunicationAdapter adapter = new CommunicationAdapter();
            adapter.setId(ResultSetMapper.getUUID(rs, "id"));
            adapter.setName(ResultSetMapper.getString(rs, "name"));

            String typeStr = ResultSetMapper.getString(rs, "type");
            if (typeStr != null) {
                adapter.setType(AdapterType.valueOf(typeStr));
            }

            String modeStr = ResultSetMapper.getString(rs, "mode");
            if (modeStr != null) {
                adapter.setMode(AdapterConfiguration.AdapterModeEnum.valueOf(modeStr));
            }

            adapter.setDirection(ResultSetMapper.getString(rs, "direction"));
            adapter.setConfiguration(ResultSetMapper.getString(rs, "configuration"));
            adapter.setActive(rs.getBoolean("is_active"));
            adapter.setDescription(ResultSetMapper.getString(rs, "description"));
            adapter.setHealthy(rs.getBoolean("is_healthy"));
            adapter.setLastHealthCheck(ResultSetMapper.getLocalDateTime(rs, "last_health_check"));
            adapter.setTenantId(ResultSetMapper.getUUID(rs, "tenant_id"));
            adapter.setCreatedAt(ResultSetMapper.getLocalDateTime(rs, "created_at"));
            adapter.setUpdatedAt(ResultSetMapper.getLocalDateTime(rs, "updated_at"));

            return adapter;
        }
    };

    /**
     * Row mapper for CommunicationAdapter with all relationships
     */
    private static final RowMapper<CommunicationAdapter> ADAPTER_WITH_RELATIONSHIPS_ROW_MAPPER = new RowMapper<CommunicationAdapter>() {
        @Override
        public CommunicationAdapter mapRow(ResultSet rs, int rowNum) throws SQLException {
            CommunicationAdapter adapter = ADAPTER_ROW_MAPPER.mapRow(rs, rowNum);

            // Map business component
            UUID businessComponentId = ResultSetMapper.getUUID(rs, "business_component_id");
            if (businessComponentId != null) {
                BusinessComponent bc = new BusinessComponent();
                bc.setId(businessComponentId);
                bc.setName(ResultSetMapper.getString(rs, "bc_name"));
                bc.setDescription(ResultSetMapper.getString(rs, "bc_description"));
                adapter.setBusinessComponent(bc);
            }

            // Map created by user
            UUID createdById = ResultSetMapper.getUUID(rs, "created_by");
            if (createdById != null) {
                User createdBy = new User();
                createdBy.setId(createdById);
                createdBy.setUsername(ResultSetMapper.getString(rs, "created_by_username"));
                createdBy.setEmail(ResultSetMapper.getString(rs, "created_by_email"));
                adapter.setCreatedBy(createdBy);
            }

            // Map updated by user
            UUID updatedById = ResultSetMapper.getUUID(rs, "updated_by");
            if (updatedById != null) {
                User updatedBy = new User();
                updatedBy.setId(updatedById);
                updatedBy.setUsername(ResultSetMapper.getString(rs, "updated_by_username"));
                updatedBy.setEmail(ResultSetMapper.getString(rs, "updated_by_email"));
                adapter.setUpdatedBy(updatedBy);
            }

            // Map external authentication
            UUID externalAuthId = ResultSetMapper.getUUID(rs, "external_auth_id");
            if (externalAuthId != null) {
                ExternalAuthentication auth = new ExternalAuthentication();
                auth.setId(externalAuthId);
                auth.setName(ResultSetMapper.getString(rs, "auth_name"));
                String authTypeStr = ResultSetMapper.getString(rs, "auth_type");
                if (authTypeStr != null) {
                    auth.setAuthType(ExternalAuthentication.AuthType.valueOf(authTypeStr));
                }
                adapter.setExternalAuthentication(auth);
            }

            return adapter;
        }
    };

    public CommunicationAdapterSqlRepository(SqlQueryExecutor sqlQueryExecutor) {
        super(sqlQueryExecutor, TABLE_NAME, ID_COLUMN, ADAPTER_ROW_MAPPER);
    }

    @Override
    public Optional<CommunicationAdapter> findById(UUID id) {
        String sql = buildSelectWithJoins() + " WHERE ca.id = ?";

        List<CommunicationAdapter> results = sqlQueryExecutor.queryForList(sql, ADAPTER_WITH_RELATIONSHIPS_ROW_MAPPER, id);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public List<CommunicationAdapter> findAll() {
        String sql = buildSelectWithJoins() + " ORDER BY ca.name";
        return sqlQueryExecutor.queryForList(sql, ADAPTER_WITH_RELATIONSHIPS_ROW_MAPPER);
    }

    public Page<CommunicationAdapter> findAll(Pageable pageable) {
        String baseQuery = buildSelectWithJoins();
        String countQuery = "SELECT COUNT(*) FROM " + TABLE_NAME;

        long total = sqlQueryExecutor.count(countQuery);
        String paginatedQuery = baseQuery + SqlPaginationHelper.buildOrderByClause(pageable.getSort()) + SqlPaginationHelper.buildPaginationClause(pageable);
        List<CommunicationAdapter> adapters = sqlQueryExecutor.queryForList(paginatedQuery, ADAPTER_WITH_RELATIONSHIPS_ROW_MAPPER);

        return new PageImpl<>(adapters, pageable, total);
    }

    public Optional<CommunicationAdapter> findByName(String name) {
        String sql = buildSelectWithJoins() + " WHERE ca.name = ?";

        List<CommunicationAdapter> results = sqlQueryExecutor.queryForList(sql, ADAPTER_WITH_RELATIONSHIPS_ROW_MAPPER, name);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public List<CommunicationAdapter> findByBusinessComponentId(UUID businessComponentId) {
        String sql = buildSelectWithJoins() + " WHERE ca.business_component_id = ? ORDER BY ca.name";
        return sqlQueryExecutor.queryForList(sql, ADAPTER_WITH_RELATIONSHIPS_ROW_MAPPER, businessComponentId);
    }

    public List<CommunicationAdapter> findByType(AdapterType type) {
        String sql = buildSelectWithJoins() + " WHERE ca.type = ? ORDER BY ca.name";
        return sqlQueryExecutor.queryForList(sql, ADAPTER_WITH_RELATIONSHIPS_ROW_MAPPER, type.toString());
    }

    public List<CommunicationAdapter> findByIsActiveTrue() {
        String sql = buildSelectWithJoins() + " WHERE ca.is_active = true ORDER BY ca.name";
        return sqlQueryExecutor.queryForList(sql, ADAPTER_WITH_RELATIONSHIPS_ROW_MAPPER);
    }

    public List<CommunicationAdapter> findByTypeAndIsActiveTrue(AdapterType type) {
        String sql = buildSelectWithJoins() + " WHERE ca.type = ? AND ca.is_active = true ORDER BY ca.name";
        return sqlQueryExecutor.queryForList(sql, ADAPTER_WITH_RELATIONSHIPS_ROW_MAPPER, type.toString());
    }

    public List<CommunicationAdapter> findByBusinessComponentIdAndIsActiveTrue(UUID businessComponentId) {
        String sql = buildSelectWithJoins() +
                     " WHERE ca.business_component_id = ? AND ca.is_active = true ORDER BY ca.name";
        return sqlQueryExecutor.queryForList(sql, ADAPTER_WITH_RELATIONSHIPS_ROW_MAPPER, businessComponentId);
    }

    public List<CommunicationAdapter> findByTenantId(UUID tenantId) {
        String sql = buildSelectWithJoins() + " WHERE ca.tenant_id = ? ORDER BY ca.name";
        return sqlQueryExecutor.queryForList(sql, ADAPTER_WITH_RELATIONSHIPS_ROW_MAPPER, tenantId);
    }

    public List<CommunicationAdapter> findUnhealthyAdapters() {
        String sql = buildSelectWithJoins() +
                     " WHERE ca.is_healthy = false AND ca.is_active = true ORDER BY ca.last_health_check";
        return sqlQueryExecutor.queryForList(sql, ADAPTER_WITH_RELATIONSHIPS_ROW_MAPPER);
    }

    @Override
    public CommunicationAdapter save(CommunicationAdapter adapter) {
        if (adapter.getId() == null) {
            adapter.setId(generateId());
        }

        boolean exists = existsById(adapter.getId());

        if (!exists) {
            return insert(adapter);
        } else {
            return update(adapter);
        }
    }

    private CommunicationAdapter insert(CommunicationAdapter adapter) {
        String sql = "INSERT INTO " + TABLE_NAME + " (" +
                     "id, name, type, mode, direction, configuration, is_active, description, " +
                     "business_component_id, created_at, updated_at, created_by, updated_by, " +
                     "external_auth_id, is_healthy, last_health_check, tenant_id" +
                     ") VALUES (?, ?, ?, ?, ?, ?::json, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        LocalDateTime now = LocalDateTime.now();
        adapter.setCreatedAt(now);
        adapter.setUpdatedAt(now);

        sqlQueryExecutor.update(sql,
            adapter.getId(),
            adapter.getName(),
            adapter.getType() != null ? adapter.getType().toString() : null,
            adapter.getMode() != null ? adapter.getMode().toString() : null,
            adapter.getDirection(),
            adapter.getConfiguration(),
            adapter.isActive(),
            adapter.getDescription(),
            adapter.getBusinessComponent() != null ? adapter.getBusinessComponent().getId() : null,
            ResultSetMapper.toTimestamp(adapter.getCreatedAt()),
            ResultSetMapper.toTimestamp(adapter.getUpdatedAt()),
            adapter.getCreatedBy() != null ? adapter.getCreatedBy().getId() : null,
            adapter.getUpdatedBy() != null ? adapter.getUpdatedBy().getId() : null,
            adapter.getExternalAuthentication() != null ? adapter.getExternalAuthentication().getId() : null,
            adapter.isHealthy(),
            ResultSetMapper.toTimestamp(adapter.getLastHealthCheck()),
            adapter.getTenantId()
        );

        return adapter;
    }

    @Override
    public CommunicationAdapter update(CommunicationAdapter adapter) {
        String sql = "UPDATE " + TABLE_NAME + " SET " +
                     "name = ?, type = ?, mode = ?, direction = ?, configuration = ?::json, " +
                     "is_active = ?, description = ?, business_component_id = ?, updated_at = ?, " +
                     "updated_by = ?, external_auth_id = ?, is_healthy = ?, last_health_check = ?, " +
                     "tenant_id = ? WHERE id = ?";

        adapter.setUpdatedAt(LocalDateTime.now());

        sqlQueryExecutor.update(sql,
            adapter.getName(),
            adapter.getType() != null ? adapter.getType().toString() : null,
            adapter.getMode() != null ? adapter.getMode().toString() : null,
            adapter.getDirection(),
            adapter.getConfiguration(),
            adapter.isActive(),
            adapter.getDescription(),
            adapter.getBusinessComponent() != null ? adapter.getBusinessComponent().getId() : null,
            ResultSetMapper.toTimestamp(adapter.getUpdatedAt()),
            adapter.getUpdatedBy() != null ? adapter.getUpdatedBy().getId() : null,
            adapter.getExternalAuthentication() != null ? adapter.getExternalAuthentication().getId() : null,
            adapter.isHealthy(),
            ResultSetMapper.toTimestamp(adapter.getLastHealthCheck()),
            adapter.getTenantId(),
            adapter.getId()
        );

        return adapter;
    }

    /**
     * Build SELECT query with all JOINs
     */
    private String buildSelectWithJoins() {
        return "SELECT ca.*, " +
               "bc.name as bc_name, bc.description as bc_description, " +
               "cu.username as created_by_username, cu.email as created_by_email, " +
               "uu.username as updated_by_username, uu.email as updated_by_email, " +
               "ea.name as auth_name, ea.auth_type as auth_type " +
               "FROM " + TABLE_NAME + " ca " +
               "LEFT JOIN business_components bc ON ca.business_component_id = bc.id " +
               "LEFT JOIN users cu ON ca.created_by = cu.id " +
               "LEFT JOIN users uu ON ca.updated_by = uu.id " +
               "LEFT JOIN external_authentications ea ON ca.external_auth_id = ea.id";
    }

    public boolean existsByName(String name) {
        String sql = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE name = ?";
        long count = sqlQueryExecutor.count(sql, name);
        return count > 0;
    }

    public int updateHealthStatus(UUID adapterId, boolean healthy, LocalDateTime checkTime) {
        String sql = "UPDATE " + TABLE_NAME + " SET is_healthy = ?, last_health_check = ? WHERE id = ?";
        return sqlQueryExecutor.update(sql, healthy, ResultSetMapper.toTimestamp(checkTime), adapterId);
    }

    public List<CommunicationAdapter> findByModeAndIsActiveTrue(AdapterConfiguration.AdapterModeEnum mode) {
        String sql = buildSelectWithJoins() + " WHERE ca.mode = ? AND ca.is_active = true ORDER BY ca.name";
        return sqlQueryExecutor.queryForList(sql, ADAPTER_WITH_RELATIONSHIPS_ROW_MAPPER, mode.toString());
    }

    public long countByBusinessComponentId(UUID businessComponentId) {
        String sql = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE business_component_id = ?";
        return sqlQueryExecutor.count(sql, businessComponentId);
    }

    // Legacy-compatible method names
    public List<CommunicationAdapter> findByBusinessComponent_Id(UUID businessComponentId) {
        return findByBusinessComponentId(businessComponentId);
    }

    public long countByBusinessComponent_Id(UUID businessComponentId) {
        return countByBusinessComponentId(businessComponentId);
    }

    public long countByTenantId(UUID tenantId) {
        String sql = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE tenant_id = ?";
        return sqlQueryExecutor.count(sql, tenantId);
    }

    public List<CommunicationAdapter> findAllById(java.util.Set<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return new java.util.ArrayList<>();
        }

        String placeholders = ids.stream()
            .map(id -> "?")
            .collect(java.util.stream.Collectors.joining(","));

        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE id IN (" + placeholders + ")";
        return sqlQueryExecutor.queryForList(sql, ADAPTER_ROW_MAPPER, ids.toArray());
    }
}