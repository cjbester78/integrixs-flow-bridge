package com.integrixs.backend.backup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Mock cloud storage service for development and testing
 */
@Service
@ConditionalOnProperty(name = "backup.cloud.provider", havingValue = "mock", matchIfMissing = true)
public class MockCloudStorageService implements CloudStorageService {

    private static final Logger logger = LoggerFactory.getLogger(MockCloudStorageService.class);
    private static final String MOCK_STORAGE_PATH = "/tmp/mock - cloud - storage";

    public MockCloudStorageService() {
        // Ensure mock storage directory exists
        try {
            Files.createDirectories(Paths.get(MOCK_STORAGE_PATH));
        } catch(Exception e) {
            logger.error("Failed to create mock storage directory", e);
        }
    }

    @Override
    public void uploadFile(String localPath) throws Exception {
        logger.info("Mock upload: {}", localPath);

        Path source = Paths.get(localPath);
        if(!Files.exists(source)) {
            throw new IllegalArgumentException("File not found: " + localPath);
        }

        String fileName = source.getFileName().toString();
        Path destination = Paths.get(MOCK_STORAGE_PATH, fileName);

        Files.copy(source, destination,
            java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        logger.info("Mock upload completed: {} -> {}", localPath, destination);
    }

    @Override
    public void downloadFile(String remotePath, String localPath) throws Exception {
        logger.info("Mock download: {} -> {}", remotePath, localPath);

        Path source = Paths.get(MOCK_STORAGE_PATH, remotePath);
        if(!Files.exists(source)) {
            throw new IllegalArgumentException("Remote file not found: " + remotePath);
        }

        Path destination = Paths.get(localPath);
        Files.createDirectories(destination.getParent());
        Files.copy(source, destination,
            java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        logger.info("Mock download completed");
    }

    @Override
    public List<String> listFiles(String prefix) throws Exception {
        logger.info("Mock list files with prefix: {}", prefix);

        Path storageDir = Paths.get(MOCK_STORAGE_PATH);
        if(!Files.exists(storageDir)) {
            return Collections.emptyList();
        }

        return Files.walk(storageDir)
            .filter(Files::isRegularFile)
            .map(path -> storageDir.relativize(path).toString())
            .filter(path -> prefix == null || path.startsWith(prefix))
            .sorted()
            .collect(Collectors.toList());
    }

    @Override
    public void deleteFile(String remotePath) throws Exception {
        logger.info("Mock delete: {}", remotePath);

        Path file = Paths.get(MOCK_STORAGE_PATH, remotePath);
        if(Files.exists(file)) {
            Files.delete(file);
            logger.info("Mock delete completed");
        } else {
            logger.warn("File not found for deletion: {}", remotePath);
        }
    }

    @Override
    public boolean fileExists(String remotePath) throws Exception {
        Path file = Paths.get(MOCK_STORAGE_PATH, remotePath);
        boolean exists = Files.exists(file);
        logger.info("Mock file exists check: {} = {}", remotePath, exists);
        return exists;
    }

    @Override
    public FileMetadata getFileMetadata(String remotePath) throws Exception {
        logger.info("Mock get metadata: {}", remotePath);

        Path file = Paths.get(MOCK_STORAGE_PATH, remotePath);
        if(!Files.exists(file)) {
            throw new IllegalArgumentException("File not found: " + remotePath);
        }

        long size = Files.size(file);
        Instant lastModified = Files.getLastModifiedTime(file).toInstant();
        String contentType = Files.probeContentType(file);

        Map<String, String> customMetadata = new HashMap<>();
        customMetadata.put("storage", "mock");
        customMetadata.put("path", file.toString());

        return new FileMetadata(remotePath, size, lastModified, contentType, customMetadata);
    }
}
