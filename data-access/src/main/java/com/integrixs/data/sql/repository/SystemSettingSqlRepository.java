package com.integrixs.data.sql.repository;

import com.integrixs.data.model.SystemSetting;
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

/**
 * SQL implementation of SystemSettingRepository using native queries.
 */
@Repository("systemSettingSqlRepository")
public class SystemSettingSqlRepository extends BaseSqlRepository<SystemSetting, UUID> {

    private static final String TABLE_NAME = "system_settings";
    private static final String ID_COLUMN = "id";

    /**
     * Row mapper for SystemSetting entity
     */
    private static final RowMapper<SystemSetting> SYSTEM_SETTING_ROW_MAPPER = new RowMapper<SystemSetting>() {
        @Override
        public SystemSetting mapRow(ResultSet rs, int rowNum) throws SQLException {
            SystemSetting setting = new SystemSetting();
            setting.setId(ResultSetMapper.getUUID(rs, "id"));
            setting.setSettingKey(ResultSetMapper.getString(rs, "setting_key"));
            setting.setSettingValue(ResultSetMapper.getString(rs, "setting_value"));
            setting.setDescription(ResultSetMapper.getString(rs, "description"));
            setting.setCategory(ResultSetMapper.getString(rs, "category"));
            setting.setDataType(ResultSetMapper.getString(rs, "data_type"));
            setting.setEncrypted(ResultSetMapper.getBoolean(rs, "is_encrypted"));
            setting.setReadonly(ResultSetMapper.getBoolean(rs, "is_readonly"));
            setting.setCreatedAt(ResultSetMapper.getLocalDateTime(rs, "created_at"));
            setting.setUpdatedAt(ResultSetMapper.getLocalDateTime(rs, "updated_at"));
            setting.setCreatedBy(ResultSetMapper.getString(rs, "created_by"));
            setting.setUpdatedBy(ResultSetMapper.getString(rs, "updated_by"));
            return setting;
        }
    };

    public SystemSettingSqlRepository(SqlQueryExecutor sqlQueryExecutor) {
        super(sqlQueryExecutor, TABLE_NAME, ID_COLUMN, SYSTEM_SETTING_ROW_MAPPER);
    }

    /**
     * Find a system setting by its key
     */
    public Optional<SystemSetting> findBySettingKey(String settingKey) {
        String sql = "SELECT * FROM system_settings WHERE setting_key = ?";
        return sqlQueryExecutor.queryForObject(sql, SYSTEM_SETTING_ROW_MAPPER, settingKey);
    }

    /**
     * Find all settings by category
     */
    public List<SystemSetting> findByCategory(String category) {
        String sql = "SELECT * FROM system_settings WHERE category = ?";
        return sqlQueryExecutor.queryForList(sql, SYSTEM_SETTING_ROW_MAPPER, category);
    }

    /**
     * Find all settings by category ordered by setting key
     */
    public List<SystemSetting> findByCategoryOrderBySettingKeyAsc(String category) {
        String sql = "SELECT * FROM system_settings WHERE category = ? ORDER BY setting_key ASC";
        return sqlQueryExecutor.queryForList(sql, SYSTEM_SETTING_ROW_MAPPER, category);
    }

    /**
     * Find all non-readonly settings
     */
    public List<SystemSetting> findByIsReadonlyFalse() {
        String sql = "SELECT * FROM system_settings WHERE is_readonly = false";
        return sqlQueryExecutor.queryForList(sql, SYSTEM_SETTING_ROW_MAPPER);
    }

    /**
     * Find all settings by category that are not readonly
     */
    public List<SystemSetting> findByCategoryAndIsReadonlyFalse(String category) {
        String sql = "SELECT * FROM system_settings WHERE category = ? AND is_readonly = false";
        return sqlQueryExecutor.queryForList(sql, SYSTEM_SETTING_ROW_MAPPER, category);
    }

    /**
     * Check if a setting key exists
     */
    public boolean existsBySettingKey(String settingKey) {
        String sql = "SELECT COUNT(*) FROM system_settings WHERE setting_key = ?";
        return sqlQueryExecutor.exists(sql, settingKey);
    }

    /**
     * Get all settings with non-null categories for distinct processing
     */
    public List<SystemSetting> findByCategoryIsNotNullOrderByCategory() {
        String sql = "SELECT * FROM system_settings WHERE category IS NOT NULL ORDER BY category";
        return sqlQueryExecutor.queryForList(sql, SYSTEM_SETTING_ROW_MAPPER);
    }

    @Override
    public SystemSetting save(SystemSetting setting) {
        if (setting.getId() == null) {
            setting.setId(generateId());
        }

        LocalDateTime now = LocalDateTime.now();
        if (setting.getCreatedAt() == null) {
            setting.setCreatedAt(now);
        }
        if (setting.getUpdatedAt() == null) {
            setting.setUpdatedAt(now);
        }

        String sql = buildInsertSql(
            "id", "setting_key", "setting_value", "description", "category",
            "data_type", "is_encrypted", "is_readonly", "created_at", "updated_at",
            "created_by", "updated_by"
        );

        sqlQueryExecutor.update(sql,
            setting.getId(),
            setting.getSettingKey(),
            setting.getSettingValue(),
            setting.getDescription(),
            setting.getCategory(),
            setting.getDataType(),
            setting.isEncrypted(),
            setting.isReadonly(),
            ResultSetMapper.toTimestamp(setting.getCreatedAt()),
            ResultSetMapper.toTimestamp(setting.getUpdatedAt()),
            setting.getCreatedBy(),
            setting.getUpdatedBy()
        );

        return setting;
    }

    @Override
    public SystemSetting update(SystemSetting setting) {
        setting.setUpdatedAt(LocalDateTime.now());

        String sql = buildUpdateSql(
            "setting_key", "setting_value", "description", "category",
            "data_type", "is_encrypted", "is_readonly", "updated_at", "updated_by"
        );

        sqlQueryExecutor.update(sql,
            setting.getSettingKey(),
            setting.getSettingValue(),
            setting.getDescription(),
            setting.getCategory(),
            setting.getDataType(),
            setting.isEncrypted(),
            setting.isReadonly(),
            ResultSetMapper.toTimestamp(setting.getUpdatedAt()),
            setting.getUpdatedBy(),
            setting.getId()
        );

        return setting;
    }
}