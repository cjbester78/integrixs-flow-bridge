package com.integrixs.backend.api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request object for creating an integration flow
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateFlowRequest {

    @NotBlank(message = "Flow name is required")
    @Size(max = 255, message = "Flow name must not exceed 255 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    private String inboundAdapterId;

    private String outboundAdapterId;

    private String sourceFlowStructureId;

    private String targetFlowStructureId;

    @Builder.Default
    private boolean active = true;
}
