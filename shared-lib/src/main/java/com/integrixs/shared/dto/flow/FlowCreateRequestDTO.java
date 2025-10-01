package com.integrixs.shared.dto.flow;

import com.integrixs.shared.validation.ValidFlowConfiguration;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for creating a new integration flow.
 *
 * <p>Contains all required information to create a new flow including
 * source/target adapters and transformation configurations.
 *
 * @author Integration Team
 * @since 1.0.0
 */
public class FlowCreateRequestDTO {

    private String name;
    private String description;
    private String inboundAdapterId;
    private String outboundAdapterId;
    private String configuration;
    private String createdBy;
    private List<FlowTransformationDTO> transformations = new ArrayList<>();
    private String status = "INACTIVE";
    private boolean activateOnCreation = false;

    // Default constructor
    public FlowCreateRequestDTO() {
    }

    // All args constructor
    public FlowCreateRequestDTO(String name, String description, String inboundAdapterId, String outboundAdapterId, String configuration, String createdBy, List<FlowTransformationDTO> transformations, String status, boolean activateOnCreation) {
        this.name = name;
        this.description = description;
        this.inboundAdapterId = inboundAdapterId;
        this.outboundAdapterId = outboundAdapterId;
        this.configuration = configuration;
        this.createdBy = createdBy;
        this.transformations = transformations != null ? transformations : new ArrayList<>();
        this.status = status;
        this.activateOnCreation = activateOnCreation;
    }

    // Getters
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getInboundAdapterId() { return inboundAdapterId; }
    public String getOutboundAdapterId() { return outboundAdapterId; }
    public String getConfiguration() { return configuration; }
    public String getCreatedBy() { return createdBy; }
    public List<FlowTransformationDTO> getTransformations() { return transformations; }
    public String getStatus() { return status; }
    public boolean isActivateOnCreation() { return activateOnCreation; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setInboundAdapterId(String inboundAdapterId) { this.inboundAdapterId = inboundAdapterId; }
    public void setOutboundAdapterId(String outboundAdapterId) { this.outboundAdapterId = outboundAdapterId; }
    public void setConfiguration(String configuration) { this.configuration = configuration; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public void setTransformations(List<FlowTransformationDTO> transformations) { this.transformations = transformations; }
    public void setStatus(String status) { this.status = status; }
    public void setActivateOnCreation(boolean activateOnCreation) { this.activateOnCreation = activateOnCreation; }

    // Builder
    public static FlowCreateRequestDTOBuilder builder() {
        return new FlowCreateRequestDTOBuilder();
    }

    public static class FlowCreateRequestDTOBuilder {
        private String name;
        private String description;
        private String inboundAdapterId;
        private String outboundAdapterId;
        private String configuration;
        private String createdBy;
        private List<FlowTransformationDTO> transformations = new ArrayList<>();
        private String status = "INACTIVE";
        private boolean activateOnCreation = false;

        public FlowCreateRequestDTOBuilder name(String name) {
            this.name = name;
            return this;
        }

        public FlowCreateRequestDTOBuilder description(String description) {
            this.description = description;
            return this;
        }

        public FlowCreateRequestDTOBuilder inboundAdapterId(String inboundAdapterId) {
            this.inboundAdapterId = inboundAdapterId;
            return this;
        }

        public FlowCreateRequestDTOBuilder outboundAdapterId(String outboundAdapterId) {
            this.outboundAdapterId = outboundAdapterId;
            return this;
        }

        public FlowCreateRequestDTOBuilder configuration(String configuration) {
            this.configuration = configuration;
            return this;
        }

        public FlowCreateRequestDTOBuilder createdBy(String createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public FlowCreateRequestDTOBuilder transformations(List<FlowTransformationDTO> transformations) {
            this.transformations = transformations;
            return this;
        }

        public FlowCreateRequestDTOBuilder status(String status) {
            this.status = status;
            return this;
        }

        public FlowCreateRequestDTOBuilder activateOnCreation(boolean activateOnCreation) {
            this.activateOnCreation = activateOnCreation;
            return this;
        }

        public FlowCreateRequestDTO build() {
            return new FlowCreateRequestDTO(name, description, inboundAdapterId, outboundAdapterId, configuration, createdBy, transformations, status, activateOnCreation);
        }
    }
}
