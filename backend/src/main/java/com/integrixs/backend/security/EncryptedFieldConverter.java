package com.integrixs.backend.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * JPA converter for automatic field encryption/decryption.
 * Apply to entity fields using @Convert annotation.
 */
@Component
@Converter
public class EncryptedFieldConverter implements AttributeConverter<String, String> {
    
    private static FieldEncryptionService encryptionService;
    
    @Autowired
    public void setEncryptionService(FieldEncryptionService service) {
        EncryptedFieldConverter.encryptionService = service;
    }
    
    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (encryptionService == null || attribute == null) {
            return attribute;
        }
        
        // Use field name from thread local or default
        String fieldName = EncryptionContext.getCurrentFieldName();
        if (fieldName == null) {
            fieldName = "default";
        }
        
        return encryptionService.encryptField(fieldName, attribute);
    }
    
    @Override
    public String convertToEntityAttribute(String dbData) {
        if (encryptionService == null || dbData == null) {
            return dbData;
        }
        
        // Use field name from thread local or default
        String fieldName = EncryptionContext.getCurrentFieldName();
        if (fieldName == null) {
            fieldName = "default";
        }
        
        return encryptionService.decryptField(fieldName, dbData);
    }
    
    /**
     * Thread-local context for field names during conversion.
     */
    public static class EncryptionContext {
        private static final ThreadLocal<String> fieldNameContext = new ThreadLocal<>();
        
        public static void setCurrentFieldName(String fieldName) {
            fieldNameContext.set(fieldName);
        }
        
        public static String getCurrentFieldName() {
            return fieldNameContext.get();
        }
        
        public static void clear() {
            fieldNameContext.remove();
        }
    }
}