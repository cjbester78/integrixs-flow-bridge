package com.integrixs.backend.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Aspect for automatic audit logging
 */
@Aspect
@Component
@Order(1)
public class AuditAspect {
    
    private static final Logger logger = LoggerFactory.getLogger(AuditAspect.class);
    
    @Autowired
    private AuditService auditService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Value("${audit.aspect.log-parameters:true}")
    private boolean logParameters;
    
    @Value("${audit.aspect.log-result:false}")
    private boolean logResult;
    
    @Value("${audit.aspect.max-parameter-length:1000}")
    private int maxParameterLength;
    
    /**
     * Audit annotation
     */
    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    public @interface Audited {
        AuditEvent.AuditEventType eventType();
        String action() default "";
        boolean logParameters() default true;
        boolean logResult() default false;
        String[] sensitiveParameters() default {};
    }
    
    /**
     * Intercept methods annotated with @Audited
     */
    @Around("@annotation(audited)")
    public Object auditMethod(ProceedingJoinPoint joinPoint, Audited audited) throws Throwable {
        Instant startTime = Instant.now();
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = method.getName();
        String action = audited.action().isEmpty() ? 
            String.format("%s.%s", className, methodName) : audited.action();
        
        Map<String, String> details = new HashMap<>();
        AuditEvent.AuditOutcome outcome = AuditEvent.AuditOutcome.SUCCESS;
        String errorMessage = null;
        Object result = null;
        
        try {
            // Log parameters if enabled
            if (audited.logParameters() && logParameters) {
                captureParameters(joinPoint, method, audited.sensitiveParameters(), details);
            }
            
            // Execute method
            result = joinPoint.proceed();
            
            // Log result if enabled
            if (audited.logResult() && logResult) {
                captureResult(result, details);
            }
            
            return result;
            
        } catch (Exception e) {
            outcome = AuditEvent.AuditOutcome.ERROR;
            errorMessage = e.getMessage();
            logger.error("Error in audited method: {}", action, e);
            throw e;
            
        } finally {
            // Calculate duration
            Duration duration = Duration.between(startTime, Instant.now());
            
            // Create and save audit event
            AuditEvent event = AuditEvent.baseBuilder(audited.eventType())
                .action(action)
                .outcome(outcome)
                .durationMs(duration.toMillis())
                .errorMessage(errorMessage)
                .details(details)
                .build();
            
            // Extract entity information if available
            extractEntityInfo(joinPoint, event);
            
            // Save audit event
            try {
                auditService.enrichWithSecurityContext(event);
                auditService.enrichWithRequestContext(event);
                auditService.save(event);
            } catch (Exception e) {
                logger.error("Failed to save audit event", e);
            }
        }
    }
    
    /**
     * Capture method parameters
     */
    private void captureParameters(ProceedingJoinPoint joinPoint, Method method, 
                                 String[] sensitiveParams, Map<String, String> details) {
        try {
            Object[] args = joinPoint.getArgs();
            String[] paramNames = getParameterNames(method);
            
            for (int i = 0; i < args.length && i < paramNames.length; i++) {
                String paramName = paramNames[i];
                
                // Skip sensitive parameters
                if (Arrays.asList(sensitiveParams).contains(paramName)) {
                    details.put("param_" + paramName, "[REDACTED]");
                    continue;
                }
                
                // Convert parameter to string
                String paramValue = convertToString(args[i]);
                
                // Truncate if too long
                if (paramValue.length() > maxParameterLength) {
                    paramValue = paramValue.substring(0, maxParameterLength) + "...";
                }
                
                details.put("param_" + paramName, paramValue);
            }
        } catch (Exception e) {
            logger.debug("Failed to capture parameters", e);
        }
    }
    
    /**
     * Capture method result
     */
    private void captureResult(Object result, Map<String, String> details) {
        try {
            if (result != null) {
                String resultString = convertToString(result);
                
                // Truncate if too long
                if (resultString.length() > maxParameterLength) {
                    resultString = resultString.substring(0, maxParameterLength) + "...";
                }
                
                details.put("result", resultString);
                details.put("resultType", result.getClass().getSimpleName());
            }
        } catch (Exception e) {
            logger.debug("Failed to capture result", e);
        }
    }
    
    /**
     * Extract entity information from method parameters
     */
    private void extractEntityInfo(ProceedingJoinPoint joinPoint, AuditEvent event) {
        try {
            Object[] args = joinPoint.getArgs();
            Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
            Annotation[][] paramAnnotations = method.getParameterAnnotations();
            
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                if (arg == null) continue;
                
                // Check for @EntityId annotation
                for (Annotation annotation : paramAnnotations[i]) {
                    if (annotation instanceof EntityId) {
                        EntityId entityId = (EntityId) annotation;
                        event.setEntityType(entityId.type());
                        event.setEntityId(arg.toString());
                    }
                }
                
                // Check for common entity types
                String className = arg.getClass().getSimpleName();
                if (className.endsWith("DTO") || className.endsWith("Entity")) {
                    // Try to extract ID using reflection
                    try {
                        Method getId = arg.getClass().getMethod("getId");
                        Object id = getId.invoke(arg);
                        if (id != null && event.getEntityId() == null) {
                            event.setEntityType(className);
                            event.setEntityId(id.toString());
                        }
                    } catch (Exception ignored) {
                        // No getId method
                    }
                    
                    // Try to extract name
                    try {
                        Method getName = arg.getClass().getMethod("getName");
                        Object name = getName.invoke(arg);
                        if (name != null && event.getEntityName() == null) {
                            event.setEntityName(name.toString());
                        }
                    } catch (Exception ignored) {
                        // No getName method
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("Failed to extract entity info", e);
        }
    }
    
    /**
     * Get parameter names from method
     */
    private String[] getParameterNames(Method method) {
        java.lang.reflect.Parameter[] parameters = method.getParameters();
        return Arrays.stream(parameters)
            .map(java.lang.reflect.Parameter::getName)
            .toArray(String[]::new);
    }
    
    /**
     * Convert object to string representation
     */
    private String convertToString(Object obj) {
        if (obj == null) {
            return "null";
        }
        
        // Handle common types
        if (obj instanceof String || obj instanceof Number || obj instanceof Boolean) {
            return obj.toString();
        }
        
        // Handle arrays
        if (obj.getClass().isArray()) {
            return Arrays.toString((Object[]) obj);
        }
        
        // Handle collections
        if (obj instanceof java.util.Collection) {
            return ((java.util.Collection<?>) obj).stream()
                .map(Object::toString)
                .collect(Collectors.joining(", ", "[", "]"));
        }
        
        // Convert to JSON for complex objects
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return obj.getClass().getSimpleName() + "@" + obj.hashCode();
        }
    }
    
    /**
     * Annotation to mark entity ID parameters
     */
    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface EntityId {
        String type();
    }
}