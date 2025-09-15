package com.integrixs.backend.api.dto.response;

import com.integrixs.data.model.OrchestrationTarget;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Response DTO for orchestration target
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrchestrationTargetResponse {

    private String id;
    private String flowId;
    private AdapterSummary targetAdapter;
    private Integer executionOrder;
    private boolean parallel;
    private String routingCondition;
    private OrchestrationTarget.ConditionType conditionType;
    private String structureId;
    private String responseStructureId;
    private boolean awaitResponse;
    private Long timeoutMs;
    private RetryPolicyResponse retryPolicy;
    private OrchestrationTarget.ErrorStrategy errorStrategy;
    private boolean active;
    private Map<String, Object> configuration;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long mappingCount;

    /**
     * Adapter summary
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdapterSummary {
        private String id;
        private String name;
        private String type;
        private String mode;
        private boolean active;
    }

    /**
     * Retry policy response
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RetryPolicyResponse {
        private Integer maxAttempts;
        private Long retryDelayMs;
        private Double backoffMultiplier;
        private Long maxRetryDelayMs;
        private String retryOnErrors;
    }
}
