package com.integrixs.data.sql.repository;

import com.integrixs.data.model.SystemConfiguration;
import com.integrixs.data.model.User;
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
 * SQL implementation of SystemConfigurationRepository using native queries.
 */
@Repository("systemConfigurationSqlRepository")
public class SystemConfigurationSqlRepository extends BaseSqlRepository<SystemConfiguration, UUID> {

    private static final String TABLE_NAME = "system_configuration";
    private static final String ID_COLUMN = "id";

    /**
     * Row mapper for SystemConfiguration entity (without User relationships)
     */
    private static final RowMapper<SystemConfiguration> SYSTEM_CONFIG_ROW_MAPPER = new RowMapper<SystemConfiguration>() {
        @Override
        public SystemConfiguration mapRow(ResultSet rs, int rowNum) throws SQLException {
            return SystemConfiguration.builder()
                    .id(ResultSetMapper.getUUID(rs, "id"))
                    .configKey(ResultSetMapper.getString(rs, "config_key"))
                    .configValue(ResultSetMapper.getString(rs, "config_value"))
                    .configType(ResultSetMapper.getString(rs, "config_type"))
                    .description(ResultSetMapper.getString(rs, "description"))
                    .createdAt(ResultSetMapper.getLocalDateTime(rs, "created_at"))
                    .updatedAt(ResultSetMapper.getLocalDateTime(rs, "updated_at"))
                    .build();
        }
    };

    /**
     * Row mapper for SystemConfiguration with User relationships
     */
    private static final RowMapper<SystemConfiguration> SYSTEM_CONFIG_WITH_USERS_ROW_MAPPER = new RowMapper<SystemConfiguration>() {
        @Override
        public SystemConfiguration mapRow(ResultSet rs, int rowNum) throws SQLException {
            SystemConfiguration config = SystemConfiguration.builder()
                    .id(ResultSetMapper.getUUID(rs, "id"))
                    .configKey(ResultSetMapper.getString(rs, "config_key"))
                    .configValue(ResultSetMapper.getString(rs, "config_value"))
                    .configType(ResultSetMapper.getString(rs, "config_type"))
                    .description(ResultSetMapper.getString(rs, "description"))
                    .createdAt(ResultSetMapper.getLocalDateTime(rs, "created_at"))
                    .updatedAt(ResultSetMapper.getLocalDateTime(rs, "updated_at"))
                    .build();

            // Map created by user if exists
            UUID createdById = ResultSetMapper.getUUID(rs, "created_by");
            if (createdById != null) {
                User createdBy = new User();
                createdBy.setId(createdById);
                createdBy.setUsername(ResultSetMapper.getString(rs, "created_by_username"));
                createdBy.setEmail(ResultSetMapper.getString(rs, "created_by_email"));
                createdBy.setFirstName(ResultSetMapper.getString(rs, "created_by_first_name"));
                createdBy.setLastName(ResultSetMapper.getString(rs, "created_by_last_name"));
                config.setCreatedBy(createdBy);
            }

            // Map updated by user if exists
            UUID updatedById = ResultSetMapper.getUUID(rs, "updated_by");
            if (updatedById != null) {
                User updatedBy = new User();
                updatedBy.setId(updatedById);
                updatedBy.setUsername(ResultSetMapper.getString(rs, "updated_by_username"));
                updatedBy.setEmail(ResultSetMapper.getString(rs, "updated_by_email"));
                updatedBy.setFirstName(ResultSetMapper.getString(rs, "updated_by_first_name"));
                updatedBy.setLastName(ResultSetMapper.getString(rs, "updated_by_last_name"));
                config.setUpdatedBy(updatedBy);
            }

            return config;
        }
    };

    private final UserSqlRepository userRepository;

    public SystemConfigurationSqlRepository(SqlQueryExecutor sqlQueryExecutor, UserSqlRepository userRepository) {
        super(sqlQueryExecutor, TABLE_NAME, ID_COLUMN, SYSTEM_CONFIG_ROW_MAPPER);
        this.userRepository = userRepository;
    }

