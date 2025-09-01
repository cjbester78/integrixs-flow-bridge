package com.integrixs.backend.api.dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Request DTO for updating a field mapping
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UpdateFieldMappingRequest extends CreateFieldMappingRequest {
    // Inherits all fields from CreateFieldMappingRequest
    // No additional fields needed for update
}