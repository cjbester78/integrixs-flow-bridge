package com.integrixs.backend.security;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Aspect for enforcing permission checks
 */
@Aspect
@Component
public class PermissionAspect {

    private static final Logger logger = LoggerFactory.getLogger(PermissionAspect.class);

    @Autowired
    private ResourceAccessService resourceAccessService;

    @Around("@annotation(com.integrixs.backend.security.RequiresPermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        RequiresPermission annotation = method.getAnnotation(RequiresPermission.class);
        if(annotation == null) {
            return joinPoint.proceed();
        }

        // Check permissions
        boolean hasAccess = false;
        ResourcePermission[] requiredPermissions = annotation.value();

        if(annotation.operation() == RequiresPermission.LogicalOperation.ALL) {
            // User needs all permissions
            hasAccess = resourceAccessService.hasAllPermissions(requiredPermissions);
        } else {
            // User needs at least one permission
            hasAccess = resourceAccessService.hasAnyPermission(requiredPermissions);
        }

        if(!hasAccess) {
            logger.warn("Access denied to method {} for user. Required permissions: {}",
                method.getName(), requiredPermissions);
            throw new AccessDeniedException(annotation.message());
        }

        // Check tenant access if required
        if(annotation.checkTenant() && TenantContext.hasTenant()) {
            if(!resourceAccessService.isUserInTenant(TenantContext.getCurrentTenant())) {
                logger.warn("Tenant access denied to method {} for user", method.getName());
                throw new AccessDeniedException("Access denied to tenant resources");
            }
        }

        return joinPoint.proceed();
    }

    @Around("@within(com.integrixs.backend.security.RequiresPermission)")
    public Object checkClassPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        Class<?> targetClass = joinPoint.getTarget().getClass();

        RequiresPermission annotation = targetClass.getAnnotation(RequiresPermission.class);
        if(annotation == null) {
            return joinPoint.proceed();
        }

        // Check permissions
        boolean hasAccess = false;
        ResourcePermission[] requiredPermissions = annotation.value();

        if(annotation.operation() == RequiresPermission.LogicalOperation.ALL) {
            hasAccess = resourceAccessService.hasAllPermissions(requiredPermissions);
        } else {
            hasAccess = resourceAccessService.hasAnyPermission(requiredPermissions);
        }

        if(!hasAccess) {
            logger.warn("Access denied to class {} for user. Required permissions: {}",
                targetClass.getName(), requiredPermissions);
            throw new AccessDeniedException(annotation.message());
        }

        return joinPoint.proceed();
    }
}
