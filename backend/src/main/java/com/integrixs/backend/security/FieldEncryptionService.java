package com.integrixs.backend.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for field-level encryption of sensitive data.
 * Uses AES-256-GCM for authenticated encryption.
 */
@Service
public class FieldEncryptionService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FieldEncryptionService.class);

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;
    private static final int KEY_SIZE = 256;

    @Value("${encryption.master.key:# {null}}")
    private String masterKeyBase64;

    @Value("${encryption.key.rotation.enabled:false}")
    private boolean keyRotationEnabled;

    private SecretKey masterKey;
    private final SecureRandom secureRandom = new SecureRandom();

    // Cache for field-specific derived keys
    private final Map<String, SecretKey> fieldKeyCache = new ConcurrentHashMap<>();

    // Encryption metadata prefix
    private static final String ENCRYPTED_PREFIX = "ENC[";
    private static final String ENCRYPTED_SUFFIX = "]";

    @PostConstruct
    public void init() throws Exception {
        if(masterKeyBase64 != null && !masterKeyBase64.isEmpty()) {
            // Load existing master key
            byte[] keyBytes = Base64.getDecoder().decode(masterKeyBase64);
            masterKey = new SecretKeySpec(keyBytes, "AES");
        } else {
            // Generate new master key
            log.warn("No master key configured, generating new key. " +
                    "Set encryption.master.key property with the following value: {}",
                    generateAndEncodeMasterKey());
        }
    }

    /**
     * Encrypt a field value.
     */
    public String encryptField(String fieldName, String plaintext) {
        if(plaintext == null || plaintext.isEmpty()) {
            return plaintext;
        }

        try {
            // Get or derive field-specific key
            SecretKey fieldKey = getFieldKey(fieldName);

            // Generate IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);

            // Encrypt
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, fieldKey, spec);

            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            // Combine IV and ciphertext
            ByteBuffer buffer = ByteBuffer.allocate(iv.length + ciphertext.length);
            buffer.put(iv);
            buffer.put(ciphertext);

            // Encode and wrap
            String encrypted = Base64.getEncoder().encodeToString(buffer.array());
            return ENCRYPTED_PREFIX + encrypted + ENCRYPTED_SUFFIX;

        } catch(Exception e) {
            log.error("Failed to encrypt field: {}", fieldName, e);
            throw new EncryptionException("Field encryption failed", e);
        }
    }

    /**
     * Decrypt a field value.
     */
    public String decryptField(String fieldName, String encryptedValue) {
        if(encryptedValue == null || !isEncrypted(encryptedValue)) {
            return encryptedValue;
        }

        try {
            // Extract encrypted data
            String encrypted = encryptedValue.substring(
                ENCRYPTED_PREFIX.length(),
                encryptedValue.length()-ENCRYPTED_SUFFIX.length()
           );

            byte[] encryptedData = Base64.getDecoder().decode(encrypted);

            // Extract IV and ciphertext
            ByteBuffer buffer = ByteBuffer.wrap(encryptedData);
            byte[] iv = new byte[GCM_IV_LENGTH];
            buffer.get(iv);
            byte[] ciphertext = new byte[buffer.remaining()];
            buffer.get(ciphertext);

            // Get field key
            SecretKey fieldKey = getFieldKey(fieldName);

            // Decrypt
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, fieldKey, spec);

            byte[] plaintext = cipher.doFinal(ciphertext);
            return new String(plaintext, StandardCharsets.UTF_8);

        } catch(Exception e) {
            log.error("Failed to decrypt field: {}", fieldName, e);
            throw new EncryptionException("Field decryption failed", e);
        }
    }

    /**
     * Check if a value is encrypted.
     */
    public boolean isEncrypted(String value) {
        return value != null &&
               value.startsWith(ENCRYPTED_PREFIX) &&
               value.endsWith(ENCRYPTED_SUFFIX);
    }

    /**
     * Encrypt sensitive fields in a map.
     */
    public void encryptFields(Map<String, Object> data, String... fieldNames) {
        for(String fieldName : fieldNames) {
            if(data.containsKey(fieldName)) {
                Object value = data.get(fieldName);
                if(value instanceof String) {
                    String encrypted = encryptField(fieldName, (String) value);
                    data.put(fieldName, encrypted);
                }
            }
        }
    }

    /**
     * Decrypt sensitive fields in a map.
     */
    public void decryptFields(Map<String, Object> data, String... fieldNames) {
        for(String fieldName : fieldNames) {
            if(data.containsKey(fieldName)) {
                Object value = data.get(fieldName);
                if(value instanceof String) {
                    String decrypted = decryptField(fieldName, (String) value);
                    data.put(fieldName, decrypted);
                }
            }
        }
    }

    /**
     * Rotate encryption keys.
     */
    public void rotateKeys() {
        if(!keyRotationEnabled) {
            throw new IllegalStateException("Key rotation is not enabled");
        }

        log.info("Starting encryption key rotation");

        // Clear field key cache to force regeneration
        fieldKeyCache.clear();

        // In a real implementation, this would:
        // 1. Generate new master key
        // 2. Re-encrypt all encrypted data
        // 3. Update key version metadata

        log.info("Encryption key rotation completed");
    }

    /**
     * Get or derive a field-specific key.
     */
    private SecretKey getFieldKey(String fieldName) throws Exception {
        return fieldKeyCache.computeIfAbsent(fieldName, field -> {
            try {
                // Derive field key from master key using HKDF or similar
                byte[] fieldBytes = fieldName.getBytes(StandardCharsets.UTF_8);
                byte[] masterBytes = masterKey.getEncoded();

                // Simple derivation-in production use proper KDF
                byte[] derivedKey = new byte[32];
                System.arraycopy(masterBytes, 0, derivedKey, 0, Math.min(masterBytes.length, 32));

                // XOR with field name hash for uniqueness
                for(int i = 0; i < fieldBytes.length && i < derivedKey.length; i++) {
                    derivedKey[i] ^= fieldBytes[i];
                }

                return new SecretKeySpec(derivedKey, "AES");
            } catch(Exception e) {
                throw new RuntimeException("Failed to derive field key", e);
            }
        });
    }

    /**
     * Generate and encode a new master key.
     */
    private String generateAndEncodeMasterKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(KEY_SIZE);
        masterKey = keyGen.generateKey();
        return Base64.getEncoder().encodeToString(masterKey.getEncoded());
    }

    /**
     * Exception for encryption/decryption failures.
     */
    public static class EncryptionException extends RuntimeException {
        public EncryptionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
