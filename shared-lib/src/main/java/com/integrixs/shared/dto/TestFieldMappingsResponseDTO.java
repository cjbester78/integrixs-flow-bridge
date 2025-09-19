package com.integrixs.shared.dto;

import java.util.List;
import java.util.ArrayList;

/**
 * Response DTO for testing field mappings
 */
public class TestFieldMappingsResponseDTO {

    private boolean success;
    private String outputXml;
    private String error;
    private List<String> warnings;
    private long executionTimeMs;

    // Default constructor
    public TestFieldMappingsResponseDTO() {
        this.warnings = new ArrayList<>();
    }

    // All args constructor
    public TestFieldMappingsResponseDTO(boolean success, String outputXml, String error, List<String> warnings, long executionTimeMs) {
        this.success = success;
        this.outputXml = outputXml;
        this.error = error;
        this.warnings = warnings != null ? warnings : new ArrayList<>();
        this.executionTimeMs = executionTimeMs;
    }

    // Getters
    public boolean isSuccess() { return success; }
    public String getOutputXml() { return outputXml; }
    public String getError() { return error; }
    public List<String> getWarnings() { return warnings; }
    public long getExecutionTimeMs() { return executionTimeMs; }

    // Setters
    public void setSuccess(boolean success) { this.success = success; }
    public void setOutputXml(String outputXml) { this.outputXml = outputXml; }
    public void setError(String error) { this.error = error; }
    public void setWarnings(List<String> warnings) { this.warnings = warnings; }
    public void setExecutionTimeMs(long executionTimeMs) { this.executionTimeMs = executionTimeMs; }

    // Builder
    public static TestFieldMappingsResponseDTOBuilder builder() {
        return new TestFieldMappingsResponseDTOBuilder();
    }

    public static class TestFieldMappingsResponseDTOBuilder {
        private boolean success;
        private String outputXml;
        private String error;
        private List<String> warnings = new ArrayList<>();
        private long executionTimeMs;

        public TestFieldMappingsResponseDTOBuilder success(boolean success) {
            this.success = success;
            return this;
        }

        public TestFieldMappingsResponseDTOBuilder outputXml(String outputXml) {
            this.outputXml = outputXml;
            return this;
        }

        public TestFieldMappingsResponseDTOBuilder error(String error) {
            this.error = error;
            return this;
        }

        public TestFieldMappingsResponseDTOBuilder warnings(List<String> warnings) {
            this.warnings = warnings;
            return this;
        }

        public TestFieldMappingsResponseDTOBuilder executionTimeMs(long executionTimeMs) {
            this.executionTimeMs = executionTimeMs;
            return this;
        }

        public TestFieldMappingsResponseDTO build() {
            return new TestFieldMappingsResponseDTO(success, outputXml, error, warnings, executionTimeMs);
        }
    }
}
