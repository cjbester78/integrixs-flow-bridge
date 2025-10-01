package com.integrixs.backend.service;

import com.integrixs.backend.dto.TenantDTO;
import com.integrixs.backend.security.RequiresPermission;
import com.integrixs.backend.security.ResourcePermission;
import com.integrixs.backend.security.RoleDefinitions;
import com.integrixs.backend.security.TenantContext;
import com.integrixs.backend.audit.AuditService;
import com.integrixs.data.model.*;
import com.integrixs.data.sql.repository.UserSqlRepository;
import com.integrixs.data.sql.repository.IntegrationFlowSqlRepository;
import com.integrixs.data.sql.repository.CommunicationAdapterSqlRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing tenants and tenant resources
 */
@Service
public class TenantManagementService {

    private static final Logger logger = LoggerFactory.getLogger(TenantManagementService.class);

    @Autowired
    private UserSqlRepository userRepository;

    @Autowired
    private IntegrationFlowSqlRepository flowRepository;

    @Autowired
    private CommunicationAdapterSqlRepository adapterRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuditService auditService;

    /**
     * Create a new tenant
     */
    @RequiresPermission(ResourcePermission.TENANT_CREATE)
    public TenantDTO createTenant(TenantDTO tenantRequest) {
        logger.info("Creating new tenant: {}", tenantRequest.getName());

        try {
            UUID tenantId = UUID.randomUUID();

            // Create tenant admin user
            User adminUser = new User();
            adminUser.setUsername(tenantRequest.getAdminUsername());
            adminUser.setEmail(tenantRequest.getAdminEmail());
            adminUser.setPasswordHash(passwordEncoder.encode(tenantRequest.getAdminPassword()));
            adminUser.setFirstName(tenantRequest.getAdminFullName());
            adminUser.setTenantId(tenantId);
            adminUser.setStatus("active");
            adminUser.setCreatedAt(LocalDateTime.now());
            adminUser = userRepository.save(adminUser);

            // Assign tenant admin role directly to user
            adminUser.setRole(RoleDefinitions.TENANT_ADMIN.getName());

            // Store tenant information
            // In a real implementation, would have a Tenant entity
            Map<String, Object> tenantInfo = new HashMap<>();
            tenantInfo.put("id", tenantId);
            tenantInfo.put("name", tenantRequest.getName());
            tenantInfo.put("description", tenantRequest.getDescription());
            tenantInfo.put("adminUserId", adminUser.getId());
            tenantInfo.put("createdDate", LocalDateTime.now());
            tenantInfo.put("active", true);
            tenantInfo.put("settings", tenantRequest.getSettings());

            // Audit tenant creation
            auditService.logSecurityEvent(
                "TENANT_CREATED",
                adminUser.getId().toString(),
                "SUCCESS",
                "Tenant created: " + tenantRequest.getName(),
                tenantInfo
           );

            // Return created tenant info
            TenantDTO result = new TenantDTO();
            result.setId(tenantId);
            result.setName(tenantRequest.getName());
            result.setDescription(tenantRequest.getDescription());
            result.setAdminUserId(adminUser.getId());
            result.setActive(true);
            result.setCreatedDate(LocalDateTime.now());

            return result;

        } catch(Exception e) {
            logger.error("Failed to create tenant", e);
            throw new RuntimeException("Failed to create tenant: " + e.getMessage(), e);
        }
    }

    /**
     * Get tenant information
     */
    @RequiresPermission(ResourcePermission.TENANT_READ)
    public TenantDTO getTenant(UUID tenantId) {
        // In a real implementation, would fetch from Tenant entity
        // For now, construct from user data
        List<User> tenantUsers = userRepository.findByTenantId(tenantId);
        if(tenantUsers.isEmpty()) {
            throw new IllegalArgumentException("Tenant not found: " + tenantId);
        }

        // Find admin user
        User adminUser = tenantUsers.stream()
            .filter(u -> RoleDefinitions.TENANT_ADMIN.getName().equals(u.getRole()))
            .findFirst()
            .orElse(tenantUsers.get(0));

        TenantDTO tenant = new TenantDTO();
        tenant.setId(tenantId);
        tenant.setName("Tenant " + tenantId); // Would come from Tenant entity
        tenant.setAdminUserId(adminUser.getId());
        tenant.setActive(true);
        tenant.setUserCount(tenantUsers.size());

        // Get resource counts
        tenant.setFlowCount((int) flowRepository.countByTenantId(tenantId));
        tenant.setAdapterCount((int) adapterRepository.countByTenantId(tenantId));

        return tenant;
    }

    /**
     * Update tenant settings
     */
    @RequiresPermission(ResourcePermission.TENANT_UPDATE)
    public void updateTenantSettings(UUID tenantId, Map<String, Object> settings) {
        logger.info("Updating settings for tenant: {}", tenantId);

        // Validate tenant exists
        if(!tenantExists(tenantId)) {
            throw new IllegalArgumentException("Tenant not found: " + tenantId);
        }

        // In a real implementation, would update Tenant entity
        // For now, just audit the change
        auditService.logConfigChange(
            "TENANT",
            tenantId.toString(),
            null, // oldValue
            settings.toString() // newValue
       );
    }

