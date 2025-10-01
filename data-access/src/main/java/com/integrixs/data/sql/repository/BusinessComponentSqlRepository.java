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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * SQL implementation of BusinessComponentRepository using native queries.
 */
@Repository("businessComponentSqlRepository")
public class BusinessComponentSqlRepository extends BaseSqlRepository<BusinessComponent, UUID> {

    private static final String TABLE_NAME = "business_components";
    private static final String ID_COLUMN = "id";

    /**
     * Row mapper for BusinessComponent entity
     */
    private static final RowMapper<BusinessComponent> BUSINESS_COMPONENT_ROW_MAPPER = new RowMapper<BusinessComponent>() {
        @Override
        public BusinessComponent mapRow(ResultSet rs, int rowNum) throws SQLException {
            BusinessComponent bc = new BusinessComponent();
            bc.setId(ResultSetMapper.getUUID(rs, "id"));
            bc.setName(ResultSetMapper.getString(rs, "name"));
            bc.setDescription(ResultSetMapper.getString(rs, "description"));
            bc.setContactEmail(ResultSetMapper.getString(rs, "contact_email"));
            bc.setContactPhone(ResultSetMapper.getString(rs, "contact_phone"));
            bc.setCreatedAt(ResultSetMapper.getLocalDateTime(rs, "created_at"));
            bc.setUpdatedAt(ResultSetMapper.getLocalDateTime(rs, "updated_at"));
            bc.setStatus(ResultSetMapper.getString(rs, "status"));
            bc.setDepartment(ResultSetMapper.getString(rs, "department"));
            return bc;
        }
    };

    public BusinessComponentSqlRepository(SqlQueryExecutor sqlQueryExecutor) {
        super(sqlQueryExecutor, TABLE_NAME, ID_COLUMN, BUSINESS_COMPONENT_ROW_MAPPER);
    }

    @Override
    public Optional<BusinessComponent> findById(UUID id) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE id = ?";

        List<BusinessComponent> results = sqlQueryExecutor.queryForList(sql, rowMapper, id);
        if (results.isEmpty()) {
            return Optional.empty();
        }

