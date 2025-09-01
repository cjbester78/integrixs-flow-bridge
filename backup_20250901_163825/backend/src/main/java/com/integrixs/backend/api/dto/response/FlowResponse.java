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
    
    private String sourceAdapterId;
    private String sourceAdapterName;
    private String sourceAdapterType;
    
    private String targetAdapterId;
    private String targetAdapterName;
    private String targetAdapterType;
    
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