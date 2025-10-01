package com.integrixs.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Data transfer object for tenant information
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TenantDTO {

    private UUID id;

    @NotBlank(message = "Tenant name is required")
    @Size(min = 3, max = 100, message = "Tenant name must be between 3 and 100 characters")
    private String name;

    private String description;

    private boolean active = true;

    private LocalDateTime createdDate;

    private LocalDateTime modifiedDate;

    private UUID adminUserId;

    // For tenant creation
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotBlank(message = "Admin username is required")
    private String adminUsername;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotBlank(message = "Admin password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String adminPassword;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Admin email is required")
    private String adminEmail;

    private String adminFullName;

    // Tenant settings
    private Map<String, Object> settings;

    // Resource quotas
    private Map<String, Integer> quotas;

    // Statistics
    private Integer userCount;
    private Integer flowCount;
    private Integer adapterCount;
    private Long storageUsedBytes;
    private Long executionCount;

    // Billing information(optional)
    private String billingPlan;
    private String billingStatus;
    private LocalDateTime billingStartDate;
    private LocalDateTime billingEndDate;

    // Feature flags
    private Map<String, Boolean> features;

    // Getters and setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(LocalDateTime modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public UUID getAdminUserId() {
        return adminUserId;
    }

    public void setAdminUserId(UUID adminUserId) {
        this.adminUserId = adminUserId;
    }

    public String getAdminUsername() {
        return adminUsername;
    }

    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    public String getAdminEmail() {
        return adminEmail;
    }

    public void setAdminEmail(String adminEmail) {
        this.adminEmail = adminEmail;
    }

    public String getAdminFullName() {
        return adminFullName;
    }

    public void setAdminFullName(String adminFullName) {
        this.adminFullName = adminFullName;
    }

    public Map<String, Object> getSettings() {
        return settings;
    }

    public void setSettings(Map<String, Object> settings) {
        this.settings = settings;
    }

    public Map<String, Integer> getQuotas() {
        return quotas;
    }

    public void setQuotas(Map<String, Integer> quotas) {
        this.quotas = quotas;
    }

    public Integer getUserCount() {
        return userCount;
    }

    public void setUserCount(Integer userCount) {
        this.userCount = userCount;
    }

    public Integer getFlowCount() {
        return flowCount;
    }

    public void setFlowCount(Integer flowCount) {
        this.flowCount = flowCount;
    }

    public Integer getAdapterCount() {
        return adapterCount;
    }

    public void setAdapterCount(Integer adapterCount) {
        this.adapterCount = adapterCount;
    }

    public Long getStorageUsedBytes() {
        return storageUsedBytes;
    }

    public void setStorageUsedBytes(Long storageUsedBytes) {
        this.storageUsedBytes = storageUsedBytes;
    }

    public Long getExecutionCount() {
        return executionCount;
    }

    public void setExecutionCount(Long executionCount) {
        this.executionCount = executionCount;
    }

    public String getBillingPlan() {
        return billingPlan;
    }

    public void setBillingPlan(String billingPlan) {
        this.billingPlan = billingPlan;
    }

    public String getBillingStatus() {
        return billingStatus;
    }

    public void setBillingStatus(String billingStatus) {
        this.billingStatus = billingStatus;
    }

    public LocalDateTime getBillingStartDate() {
        return billingStartDate;
    }

    public void setBillingStartDate(LocalDateTime billingStartDate) {
        this.billingStartDate = billingStartDate;
    }

    public LocalDateTime getBillingEndDate() {
        return billingEndDate;
    }

    public void setBillingEndDate(LocalDateTime billingEndDate) {
        this.billingEndDate = billingEndDate;
    }

    public Map<String, Boolean> getFeatures() {
        return features;
    }

    public void setFeatures(Map<String, Boolean> features) {
        this.features = features;
    }
}
