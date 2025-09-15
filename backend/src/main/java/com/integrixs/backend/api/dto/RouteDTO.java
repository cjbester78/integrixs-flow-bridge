package com.integrixs.backend.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for flow route
 */
@Data
@NoArgsConstructor
public class RouteDTO {
    private String id;
    private String flowId;
    private String routeName;
    private String sourceStep;
    private String targetStep;
    private Integer priority;
    private String conditionOperator;
    private boolean enabled = true;
}
