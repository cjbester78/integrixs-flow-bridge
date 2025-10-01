package com.integrixs.backend.api.controller;

import com.integrixs.backend.api.dto.request.TestConditionRequest;
import com.integrixs.backend.api.dto.response.TestConditionResponse;
import com.integrixs.backend.service.ConditionEvaluationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/conditions")
@Tag(name = "Condition Testing", description = "Test routing conditions")
@PreAuthorize("hasRole('USER')")
public class ConditionTestController {

    private final ConditionEvaluationService conditionEvaluationService;

    public ConditionTestController(ConditionEvaluationService conditionEvaluationService) {
        this.conditionEvaluationService = conditionEvaluationService;
    }

    @PostMapping("/test")
    @Operation(summary = "Test a routing condition",
              description = "Evaluate a routing condition against a test payload")
    public ResponseEntity<TestConditionResponse> testCondition(
            @Valid @RequestBody TestConditionRequest request) {

        TestConditionResponse response = conditionEvaluationService.evaluateCondition(
            request.getCondition(),
            request.getConditionType(),
            request.getPayload()
       );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/validate")
    @Operation(summary = "Validate condition syntax",
              description = "Check if a condition has valid syntax")
    public ResponseEntity<TestConditionResponse> validateCondition(
            @Valid @RequestBody TestConditionRequest request) {

        TestConditionResponse response = conditionEvaluationService.validateCondition(
            request.getCondition(),
            request.getConditionType()
       );

        return ResponseEntity.ok(response);
    }
}
