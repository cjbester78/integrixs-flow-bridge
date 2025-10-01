package com.integrixs.shared.dto.export;

import jakarta.validation.constraints.NotNull;
import java.util.Set;
import java.util.HashSet;
import java.util.List;

/**
 * DTO for requesting flow export with options.
 */
public class FlowExportRequestDTO {

    private String flowId;
    private ExportOptions options = new ExportOptions();
    private boolean includeBusinessComponent = true;
    private boolean includeAdapterConfigs = true;
    private boolean includeCertificateReferences = true;
    private boolean includeSensitiveData = false;
    private boolean includeStatistics = false;
    private boolean includeAuditInfo = false;
    private ExportFormat format = ExportFormat.JSON;
    private boolean compress = false;
    private String environment;
    private String description;
    private Set<String> tags;

    // Default constructor
    public FlowExportRequestDTO() {
        this.tags = new HashSet<>();
    }

    // All args constructor
    public FlowExportRequestDTO(String flowId, ExportOptions options, boolean includeBusinessComponent, boolean includeAdapterConfigs, boolean includeCertificateReferences, boolean includeSensitiveData, boolean includeStatistics, boolean includeAuditInfo, ExportFormat format, boolean compress, String environment, String description, Set<String> tags) {
        this.flowId = flowId;
        this.options = options;
        this.includeBusinessComponent = includeBusinessComponent;
        this.includeAdapterConfigs = includeAdapterConfigs;
        this.includeCertificateReferences = includeCertificateReferences;
        this.includeSensitiveData = includeSensitiveData;
        this.includeStatistics = includeStatistics;
        this.includeAuditInfo = includeAuditInfo;
        this.format = format;
        this.compress = compress;
        this.environment = environment;
        this.description = description;
        this.tags = tags != null ? tags : new HashSet<>();
    }

    // Getters
    public String getFlowId() { return flowId; }
    public ExportOptions getOptions() { return options; }
    public boolean isIncludeBusinessComponent() { return includeBusinessComponent; }
    public boolean isIncludeAdapterConfigs() { return includeAdapterConfigs; }
    public boolean isIncludeCertificateReferences() { return includeCertificateReferences; }
    public boolean isIncludeSensitiveData() { return includeSensitiveData; }
    public boolean isIncludeStatistics() { return includeStatistics; }
    public boolean isIncludeAuditInfo() { return includeAuditInfo; }
    public ExportFormat getFormat() { return format; }
    public boolean isCompress() { return compress; }
    public String getEnvironment() { return environment; }
    public String getDescription() { return description; }
    public Set<String> getTags() { return tags; }

    // Setters
    public void setFlowId(String flowId) { this.flowId = flowId; }
    public void setOptions(ExportOptions options) { this.options = options; }
    public void setIncludeBusinessComponent(boolean includeBusinessComponent) { this.includeBusinessComponent = includeBusinessComponent; }
    public void setIncludeAdapterConfigs(boolean includeAdapterConfigs) { this.includeAdapterConfigs = includeAdapterConfigs; }
    public void setIncludeCertificateReferences(boolean includeCertificateReferences) { this.includeCertificateReferences = includeCertificateReferences; }
    public void setIncludeSensitiveData(boolean includeSensitiveData) { this.includeSensitiveData = includeSensitiveData; }
    public void setIncludeStatistics(boolean includeStatistics) { this.includeStatistics = includeStatistics; }
    public void setIncludeAuditInfo(boolean includeAuditInfo) { this.includeAuditInfo = includeAuditInfo; }
    public void setFormat(ExportFormat format) { this.format = format; }
    public void setCompress(boolean compress) { this.compress = compress; }
    public void setEnvironment(String environment) { this.environment = environment; }
    public void setDescription(String description) { this.description = description; }
    public void setTags(Set<String> tags) { this.tags = tags; }

    // Builder
    public static FlowExportRequestDTOBuilder builder() {
        return new FlowExportRequestDTOBuilder();
    }

    public static class FlowExportRequestDTOBuilder {
        private String flowId;
        private ExportOptions options = new ExportOptions();
        private boolean includeBusinessComponent = true;
        private boolean includeAdapterConfigs = true;
        private boolean includeCertificateReferences = true;
        private boolean includeSensitiveData = false;
        private boolean includeStatistics = false;
        private boolean includeAuditInfo = false;
        private ExportFormat format = ExportFormat.JSON;
        private boolean compress = false;
        private String environment;
        private String description;
        private Set<String> tags = new HashSet<>();

