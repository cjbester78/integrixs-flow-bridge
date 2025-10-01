package com.integrixs.backend.dto;

import java.util.Map;

/**
 * Result of streaming file upload and processing
 */
public class StreamingUploadResult {

    private String sessionId;
    private String fileName;
    private long fileSize;
    private int elementsProcessed;
    private long processingTimeMs;
    private boolean success;
    private String errorMessage;
    private Map<String, Object> structure;
    private Map<String, Object> metadata;

    // Constructors
    public StreamingUploadResult() {}

    public StreamingUploadResult(String sessionId, String fileName, long fileSize) {
        this.sessionId = sessionId;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.success = true;
    }

    // Getters and Setters
    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public int getElementsProcessed() {
        return elementsProcessed;
    }

    public void setElementsProcessed(int elementsProcessed) {
        this.elementsProcessed = elementsProcessed;
    }

    public long getProcessingTimeMs() {
        return processingTimeMs;
    }

    public void setProcessingTimeMs(long processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        this.success = false;
    }

    public Map<String, Object> getStructure() {
        return structure;
    }

    public void setStructure(Map<String, Object> structure) {
        this.structure = structure;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    /**
     * Calculate processing rate in elements per second
     */
    public double getProcessingRate() {
        if(processingTimeMs > 0) {
            return(elementsProcessed * 1000.0) / processingTimeMs;
        }
        return 0;
    }

    /**
     * Calculate throughput in MB/s
     */
    public double getThroughputMBps() {
        if(processingTimeMs > 0) {
            return(fileSize / (1024.0 * 1024.0)) / (processingTimeMs / 1000.0);
        }
        return 0;
    }
}
