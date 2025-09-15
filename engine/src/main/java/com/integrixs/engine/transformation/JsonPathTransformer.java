package com.integrixs.engine.transformation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.*;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for performing JSONPath - based transformations on JSON data
 */
@Service
public class JsonPathTransformer {

    private static final Logger logger = LoggerFactory.getLogger(JsonPathTransformer.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Configuration for JsonPath
    private final Configuration jsonPathConfig;

    // Cache for compiled JSONPath expressions
    private final Map<String, JsonPath> pathCache = new ConcurrentHashMap<>();

    public JsonPathTransformer() {
        // Configure JsonPath to use Jackson
        this.jsonPathConfig = Configuration.builder()
                .jsonProvider(new JacksonJsonProvider())
                .mappingProvider(new JacksonMappingProvider())
                .options(Option.DEFAULT_PATH_LEAF_TO_NULL)
                .build();
    }

    /**
     * Extract value from JSON using JSONPath expression
     */
    public Object extractValue(String json, String jsonPath) {
        try {
            JsonPath compiledPath = getCompiledPath(jsonPath);
            DocumentContext context = JsonPath.using(jsonPathConfig).parse(json);
            return context.read(compiledPath);
        } catch(Exception e) {
            logger.error("Error extracting value with JSONPath {}: {}", jsonPath, e.getMessage());
            return null;
        }
    }

    /**
     * Extract multiple values from JSON using multiple JSONPath expressions
     */
    public Map<String, Object> extractValues(String json, Map<String, String> pathMappings) {
        Map<String, Object> results = new ConcurrentHashMap<>();
        DocumentContext context = JsonPath.using(jsonPathConfig).parse(json);

        for(Map.Entry<String, String> entry : pathMappings.entrySet()) {
            String key = entry.getKey();
            String jsonPath = entry.getValue();

            try {
                JsonPath compiledPath = getCompiledPath(jsonPath);
                Object value = context.read(compiledPath);
                results.put(key, value);
            } catch(Exception e) {
                logger.error("Error extracting value for key {} with path {}: {}",
                    key, jsonPath, e.getMessage());
                results.put(key, null);
            }
        }

        return results;
    }

    /**
     * Transform JSON by applying JSONPath - based mappings
     */
    public String transformJson(String sourceJson, List<TransformationRule> rules) {
        try {
            DocumentContext sourceContext = JsonPath.using(jsonPathConfig).parse(sourceJson);
            ObjectNode targetNode = objectMapper.createObjectNode();

            for(TransformationRule rule : rules) {
                applyTransformationRule(sourceContext, targetNode, rule);
            }

            return objectMapper.writeValueAsString(targetNode);
        } catch(Exception e) {
            logger.error("Error transforming JSON: {}", e.getMessage());
            throw new RuntimeException("JSON transformation failed", e);
        }
    }

    /**
     * Apply a single transformation rule
     */
    private void applyTransformationRule(DocumentContext sourceContext, ObjectNode targetNode,
                                       TransformationRule rule) {
        try {
            // Extract value from source
            Object value = sourceContext.read(getCompiledPath(rule.getSourcePath()));

            // Apply transformation if specified
            if(rule.getTransformFunction() != null) {
                value = rule.getTransformFunction().apply(value);
            }

            // Set value in target
            setValueByPath(targetNode, rule.getTargetPath(), value);

        } catch(PathNotFoundException e) {
            if(!rule.isOptional()) {
                logger.error("Required path not found: {}", rule.getSourcePath());
                throw new RuntimeException("Required path not found: " + rule.getSourcePath());
            }
            // For optional paths, we just skip
        } catch(Exception e) {
            logger.error("Error applying transformation rule: {}", e.getMessage());
            throw new RuntimeException("Transformation rule failed", e);
        }
    }

    /**
     * Set value in JSON node by path
     */
    private void setValueByPath(ObjectNode rootNode, String path, Object value) {
        String[] segments = path.split("\\.");
        ObjectNode currentNode = rootNode;

        for(int i = 0; i < segments.length - 1; i++) {
            String segment = segments[i];

            if(segment.endsWith("[]")) {
                // Array notation
                String arrayName = segment.substring(0, segment.length() - 2);
                if(!currentNode.has(arrayName)) {
                    currentNode.putArray(arrayName);
                }
                // For simplicity, we'll add to array
                ArrayNode arrayNode = (ArrayNode) currentNode.get(arrayName);
                ObjectNode newNode = objectMapper.createObjectNode();
                arrayNode.add(newNode);
                currentNode = newNode;
            } else {
                // Object notation
                if(!currentNode.has(segment)) {
                    currentNode.putObject(segment);
                }
                currentNode = (ObjectNode) currentNode.get(segment);
            }
        }

        // Set the final value
        String finalSegment = segments[segments.length - 1];
        setNodeValue(currentNode, finalSegment, value);
    }

    /**
     * Set value on a node based on type
     */
    private void setNodeValue(ObjectNode node, String fieldName, Object value) {
        if(value == null) {
            node.putNull(fieldName);
        } else if(value instanceof String) {
            node.put(fieldName, (String) value);
        } else if(value instanceof Boolean) {
            node.put(fieldName, (Boolean) value);
        } else if(value instanceof Integer) {
            node.put(fieldName, (Integer) value);
        } else if(value instanceof Long) {
            node.put(fieldName, (Long) value);
        } else if(value instanceof Double) {
            node.put(fieldName, (Double) value);
        } else if(value instanceof List) {
            ArrayNode arrayNode = node.putArray(fieldName);
            for(Object item : (List<?>) value) {
                addArrayValue(arrayNode, item);
            }
        } else if(value instanceof Map) {
            ObjectNode objectNode = node.putObject(fieldName);
            Map<?, ?> map = (Map<?, ?>) value;
            for(Map.Entry<?, ?> entry : map.entrySet()) {
                setNodeValue(objectNode, entry.getKey().toString(), entry.getValue());
            }
        } else if(value instanceof JsonNode) {
            node.set(fieldName, (JsonNode) value);
        } else {
            // Convert to string as fallback
            node.put(fieldName, value.toString());
        }
    }

    /**
     * Add value to array node
     */
    private void addArrayValue(ArrayNode arrayNode, Object value) {
        if(value == null) {
            arrayNode.addNull();
        } else if(value instanceof String) {
            arrayNode.add((String) value);
        } else if(value instanceof Boolean) {
            arrayNode.add((Boolean) value);
        } else if(value instanceof Integer) {
            arrayNode.add((Integer) value);
        } else if(value instanceof Long) {
            arrayNode.add((Long) value);
        } else if(value instanceof Double) {
            arrayNode.add((Double) value);
        } else if(value instanceof JsonNode) {
            arrayNode.add((JsonNode) value);
        } else {
            arrayNode.add(value.toString());
        }
    }

    /**
     * Get compiled JSONPath from cache or compile new one
     */
    private JsonPath getCompiledPath(String jsonPath) {
        return pathCache.computeIfAbsent(jsonPath, path -> JsonPath.compile(path));
    }

    /**
     * Clear the path cache
     */
    public void clearCache() {
        pathCache.clear();
    }

    /**
     * Validate a JSONPath expression
     */
    public boolean isValidPath(String jsonPath) {
        try {
            JsonPath.compile(jsonPath);
            return true;
        } catch(Exception e) {
            return false;
        }
    }

    /**
     * Transformation rule class
     */
    public static class TransformationRule {
        private String sourcePath;
        private String targetPath;
        private boolean optional;
        private java.util.function.Function<Object, Object> transformFunction;

        public TransformationRule(String sourcePath, String targetPath) {
            this(sourcePath, targetPath, false, null);
        }

        public TransformationRule(String sourcePath, String targetPath, boolean optional) {
            this(sourcePath, targetPath, optional, null);
        }

        public TransformationRule(String sourcePath, String targetPath, boolean optional,
                                java.util.function.Function<Object, Object> transformFunction) {
            this.sourcePath = sourcePath;
            this.targetPath = targetPath;
            this.optional = optional;
            this.transformFunction = transformFunction;
        }

        // Getters
        public String getSourcePath() { return sourcePath; }
        public String getTargetPath() { return targetPath; }
        public boolean isOptional() { return optional; }
        public java.util.function.Function<Object, Object> getTransformFunction() { return transformFunction; }
    }
}
