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
    private List<FlowTransformationDTO> transformations;
    private List<CertificateReferenceDTO> certificateReferences;
    
    // Default constructor
    public FlowExportDTO() {
        this.adapters = new ArrayList<>();
        this.transformations = new ArrayList<>();
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
    public List<CertificateReferenceDTO> getCertificateReferences() { return certificateReferences; }
    
    // Setters
    public void setExportVersion(String exportVersion) { this.exportVersion = exportVersion; }
    public void setExportDate(LocalDateTime exportDate) { this.exportDate = exportDate; }
    public void setMetadata(ExportMetadata metadata) { this.metadata = metadata; }
    public void setFlow(FlowDTO flow) { this.flow = flow; }
    public void setBusinessComponent(BusinessComponentDTO businessComponent) { this.businessComponent = businessComponent; }
    public void setAdapters(List<CommunicationAdapterDTO> adapters) { this.adapters = adapters; }
    public void setTransformations(List<FlowTransformationDTO> transformations) { this.transformations = transformations; }
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
        private String exportedBy;
        private String systemVersion;
        private Map<String, Integer> objectCounts;
        private String description;
        
        public ExportMetadata() {
            this.objectCounts = new HashMap<>();
        }
        
        public ExportMetadata(String exportedBy, String systemVersion, Map<String, Integer> objectCounts, String description) {
            this.exportedBy = exportedBy;
            this.systemVersion = systemVersion;
            this.objectCounts = objectCounts != null ? objectCounts : new HashMap<>();
            this.description = description;
        }
        
        // Getters
        public String getExportedBy() { return exportedBy; }
        public String getSystemVersion() { return systemVersion; }
        public Map<String, Integer> getObjectCounts() { return objectCounts; }
        public String getDescription() { return description; }
        
        // Setters
        public void setExportedBy(String exportedBy) { this.exportedBy = exportedBy; }
        public void setSystemVersion(String systemVersion) { this.systemVersion = systemVersion; }
        public void setObjectCounts(Map<String, Integer> objectCounts) { this.objectCounts = objectCounts; }
        public void setDescription(String description) { this.description = description; }
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
        public String getCertificateId() { return certificateId; }
        public String getCertificateName() { return certificateName; }
        public String getCertificateType() { return certificateType; }
        public String getUsedByAdapterIds() { return usedByAdapterIds; }
        
        // Setters
        public void setCertificateId(String certificateId) { this.certificateId = certificateId; }
        public void setCertificateName(String certificateName) { this.certificateName = certificateName; }
        public void setCertificateType(String certificateType) { this.certificateType = certificateType; }
        public void setUsedByAdapterIds(String usedByAdapterIds) { this.usedByAdapterIds = usedByAdapterIds; }
    }
}