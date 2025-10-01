package com.integrixs.engine.mapper;

import com.integrixs.data.model.FieldMapping;
import com.integrixs.data.model.TransformationCustomFunction;
import com.integrixs.data.sql.repository.TransformationCustomFunctionSqlRepository;
import com.integrixs.engine.transformation.TransformationFunctionExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;
import java.util.Arrays;

/**
 * Service for hierarchical XML field mapping supporting XPath expressions and array structures
 */
@Service
public class HierarchicalXmlFieldMapper {

    private static final Logger logger = LoggerFactory.getLogger(HierarchicalXmlFieldMapper.class);

    @Autowired
    private TransformationFunctionExecutor functionExecutor;

    @Autowired
    private TransformationCustomFunctionSqlRepository functionRepository;

    /**
     * Map source XML to target XML using field mappings
     *
     * @param sourceXml Source XML document as string
     * @param targetXmlTemplate Target XML template(optional)
     * @param fieldMappings List of field mappings
     * @param namespaces Map of namespace prefixes to URIs
     * @return Mapped target XML as string
     * @throws Exception if mapping fails
     */
    public String mapXmlFields(String sourceXml, String targetXmlTemplate,
                              List<FieldMapping> fieldMappings,
                              Map<String, String> namespaces) throws Exception {

        logger.info("Starting XML field mapping with {} mappings", fieldMappings.size());
        logger.debug("Source XML: {}", sourceXml);
        logger.debug("Target template: {}", targetXmlTemplate);

        // Parse source XML
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document sourceDoc = builder.parse(new InputSource(new StringReader(sourceXml)));

        // Create or parse target document
        Document targetDoc;

        // Check if we need to create a SOAP envelope structure
        boolean needsSoapEnvelope = false;

        // Check if we have a SOAP/WSDL namespace that indicates we need a SOAP envelope
        if(namespaces != null && !namespaces.isEmpty()) {
            logger.debug("Checking {} namespaces for SOAP indicators", namespaces.size());
            for(Map.Entry<String, String> entry : namespaces.entrySet()) {
                String prefix = entry.getKey();
                String uri = entry.getValue();
                logger.trace("Checking namespace {} = {}", prefix, uri);
                if(uri.contains("w3schools.com") || uri.contains("www.w3schools.com") ||
                    uri.contains("webserviceX") || uri.contains("tempuri.org") ||
                    uri.contains("/soap/")) {
                    needsSoapEnvelope = true;
                    logger.info("Detected SOAP web service namespace '{}' = '{}', will create SOAP envelope", prefix, uri);
                    break;
                }
            }
            logger.debug("SOAP envelope needed: {}", needsSoapEnvelope);
        } else {
            logger.debug("No namespaces provided for SOAP detection");
        }

        // Also check if source is already SOAP
        if(!needsSoapEnvelope && (sourceXml.contains("http://schemas.xmlsoap.org/soap/envelope/") ||
            sourceXml.contains("soap:Envelope") ||
            sourceXml.contains("soapenv:Envelope"))) {
            needsSoapEnvelope = true;
            logger.info("Detected SOAP in source XML, will create SOAP envelope");
        }

        // Now decide how to create the target document
        logger.debug("Target template is: {}", targetXmlTemplate == null ? "null" :
                    (targetXmlTemplate.isEmpty() ? "empty" : "provided"));
        logger.debug("Need SOAP envelope: {}", needsSoapEnvelope);

        if(targetXmlTemplate != null && !targetXmlTemplate.isEmpty()) {
            logger.debug("Using provided target template");
            targetDoc = builder.parse(new InputSource(new StringReader(targetXmlTemplate)));
        } else if(needsSoapEnvelope) {
            logger.debug("Creating SOAP envelope structure for target");
            targetDoc = builder.newDocument();

            // Get the target namespace from passed namespaces
            String targetNamespace = null;
            String targetPrefix = null;

            // Find the target namespace and prefix from passed namespaces
            if(namespaces != null) {
                // First, check if we have a "tns" prefix - this is typically the service namespace
                if(namespaces.containsKey("tns")) {
                    targetPrefix = "tns";
                    targetNamespace = namespaces.get("tns");
                    logger.debug("Using service namespace(tns): {} = {}", targetPrefix, targetNamespace);
                } else {
                    // Otherwise look for a non - standard namespace
                    for(Map.Entry<String, String> entry : namespaces.entrySet()) {
                        String ns = entry.getValue();
                        String prefix = entry.getKey();
                        // Look for the non - SOAP namespace that will be our target
                        // Skip standard namespaces like xsd, wsdl, etc.
                        if(!ns.contains("schemas.xmlsoap.org") &&
                            !ns.contains("www.w3.org/2001/XMLSchema") &&
                            !ns.contains("schemas.xmlsoap.org/wsdl") &&
                            !prefix.equals("xsd") &&
                            !prefix.equals("wsdl") &&
                            !prefix.equals("s") &&
                            !prefix.isEmpty()) {
                            targetNamespace = ns;
                            targetPrefix = prefix;
                            logger.debug("Found target namespace: {} = {}", targetPrefix, targetNamespace);
                            break;
                        }
                    }
                }
            }


            // Create basic SOAP envelope
            Element envelope = targetDoc.createElementNS("http://schemas.xmlsoap.org/soap/envelope/", "soapenv:Envelope");
            envelope.setAttribute("xmlns:soapenv", "http://schemas.xmlsoap.org/soap/envelope/");

            // Add target namespace if found
            if(targetNamespace != null && targetPrefix != null) {
                envelope.setAttribute("xmlns:" + targetPrefix, targetNamespace);
            }

            Element body = targetDoc.createElementNS("http://schemas.xmlsoap.org/soap/envelope/", "soapenv:Body");
            envelope.appendChild(body);
            targetDoc.appendChild(envelope);

            logger.debug("Created SOAP envelope structure");
        } else {
            logger.debug("No target template provided, creating basic structure");
            targetDoc = builder.newDocument();
            Element root = targetDoc.createElement("mappedData");
            targetDoc.appendChild(root);
        }

        // Create XPath with namespace support
        XPath xpath = XPathFactory.newInstance().newXPath();

        // Use provided namespaces or defaults
        Map<String, String> effectiveNamespaces = new HashMap<>();

        // Add default SOAP namespace
        effectiveNamespaces.put("soapenv", "http://schemas.xmlsoap.org/soap/envelope/");

        // Add all provided namespaces
        if(namespaces != null && !namespaces.isEmpty()) {
            effectiveNamespaces.putAll(namespaces);
            logger.debug("Using {} namespaces from flow structures", namespaces.size());
        } else {
            logger.warn("No namespaces provided, no defaults will be added");
        }

        xpath.setNamespaceContext(new MapNamespaceContext(effectiveNamespaces));

        // Process each field mapping
        for(int i = 0; i < fieldMappings.size(); i++) {
            FieldMapping mapping = fieldMappings.get(i);
            logger.debug("Processing mapping {}: sourceFields = '{}', targetField = '{}', sourceXPath = '{}', targetXPath = '{}'",
                i + 1, mapping.getSourceFields(), mapping.getTargetField(), mapping.getSourceXPath(), mapping.getTargetXPath());

            try {
                if(mapping.isArrayMapping()) {
                    processArrayMapping(sourceDoc, targetDoc, mapping, xpath);
                } else {
                    processSimpleMapping(sourceDoc, targetDoc, mapping, xpath, effectiveNamespaces);
                }
            } catch(Exception e) {
                logger.error("Error processing mapping {}: {}", i + 1, e.getMessage(), e);
            }
        }

        // Convert result to string
        String result = documentToString(targetDoc);
        logger.info("Field mapping completed");
        logger.debug("========== TRANSFORMED XML OUTPUT ==========");
        logger.debug("{}", result);
        logger.debug("============================================");
        return result;
    }

