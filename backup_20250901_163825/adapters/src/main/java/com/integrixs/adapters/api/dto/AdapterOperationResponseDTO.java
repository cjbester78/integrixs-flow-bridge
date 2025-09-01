package com.integrixs.adapters.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO for adapter operation responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdapterOperationResponseDTO {
    private String adapterId;
    private boolean success;
    private String message;
    private String errorMessage;
    private Object data;
    private Map<String, Object> metadata;
    private int recordsProcessed;
}