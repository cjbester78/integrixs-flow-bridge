package com.integrixs.backend.api.dto.request;

/**
 * Request DTO for updating a field mapping
 */
public class UpdateFieldMappingRequest extends CreateFieldMappingRequest {
    // Inherits all fields from CreateFieldMappingRequest
    // No additional fields needed for update

    // Default constructor
    public UpdateFieldMappingRequest() {
    }

    @Override
    public boolean equals(Object o) {
        // Since this class adds no new fields, we can use the parent's equals
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        // Since this class adds no new fields, we can use the parent's hashCode
        return super.hashCode();
    }
}