    private void processSimpleMapping(Document sourceDoc, Document targetDoc,
                                     FieldMapping mapping, XPath xpath, Map<String, String> namespaces) throws Exception {

        String sourceXPath = mapping.getSourceXPath();
        String targetXPath = mapping.getTargetXPath();

        if(sourceXPath == null || targetXPath == null) {
            // Fall back to legacy field mapping
            processLegacyMapping(sourceDoc, targetDoc, mapping, xpath, namespaces);
            return;
        }

        // Evaluate source XPath - try with namespace prefix if direct path fails
        XPathExpression sourceExpr = xpath.compile(sourceXPath);
        Object sourceResult = sourceExpr.evaluate(sourceDoc, XPathConstants.NODESET);
        NodeList sourceNodes = (NodeList) sourceResult;

        // If no nodes found and the XPath doesn't contain a prefix, try with wildcard namespace
        if(sourceNodes.getLength() == 0 && !sourceXPath.contains(":")) {
            // Try with wildcard namespace prefix
            String wildcardXPath = sourceXPath;
            // Handle //elementName pattern
            wildcardXPath = wildcardXPath.replaceAll("//([^/\\[\\*] + )", "//*[local - name() = '$1']");
            // Handle /elementName pattern(but not /*[...])
            wildcardXPath = wildcardXPath.replaceAll("(?<!\\*\\[local - name\\(\\) = '[^']*'\\])/([^/\\[\\*] + )", "/*[local - name() = '$1']");

            logger.debug("No nodes found for ' {}', trying wildcard XPath: ' {}'", sourceXPath, wildcardXPath);
            sourceExpr = xpath.compile(wildcardXPath);
            sourceResult = sourceExpr.evaluate(sourceDoc, XPathConstants.NODESET);
            sourceNodes = (NodeList) sourceResult;
        }

        if(sourceNodes.getLength() > 0) {
            Node sourceNode = sourceNodes.item(0);
            String value = getNodeValue(sourceNode);

            // Apply transformation if defined
            if(mapping.getJavaFunction() != null) {
                value = applyTransformation(value, mapping.getJavaFunction());
            }

            // Set value at target XPath
            setValueAtXPath(targetDoc, targetXPath, value, xpath);
        }
    }

