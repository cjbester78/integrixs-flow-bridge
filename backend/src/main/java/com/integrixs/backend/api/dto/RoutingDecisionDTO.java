package com.integrixs.backend.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for routing decision result
 */
@Data
@NoArgsConstructor
public class RoutingDecisionDTO {
    private boolean success;
    private String routeId;
    private String routeName;
    private String targetStep;
    private String errorMessage;
}
