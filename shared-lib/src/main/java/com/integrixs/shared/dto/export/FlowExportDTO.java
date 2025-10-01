package com.integrixs.shared.dto.export;

import com.integrixs.shared.dto.FlowDTO;
import com.integrixs.shared.dto.business.BusinessComponentDTO;
import com.integrixs.shared.dto.CommunicationAdapterDTO;
import com.integrixs.shared.dto.flow.FlowTransformationDTO;
import com.integrixs.shared.dto.certificate.CertificateDTO;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DTO for exporting a flow and all its dependencies
 */
public class FlowExportDTO {

    private String exportVersion;
    private LocalDateTime exportDate;
    private ExportMetadata metadata;
    private FlowDTO flow;
    private BusinessComponentDTO businessComponent;
    private List<CommunicationAdapterDTO> adapters;
    private CommunicationAdapterDTO inboundAdapter;
    private CommunicationAdapterDTO outboundAdapter;
    private List<FlowTransformationDTO> transformations;
    private List<com.integrixs.shared.dto.FieldMappingDTO> fieldMappings;
    private List<CertificateReferenceDTO> certificateReferences;

    // Default constructor
    public FlowExportDTO() {
        this.adapters = new ArrayList<>();
        this.transformations = new ArrayList<>();
        this.fieldMappings = new ArrayList<>();
        this.certificateReferences = new ArrayList<>();
    }

    // All args constructor
    public FlowExportDTO(String exportVersion, LocalDateTime exportDate, ExportMetadata metadata, FlowDTO flow, BusinessComponentDTO businessComponent, List<CommunicationAdapterDTO> adapters, List<FlowTransformationDTO> transformations, List<CertificateReferenceDTO> certificateReferences) {
        this.exportVersion = exportVersion;
        this.exportDate = exportDate;
        this.metadata = metadata;
        this.flow = flow;
        this.businessComponent = businessComponent;
        this.adapters = adapters != null ? adapters : new ArrayList<>();
        this.transformations = transformations != null ? transformations : new ArrayList<>();
        this.certificateReferences = certificateReferences != null ? certificateReferences : new ArrayList<>();
    }

    // Getters
    public String getExportVersion() { return exportVersion; }
    public LocalDateTime getExportDate() { return exportDate; }
    public ExportMetadata getMetadata() { return metadata; }
    public FlowDTO getFlow() { return flow; }
    public BusinessComponentDTO getBusinessComponent() { return businessComponent; }
    public List<CommunicationAdapterDTO> getAdapters() { return adapters; }
    public List<FlowTransformationDTO> getTransformations() { return transformations; }
    public CommunicationAdapterDTO getInboundAdapter() { return inboundAdapter; }
    public CommunicationAdapterDTO getOutboundAdapter() { return outboundAdapter; }
    public List<com.integrixs.shared.dto.FieldMappingDTO> getFieldMappings() { return fieldMappings; }
    public List<CertificateReferenceDTO> getCertificateReferences() { return certificateReferences; }

    // Setters
    public void setExportVersion(String exportVersion) { this.exportVersion = exportVersion; }
    public void setExportDate(LocalDateTime exportDate) { this.exportDate = exportDate; }
    public void setMetadata(ExportMetadata metadata) { this.metadata = metadata; }
    public void setFlow(FlowDTO flow) { this.flow = flow; }
    public void setBusinessComponent(BusinessComponentDTO businessComponent) { this.businessComponent = businessComponent; }
    public void setAdapters(List<CommunicationAdapterDTO> adapters) { this.adapters = adapters; }
    public void setTransformations(List<FlowTransformationDTO> transformations) { this.transformations = transformations; }
    public void setInboundAdapter(CommunicationAdapterDTO inboundAdapter) { this.inboundAdapter = inboundAdapter; }
    public void setOutboundAdapter(CommunicationAdapterDTO outboundAdapter) { this.outboundAdapter = outboundAdapter; }
    public void setFieldMappings(List<com.integrixs.shared.dto.FieldMappingDTO> fieldMappings) { this.fieldMappings = fieldMappings; }
    public void setCertificateReferences(List<CertificateReferenceDTO> certificateReferences) { this.certificateReferences = certificateReferences; }

    // Builder
    public static FlowExportDTOBuilder builder() {
        return new FlowExportDTOBuilder();
    }