    private void processArrayMapping(Document sourceDoc, Document targetDoc,
                                    FieldMapping mapping, XPath xpath) throws Exception {

        String arrayContextPath = mapping.getArrayContextPath();
        String sourceXPath = mapping.getSourceXPath();
        String targetXPath = mapping.getTargetXPath();

        if(arrayContextPath == null) {
            throw new IllegalArgumentException("Array context path is required for array mapping");
        }

        // Get all items in the array
        XPathExpression arrayExpr = xpath.compile(arrayContextPath);
        NodeList arrayNodes = (NodeList) arrayExpr.evaluate(sourceDoc, XPathConstants.NODESET);

        // Process each array item
        for(int i = 0; i < arrayNodes.getLength(); i++) {
            Node arrayNode = arrayNodes.item(i);

            // Evaluate source XPath relative to array item
            XPathExpression sourceExpr = xpath.compile(sourceXPath);
            Object sourceResult = sourceExpr.evaluate(arrayNode, XPathConstants.NODESET);
            NodeList sourceNodes = (NodeList) sourceResult;

            if(sourceNodes.getLength() > 0) {
                String value = getNodeValue(sourceNodes.item(0));

                // Apply transformation if defined
                if(mapping.getJavaFunction() != null) {
                    value = applyTransformation(value, mapping.getJavaFunction());
                }

                // Create target path with array index
                String indexedTargetPath = targetXPath.replace("[*]", "[" + (i + 1) + "]");

                // Ensure parent structure exists
                ensurePathExists(targetDoc, indexedTargetPath, xpath);

                // Set value at target XPath
                setValueAtXPath(targetDoc, indexedTargetPath, value, xpath);
            }
        }
    }

