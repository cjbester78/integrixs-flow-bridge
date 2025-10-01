package com.integrixs.backend.security;

import com.integrixs.data.model.User;
import com.integrixs.data.sql.repository.UserSqlRepository;
import com.integrixs.backend.config.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for checking resource access permissions
 */
@Service
public class ResourceAccessService {

    private static final Logger logger = LoggerFactory.getLogger(ResourceAccessService.class);

    private final UserSqlRepository userRepository;

    @Autowired
    public ResourceAccessService(UserSqlRepository userRepository) {
        this.userRepository = userRepository;
    }


    /**
     * Check if current user has permission
     */
    public boolean hasPermission(ResourcePermission permission) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth == null || !auth.isAuthenticated()) {
            return false;
        }

        return hasPermission(auth.getName(), permission);
    }

    /**
     * Check if user has permission
     */
    @Cacheable(value = "userPermissions", key = "#username + ':' + #permission.permission")
    public boolean hasPermission(String username, ResourcePermission permission) {
        try {
            // Get user
            Optional<User> userOptional = userRepository.findByUsername(username);
            if(userOptional.isEmpty()) {
                return false;
            }
            User user = userOptional.get();

            // Check if user is active
            if(!user.isActive()) {
                return false;
            }

            // Check user role for permission
            RoleDefinitions.Role role = RoleDefinitions.getRole(user.getRole());
            if(role != null && role.hasPermission(permission)) {
                // Additional tenant check if needed
                if(requiresTenantCheck(permission)) {
                    return checkTenantAccess(user, user.getRole());
                }
                return true;
            }

            return false;

        } catch(Exception e) {
            logger.error("Error checking permission", e);
            return false;
        }
    }

    /**
     * Check if current user has any of the permissions
     */
    public boolean hasAnyPermission(ResourcePermission... permissions) {
        for(ResourcePermission permission : permissions) {
            if(hasPermission(permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if current user has all permissions
     */
    public boolean hasAllPermissions(ResourcePermission... permissions) {
        for(ResourcePermission permission : permissions) {
            if(!hasPermission(permission)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get all permissions for current user
     */
    public Set<ResourcePermission> getCurrentUserPermissions() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth == null || !auth.isAuthenticated()) {
            return Collections.emptySet();
        }

        return getUserPermissions(auth.getName());
    }

    /**
     * Get all permissions for a user
     */
    @Cacheable(value = "allUserPermissions", key = "#username")
    public Set<ResourcePermission> getUserPermissions(String username) {
        Set<ResourcePermission> permissions = new HashSet<>();

        try {
            // Get user
            Optional<User> userOptional = userRepository.findByUsername(username);
            if(userOptional.isEmpty()) {
                return permissions;
            }
            User user = userOptional.get();

            // Get permissions from user's role
            RoleDefinitions.Role role = RoleDefinitions.getRole(user.getRole());
            if(role != null) {
                permissions.addAll(role.getPermissions());
            }

        } catch(Exception e) {
            logger.error("Error getting user permissions", e);
        }

        return permissions;
    }

    /**
     * Check if user can access resource in specific tenant
     */
    public boolean canAccessResourceInTenant(UUID resourceId, UUID tenantId, ResourcePermission permission) {
        if(!hasPermission(permission)) {
            return false;
        }

        // System admins can access all tenants
        if(hasPermission(ResourcePermission.ADMIN_SYSTEM)) {
            return true;
        }

        // Check if user belongs to the tenant
        return isUserInTenant(tenantId);
    }

    /**
     * Check if current user belongs to tenant
     */
    public boolean isUserInTenant(UUID tenantId) {
        if(tenantId == null) {
            return true; // No tenant restriction
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth == null || !auth.isAuthenticated()) {
            return false;
        }

        Optional<User> userOptional = userRepository.findByUsername(auth.getName());
        if(userOptional.isEmpty()) {
            return false;
        }
        User user = userOptional.get();

        // Check if user's tenant matches
        // TODO: Add tenant support to User entity
        // if(user.getTenantId() != null && user.getTenantId().equals(tenantId)) {
        //     return true;
        // }

        // Check if user has cross - tenant access(system admin)
        return hasPermission(ResourcePermission.ADMIN_SYSTEM);
    }

    /**
     * Get accessible tenants for current user
     */
    public Set<UUID> getAccessibleTenants() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth == null || !auth.isAuthenticated()) {
            return Collections.emptySet();
        }

        // System admins can access all tenants
        if(hasPermission(ResourcePermission.ADMIN_SYSTEM)) {
            return null; // Null means all tenants
        }

        // Regular users can only access their own tenant
        Optional<User> userOptional = userRepository.findByUsername(auth.getName());
        if(userOptional.isPresent()) {
            User user = userOptional.get();
            if(user.getTenantId() != null) {
                return Collections.singleton(user.getTenantId());
            }
        }

        return Collections.emptySet();
    }

    /**
     * Filter resources by tenant access
     */
    public <T> List<T> filterByTenantAccess(List<T> resources, java.util.function.Function<T, UUID> tenantIdExtractor) {
        Set<UUID> accessibleTenants = getAccessibleTenants();

        // Null means all tenants are accessible
        if(accessibleTenants == null) {
            return resources;
        }

        // Filter by accessible tenants
        return resources.stream()
            .filter(resource -> {
                UUID tenantId = tenantIdExtractor.apply(resource);
                return tenantId == null || accessibleTenants.contains(tenantId);
            })
            .collect(Collectors.toList());
    }

    /**
     * Check if permission requires tenant check
     */
    private boolean requiresTenantCheck(ResourcePermission permission) {
        // Admin permissions don't require tenant check
        return !permission.getPermission().startsWith("admin:");
    }

    /**
     * Check tenant access for user
     */
    private boolean checkTenantAccess(User user, String userRole) {
        String currentTenantStr = TenantContext.getCurrentTenant();
        UUID currentTenant = currentTenantStr != null ? UUID.fromString(currentTenantStr) : null;

        // No tenant context means no restriction
        if(currentTenant == null) {
            return true;
        }

        // Check if user belongs to current tenant
        if(user.getTenantId() != null && user.getTenantId().equals(currentTenant)) {
            return true;
        }

        // System admins have cross - tenant access
        return "SYSTEM_ADMIN".equals(userRole);
    }
}