    public static class FlowExportDTOBuilder {
        private String exportVersion;
        private LocalDateTime exportDate;
        private ExportMetadata metadata;
        private FlowDTO flow;
        private BusinessComponentDTO businessComponent;
        private List<CommunicationAdapterDTO> adapters = new ArrayList<>();
        private List<FlowTransformationDTO> transformations = new ArrayList<>();
        private List<CertificateReferenceDTO> certificateReferences = new ArrayList<>();

        public FlowExportDTOBuilder exportVersion(String exportVersion) {
            this.exportVersion = exportVersion;
            return this;
        }

        public FlowExportDTOBuilder exportDate(LocalDateTime exportDate) {
            this.exportDate = exportDate;
            return this;
        }

        public FlowExportDTOBuilder metadata(ExportMetadata metadata) {
            this.metadata = metadata;
            return this;
        }

        public FlowExportDTOBuilder flow(FlowDTO flow) {
            this.flow = flow;
            return this;
        }

        public FlowExportDTOBuilder businessComponent(BusinessComponentDTO businessComponent) {
            this.businessComponent = businessComponent;
            return this;
        }

        public FlowExportDTOBuilder adapters(List<CommunicationAdapterDTO> adapters) {
            this.adapters = adapters;
            return this;
        }

        public FlowExportDTOBuilder transformations(List<FlowTransformationDTO> transformations) {
            this.transformations = transformations;
            return this;
        }

        public FlowExportDTOBuilder certificateReferences(List<CertificateReferenceDTO> certificateReferences) {
            this.certificateReferences = certificateReferences;
            return this;
        }

        public FlowExportDTO build() {
            return new FlowExportDTO(exportVersion, exportDate, metadata, flow, businessComponent, adapters, transformations, certificateReferences);
        }
    }

    /**
     * Metadata about the export
     */
    public static class ExportMetadata {
        private String exportId;
        private String exportVersion;
        private String applicationVersion;
        private LocalDateTime exportDate;
        private String exportedBy;
        private String exportedByUsername;
        private String systemVersion;
        private Map<String, Integer> objectCounts;
        private String description;
        private String environment;
        private Map<String, String> tags;

        public ExportMetadata() {
            this.objectCounts = new HashMap<>();
            this.tags = new HashMap<>();
        }

        public ExportMetadata(String exportId, String exportVersion, String applicationVersion, LocalDateTime exportDate, String exportedBy, String exportedByUsername, String systemVersion, Map<String, Integer> objectCounts, String description, String environment, Map<String, String> tags) {
            this.exportId = exportId;
            this.exportVersion = exportVersion;
            this.applicationVersion = applicationVersion;
            this.exportDate = exportDate;
            this.exportedBy = exportedBy;
            this.exportedByUsername = exportedByUsername;
            this.systemVersion = systemVersion;
            this.objectCounts = objectCounts != null ? objectCounts : new HashMap<>();
            this.description = description;
            this.environment = environment;
            this.tags = tags != null ? tags : new HashMap<>();
        }

        // Getters
        public String getExportId() { return exportId; }
        public String getExportVersion() { return exportVersion; }
        public String getApplicationVersion() { return applicationVersion; }
        public LocalDateTime getExportDate() { return exportDate; }
        public String getExportedBy() { return exportedBy; }
        public String getExportedByUsername() { return exportedByUsername; }
        public String getSystemVersion() { return systemVersion; }
        public Map<String, Integer> getObjectCounts() { return objectCounts; }
        public String getDescription() { return description; }
        public String getEnvironment() { return environment; }
        public Map<String, String> getTags() { return tags; }

        // Setters
        public void setExportId(String exportId) { this.exportId = exportId; }
        public void setExportVersion(String exportVersion) { this.exportVersion = exportVersion; }
        public void setApplicationVersion(String applicationVersion) { this.applicationVersion = applicationVersion; }
        public void setExportDate(LocalDateTime exportDate) { this.exportDate = exportDate; }
        public void setExportedBy(String exportedBy) { this.exportedBy = exportedBy; }
        public void setExportedByUsername(String exportedByUsername) { this.exportedByUsername = exportedByUsername; }
        public void setSystemVersion(String systemVersion) { this.systemVersion = systemVersion; }
        public void setObjectCounts(Map<String, Integer> objectCounts) { this.objectCounts = objectCounts; }
        public void setDescription(String description) { this.description = description; }
        public void setEnvironment(String environment) { this.environment = environment; }
        public void setTags(Map<String, String> tags) { this.tags = tags; }