    private void processLegacyMapping(Document sourceDoc, Document targetDoc,
                                     FieldMapping mapping, XPath xpath, Map<String, String> namespaces) throws Exception {

        logger.debug("Processing legacy mapping: sourceFields = {}, targetField = {}",
            mapping.getSourceFields(), mapping.getTargetField());

        // Handle legacy field - based mapping
        List<String> sourceFieldsList = mapping.getSourceFieldsList();
        String targetField = mapping.getTargetField();

        if(sourceFieldsList == null || sourceFieldsList.isEmpty() || targetField == null) {
            logger.debug("Skipping mapping due to null/empty fields");
            return;
        }

        // Get the first source field from the list
        String sourceField = sourceFieldsList.get(0);

        // Check if sourceField is a literal value or a field reference
        String value = null;

        // Skip if source field is the root element name
        if(sourceDoc.getDocumentElement() != null &&
            sourceField.equals(sourceDoc.getDocumentElement().getNodeName())) {
            logger.debug("Skipping extraction from root element: {}", sourceField);
            return;
        }

        // Try to extract value from the source field
        if(sourceField != null && (sourceField.startsWith("//") || sourceField.contains("/"))) {
            // It's an XPath expression
            logger.debug("Source is XPath expression: {}", sourceField);
            XPathExpression sourceExpr = xpath.compile(sourceField);
            NodeList sourceNodes = (NodeList) sourceExpr.evaluate(sourceDoc, XPathConstants.NODESET);

            if(sourceNodes.getLength() > 0) {
                value = getNodeValue(sourceNodes.item(0));
                logger.debug("Found value from XPath: {}", value);
            } else {
                // No nodes found, skip this mapping
                logger.debug("No nodes found for XPath: {}", sourceField);
                return;
            }
        } else {
            // Try as element name
            logger.debug("Trying to extract value from element: {}", sourceField);
            XPathExpression sourceExpr = xpath.compile("//" + sourceField);
            NodeList sourceNodes = (NodeList) sourceExpr.evaluate(sourceDoc, XPathConstants.NODESET);

            if(sourceNodes.getLength() > 0) {
                value = getNodeValue(sourceNodes.item(0));
                logger.debug("Found value from element ' {}': ' {}'", sourceField, value);
            } else {
                // Try with wildcard namespace if no prefix
                String wildcardXPath = "//*[local - name() = '" + sourceField + "']";
                logger.debug("No nodes found for ' {}', trying wildcard XPath: ' {}'", sourceField, wildcardXPath);
                sourceExpr = xpath.compile(wildcardXPath);
                sourceNodes = (NodeList) sourceExpr.evaluate(sourceDoc, XPathConstants.NODESET);

                if(sourceNodes.getLength() > 0) {
                    value = getNodeValue(sourceNodes.item(0));
                    logger.debug("Found value from wildcard element ' {}': ' {}'", sourceField, value);
                } else {
                    // If no element found and it looks like a simple value, use it as literal
                    if(!sourceField.contains("<") && !sourceField.contains(" {")) {
                        value = sourceField;
                        logger.debug("Using as literal value: {}", value);
                    } else {
                        logger.debug("No nodes found for element: {}", sourceField);
                        return;
                    }
                }
            }
        }

        // Handle targetField - ensure it's a proper XPath
        String targetXPath = targetField;

        // Check if the target field has namespace prefix
        if(targetField.contains(":")) {
            // It already has a namespace prefix, use as - is but ensure it starts with //
            if(!targetXPath.startsWith("/")) {
                targetXPath = "//" + targetField;
            }
        } else {
            // No namespace prefix in target field - need to find the appropriate namespace
            // Look through the namespaces to find a non - SOAP namespace for the target
            String targetPrefix = null;

            // Find the appropriate namespace prefix for the target field
            logger.trace("Looking for namespace prefix for field '{}'. Available namespaces:", targetField);
            for(Map.Entry<String, String> ns : namespaces.entrySet()) {
                logger.trace(" {} = {}", ns.getKey(), ns.getValue());
            }

            // First check if we have a "tns" prefix - this is typically the service namespace
            if(namespaces.containsKey("tns")) {
                targetPrefix = "tns";
                logger.debug("Using service namespace prefix 'tns' for target field '{}'", targetField);
            } else {
                // Otherwise look for a non - standard namespace
                for(Map.Entry<String, String> ns : namespaces.entrySet()) {
                    String uri = ns.getValue();
                    String prefix = ns.getKey();

                    // Skip SOAP and standard namespaces
                    if(!uri.contains("schemas.xmlsoap.org") &&
                        !uri.contains("www.w3.org/2001/XMLSchema") &&
                        !uri.contains("schemas.xmlsoap.org/wsdl") &&
                        !prefix.equals("xsd") &&
                        !prefix.equals("wsdl") &&
                        !prefix.equals("s") &&
                        !prefix.isEmpty()) {
                        targetPrefix = prefix;
                        logger.debug("Selected namespace prefix '{}' (URI: {}) for target field '{}'", prefix, uri, targetField);
                        break;
                    }
                }
            }


            // Build the XPath with or without namespace prefix
            if(targetPrefix != null) {
                targetXPath = "//" + targetPrefix + ":" + targetField;
            } else {
                // No suitable namespace found, try without prefix
                targetXPath = "//" + targetField;
            }
        }
        logger.trace("Target XPath: {} (original: {})", targetXPath, targetField);

        // Apply transformation if defined
        if(mapping.getJavaFunction() != null) {
            value = applyTransformation(value, mapping.getJavaFunction());
            logger.debug("Applied transformation, new value: {}", value);
        }

        // Set value at target XPath
        logger.trace("Setting value '{}' at XPath '{}'", value, targetXPath);
        setValueAtXPath(targetDoc, targetXPath, value, xpath);
    }

