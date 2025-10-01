package com.integrixs.backend.application.service;

import com.integrixs.backend.api.dto.response.SystemConfigResponse;
import com.integrixs.backend.api.dto.response.TimezoneResponse;
import com.integrixs.backend.domain.service.ConfigurationManagementService;
import com.integrixs.backend.service.AuditTrailService;
import com.integrixs.data.model.SystemConfiguration;
import com.integrixs.data.model.User;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application service for system configuration management
 */
@Service
public class SystemConfigurationApplicationService {

    private static final Logger log = LoggerFactory.getLogger(SystemConfigurationApplicationService.class);


    private final ConfigurationManagementService configurationService;
    private final AuditTrailService auditTrailService;
    private final com.integrixs.data.sql.repository.SystemConfigurationSqlRepository systemConfigurationRepository;

    public SystemConfigurationApplicationService(ConfigurationManagementService configurationService,
                                               AuditTrailService auditTrailService,
                                               com.integrixs.data.sql.repository.SystemConfigurationSqlRepository systemConfigurationRepository) {
        this.configurationService = configurationService;
        this.auditTrailService = auditTrailService;
        this.systemConfigurationRepository = systemConfigurationRepository;
    }

    /**
     * Get all system configurations
     */
    public SystemConfigResponse getAllConfigurations() {
        log.debug("Getting all system configurations");

        Map<String, String> configs = new HashMap<>();
        List<SystemConfiguration> allConfigs = systemConfigurationRepository.findAll();

        for(SystemConfiguration config : allConfigs) {
            configs.put(config.getConfigKey(), config.getConfigValue());
        }

        // Add defaults if not present
        configs.putIfAbsent(ConfigurationManagementService.TIMEZONE_KEY, configurationService.getSystemTimezone());
        configs.putIfAbsent(ConfigurationManagementService.DATE_FORMAT_KEY, configurationService.getDateFormat());
        configs.putIfAbsent(ConfigurationManagementService.TIME_FORMAT_KEY, configurationService.getTimeFormat());
        configs.putIfAbsent(ConfigurationManagementService.DATETIME_FORMAT_KEY, configurationService.getDateTimeFormat());

        return SystemConfigResponse.builder()
            .timezone(configurationService.getSystemTimezone())
            .dateFormat(configurationService.getDateFormat())
            .timeFormat(configurationService.getTimeFormat())
            .dateTimeFormat(configurationService.getDateTimeFormat())
            .allConfigurations(configs)
            .build();
    }

    /**
     * Update system timezone
     */
    @CacheEvict(value = "systemConfig", key = "'timezone'")
    public void updateSystemTimezone(String timezone, User performedBy) {
        log.info("Updating system timezone to: {} by user: {}", timezone, performedBy.getUsername());

        // Validate timezone
        if(!configurationService.isValidTimezone(timezone)) {
            throw new IllegalArgumentException("Invalid timezone: " + timezone);
        }

        // Save configuration
        configurationService.saveConfiguration(
            ConfigurationManagementService.TIMEZONE_KEY,
            timezone,
            "System timezone",
            "STRING"
       );

        // Audit the change
        auditTrailService.logUserAction(
            performedBy,
            "SystemConfiguration",
            ConfigurationManagementService.TIMEZONE_KEY,
            "UPDATE_TIMEZONE"
       );

        log.info("System timezone updated successfully to: {}", timezone);
    }

    /**
     * Update date format
     */
    @CacheEvict(value = "systemConfig", key = "'dateFormat'")
    public void updateDateFormat(String format, User performedBy) {
        log.info("Updating date format to: {} by user: {}", format, performedBy.getUsername());

        configurationService.saveConfiguration(
            ConfigurationManagementService.DATE_FORMAT_KEY,
            format,
            "Date display format",
            "STRING"
       );

        auditTrailService.logUserAction(
            performedBy,
            "SystemConfiguration",
            ConfigurationManagementService.DATE_FORMAT_KEY,
            "UPDATE_DATE_FORMAT"
       );
    }

