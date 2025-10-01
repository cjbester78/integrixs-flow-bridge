package com.integrixs.backend.security;

import java.lang.annotation.*;

/**
 * Annotation for method - level permission checks
 */
@Target( {ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresPermission {

    /**
     * Required permission(s)
     */
    ResourcePermission[] value();

    /**
     * Logical operation for multiple permissions
     */
    LogicalOperation operation() default LogicalOperation.ANY;

    /**
     * Whether to check tenant access
     */
    boolean checkTenant() default true;

    /**
     * Custom error message
     */
    String message() default "Access denied";

    /**
     * Logical operation for permissions
     */
    enum LogicalOperation {
        ANY, // User needs at least one permission
        ALL   // User needs all permissions
    }
}
