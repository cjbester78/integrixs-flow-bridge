package com.integrixs.data.model;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing SystemSetting.
 * This maps to the corresponding table in the database for system - wide configuration.
 */
public class SystemSetting {

        /** Unique identifier(UUID) for the entity */
    private UUID id;

    /** Setting key/name(e.g., 'base_domain', 'default_timeout') */
    private String settingKey;

    /** Setting value */
    private String settingValue;

    /** Description of what this setting controls */
    private String description;

    /** Category for grouping settings(e.g., 'network', 'security', 'integration') */
    private String category;

    /** Data type of the setting value(STRING, INTEGER, BOOLEAN, URL, etc.) */
    private String dataType = "STRING";

    /** Whether the setting value is encrypted */
    private boolean isEncrypted = false;

    /** Whether this setting can be modified via UI */
    private boolean isReadonly = false;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String createdBy;

    private String updatedBy;

    // === Constructors ===

    public SystemSetting() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public SystemSetting(String settingKey, String settingValue, String description, String category) {
        this();
        this.settingKey = settingKey;
        this.settingValue = settingValue;
        this.description = description;
        this.category = category;
    }

    // === Getters and Setters ===

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getSettingKey() {
        return settingKey;
    }

    public void setSettingKey(String settingKey) {
        this.settingKey = settingKey;
    }

    public String getSettingValue() {
        return settingValue;
    }

    public void setSettingValue(String settingValue) {
        this.settingValue = settingValue;
        this.updatedAt = LocalDateTime.now();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public boolean isEncrypted() {
        return isEncrypted;
    }

    public void setEncrypted(boolean encrypted) {
        isEncrypted = encrypted;
    }

    public boolean isReadonly() {
        return isReadonly;
    }

    public void setReadonly(boolean readonly) {
        isReadonly = readonly;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    @Override
    public String toString() {
        return "SystemSetting {" +
                "id = '" + id + '\'' +
                ", settingKey = '" + settingKey + '\'' +
                ", settingValue = '" + (isEncrypted ? "[ENCRYPTED]" : settingValue) + '\'' +
                ", description = '" + description + '\'' +
                ", category = '" + category + '\'' +
                ", dataType = '" + dataType + '\'' +
                '}';
    }
}
