package com.integrixs.backend.config;

/**
 * Thread-local storage for tenant context information
 * Used for multi-tenancy support across the application
 */
public class TenantContext {

    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_USER = new ThreadLocal<>();
    private static final ThreadLocal<String> CURRENT_SESSION = new ThreadLocal<>();

    /**
     * Set the current tenant ID
     */
    public static void setCurrentTenant(String tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    /**
     * Get the current tenant ID
     */
    public static String getCurrentTenant() {
        return CURRENT_TENANT.get();
    }

    /**
     * Set the current user ID
     */
    public static void setCurrentUser(String userId) {
        CURRENT_USER.set(userId);
    }

    /**
     * Get the current user ID
     */
    public static String getCurrentUser() {
        return CURRENT_USER.get();
    }

    /**
     * Set the current session ID
     */
    public static void setCurrentSession(String sessionId) {
        CURRENT_SESSION.set(sessionId);
    }

    /**
     * Get the current session ID
     */
    public static String getCurrentSession() {
        return CURRENT_SESSION.get();
    }

    /**
     * Clear all context information
     * Should be called at the end of request processing
     */
    public static void clear() {
        CURRENT_TENANT.remove();
        CURRENT_USER.remove();
        CURRENT_SESSION.remove();
    }

    /**
     * Check if tenant context is set
     */
    public static boolean hasTenant() {
        return CURRENT_TENANT.get() != null;
    }

    /**
     * Get tenant ID or default
     */
    public static String getCurrentTenantOrDefault(String defaultTenant) {
        String tenant = CURRENT_TENANT.get();
        return tenant != null ? tenant : defaultTenant;
    }
}