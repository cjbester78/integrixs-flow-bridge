# Flow Execution Service Documentation

## Overview

The Flow Execution Service handles synchronous flow execution for real-time request/response processing, particularly for API integrations (SOAP, REST endpoints). This service has been enhanced with comprehensive XML validation, WSDL sample extraction, and response transformation capabilities.

## Key Components

### 1. FlowExecutionSyncService
Main service that orchestrates the flow execution pipeline:
- Message validation against source structure
- Field mapping transformations
- Adapter execution
- Response transformations

### 2. XmlValidationService
Provides XML validation capabilities:
- Validates XML against FlowStructure (WSDL-based)
- Validates XML against MessageStructure (XSD-based)
- Extracts XSD schemas from WSDL definitions
- Comprehensive error reporting with line numbers

### 3. WsdlSampleExtractorService
Extracts sample XML from WSDL definitions:
- Generates sample request XML for operations
- Generates sample response XML for operations
- Handles both element-based and type-based message parts
- Creates appropriate sample values for simple types

## Features

### XML Validation

#### FlowStructure Validation
```java
// Validate message against FlowStructure (WSDL)
XmlValidationService.ValidationResult result = 
    xmlValidationService.validateMessageAgainstFlowStructure(
        xmlMessage, flowStructure, context
    );

if (!result.isValid()) {
    logger.error("Validation errors: {}", result.getErrors());
}
```

#### MessageStructure Validation
```java
// Validate message against MessageStructure (XSD)
XmlValidationService.ValidationResult result = 
    xmlValidationService.validateMessageAgainstMessageStructure(
        xmlMessage, messageStructure, context
    );
```

### WSDL Sample Extraction

```java
// Extract sample request XML for an operation
String sampleRequest = wsdlSampleExtractor.extractSampleRequestXml(
    wsdlContent, "GetWeather"
);

// Extract sample response XML
String sampleResponse = wsdlSampleExtractor.extractSampleResponseXml(
    wsdlContent, "GetWeather"
);
```

### Response Transformations

The service now supports bidirectional transformations:
1. **Request Transformation**: Source → Target (lowest execution order)
2. **Response Transformation**: Target → Source (higher execution order)

```java
// Response transformation is automatically applied if:
// 1. Flow has WITH_MAPPING mode
// 2. Multiple transformations exist
// 3. Response transformation has execution order > 1
```

## Flow Execution Pipeline

1. **Validation Phase**
   - Validate incoming message against source FlowStructure
   - Extract and apply XSD schemas from WSDL
   - Report validation errors with details

2. **Request Transformation**
   - Apply field mappings if flow has WITH_MAPPING mode
   - Use WSDL sample extraction for target template
   - Preserve namespace mappings

3. **Adapter Execution**
   - Execute target adapter with transformed message
   - Capture response from external system

4. **Response Transformation**
   - Apply response mappings if configured
   - Transform from target format back to source format
   - Maintain namespace consistency

## Configuration

### Context Parameters

The flow execution accepts these context parameters:

```java
Map<String, Object> context = new HashMap<>();
context.put("flowId", flow.getId().toString());
context.put("flowName", flow.getName());
context.put("protocol", "SOAP"); // or "REST"
context.put("headers", headers);
context.put("correlationId", correlationId);
context.put("operationName", "GetWeather"); // For WSDL sample extraction
```

### Validation Modes

Currently, validation errors are logged but processing continues. Future enhancement could add strict validation:

```java
// Potential future configuration
flow.setValidationMode(ValidationMode.STRICT); // Fail on validation error
flow.setValidationMode(ValidationMode.LENIENT); // Log and continue
```

## Error Handling

### Validation Errors
- Detailed error messages with line and column numbers
- XPath locations for complex validation failures
- Namespace-related issues clearly identified

### Transformation Errors
- Fallback to original message if transformation fails
- Comprehensive error logging with stack traces
- Correlation ID tracking throughout pipeline

## Usage Examples

### Basic Flow Execution
```java
String response = flowExecutionSyncService.processMessage(
    integrationFlow,
    requestXml,
    headers,
    "SOAP"
);
```

### With Operation Context
```java
Map<String, String> headers = new HashMap<>();
headers.put("correlationId", UUID.randomUUID().toString());
headers.put("isEndpointFlow", "true");

Map<String, Object> context = new HashMap<>();
context.put("operationName", "CelsiusToFahrenheit");

String response = flowExecutionSyncService.processMessage(
    flow,
    soapRequest,
    headers,
    "SOAP"
);
```

## Best Practices

1. **Always Include Correlation ID**: Helps track message flow through logs
2. **Specify Operation Name**: Enables better WSDL sample extraction
3. **Validate WSDL Content**: Ensure FlowStructures have valid WSDL
4. **Test Transformations**: Verify both request and response mappings
5. **Monitor Validation**: Check logs for validation warnings

## Troubleshooting

### Common Issues

1. **Validation Failures**
   - Check if XML namespaces match WSDL definitions
   - Verify element names and structure
   - Ensure required elements are present

2. **Sample Extraction Issues**
   - Verify operation name matches WSDL
   - Check if WSDL has proper schema definitions
   - Ensure WSDL is well-formed

3. **Transformation Errors**
   - Verify field mappings are correct
   - Check namespace prefixes in XPath expressions
   - Ensure source and target structures align

## Future Enhancements

1. **Strict Validation Mode**: Configuration to fail fast on validation errors
2. **Schema Caching**: Cache parsed schemas for performance
3. **Custom Validators**: Plugin system for business rule validation
4. **Async Validation**: Non-blocking validation for large messages
5. **Validation Reports**: Detailed HTML/PDF validation reports