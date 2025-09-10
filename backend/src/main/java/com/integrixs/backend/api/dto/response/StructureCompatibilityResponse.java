package com.integrixs.backend.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.ArrayList;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StructureCompatibilityResponse {
    
    private int overallCompatibility;  // 0-100 percentage
    private boolean isCompatible;
    
    @Builder.Default
    private List<CompatibilityIssue> issues = new ArrayList<>();
    
    @Builder.Default
    private List<FieldMapping> mappings = new ArrayList<>();
    
    private StructureMetadata sourceMetadata;
    private StructureMetadata targetMetadata;
    
    @Builder.Default
    private List<String> recommendations = new ArrayList<>();
}