package com.integrixs.backend.service;

import com.integrixs.data.model.FlowStructure;
import com.integrixs.data.model.MessageStructure;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.wsdl.Definition;
import javax.wsdl.Types;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.StringReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

/**
 * Service for XML validation against XSD schemas and WSDL definitions
 */
@Service
public class XmlValidationService {

    /**
     * Validates an XML message against a FlowStructure(which contains WSDL)
     */

    private static final Logger log = LoggerFactory.getLogger(XmlValidationService.class);

    public ValidationResult validateMessageAgainstFlowStructure(String xmlMessage, FlowStructure flowStructure, Map<String, Object> context) {
        ValidationResult result = new ValidationResult();

        try {
            if(flowStructure.getWsdlContent() == null || flowStructure.getWsdlContent().trim().isEmpty()) {
                log.warn("FlowStructure {} has no WSDL content, skipping validation", flowStructure.getName());
                result.setValid(true);
                result.setValidatedMessage(xmlMessage);
                return result;
            }

            // Extract XSD schemas from WSDL
            List<String> xsdSchemas = extractXsdFromWsdl(flowStructure.getWsdlContent());

            if(xsdSchemas.isEmpty()) {
                log.warn("No XSD schemas found in WSDL for FlowStructure {}", flowStructure.getName());
                result.setValid(true);
                result.setValidatedMessage(xmlMessage);
                return result;
            }

            // Validate XML against each schema
            for(String xsdSchema : xsdSchemas) {
                ValidationResult schemaResult = validateXmlAgainstXsd(xmlMessage, xsdSchema);
                if(!schemaResult.isValid()) {
                    result.setValid(false);
                    result.getErrors().addAll(schemaResult.getErrors());
                }
            }

            if(result.isValid()) {
                result.setValidatedMessage(xmlMessage);
                log.info("Message validated successfully against FlowStructure {}", flowStructure.getName());
            } else {
                log.error("Message validation failed against FlowStructure {}: {}",
                    flowStructure.getName(), String.join(", ", result.getErrors()));
            }

        } catch(Exception e) {
            log.error("Error validating message against FlowStructure", e);
            result.setValid(false);
            result.getErrors().add("Validation error: " + e.getMessage());
        }

        return result;
    }

    /**
     * Validates an XML message against a MessageStructure(which contains XSD)
     */
    public ValidationResult validateMessageAgainstMessageStructure(String xmlMessage, MessageStructure messageStructure, Map<String, Object> context) {
        ValidationResult result = new ValidationResult();

        try {
            if(messageStructure.getXsdContent() == null || messageStructure.getXsdContent().trim().isEmpty()) {
                log.warn("MessageStructure {} has no XSD content, skipping validation", messageStructure.getName());
                result.setValid(true);
                result.setValidatedMessage(xmlMessage);
                return result;
            }

            // Validate XML against XSD
            result = validateXmlAgainstXsd(xmlMessage, messageStructure.getXsdContent());

            if(result.isValid()) {
                log.info("Message validated successfully against MessageStructure {}", messageStructure.getName());
            } else {
                log.error("Message validation failed against MessageStructure {}: {}",
                    messageStructure.getName(), String.join(", ", result.getErrors()));
            }

        } catch(Exception e) {
            log.error("Error validating message against MessageStructure", e);
            result.setValid(false);
            result.getErrors().add("Validation error: " + e.getMessage());
        }

        return result;
    }

    /**
     * Validates XML against an XSD schema
     */
    public ValidationResult validateXmlAgainstXsd(String xmlContent, String xsdContent) {
        ValidationResult result = new ValidationResult();
        List<String> errors = new ArrayList<>();

        try {
            // Create schema factory
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

            // Create schema from XSD
            Source schemaSource = new StreamSource(new StringReader(xsdContent));
            javax.xml.validation.Schema schema = schemaFactory.newSchema(schemaSource);

            // Create validator
            Validator validator = schema.newValidator();

            // Set error handler
            validator.setErrorHandler(new ErrorHandler() {
                @Override
                public void warning(SAXParseException e) {
                    log.warn("Validation warning: {} at line {}, column {}",
                        e.getMessage(), e.getLineNumber(), e.getColumnNumber());
                }

                @Override
                public void error(SAXParseException e) {
                    String error = String.format("Validation error at line %d, column %d: %s",
                        e.getLineNumber(), e.getColumnNumber(), e.getMessage());
                    errors.add(error);
                    log.error(error);
                }

                @Override
                public void fatalError(SAXParseException e) throws SAXException {
                    String error = String.format("Fatal validation error at line %d, column %d: %s",
                        e.getLineNumber(), e.getColumnNumber(), e.getMessage());
                    errors.add(error);
                    log.error(error);
                    throw e;
                }
            });

            // Parse XML
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(xmlContent)));

            // Validate
            validator.validate(new DOMSource(document));

            result.setValid(errors.isEmpty());
            result.setErrors(errors);
            result.setValidatedMessage(xmlContent);

        } catch(Exception e) {
            log.error("Error during XML validation", e);
            result.setValid(false);
            result.getErrors().add("Validation failed: " + e.getMessage());
        }

        return result;
    }

    /**
     * Extracts XSD schemas from WSDL content
     */
    private List<String> extractXsdFromWsdl(String wsdlContent) {
        List<String> schemas = new ArrayList<>();

        try {
            // Parse WSDL
            WSDLFactory factory = WSDLFactory.newInstance();
            WSDLReader reader = factory.newWSDLReader();
            reader.setFeature("javax.wsdl.verbose", false);
            Definition definition = reader.readWSDL(null, new InputSource(new StringReader(wsdlContent)));

            // Extract schemas from types section
            Types types = definition.getTypes();
            if(types != null) {
                List<?> extensibilityElements = types.getExtensibilityElements();
                for(Object element : extensibilityElements) {
                    if(element instanceof Schema) {
                        Schema schema = (Schema) element;
                        Element schemaElement = schema.getElement();

                        // Convert schema element to string
                        String schemaStr = elementToString(schemaElement);
                        if(schemaStr != null && !schemaStr.trim().isEmpty()) {
                            schemas.add(schemaStr);
                        }
                    }
                }
            }

        } catch(Exception e) {
            log.error("Error extracting XSD from WSDL", e);
        }

        return schemas;
    }

    /**
     * Converts a DOM Element to string
     */
    private String elementToString(Element element) {
        try {
            javax.xml.transform.TransformerFactory tf = javax.xml.transform.TransformerFactory.newInstance();
            javax.xml.transform.Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes");

            java.io.StringWriter writer = new java.io.StringWriter();
            transformer.transform(new DOMSource(element), new javax.xml.transform.stream.StreamResult(writer));

            return writer.toString();
        } catch(Exception e) {
            log.error("Error converting element to string", e);
            return null;
        }
    }

    /**
     * Validation result class
     */
    public static class ValidationResult {
        private boolean valid;
        private String validatedMessage;
        private List<String> errors = new ArrayList<>();

        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }

        public String getValidatedMessage() { return validatedMessage; }
        public void setValidatedMessage(String validatedMessage) { this.validatedMessage = validatedMessage; }

        public List<String> getErrors() { return errors; }
        public void setErrors(List<String> errors) { this.errors = errors; }
    }
}