        // Builder
        public static ExportMetadataBuilder builder() {
            return new ExportMetadataBuilder();
        }

        public static class ExportMetadataBuilder {
            private String exportId;
            private String exportVersion;
            private String applicationVersion;
            private LocalDateTime exportDate;
            private String exportedBy;
            private String exportedByUsername;
            private String systemVersion;
            private Map<String, Integer> objectCounts = new HashMap<>();
            private String description;
            private String environment;
            private Map<String, String> tags = new HashMap<>();

            public ExportMetadataBuilder exportId(String exportId) {
                this.exportId = exportId;
                return this;
            }

            public ExportMetadataBuilder exportVersion(String exportVersion) {
                this.exportVersion = exportVersion;
                return this;
            }

            public ExportMetadataBuilder applicationVersion(String applicationVersion) {
                this.applicationVersion = applicationVersion;
                return this;
            }

            public ExportMetadataBuilder exportDate(LocalDateTime exportDate) {
                this.exportDate = exportDate;
                return this;
            }

            public ExportMetadataBuilder exportedBy(String exportedBy) {
                this.exportedBy = exportedBy;
                return this;
            }

            public ExportMetadataBuilder exportedByUsername(String exportedByUsername) {
                this.exportedByUsername = exportedByUsername;
                return this;
            }

            public ExportMetadataBuilder systemVersion(String systemVersion) {
                this.systemVersion = systemVersion;
                return this;
            }

            public ExportMetadataBuilder objectCounts(Map<String, Integer> objectCounts) {
                this.objectCounts = objectCounts;
                return this;
            }

            public ExportMetadataBuilder description(String description) {
                this.description = description;
                return this;
            }

            public ExportMetadataBuilder environment(String environment) {
                this.environment = environment;
                return this;
            }

            public ExportMetadataBuilder tags(Map<String, String> tags) {
                this.tags = tags;
                return this;
            }

            public ExportMetadata build() {
                return new ExportMetadata(exportId, exportVersion, applicationVersion, exportDate, exportedBy, exportedByUsername, systemVersion, objectCounts, description, environment, tags);
            }
        }
    }

    /**
     * Reference to a certificate used by an adapter
     */
    public static class CertificateReferenceDTO {
        private String certificateId;
        private String certificateName;
        private String certificateType;
        private String usedByAdapterIds;

        public CertificateReferenceDTO() {
        }

        public CertificateReferenceDTO(String certificateId, String certificateName, String certificateType, String usedByAdapterIds) {
            this.certificateId = certificateId;
            this.certificateName = certificateName;
            this.certificateType = certificateType;
            this.usedByAdapterIds = usedByAdapterIds;
        }

        // Getters
        public String getId() { return certificateId; }
        public String getName() { return certificateName; }
        public String getCertificateId() { return certificateId; }
        public String getCertificateName() { return certificateName; }
        public String getCertificateType() { return certificateType; }
        public String getUsedByAdapterIds() { return usedByAdapterIds; }

        // Setters
        public void setCertificateId(String certificateId) { this.certificateId = certificateId; }
        public void setCertificateName(String certificateName) { this.certificateName = certificateName; }
        public void setCertificateType(String certificateType) { this.certificateType = certificateType; }
        public void setUsedByAdapterIds(String usedByAdapterIds) { this.usedByAdapterIds = usedByAdapterIds; }

        // Builder
        public static CertificateReferenceDTOBuilder builder() {
            return new CertificateReferenceDTOBuilder();
        }

        public static class CertificateReferenceDTOBuilder {
            private String certificateId;
            private String certificateName;
            private String certificateType;
            private String usedByAdapterIds;

            public CertificateReferenceDTOBuilder certificateId(String certificateId) {
                this.certificateId = certificateId;
                return this;
            }

            public CertificateReferenceDTOBuilder certificateName(String certificateName) {
                this.certificateName = certificateName;
                return this;
            }

            public CertificateReferenceDTOBuilder certificateType(String certificateType) {
                this.certificateType = certificateType;
                return this;
            }

            public CertificateReferenceDTOBuilder usedByAdapterIds(String usedByAdapterIds) {
                this.usedByAdapterIds = usedByAdapterIds;
                return this;
            }

            public CertificateReferenceDTO build() {
                return new CertificateReferenceDTO(certificateId, certificateName, certificateType, usedByAdapterIds);
            }
        }
    }
}