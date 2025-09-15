package com.integrixs.backend.api.dto.request;

import com.integrixs.data.model.OrchestrationTarget;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.util.Map;

/**
 * Request DTO for creating an orchestration target
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrchestrationTargetRequest {

    @NotBlank(message = "Target adapter ID is required")
    private String targetAdapterId;

    @Min(value = 0, message = "Execution order must be non - negative")
    private Integer executionOrder;

    @Builder.Default
    private boolean parallel = false;

    private String routingCondition;

    @Builder.Default
    private OrchestrationTarget.ConditionType conditionType = OrchestrationTarget.ConditionType.ALWAYS;

    private String structureId;

    private String responseStructureId;

    @Builder.Default
    private boolean awaitResponse = false;

    @Min(value = 0, message = "Timeout must be non - negative")
    @Max(value = 3600000, message = "Timeout cannot exceed 1 hour")
    @Builder.Default
    private Long timeoutMs = 30000L;

    @Builder.Default
    private RetryPolicyDto retryPolicy = RetryPolicyDto.builder().build();

    @Builder.Default
    private OrchestrationTarget.ErrorStrategy errorStrategy = OrchestrationTarget.ErrorStrategy.FAIL_FLOW;

    @Builder.Default
    private boolean active = true;

    private Map<String, Object> configuration;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    /**
     * Retry policy DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RetryPolicyDto {

        @Min(0)
        @Max(10)
        @Builder.Default
        private Integer maxAttempts = 3;

        @Min(0)
        @Max(300000)
        @Builder.Default
        private Long retryDelayMs = 1000L;

        @Min(1)
        @Max(10)
        @Builder.Default
        private Double backoffMultiplier = 2.0;

        @Min(0)
        @Max(3600000)
        @Builder.Default
        private Long maxRetryDelayMs = 60000L;

        private String retryOnErrors;
    }
}