        public FlowExportRequestDTOBuilder flowId(String flowId) {
            this.flowId = flowId;
            return this;
        }

        public FlowExportRequestDTOBuilder options(ExportOptions options) {
            this.options = options;
            return this;
        }

        public FlowExportRequestDTOBuilder includeBusinessComponent(boolean includeBusinessComponent) {
            this.includeBusinessComponent = includeBusinessComponent;
            return this;
        }

        public FlowExportRequestDTOBuilder includeAdapterConfigs(boolean includeAdapterConfigs) {
            this.includeAdapterConfigs = includeAdapterConfigs;
            return this;
        }

        public FlowExportRequestDTOBuilder includeCertificateReferences(boolean includeCertificateReferences) {
            this.includeCertificateReferences = includeCertificateReferences;
            return this;
        }

        public FlowExportRequestDTOBuilder includeSensitiveData(boolean includeSensitiveData) {
            this.includeSensitiveData = includeSensitiveData;
            return this;
        }

        public FlowExportRequestDTOBuilder includeStatistics(boolean includeStatistics) {
            this.includeStatistics = includeStatistics;
            return this;
        }

        public FlowExportRequestDTOBuilder includeAuditInfo(boolean includeAuditInfo) {
            this.includeAuditInfo = includeAuditInfo;
            return this;
        }

        public FlowExportRequestDTOBuilder format(ExportFormat format) {
            this.format = format;
            return this;
        }

        public FlowExportRequestDTOBuilder compress(boolean compress) {
            this.compress = compress;
            return this;
        }

        public FlowExportRequestDTOBuilder environment(String environment) {
            this.environment = environment;
            return this;
        }

        public FlowExportRequestDTOBuilder description(String description) {
            this.description = description;
            return this;
        }

        public FlowExportRequestDTOBuilder tags(Set<String> tags) {
            this.tags = tags;
            return this;
        }

        public FlowExportRequestDTO build() {
            return new FlowExportRequestDTO(flowId, options, includeBusinessComponent, includeAdapterConfigs, includeCertificateReferences, includeSensitiveData, includeStatistics, includeAuditInfo, format, compress, environment, description, tags);
        }
    }

    /**
     * Export options
     */
    public static class ExportOptions {
        private boolean includeBusinessComponent = true;
        private boolean includeAdapterConfigs = true;
        private boolean includeCertificateReferences = true;
        private boolean includeSensitiveData = false;
        private boolean includeStatistics = false;
        private boolean includeAuditInfo = false;
        private List<String> tags;

        // Default constructor
        public ExportOptions() {
        }

        // Getters
        public boolean isIncludeBusinessComponent() { return includeBusinessComponent; }
        public boolean isIncludeAdapterConfigs() { return includeAdapterConfigs; }
        public boolean isIncludeCertificateReferences() { return includeCertificateReferences; }
        public boolean isIncludeSensitiveData() { return includeSensitiveData; }
        public boolean isIncludeStatistics() { return includeStatistics; }
        public boolean isIncludeAuditInfo() { return includeAuditInfo; }
        public List<String> getTags() { return tags; }

        // Setters
        public void setIncludeBusinessComponent(boolean includeBusinessComponent) { this.includeBusinessComponent = includeBusinessComponent; }
        public void setIncludeAdapterConfigs(boolean includeAdapterConfigs) { this.includeAdapterConfigs = includeAdapterConfigs; }
        public void setIncludeCertificateReferences(boolean includeCertificateReferences) { this.includeCertificateReferences = includeCertificateReferences; }
        public void setIncludeSensitiveData(boolean includeSensitiveData) { this.includeSensitiveData = includeSensitiveData; }
        public void setIncludeStatistics(boolean includeStatistics) { this.includeStatistics = includeStatistics; }
        public void setIncludeAuditInfo(boolean includeAuditInfo) { this.includeAuditInfo = includeAuditInfo; }
        public void setTags(List<String> tags) { this.tags = tags; }
    }

    /**
     * Export format enum
     */
    public enum ExportFormat {
        JSON,
        XML,
        YAML
    }
}
