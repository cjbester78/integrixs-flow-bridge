package com.integrixs.backend.service;

import com.integrixs.backend.api.dto.response.StructureValidationResponse;
import org.springframework.stereotype.Service;

@Service
public class JsonSchemaValidationService {

    public StructureValidationResponse validateJsonSchema(String jsonSchema) {
        // This will be implemented in the next task
        return StructureValidationResponse.builder()
            .valid(true)
            .message("JSON Schema validation will be implemented in the next phase")
            .build();
    }
}