    /**
     * Add user to tenant
     */
    @RequiresPermission(ResourcePermission.TENANT_MANAGE_USERS)
    public User addUserToTenant(UUID tenantId, User userRequest, String role) {
        logger.info("Adding user {} to tenant {}", userRequest.getUsername(), tenantId);

        // Validate tenant
        if(!tenantExists(tenantId)) {
            throw new IllegalArgumentException("Tenant not found: " + tenantId);
        }

        // Create user
        User user = new User();
        user.setUsername(userRequest.getUsername());
        user.setEmail(userRequest.getEmail());
        user.setPasswordHash(passwordEncoder.encode(userRequest.getPasswordHash()));
        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());
        user.setTenantId(tenantId);
        user.setStatus("active");
        user.setCreatedAt(LocalDateTime.now());
        user = userRepository.save(user);

        // Assign role directly to user
        user.setRole(role);
        userRepository.save(user);

        return user;
    }

    /**
     * Remove user from tenant
     */
    @RequiresPermission(ResourcePermission.TENANT_MANAGE_USERS)
    public void removeUserFromTenant(UUID tenantId, UUID userId) {
        logger.info("Removing user {} from tenant {}", userId, tenantId);

        Optional<User> userOpt = userRepository.findById(userId);
        if(!userOpt.isPresent()) {
            throw new IllegalArgumentException("User not found: " + userId);
        }

        User user = userOpt.get();
        if(!tenantId.equals(user.getTenantId())) {
            throw new IllegalArgumentException("User does not belong to tenant");
        }

        // Deactivate user instead of deleting
        user.setStatus("inactive");
        userRepository.save(user);
    }

    /**
     * List users in tenant
     */
    @RequiresPermission(ResourcePermission.TENANT_READ)
    public List<User> listTenantUsers(UUID tenantId) {
        return userRepository.findByTenantId(tenantId);
    }

    /**
     * Get tenant resource statistics
     */
    @RequiresPermission(ResourcePermission.TENANT_READ)
    public Map<String, Object> getTenantStatistics(UUID tenantId) {
        Map<String, Object> stats = new HashMap<>();

        stats.put("tenantId", tenantId);
        stats.put("userCount", userRepository.countByTenantId(tenantId));
        stats.put("flowCount", flowRepository.countByTenantId(tenantId));
        stats.put("adapterCount", adapterRepository.countByTenantId(tenantId));
        stats.put("activeFlows", flowRepository.countByTenantId(tenantId)); // TODO: Add active filter when method is available

        // Add more statistics as needed

        return stats;
    }

    /**
     * Set resource quotas for tenant
     */
    @RequiresPermission(ResourcePermission.ADMIN_TENANTS)
    public void setTenantQuotas(UUID tenantId, Map<String, Integer> quotas) {
        logger.info("Setting quotas for tenant: {}", tenantId);

        // In a real implementation, would store in Tenant entity
        // Quotas could include:
        // - maxUsers
        // - maxFlows
        // - maxAdapters
        // - maxExecutionsPerDay
        // - maxStorageGB

        auditService.logConfigChange(
            "TENANT_QUOTAS",
            tenantId.toString(),
            null, // oldValue
            quotas.toString() // newValue
       );
    }

    /**
     * Check if tenant exists
     */
    private boolean tenantExists(UUID tenantId) {
        // In a real implementation, would check Tenant entity
        return userRepository.countByTenantId(tenantId) > 0;
    }

    /**
     * Execute operation in tenant context
     */
    public <T> T executeInTenantContext(UUID tenantId, TenantContext.TenantOperation<T> operation) throws Exception {
        return TenantContext.executeWithTenant(tenantId, operation);
    }

    /**
     * Migrate resources between tenants
     */
    @RequiresPermission(ResourcePermission.ADMIN_TENANTS)
    public void migrateResources(UUID sourceTenantId, UUID targetTenantId,
                               Set<UUID> flowIds, Set<UUID> adapterIds) {
        logger.info("Migrating resources from tenant {} to tenant {}", sourceTenantId, targetTenantId);

        // Validate tenants
        if(!tenantExists(sourceTenantId) || !tenantExists(targetTenantId)) {
            throw new IllegalArgumentException("Source or target tenant not found");
        }

        // Migrate flows
        if(flowIds != null && !flowIds.isEmpty()) {
            List<IntegrationFlow> flows = flowRepository.findAllById(flowIds);
            flows.forEach(flow -> {
                if(flow.getTenantId().equals(sourceTenantId)) {
                    flow.setTenantId(targetTenantId);
                    flowRepository.save(flow);
                }
            });
        }

        // Migrate adapters
        if(adapterIds != null && !adapterIds.isEmpty()) {
            List<CommunicationAdapter> adapters = adapterRepository.findAllById(adapterIds);
            adapters.forEach(adapter -> {
                if(adapter.getTenantId().equals(sourceTenantId)) {
                    adapter.setTenantId(targetTenantId);
                    adapterRepository.save(adapter);
                }
            });
        }

        // Audit migration
        Map<String, Object> migrationDetails = new HashMap<>();
        migrationDetails.put("sourceTenant", sourceTenantId);
        migrationDetails.put("targetTenant", targetTenantId);
        migrationDetails.put("flowIds", flowIds);
        migrationDetails.put("adapterIds", adapterIds);

        auditService.logSecurityEvent(
            "RESOURCE_MIGRATION",
            "SYSTEM",
            "SUCCESS",
            "Resources migrated between tenants",
            migrationDetails
       );
    }
}
