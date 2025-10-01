package com.integrixs.backend.config;

import com.integrixs.data.model.SystemConfiguration;
import com.integrixs.data.sql.repository.SystemConfigurationSqlRepository;
import com.integrixs.shared.enums.EnvironmentType;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Initializes environment configuration from database on application startup.
 */
@Component
public class EnvironmentInitializer {

    private static final Logger log = LoggerFactory.getLogger(EnvironmentInitializer.class);


    private final EnvironmentConfig environmentConfig;
    private final SystemConfigurationSqlRepository systemConfigurationRepository;

    private static final String ENVIRONMENT_TYPE_KEY = "environment.type";
    private static final String ENFORCE_RESTRICTIONS_KEY = "environment.enforceRestrictions";

    public EnvironmentInitializer(EnvironmentConfig environmentConfig,
                                 SystemConfigurationSqlRepository systemConfigurationRepository) {
        this.environmentConfig = environmentConfig;
        this.systemConfigurationRepository = systemConfigurationRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initializeEnvironment() {
        log.info("Initializing environment configuration from database...");

        try {
            // Load environment type from database
            Optional<SystemConfiguration> envTypeConfig =
                systemConfigurationRepository.findByConfigKey(ENVIRONMENT_TYPE_KEY);

            if(envTypeConfig.isPresent()) {
                String envTypeValue = envTypeConfig.get().getConfigValue();
                try {
                    EnvironmentType envType = EnvironmentType.valueOf(envTypeValue);
                    environmentConfig.setType(envType);
                    log.info("Environment type loaded from database: {}", envType.getDisplayName());
                } catch(IllegalArgumentException e) {
                    log.error("Invalid environment type in database: {}. Using default: {}",
                        envTypeValue, environmentConfig.getType());
                }
            } else {
                log.info("No environment type found in database. Using default: {}",
                    environmentConfig.getType());

                // Save default to database
                SystemConfiguration config = new SystemConfiguration();
                config.setConfigKey(ENVIRONMENT_TYPE_KEY);
                config.setConfigValue(environmentConfig.getType().name());
                config.setConfigType("STRING");
                config.setDescription("System environment type");
                systemConfigurationRepository.save(config);
            }

            // Load enforce restrictions flag
            Optional<SystemConfiguration> enforceConfig =
                systemConfigurationRepository.findByConfigKey(ENFORCE_RESTRICTIONS_KEY);

            if(enforceConfig.isPresent()) {
                boolean enforce = Boolean.parseBoolean(enforceConfig.get().getConfigValue());
                environmentConfig.setEnforceRestrictions(enforce);
                log.info("Enforce restrictions loaded from database: {}", enforce);
            }

            log.info("Environment initialization complete. Type: {}, Enforce Restrictions: {}",
                environmentConfig.getType().getDisplayName(),
                environmentConfig.isEnforceRestrictions());

        } catch(Exception e) {
            log.error("Failed to initialize environment from database", e);
        }
    }
}
