package com.integrixs.backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DatabaseConfigurationService implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfigurationService.class);

    private final Map<String, String> configurationCache = new ConcurrentHashMap<>();
    private JdbcTemplate jdbcTemplate;

    @Value("${spring.profiles.active:}")
    private String activeProfiles;

    @Value("${spring.config.use-database:true}")
    private boolean useDatabaseConfig;

    @Autowired
    public DatabaseConfigurationService(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        if (useDatabaseConfig) {
            loadConfigurations();
        }
    }

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        if (!useDatabaseConfig) {
            logger.info("Database configuration is disabled. Using YAML files.");
            return;
        }

        ConfigurableEnvironment environment = event.getEnvironment();
        try {
            // Load configurations from database and add as property source
            Map<String, Object> dbConfigs = loadConfigurationsForEnvironment(environment);
            MapPropertySource dbPropertySource = new MapPropertySource("databaseConfig", dbConfigs);

            // Add with higher precedence than application.yml but lower than system properties
            environment.getPropertySources().addAfter("systemEnvironment", dbPropertySource);

            logger.info("Loaded {} configurations from database", dbConfigs.size());
        } catch (Exception e) {
            logger.error("Failed to load configurations from database. Falling back to YAML.", e);
        }
    }

    private void loadConfigurations() {
        try {
            // Load application configurations
            loadApplicationConfigurations();

            // Load messaging configurations
            loadMessagingConfigurations();

            // Load security configurations
            loadSecurityConfigurations();

            // Load database configurations
            loadDatabaseConfigurations();

            // Load monitoring configurations
            loadMonitoringConfigurations();

            // Load integration configurations
            loadIntegrationConfigurations();

            logger.info("Successfully loaded all configurations from database");
        } catch (Exception e) {
            logger.error("Error loading configurations from database", e);
        }
    }

    private void loadApplicationConfigurations() {
        String sql = """
            SELECT ac.config_key, ac.config_value, ac.is_encrypted, cc.code as category
            FROM application_configurations ac
            JOIN configuration_categories cc ON ac.category_id = cc.id
            WHERE (ac.environment IS NULL OR ac.environment = ?)
            AND (ac.profile IS NULL OR ac.profile IN (?))
            """;

        List<String> profiles = Arrays.asList(activeProfiles.split(","));
        jdbcTemplate.query(sql, new Object[]{getEnvironment(), profiles},
            new ConfigurationRowMapper());
    }

    private void loadMessagingConfigurations() {
        String sql = """
            SELECT
                messaging_system || '.' || config_group || '.' || config_key as config_key,
                config_value,
                is_encrypted
            FROM messaging_configurations
            WHERE environment IS NULL OR environment = ?
            """;

        jdbcTemplate.query(sql, new Object[]{getEnvironment()},
            new ConfigurationRowMapper());
    }

    private void loadSecurityConfigurations() {
        String sql = """
            SELECT
                'security.' || security_domain || '.' || config_key as config_key,
                config_value,
                is_encrypted
            FROM security_configurations
            WHERE environment IS NULL OR environment = ?
            """;

        jdbcTemplate.query(sql, new Object[]{getEnvironment()},
            new ConfigurationRowMapper());
    }

    private void loadDatabaseConfigurations() {
        String sql = """
            SELECT
                'datasource.' || database_type || '.' || config_key as config_key,
                config_value,
                is_encrypted
            FROM database_configurations
            WHERE environment IS NULL OR environment = ?
            """;

        jdbcTemplate.query(sql, new Object[]{getEnvironment()},
            new ConfigurationRowMapper());
    }

    private void loadMonitoringConfigurations() {
        String sql = """
            SELECT
                'monitoring.' || monitoring_type || '.' || config_key as config_key,
                config_value,
                false as is_encrypted
            FROM monitoring_configurations
            """;

        jdbcTemplate.query(sql, new ConfigurationRowMapper());
    }

    private void loadIntegrationConfigurations() {
        String sql = """
            SELECT
                'integration.' || integration_type || '.' ||
                CASE WHEN config_group IS NOT NULL THEN config_group || '.' ELSE '' END ||
                config_key as config_key,
                config_value,
                is_encrypted
            FROM integration_configurations
            """;

        jdbcTemplate.query(sql, new ConfigurationRowMapper());
    }

    private Map<String, Object> loadConfigurationsForEnvironment(ConfigurableEnvironment environment) {
        Map<String, Object> configs = new HashMap<>();

        // Load all configurations based on active profiles and environment
        String activeEnv = environment.getProperty("spring.config.active-environment", getEnvironment());
        List<String> profiles = Arrays.asList(environment.getActiveProfiles());

        // Add all cached configurations
        configs.putAll(configurationCache);

        return configs;
    }

    public String getConfiguration(String key) {
        return configurationCache.get(key);
    }

    public String getConfiguration(String key, String defaultValue) {
        return configurationCache.getOrDefault(key, defaultValue);
    }

    public Integer getIntConfiguration(String key, Integer defaultValue) {
        String value = configurationCache.get(key);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                logger.warn("Invalid integer configuration for key: {}", key);
            }
        }
        return defaultValue;
    }

    public Boolean getBooleanConfiguration(String key, Boolean defaultValue) {
        String value = configurationCache.get(key);
        if (value != null) {
            return Boolean.parseBoolean(value);
        }
        return defaultValue;
    }

    public void updateConfiguration(String key, String value) {
        // Update in database
        String updateSql = """
            UPDATE application_configurations
            SET config_value = ?, updated_at = CURRENT_TIMESTAMP
            WHERE config_key = ?
            """;

        int updated = jdbcTemplate.update(updateSql, value, key);
        if (updated > 0) {
            // Update cache
            configurationCache.put(key, value);
            logger.info("Updated configuration: {} = {}", key, value);
        }
    }

    public void refreshConfigurations() {
        logger.info("Refreshing configurations from database");
        configurationCache.clear();
        loadConfigurations();
    }

    private String getEnvironment() {
        // Determine environment from active profiles
        if (activeProfiles.contains("prod")) {
            return "prod";
        } else if (activeProfiles.contains("test")) {
            return "test";
        }
        return "dev";
    }

    private String decryptValue(String encryptedValue) {
        // TODO: Implement decryption logic using master key
        // For now, return as-is
        return encryptedValue;
    }

    private class ConfigurationRowMapper implements RowMapper<Void> {
        @Override
        public Void mapRow(ResultSet rs, int rowNum) throws SQLException {
            String key = rs.getString("config_key");
            String value = rs.getString("config_value");
            boolean isEncrypted = rs.getBoolean("is_encrypted");

            if (isEncrypted) {
                value = decryptValue(value);
            }

            configurationCache.put(key, value);
            return null;
        }
    }
}