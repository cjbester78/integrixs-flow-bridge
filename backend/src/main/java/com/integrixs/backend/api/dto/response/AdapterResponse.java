package com.integrixs.backend.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Response object for communication adapter
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdapterResponse {

    private String id;
    private String name;
    private String type;
    private String mode;
    private String direction;
    private String description;

    private String businessComponentId;
    private String businessComponentName;

    private String externalAuthId;
    private String externalAuthName;

    private boolean active;
    private boolean healthy;
    private String status;

    private LocalDateTime lastHealthCheck;
    private LocalDateTime lastTestDate;
    private String lastTestResult;

    private Map<String, Object> configuration;

    private Integer usageCount;
    private LocalDateTime lastUsedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