    private String getNodeValue(Node node) {
        if(node.getNodeType() == Node.ELEMENT_NODE) {
            return node.getTextContent();
        } else if(node.getNodeType() == Node.ATTRIBUTE_NODE) {
            return node.getNodeValue();
        }
        return node.getNodeValue();
    }

    private void setValueAtXPath(Document doc, String xpath, String value, XPath xpathEval)
            throws Exception {

        logger.debug("Setting value ' {}' at XPath ' {}'", value, xpath);

        // First try to find existing node
        XPathExpression expr = xpathEval.compile(xpath);
        NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

        logger.debug("Found {} existing nodes for XPath ' {}'", nodes.getLength(), xpath);

        if(nodes.getLength() > 0) {
            // Update existing node
            Node node = nodes.item(0);
            logger.debug("Updating existing node: {} (type: {})", node.getNodeName(), node.getNodeType());
            if(node.getNodeType() == Node.ELEMENT_NODE) {
                node.setTextContent(value);
                logger.debug("Set text content to: {}", value);
            } else if(node.getNodeType() == Node.ATTRIBUTE_NODE) {
                ((Attr) node).setValue(value);
                logger.debug("Set attribute value to: {}", value);
            }
        } else {
            // Create new node structure
            logger.debug("No existing node found, creating new node structure");
            createNodeFromXPath(doc, xpath, value);
        }
    }

    private void ensurePathExists(Document doc, String xpath, XPath xpathEval) throws Exception {
        // Remove the last element to get parent path
        int lastSlash = xpath.lastIndexOf('/');
        if(lastSlash > 0) {
            String parentPath = xpath.substring(0, lastSlash);

            // Check if parent exists
            XPathExpression expr = xpathEval.compile(parentPath);
            NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

            if(nodes.getLength() == 0) {
                // Create parent structure
                createNodeFromXPath(doc, parentPath, null);
            }
        }
    }

