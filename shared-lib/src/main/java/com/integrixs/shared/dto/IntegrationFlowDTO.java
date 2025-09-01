package com.integrixs.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for IntegrationFlow entity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntegrationFlowDTO {
    private String id;
    private String name;
    private String description;
    private String inboundAdapterId;
    private String outboundAdapterId;
    private String sourceFlowStructureId;
    private String targetFlowStructureId;
    private String status;
    private boolean isActive;
    private String mappingMode; // WITH_MAPPING or PASS_THROUGH
    private boolean skipXmlConversion; // Skip XML conversion for direct passthrough
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private LocalDateTime lastExecutionAt;
    private Integer executionCount;
    private Integer successCount;
    private Integer errorCount;
    private String businessComponentId;
    
    // Adapter details
    private String inboundAdapterName;
    private String inboundAdapterType;
    private String outboundAdapterName;
    private String outboundAdapterType;
}