package com.integrixs.backend.domain.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrixs.backend.security.CredentialEncryptionService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Domain service for handling adapter configuration encryption/decryption
 */
@Service
public class AdapterConfigurationService {

    private static final Logger log = LoggerFactory.getLogger(AdapterConfigurationService.class);


    private final CredentialEncryptionService encryptionService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AdapterConfigurationService(CredentialEncryptionService encryptionService) {
        this.encryptionService = encryptionService;
    }

    // Fields that contain sensitive data and should be encrypted
    private static final List<String> SENSITIVE_FIELDS = Arrays.asList(
        "password", "apiKey", "secret", "token", "privateKey",
        "clientSecret", "passphrase", "credential", "authToken"
   );

    /**
     * Encrypts sensitive fields in the configuration
     */
    public String encryptConfiguration(String configJson) {
        if(configJson == null || configJson.trim().isEmpty()) {
            return configJson;
        }

        try {
            Map<String, Object> config = objectMapper.readValue(configJson,
                new TypeReference<Map<String, Object>>() {});

            Map<String, Object> encryptedConfig = encryptSensitiveFields(config);

            return objectMapper.writeValueAsString(encryptedConfig);
        } catch(Exception e) {
            log.error("Error encrypting configuration", e);
            throw new RuntimeException("Failed to encrypt configuration", e);
        }
    }

    /**
     * Decrypts sensitive fields in the configuration
     */
    public String decryptConfiguration(String encryptedConfigJson) {
        if(encryptedConfigJson == null || encryptedConfigJson.trim().isEmpty()) {
            return encryptedConfigJson;
        }

        try {
            Map<String, Object> config = objectMapper.readValue(encryptedConfigJson,
                new TypeReference<Map<String, Object>>() {});

            Map<String, Object> decryptedConfig = decryptSensitiveFields(config);

            return objectMapper.writeValueAsString(decryptedConfig);
        } catch(Exception e) {
            log.error("Error decrypting configuration", e);
            throw new RuntimeException("Failed to decrypt configuration", e);
        }
    }

    /**
     * Parses configuration JSON to Map
     */
    public Map<String, Object> parseConfiguration(String configJson) {
        if(configJson == null || configJson.trim().isEmpty()) {
            return new HashMap<>();
        }

        try {
            return objectMapper.readValue(configJson,
                new TypeReference<Map<String, Object>>() {});
        } catch(Exception e) {
            log.error("Error parsing configuration", e);
            throw new RuntimeException("Invalid configuration format", e);
        }
    }

    /**
     * Converts configuration Map to JSON
     */
    public String serializeConfiguration(Map<String, Object> config) {
        if(config == null || config.isEmpty()) {
            return " {}";
        }

        try {
            return objectMapper.writeValueAsString(config);
        } catch(Exception e) {
            log.error("Error serializing configuration", e);
            throw new RuntimeException("Failed to serialize configuration", e);
        }
    }

    private Map<String, Object> encryptSensitiveFields(Map<String, Object> config) {
        Map<String, Object> result = new HashMap<>();

        for(Map.Entry<String, Object> entry : config.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if(value instanceof String && isSensitiveField(key)) {
                // Encrypt sensitive string values
                result.put(key, encryptionService.encrypt((String) value));
            } else if(value instanceof Map) {
                // Recursively handle nested maps
                @SuppressWarnings("unchecked")
                Map<String, Object> nestedMap = (Map<String, Object>) value;
                result.put(key, encryptSensitiveFields(nestedMap));
            } else {
                // Keep non - sensitive values as - is
                result.put(key, value);
            }
        }

        return result;
    }

    private Map<String, Object> decryptSensitiveFields(Map<String, Object> config) {
        Map<String, Object> result = new HashMap<>();

        for(Map.Entry<String, Object> entry : config.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if(value instanceof String && isSensitiveField(key)) {
                String strValue = (String) value;
                // Decrypt if it's an encrypted value
                if(strValue.startsWith("ENC:")) {
                    result.put(key, encryptionService.decrypt(strValue));
                } else {
                    result.put(key, strValue);
                }
            } else if(value instanceof Map) {
                // Recursively handle nested maps
                @SuppressWarnings("unchecked")
                Map<String, Object> nestedMap = (Map<String, Object>) value;
                result.put(key, decryptSensitiveFields(nestedMap));
            } else {
                // Keep non - sensitive values as - is
                result.put(key, value);
            }
        }

        return result;
    }

    private boolean isSensitiveField(String fieldName) {
        String lowerFieldName = fieldName.toLowerCase();
        return SENSITIVE_FIELDS.stream()
            .anyMatch(lowerFieldName::contains);
    }
}
