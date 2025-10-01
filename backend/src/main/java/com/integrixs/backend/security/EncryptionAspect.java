package com.integrixs.backend.security;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Aspect for automatic encryption/decryption of annotated fields.
 */
@Aspect
@Component
public class EncryptionAspect {

    private static final Logger log = LoggerFactory.getLogger(EncryptionAspect.class);

    private final FieldEncryptionService encryptionService;
    
    @Autowired
    private ApplicationContext applicationContext;

    public EncryptionAspect(FieldEncryptionService encryptionService) {
        this.encryptionService = encryptionService;
    }
    
    /**
     * Check if the application context is fully initialized.
     */
    private boolean isApplicationContextReady() {
        try {
            return applicationContext != null && applicationContext.getDisplayName() != null;
        } catch (Exception e) {
            log.debug("Application context not ready yet: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Intercept repository save methods to encrypt fields.
     */
    @Around("@within(org.springframework.stereotype.Repository) && execution(* save*(..))")
    public Object encryptOnSave(ProceedingJoinPoint joinPoint) throws Throwable {
        // Skip encryption during application startup
        if (!isApplicationContextReady()) {
            return joinPoint.proceed();
        }
        
        Object[] args = joinPoint.getArgs();

        // Encrypt fields in entities before saving
        for(Object arg : args) {
            if(arg != null) {
                encryptEntityFields(arg);
            }
        }

        return joinPoint.proceed(args);
    }

    /**
     * Intercept repository find methods to decrypt fields.
     */
    @Around("@within(org.springframework.stereotype.Repository) && execution(* find*(..))")
    public Object decryptOnFind(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();

        // Skip decryption during application startup
        if (!isApplicationContextReady()) {
            return result;
        }

        // Decrypt fields in returned entities
        if(result != null) {
            if(result instanceof Iterable) {
                for(Object entity : (Iterable<?>) result) {
                    decryptEntityFields(entity);
                }
            } else {
                decryptEntityFields(result);
            }
        }

        return result;
    }

    /**
     * Encrypt fields annotated with @EncryptedField.
     */
    private void encryptEntityFields(Object entity) {
        Class<?> clazz = entity.getClass();
        Field[] fields = clazz.getDeclaredFields();

        for(Field field : fields) {
            if(field.isAnnotationPresent(EncryptedField.class)) {
                try {
                    field.setAccessible(true);
                    Object value = field.get(entity);

                    if(value instanceof String) {
                        String encrypted = encryptionService.encryptField(
                            field.getName(), (String) value);
                        field.set(entity, encrypted);
                    }
                } catch(Exception e) {
                    log.error("Failed to encrypt field: {}", field.getName(), e);
                }
            }
        }
    }

    /**
     * Decrypt fields annotated with @EncryptedField.
     */
    private void decryptEntityFields(Object entity) {
        Class<?> clazz = entity.getClass();
        Field[] fields = clazz.getDeclaredFields();

        for(Field field : fields) {
            if(field.isAnnotationPresent(EncryptedField.class)) {
                try {
                    field.setAccessible(true);
                    Object value = field.get(entity);

                    if(value instanceof String) {
                        String decrypted = encryptionService.decryptField(
                            field.getName(), (String) value);
                        field.set(entity, decrypted);
                    }
                } catch(Exception e) {
                    log.error("Failed to decrypt field: {}", field.getName(), e);
                }
            }
        }
    }

    /**
     * Intercept methods annotated with @EncryptParameters.
     */
    @Around("@annotation(encryptParameters)")
    public Object encryptMethodParameters(ProceedingJoinPoint joinPoint,
                                        EncryptParameters encryptParameters) throws Throwable {
        Object[] args = joinPoint.getArgs();
        String[] fieldNames = encryptParameters.value();

        // Get method parameter names
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = signature.getParameterNames();

        // Encrypt specified parameters
        Map<Integer, String> originalValues = new HashMap<>();
        for(int i = 0; i < paramNames.length; i++) {
            for(String fieldName : fieldNames) {
                if(fieldName.equals(paramNames[i]) && args[i] instanceof String) {
                    originalValues.put(i, (String) args[i]);
                    args[i] = encryptionService.encryptField(fieldName, (String) args[i]);
                }
            }
        }

        try {
            return joinPoint.proceed(args);
        } finally {
            // Restore original values
            for(Map.Entry<Integer, String> entry : originalValues.entrySet()) {
                args[entry.getKey()] = entry.getValue();
            }
        }
    }

    /**
     * Intercept methods annotated with @DecryptResult.
     */
    @Around("@annotation(decryptResult)")
    public Object decryptMethodResult(ProceedingJoinPoint joinPoint,
                                    DecryptResult decryptResult) throws Throwable {
        Object result = joinPoint.proceed();

        if(result instanceof String) {
            String fieldName = decryptResult.fieldName();
            if(fieldName.isEmpty()) {
                Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
                fieldName = method.getName();
            }

            return encryptionService.decryptField(fieldName, (String) result);
        }

        return result;
    }
}
