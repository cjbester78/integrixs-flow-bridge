package com.integrixs.backend.api.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for updating system timezone
 */
@Data
public class UpdateTimezoneRequest {

    @NotBlank(message = "Timezone is required")
    private String timezone;
}
