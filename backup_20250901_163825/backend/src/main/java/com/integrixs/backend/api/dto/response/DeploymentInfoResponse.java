package com.integrixs.backend.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Response DTO for deployment information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeploymentInfoResponse {
    
    private String flowId;
    private String endpoint;
    private LocalDateTime deployedAt;
    private String deployedBy;
    private String status;
    private Map<String, Object> metadata;
}