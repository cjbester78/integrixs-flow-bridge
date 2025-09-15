package com.integrixs.backend.api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Response DTO for business component
 */
@Data
@Builder
public class BusinessComponentResponse {
    private String id;
    private String name;
    private String description;
    private String contactEmail;
    private String contactPhone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long associatedAdapterCount;
}
