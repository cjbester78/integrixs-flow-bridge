package com.integrixs.backend.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO for router configuration
 */
@Data
@NoArgsConstructor
public class RouterConfigDTO {
    private String routerId;
    private String routerType;
    private List<Map<String, Object>> choices;
    private Map<String, String> contentRoutes;
    private String extractionPath;
    private String sourceType;
    private List<String> recipients;
    private String recipientListVariable;
    private List<String> roundRobinTargets;
    private Map<String, Integer> weightedTargets;
}
