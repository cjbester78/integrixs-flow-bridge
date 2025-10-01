package com.integrixs.engine.domain.service;

import com.integrixs.engine.domain.model.FlowExecutionContext;
import com.integrixs.engine.domain.model.FlowExecutionResult;
import com.integrixs.data.model.IntegrationFlow;
import com.integrixs.data.model.FieldMapping;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Domain service interface for flow execution
 */
public interface FlowExecutionService {

    /**
     * Execute a flow with the given message
     * @param flow The integration flow
     * @param message The input message
     * @param context The execution context
     * @return Execution result
     */
    FlowExecutionResult executeFlow(IntegrationFlow flow, Object message, FlowExecutionContext context);

    /**
     * Execute a flow asynchronously
     * @param flow The integration flow
     * @param message The input message
     * @param context The execution context
     * @return Future with execution result
     */
    CompletableFuture<FlowExecutionResult> executeFlowAsync(IntegrationFlow flow, Object message, FlowExecutionContext context);

    /**
     * Process message through source adapter
     * @param message The input message
     * @param inboundAdapterId Source adapter ID
     * @param context The execution context
     * @return Processed message
     */
    Object processSourceAdapter(Object message, String inboundAdapterId, FlowExecutionContext context);

    /**
     * Apply field mappings to message
     * @param message The message to transform
     * @param mappings The field mappings
     * @param context The execution context
     * @return Transformed message
     */
    Object applyFieldMappings(Object message, List<FieldMapping> mappings, FlowExecutionContext context);

    /**
     * Send message through target adapter
     * @param message The message to send
     * @param outboundAdapterId Target adapter ID
     * @param context The execution context
     */
    void sendToTargetAdapter(Object message, String outboundAdapterId, FlowExecutionContext context);

    /**
     * Validate flow configuration
     * @param flow The integration flow
     * @throws IllegalArgumentException if flow is invalid
     */
    void validateFlow(IntegrationFlow flow);

    /**
     * Check if flow is ready for execution
     * @param flowId The flow ID
     * @return true if ready
     */
    boolean isFlowReady(String flowId);
}
