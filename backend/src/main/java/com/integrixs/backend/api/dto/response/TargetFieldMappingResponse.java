package com.integrixs.backend.api.dto.response;

import com.integrixs.data.model.TargetFieldMapping;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for target field mapping
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TargetFieldMappingResponse {

    private String id;
    private String orchestrationTargetId;
    private String sourceFieldPath;
    private String targetFieldPath;
    private TargetFieldMapping.MappingType mappingType;
    private String transformationExpression;
    private String constantValue;
    private String conditionExpression;
    private String defaultValue;
    private String targetDataType;
    private boolean required;
    private Integer mappingOrder;
    private String visualFlowData;
    private String validationRules;
    private String description;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
