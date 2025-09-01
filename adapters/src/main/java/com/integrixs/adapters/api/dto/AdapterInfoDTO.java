package com.integrixs.adapters.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for adapter information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdapterInfoDTO {
    private String adapterId;
    private String name;
    private String description;
    private String adapterType;
    private String adapterMode;
    private boolean isActive;
    private String status;
    private Long createdAt;
    private Long updatedAt;
}