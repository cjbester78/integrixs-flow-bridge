package com.integrixs.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrixs.backend.security.AuditLogEncryptionService;
import com.integrixs.backend.security.FieldEncryptionService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for audit log encryption.
 * Enables encryption of sensitive data in audit trails and system logs.
 */
@Configuration
public class AuditEncryptionConfig {

    /**
     * Create AuditLogEncryptionService bean when encryption is enabled.
     */
    @Bean
    @ConditionalOnProperty(
        prefix = "audit.encryption",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = false
   )
    public AuditLogEncryptionService auditLogEncryptionService(
            FieldEncryptionService fieldEncryptionService,
            ObjectMapper objectMapper) {
        return new AuditLogEncryptionService(fieldEncryptionService, objectMapper);
    }
}
