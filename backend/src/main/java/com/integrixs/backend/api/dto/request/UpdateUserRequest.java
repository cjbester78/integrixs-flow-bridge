package com.integrixs.backend.api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.util.Map;

/**
 * Request DTO for updating user information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    @Email(message = "Email must be valid")
    private String email;

    @Size(max = 100, message = "First name cannot exceed 100 characters")
    private String firstName;

    @Size(max = 100, message = "Last name cannot exceed 100 characters")
    private String lastName;

    @Pattern(regexp = "^(ADMINISTRATOR|DEVELOPER|INTEGRATOR|VIEWER)$",
            message = "Role must be one of: ADMINISTRATOR, DEVELOPER, INTEGRATOR, VIEWER")
    private String role;

    private String status;

    private Map<String, Object> permissions;
}
