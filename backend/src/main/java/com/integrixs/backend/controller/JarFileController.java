package com.integrixs.backend.controller;

import com.integrixs.backend.service.JarFileService;
import com.integrixs.shared.dto.JarFileDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jar - files")
@CrossOrigin(origins = "*")
public class JarFileController {

    private static final Logger logger = LoggerFactory.getLogger(JarFileController.class);

    private final JarFileService jarFileService;

    public JarFileController(JarFileService jarFileService) {
        this.jarFileService = jarFileService;
    }

    /**
     * Get all JAR files
     */
    @GetMapping
    public ResponseEntity<List<JarFileDTO>> getAllJarFiles() {
        logger.debug("Getting all JAR files");
        List<JarFileDTO> jarFiles = jarFileService.getAllJarFiles();
        return ResponseEntity.ok(jarFiles);
    }

    /**
     * Get JAR file by ID
     */
    @GetMapping("/ {id}")
    public ResponseEntity<JarFileDTO> getJarFileById(@PathVariable String id) {
        logger.debug("Getting JAR file by id: {}", id);
        JarFileDTO jarFile = jarFileService.getJarFileById(id);
        return ResponseEntity.ok(jarFile);
    }

    /**
     * Upload a new JAR file
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<JarFileDTO> uploadJarFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description,
            Authentication authentication) throws IOException {

        logger.info("Uploading JAR file: {} by {}", file.getOriginalFilename(),
                authentication != null ? authentication.getName() : "anonymous");

        String uploadedBy = authentication != null ? authentication.getName() : "anonymous";
        JarFileDTO uploaded = jarFileService.uploadJarFile(file, uploadedBy, description);

        return ResponseEntity.ok(uploaded);
    }

    /**
     * Download JAR file
     */
    @GetMapping("/ {id}/download")
    public ResponseEntity<Resource> downloadJarFile(@PathVariable String id) throws IOException {
        logger.debug("Downloading JAR file: {}", id);

        JarFileDTO jarFile = jarFileService.getJarFileById(id);
        byte[] content = jarFileService.getJarFileContent(id);

        ByteArrayResource resource = new ByteArrayResource(content);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename = \"" + jarFile.getName() + "\"")
                .contentLength(content.length)
                .body(resource);
    }

    /**
     * Delete JAR file(soft delete)
     */
    @DeleteMapping("/ {id}")
    public ResponseEntity<Void> deleteJarFile(@PathVariable String id) {
        logger.info("Deleting JAR file: {}", id);
        jarFileService.deleteJarFile(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Permanently delete JAR file
     */
    @DeleteMapping("/ {id}/permanent")
    public ResponseEntity<Void> permanentlyDeleteJarFile(@PathVariable String id) throws IOException {
        logger.warn("Permanently deleting JAR file: {}", id);
        jarFileService.permanentlyDeleteJarFile(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Search JAR files
     */
    @GetMapping("/search")
    public ResponseEntity<List<JarFileDTO>> searchJarFiles(
            @RequestParam(value = "q", required = false) String query) {
        logger.debug("Searching JAR files with query: {}", query);
        List<JarFileDTO> results = jarFileService.searchJarFiles(query);
        return ResponseEntity.ok(results);
    }

    /**
     * Get storage statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStorageStats() {
        logger.debug("Getting JAR storage statistics");

        long totalSize = jarFileService.getTotalStorageSize();
        List<JarFileDTO> allFiles = jarFileService.getAllJarFiles();

        Map<String, Object> stats = Map.of(
                "totalFiles", allFiles.size(),
                "totalSizeBytes", totalSize,
                "totalSizeMB", totalSize / 1024.0 / 1024.0
       );

        return ResponseEntity.ok(stats);
    }
}
