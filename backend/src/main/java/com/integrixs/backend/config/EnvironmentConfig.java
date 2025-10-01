package com.integrixs.backend.config;

import com.integrixs.shared.enums.EnvironmentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * Configuration for system environment type.
 * Controls available functionality based on deployment environment.
 */
@Configuration
@ConfigurationProperties(prefix = "system.environment")
public class EnvironmentConfig {

    private static final Logger logger = LoggerFactory.getLogger(EnvironmentConfig.class);

    /**
     * The environment type of the system
     */
    private EnvironmentType type = EnvironmentType.DEVELOPMENT;

    /**
     * Whether to enforce environment restrictions
     */
    private boolean enforceRestrictions = true;

    /**
     * Custom message to display for restricted actions
     */
    private String restrictionMessage = "This action is not allowed in %s environment";

    @PostConstruct
    public void init() {
        logger.info("System Environment: " + type.getDisplayName());
        logger.info("Enforce Restrictions: " + enforceRestrictions);
    }

    /**
     * Check if the system is in development mode
     */
    public boolean isDevelopment() {
        return type == EnvironmentType.DEVELOPMENT;
    }

    /**
     * Check if the system is in QA mode
     */
    public boolean isQualityAssurance() {
        return type == EnvironmentType.QUALITY_ASSURANCE;
    }

    /**
     * Check if the system is in production mode
     */
    public boolean isProduction() {
        return type == EnvironmentType.PRODUCTION;
    }

    /**
     * Check if creating flows is allowed
     */
    public boolean canCreateFlows() {
        return !enforceRestrictions || isDevelopment();
    }

    /**
     * Check if creating adapters is allowed
     */
    public boolean canCreateAdapters() {
        return !enforceRestrictions || isDevelopment();
    }

    /**
     * Check if modifying adapter configuration is allowed
     */
    public boolean canModifyAdapterConfig() {
        return true; // Allowed in all environments
    }

    /**
     * Check if importing flows is allowed
     */
    public boolean canImportFlows() {
        return true; // Allowed in all environments
    }

    /**
     * Check if deploying flows is allowed
     */
    public boolean canDeployFlows() {
        return true; // Allowed in all environments
    }

    /**
     * Check if creating business components is allowed
     */
    public boolean canCreateBusinessComponents() {
        return !enforceRestrictions || isDevelopment();
    }

    /**
     * Check if creating message structures is allowed
     */
    public boolean canCreateMessageStructures() {
        return !enforceRestrictions || isDevelopment();
    }

    /**
     * Check if creating flow structures is allowed
     */
    public boolean canCreateFlowStructures() {
        return !enforceRestrictions || isDevelopment();
    }

    /**
     * Get formatted restriction message
     */
    public String getFormattedRestrictionMessage() {
        return String.format(restrictionMessage, type.getDisplayName());
    }

    // Getters and setters
    public EnvironmentType getType() {
        return type;
    }

    public void setType(EnvironmentType type) {
        this.type = type;
    }

    public boolean isEnforceRestrictions() {
        return enforceRestrictions;
    }

    public void setEnforceRestrictions(boolean enforceRestrictions) {
        this.enforceRestrictions = enforceRestrictions;
    }

    public String getRestrictionMessage() {
        return restrictionMessage;
    }

    public void setRestrictionMessage(String restrictionMessage) {
        this.restrictionMessage = restrictionMessage;
    }
}
