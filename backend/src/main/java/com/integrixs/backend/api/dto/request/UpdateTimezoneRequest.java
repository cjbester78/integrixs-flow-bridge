package com.integrixs.backend.api.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for updating system timezone
 */
public class UpdateTimezoneRequest {

    @NotBlank(message = "Timezone is required")
    private String timezone;

    // Default constructor
    public UpdateTimezoneRequest() {
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
}
