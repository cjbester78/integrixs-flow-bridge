package com.integrixs.backend.security;

import java.util.UUID;

/**
 * Thread - local tenant context for multi - tenancy support
 */
public class TenantContext {

    private static final ThreadLocal<UUID> currentTenant = new ThreadLocal<>();

    /**
     * Get current tenant ID
     */
    public static UUID getCurrentTenant() {
        return currentTenant.get();
    }

    /**
     * Set current tenant ID
     */
    public static void setCurrentTenant(UUID tenantId) {
        currentTenant.set(tenantId);
    }

    /**
     * Clear current tenant
     */
    public static void clear() {
        currentTenant.remove();
    }

    /**
     * Check if tenant context is set
     */
    public static boolean hasTenant() {
        return currentTenant.get() != null;
    }

    /**
     * Execute code within a tenant context
     */
    public static <T> T executeWithTenant(UUID tenantId, TenantOperation<T> operation) throws Exception {
        UUID previousTenant = getCurrentTenant();
        try {
            setCurrentTenant(tenantId);
            return operation.execute();
        } finally {
            if(previousTenant != null) {
                setCurrentTenant(previousTenant);
            } else {
                clear();
            }
        }
    }

    /**
     * Functional interface for tenant operations
     */
    @FunctionalInterface
    public interface TenantOperation<T> {
        T execute() throws Exception;
    }
}
