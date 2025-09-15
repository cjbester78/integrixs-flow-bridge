package com.integrixs.engine.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DTO for adapter execution responses
 */
@Data
@NoArgsConstructor
public class AdapterExecutionResponseDTO {
    private String executionId;
    private boolean success;
    private Object data;
    private String errorMessage;
    private String errorCode;
    private LocalDateTime timestamp;
    private Long executionTimeMs;
    private Map<String, Object> metadata = new HashMap<>();
    private List<String> warnings = new ArrayList<>();
    private String adapterType;
    private String adapterId;
}
