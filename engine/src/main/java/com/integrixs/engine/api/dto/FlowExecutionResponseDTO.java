package com.integrixs.engine.api.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DTO for flow execution responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlowExecutionResponseDTO {
    private String executionId;
    private String flowId;
    private boolean success;
    private Object processedData;
    private String errorMessage;
    private String errorCode;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    private Long executionTimeMs;
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
    @Builder.Default
    private List<String> warnings = new ArrayList<>();
    private String sourceAdapterId;
    private String targetAdapterId;
    private Integer recordsProcessed;
}