package com.integrixs.data.sql.repository;

import com.integrixs.data.model.AdapterType;
import com.integrixs.data.model.AdapterCategory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.integrixs.data.sql.core.BaseSqlRepository;
import com.integrixs.data.sql.core.ResultSetMapper;
import com.integrixs.data.sql.core.SqlQueryExecutor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Map;

/**
 * SQL implementation of AdapterTypeRepository using native queries.
 */
@Repository
public class AdapterTypeSqlRepository extends BaseSqlRepository<AdapterType, UUID> {

    private static final String TABLE_NAME = "adapter_types";
    private static final String ID_COLUMN = "id";

    /**
     * Row mapper for AdapterType entity
     */
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final RowMapper<AdapterType> ADAPTER_TYPE_ROW_MAPPER = new RowMapper<AdapterType>() {
        @Override
        public AdapterType mapRow(ResultSet rs, int rowNum) throws SQLException {
            AdapterType type = new AdapterType();
            type.setId(ResultSetMapper.getUUID(rs, "id"));
            type.setCode(ResultSetMapper.getString(rs, "code"));
            type.setName(ResultSetMapper.getString(rs, "name"));
            type.setVendor(ResultSetMapper.getString(rs, "vendor"));
            type.setVersion(ResultSetMapper.getString(rs, "version"));
            type.setDescription(ResultSetMapper.getString(rs, "description"));
            type.setIcon(ResultSetMapper.getString(rs, "icon"));
            type.setSupportsInbound(rs.getBoolean("supports_inbound"));
            type.setSupportsOutbound(rs.getBoolean("supports_outbound"));
            type.setSupportsBidirectional(rs.getBoolean("supports_bidirectional"));
            type.setDocumentationUrl(ResultSetMapper.getString(rs, "documentation_url"));
            type.setSupportUrl(ResultSetMapper.getString(rs, "support_url"));
            type.setPricingTier(ResultSetMapper.getString(rs, "pricing_tier"));
            type.setStatus(ResultSetMapper.getString(rs, "status"));
            type.setCertified(rs.getBoolean("is_certified"));
            type.setCertificationDate(ResultSetMapper.getLocalDateTime(rs, "certification_date"));
            type.setCreatedAt(ResultSetMapper.getLocalDateTime(rs, "created_at"));
            type.setUpdatedAt(ResultSetMapper.getLocalDateTime(rs, "updated_at"));

            // Handle category relationship
            UUID categoryId = ResultSetMapper.getUUID(rs, "category_id");
            if (categoryId != null) {
                AdapterCategory category = new AdapterCategory();
                category.setId(categoryId);
                type.setCategory(category);
            }

            // Handle JSON fields
            try {
                String inboundSchema = ResultSetMapper.getString(rs, "inbound_config_schema");
                if (inboundSchema != null) {
                    type.setInboundConfigSchema(objectMapper.readValue(inboundSchema, new TypeReference<Map<String, Object>>(){}));
                }

                String outboundSchema = ResultSetMapper.getString(rs, "outbound_config_schema");
                if (outboundSchema != null) {
                    type.setOutboundConfigSchema(objectMapper.readValue(outboundSchema, new TypeReference<Map<String, Object>>(){}));
                }

                String commonSchema = ResultSetMapper.getString(rs, "common_config_schema");
                if (commonSchema != null) {
                    type.setCommonConfigSchema(objectMapper.readValue(commonSchema, new TypeReference<Map<String, Object>>(){}));
                }

                String capabilities = ResultSetMapper.getString(rs, "capabilities");
                if (capabilities != null) {
                    type.setCapabilities(objectMapper.readValue(capabilities, new TypeReference<Map<String, Object>>(){}));
                }

                String protocols = ResultSetMapper.getString(rs, "supported_protocols");
                if (protocols != null) {
                    type.setSupportedProtocols(objectMapper.readValue(protocols, String[].class));
                }

                String formats = ResultSetMapper.getString(rs, "supported_formats");
                if (formats != null) {
                    type.setSupportedFormats(objectMapper.readValue(formats, String[].class));
                }

                String authMethods = ResultSetMapper.getString(rs, "authentication_methods");
                if (authMethods != null) {
                    type.setAuthenticationMethods(objectMapper.readValue(authMethods, String[].class));
                }
            } catch (Exception e) {
                // Log error but continue
            }

            return type;
        }
    };

