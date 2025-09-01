package com.integrixs.adapters.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO for adapter status responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdapterStatusResponseDTO {
    private String adapterId;
    private String adapterName;
    private String adapterType;
    private String adapterMode;
    private boolean isActive;
    private String status;
    private Map<String, Object> metadata;
    private String lastError;
    private Long lastActivityTimestamp;
    private Integer messagesProcessed;
    private Integer errorCount;
}