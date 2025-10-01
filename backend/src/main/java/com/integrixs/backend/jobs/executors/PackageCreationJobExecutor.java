package com.integrixs.backend.jobs.executors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrixs.backend.dto.PackageCreationRequest;
import com.integrixs.backend.dto.PackageCreationResult;
import com.integrixs.backend.jobs.BackgroundJob;
import com.integrixs.backend.jobs.JobExecutor;
import com.integrixs.backend.service.TransactionalPackageCreationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Job executor for package creation
 */
@Component
public class PackageCreationJobExecutor implements JobExecutor {

    private static final Logger logger = LoggerFactory.getLogger(PackageCreationJobExecutor.class);

    public static final String JOB_TYPE = "PACKAGE_CREATION";

    @Autowired
    private TransactionalPackageCreationService packageCreationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public String getJobType() {
        return JOB_TYPE;
    }

    @Override
    public Map<String, String> execute(BackgroundJob job, ProgressCallback progressCallback) throws Exception {
        logger.info("Starting package creation job: {}", job.getId());

        // Extract parameters
        String parametersJson = job.getParameters();
        if(parametersJson == null) {
            throw new IllegalArgumentException("Missing parameters");
        }

        // Deserialize parameters to get request JSON
        Map<String, String> params = objectMapper.readValue(parametersJson, new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>() {});
        String requestJson = params.get("request");
        if(requestJson == null) {
            throw new IllegalArgumentException("Missing request parameter");
        }

        // Deserialize request
        PackageCreationRequest request = objectMapper.readValue(requestJson, PackageCreationRequest.class);

        // Set user and tenant from job
        if(request.getUserId() == null && job.getCreatedBy() != null) {
            request.setUserId(job.getCreatedBy().getId());
        }
        if(request.getTenantId() == null && job.getTenantId() != null) {
            request.setTenantId(job.getTenantId());
        }

        // Report initial progress
        progressCallback.updateProgress(5, "Starting package creation");

        // Create custom progress wrapper that updates job progress
        PackageCreationResult result = packageCreationService.createPackage(request);

        // Build result map
        Map<String, String> results = new HashMap<>();
        results.put("success", String.valueOf(result.isSuccess()));
        results.put("flowId", result.getFlowId() != null ? result.getFlowId().toString() : null);
        results.put("flowName", result.getFlowName());
        results.put("correlationId", result.getCorrelationId().toString());
        results.put("message", result.getMessage());

        if(!result.isSuccess()) {
            results.put("error", result.getErrorMessage());
        }

        // Store detailed result as JSON
        results.put("details", objectMapper.writeValueAsString(result));

        progressCallback.updateProgress(100, "Package creation completed");

        return results;
    }

    @Override
    public void validateParameters(Map<String, String> parameters) throws IllegalArgumentException {
        if(!parameters.containsKey("request")) {
            throw new IllegalArgumentException("Missing required parameter: request");
        }

        // Try to parse the request to validate JSON
        try {
            objectMapper.readValue(parameters.get("request"), PackageCreationRequest.class);
        } catch(Exception e) {
            throw new IllegalArgumentException("Invalid request JSON: " + e.getMessage());
        }
    }

    @Override
    public Long getEstimatedDuration() {
        // Estimate 30 seconds to 2 minutes for package creation
        return 60000L; // 1 minute
    }

    @Override
    public boolean isRetryable() {
        // Package creation can be retried on failure
        return true;
    }

    /**
     * Helper method to create job parameters for package creation
     */
    public static Map<String, String> createJobParameters(PackageCreationRequest request, ObjectMapper objectMapper) throws Exception {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("request", objectMapper.writeValueAsString(request));
        parameters.put("flowName", request.getFlowName());
        parameters.put("flowType", request.getFlowType());
        return parameters;
    }
}
