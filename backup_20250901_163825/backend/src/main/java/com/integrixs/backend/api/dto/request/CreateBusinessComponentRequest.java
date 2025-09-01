package com.integrixs.backend.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request DTO for creating a business component
 */
@Data
public class CreateBusinessComponentRequest {
    
    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    @Email(message = "Invalid email format")
    private String contactEmail;
    
    @Pattern(regexp = "^[+]?[0-9\\-\\s()]+$", message = "Invalid phone format")
    @Size(max = 50, message = "Phone number must not exceed 50 characters")
    private String contactPhone;
}