    private void createNodeFromXPath(Document doc, String xpath, String value) {
        // Parse XPath and create node structure
        String[] parts = xpath.split("/");
        Node currentNode = doc.getDocumentElement();

        for(int i = 1; i < parts.length; i++) {
            String part = parts[i];
            if(part.isEmpty()) continue;

            // Handle array indices
            String elementName = part;
            int arrayIndex = -1;
            if(part.contains("[") && part.contains("]")) {
                elementName = part.substring(0, part.indexOf('['));
                String indexStr = part.substring(part.indexOf('[') + 1, part.indexOf(']'));
                try {
                    arrayIndex = Integer.parseInt(indexStr) - 1; // XPath uses 1 - based indexing
                } catch(NumberFormatException e) {
                    // Ignore invalid indices
                }
            }

            // Find or create child element
            NodeList children = currentNode.getChildNodes();
            Element targetElement = null;
            int currentIndex = 0;

            for(int j = 0; j < children.getLength(); j++) {
                Node child = children.item(j);
                if(child.getNodeType() == Node.ELEMENT_NODE &&
                    child.getNodeName().equals(elementName)) {
                    if(arrayIndex < 0 || currentIndex == arrayIndex) {
                        targetElement = (Element) child;
                        break;
                    }
                    currentIndex++;
                }
            }

            if(targetElement == null) {
                // Create new element
                targetElement = doc.createElement(elementName);
                currentNode.appendChild(targetElement);
            }

            currentNode = targetElement;
        }

        // Set value on final node
        if(value != null && currentNode.getNodeType() == Node.ELEMENT_NODE) {
            currentNode.setTextContent(value);
        }
    }

    private String applyTransformation(String value, String javaFunction) {
        try {
            // Parse function call(e.g., "concat(field1, field2, '-')")
            if(javaFunction == null || javaFunction.trim().isEmpty()) {
                return value;
            }

            // Extract function name
            int parenIndex = javaFunction.indexOf('(');
            if(parenIndex == -1) {
                logger.warn("Invalid function format: {}", javaFunction);
                return value;
            }

            String functionName = javaFunction.substring(0, parenIndex).trim();

            // Get function body from database
            Optional<TransformationCustomFunction> functionOpt = functionRepository.findByName(functionName);
            if(!functionOpt.isPresent()) {
                logger.warn("Function not found in database: {}", functionName);
                return value;
            }
            TransformationCustomFunction function = functionOpt.get();

            // Create context with the current value
            Map<String, Object> context = new HashMap<>();
            context.put("value", value);
            context.put("field", value); // Alternative reference

            // Replace the first argument if it's referencing the field value
            String modifiedCall = javaFunction;
            String argsString = javaFunction.substring(parenIndex + 1, javaFunction.lastIndexOf(')')).trim();
            if(!argsString.isEmpty()) {
                String[] args = argsString.split(",", 2);
                if(args.length > 0 && (args[0].trim().equals("value") || args[0].trim().equals("field"))) {
                    // Replace with actual value
                    modifiedCall = functionName + "(\"" + value.replace("\"", "\\\"") + "\"";
                    if(args.length > 1) {
                        modifiedCall += "," + args[1];
                    }
                    modifiedCall += ")";
                }
            }

            // Execute the function
            Object result = functionExecutor.executeFunctionCall(modifiedCall, function.getFunctionBody(), context);

            return result != null ? result.toString() : "";

        } catch(Exception e) {
            logger.error("Error applying transformation function: {}", javaFunction, e);
            return value; // Return original value on error
        }
    }

    private String documentToString(Document doc) throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(" {http://xml.apache.org/xslt}indent - amount", "2");

        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));

        return writer.toString();
    }

    /**
     * Simple namespace context implementation using a map
     */
    private static class MapNamespaceContext implements NamespaceContext {
        private final Map<String, String> prefixToUri;
        private final Map<String, String> uriToPrefix;

        public MapNamespaceContext(Map<String, String> namespaces) {
            this.prefixToUri = new HashMap<>(namespaces);
            this.uriToPrefix = new HashMap<>();
            for(Map.Entry<String, String> entry : namespaces.entrySet()) {
                uriToPrefix.put(entry.getValue(), entry.getKey());
            }
        }

        @Override
        public String getNamespaceURI(String prefix) {
            return prefixToUri.get(prefix);
        }

        @Override
        public String getPrefix(String namespaceURI) {
            return uriToPrefix.get(namespaceURI);
        }

        @Override
        public Iterator<String> getPrefixes(String namespaceURI) {
            String prefix = getPrefix(namespaceURI);
            if(prefix == null) {
                return Collections.emptyIterator();
            }
            return Collections.singletonList(prefix).iterator();
        }
    }
}