    @Override
    public Optional<SystemConfiguration> findById(UUID id) {
        String sql = "SELECT sc.*, " +
                     "cu.username as created_by_username, cu.email as created_by_email, " +
                     "cu.first_name as created_by_first_name, cu.last_name as created_by_last_name, " +
                     "uu.username as updated_by_username, uu.email as updated_by_email, " +
                     "uu.first_name as updated_by_first_name, uu.last_name as updated_by_last_name " +
                     "FROM " + TABLE_NAME + " sc " +
                     "LEFT JOIN users cu ON sc.created_by = cu.id " +
                     "LEFT JOIN users uu ON sc.updated_by = uu.id " +
                     "WHERE sc.id = ?";

        List<SystemConfiguration> results = sqlQueryExecutor.queryForList(sql, SYSTEM_CONFIG_WITH_USERS_ROW_MAPPER, id);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public List<SystemConfiguration> findAll() {
        String sql = "SELECT sc.*, " +
                     "cu.username as created_by_username, cu.email as created_by_email, " +
                     "cu.first_name as created_by_first_name, cu.last_name as created_by_last_name, " +
                     "uu.username as updated_by_username, uu.email as updated_by_email, " +
                     "uu.first_name as updated_by_first_name, uu.last_name as updated_by_last_name " +
                     "FROM " + TABLE_NAME + " sc " +
                     "LEFT JOIN users cu ON sc.created_by = cu.id " +
                     "LEFT JOIN users uu ON sc.updated_by = uu.id " +
                     "ORDER BY sc.config_key";

        return sqlQueryExecutor.queryForList(sql, SYSTEM_CONFIG_WITH_USERS_ROW_MAPPER);
    }

    public Page<SystemConfiguration> findAll(Pageable pageable) {
        String baseQuery = "SELECT sc.*, " +
                          "cu.username as created_by_username, cu.email as created_by_email, " +
                          "cu.first_name as created_by_first_name, cu.last_name as created_by_last_name, " +
                          "uu.username as updated_by_username, uu.email as updated_by_email, " +
                          "uu.first_name as updated_by_first_name, uu.last_name as updated_by_last_name " +
                          "FROM " + TABLE_NAME + " sc " +
                          "LEFT JOIN users cu ON sc.created_by = cu.id " +
                          "LEFT JOIN users uu ON sc.updated_by = uu.id";

        String countQuery = "SELECT COUNT(*) FROM " + TABLE_NAME;

        long total = sqlQueryExecutor.count(countQuery);
        String paginatedQuery = baseQuery + SqlPaginationHelper.buildOrderByClause(pageable.getSort()) + SqlPaginationHelper.buildPaginationClause(pageable);
        List<SystemConfiguration> configs = sqlQueryExecutor.queryForList(paginatedQuery, SYSTEM_CONFIG_WITH_USERS_ROW_MAPPER);

        return new PageImpl<>(configs, pageable, total);
    }

    public Optional<SystemConfiguration> findByConfigKey(String configKey) {
        String sql = "SELECT sc.*, " +
                     "cu.username as created_by_username, cu.email as created_by_email, " +
                     "cu.first_name as created_by_first_name, cu.last_name as created_by_last_name, " +
                     "uu.username as updated_by_username, uu.email as updated_by_email, " +
                     "uu.first_name as updated_by_first_name, uu.last_name as updated_by_last_name " +
                     "FROM " + TABLE_NAME + " sc " +
                     "LEFT JOIN users cu ON sc.created_by = cu.id " +
                     "LEFT JOIN users uu ON sc.updated_by = uu.id " +
                     "WHERE sc.config_key = ?";

        List<SystemConfiguration> results = sqlQueryExecutor.queryForList(sql, SYSTEM_CONFIG_WITH_USERS_ROW_MAPPER, configKey);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public List<SystemConfiguration> findByConfigType(String configType) {
        String sql = "SELECT sc.*, " +
                     "cu.username as created_by_username, cu.email as created_by_email, " +
                     "cu.first_name as created_by_first_name, cu.last_name as created_by_last_name, " +
                     "uu.username as updated_by_username, uu.email as updated_by_email, " +
                     "uu.first_name as updated_by_first_name, uu.last_name as updated_by_last_name " +
                     "FROM " + TABLE_NAME + " sc " +
                     "LEFT JOIN users cu ON sc.created_by = cu.id " +
                     "LEFT JOIN users uu ON sc.updated_by = uu.id " +
                     "WHERE sc.config_type = ? " +
                     "ORDER BY sc.config_key";

        return sqlQueryExecutor.queryForList(sql, SYSTEM_CONFIG_WITH_USERS_ROW_MAPPER, configType);
    }

    public List<SystemConfiguration> findByConfigKeyStartingWith(String prefix) {
        String sql = "SELECT sc.*, " +
                     "cu.username as created_by_username, cu.email as created_by_email, " +
                     "cu.first_name as created_by_first_name, cu.last_name as created_by_last_name, " +
                     "uu.username as updated_by_username, uu.email as updated_by_email, " +
                     "uu.first_name as updated_by_first_name, uu.last_name as updated_by_last_name " +
                     "FROM " + TABLE_NAME + " sc " +
                     "LEFT JOIN users cu ON sc.created_by = cu.id " +
                     "LEFT JOIN users uu ON sc.updated_by = uu.id " +
                     "WHERE sc.config_key LIKE ? " +
                     "ORDER BY sc.config_key";

        return sqlQueryExecutor.queryForList(sql, SYSTEM_CONFIG_WITH_USERS_ROW_MAPPER, prefix + "%");
    }

    public List<SystemConfiguration> findByConfigKeyContaining(String keyword) {
        String sql = "SELECT sc.*, " +
                     "cu.username as created_by_username, cu.email as created_by_email, " +
                     "cu.first_name as created_by_first_name, cu.last_name as created_by_last_name, " +
                     "uu.username as updated_by_username, uu.email as updated_by_email, " +
                     "uu.first_name as updated_by_first_name, uu.last_name as updated_by_last_name " +
                     "FROM " + TABLE_NAME + " sc " +
                     "LEFT JOIN users cu ON sc.created_by = cu.id " +
                     "LEFT JOIN users uu ON sc.updated_by = uu.id " +
                     "WHERE sc.config_key LIKE ? " +
                     "ORDER BY sc.config_key";

        return sqlQueryExecutor.queryForList(sql, SYSTEM_CONFIG_WITH_USERS_ROW_MAPPER, "%" + keyword + "%");
    }

    @Override
    public SystemConfiguration save(SystemConfiguration config) {
        if (config.getId() == null) {
            config.setId(generateId());
        }

        boolean exists = existsById(config.getId());

        if (!exists) {
            return insert(config);
        } else {
            return update(config);
        }
    }

    private SystemConfiguration insert(SystemConfiguration config) {
        String sql = "INSERT INTO " + TABLE_NAME + " (" +
                     "id, config_key, config_value, config_type, description, " +
                     "created_at, updated_at, created_by, updated_by" +
                     ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        LocalDateTime now = LocalDateTime.now();
        config.setCreatedAt(now);
        config.setUpdatedAt(now);

        sqlQueryExecutor.update(sql,
            config.getId(),
            config.getConfigKey(),
            config.getConfigValue(),
            config.getConfigType(),
            config.getDescription(),
            ResultSetMapper.toTimestamp(config.getCreatedAt()),
            ResultSetMapper.toTimestamp(config.getUpdatedAt()),
            config.getCreatedBy() != null ? config.getCreatedBy().getId() : null,
            config.getUpdatedBy() != null ? config.getUpdatedBy().getId() : null
        );

        return config;
    }

    @Override
    public SystemConfiguration update(SystemConfiguration config) {
        String sql = "UPDATE " + TABLE_NAME + " SET " +
                     "config_key = ?, config_value = ?, config_type = ?, description = ?, " +
                     "updated_at = ?, updated_by = ? " +
                     "WHERE id = ?";

        config.setUpdatedAt(LocalDateTime.now());

        sqlQueryExecutor.update(sql,
            config.getConfigKey(),
            config.getConfigValue(),
            config.getConfigType(),
            config.getDescription(),
            ResultSetMapper.toTimestamp(config.getUpdatedAt()),
            config.getUpdatedBy() != null ? config.getUpdatedBy().getId() : null,
            config.getId()
        );

        return config;
    }

    public boolean existsByConfigKey(String configKey) {
        String sql = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE config_key = ?";
        long count = sqlQueryExecutor.count(sql, configKey);
        return count > 0;
    }

    /**
     * Get configuration value by key (optimized version without user info)
     */
    public Optional<String> getValueByKey(String configKey) {
        String sql = "SELECT config_value FROM " + TABLE_NAME + " WHERE config_key = ?";
        List<String> results = sqlQueryExecutor.queryForList(sql, (rs, rowNum) -> rs.getString("config_value"), configKey);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    /**
     * Update only the value for a config key
     */
    public int updateValueByKey(String configKey, String configValue, UUID updatedById) {
        String sql = "UPDATE " + TABLE_NAME + " SET " +
                     "config_value = ?, updated_at = ?, updated_by = ? " +
                     "WHERE config_key = ?";

        return sqlQueryExecutor.update(sql,
            configValue,
            ResultSetMapper.toTimestamp(LocalDateTime.now()),
            updatedById,
            configKey);
    }

    /**
     * Delete configurations by key pattern
     */
    public int deleteByConfigKeyStartingWith(String prefix) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE config_key LIKE ?";
        return sqlQueryExecutor.update(sql, prefix + "%");
    }
}