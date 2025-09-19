package com.integrixs.backend.service;

import org.springframework.stereotype.Service;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.wsdl.*;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.io.StringWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

/**
 * Service for extracting sample XML from WSDL definitions
 */
@Service
public class WsdlSampleExtractorService {

    /**
     * Extracts a sample XML message for a specific operation from WSDL
     */

    private static final Logger log = LoggerFactory.getLogger(WsdlSampleExtractorService.class);

    public String extractSampleXmlForOperation(String wsdlContent, String operationName, String messageType) {
        try {
            // Parse WSDL
            Definition definition = parseWsdl(wsdlContent);
            if(definition == null) {
                log.error("Failed to parse WSDL");
                return null;
            }

            // Find the operation
            Operation operation = findOperation(definition, operationName);
            if(operation == null) {
                log.error("Operation {} not found in WSDL", operationName);
                return null;
            }

            // Get the message based on type(input/output)
            Message message = null;
            if("input".equalsIgnoreCase(messageType) && operation.getInput() != null) {
                message = operation.getInput().getMessage();
            } else if("output".equalsIgnoreCase(messageType) && operation.getOutput() != null) {
                message = operation.getOutput().getMessage();
            }

            if(message == null) {
                log.error("No {} message found for operation {}", messageType, operationName);
                return null;
            }

            // Generate sample XML from message
            return generateSampleXmlFromMessage(definition, message);

        } catch(Exception e) {
            log.error("Error extracting sample XML from WSDL", e);
            return null;
        }
    }

    /**
     * Extracts sample request XML for an operation
     */
    public String extractSampleRequestXml(String wsdlContent, String operationName) {
        return extractSampleXmlForOperation(wsdlContent, operationName, "input");
    }

    /**
     * Extracts sample response XML for an operation
     */
    public String extractSampleResponseXml(String wsdlContent, String operationName) {
        return extractSampleXmlForOperation(wsdlContent, operationName, "output");
    }

    /**
     * Parses WSDL content
     */
    private Definition parseWsdl(String wsdlContent) {
        try {
            WSDLFactory factory = WSDLFactory.newInstance();
            WSDLReader reader = factory.newWSDLReader();
            reader.setFeature("javax.wsdl.verbose", false);
            reader.setFeature("javax.wsdl.importDocuments", true);

            return reader.readWSDL(null, new InputSource(new StringReader(wsdlContent)));
        } catch(WSDLException e) {
            log.error("Error parsing WSDL", e);
            return null;
        }
    }

    /**
     * Finds an operation in the WSDL definition
     */
    private Operation findOperation(Definition definition, String operationName) {
        Map<?, ?> portTypes = definition.getPortTypes();
        for(Object portTypeObj : portTypes.values()) {
            PortType portType = (PortType) portTypeObj;
            List<?> operations = portType.getOperations();
            for(Object opObj : operations) {
                Operation op = (Operation) opObj;
                if(op.getName().equals(operationName)) {
                    return op;
                }
            }
        }
        return null;
    }

    /**
     * Generates sample XML from a WSDL message
     */
    private String generateSampleXmlFromMessage(Definition definition, Message message) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();

            // Get message parts
            Map<?, ?> parts = message.getParts();
            if(parts.isEmpty()) {
                return null;
            }

            Element rootElement = null;

            // Process each part
            for(Object partObj : parts.values()) {
                Part part = (Part) partObj;

                if(part.getElementName() != null) {
                    // Element - based message part
                    QName elementQName = part.getElementName();
                    Element element = createElementFromSchema(doc, definition, elementQName);
                    if(element != null) {
                        if(rootElement == null) {
                            rootElement = element;
                            doc.appendChild(element);
                        } else {
                            rootElement.appendChild(element);
                        }
                    }
                } else if(part.getTypeName() != null) {
                    // Type - based message part
                    QName typeQName = part.getTypeName();
                    String partName = part.getName();
                    Element element = createElementForType(doc, definition, partName, typeQName);
                    if(element != null) {
                        if(rootElement == null) {
                            rootElement = element;
                            doc.appendChild(element);
                        } else {
                            rootElement.appendChild(element);
                        }
                    }
                }
            }

            // Convert document to string
            if(rootElement != null) {
                return documentToString(doc);
            }

