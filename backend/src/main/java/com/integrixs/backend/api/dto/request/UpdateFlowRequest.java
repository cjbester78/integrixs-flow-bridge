package com.integrixs.backend.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request object for updating an integration flow
 */
public class UpdateFlowRequest {

    @NotBlank(message = "Flow name is required")
    @Size(max = 255, message = "Flow name must not exceed 255 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    private String inboundAdapterId;

    private String outboundAdapterId;

    private String sourceFlowStructureId;

    private String targetFlowStructureId;

    private boolean active;

    // Default constructor
    public UpdateFlowRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInboundAdapterId() {
        return inboundAdapterId;
    }

    public void setInboundAdapterId(String inboundAdapterId) {
        this.inboundAdapterId = inboundAdapterId;
    }

    public String getOutboundAdapterId() {
        return outboundAdapterId;
    }

    public void setOutboundAdapterId(String outboundAdapterId) {
        this.outboundAdapterId = outboundAdapterId;
    }

    public String getSourceFlowStructureId() {
        return sourceFlowStructureId;
    }

    public void setSourceFlowStructureId(String sourceFlowStructureId) {
        this.sourceFlowStructureId = sourceFlowStructureId;
    }

    public String getTargetFlowStructureId() {
        return targetFlowStructureId;
    }

    public void setTargetFlowStructureId(String targetFlowStructureId) {
        this.targetFlowStructureId = targetFlowStructureId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
