package com.integrixs.backend.security;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Predefined role definitions for RBAC
 */
public class RoleDefinitions {

    /**
     * System administrator - full access
     */
    public static final Role SYSTEM_ADMIN = new Role(
        "SYSTEM_ADMIN",
        "System Administrator",
        new HashSet<>(Arrays.asList(ResourcePermission.values()))
   );

    /**
     * Tenant administrator - full access within tenant
     */
    public static final Role TENANT_ADMIN = new Role(
        "TENANT_ADMIN",
        "Tenant Administrator",
        new HashSet<>(Arrays.asList(
            ResourcePermission.FLOW_CREATE,
            ResourcePermission.FLOW_READ,
            ResourcePermission.FLOW_UPDATE,
            ResourcePermission.FLOW_DELETE,
            ResourcePermission.FLOW_EXECUTE,
            ResourcePermission.FLOW_DEPLOY,
            ResourcePermission.ADAPTER_CREATE,
            ResourcePermission.ADAPTER_READ,
            ResourcePermission.ADAPTER_UPDATE,
            ResourcePermission.ADAPTER_DELETE,
            ResourcePermission.ADAPTER_TEST,
            ResourcePermission.STRUCTURE_CREATE,
            ResourcePermission.STRUCTURE_READ,
            ResourcePermission.STRUCTURE_UPDATE,
            ResourcePermission.STRUCTURE_DELETE,
            ResourcePermission.TRANSFORMATION_CREATE,
            ResourcePermission.TRANSFORMATION_READ,
            ResourcePermission.TRANSFORMATION_UPDATE,
            ResourcePermission.TRANSFORMATION_DELETE,
            ResourcePermission.TRANSFORMATION_TEST,
            ResourcePermission.PACKAGE_CREATE,
            ResourcePermission.PACKAGE_READ,
            ResourcePermission.PACKAGE_UPDATE,
            ResourcePermission.PACKAGE_DELETE,
            ResourcePermission.MONITOR_FLOWS,
            ResourcePermission.MONITOR_METRICS,
            ResourcePermission.MONITOR_LOGS,
            ResourcePermission.MONITOR_HEALTH,
            ResourcePermission.TENANT_MANAGE_USERS
       ))
   );

    /**
     * Developer - can create and modify flows
     */
    public static final Role DEVELOPER = new Role(
        "DEVELOPER",
        "Developer",
        new HashSet<>(Arrays.asList(
            ResourcePermission.FLOW_CREATE,
            ResourcePermission.FLOW_READ,
            ResourcePermission.FLOW_UPDATE,
            ResourcePermission.FLOW_EXECUTE,
            ResourcePermission.FLOW_DEPLOY,
            ResourcePermission.ADAPTER_CREATE,
            ResourcePermission.ADAPTER_READ,
            ResourcePermission.ADAPTER_UPDATE,
            ResourcePermission.ADAPTER_TEST,
            ResourcePermission.STRUCTURE_CREATE,
            ResourcePermission.STRUCTURE_READ,
            ResourcePermission.STRUCTURE_UPDATE,
            ResourcePermission.TRANSFORMATION_CREATE,
            ResourcePermission.TRANSFORMATION_READ,
            ResourcePermission.TRANSFORMATION_UPDATE,
            ResourcePermission.TRANSFORMATION_TEST,
            ResourcePermission.PACKAGE_CREATE,
            ResourcePermission.PACKAGE_READ,
            ResourcePermission.PACKAGE_UPDATE,
            ResourcePermission.MONITOR_FLOWS,
            ResourcePermission.MONITOR_METRICS
       ))
   );

    /**
     * Operator - can execute and monitor flows
     */
    public static final Role OPERATOR = new Role(
        "OPERATOR",
        "Operator",
        new HashSet<>(Arrays.asList(
            ResourcePermission.FLOW_READ,
            ResourcePermission.FLOW_EXECUTE,
            ResourcePermission.ADAPTER_READ,
            ResourcePermission.STRUCTURE_READ,
            ResourcePermission.TRANSFORMATION_READ,
            ResourcePermission.PACKAGE_READ,
            ResourcePermission.MONITOR_FLOWS,
            ResourcePermission.MONITOR_METRICS,
            ResourcePermission.MONITOR_HEALTH
       ))
   );

    /**
     * Viewer - read - only access
     */
    public static final Role VIEWER = new Role(
        "VIEWER",
        "Viewer",
        new HashSet<>(Arrays.asList(
            ResourcePermission.FLOW_READ,
            ResourcePermission.ADAPTER_READ,
            ResourcePermission.STRUCTURE_READ,
            ResourcePermission.TRANSFORMATION_READ,
            ResourcePermission.PACKAGE_READ,
            ResourcePermission.MONITOR_HEALTH
       ))
   );

    /**
     * Guest - minimal access
     */
    public static final Role GUEST = new Role(
        "GUEST",
        "Guest",
        new HashSet<>(Arrays.asList(
            ResourcePermission.MONITOR_HEALTH
       ))
   );

    /**
     * Role definition class
     */
    public static class Role {
        private final String name;
        private final String description;
        private final Set<ResourcePermission> permissions;

        public Role(String name, String description, Set<ResourcePermission> permissions) {
            this.name = name;
            this.description = description;
            this.permissions = Collections.unmodifiableSet(permissions);
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public Set<ResourcePermission> getPermissions() {
            return permissions;
        }

        public boolean hasPermission(ResourcePermission permission) {
            return permissions.contains(permission);
        }

        public boolean hasPermission(String permission) {
            return permissions.stream()
                .anyMatch(p -> p.getPermission().equals(permission));
        }
    }

    /**
     * Get role by name
     */
    public static Role getRole(String roleName) {
        switch(roleName) {
            case "SYSTEM_ADMIN":
                return SYSTEM_ADMIN;
            case "TENANT_ADMIN":
                return TENANT_ADMIN;
            case "DEVELOPER":
                return DEVELOPER;
            case "OPERATOR":
                return OPERATOR;
            case "VIEWER":
                return VIEWER;
            case "GUEST":
                return GUEST;
            default:
                return null;
        }
    }

    /**
     * Get all predefined roles
     */
    public static Set<Role> getAllRoles() {
        return new HashSet<>(Arrays.asList(
            SYSTEM_ADMIN,
            TENANT_ADMIN,
            DEVELOPER,
            OPERATOR,
            VIEWER,
            GUEST
       ));
    }
}
