package com.integrixs.data.sql.repository;

import com.integrixs.data.model.ExternalAuthentication;
import com.integrixs.data.sql.mapper.ExternalAuthenticationRowMapper;
import com.integrixs.data.sql.core.ResultSetMapper;
import com.integrixs.data.sql.core.SqlQueryExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * SQL repository implementation for ExternalAuthentication entity.
 * Provides direct SQL operations using native SQL.
 */
@Repository
public class ExternalAuthenticationSqlRepository {

    private static final String TABLE_NAME = "external_authentications";

    private final SqlQueryExecutor sqlQueryExecutor;
    private final ExternalAuthenticationRowMapper rowMapper = new ExternalAuthenticationRowMapper();

    public ExternalAuthenticationSqlRepository(SqlQueryExecutor sqlQueryExecutor) {
        this.sqlQueryExecutor = sqlQueryExecutor;
    }

    public ExternalAuthentication save(ExternalAuthentication entity) {
        if (entity.getId() == null) {
            entity.setId(UUID.randomUUID());
            String sql = "INSERT INTO " + TABLE_NAME + " (" +
                        "id, name, description, auth_type, username, password, " +
                        "api_key, token, client_id, client_secret, auth_url, " +
                        "token_url, scope, grant_type, additional_params, " +
                        "business_component_id, is_active, created_at, updated_at, created_by, updated_by" +
                        ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, ?, ?)";
            sqlQueryExecutor.update(sql,
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getAuthType() != null ? entity.getAuthType().toString() : null,
                entity.getUsername(),
                entity.getEncryptedPassword(),
                entity.getEncryptedApiKey(),
                entity.getEncryptedAccessToken(),
                entity.getClientId(),
                entity.getEncryptedClientSecret(),
                entity.getAuthorizationEndpoint(),
                entity.getTokenEndpoint(),
                entity.getScopes(),
                entity.getGrantType(),
                entity.getCustomHeaders() != null ? entity.getCustomHeaders().toString() : null,
                entity.getBusinessComponent() != null ? entity.getBusinessComponent().getId() : null,
                entity.isActive(),
                entity.getCreatedBy() != null ? entity.getCreatedBy().getUsername() : null,
                null // updatedBy not in model
            );
        } else {
            update(entity);
        }
        return entity;
    }

    public Optional<ExternalAuthentication> findById(UUID id) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE id = ?";
        return sqlQueryExecutor.queryForObject(sql, rowMapper, id);
    }

    public List<ExternalAuthentication> findAll() {
        String sql = "SELECT * FROM " + TABLE_NAME + " ORDER BY created_at DESC";
        return sqlQueryExecutor.queryForList(sql, rowMapper);
    }

    public Page<ExternalAuthentication> findAll(Pageable pageable) {
        String countSql = "SELECT COUNT(*) FROM " + TABLE_NAME;
        long total = sqlQueryExecutor.count(countSql);

        String dataSql = "SELECT * FROM " + TABLE_NAME + " ORDER BY created_at DESC LIMIT ? OFFSET ?";
        List<ExternalAuthentication> data = sqlQueryExecutor.queryForList(dataSql, rowMapper,
                                                            pageable.getPageSize(),
                                                            pageable.getOffset());

        return new PageImpl<>(data, pageable, total);
    }

    public ExternalAuthentication update(ExternalAuthentication entity) {
        String sql = "UPDATE " + TABLE_NAME + " SET " +
                    "name = ?, description = ?, auth_type = ?, username = ?, password = ?, " +
                    "api_key = ?, token = ?, client_id = ?, client_secret = ?, auth_url = ?, " +
                    "token_url = ?, scope = ?, grant_type = ?, additional_params = ?, " +
                    "is_active = ?, updated_at = CURRENT_TIMESTAMP, updated_by = ? " +
                    "WHERE id = ?";
        sqlQueryExecutor.update(sql,
            entity.getName(),
            entity.getDescription(),
            entity.getAuthType() != null ? entity.getAuthType().toString() : null,
            entity.getUsername(),
            entity.getEncryptedPassword(),
            entity.getEncryptedApiKey(),
            entity.getEncryptedAccessToken(),
            entity.getClientId(),
            entity.getEncryptedClientSecret(),
            entity.getAuthorizationEndpoint(),
            entity.getTokenEndpoint(),
            entity.getScopes(),
            entity.getGrantType(),
            entity.getCustomHeaders() != null ? entity.getCustomHeaders().toString() : null,
            entity.isActive(),
            null, // updatedBy not in model
            entity.getId()
        );
        return entity;
    }

    public void deleteById(UUID id) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE id = ?";
        sqlQueryExecutor.update(sql, id);
    }

    public long count() {
        String sql = "SELECT COUNT(*) FROM " + TABLE_NAME;
        return sqlQueryExecutor.count(sql);
    }

    public boolean existsByNameAndBusinessComponent(String name, Object businessComponent) {
        String sql = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE name = ? AND business_component_id = ?";
        UUID businessComponentId = null;
        if (businessComponent instanceof com.integrixs.data.model.BusinessComponent) {
            businessComponentId = ((com.integrixs.data.model.BusinessComponent) businessComponent).getId();
        }
        long count = sqlQueryExecutor.count(sql, name, businessComponentId);
        return count > 0;
    }

    public List<ExternalAuthentication> findByBusinessComponentId(UUID businessComponentId) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE business_component_id = ? ORDER BY name";
        return sqlQueryExecutor.queryForList(sql, rowMapper, businessComponentId);
    }

    public Optional<ExternalAuthentication> findByIdAndBusinessComponentId(UUID id, UUID businessComponentId) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE id = ? AND business_component_id = ?";
        return sqlQueryExecutor.queryForObject(sql, rowMapper, id, businessComponentId);
    }

    public com.integrixs.data.model.BusinessComponent getReferenceById(UUID id) {
        com.integrixs.data.model.BusinessComponent bc = new com.integrixs.data.model.BusinessComponent();
        bc.setId(id);
        return bc;
    }

    public List<ExternalAuthentication> findOAuth2TokensNeedingRefresh(LocalDateTime expiryThreshold) {
        String sql = "SELECT * FROM " + TABLE_NAME +
                    " WHERE auth_type = 'OAUTH2' AND token_expires_at < ? AND is_active = true";
        return sqlQueryExecutor.queryForList(sql, rowMapper, ResultSetMapper.toTimestamp(expiryThreshold));
    }

    public void incrementErrorCount(UUID id) {
        String sql = "UPDATE " + TABLE_NAME + " SET error_count = COALESCE(error_count, 0) + 1 WHERE id = ?";
        sqlQueryExecutor.update(sql, id);
    }

    public void resetErrorCount(UUID id) {
        String sql = "UPDATE " + TABLE_NAME + " SET error_count = 0 WHERE id = ?";
        sqlQueryExecutor.update(sql, id);
    }

    public void updateTokenInfo(UUID id, String accessToken, String refreshToken, LocalDateTime expiresAt) {
        String sql = "UPDATE " + TABLE_NAME +
                    " SET access_token = ?, refresh_token = ?, token_expires_at = ?, " +
                    "last_used_at = CURRENT_TIMESTAMP, error_count = 0 WHERE id = ?";
        sqlQueryExecutor.update(sql, accessToken, refreshToken, ResultSetMapper.toTimestamp(expiresAt), id);
    }

    public void incrementUsageCount(UUID id) {
        String sql = "UPDATE " + TABLE_NAME +
                    " SET usage_count = COALESCE(usage_count, 0) + 1, last_used_at = CURRENT_TIMESTAMP WHERE id = ?";
        sqlQueryExecutor.update(sql, id);
    }

    public List<ExternalAuthentication> findByBusinessComponentAndIsActiveTrue(com.integrixs.data.model.BusinessComponent businessComponent) {
        String sql = "SELECT * FROM " + TABLE_NAME +
                    " WHERE business_component_id = ? AND is_active = true ORDER BY name";
        return sqlQueryExecutor.queryForList(sql, rowMapper, businessComponent.getId());
    }
}
