package com.integrixs.backend.api.controller;

import com.integrixs.backend.api.dto.request.ValidateStructureRequest;
import com.integrixs.backend.api.dto.response.StructureValidationResponse;
import com.integrixs.backend.service.WsdlValidationService;
import com.integrixs.backend.service.JsonSchemaValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/structures")
@Tag(name = "Structure Validation", description = "Validate WSDL, JSON Schema and other structure formats")
@PreAuthorize("hasRole('USER')")
public class StructureValidationController {

    private final WsdlValidationService wsdlValidationService;
    private final JsonSchemaValidationService jsonSchemaValidationService;

    public StructureValidationController(WsdlValidationService wsdlValidationService,
                                       JsonSchemaValidationService jsonSchemaValidationService) {
        this.wsdlValidationService = wsdlValidationService;
        this.jsonSchemaValidationService = jsonSchemaValidationService;
    }

    @PostMapping("/validate")
    @Operation(summary = "Validate a structure",
              description = "Validate WSDL, JSON Schema or other structure formats with real - time feedback")
    public ResponseEntity<StructureValidationResponse> validateStructure(
            @Valid @RequestBody ValidateStructureRequest request) {

        StructureValidationResponse response;

        switch(request.getStructureType()) {
            case "WSDL":
                WsdlValidationService.ValidationResult wsdlResult =
                    wsdlValidationService.validateWsdl(request.getContent());
                response = convertWsdlResult(wsdlResult);
                break;

            case "JSON_SCHEMA":
                response = jsonSchemaValidationService.validateJsonSchema(request.getContent());
                break;

            case "XSD":
                response = StructureValidationResponse.builder()
                    .valid(false)
                    .message("XSD validation not yet implemented")
                    .build();
                break;

            default:
                response = StructureValidationResponse.builder()
                    .valid(false)
                    .message("Unknown structure type: " + request.getStructureType())
                    .build();
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/wsdl/extract - operations")
    @Operation(summary = "Extract operations from WSDL",
              description = "Parse WSDL and extract available operations and message types")
    public ResponseEntity<StructureValidationResponse> extractWsdlOperations(
            @Valid @RequestBody ValidateStructureRequest request) {

        if(!"WSDL".equals(request.getStructureType())) {
            return ResponseEntity.badRequest().body(
                StructureValidationResponse.builder()
                    .valid(false)
                    .message("This endpoint only accepts WSDL content")
                    .build()
           );
        }

        WsdlValidationService.ValidationResult result =
            wsdlValidationService.validateWsdl(request.getContent());

        return ResponseEntity.ok(convertWsdlResult(result));
    }

    private StructureValidationResponse convertWsdlResult(WsdlValidationService.ValidationResult wsdlResult) {
        StructureValidationResponse response = new StructureValidationResponse();
        response.setValid(wsdlResult.isValid());

        // Convert issues
        response.setIssues(wsdlResult.getIssues().stream()
            .map(issue -> StructureValidationResponse.Issue.builder()
                .type(StructureValidationResponse.IssueType.valueOf(issue.getType()))
                .message(issue.getMessage())
                .line(issue.getLine())
                .column(issue.getColumn())
                .path(issue.getPath())
                .build())
            .toList());

        // Convert metadata if present
        if(wsdlResult.getMetadata() != null) {
            StructureValidationResponse.WsdlMetadata metadata = new StructureValidationResponse.WsdlMetadata();
            metadata.setTargetNamespace(wsdlResult.getMetadata().getTargetNamespace());
            metadata.setVersion(wsdlResult.getMetadata().getVersion());
            metadata.setNamespaces(wsdlResult.getMetadata().getNamespaces());

            // Convert services
            metadata.setServices(wsdlResult.getMetadata().getServices().stream()
                .map(service -> {
                    StructureValidationResponse.ServiceInfo serviceInfo = new StructureValidationResponse.ServiceInfo();
                    serviceInfo.setName(service.getName());
                    serviceInfo.setPorts(service.getPorts().stream()
                        .map(port -> {
                            StructureValidationResponse.PortInfo portInfo = new StructureValidationResponse.PortInfo();
                            portInfo.setName(port.getName());
                            portInfo.setBinding(port.getBinding());
                            portInfo.setAddress(port.getAddress());
                            return portInfo;
                        })
                        .toList());
                    return serviceInfo;
                })
                .toList());

            // Convert port types
            metadata.setPortTypes(wsdlResult.getMetadata().getPortTypes().stream()
                .map(portType -> {
                    StructureValidationResponse.PortTypeInfo portTypeInfo = new StructureValidationResponse.PortTypeInfo();
                    portTypeInfo.setName(portType.getName());
                    portTypeInfo.setOperations(portType.getOperations().stream()
                        .map(op -> {
                            StructureValidationResponse.OperationInfo opInfo = new StructureValidationResponse.OperationInfo();
                            opInfo.setName(op.getName());
                            opInfo.setInputMessage(op.getInputMessage());
                            opInfo.setOutputMessage(op.getOutputMessage());
                            opInfo.setFaultMessages(op.getFaultMessages());
                            return opInfo;
                        })
                        .toList());
                    return portTypeInfo;
                })
                .toList());

            // Convert messages
            metadata.setMessages(wsdlResult.getMetadata().getMessages().stream()
                .map(message -> {
                    StructureValidationResponse.MessageInfo messageInfo = new StructureValidationResponse.MessageInfo();
                    messageInfo.setName(message.getName());
                    messageInfo.setParts(message.getParts().stream()
                        .map(part -> {
                            StructureValidationResponse.PartInfo partInfo = new StructureValidationResponse.PartInfo();
                            partInfo.setName(part.getName());
                            partInfo.setType(part.getType());
                            partInfo.setElement(part.getElement());
                            return partInfo;
                        })
                        .toList());
                    return messageInfo;
                })
                .toList());

            response.setWsdlMetadata(metadata);
        }

        // Generate summary message
        long errorCount = response.getIssues().stream()
            .filter(i -> i.getType() == StructureValidationResponse.IssueType.ERROR)
            .count();
        long warningCount = response.getIssues().stream()
            .filter(i -> i.getType() == StructureValidationResponse.IssueType.WARNING)
            .count();

        if(response.isValid()) {
            response.setMessage("Valid WSDL");
            if(warningCount > 0) {
                response.setMessage(response.getMessage() + " with " + warningCount + " warning(s)");
            }
        } else {
            response.setMessage("Invalid WSDL: " + errorCount + " error(s), " + warningCount + " warning(s)");
        }

        return response;
    }
}
