package com.integrixs.backend.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.integrixs.backend.service.AuditTrailService;
import com.integrixs.data.model.AuditTrail;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.annotation.PostConstruct;
import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.Map;

/**
 * Service for encrypting and decrypting sensitive credentials
 * Uses AES-256-GCM encryption with PBKDF2 key derivation
 */
@Service
public class CredentialEncryptionService implements com.integrixs.shared.services.CredentialEncryptionService {

    private static final Logger log = LoggerFactory.getLogger(CredentialEncryptionService.class);
    
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BIT = 128;
    private static final int IV_LENGTH_BYTE = 12;
    private static final int SALT_LENGTH_BYTE = 16;
    private static final int ITERATION_COUNT = 65536;
    private static final int KEY_LENGTH = 256;

    @Value("${integrix.security.master-key:#{null}}")
    private String masterKey;

    @Value("${integrix.security.encryption.enabled:true}")
    private boolean encryptionEnabled;

    private SecretKey secretKey;

    @PostConstruct
    public void init() throws Exception {
        if(encryptionEnabled) {
            if(masterKey == null || masterKey.isEmpty()) {
                throw new IllegalStateException(
                    "Master key not configured. Please set integrix.security.master-key in application properties"
               );
            }

            // Derive key from master key using PBKDF2
            byte[] salt = "IntegrixFlowBridge".getBytes(); // Fixed salt for master key
            this.secretKey = deriveKey(masterKey, salt);
        }
    }

    /**
     * Encrypt a credential value
     * @param plaintext The credential to encrypt
     * @return Base64 encoded encrypted value with salt and IV
     */
    @Override
    public String encrypt(String plaintext) {
        if(!encryptionEnabled || plaintext == null) {
            return plaintext;
        }

        try {
            // Generate random IV
            byte[] iv = new byte[IV_LENGTH_BYTE];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);

            // Generate random salt for this encryption
            byte[] salt = new byte[SALT_LENGTH_BYTE];
            random.nextBytes(salt);

            // Derive key with salt
            SecretKey key = deriveKey(masterKey, salt);

            // Encrypt
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);

            byte[] cipherText = cipher.doFinal(plaintext.getBytes("UTF-8"));

            // Combine salt + iv + ciphertext
            byte[] combined = new byte[salt.length + iv.length + cipherText.length];
            System.arraycopy(salt, 0, combined, 0, salt.length);
            System.arraycopy(iv, 0, combined, salt.length, iv.length);
            System.arraycopy(cipherText, 0, combined, salt.length + iv.length, cipherText.length);

            // Return base64 encoded
            return "ENC:" + Base64.getEncoder().encodeToString(combined);

        } catch(Exception e) {
            throw new RuntimeException("Failed to encrypt credential", e);
        }
    }

    /**
     * Decrypt a credential value
     * @param encryptedText The encrypted credential(base64 encoded)
     * @return The decrypted plaintext
     */
    @Override
    public String decrypt(String encryptedText) {
        if(!encryptionEnabled || encryptedText == null) {
            return encryptedText;
        }

        // Check if it's encrypted(starts with ENC:)
        if(!encryptedText.startsWith("ENC:")) {
            return encryptedText; // Return as-is if not encrypted
        }

        try {
            // Remove prefix and decode
            String base64 = encryptedText.substring(4);
            byte[] combined = Base64.getDecoder().decode(base64);

            // Extract salt, IV and ciphertext
            byte[] salt = new byte[SALT_LENGTH_BYTE];
            byte[] iv = new byte[IV_LENGTH_BYTE];
            byte[] cipherText = new byte[combined.length-SALT_LENGTH_BYTE-IV_LENGTH_BYTE];

            System.arraycopy(combined, 0, salt, 0, salt.length);
            System.arraycopy(combined, salt.length, iv, 0, iv.length);
            System.arraycopy(combined, salt.length + iv.length, cipherText, 0, cipherText.length);

            // Derive key with salt
            SecretKey key = deriveKey(masterKey, salt);

            // Decrypt
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);

            byte[] plainText = cipher.doFinal(cipherText);

            return new String(plainText, "UTF-8");

        } catch(Exception e) {
            throw new RuntimeException("Failed to decrypt credential", e);
        }
    }

    /**
     * Check if a value is encrypted
     */
    @Override
    public boolean isEncrypted(String value) {
        return value != null && value.startsWith("ENC:");
    }

    /**
     * Encrypt credential data with a specific key alias
     * For this implementation, we ignore the keyAlias and use the master key
     */
    @Override
    public String encrypt(String plainText, String keyAlias) {
        // For simplicity, ignore keyAlias and use standard encryption
        return encrypt(plainText);
    }

    /**
     * Decrypt credential data with a specific key alias
     * For this implementation, we ignore the keyAlias and use the master key
     */
    @Override
    public String decrypt(String encryptedText, String keyAlias) {
        // For simplicity, ignore keyAlias and use standard decryption
        return decrypt(encryptedText);
    }

    /**
     * Derive key from password using PBKDF2
     */
    private SecretKey deriveKey(String password, byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);
        SecretKey tmp = factory.generateSecret(spec);

        return new SecretKeySpec(tmp.getEncoded(), "AES");
    }

    /**
     * Generate a secure random master key
     */
    public static String generateMasterKey() {
        SecureRandom random = new SecureRandom();
        byte[] key = new byte[32]; // 256 bits
        random.nextBytes(key);
        return Base64.getEncoder().encodeToString(key);
    }

    /**
     * Encrypt adapter configuration passwords
     */
    public void encryptAdapterConfig(Map<String, Object> config) {
        // List of fields that contain passwords/sensitive data
        String[] sensitiveFields = {
            "password", "apiKey", "secret", "token", "privateKey",
            "passphrase", "clientSecret", "accessToken", "refreshToken",
            "bearerToken", "basicPassword", "sslKeyStorePassword",
            "sslTrustStorePassword", "jwtToken", "oauthAccessToken"
        };

        for(String field : sensitiveFields) {
            if(config.containsKey(field)) {
                Object value = config.get(field);
                if(value instanceof String && !isEncrypted((String) value)) {
                    config.put(field, encrypt((String) value));
                }
            }
        }
    }

    /**
     * Decrypt adapter configuration passwords
     */
    public void decryptAdapterConfig(Map<String, Object> config) {
        String[] sensitiveFields = {
            "password", "apiKey", "secret", "token", "privateKey",
            "passphrase", "clientSecret", "accessToken", "refreshToken",
            "bearerToken", "basicPassword", "sslKeyStorePassword",
            "sslTrustStorePassword", "jwtToken", "oauthAccessToken"
        };

        for(String field : sensitiveFields) {
            if(config.containsKey(field)) {
                Object value = config.get(field);
                if(value instanceof String && isEncrypted((String) value)) {
                    config.put(field, decrypt((String) value));
                }
            }
        }
    }
}
