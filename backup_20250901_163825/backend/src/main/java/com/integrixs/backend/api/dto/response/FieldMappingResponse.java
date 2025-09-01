package com.integrixs.backend.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for field mapping
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldMappingResponse {
    
    private String id;
    private String transformationId;
    private List<String> sourceFields;
    private String targetField;
    private String javaFunction;
    private String mappingRule;
    private String inputTypes;
    private String outputType;
    private String description;
    private String version;
    private String functionName;
    private boolean active;
    private boolean arrayMapping;
    private String arrayContextPath;
    private String sourceXPath;
    private String targetXPath;
    private Integer mappingOrder;
    private Object visualFlowData;
    private Object functionNode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}