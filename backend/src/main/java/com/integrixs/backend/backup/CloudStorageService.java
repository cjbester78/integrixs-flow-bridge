package com.integrixs.backend.backup;

import java.util.List;

/**
 * Interface for cloud storage operations
 * Implementations can support S3, Azure Blob Storage, Google Cloud Storage, etc.
 */
public interface CloudStorageService {
    /**
     * Upload a file to cloud storage
     * @param localPath Path to local file to upload
     * @throws Exception if upload fails
     */
    void uploadFile(String localPath) throws Exception;

    /**
     * Download a file from cloud storage
     * @param remotePath Path in cloud storage
     * @param localPath Local path to save file
     * @throws Exception if download fails
     */
    void downloadFile(String remotePath, String localPath) throws Exception;

    /**
     * List files in cloud storage with given prefix
     * @param prefix Path prefix to filter files
     * @return List of file paths
     * @throws Exception if listing fails
     */
    List<String> listFiles(String prefix) throws Exception;

    /**
     * Delete a file from cloud storage
     * @param remotePath Path in cloud storage
     * @throws Exception if deletion fails
     */
    void deleteFile(String remotePath) throws Exception;

    /**
     * Check if a file exists in cloud storage
     * @param remotePath Path in cloud storage
     * @return true if file exists
     * @throws Exception if check fails
     */
    boolean fileExists(String remotePath) throws Exception;

    /**
     * Get file metadata
     * @param remotePath Path in cloud storage
     * @return File metadata including size, last modified, etc.
     * @throws Exception if metadata retrieval fails
     */
    FileMetadata getFileMetadata(String remotePath) throws Exception;

    /**
     * File metadata class
     */
    class FileMetadata {
        private final String path;
        private final long size;
        private final java.time.Instant lastModified;
        private final String contentType;
        private final java.util.Map<String, String> customMetadata;

        public FileMetadata(String path, long size, java.time.Instant lastModified,
                          String contentType, java.util.Map<String, String> customMetadata) {
            this.path = path;
            this.size = size;
            this.lastModified = lastModified;
            this.contentType = contentType;
            this.customMetadata = customMetadata;
        }

        // Getters
        public String getPath() { return path; }
        public long getSize() { return size; }
        public java.time.Instant getLastModified() { return lastModified; }
        public String getContentType() { return contentType; }
        public java.util.Map<String, String> getCustomMetadata() { return customMetadata; }
    }
}
