package com.integrixs.adapters.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for create adapter response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAdapterResponseDTO {
    private String adapterId;
    private boolean success;
    private String message;
    private String errorMessage;
}