    /**
     * Update time format
     */
    @CacheEvict(value = "systemConfig", key = "'timeFormat'")
    public void updateTimeFormat(String format, User performedBy) {
        log.info("Updating time format to: {} by user: {}", format, performedBy.getUsername());

        configurationService.saveConfiguration(
            ConfigurationManagementService.TIME_FORMAT_KEY,
            format,
            "Time display format",
            "STRING"
       );

        auditTrailService.logUserAction(
            performedBy,
            "SystemConfiguration",
            ConfigurationManagementService.TIME_FORMAT_KEY,
            "UPDATE_TIME_FORMAT"
       );
    }

    /**
     * Update datetime format
     */
    @CacheEvict(value = "systemConfig", key = "'dateTimeFormat'")
    public void updateDateTimeFormat(String format, User performedBy) {
        log.info("Updating datetime format to: {} by user: {}", format, performedBy.getUsername());

        configurationService.saveConfiguration(
            ConfigurationManagementService.DATETIME_FORMAT_KEY,
            format,
            "DateTime display format",
            "STRING"
       );

        auditTrailService.logUserAction(
            performedBy,
            "SystemConfiguration",
            ConfigurationManagementService.DATETIME_FORMAT_KEY,
            "UPDATE_DATETIME_FORMAT"
       );
    }

    /**
     * Get available timezones
     */
    public List<TimezoneResponse> getAvailableTimezones() {
        return ZoneId.getAvailableZoneIds().stream()
            .sorted()
            .map(zoneId -> TimezoneResponse.builder()
                .id(zoneId)
                .displayName(formatTimezoneDisplay(zoneId))
                .build())
            .collect(Collectors.toList());
    }

    /**
     * Get system timezone info
     */
    @Cacheable(value = "systemConfig", key = "'timezoneInfo'")
    public SystemConfigResponse getTimezoneInfo() {
        return SystemConfigResponse.builder()
            .timezone(configurationService.getSystemTimezone())
            .dateFormat(configurationService.getDateFormat())
            .timeFormat(configurationService.getTimeFormat())
            .dateTimeFormat(configurationService.getDateTimeFormat())
            .build();
    }

    /**
     * Format timezone display name
     */
    private String formatTimezoneDisplay(String zoneId) {
        try {
            TimeZone tz = TimeZone.getTimeZone(zoneId);
            int offset = tz.getRawOffset() / 3600000;
            String offsetStr = offset >= 0 ? " + " + offset : String.valueOf(offset);
            return String.format("%s(UTC%s)", zoneId, offsetStr);
        } catch(Exception e) {
            return zoneId;
        }
    }

    /**
     * Get current environment type
     */
    public String getEnvironmentType() {
        return configurationService.getConfigValue("environment.type", "DEVELOPMENT");
    }

    /**
     * Check if role can create resources
     */
    public boolean canCreateResources(String role) {
        String env = configurationService.getConfigValue("environment.type", "DEVELOPMENT");
        if("PRODUCTION".equals(env) || "QUALITY_ASSURANCE".equals(env)) {
            return false;
        }
        return "ADMINISTRATOR".equals(role) || "DEVELOPER".equals(role) || "INTEGRATOR".equals(role);
    }

    /**
     * Check if role can modify resources
     */
    public boolean canModifyResources(String role) {
        String env = configurationService.getConfigValue("environment.type", "DEVELOPMENT");
        if("PRODUCTION".equals(env) || "QUALITY_ASSURANCE".equals(env)) {
            return false;
        }
        return "ADMINISTRATOR".equals(role) || "DEVELOPER".equals(role) || "INTEGRATOR".equals(role);
    }

    /**
     * Check if role can delete resources
     */
    public boolean canDeleteResources(String role) {
        String env = configurationService.getConfigValue("environment.type", "DEVELOPMENT");
        if("PRODUCTION".equals(env) || "QUALITY_ASSURANCE".equals(env)) {
            return false;
        }
        return "ADMINISTRATOR".equals(role) || "DEVELOPER".equals(role);
    }

    /**
     * Check if role can deploy flows
     */
    public boolean canDeployFlows(String role) {
        return "ADMINISTRATOR".equals(role) || "DEVELOPER".equals(role) || "INTEGRATOR".equals(role);
    }
}