    public AdapterTypeSqlRepository(SqlQueryExecutor sqlQueryExecutor) {
        super(sqlQueryExecutor, TABLE_NAME, ID_COLUMN, ADAPTER_TYPE_ROW_MAPPER);
    }

    public Optional<AdapterType> findByName(String name) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE name = ?";
        return sqlQueryExecutor.queryForObject(sql, ADAPTER_TYPE_ROW_MAPPER, name);
    }

    public List<AdapterType> findByCategoryId(UUID categoryId) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE category_id = ?";
        return sqlQueryExecutor.queryForList(sql, ADAPTER_TYPE_ROW_MAPPER, categoryId);
    }

    public List<AdapterType> findByStatus(String status) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE status = ? ORDER BY name";
        return sqlQueryExecutor.queryForList(sql, ADAPTER_TYPE_ROW_MAPPER, status);
    }

    public List<AdapterType> findBySupportsInboundTrue() {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE supports_inbound = true AND status = 'active' ORDER BY name";
        return sqlQueryExecutor.queryForList(sql, ADAPTER_TYPE_ROW_MAPPER);
    }

    public List<AdapterType> findBySupportsOutboundTrue() {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE supports_outbound = true AND status = 'active' ORDER BY name";
        return sqlQueryExecutor.queryForList(sql, ADAPTER_TYPE_ROW_MAPPER);
    }

    @Override
    public AdapterType save(AdapterType type) {
        if (type.getId() == null) {
            type.setId(generateId());
        }

        boolean exists = existsById(type.getId());

        if (!exists) {
            return insert(type);
        } else {
            return update(type);
        }
    }

    private AdapterType insert(AdapterType type) {
        String sql = "INSERT INTO " + TABLE_NAME + " (" +
                     "id, code, name, category_id, vendor, version, description, icon, " +
                     "supports_inbound, supports_outbound, supports_bidirectional, " +
                     "inbound_config_schema, outbound_config_schema, common_config_schema, " +
                     "capabilities, supported_protocols, supported_formats, authentication_methods, " +
                     "documentation_url, support_url, pricing_tier, status, is_certified, " +
                     "certification_date, created_at, updated_at" +
                     ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        LocalDateTime now = LocalDateTime.now();
        if (type.getCreatedAt() == null) {
            type.setCreatedAt(now);
        }
        if (type.getUpdatedAt() == null) {
            type.setUpdatedAt(now);
        }

        try {
            sqlQueryExecutor.update(sql,
                type.getId(),
                type.getCode(),
                type.getName(),
                type.getCategory() != null ? type.getCategory().getId() : null,
                type.getVendor(),
                type.getVersion(),
                type.getDescription(),
                type.getIcon(),
                type.isSupportsInbound(),
                type.isSupportsOutbound(),
                type.isSupportsBidirectional(),
                type.getInboundConfigSchema() != null ? objectMapper.writeValueAsString(type.getInboundConfigSchema()) : null,
                type.getOutboundConfigSchema() != null ? objectMapper.writeValueAsString(type.getOutboundConfigSchema()) : null,
                type.getCommonConfigSchema() != null ? objectMapper.writeValueAsString(type.getCommonConfigSchema()) : null,
                type.getCapabilities() != null ? objectMapper.writeValueAsString(type.getCapabilities()) : null,
                type.getSupportedProtocols() != null ? objectMapper.writeValueAsString(type.getSupportedProtocols()) : null,
                type.getSupportedFormats() != null ? objectMapper.writeValueAsString(type.getSupportedFormats()) : null,
                type.getAuthenticationMethods() != null ? objectMapper.writeValueAsString(type.getAuthenticationMethods()) : null,
                type.getDocumentationUrl(),
                type.getSupportUrl(),
                type.getPricingTier(),
                type.getStatus(),
                type.isCertified(),
                ResultSetMapper.toTimestamp(type.getCertificationDate()),
                ResultSetMapper.toTimestamp(type.getCreatedAt()),
                ResultSetMapper.toTimestamp(type.getUpdatedAt())
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to insert adapter type", e);
        }

        return type;
    }

    @Override
    public AdapterType update(AdapterType type) {
        String sql = "UPDATE " + TABLE_NAME + " SET " +
                     "code = ?, name = ?, category_id = ?, vendor = ?, version = ?, " +
                     "description = ?, icon = ?, supports_inbound = ?, supports_outbound = ?, " +
                     "supports_bidirectional = ?, inbound_config_schema = ?, outbound_config_schema = ?, " +
                     "common_config_schema = ?, capabilities = ?, supported_protocols = ?, " +
                     "supported_formats = ?, authentication_methods = ?, documentation_url = ?, " +
                     "support_url = ?, pricing_tier = ?, status = ?, is_certified = ?, " +
                     "certification_date = ?, updated_at = ? WHERE id = ?";

        type.setUpdatedAt(LocalDateTime.now());

        try {
            sqlQueryExecutor.update(sql,
                type.getCode(),
                type.getName(),
                type.getCategory() != null ? type.getCategory().getId() : null,
                type.getVendor(),
                type.getVersion(),
                type.getDescription(),
                type.getIcon(),
                type.isSupportsInbound(),
                type.isSupportsOutbound(),
                type.isSupportsBidirectional(),
                type.getInboundConfigSchema() != null ? objectMapper.writeValueAsString(type.getInboundConfigSchema()) : null,
                type.getOutboundConfigSchema() != null ? objectMapper.writeValueAsString(type.getOutboundConfigSchema()) : null,
                type.getCommonConfigSchema() != null ? objectMapper.writeValueAsString(type.getCommonConfigSchema()) : null,
                type.getCapabilities() != null ? objectMapper.writeValueAsString(type.getCapabilities()) : null,
                type.getSupportedProtocols() != null ? objectMapper.writeValueAsString(type.getSupportedProtocols()) : null,
                type.getSupportedFormats() != null ? objectMapper.writeValueAsString(type.getSupportedFormats()) : null,
                type.getAuthenticationMethods() != null ? objectMapper.writeValueAsString(type.getAuthenticationMethods()) : null,
                type.getDocumentationUrl(),
                type.getSupportUrl(),
                type.getPricingTier(),
                type.getStatus(),
                type.isCertified(),
                ResultSetMapper.toTimestamp(type.getCertificationDate()),
                ResultSetMapper.toTimestamp(type.getUpdatedAt()),
                type.getId()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to update adapter type", e);
        }

        return type;
    }

    public Optional<AdapterType> findByCode(String code) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE code = ?";
        List<AdapterType> results = sqlQueryExecutor.queryForList(sql, ADAPTER_TYPE_ROW_MAPPER, code);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public boolean existsByCode(String code) {
        String sql = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE code = ?";
        Long count = sqlQueryExecutor.count(sql, code);
        return count > 0;
    }

    public org.springframework.data.domain.Page<AdapterType> findWithFilters(UUID categoryId, String search, String status, org.springframework.data.domain.Pageable pageable) {
        StringBuilder sql = new StringBuilder("SELECT * FROM " + TABLE_NAME + " WHERE 1=1");
        java.util.List<Object> params = new java.util.ArrayList<>();

        if (categoryId != null) {
            sql.append(" AND category_id = ?");
            params.add(categoryId);
        }

        if (search != null && !search.isEmpty()) {
            sql.append(" AND (LOWER(name) LIKE ? OR LOWER(code) LIKE ? OR LOWER(description) LIKE ?)");
            String searchPattern = "%" + search.toLowerCase() + "%";
            params.add(searchPattern);
            params.add(searchPattern);
            params.add(searchPattern);
        }

        if (status != null && !status.isEmpty()) {
            sql.append(" AND status = ?");
            params.add(status);
        }

        // Count query
        String countSql = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE 1=1" +
                         sql.toString().substring(sql.toString().indexOf(" WHERE 1=1") + 10);
        long total = sqlQueryExecutor.count(countSql, params.toArray());

        // Add pagination
        sql.append(" ORDER BY name");
        sql.append(" LIMIT ").append(pageable.getPageSize());
        sql.append(" OFFSET ").append(pageable.getOffset());

        List<AdapterType> content = sqlQueryExecutor.queryForList(sql.toString(), ADAPTER_TYPE_ROW_MAPPER, params.toArray());

        return new org.springframework.data.domain.PageImpl<>(content, pageable, total);
    }
}