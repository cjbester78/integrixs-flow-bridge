package com.integrixs.backend.application.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrixs.backend.dto.configuration.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ConfigurationManagementApplicationService {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationManagementApplicationService.class);

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    @Value("${integrixs.encryption.enabled:true}")
    private boolean encryptionEnabled;

    @Autowired
    public ConfigurationManagementApplicationService(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    public List<ConfigurationCategoryDto> getCategories() {
        String sql = "SELECT * FROM configuration_categories WHERE is_active = true ORDER BY display_order";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            ConfigurationCategoryDto dto = new ConfigurationCategoryDto();
            dto.setId(rs.getString("id"));
            dto.setCode(rs.getString("code"));
            dto.setName(rs.getString("name"));
            dto.setDescription(rs.getString("description"));
            dto.setParentCategoryId(rs.getString("parent_category_id"));
            dto.setDisplayOrder(rs.getInt("display_order"));
            dto.setIsActive(rs.getBoolean("is_active"));
            return dto;
        });
    }

    public List<ConfigurationDto> getConfigurationsByCategory(String categoryCode, String environment) {
        String sql = "SELECT ac.*, cc.code as category_code " +
                    "FROM application_configurations ac " +
                    "JOIN configuration_categories cc ON ac.category_id = cc.id " +
                    "WHERE cc.code = ? ";

        List<Object> params = new ArrayList<>();
        params.add(categoryCode);

        if (environment != null) {
            sql += "AND (ac.environment = ? OR ac.environment IS NULL) ";
            params.add(environment);
        }

        sql += "ORDER BY ac.config_key";

        return jdbcTemplate.query(sql, params.toArray(), (rs, rowNum) -> {
            ConfigurationDto dto = new ConfigurationDto();
            dto.setId(rs.getString("id"));
            dto.setCategoryId(rs.getString("category_id"));
            dto.setConfigKey(rs.getString("config_key"));
            dto.setConfigValue(rs.getString("config_value"));
            dto.setConfigType(rs.getString("config_type"));
            dto.setDefaultValue(rs.getString("default_value"));
            dto.setDescription(rs.getString("description"));
            dto.setIsRequired(rs.getBoolean("is_required"));
            dto.setIsEncrypted(rs.getBoolean("is_encrypted"));
            dto.setIsSensitive(rs.getBoolean("is_sensitive"));
            dto.setValidationRules(rs.getString("validation_rules"));

            // Parse allowed values from JSON
            String allowedValuesJson = rs.getString("allowed_values");
            if (allowedValuesJson != null) {
                try {
                    dto.setAllowedValues(objectMapper.readValue(allowedValuesJson,
                        new TypeReference<List<String>>() {}));
                } catch (Exception e) {
                    logger.warn("Failed to parse allowed values", e);
                }
            }

            dto.setEnvironment(rs.getString("environment"));
            dto.setProfile(rs.getString("profile"));
            return dto;
        });
    }

    public List<MessagingConfigurationDto> getMessagingConfigurations(String messagingSystem) {
        String sql = "SELECT * FROM messaging_configurations ";
        List<Object> params = new ArrayList<>();

        if (messagingSystem != null) {
            sql += "WHERE messaging_system = ? ";
            params.add(messagingSystem);
        }

        sql += "ORDER BY messaging_system, config_group, config_key";

        return jdbcTemplate.query(sql, params.toArray(), (rs, rowNum) -> {
            MessagingConfigurationDto dto = new MessagingConfigurationDto();
            dto.setId(rs.getString("id"));
            dto.setMessagingSystem(rs.getString("messaging_system"));
            dto.setConfigGroup(rs.getString("config_group"));
            dto.setConfigKey(rs.getString("config_key"));
            dto.setConfigValue(rs.getString("config_value"));
            dto.setConfigType(rs.getString("config_type"));
            dto.setDefaultValue(rs.getString("default_value"));
            dto.setDescription(rs.getString("description"));
            dto.setIsRequired(rs.getBoolean("is_required"));
            dto.setIsEncrypted(rs.getBoolean("is_encrypted"));
            dto.setEnvironment(rs.getString("environment"));
            return dto;
        });
    }

    public List<SecurityConfigurationDto> getSecurityConfigurations(String securityDomain) {
        String sql = "SELECT * FROM security_configurations ";
        List<Object> params = new ArrayList<>();

        if (securityDomain != null) {
            sql += "WHERE security_domain = ? ";
            params.add(securityDomain);
        }

        sql += "ORDER BY security_domain, config_key";

        return jdbcTemplate.query(sql, params.toArray(), (rs, rowNum) -> {
            SecurityConfigurationDto dto = new SecurityConfigurationDto();
            dto.setId(rs.getString("id"));
            dto.setSecurityDomain(rs.getString("security_domain"));
            dto.setConfigKey(rs.getString("config_key"));
            dto.setConfigValue(rs.getString("config_value"));
            dto.setConfigType(rs.getString("config_type"));
            dto.setDefaultValue(rs.getString("default_value"));
            dto.setDescription(rs.getString("description"));
            dto.setIsRequired(rs.getBoolean("is_required"));
            dto.setIsEncrypted(rs.getBoolean("is_encrypted"));
            dto.setIsSensitive(rs.getBoolean("is_sensitive"));
            dto.setEnvironment(rs.getString("environment"));
            return dto;
        });
    }

    public List<AdapterConfigurationDto> getAdapterDefaultConfigurations(String adapterTypeId) {
        String sql = "SELECT * FROM adapter_default_configurations " +
                    "WHERE adapter_type_id = ? " +
                    "ORDER BY display_order, config_key";

        return jdbcTemplate.query(sql, new Object[]{adapterTypeId}, (rs, rowNum) -> {
            AdapterConfigurationDto dto = new AdapterConfigurationDto();
            dto.setId(rs.getString("id"));
            dto.setAdapterTypeId(rs.getString("adapter_type_id"));
            dto.setConfigKey(rs.getString("config_key"));
            dto.setConfigValue(rs.getString("config_value"));
            dto.setConfigType(rs.getString("config_type"));
            dto.setDefaultValue(rs.getString("default_value"));
            dto.setDescription(rs.getString("description"));
            dto.setIsRequired(rs.getBoolean("is_required"));
            dto.setIsEncrypted(rs.getBoolean("is_encrypted"));
            dto.setValidationRules(rs.getString("validation_rules"));
            dto.setDisplayOrder(rs.getInt("display_order"));
            dto.setUiComponent(rs.getString("ui_component"));
            dto.setUiOptions(rs.getString("ui_options"));
            return dto;
        });
    }

    public void updateConfiguration(String configKey, String configValue, String tableName) {
        String sql = String.format("UPDATE %s SET config_value = ?, updated_at = ? WHERE config_key = ?", tableName);
        int updated = jdbcTemplate.update(sql, configValue, LocalDateTime.now(), configKey);

        if (updated > 0) {
            // Log to history
            logConfigurationChange(tableName, configKey, configValue);
            logger.info("Updated configuration {} in table {}", configKey, tableName);
        }
    }

    public void updateConfigurations(Map<String, String> configurations, String tableName) {
        for (Map.Entry<String, String> entry : configurations.entrySet()) {
            updateConfiguration(entry.getKey(), entry.getValue(), tableName);
        }
    }

    public void refreshConfigurations() {
        // This would trigger a refresh event in a real implementation
        logger.info("Configuration refresh requested");
        // In a real implementation, this would notify all services to reload configs
    }

    public byte[] exportConfigurations(String category, String environment) {
        Map<String, Object> exportData = new HashMap<>();

        if (category != null) {
            exportData.put("configurations", getConfigurationsByCategory(category, environment));
        } else {
            // Export all
            exportData.put("application", getConfigurationsByCategory("general", environment));
            exportData.put("messaging", getMessagingConfigurations(null));
            exportData.put("security", getSecurityConfigurations(null));
        }

        try {
            return objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsBytes(exportData);
        } catch (Exception e) {
            logger.error("Failed to export configurations", e);
            throw new RuntimeException("Export failed", e);
        }
    }

    public Map<String, Integer> importConfigurations(MultipartFile file) {
        Map<String, Integer> result = new HashMap<>();
        result.put("imported", 0);
        result.put("skipped", 0);

        try {
            Map<String, Object> importData = objectMapper.readValue(
                file.getBytes(),
                new TypeReference<Map<String, Object>>() {}
            );

            // Process import data
            // This is a simplified version - real implementation would handle
            // different configuration types and validation

            logger.info("Configuration import completed: {}", result);
        } catch (IOException e) {
            logger.error("Failed to import configurations", e);
            throw new RuntimeException("Import failed", e);
        }

        return result;
    }

    private void logConfigurationChange(String tableName, String configKey, String newValue) {
        // TODO: Get actual user from security context
        String currentUser = getCurrentUser();

        String sql = "INSERT INTO configuration_history " +
                    "(table_name, config_key, old_value, new_value, changed_by, changed_at) " +
                    "VALUES (?, ?, " +
                    "(SELECT config_value FROM " + tableName + " WHERE config_key = ?), " +
                    "?, ?, ?)";

        jdbcTemplate.update(sql, tableName, configKey, configKey, newValue, currentUser, LocalDateTime.now());
    }

    private String getCurrentUser() {
        // Get from Spring Security context when available
        return "system";
    }
}