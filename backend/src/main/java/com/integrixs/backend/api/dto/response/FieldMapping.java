package com.integrixs.backend.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldMapping {
    
    private String sourcePath;
    private String targetPath;
    private String sourceType;
    private String targetType;
    private boolean compatible;
    private boolean transformationRequired;
    private String transformationHint;
}