            return null;

        } catch(Exception e) {
            log.error("Error generating sample XML", e);
            return null;
        }
    }

    /**
     * Creates an element from schema definition
     */
    private Element createElementFromSchema(Document doc, Definition definition, QName elementQName) {
        try {
            // Get schemas from WSDL
            Types types = definition.getTypes();
            if(types == null) {
                return null;
            }

            // Look for the element in schemas
            List<?> extensibilityElements = types.getExtensibilityElements();
            for(Object extElement : extensibilityElements) {
                if(extElement instanceof Schema) {
                    Schema schema = (Schema) extElement;
                    Element schemaElement = schema.getElement();

                    // Find element definition
                    NodeList elements = schemaElement.getElementsByTagNameNS(XMLConstants.W3C_XML_SCHEMA_NS_URI, "element");
                    for(int i = 0; i < elements.getLength(); i++) {
                        Element elem = (Element) elements.item(i);
                        String name = elem.getAttribute("name");

                        if(name.equals(elementQName.getLocalPart())) {
                            // Create sample element
                            return createSampleElement(doc, elem, elementQName.getNamespaceURI(), schemaElement);
                        }
                    }
                }
            }

        } catch(Exception e) {
            log.error("Error creating element from schema", e);
        }

        // Fallback: create simple element
        Element element = doc.createElementNS(elementQName.getNamespaceURI(), elementQName.getLocalPart());
        element.setTextContent("?");
        return element;
    }

    /**
     * Creates an element for a type
     */
    private Element createElementForType(Document doc, Definition definition, String elementName, QName typeQName) {
        String namespace = typeQName.getNamespaceURI();
        Element element = doc.createElementNS(namespace, elementName);

        // Add sample content based on type
        String localPart = typeQName.getLocalPart();
        if(isSimpleType(localPart)) {
            element.setTextContent(getSampleValueForType(localPart));
        } else {
            // Complex type - try to find definition
            element.setTextContent("?");
        }

        return element;
    }

    /**
     * Creates a sample element from schema element definition
     */
    private Element createSampleElement(Document doc, Element schemaElement, String namespace, Element schemaRoot) {
        String name = schemaElement.getAttribute("name");
        Element element = doc.createElementNS(namespace, name);

        // Check if it's a complex type
        NodeList complexTypes = schemaElement.getElementsByTagNameNS(XMLConstants.W3C_XML_SCHEMA_NS_URI, "complexType");
        if(complexTypes.getLength() > 0) {
            Element complexType = (Element) complexTypes.item(0);
            processComplexType(doc, element, complexType, namespace, schemaRoot);
        } else {
            // Simple type or reference
            String type = schemaElement.getAttribute("type");
            if(type != null && !type.isEmpty()) {
                element.setTextContent(getSampleValueForType(type));
            } else {
                element.setTextContent("?");
            }
        }

        return element;
    }

    /**
     * Processes a complex type
     */
    private void processComplexType(Document doc, Element parent, Element complexType, String namespace, Element schemaRoot) {
        // Look for sequence/all/choice
        NodeList sequences = complexType.getElementsByTagNameNS(XMLConstants.W3C_XML_SCHEMA_NS_URI, "sequence");
        if(sequences.getLength() > 0) {
            processSequence(doc, parent, (Element) sequences.item(0), namespace, schemaRoot);
        }

        NodeList alls = complexType.getElementsByTagNameNS(XMLConstants.W3C_XML_SCHEMA_NS_URI, "all");
        if(alls.getLength() > 0) {
            processSequence(doc, parent, (Element) alls.item(0), namespace, schemaRoot);
        }

        // Process attributes
        NodeList attributes = complexType.getElementsByTagNameNS(XMLConstants.W3C_XML_SCHEMA_NS_URI, "attribute");
        for(int i = 0; i < attributes.getLength(); i++) {
            Element attr = (Element) attributes.item(i);
            String attrName = attr.getAttribute("name");
            String attrType = attr.getAttribute("type");
            if(attrName != null && !attrName.isEmpty()) {
                parent.setAttribute(attrName, getSampleValueForType(attrType));
            }
        }
    }

    /**
     * Processes a sequence
     */
    private void processSequence(Document doc, Element parent, Element sequence, String namespace, Element schemaRoot) {
        NodeList children = sequence.getChildNodes();
        for(int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if(child.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element) child;
                String tagName = childElement.getLocalName();

                if("element".equals(tagName)) {
                    String name = childElement.getAttribute("name");
                    String type = childElement.getAttribute("type");
                    String minOccurs = childElement.getAttribute("minOccurs");

                    // Skip optional elements(minOccurs = "0")
                    if("0".equals(minOccurs)) {
                        continue;
                    }

                    Element newElement = doc.createElementNS(namespace, name);
                    if(type != null && !type.isEmpty()) {
                        newElement.setTextContent(getSampleValueForType(type));
                    } else {
                        // Check for inline complex type
                        NodeList complexTypes = childElement.getElementsByTagNameNS(XMLConstants.W3C_XML_SCHEMA_NS_URI, "complexType");
                        if(complexTypes.getLength() > 0) {
                            processComplexType(doc, newElement, (Element) complexTypes.item(0), namespace, schemaRoot);
                        } else {
                            newElement.setTextContent("?");
                        }
                    }
                    parent.appendChild(newElement);
                }
            }
        }
    }

    /**
     * Checks if a type is a simple XSD type
     */
    private boolean isSimpleType(String type) {
        return type.startsWith("xsd:") || type.startsWith("xs:") ||
               Arrays.asList("string", "int", "integer", "decimal", "boolean", "date", "dateTime").contains(type);
    }

    /**
     * Gets sample value for a type
     */
    private String getSampleValueForType(String type) {
        if(type == null || type.isEmpty()) {
            return "?";
        }

        // Remove namespace prefix
        if(type.contains(":")) {
            type = type.substring(type.indexOf(":") + 1);
        }

        switch(type.toLowerCase()) {
            case "string":
                return "string";
            case "int":
            case "integer":
                return "0";
            case "long":
                return "0";
            case "decimal":
            case "double":
            case "float":
                return "0.0";
            case "boolean":
                return "false";
            case "date":
                return "2024-01-01";
            case "datetime":
            case "datetimeoffset":
                return "2024-01-01T00:00:00";
            default:
                return "?";
        }
    }

    /**
     * Converts a document to string
     */
    private String documentToString(Document doc) {
        try {
            javax.xml.transform.TransformerFactory tf = javax.xml.transform.TransformerFactory.newInstance();
            javax.xml.transform.Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(" {http://xml.apache.org/xslt}indent - amount", "2");

            StringWriter writer = new StringWriter();
            transformer.transform(new javax.xml.transform.dom.DOMSource(doc),
                new javax.xml.transform.stream.StreamResult(writer));

            return writer.toString();
        } catch(Exception e) {
            log.error("Error converting document to string", e);
            return null;
        }
    }
}
