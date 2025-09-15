package com.integrixs.shared.services;

/**
 * Service for encrypting and decrypting credentials
 */
public interface CredentialEncryptionService {

    /**
     * Encrypt a credential string
     * @param plainText The plain text credential to encrypt
     * @return The encrypted credential
     */
    String encrypt(String plainText);

    /**
     * Decrypt a credential string
     * @param encryptedText The encrypted credential
     * @return The decrypted plain text credential
     */
    String decrypt(String encryptedText);

    /**
     * Encrypt credential data with a specific key
     * @param plainText The plain text credential to encrypt
     * @param keyAlias The key alias to use for encryption
     * @return The encrypted credential
     */
    String encrypt(String plainText, String keyAlias);

    /**
     * Decrypt credential data with a specific key
     * @param encryptedText The encrypted credential
     * @param keyAlias The key alias to use for decryption
     * @return The decrypted plain text credential
     */
    String decrypt(String encryptedText, String keyAlias);

    /**
     * Check if a string is encrypted
     * @param text The text to check
     * @return True if the text appears to be encrypted, false otherwise
     */
    boolean isEncrypted(String text);

    /**
     * Decrypt a credential only if it is encrypted
     * @param text The text that may be encrypted
     * @return The decrypted text if it was encrypted, otherwise the original text
     */
    default String decryptIfNeeded(String text) {
        if(text == null || text.isEmpty()) {
            return text;
        }
        return isEncrypted(text) ? decrypt(text) : text;
    }

}
