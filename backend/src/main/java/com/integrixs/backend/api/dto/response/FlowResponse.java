package com.integrixs.backend.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response object for integration flow
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlowResponse {
    
    private String id;
    private String name;
    private String description;
    
    private String inboundAdapterId;
    private String inboundAdapterName;
    private String inboundAdapterType;
    
    private String outboundAdapterId;
    private String outboundAdapterName;
    private String outboundAdapterType;
    
    private String sourceFlowStructureId;
    private String sourceFlowStructureName;
    
    private String targetFlowStructureId;
    private String targetFlowStructureName;
    
    private String status;
    private boolean active;
    
    private Integer executionCount;
    private Integer successCount;
    private Integer errorCount;
    
    private LocalDateTime lastExecutionAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private String createdBy;
}