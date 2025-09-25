package com.integrixs.backend.domain.service;

import com.integrixs.data.sql.repository.SystemConfigurationSqlRepository;
import com.integrixs.data.model.SystemConfiguration;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.zone.ZoneRulesException;
import java.util.Optional;

/**
 * Domain service for managing system configurations
 */
@Service
public class ConfigurationManagementService {

    private final SystemConfigurationSqlRepository configurationRepository;

    public ConfigurationManagementService(SystemConfigurationSqlRepository configurationRepository) {
        this.configurationRepository = configurationRepository;
    }

    // Configuration keys
    public static final String TIMEZONE_KEY = "system.timezone";
    public static final String DATE_FORMAT_KEY = "system.dateFormat";
    public static final String TIME_FORMAT_KEY = "system.timeFormat";
    public static final String DATETIME_FORMAT_KEY = "system.dateTimeFormat";

    // Default values
    private static final String DEFAULT_TIMEZONE = "Africa/Johannesburg";
    private static final String DEFAULT_DATE_FORMAT = "yyyy - MM - dd";
    private static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";
    private static final String DEFAULT_DATETIME_FORMAT = "yyyy - MM - dd HH:mm:ss";

    /**
     * Get configuration value by key
     */
    public String getConfigValue(String key, String defaultValue) {
        Optional<SystemConfiguration> config = configurationRepository.findByConfigKey(key);
        return config.map(SystemConfiguration::getConfigValue).orElse(defaultValue);
    }

    /**
     * Validate timezone string
     */
    public boolean isValidTimezone(String timezone) {
        try {
            ZoneId.of(timezone);
            return true;
        } catch(ZoneRulesException | NullPointerException e) {
            return false;
        }
    }

    /**
     * Create or update a configuration
     */
    public SystemConfiguration saveConfiguration(String key, String value, String description, String configType) {
        SystemConfiguration config = configurationRepository.findByConfigKey(key)
            .orElse(SystemConfiguration.builder()
                .configKey(key)
                .configType(configType)
                .description(description)
                .build());

        config.setConfigValue(value);
        return configurationRepository.save(config);
    }

    /**
     * Get system timezone
     */
    public String getSystemTimezone() {
        return getConfigValue(TIMEZONE_KEY, DEFAULT_TIMEZONE);
    }

    /**
     * Get date format
     */
    public String getDateFormat() {
        return getConfigValue(DATE_FORMAT_KEY, DEFAULT_DATE_FORMAT);
    }

    /**
     * Get time format
     */
    public String getTimeFormat() {
        return getConfigValue(TIME_FORMAT_KEY, DEFAULT_TIME_FORMAT);
    }

    /**
     * Get datetime format
     */
    public String getDateTimeFormat() {
        return getConfigValue(DATETIME_FORMAT_KEY, DEFAULT_DATETIME_FORMAT);
    }
}