        BusinessComponent bc = results.get(0);
        // Use separate methods to load related entities when needed
        return Optional.of(bc);
    }

    @Override
    public List<BusinessComponent> findAll() {
        String sql = "SELECT * FROM " + TABLE_NAME + " ORDER BY name";
        return sqlQueryExecutor.queryForList(sql, rowMapper);
    }

    public Page<BusinessComponent> findAll(Pageable pageable) {
        String baseQuery = "SELECT * FROM " + TABLE_NAME;
        String countQuery = "SELECT COUNT(*) FROM " + TABLE_NAME;

        long total = sqlQueryExecutor.count(countQuery);
        String paginatedQuery = baseQuery + SqlPaginationHelper.buildOrderByClause(pageable.getSort()) + SqlPaginationHelper.buildPaginationClause(pageable);
        List<BusinessComponent> components = sqlQueryExecutor.queryForList(paginatedQuery, rowMapper);

        return new PageImpl<>(components, pageable, total);
    }

    public Optional<BusinessComponent> findByName(String name) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE name = ?";

        List<BusinessComponent> results = sqlQueryExecutor.queryForList(sql, rowMapper, name);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public List<BusinessComponent> findByStatus(String status) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE status = ? ORDER BY name";
        return sqlQueryExecutor.queryForList(sql, rowMapper, status);
    }

    public List<BusinessComponent> findByDepartment(String department) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE department = ? ORDER BY name";
        return sqlQueryExecutor.queryForList(sql, rowMapper, department);
    }

    @Override
    public BusinessComponent save(BusinessComponent bc) {
        if (bc.getId() == null) {
            bc.setId(generateId());
        }

        boolean exists = existsById(bc.getId());

        if (!exists) {
            return insert(bc);
        } else {
            return update(bc);
        }
    }

    private BusinessComponent insert(BusinessComponent bc) {
        String sql = "INSERT INTO " + TABLE_NAME + " (" +
                     "id, name, description, contact_email, contact_phone, " +
                     "created_at, updated_at, status, department" +
                     ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        LocalDateTime now = LocalDateTime.now();
        if (bc.getCreatedAt() == null) {
            bc.setCreatedAt(now);
        }
        if (bc.getUpdatedAt() == null) {
            bc.setUpdatedAt(now);
        }
        if (bc.getStatus() == null) {
            bc.setStatus("ACTIVE");
        }

        sqlQueryExecutor.update(sql,
            bc.getId(),
            bc.getName(),
            bc.getDescription(),
            bc.getContactEmail(),
            bc.getContactPhone(),
            ResultSetMapper.toTimestamp(bc.getCreatedAt()),
            ResultSetMapper.toTimestamp(bc.getUpdatedAt()),
            bc.getStatus(),
            bc.getDepartment()
        );

        return bc;
    }

    @Override
    public BusinessComponent update(BusinessComponent bc) {
        String sql = "UPDATE " + TABLE_NAME + " SET " +
                     "name = ?, description = ?, contact_email = ?, contact_phone = ?, " +
                     "updated_at = ?, status = ?, department = ? " +
                     "WHERE id = ?";

        bc.setUpdatedAt(LocalDateTime.now());

        sqlQueryExecutor.update(sql,
            bc.getName(),
            bc.getDescription(),
            bc.getContactEmail(),
            bc.getContactPhone(),
            ResultSetMapper.toTimestamp(bc.getUpdatedAt()),
            bc.getStatus(),
            bc.getDepartment(),
            bc.getId()
        );

        return bc;
    }

    /**
     * Check if a business component exists by name
     */
    public boolean existsByName(String name) {
        String sql = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE name = ?";
        long count = sqlQueryExecutor.count(sql, name);
        return count > 0;
    }

    /**
     * Check if a business component exists by name excluding a specific ID
     */
    public boolean existsByNameAndIdNot(String name, UUID id) {
        String sql = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE name = ? AND id != ?";
        long count = sqlQueryExecutor.count(sql, name, id);
        return count > 0;
    }

    /**
     * Update only the status of a business component
     */
    public int updateStatus(UUID id, String status) {
        String sql = "UPDATE " + TABLE_NAME + " SET status = ?, updated_at = ? WHERE id = ?";
        return sqlQueryExecutor.update(sql, status, ResultSetMapper.toTimestamp(LocalDateTime.now()), id);
    }

    /**
     * Count active business components
     */
    public long countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE status = ?";
        return sqlQueryExecutor.count(sql, status);
    }

    /**
     * Load communication adapters for a business component
     */
    public List<CommunicationAdapter> loadCommunicationAdapters(UUID businessComponentId) {
        String sql = "SELECT * FROM communication_adapters WHERE business_component_id = ? ORDER BY name";

        return sqlQueryExecutor.queryForList(sql, new RowMapper<CommunicationAdapter>() {
            @Override
            public CommunicationAdapter mapRow(ResultSet rs, int rowNum) throws SQLException {
                CommunicationAdapter adapter = new CommunicationAdapter();
                adapter.setId(ResultSetMapper.getUUID(rs, "id"));
                adapter.setName(ResultSetMapper.getString(rs, "name"));
                adapter.setDescription(ResultSetMapper.getString(rs, "description"));
                // Map other fields as needed
                return adapter;
            }
        }, businessComponentId);
    }

    /**
     * Load integration flows for a business component
     */
    public List<IntegrationFlow> loadIntegrationFlows(UUID businessComponentId) {
        String sql = "SELECT * FROM integration_flows WHERE business_component_id = ? ORDER BY name";

        return sqlQueryExecutor.queryForList(sql, new RowMapper<IntegrationFlow>() {
            @Override
            public IntegrationFlow mapRow(ResultSet rs, int rowNum) throws SQLException {
                IntegrationFlow flow = new IntegrationFlow();
                flow.setId(ResultSetMapper.getUUID(rs, "id"));
                flow.setName(ResultSetMapper.getString(rs, "name"));
                flow.setDescription(ResultSetMapper.getString(rs, "description"));
                // Map other fields as needed
                return flow;
            }
        }, businessComponentId);
    }

    /**
     * Count communication adapters for a business component
     */
    public long countCommunicationAdapters(UUID businessComponentId) {
        String sql = "SELECT COUNT(*) FROM communication_adapters WHERE business_component_id = ?";
        return sqlQueryExecutor.count(sql, businessComponentId);
    }

    /**
     * Count integration flows for a business component
     */
    public long countIntegrationFlows(UUID businessComponentId) {
        String sql = "SELECT COUNT(*) FROM integration_flows WHERE business_component_id = ?";
        return sqlQueryExecutor.count(sql, businessComponentId);
    }

    /**
     * Find business components with adapters
     */
    public List<BusinessComponent> findComponentsWithAdapters() {
        String sql = "SELECT DISTINCT bc.* FROM " + TABLE_NAME + " bc " +
                     "INNER JOIN communication_adapters ca ON bc.id = ca.business_component_id " +
                     "ORDER BY bc.name";
        return sqlQueryExecutor.queryForList(sql, rowMapper);
    }

    /**
     * Find business components with flows
     */
    public List<BusinessComponent> findComponentsWithFlows() {
        String sql = "SELECT DISTINCT bc.* FROM " + TABLE_NAME + " bc " +
                     "INNER JOIN integration_flows if ON bc.id = if.business_component_id " +
                     "ORDER BY bc.name";
        return sqlQueryExecutor.queryForList(sql, rowMapper);
    }

    /**
     * Delete a business component only if it has no related entities
     */
    @Override
    public void deleteById(UUID id) {
        // Check for related entities
        long adapterCount = countCommunicationAdapters(id);
        long flowCount = countIntegrationFlows(id);

        if (adapterCount > 0 || flowCount > 0) {
            throw new IllegalStateException("Cannot delete business component with existing adapters or flows");
        }

        super.deleteById(id);
    }

    /**
     * Find all business components with their entity counts
     */
    public List<BusinessComponentWithCounts> findAllWithCounts() {
        String sql = "SELECT bc.*, " +
                     "(SELECT COUNT(*) FROM communication_adapters WHERE business_component_id = bc.id) as adapter_count, " +
                     "(SELECT COUNT(*) FROM integration_flows WHERE business_component_id = bc.id) as flow_count " +
                     "FROM " + TABLE_NAME + " bc " +
                     "ORDER BY bc.name";

        return sqlQueryExecutor.queryForList(sql, new RowMapper<BusinessComponentWithCounts>() {
            @Override
            public BusinessComponentWithCounts mapRow(ResultSet rs, int rowNum) throws SQLException {
                BusinessComponent bc = BUSINESS_COMPONENT_ROW_MAPPER.mapRow(rs, rowNum);
                long adapterCount = rs.getLong("adapter_count");
                long flowCount = rs.getLong("flow_count");
                return new BusinessComponentWithCounts(bc, adapterCount, flowCount);
            }
        });
    }

    /**
     * DTO for business component with counts
     */
    public static class BusinessComponentWithCounts {
        private final BusinessComponent component;
        private final long adapterCount;
        private final long flowCount;

        public BusinessComponentWithCounts(BusinessComponent component, long adapterCount, long flowCount) {
            this.component = component;
            this.adapterCount = adapterCount;
            this.flowCount = flowCount;
        }

        public BusinessComponent getComponent() {
            return component;
        }

        public long getAdapterCount() {
            return adapterCount;
        }

        public long getFlowCount() {
            return flowCount;
        }
    }

    /**
     * Search business components by name or email
     */
    public List<BusinessComponent> search(String searchTerm) {
        String sql = "SELECT * FROM " + TABLE_NAME +
                     " WHERE LOWER(name) LIKE LOWER(?) OR LOWER(contact_email) LIKE LOWER(?) " +
                     "ORDER BY name";
        String searchPattern = "%" + searchTerm + "%";
        return sqlQueryExecutor.queryForList(sql, rowMapper, searchPattern, searchPattern);
    }
}