package com.integrixs.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import org.xml.sax.InputSource;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Flow Context Service - Manages variables and state across flow execution steps
 * Provides variable storage, expression evaluation, and context manipulation
 */
@Service
public class FlowContextService {

    private static final Logger logger = LoggerFactory.getLogger(FlowContextService.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    // In - memory context storage(can be replaced with Redis for distributed systems)
    private final Map<String, FlowContext> activeContexts = new ConcurrentHashMap<>();

    // Pattern for variable references ${variableName}
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");

    /**
     * Create a new flow context
     */
    public FlowContext createContext(String flowId, String executionId) {
        String contextId = generateContextId(flowId, executionId);
        FlowContext context = new FlowContext(contextId, flowId, executionId);
        activeContexts.put(contextId, context);
        logger.info("Created flow context: {}", contextId);
        return context;
    }

    /**
     * Get an existing flow context
     */
    public FlowContext getContext(String flowId, String executionId) {
        String contextId = generateContextId(flowId, executionId);
        return activeContexts.get(contextId);
    }

    /**
     * Remove a flow context(cleanup after execution)
     */
    public void removeContext(String flowId, String executionId) {
        String contextId = generateContextId(flowId, executionId);
        activeContexts.remove(contextId);
        logger.info("Removed flow context: {}", contextId);
    }

    /**
     * Set a variable in the context
     */
    public void setVariable(FlowContext context, String name, Object value) {
        context.setVariable(name, value);
        logger.debug("Set variable ' {}' in context {}", name, context.getContextId());
    }

    /**
     * Get a variable from the context
     */
    public Object getVariable(FlowContext context, String name) {
        return context.getVariable(name);
    }

    /**
     * Evaluate an expression with variable substitution
     */
    public String evaluateExpression(FlowContext context, String expression) {
        if(expression == null) return null;

        Matcher matcher = VARIABLE_PATTERN.matcher(expression);
        StringBuffer result = new StringBuffer();

        while(matcher.find()) {
            String variableName = matcher.group(1);
            Object value = context.getVariable(variableName);
            String replacement = value != null ? value.toString() : "";
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }

        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * Evaluate a condition expression
     */
    public boolean evaluateCondition(FlowContext context, String condition) {
        if(condition == null || condition.trim().isEmpty()) {
            return true; // Empty condition is always true
        }

        try {
            // First, substitute variables
            String evaluatedCondition = evaluateExpression(context, condition);

            // Simple condition evaluation
            // Support basic operators: ==, !=, <, >, <=, >=, contains, matches

            // Equality check
            if(evaluatedCondition.contains("==")) {
                String[] parts = evaluatedCondition.split("==", 2);
                return parts[0].trim().equals(parts[1].trim());
            }

            // Inequality check
            if(evaluatedCondition.contains("!=")) {
                String[] parts = evaluatedCondition.split("!=", 2);
                return !parts[0].trim().equals(parts[1].trim());
            }

            // Contains check
            if(evaluatedCondition.contains(" contains ")) {
                String[] parts = evaluatedCondition.split(" contains ", 2);
                return parts[0].trim().contains(parts[1].trim());
            }

            // Regex match
            if(evaluatedCondition.contains(" matches ")) {
                String[] parts = evaluatedCondition.split(" matches ", 2);
                return parts[0].trim().matches(parts[1].trim());
            }

            // Numeric comparisons
            if(evaluatedCondition.matches(".*[<>] = ?.*")) {
                return evaluateNumericCondition(evaluatedCondition);
            }

            // Boolean value
            return Boolean.parseBoolean(evaluatedCondition.trim());

        } catch(Exception e) {
            logger.error("Error evaluating condition: {}", condition, e);
            return false;
        }
    }

    /**
     * Extract value from message using XPath
     */
    public String extractValueFromXml(String xml, String xpath) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xml)));

            XPath xPath = XPathFactory.newInstance().newXPath();
            NodeList nodes = (NodeList) xPath.compile(xpath).evaluate(doc, XPathConstants.NODESET);

            if(nodes.getLength() > 0) {
                return nodes.item(0).getTextContent();
            }

            return null;
        } catch(Exception e) {
            logger.error("Error extracting value from XML with XPath: {}", xpath, e);
            return null;
        }
    }

    /**
     * Extract value from JSON using JSONPath
     */
    public String extractValueFromJson(String json, String jsonPath) {
        try {
            JsonNode root = objectMapper.readTree(json);

            // Simple JSONPath implementation
            String[] parts = jsonPath.split("\\.");
            JsonNode current = root;

            for(String part : parts) {
                if(part.startsWith("$")) continue; // Skip root $

                // Handle array notation [index]
                if(part.contains("[") && part.contains("]")) {
                    String fieldName = part.substring(0, part.indexOf("["));
                    String indexStr = part.substring(part.indexOf("[") + 1, part.indexOf("]"));
                    int index = Integer.parseInt(indexStr);

                    current = current.get(fieldName);
                    if(current != null && current.isArray()) {
                        current = current.get(index);
                    }
                } else {
                    current = current.get(part);
                }

                if(current == null) break;
            }

            return current != null ? current.asText() : null;

        } catch(Exception e) {
            logger.error("Error extracting value from JSON with path: {}", jsonPath, e);
            return null;
        }
    }

    /**
     * Set message header
     */
    public void setHeader(FlowContext context, String name, String value) {
        context.setHeader(name, value);
    }

    /**
     * Get message header
     */
    public String getHeader(FlowContext context, String name) {
        return context.getHeader(name);
    }

    /**
     * Set the current message payload
     */
    public void setPayload(FlowContext context, Object payload) {
        context.setPayload(payload);
    }

    /**
     * Get the current message payload
     */
    public Object getPayload(FlowContext context) {
        return context.getPayload();
    }

    /**
     * Create a checkpoint(for rollback support)
     */
    public void createCheckpoint(FlowContext context, String checkpointName) {
        context.createCheckpoint(checkpointName);
    }

    /**
     * Restore from checkpoint
     */
    public void restoreCheckpoint(FlowContext context, String checkpointName) {
        context.restoreCheckpoint(checkpointName);
    }

    // Helper methods

    private String generateContextId(String flowId, String executionId) {
        return flowId + "_" + executionId;
    }

    private boolean evaluateNumericCondition(String condition) {
        try {
            Pattern pattern = Pattern.compile("(. + ?)\\s*([<>] = ?)\\s*(. + )");
            Matcher matcher = pattern.matcher(condition);

            if(matcher.matches()) {
                double left = Double.parseDouble(matcher.group(1).trim());
                String operator = matcher.group(2);
                double right = Double.parseDouble(matcher.group(3).trim());

                switch(operator) {
                    case "<": return left < right;
                    case ">": return left > right;
                    case "<=": return left <= right;
                    case ">=": return left >= right;
                    default: return false;
                }
            }
        } catch(NumberFormatException e) {
            // Not numeric comparison
        }
        return false;
    }

    /**
     * Flow context class
     */
    public static class FlowContext {
        private final String contextId;
        private final String flowId;
        private final String executionId;
        private final LocalDateTime createdAt;
        private final Map<String, Object> variables;
        private final Map<String, String> headers;
        private Object payload;
        private final Map<String, ContextCheckpoint> checkpoints;

        public FlowContext(String contextId, String flowId, String executionId) {
            this.contextId = contextId;
            this.flowId = flowId;
            this.executionId = executionId;
            this.createdAt = LocalDateTime.now();
            this.variables = new ConcurrentHashMap<>();
            this.headers = new ConcurrentHashMap<>();
            this.checkpoints = new ConcurrentHashMap<>();

            // Set default variables
            this.variables.put("flowId", flowId);
            this.variables.put("executionId", executionId);
            this.variables.put("timestamp", System.currentTimeMillis());
        }

        public void setVariable(String name, Object value) {
            variables.put(name, value);
        }

        public Object getVariable(String name) {
            return variables.get(name);
        }

        public Map<String, Object> getAllVariables() {
            return new HashMap<>(variables);
        }

        public void setHeader(String name, String value) {
            headers.put(name, value);
        }

        public String getHeader(String name) {
            return headers.get(name);
        }

        public Map<String, String> getAllHeaders() {
            return new HashMap<>(headers);
        }

        public void setPayload(Object payload) {
            this.payload = payload;
        }

        public Object getPayload() {
            return payload;
        }

        public void createCheckpoint(String name) {
            ContextCheckpoint checkpoint = new ContextCheckpoint();
            checkpoint.variables = new HashMap<>(variables);
            checkpoint.headers = new HashMap<>(headers);
            checkpoint.payload = payload;
            checkpoint.timestamp = LocalDateTime.now();

            checkpoints.put(name, checkpoint);
        }

        public void restoreCheckpoint(String name) {
            ContextCheckpoint checkpoint = checkpoints.get(name);
            if(checkpoint != null) {
                variables.clear();
                variables.putAll(checkpoint.variables);
                headers.clear();
                headers.putAll(checkpoint.headers);
                payload = checkpoint.payload;
            }
        }

        public String getContextId() { return contextId; }
        public String getFlowId() { return flowId; }
        public String getExecutionId() { return executionId; }
        public LocalDateTime getCreatedAt() { return createdAt; }

        private static class ContextCheckpoint {
            Map<String, Object> variables;
            Map<String, String> headers;
            Object payload;
            LocalDateTime timestamp;
        }
    }
}
