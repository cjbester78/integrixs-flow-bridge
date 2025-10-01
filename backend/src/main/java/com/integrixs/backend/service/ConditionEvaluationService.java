package com.integrixs.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrixs.backend.api.dto.response.TestConditionResponse;
import com.integrixs.backend.exception.BusinessException;
import com.integrixs.data.model.RouteCondition;
import com.jayway.jsonpath.JsonPath;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ConditionEvaluationService {

    private static final Logger log = LoggerFactory.getLogger(ConditionEvaluationService.class);


    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ExpressionParser parser = new SpelExpressionParser();

    /**
     * Evaluation condition types - different from RouteCondition.ConditionType
     * These are for runtime evaluation, not storage
     */
    public enum EvaluationConditionType {
        ALWAYS,
        EXPRESSION,
        JSONPATH,
        XPATH,
        REGEX,
        HEADER_MATCH,
        CONTENT_TYPE,
        CUSTOM
    }

    public TestConditionResponse evaluateCondition(String condition, EvaluationConditionType conditionType, Map<String, Object> payload) {
        String testId = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();
        List<TestConditionResponse.ExecutionStep> steps = new ArrayList<>();

        try {
            boolean result = false;
            String error = null;

            switch(conditionType) {
                case ALWAYS:
                    result = true;
                    steps.add(TestConditionResponse.ExecutionStep.builder()
                        .description("Always matches")
                        .result(true)
                        .build());
                    break;

                case EXPRESSION:
                    result = evaluateExpression(condition, payload, steps);
                    break;

                case JSONPATH:
                    result = evaluateJsonPath(condition, payload, steps);
                    break;

                case XPATH:
                    result = evaluateXPath(condition, payload, steps);
                    break;

                case REGEX:
                    result = evaluateRegex(condition, payload, steps);
                    break;

                case HEADER_MATCH:
                    result = evaluateHeaderMatch(condition, payload, steps);
                    break;

                case CONTENT_TYPE:
                    result = evaluateContentType(condition, payload, steps);
                    break;

                case CUSTOM:
                    throw new BusinessException("Custom condition evaluation not implemented");

                default:
                    throw new BusinessException("Unknown condition type: " + conditionType);
            }

            long executionTime = System.currentTimeMillis() - startTime;

            return TestConditionResponse.builder()
                .id(testId)
                .timestamp(Instant.now())
                .condition(condition)
                .conditionType(conditionType.name())
                .result(result)
                .executionTimeMs(executionTime)
                .error(error)
                .details(TestConditionResponse.TestDetails.builder()
                    .evaluatedExpression(condition)
                    .variables(Map.of("payload", payload))
                    .steps(steps)
                    .build())
                .build();

        } catch(Exception e) {
            log.error("Error evaluating condition", e);
            long executionTime = System.currentTimeMillis() - startTime;

            return TestConditionResponse.builder()
                .id(testId)
                .timestamp(Instant.now())
                .condition(condition)
                .conditionType(conditionType.name())
                .result(false)
                .executionTimeMs(executionTime)
                .error(e.getMessage())
                .details(TestConditionResponse.TestDetails.builder()
                    .evaluatedExpression(condition)
                    .variables(Map.of("payload", payload))
                    .steps(steps)
                    .build())
                .build();
        }
    }

    public TestConditionResponse validateCondition(String condition, EvaluationConditionType conditionType) {
        String testId = UUID.randomUUID().toString();
        List<TestConditionResponse.ExecutionStep> steps = new ArrayList<>();

        try {
            boolean valid = false;
            String error = null;

            switch(conditionType) {
                case ALWAYS:
                    valid = true;
                    break;

                case EXPRESSION:
                    parser.parseExpression(condition);
                    valid = true;
                    steps.add(TestConditionResponse.ExecutionStep.builder()
                        .description("Expression syntax is valid")
                        .result("Valid SpEL expression")
                        .build());
                    break;

                case JSONPATH:
                    JsonPath.compile(condition);
                    valid = true;
                    steps.add(TestConditionResponse.ExecutionStep.builder()
                        .description("JSONPath syntax is valid")
                        .result("Valid JSONPath expression")
                        .build());
                    break;

                case REGEX:
                    Pattern.compile(condition);
                    valid = true;
                    steps.add(TestConditionResponse.ExecutionStep.builder()
                        .description("Regex syntax is valid")
                        .result("Valid regular expression")
                        .build());
                    break;

                default:
                    valid = true; // Assume valid for other types
            }

            return TestConditionResponse.builder()
                .id(testId)
                .timestamp(Instant.now())
                .condition(condition)
                .conditionType(conditionType.name())
                .result(valid)
                .executionTimeMs(0L)
                .details(TestConditionResponse.TestDetails.builder()
                    .evaluatedExpression(condition)
                    .steps(steps)
                    .build())
                .build();

        } catch(Exception e) {
            return TestConditionResponse.builder()
                .id(testId)
                .timestamp(Instant.now())
                .condition(condition)
                .conditionType(conditionType.name())
                .result(false)
                .executionTimeMs(0L)
                .error("Invalid syntax: " + e.getMessage())
                .build();
        }
    }

    private boolean evaluateExpression(String condition, Map<String, Object> payload, List<TestConditionResponse.ExecutionStep> steps) {
        try {
            Expression exp = parser.parseExpression(condition);
            EvaluationContext context = new StandardEvaluationContext();
            context.setVariable("payload", payload);
            context.setVariable("headers", payload.get("headers"));

            steps.add(TestConditionResponse.ExecutionStep.builder()
                .description("Parsing SpEL expression")
                .result(condition)
                .build());

            Boolean result = exp.getValue(context, Boolean.class);

            steps.add(TestConditionResponse.ExecutionStep.builder()
                .description("Expression evaluation result")
                .result(result)
                .build());

            return Boolean.TRUE.equals(result);
        } catch(Exception e) {
            steps.add(TestConditionResponse.ExecutionStep.builder()
                .description("Expression evaluation failed")
                .result(e.getMessage())
                .build());
            throw new BusinessException("Expression evaluation failed: " + e.getMessage(), e);
        }
    }

    private boolean evaluateJsonPath(String jsonPath, Map<String, Object> payload, List<TestConditionResponse.ExecutionStep> steps) {
        try {
            String json = objectMapper.writeValueAsString(payload);

            steps.add(TestConditionResponse.ExecutionStep.builder()
                .description("Converting payload to JSON")
                .result("JSON conversion successful")
                .build());

            Object result = JsonPath.read(json, jsonPath);

            steps.add(TestConditionResponse.ExecutionStep.builder()
                .description("JSONPath evaluation")
                .result(result)
                .build());

            // If result is boolean, return it
            if(result instanceof Boolean) {
                return(Boolean) result;
            }

            // If result exists and is not null/empty, consider it a match
            if(result != null) {
                if(result instanceof Collection) {
                    return !((Collection<?>) result).isEmpty();
                }
                return true;
            }

            return false;
        } catch(Exception e) {
            steps.add(TestConditionResponse.ExecutionStep.builder()
                .description("JSONPath evaluation failed")
                .result(e.getMessage())
                .build());
            throw new BusinessException("JSONPath evaluation failed: " + e.getMessage(), e);
        }
    }

    private boolean evaluateXPath(String xpath, Map<String, Object> payload, List<TestConditionResponse.ExecutionStep> steps) {
        try {
            // Convert payload to XML(simplified - in real implementation, use proper XML converter)
            String xml = "<root>" + convertToXml(payload) + "</root>";

            steps.add(TestConditionResponse.ExecutionStep.builder()
                .description("Converting payload to XML")
                .result("XML conversion successful")
                .build());

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xml)));

            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xPath = xPathfactory.newXPath();
            XPathExpression expr = xPath.compile(xpath);

            Object result = expr.evaluate(doc, XPathConstants.BOOLEAN);

            steps.add(TestConditionResponse.ExecutionStep.builder()
                .description("XPath evaluation")
                .result(result)
                .build());

            return(Boolean) result;
        } catch(Exception e) {
            steps.add(TestConditionResponse.ExecutionStep.builder()
                .description("XPath evaluation failed")
                .result(e.getMessage())
                .build());
            throw new BusinessException("XPath evaluation failed: " + e.getMessage(), e);
        }
    }

    private boolean evaluateRegex(String regex, Map<String, Object> payload, List<TestConditionResponse.ExecutionStep> steps) {
        try {
            String payloadStr = objectMapper.writeValueAsString(payload);

            steps.add(TestConditionResponse.ExecutionStep.builder()
                .description("Converting payload to string")
                .result("Payload size: " + payloadStr.length() + " chars")
                .build());

            Pattern pattern = Pattern.compile(regex);
            boolean matches = pattern.matcher(payloadStr).find();

            steps.add(TestConditionResponse.ExecutionStep.builder()
                .description("Regex pattern matching")
                .result(matches ? "Pattern found" : "No match")
                .build());

            return matches;
        } catch(PatternSyntaxException e) {
            throw new BusinessException("Invalid regex pattern: " + e.getMessage(), e);
        } catch(Exception e) {
            throw new BusinessException("Regex evaluation failed: " + e.getMessage(), e);
        }
    }

    private boolean evaluateHeaderMatch(String headerMatch, Map<String, Object> payload, List<TestConditionResponse.ExecutionStep> steps) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, String> headers = (Map<String, String>) payload.get("headers");
            if(headers == null) {
                steps.add(TestConditionResponse.ExecutionStep.builder()
                    .description("No headers found in payload")
                    .result(false)
                    .build());
                return false;
            }

            // Parse header match condition(e.g., "X - Custom - Header: value")
            String[] parts = headerMatch.split(":", 2);
            if(parts.length != 2) {
                throw new BusinessException("Invalid header match format. Expected 'Header: value'");
            }

            String headerName = parts[0].trim();
            String expectedValue = parts[1].trim();
            String actualValue = headers.get(headerName);

            steps.add(TestConditionResponse.ExecutionStep.builder()
                .description("Checking header: " + headerName)
                .result("Expected: " + expectedValue + ", Actual: " + actualValue)
                .build());

            return expectedValue.equals(actualValue);
        } catch(Exception e) {
            throw new BusinessException("Header match evaluation failed: " + e.getMessage(), e);
        }
    }

    private boolean evaluateContentType(String contentType, Map<String, Object> payload, List<TestConditionResponse.ExecutionStep> steps) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, String> headers = (Map<String, String>) payload.get("headers");
            if(headers == null) {
                steps.add(TestConditionResponse.ExecutionStep.builder()
                    .description("No headers found, assuming default content type")
                    .result("application/json")
                    .build());
                return "application/json".equals(contentType);
            }

            String actualContentType = headers.get("Content - Type");
            if(actualContentType == null) {
                actualContentType = "application/json"; // Default
            }

            steps.add(TestConditionResponse.ExecutionStep.builder()
                .description("Content - Type check")
                .result("Expected: " + contentType + ", Actual: " + actualContentType)
                .build());

            return contentType.equals(actualContentType);
        } catch(Exception e) {
            throw new BusinessException("Content type evaluation failed: " + e.getMessage(), e);
        }
    }

    private String convertToXml(Map<String, Object> map) {
        StringBuilder xml = new StringBuilder();
        for(Map.Entry<String, Object> entry : map.entrySet()) {
            xml.append("<").append(entry.getKey()).append(">");
            if(entry.getValue() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> nested = (Map<String, Object>) entry.getValue();
                xml.append(convertToXml(nested));
            } else {
                xml.append(entry.getValue());
            }
            xml.append("</").append(entry.getKey()).append(">");
        }
        return xml.toString();
    }
}
