package com.integrixs.backend.domain.service;

import com.integrixs.data.model.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Domain service for flow execution business logic
 */
@Service
public class FlowExecutionService {

    /**
     * Validate that a flow can be executed
     * @param flow The flow to validate
     * @param inboundAdapter The source adapter
     * @param outboundAdapter The target adapter
     * @throws IllegalStateException if flow cannot be executed
     */
    public void validateFlowExecution(IntegrationFlow flow, CommunicationAdapter inboundAdapter, CommunicationAdapter outboundAdapter) {
        if(flow == null) {
            throw new IllegalArgumentException("Flow cannot be null");
        }

        if(!flow.isActive()) {
            throw new IllegalStateException("Cannot execute inactive flow: " + flow.getName());
        }

        if(inboundAdapter == null) {
            throw new IllegalStateException("Source adapter not found for flow: " + flow.getName());
        }

        if(outboundAdapter == null) {
            throw new IllegalStateException("Target adapter not found for flow: " + flow.getName());
        }

        if(!inboundAdapter.isActive()) {
            throw new IllegalStateException("Cannot execute flow: Source adapter '" + inboundAdapter.getName() +
                "' is in a stopped status. Please activate the adapter before using it in a flow.");
        }

        if(!outboundAdapter.isActive()) {
            throw new IllegalStateException("Cannot execute flow: Target adapter '" + outboundAdapter.getName() +
                "' is in a stopped status. Please activate the adapter before using it in a flow.");
        }
    }

    /**
     * Check if the flow should use direct transfer(skip XML conversion)
     * @param flow The flow to check
     * @return true if direct transfer should be used
     */
    public boolean shouldUseDirectTransfer(IntegrationFlow flow) {
        return flow.isSkipXmlConversion();
    }

    /**
     * Check if data is binary and should skip XML conversion
     * @param data The data to check
     * @return true if data is binary
     */
    public boolean isBinaryData(Object data) {
        if(data == null) {
            return false;
        }

        // Check if it's byte array
        if(data instanceof byte[]) {
            byte[] bytes = (byte[]) data;
            return isBinaryContent(bytes);
        }

        // Check if it's a string that looks like binary
        if(data instanceof String) {
            String str = (String) data;
            // Very simple heuristic - check for non - printable characters
            for(char c : str.toCharArray()) {
                if(c < 32 && c != '\t' && c != '\n' && c != '\r') {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Check if byte content is binary
     */
    private boolean isBinaryContent(byte[] content) {
        if(content == null || content.length == 0) {
            return false;
        }

        // Check first few bytes for binary signatures
        // Common binary file signatures
        if(content.length >= 4) {
            // PDF
            if(content[0] == 0x25 && content[1] == 0x50 && content[2] == 0x44 && content[3] == 0x46) {
                return true;
            }
            // ZIP
            if(content[0] == 0x50 && content[1] == 0x4B && content[2] == 0x03 && content[3] == 0x04) {
                return true;
            }
            // JPEG
            if(content[0] == (byte) 0xFF && content[1] == (byte) 0xD8 && content[2] == (byte) 0xFF) {
                return true;
            }
            // PNG
            if(content[0] == (byte) 0x89 && content[1] == 0x50 && content[2] == 0x4E && content[3] == 0x47) {
                return true;
            }
        }

        // Check for null bytes or other binary indicators in first 512 bytes
        int checkLength = Math.min(content.length, 512);
        int nonPrintableCount = 0;

        for(int i = 0; i < checkLength; i++) {
            byte b = content[i];
            if(b == 0 || (b < 32 && b != '\t' && b != '\n' && b != '\r')) {
                nonPrintableCount++;
            }
        }

        // If more than 10% non - printable, consider it binary
        return nonPrintableCount > checkLength * 0.1;
    }

    /**
     * Check if mapping is required for the flow
     * @param flow The flow to check
     * @return true if mapping is required
     */
    public boolean isMappingRequired(IntegrationFlow flow) {
        return flow.getMappingMode() == MappingMode.WITH_MAPPING;
    }

    /**
     * Convert raw data to string format for logging
     * @param rawData The raw data
     * @return String representation
     */
    public String convertRawDataToString(Object rawData) {
        if(rawData == null) {
            return "null";
        }

        if(rawData instanceof byte[]) {
            return new String((byte[]) rawData);
        }

        return rawData.toString();
    }

    /**
     * Build execution context for flow
     * @param correlationId The correlation ID
     * @param flowId The flow ID
     * @return Execution context map
     */
    public Map<String, Object> buildExecutionContext(String correlationId, UUID flowId) {
        return Map.of(
            "correlationId", correlationId,
            "flowId", flowId.toString(),
            "timestamp", System.currentTimeMillis()
       );
    }

    /**
     * Determine the order of transformation execution
     * @param transformations List of transformations
     * @return Ordered list of transformations
     */
    public List<FlowTransformation> orderTransformations(List<FlowTransformation> transformations) {
        // For now, return as - is, but in future could implement ordering logic
        return transformations.stream()
            .filter(FlowTransformation::isActive)
            .toList();
    }

    /**
     * Check if a transformation type is supported
     * @param type The transformation type
     * @return true if supported
     */
    public boolean isTransformationTypeSupported(FlowTransformation.TransformationType type) {
        return type == FlowTransformation.TransformationType.FIELD_MAPPING ||
               type == FlowTransformation.TransformationType.CUSTOM_FUNCTION ||
               type == FlowTransformation.TransformationType.FILTER ||
               type == FlowTransformation.TransformationType.ENRICHMENT ||
               type == FlowTransformation.TransformationType.VALIDATION;
    }
}
