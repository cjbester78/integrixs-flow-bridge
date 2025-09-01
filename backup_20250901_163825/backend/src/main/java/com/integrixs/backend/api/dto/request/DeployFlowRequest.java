package com.integrixs.backend.api.dto.request;

import lombok.Data;

/**
 * Request DTO for deploying a flow
 */
@Data
public class DeployFlowRequest {
    // Currently empty as flowId comes from path and userId from authentication
    // Reserved for future deployment options
}