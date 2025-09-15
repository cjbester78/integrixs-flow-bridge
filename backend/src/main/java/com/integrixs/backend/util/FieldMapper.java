package com.integrixs.backend.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrixs.data.model.FieldMapping;
import com.integrixs.data.model.TransformationCustomFunction;
import com.integrixs.backend.service.DevelopmentFunctionService;
import com.integrixs.backend.service.JavaTransformationEngine;

import java.util.*;
import java.util.stream.Collectors;

public class FieldMapper {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Applies field mappings to input JSON, supports Java functions
     *
     * @param inputJson JSON string input data
     * @param mappings List of FieldMapping objects
     * @param javaTransformationEngine Engine to execute Java functions
     * @return JSON string of mapped output fields
     */
    public static String apply(String inputJson, List<FieldMapping> mappings, JavaTransformationEngine javaTransformationEngine) {
        try {
            // Parse input JSON to a Map
            Map<String, Object> inputMap = objectMapper.readValue(inputJson, new TypeReference<>() {});
            Map<String, Object> outputMap = new LinkedHashMap<>();

            for(FieldMapping mapping : mappings) {
                if(!mapping.isActive()) continue;

                // Parse sourceFields JSON array
                List<String> sourceFields = objectMapper.readValue(
                        mapping.getSourceFields(), new TypeReference<List<String>>() {}
               );

                // Extract values for source fields
                Map<String, Object> sourceData = new HashMap<>();
                for(String field : sourceFields) {
                    sourceData.put(field, inputMap.get(field));
                }

                Object value;
                String functionName = mapping.getJavaFunction();

                // Check if this is a function - based mapping
                if(functionName != null && !functionName.isBlank() && javaTransformationEngine != null) {
                    // Execute Java function
                    Object[] args = sourceFields.stream()
                            .map(sourceData::get)
                            .toArray();
                    value = javaTransformationEngine.executeFunction(functionName, args);
                } else if(mapping.getMappingRule() != null && !mapping.getMappingRule().isBlank()) {
                    // Simple mapping rule fallback: concatenate all non - null values with a space
                    value = sourceFields.stream()
                            .map(sourceData::get)
                            .filter(Objects::nonNull)
                            .map(Object::toString)
                            .collect(Collectors.joining(" "));
                } else {
                    // Direct mapping: take first available field or null if none
                    value = sourceFields.isEmpty() ? null : sourceData.get(sourceFields.get(0));
                }

                outputMap.put(mapping.getTargetField(), value);
            }

            return objectMapper.writeValueAsString(outputMap);

        } catch(Exception e) {
            throw new RuntimeException("Failed to apply field mappings", e);
        }
    }
}
