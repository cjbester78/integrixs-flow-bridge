package com.integrixs.backend.security;

/**
 * Resource permissions for RBAC
 */
public enum ResourcePermission {
    // Flow permissions
    FLOW_CREATE("flow:create", "Create integration flows"),
    FLOW_READ("flow:read", "Read integration flows"),
    FLOW_UPDATE("flow:update", "Update integration flows"),
    FLOW_DELETE("flow:delete", "Delete integration flows"),
    FLOW_EXECUTE("flow:execute", "Execute integration flows"),
    FLOW_DEPLOY("flow:deploy", "Deploy flows to process engine"),

    // Adapter permissions
    ADAPTER_CREATE("adapter:create", "Create adapters"),
    ADAPTER_READ("adapter:read", "Read adapter configurations"),
    ADAPTER_UPDATE("adapter:update", "Update adapter configurations"),
    ADAPTER_DELETE("adapter:delete", "Delete adapters"),
    ADAPTER_TEST("adapter:test", "Test adapter connections"),

    // Structure permissions
    STRUCTURE_CREATE("structure:create", "Create message structures"),
    STRUCTURE_READ("structure:read", "Read message structures"),
    STRUCTURE_UPDATE("structure:update", "Update message structures"),
    STRUCTURE_DELETE("structure:delete", "Delete message structures"),

    // Transformation permissions
    TRANSFORMATION_CREATE("transformation:create", "Create transformations"),
    TRANSFORMATION_READ("transformation:read", "Read transformations"),
    TRANSFORMATION_UPDATE("transformation:update", "Update transformations"),
    TRANSFORMATION_DELETE("transformation:delete", "Delete transformations"),
    TRANSFORMATION_TEST("transformation:test", "Test transformations"),

    // Package permissions
    PACKAGE_CREATE("package:create", "Create integration packages"),
    PACKAGE_READ("package:read", "Read package configurations"),
    PACKAGE_UPDATE("package:update", "Update packages"),
    PACKAGE_DELETE("package:delete", "Delete packages"),

    // Admin permissions
    ADMIN_USERS("admin:users", "Manage users"),
    ADMIN_ROLES("admin:roles", "Manage roles"),
    ADMIN_TENANTS("admin:tenants", "Manage tenants"),
    ADMIN_SYSTEM("admin:system", "System administration"),
    ADMIN_AUDIT("admin:audit", "Access audit logs"),

    // Monitoring permissions
    MONITOR_FLOWS("monitor:flows", "Monitor flow executions"),
    MONITOR_METRICS("monitor:metrics", "View system metrics"),
    MONITOR_LOGS("monitor:logs", "View system logs"),
    MONITOR_HEALTH("monitor:health", "View health status"),

    // Tenant permissions
    TENANT_CREATE("tenant:create", "Create tenants"),
    TENANT_READ("tenant:read", "Read tenant information"),
    TENANT_UPDATE("tenant:update", "Update tenant settings"),
    TENANT_DELETE("tenant:delete", "Delete tenants"),
    TENANT_MANAGE_USERS("tenant:manage-users", "Manage tenant users"),

    // Audit permissions
    VIEW_AUDIT_LOGS("audit:view", "View audit logs"),
    VIEW_SECURITY_LOGS("audit:view-security", "View security logs"),
    EXPORT_AUDIT_LOGS("audit:export", "Export audit logs"),
    GENERATE_REPORTS("audit:generate-reports", "Generate audit reports"),

    // Documentation permissions
    VIEW_DOCUMENTATION("doc:view", "View documentation");

    private final String permission;
    private final String description;

    ResourcePermission(String permission, String description) {
        this.permission = permission;
        this.description = description;
    }

    public String getPermission() {
        return permission;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Check if permission string matches
     */
    public boolean matches(String permission) {
        return this.permission.equals(permission);
    }

    /**
     * Get permission by string value
     */
    public static ResourcePermission fromString(String permission) {
        for(ResourcePermission rp : values()) {
            if(rp.permission.equals(permission)) {
                return rp;
            }
        }
        throw new IllegalArgumentException("Unknown permission: " + permission);
    }
}
