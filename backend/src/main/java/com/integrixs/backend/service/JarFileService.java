package com.integrixs.backend.service;

import com.integrixs.data.model.JarFile;
import com.integrixs.data.sql.repository.JarFileSqlRepository;
import com.integrixs.shared.dto.JarFileDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class JarFileService {

    private static final Logger logger = LoggerFactory.getLogger(JarFileService.class);

    private final JarFileSqlRepository jarFileRepository;

    @Value("${jar.max.size:52428800}") // 50MB default
    private long maxFileSize;

    public JarFileService(JarFileSqlRepository jarFileRepository) {
        this.jarFileRepository = jarFileRepository;
    }

    /**
     * Get all active JAR files
     */
    public List<JarFileDTO> getAllJarFiles() {
        logger.debug("Getting all JAR files");
        return jarFileRepository.findAllActive().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Upload a new JAR file
     */
    public JarFileDTO uploadJarFile(MultipartFile file, String uploadedBy, String description) throws IOException {
        logger.info("Uploading JAR file: {} by {}", file.getOriginalFilename(), uploadedBy);

        // Validate file
        validateJarFile(file);

        // Read file content
        byte[] fileContent = file.getBytes();

        // Calculate checksum
        String checksum = calculateChecksum(fileContent);

        // Check if file already exists
        Optional<JarFile> existing = jarFileRepository.findByChecksum(checksum);
        if(existing.isPresent() && existing.get().isActive()) {
            throw new IllegalArgumentException("JAR file with same content already exists");
        }

        // Create database entry with file content
        JarFile jarFile = new JarFile();
        jarFile.setFileName(file.getOriginalFilename());
        jarFile.setDisplayName(file.getOriginalFilename());
        jarFile.setFileContent(fileContent);
        jarFile.setFileSize(file.getSize());
        jarFile.setChecksum(checksum);
        jarFile.setUploadedBy(uploadedBy);
        jarFile.setDescription(description);
        jarFile.setActive(true);

        // Extract version from filename if present
        String version = extractVersionFromFilename(file.getOriginalFilename());
        if(version != null) {
            jarFile.setVersion(version);
        }

        // Set metadata
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("contentType", file.getContentType());
        metadata.put("originalSize", file.getSize());
        jarFile.setMetadata(metadata);

        JarFile saved = jarFileRepository.save(jarFile);
        logger.info("Successfully uploaded JAR file with ID: {}", saved.getId());

        return convertToDTO(saved);
    }

    /**
     * Get JAR file by ID
     */
    public JarFileDTO getJarFileById(String id) {
        logger.debug("Getting JAR file by id: {}", id);

        UUID uuid = UUID.fromString(id);
        return jarFileRepository.findById(uuid)
                .map(this::convertToDTO)
                .orElseThrow(() -> new NoSuchElementException("JAR file not found with ID: " + id));
    }

    /**
     * Get JAR file content
     */
    public byte[] getJarFileContent(String id) throws IOException {
        logger.debug("Getting JAR file content for id: {}", id);

        UUID uuid = UUID.fromString(id);
        JarFile jarFile = jarFileRepository.findById(uuid)
                .orElseThrow(() -> new NoSuchElementException("JAR file not found with ID: " + id));

        if(!jarFile.isActive()) {
            throw new IllegalStateException("JAR file is not active: " + id);
        }

        return jarFile.getFileContent();
    }

    /**
     * Delete JAR file(soft delete)
     */
    public void deleteJarFile(String id) {
        logger.info("Deleting JAR file with id: {}", id);

        UUID uuid = UUID.fromString(id);
        JarFile jarFile = jarFileRepository.findById(uuid)
                .orElseThrow(() -> new NoSuchElementException("JAR file not found with ID: " + id));

        // Check if JAR is being used by any plugins
        // This would require checking AdapterPlugin references

        jarFile.setActive(false);
        jarFileRepository.save(jarFile);

        logger.info("JAR file marked as inactive: {}", id);
    }

    /**
     * Permanently delete JAR file
     */
    public void permanentlyDeleteJarFile(String id) throws IOException {
        logger.warn("Permanently deleting JAR file with id: {}", id);

        UUID uuid = UUID.fromString(id);
        JarFile jarFile = jarFileRepository.findById(uuid)
                .orElseThrow(() -> new NoSuchElementException("JAR file not found with ID: " + id));

        // Delete from database(file content is stored in DB)
        jarFileRepository.deleteById(jarFile.getId());

        logger.info("JAR file permanently deleted: {}", id);
    }

    /**
     * Search JAR files
     */
    public List<JarFileDTO> searchJarFiles(String query) {
        logger.debug("Searching JAR files with query: {}", query);

        if(query == null || query.trim().isEmpty()) {
            return getAllJarFiles();
        }

        return jarFileRepository.searchByQuery(query).stream()
                .filter(JarFile::isActive)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get total storage size used by JAR files
     */
    public long getTotalStorageSize() {
        Long totalSize = jarFileRepository.getTotalActiveFileSize();
        return totalSize != null ? totalSize : 0L;
    }

    /**
     * Validate JAR file before upload
     */
    private void validateJarFile(MultipartFile file) {
        // Check if file is empty
        if(file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Check file extension
        String filename = file.getOriginalFilename();
        if(filename == null || !filename.toLowerCase().endsWith(".jar")) {
            throw new IllegalArgumentException("File must be a JAR file");
        }

        // Check file size
        if(file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size of " +
                    (maxFileSize / 1024 / 1024) + " MB");
        }

        // Verify it's actually a JAR file(check magic bytes)
        try {
            byte[] header = new byte[4];
            file.getInputStream().read(header);
            file.getInputStream().reset();

            // JAR files are ZIP files, check for ZIP magic number
            if(!(header[0] == 0x50 && header[1] == 0x4B &&
                  (header[2] == 0x03 || header[2] == 0x05 || header[2] == 0x07) &&
                  (header[3] == 0x04 || header[3] == 0x06 || header[3] == 0x08))) {
                throw new IllegalArgumentException("File is not a valid JAR file");
            }
        } catch(IOException e) {
            throw new IllegalArgumentException("Unable to read file", e);
        }
    }

    /**
     * Calculate SHA-256 checksum of file content
     */
    private String calculateChecksum(byte[] content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(content);

            StringBuilder hexString = new StringBuilder();
            for(byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if(hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();

        } catch(NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Extract version from filename
     */
    private String extractVersionFromFilename(String filename) {
        if(filename == null) {
            return null;
        }

        // Common version patterns: name-1.0.0.jar, name_v1.0.0.jar, name-1.0.jar
        String[] patterns = {
            ".*[ - _]v?([0-9] + \\.[0-9] + \\.[0-9] + )\\.jar$",
            ".*[ - _]v?([0-9] + \\.[0-9] + )\\.jar$",
            ".*[ - _]v?([0-9] + )\\.jar$"
        };

        for(String pattern : patterns) {
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern,
                java.util.regex.Pattern.CASE_INSENSITIVE);
            java.util.regex.Matcher m = p.matcher(filename);
            if(m.matches()) {
                return m.group(1);
            }
        }

        return null;
    }

    /**
     * Convert JarFile entity to DTO
     */
    private JarFileDTO convertToDTO(JarFile jarFile) {
        return JarFileDTO.builder()
                .id(jarFile.getId().toString())
                .name(jarFile.getDisplayName())
                .version(jarFile.getVersion())
                .description(jarFile.getDescription())
                .size(jarFile.getFileSize())
                .uploadedAt(jarFile.getUploadedAt())
                .uploadedBy(jarFile.getUploadedBy())
                .build();
    }
}
