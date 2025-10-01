package com.integrixs.backend.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.io.support.ResourcePropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Environment post-processor that loads configurations from database
 * This runs early in the Spring Boot startup process
 */
public class DatabasePropertySourceFactory implements EnvironmentPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(DatabasePropertySourceFactory.class);
    private static final String PROPERTY_SOURCE_NAME = "databaseConfigurationProperties";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        // Check if database configuration is enabled
        String useDbConfig = environment.getProperty("spring.config.use-database", "true");
        if (!"true".equals(useDbConfig)) {
            logger.info("Database configuration is disabled. Using YAML files.");
            return;
        }

        try {
            // Create a temporary data source to load configurations
            DataSource dataSource = createDataSource(environment);
            Map<String, Object> configurations = loadConfigurationsFromDatabase(dataSource, environment);

            if (!configurations.isEmpty()) {
                // Create property source and add it
                MapPropertySource propertySource = new MapPropertySource(PROPERTY_SOURCE_NAME, configurations);
                MutablePropertySources propertySources = environment.getPropertySources();

                // Add after system environment but before application properties
                if (propertySources.contains("systemEnvironment")) {
                    propertySources.addAfter("systemEnvironment", propertySource);
                } else {
                    propertySources.addFirst(propertySource);
                }

                logger.info("Loaded {} configurations from database", configurations.size());
            }
        } catch (Exception e) {
            logger.warn("Could not load configurations from database. Using default YAML configurations.", e);
        }
    }

    private DataSource createDataSource(ConfigurableEnvironment environment) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();

        // Get database connection properties from environment
        String url = environment.getProperty("spring.datasource.url");
        String username = environment.getProperty("spring.datasource.username");
        String password = environment.getProperty("spring.datasource.password");
        String driverClassName = environment.getProperty("spring.datasource.driver-class-name",
            "org.postgresql.Driver");

        // Return null if required properties are missing
        if (url == null || username == null || password == null) {
            throw new IllegalStateException("Database connection properties are not configured");
        }

        dataSource.setDriverClassName(driverClassName);
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        return dataSource;
    }

    private Map<String, Object> loadConfigurationsFromDatabase(DataSource dataSource, ConfigurableEnvironment environment) {
        Map<String, Object> configurations = new HashMap<>();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        String activeEnv = determineEnvironment(environment);
        List<String> activeProfiles = List.of(environment.getActiveProfiles());

        try {
            // Load application configurations
            loadApplicationConfigurations(jdbcTemplate, configurations, activeEnv, activeProfiles);

            // Load messaging configurations
            loadMessagingConfigurations(jdbcTemplate, configurations, activeEnv);

            // Load security configurations
            loadSecurityConfigurations(jdbcTemplate, configurations, activeEnv);

            // Load database configurations
            loadDatabaseConfigurations(jdbcTemplate, configurations, activeEnv);

            // Load monitoring configurations
            loadMonitoringConfigurations(jdbcTemplate, configurations);

            // Load integration configurations
            loadIntegrationConfigurations(jdbcTemplate, configurations);

            // Load feature flags
            loadFeatureConfigurations(jdbcTemplate, configurations);

        } catch (Exception e) {
            logger.error("Error loading configurations from database", e);
        }

        return configurations;
    }

    private void loadApplicationConfigurations(JdbcTemplate jdbcTemplate, Map<String, Object> configs,
                                              String environment, List<String> profiles) {
        String sql = """
            SELECT config_key, config_value, config_type, is_encrypted
            FROM application_configurations
            WHERE (environment IS NULL OR environment = ?)
            AND (profile IS NULL OR profile = ANY(?))
            """;

        jdbcTemplate.query(sql, rs -> {
            String key = rs.getString("config_key");
            String value = rs.getString("config_value");
            String type = rs.getString("config_type");
            boolean isEncrypted = rs.getBoolean("is_encrypted");

            if (isEncrypted) {
                value = decryptValue(value);
            }

            configs.put(key, convertValue(value, type));
        }, environment, profiles.toArray(new String[0]));
    }

    private void loadMessagingConfigurations(JdbcTemplate jdbcTemplate, Map<String, Object> configs, String environment) {
        String sql = """
            SELECT
                LOWER(messaging_system) || '.' || config_group || '.' || config_key as full_key,
                config_value,
                config_type,
                is_encrypted
            FROM messaging_configurations
            WHERE environment IS NULL OR environment = ?
            """;

        jdbcTemplate.query(sql, rs -> {
            String key = "spring." + rs.getString("full_key").toLowerCase();
            String value = rs.getString("config_value");
            String type = rs.getString("config_type");
            boolean isEncrypted = rs.getBoolean("is_encrypted");

            if (isEncrypted) {
                value = decryptValue(value);
            }

            configs.put(key, convertValue(value, type));
        }, environment);
    }

    private void loadSecurityConfigurations(JdbcTemplate jdbcTemplate, Map<String, Object> configs, String environment) {
        String sql = """
            SELECT
                LOWER(security_domain) || '.' || config_key as full_key,
                config_value,
                config_type,
                is_encrypted
            FROM security_configurations
            WHERE environment IS NULL OR environment = ?
            """;

        jdbcTemplate.query(sql, rs -> {
            String key = rs.getString("full_key").toLowerCase();
            String value = rs.getString("config_value");
            String type = rs.getString("config_type");
            boolean isEncrypted = rs.getBoolean("is_encrypted");

            if (isEncrypted || type.equals("ENCRYPTED")) {
                value = decryptValue(value);
            }

            configs.put(key, convertValue(value, type));
        }, environment);
    }

    private void loadDatabaseConfigurations(JdbcTemplate jdbcTemplate, Map<String, Object> configs, String environment) {
        String sql = """
            SELECT
                config_key,
                config_value,
                config_type,
                is_encrypted
            FROM database_configurations
            WHERE database_type = 'PRIMARY'
            AND (environment IS NULL OR environment = ?)
            """;

        jdbcTemplate.query(sql, rs -> {
            String key = rs.getString("config_key");
            String value = rs.getString("config_value");
            String type = rs.getString("config_type");
            boolean isEncrypted = rs.getBoolean("is_encrypted");

            if (isEncrypted || type.equals("ENCRYPTED")) {
                value = decryptValue(value);
            }

            configs.put(key, convertValue(value, type));
        }, environment);
    }

    private void loadMonitoringConfigurations(JdbcTemplate jdbcTemplate, Map<String, Object> configs) {
        String sql = """
            SELECT
                LOWER(monitoring_type) || '.' || config_key as full_key,
                config_value,
                config_type
            FROM monitoring_configurations
            """;

        jdbcTemplate.query(sql, rs -> {
            String key = rs.getString("full_key").toLowerCase();
            String value = rs.getString("config_value");
            String type = rs.getString("config_type");

            configs.put(key, convertValue(value, type));
        });
    }

    private void loadIntegrationConfigurations(JdbcTemplate jdbcTemplate, Map<String, Object> configs) {
        String sql = """
            SELECT
                LOWER(integration_type) || '.' ||
                CASE WHEN config_group IS NOT NULL THEN config_group || '.' ELSE '' END ||
                config_key as full_key,
                config_value,
                config_type,
                is_encrypted
            FROM integration_configurations
            """;

        jdbcTemplate.query(sql, rs -> {
            String key = rs.getString("full_key").toLowerCase();
            String value = rs.getString("config_value");
            String type = rs.getString("config_type");
            boolean isEncrypted = rs.getBoolean("is_encrypted");

            if (isEncrypted) {
                value = decryptValue(value);
            }

            configs.put(key, convertValue(value, type));
        });
    }

    private void loadFeatureConfigurations(JdbcTemplate jdbcTemplate, Map<String, Object> configs) {
        String sql = """
            SELECT
                'features.' || feature_key as full_key,
                is_enabled
            FROM feature_configurations
            WHERE (start_date IS NULL OR start_date <= CURRENT_TIMESTAMP)
            AND (end_date IS NULL OR end_date >= CURRENT_TIMESTAMP)
            """;

        jdbcTemplate.query(sql, rs -> {
            String key = rs.getString("full_key");
            boolean value = rs.getBoolean("is_enabled");
            configs.put(key, value);
        });
    }

    private Object convertValue(String value, String type) {
        if (value == null) {
            return null;
        }

        return switch (type.toUpperCase()) {
            case "NUMBER" -> {
                try {
                    if (value.contains(".")) {
                        yield Double.parseDouble(value);
                    } else {
                        yield Long.parseLong(value);
                    }
                } catch (NumberFormatException e) {
                    yield value;
                }
            }
            case "BOOLEAN" -> Boolean.parseBoolean(value);
            case "JSON", "YAML" -> value; // Keep as string, will be parsed by consumers
            default -> value;
        };
    }

    private String decryptValue(String encryptedValue) {
        // TODO: Implement proper decryption using the master key
        // For now, return the value as-is
        return encryptedValue;
    }

    private String determineEnvironment(ConfigurableEnvironment environment) {
        String[] profiles = environment.getActiveProfiles();
        for (String profile : profiles) {
            if ("prod".equals(profile)) {
                return "prod";
            } else if ("test".equals(profile)) {
                return "test";
            }
        }
        return "dev";
    }
}