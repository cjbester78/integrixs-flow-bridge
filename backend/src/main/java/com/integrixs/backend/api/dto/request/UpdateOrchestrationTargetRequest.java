package com.integrixs.backend.api.dto.request;

import com.integrixs.data.model.OrchestrationTarget;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.util.Map;

/**
 * Request DTO for updating an orchestration target
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrchestrationTargetRequest {
    
    @Min(value = 0, message = "Execution order must be non-negative")
    private Integer executionOrder;
    
    private Boolean parallel;
    
    private String routingCondition;
    
    private OrchestrationTarget.ConditionType conditionType;
    
    private String structureId;
    
    private String responseStructureId;
    
    private Boolean awaitResponse;
    
    @Min(value = 0, message = "Timeout must be non-negative")
    @Max(value = 3600000, message = "Timeout cannot exceed 1 hour")
    private Long timeoutMs;
    
    private RetryPolicyDto retryPolicy;
    
    private OrchestrationTarget.ErrorStrategy errorStrategy;
    
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
        private Integer maxAttempts;
        
        @Min(0)
        @Max(300000)
        private Long retryDelayMs;
        
        @Min(1)
        @Max(10)
        private Double backoffMultiplier;
        
        @Min(0)
        @Max(3600000)
        private Long maxRetryDelayMs;
        
        private String retryOnErrors;
    }
}