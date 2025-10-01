package com.integrixs.backend.infrastructure.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Infrastructure service for certificate file storage
 * Handles file system operations for certificates
 */
@Service
public class CertificateStorageService {

    private static final Logger log = LoggerFactory.getLogger(CertificateStorageService.class);

    @Value("${certificates.storage.path:/opt/integrixlab/certs}")
    private String certStoragePath;

    /**
     * Save certificate file to disk
     */
    public void saveCertificateFile(String fileName, byte[] content) throws IOException {
        Path directory = Paths.get(certStoragePath);

        // Create directory if it doesn't exist
        if(!Files.exists(directory)) {
            Files.createDirectories(directory);
            log.info("Created certificate storage directory: {}", directory);
        }

        Path filePath = directory.resolve(fileName);

        try(FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
            fos.write(content);
            log.info("Certificate file saved: {}", filePath);
        } catch(IOException e) {
            log.error("Failed to save certificate file: {}", filePath, e);
            throw new IOException("Failed to save certificate file", e);
        }
    }

    /**
     * Save certificate file from multipart
     */
    public void saveCertificateFile(String fileName, MultipartFile file) throws IOException {
        saveCertificateFile(fileName, file.getBytes());
    }

    /**
     * Read certificate file from disk
     */
    public byte[] readCertificateFile(String fileName) throws IOException {
        Path filePath = Paths.get(certStoragePath, fileName);

        if(!Files.exists(filePath)) {
            throw new IOException("Certificate file not found: " + fileName);
        }

        try {
            return Files.readAllBytes(filePath);
        } catch(IOException e) {
            log.error("Failed to read certificate file: {}", filePath, e);
            throw new IOException("Failed to read certificate file", e);
        }
    }

    /**
     * Delete certificate file from disk
     */
    public void deleteCertificateFile(String fileName) throws IOException {
        Path filePath = Paths.get(certStoragePath, fileName);

        if(Files.exists(filePath)) {
            try {
                Files.delete(filePath);
                log.info("Certificate file deleted: {}", filePath);
            } catch(IOException e) {
                log.error("Failed to delete certificate file: {}", filePath, e);
                throw new IOException("Failed to delete certificate file", e);
            }
        } else {
            log.warn("Certificate file not found for deletion: {}", filePath);
        }
    }

    /**
     * Check if certificate file exists
     */
    public boolean certificateFileExists(String fileName) {
        Path filePath = Paths.get(certStoragePath, fileName);
        return Files.exists(filePath);
    }

    /**
     * Get storage path
     */
    public String getStoragePath() {
        return certStoragePath;
    }
}
