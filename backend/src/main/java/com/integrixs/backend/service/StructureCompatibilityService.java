package com.integrixs.backend.service;

import com.integrixs.backend.api.dto.request.StructureCompatibilityRequest;
import com.integrixs.backend.api.dto.response.StructureCompatibilityResponse;
import com.integrixs.backend.api.dto.response.CompatibilityIssue;
import com.integrixs.backend.api.dto.response.FieldMapping;
import com.integrixs.backend.api.dto.response.StructureMetadata;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class StructureCompatibilityService {

    private static final Logger log = LoggerFactory.getLogger(StructureCompatibilityService.class);


    private final ObjectMapper objectMapper;

    public StructureCompatibilityService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public StructureCompatibilityResponse analyzeCompatibility(StructureCompatibilityRequest request) {
        try {
            log.info("Analyzing compatibility between {} and {} structures",
                    request.getSourceType(), request.getTargetType());

            // Extract metadata from both structures
            StructureMetadata sourceMetadata = extractMetadata(
                    request.getSourceContent(),
                    request.getSourceType()
           );

            StructureMetadata targetMetadata = extractMetadata(
                    request.getTargetContent(),
                    request.getTargetType()
           );

            // Analyze compatibility
            List<CompatibilityIssue> issues = new ArrayList<>();
            List<FieldMapping> mappings = new ArrayList<>();

            // Check for type mismatches and missing fields
            analyzeFieldCompatibility(sourceMetadata, targetMetadata, issues, mappings);

            // Check namespace compatibility for XML - based structures
            if(isXmlBased(request.getSourceType()) && isXmlBased(request.getTargetType())) {
                analyzeNamespaceCompatibility(sourceMetadata, targetMetadata, issues);
            }

            // Calculate overall compatibility score
            int compatibilityScore = calculateCompatibilityScore(issues, mappings);
            boolean isCompatible = compatibilityScore >= 70 && !hasBlockingErrors(issues);

            // Generate recommendations
            List<String> recommendations = generateRecommendations(issues, mappings);

            return StructureCompatibilityResponse.builder()
                    .overallCompatibility(compatibilityScore)
                    .isCompatible(isCompatible)
                    .issues(issues)
                    .mappings(mappings)
                    .sourceMetadata(sourceMetadata)
                    .targetMetadata(targetMetadata)
                    .recommendations(recommendations)
                    .build();

        } catch(Exception e) {
            log.error("Error analyzing structure compatibility", e);
            return StructureCompatibilityResponse.builder()
                    .overallCompatibility(0)
                    .isCompatible(false)
                    .issues(Arrays.asList(
                            CompatibilityIssue.builder()
                                    .severity(CompatibilityIssue.Severity.ERROR)
                                    .category(CompatibilityIssue.Category.OTHER)
                                    .message("Failed to analyze compatibility: " + e.getMessage())
                                    .build()
                   ))
                    .build();
        }
    }

    private StructureMetadata extractMetadata(String content, String type) throws Exception {
        switch(type) {
            case "WSDL":
                return extractWsdlMetadata(content);
            case "JSON_SCHEMA":
                return extractJsonSchemaMetadata(content);
            case "XSD":
                return extractXsdMetadata(content);
            default:
                throw new IllegalArgumentException("Unsupported structure type: " + type);
        }
    }

    private StructureMetadata extractWsdlMetadata(String wsdl) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new ByteArrayInputStream(wsdl.getBytes(StandardCharsets.UTF_8)));

        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xpath = xPathFactory.newXPath();

        List<StructureMetadata.Field> fields = new ArrayList<>();
        Map<String, String> namespaces = new HashMap<>();

        // Extract namespaces
        NodeList namespaceNodes = (NodeList) xpath.evaluate(
                "//@*[namespace - uri() = 'http://www.w3.org/2000/xmlns/']",
                doc,
                XPathConstants.NODESET
       );

        for(int i = 0; i < namespaceNodes.getLength(); i++) {
            Node node = namespaceNodes.item(i);
            String prefix = node.getLocalName().equals("xmlns") ? "default" : node.getLocalName();
            namespaces.put(prefix, node.getNodeValue());
        }

        // Extract message parts as fields
        NodeList partNodes = (NodeList) xpath.evaluate(
                "//wsdl:message/wsdl:part | //message/part",
                doc,
                XPathConstants.NODESET
       );

        for(int i = 0; i < partNodes.getLength(); i++) {
            Node partNode = partNodes.item(i);
            String name = partNode.getAttributes().getNamedItem("name").getNodeValue();
            String type = getAttributeValue(partNode, "type", "element");

            fields.add(StructureMetadata.Field.builder()
                    .path(name)
                    .type(type)
                    .required(true) // Assume all WSDL parts are required
                    .build());
        }

        return StructureMetadata.builder()
                .fields(fields)
                .namespaces(namespaces)
                .build();
    }

    private StructureMetadata extractJsonSchemaMetadata(String jsonSchema) throws Exception {
        JsonNode schema = objectMapper.readTree(jsonSchema);
        List<StructureMetadata.Field> fields = new ArrayList<>();

        // Extract required fields
        Set<String> requiredFields = new HashSet<>();
        if(schema.has("required") && schema.get("required").isArray()) {
            schema.get("required").forEach(field -> requiredFields.add(field.asText()));
        }

        // Extract properties
        if(schema.has("properties") && schema.get("properties").isObject()) {
            schema.get("properties").fields().forEachRemaining(entry -> {
                String fieldName = entry.getKey();
                JsonNode fieldSchema = entry.getValue();
                String fieldType = fieldSchema.has("type") ? fieldSchema.get("type").asText() : "any";

                fields.add(StructureMetadata.Field.builder()
                        .path(fieldName)
                        .type(fieldType)
                        .required(requiredFields.contains(fieldName))
                        .build());

                // Handle nested objects
                if("object".equals(fieldType) && fieldSchema.has("properties")) {
                    extractNestedFields(fieldName, fieldSchema.get("properties"),
                            requiredFields, fields);
                }
            });
        }

        return StructureMetadata.builder()
                .fields(fields)
                .build();
    }

    private void extractNestedFields(String parentPath, JsonNode properties,
                                     Set<String> requiredFields, List<StructureMetadata.Field> fields) {
        properties.fields().forEachRemaining(entry -> {
            String fieldName = entry.getKey();
            JsonNode fieldSchema = entry.getValue();
            String fieldPath = parentPath + "." + fieldName;
            String fieldType = fieldSchema.has("type") ? fieldSchema.get("type").asText() : "any";

            fields.add(StructureMetadata.Field.builder()
                    .path(fieldPath)
                    .type(fieldType)
                    .required(requiredFields.contains(fieldPath))
                    .build());

            // Recursively handle nested objects
            if("object".equals(fieldType) && fieldSchema.has("properties")) {
                extractNestedFields(fieldPath, fieldSchema.get("properties"),
                        requiredFields, fields);
            }
        });
    }

    private StructureMetadata extractXsdMetadata(String xsd) throws Exception {
        // Similar to WSDL extraction but focusing on XSD elements
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new ByteArrayInputStream(xsd.getBytes(StandardCharsets.UTF_8)));

        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xpath = xPathFactory.newXPath();

        List<StructureMetadata.Field> fields = new ArrayList<>();

        // Extract elements
        NodeList elementNodes = (NodeList) xpath.evaluate(
                "//xs:element | //xsd:element",
                doc,
                XPathConstants.NODESET
       );

        for(int i = 0; i < elementNodes.getLength(); i++) {
            Node elementNode = elementNodes.item(i);
            String name = getAttributeValue(elementNode, "name", null);
            if(name != null) {
                String type = getAttributeValue(elementNode, "type", "string");
                boolean required = "1".equals(getAttributeValue(elementNode, "minOccurs", "1"));

                fields.add(StructureMetadata.Field.builder()
                        .path(name)
                        .type(type)
                        .required(required)
                        .build());
            }
        }

        return StructureMetadata.builder()
                .fields(fields)
                .build();
    }

    private void analyzeFieldCompatibility(StructureMetadata source, StructureMetadata target,
                                           List<CompatibilityIssue> issues, List<FieldMapping> mappings) {
        // Create maps for easier lookup
        Map<String, StructureMetadata.Field> sourceFieldMap = source.getFields().stream()
                .collect(Collectors.toMap(StructureMetadata.Field::getPath, f -> f));
        Map<String, StructureMetadata.Field> targetFieldMap = target.getFields().stream()
                .collect(Collectors.toMap(StructureMetadata.Field::getPath, f -> f));

        // Check source fields
        for(StructureMetadata.Field sourceField : source.getFields()) {
            // Try to find matching target field
            StructureMetadata.Field targetField = findBestMatch(sourceField, target.getFields());

            if(targetField != null) {
                // Found a match, check compatibility
                boolean compatible = areTypesCompatible(sourceField.getType(), targetField.getType());
                boolean transformationRequired = !sourceField.getType().equals(targetField.getType());

                mappings.add(FieldMapping.builder()
                        .sourcePath(sourceField.getPath())
                        .targetPath(targetField.getPath())
                        .sourceType(sourceField.getType())
                        .targetType(targetField.getType())
                        .compatible(compatible)
                        .transformationRequired(transformationRequired)
                        .transformationHint(transformationRequired ?
                                getTransformationHint(sourceField.getType(), targetField.getType()) : null)
                        .build());

                if(!compatible) {
                    issues.add(CompatibilityIssue.builder()
                            .severity(CompatibilityIssue.Severity.ERROR)
                            .category(CompatibilityIssue.Category.TYPE_MISMATCH)
                            .sourcePath(sourceField.getPath())
                            .targetPath(targetField.getPath())
                            .message(String.format("Type mismatch: source is '%s' but target expects '%s'",
                                    sourceField.getType(), targetField.getType()))
                            .suggestion(getTransformationHint(sourceField.getType(), targetField.getType()))
                            .build());
                }
            } else if(sourceField.isRequired()) {
                // Required field has no match
                issues.add(CompatibilityIssue.builder()
                        .severity(CompatibilityIssue.Severity.WARNING)
                        .category(CompatibilityIssue.Category.MISSING_FIELD)
                        .sourcePath(sourceField.getPath())
                        .message("Required field in source has no corresponding field in target")
                        .suggestion("Map to a target field or add new field to target structure")
                        .build());
            }
        }

        // Check for required target fields that have no source
        for(StructureMetadata.Field targetField : target.getFields()) {
            if(targetField.isRequired() && !hasMappingToTarget(targetField.getPath(), mappings)) {
                issues.add(CompatibilityIssue.builder()
                        .severity(CompatibilityIssue.Severity.WARNING)
                        .category(CompatibilityIssue.Category.MISSING_FIELD)
                        .targetPath(targetField.getPath())
                        .message("Required target field has no corresponding source field")
                        .suggestion("Provide a default value or map from another source field")
                        .build());
            }
        }
    }

    private StructureMetadata.Field findBestMatch(StructureMetadata.Field sourceField,
                                                  List<StructureMetadata.Field> targetFields) {
        // First try exact path match
        for(StructureMetadata.Field targetField : targetFields) {
            if(sourceField.getPath().equals(targetField.getPath())) {
                return targetField;
            }
        }

        // Try matching by field name(last part of path)
        String sourceName = getFieldName(sourceField.getPath());
        for(StructureMetadata.Field targetField : targetFields) {
            String targetName = getFieldName(targetField.getPath());
            if(sourceName.equalsIgnoreCase(targetName)) {
                return targetField;
            }
        }

        // Try fuzzy matching(e.g., customerEmail -> customer.email)
        for(StructureMetadata.Field targetField : targetFields) {
            if(isFuzzyMatch(sourceField.getPath(), targetField.getPath())) {
                return targetField;
            }
        }

        return null;
    }

    private boolean areTypesCompatible(String sourceType, String targetType) {
        if(sourceType.equals(targetType)) {
            return true;
        }

        // Check common compatible types
        Map<String, Set<String>> compatibilityMap = new HashMap<>();
        compatibilityMap.put("string", new HashSet<>(Arrays.asList("string", "text")));
        compatibilityMap.put("number", new HashSet<>(Arrays.asList("number", "integer", "float", "double", "decimal")));
        compatibilityMap.put("integer", new HashSet<>(Arrays.asList("number", "integer", "long")));
        compatibilityMap.put("boolean", new HashSet<>(Arrays.asList("boolean", "bool")));
        compatibilityMap.put("array", new HashSet<>(Arrays.asList("array", "list")));
        compatibilityMap.put("object", new HashSet<>(Arrays.asList("object", "map")));

        Set<String> compatibleTypes = compatibilityMap.get(sourceType.toLowerCase());
        return compatibleTypes != null && compatibleTypes.contains(targetType.toLowerCase());
    }

    private String getTransformationHint(String sourceType, String targetType) {
        if(sourceType.equals("number") && targetType.equals("string")) {
            return "Apply number - to - string transformation";
        }
        if(sourceType.equals("string") && targetType.equals("number")) {
            return "Apply string - to - number transformation with error handling";
        }
        if(sourceType.contains("date") || targetType.contains("date")) {
            return "Apply date format transformation";
        }
        if(sourceType.equals("array") && !targetType.equals("array")) {
            return "Extract single value from array or aggregate array values";
        }
        return "Custom transformation required";
    }

    private void analyzeNamespaceCompatibility(StructureMetadata source, StructureMetadata target,
                                               List<CompatibilityIssue> issues) {
        if(source.getNamespaces() == null || target.getNamespaces() == null) {
            return;
        }

        // Check for namespace conflicts
        for(Map.Entry<String, String> sourceNs : source.getNamespaces().entrySet()) {
            String targetNsUri = target.getNamespaces().get(sourceNs.getKey());
            if(targetNsUri != null && !targetNsUri.equals(sourceNs.getValue())) {
                issues.add(CompatibilityIssue.builder()
                        .severity(CompatibilityIssue.Severity.WARNING)
                        .category(CompatibilityIssue.Category.NAMESPACE_ISSUE)
                        .message(String.format("Namespace prefix '%s' has different URIs in source and target",
                                sourceNs.getKey()))
                        .suggestion("Use namespace mapping or transformation")
                        .build());
            }
        }
    }

    private int calculateCompatibilityScore(List<CompatibilityIssue> issues, List<FieldMapping> mappings) {
        if(mappings.isEmpty()) {
            return 0;
        }

        int score = 100;

        // Deduct points for issues
        for(CompatibilityIssue issue : issues) {
            switch(issue.getSeverity()) {
                case ERROR:
                    score -= 15;
                    break;
                case WARNING:
                    score -= 5;
                    break;
                case INFO:
                    score -= 2;
                    break;
            }
        }

        // Consider mapping compatibility
        long incompatibleMappings = mappings.stream()
                .filter(m -> !m.isCompatible())
                .count();
        score -= (int) (incompatibleMappings * 10);

        return Math.max(0, Math.min(100, score));
    }

    private boolean hasBlockingErrors(List<CompatibilityIssue> issues) {
        return issues.stream()
                .anyMatch(issue -> issue.getSeverity() == CompatibilityIssue.Severity.ERROR &&
                                   issue.getCategory() == CompatibilityIssue.Category.TYPE_MISMATCH);
    }

    private List<String> generateRecommendations(List<CompatibilityIssue> issues, List<FieldMapping> mappings) {
        List<String> recommendations = new ArrayList<>();

        // Analyze issue patterns
        Map<CompatibilityIssue.Category, Long> issueCounts = issues.stream()
                .collect(Collectors.groupingBy(CompatibilityIssue::getCategory, Collectors.counting()));

        if(issueCounts.getOrDefault(CompatibilityIssue.Category.TYPE_MISMATCH, 0L) > 3) {
            recommendations.add("Consider implementing a comprehensive type transformation layer");
        }

        if(issueCounts.getOrDefault(CompatibilityIssue.Category.MISSING_FIELD, 0L) > 5) {
            recommendations.add("Review field mapping requirements and consider structural alignment");
        }

        if(issueCounts.containsKey(CompatibilityIssue.Category.NAMESPACE_ISSUE)) {
            recommendations.add("Implement namespace mapping to handle XML namespace differences");
        }

        // Check for common transformation patterns
        long stringToNumberTransformations = mappings.stream()
                .filter(m -> "string".equals(m.getSourceType()) && "number".equals(m.getTargetType()))
                .count();
        if(stringToNumberTransformations > 2) {
            recommendations.add("Implement robust string - to - number conversion with validation");
        }

        return recommendations;
    }

    private boolean isXmlBased(String type) {
        return "WSDL".equals(type) || "XSD".equals(type);
    }

    private String getAttributeValue(Node node, String attributeName, String defaultValue) {
        Node attr = node.getAttributes().getNamedItem(attributeName);
        return attr != null ? attr.getNodeValue() : defaultValue;
    }

    private String getFieldName(String path) {
        int lastDot = path.lastIndexOf('.');
        return lastDot >= 0 ? path.substring(lastDot + 1) : path;
    }

    private boolean isFuzzyMatch(String sourcePath, String targetPath) {
        // Simple fuzzy matching logic
        String sourceName = getFieldName(sourcePath).toLowerCase();
        String targetName = getFieldName(targetPath).toLowerCase();

        // Check if one contains the other
        if(sourceName.contains(targetName) || targetName.contains(sourceName)) {
            return true;
        }

        // Check for common variations
        String[] sourceWords = sourceName.split("(? = [A - Z])|_|-");
        String[] targetWords = targetName.split("(? = [A - Z])|_|-");

        Set<String> sourceWordSet = new HashSet<>(Arrays.asList(sourceWords));
        Set<String> targetWordSet = new HashSet<>(Arrays.asList(targetWords));

        sourceWordSet.retainAll(targetWordSet);
        return !sourceWordSet.isEmpty();
    }

    private boolean hasMappingToTarget(String targetPath, List<FieldMapping> mappings) {
        return mappings.stream()
                .anyMatch(m -> targetPath.equals(m.getTargetPath()));
    }
}
