package com.integrixs.backend.service;

import com.integrixs.backend.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.StringReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

@Service
public class WsdlValidationService {

    private static final Logger log = LoggerFactory.getLogger(WsdlValidationService.class);


    public static class ValidationResult {
        private boolean valid;
        private List<ValidationIssue> issues = new ArrayList<>();
        private WsdlMetadata metadata;

        // Getters and setters
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        public List<ValidationIssue> getIssues() { return issues; }
        public void setIssues(List<ValidationIssue> issues) { this.issues = issues; }
        public WsdlMetadata getMetadata() { return metadata; }
        public void setMetadata(WsdlMetadata metadata) { this.metadata = metadata; }
    }

    public static class ValidationIssue {
        private String type; // ERROR, WARNING, INFO
        private String message;
        private Integer line;
        private Integer column;
        private String path;

        public ValidationIssue(String type, String message) {
            this.type = type;
            this.message = message;
        }

        public ValidationIssue(String type, String message, Integer line, Integer column) {
            this.type = type;
            this.message = message;
            this.line = line;
            this.column = column;
        }

        // Getters and setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public Integer getLine() { return line; }
        public void setLine(Integer line) { this.line = line; }
        public Integer getColumn() { return column; }
        public void setColumn(Integer column) { this.column = column; }
        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }
    }

    public static class WsdlMetadata {
        private String targetNamespace;
        private List<ServiceInfo> services = new ArrayList<>();
        private List<PortTypeInfo> portTypes = new ArrayList<>();
        private List<MessageInfo> messages = new ArrayList<>();
        private Map<String, String> namespaces = new HashMap<>();
        private String version;

        // Getters and setters
        public String getTargetNamespace() { return targetNamespace; }
        public void setTargetNamespace(String targetNamespace) { this.targetNamespace = targetNamespace; }
        public List<ServiceInfo> getServices() { return services; }
        public void setServices(List<ServiceInfo> services) { this.services = services; }
        public List<PortTypeInfo> getPortTypes() { return portTypes; }
        public void setPortTypes(List<PortTypeInfo> portTypes) { this.portTypes = portTypes; }
        public List<MessageInfo> getMessages() { return messages; }
        public void setMessages(List<MessageInfo> messages) { this.messages = messages; }
        public Map<String, String> getNamespaces() { return namespaces; }
        public void setNamespaces(Map<String, String> namespaces) { this.namespaces = namespaces; }
        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
    }

    public static class ServiceInfo {
        private String name;
        private List<PortInfo> ports = new ArrayList<>();

        public ServiceInfo(String name) { this.name = name; }

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public List<PortInfo> getPorts() { return ports; }
        public void setPorts(List<PortInfo> ports) { this.ports = ports; }
    }

    public static class PortInfo {
        private String name;
        private String binding;
        private String address;

        public PortInfo(String name, String binding) {
            this.name = name;
            this.binding = binding;
        }

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getBinding() { return binding; }
        public void setBinding(String binding) { this.binding = binding; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
    }

    public static class PortTypeInfo {
        private String name;
        private List<OperationInfo> operations = new ArrayList<>();

        public PortTypeInfo(String name) { this.name = name; }

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public List<OperationInfo> getOperations() { return operations; }
        public void setOperations(List<OperationInfo> operations) { this.operations = operations; }
    }

    public static class OperationInfo {
        private String name;
        private String inputMessage;
        private String outputMessage;
        private List<String> faultMessages = new ArrayList<>();

        public OperationInfo(String name) { this.name = name; }

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getInputMessage() { return inputMessage; }
        public void setInputMessage(String inputMessage) { this.inputMessage = inputMessage; }
        public String getOutputMessage() { return outputMessage; }
        public void setOutputMessage(String outputMessage) { this.outputMessage = outputMessage; }
        public List<String> getFaultMessages() { return faultMessages; }
        public void setFaultMessages(List<String> faultMessages) { this.faultMessages = faultMessages; }
    }

    public static class MessageInfo {
        private String name;
        private List<PartInfo> parts = new ArrayList<>();

        public MessageInfo(String name) { this.name = name; }

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public List<PartInfo> getParts() { return parts; }
        public void setParts(List<PartInfo> parts) { this.parts = parts; }
    }

    public static class PartInfo {
        private String name;
        private String type;
        private String element;

        public PartInfo(String name) { this.name = name; }

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getElement() { return element; }
        public void setElement(String element) { this.element = element; }
    }

    public ValidationResult validateWsdl(String wsdlContent) {
        ValidationResult result = new ValidationResult();
        result.setValid(true);

        try {
            // Step 1: Basic XML validation
            validateXmlSyntax(wsdlContent, result);

            if(!result.isValid()) {
                return result;
            }

            // Step 2: WSDL schema validation
            validateAgainstWsdlSchema(wsdlContent, result);

            // Step 3: Parse WSDL using WSDL4J
            Definition definition = parseWsdl(wsdlContent, result);

            if(definition != null) {
                // Step 4: Extract metadata
                WsdlMetadata metadata = extractMetadata(definition);
                result.setMetadata(metadata);

                // Step 5: Perform semantic validation
                performSemanticValidation(definition, result);

                // Step 6: Check best practices
                checkBestPractices(definition, wsdlContent, result);
            }

        } catch(Exception e) {
            log.error("Error validating WSDL", e);
            result.setValid(false);
            result.getIssues().add(new ValidationIssue("ERROR",
                "Unexpected error during validation: " + e.getMessage()));
        }

        // Set overall validity based on errors
        boolean hasErrors = result.getIssues().stream()
            .anyMatch(issue -> "ERROR".equals(issue.getType()));
        result.setValid(!hasErrors);

        return result;
    }

    private void validateXmlSyntax(String xmlContent, ValidationResult result) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setErrorHandler(new ErrorHandler() {
                @Override
                public void warning(SAXParseException e) {
                    result.getIssues().add(new ValidationIssue("WARNING",
                        e.getMessage(), e.getLineNumber(), e.getColumnNumber()));
                }

                @Override
                public void error(SAXParseException e) {
                    result.getIssues().add(new ValidationIssue("ERROR",
                        e.getMessage(), e.getLineNumber(), e.getColumnNumber()));
                    result.setValid(false);
                }

                @Override
                public void fatalError(SAXParseException e) {
                    result.getIssues().add(new ValidationIssue("ERROR",
                        e.getMessage(), e.getLineNumber(), e.getColumnNumber()));
                    result.setValid(false);
                }
            });

            builder.parse(new InputSource(new StringReader(xmlContent)));

        } catch(Exception e) {
            result.setValid(false);
            result.getIssues().add(new ValidationIssue("ERROR",
                "XML parsing failed: " + e.getMessage()));
        }
    }

    private void validateAgainstWsdlSchema(String wsdlContent, ValidationResult result) {
        try {
            // Load WSDL schema from classpath
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

            // WSDL 1.1 schema
            Source wsdlSchema = new StreamSource(
                getClass().getResourceAsStream("/schemas/wsdl11.xsd")
           );

            if(wsdlSchema != null) {
                Schema schema = schemaFactory.newSchema(wsdlSchema);
                Validator validator = schema.newValidator();

                validator.setErrorHandler(new ErrorHandler() {
                    @Override
                    public void warning(SAXParseException e) {
                        result.getIssues().add(new ValidationIssue("WARNING",
                            "Schema validation: " + e.getMessage(),
                            e.getLineNumber(), e.getColumnNumber()));
                    }

                    @Override
                    public void error(SAXParseException e) {
                        result.getIssues().add(new ValidationIssue("ERROR",
                            "Schema validation: " + e.getMessage(),
                            e.getLineNumber(), e.getColumnNumber()));
                    }

                    @Override
                    public void fatalError(SAXParseException e) {
                        result.getIssues().add(new ValidationIssue("ERROR",
                            "Schema validation: " + e.getMessage(),
                            e.getLineNumber(), e.getColumnNumber()));
                        result.setValid(false);
                    }
                });

                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(new InputSource(new StringReader(wsdlContent)));

                validator.validate(new DOMSource(doc));
            } else {
                result.getIssues().add(new ValidationIssue("INFO",
                    "WSDL schema validation skipped(schema not available)"));
            }

        } catch(Exception e) {
            // Schema validation is optional, so we just warn
            result.getIssues().add(new ValidationIssue("WARNING",
                "Schema validation failed: " + e.getMessage()));
        }
    }

    private Definition parseWsdl(String wsdlContent, ValidationResult result) {
        try {
            WSDLFactory factory = WSDLFactory.newInstance();
            WSDLReader reader = factory.newWSDLReader();
            reader.setFeature("javax.wsdl.verbose", false);
            reader.setFeature("javax.wsdl.importDocuments", true);

            Definition definition = reader.readWSDL(null,
                new InputSource(new StringReader(wsdlContent)));

            if(definition == null) {
                result.getIssues().add(new ValidationIssue("ERROR",
                    "Failed to parse WSDL document"));
                result.setValid(false);
            }

            return definition;

        } catch(WSDLException e) {
            result.getIssues().add(new ValidationIssue("ERROR",
                "WSDL parsing error: " + e.getMessage()));
            result.setValid(false);
            return null;
        }
    }

    private WsdlMetadata extractMetadata(Definition definition) {
        WsdlMetadata metadata = new WsdlMetadata();

        // Target namespace
        metadata.setTargetNamespace(definition.getTargetNamespace());

        // Namespaces
        @SuppressWarnings("unchecked")
        Map<String, String> namespaces = definition.getNamespaces();
        metadata.setNamespaces(namespaces);

        // WSDL version(1.1 is assumed as WSDL4J primarily supports 1.1)
        metadata.setVersion("1.1");

        // Services
        if(definition.getServices() != null) {
            definition.getServices().values().forEach(service -> {
                javax.wsdl.Service wsdlService = (javax.wsdl.Service) service;
                ServiceInfo serviceInfo = new ServiceInfo(wsdlService.getQName().getLocalPart());

                if(wsdlService.getPorts() != null) {
                    wsdlService.getPorts().values().forEach(port -> {
                        javax.wsdl.Port wsdlPort = (javax.wsdl.Port) port;
                        PortInfo portInfo = new PortInfo(
                            wsdlPort.getName(),
                            wsdlPort.getBinding().getQName().toString()
                       );

                        // Extract address from extensibility elements
                        wsdlPort.getExtensibilityElements().forEach(elem -> {
                            if(elem instanceof javax.wsdl.extensions.soap.SOAPAddress) {
                                javax.wsdl.extensions.soap.SOAPAddress soapAddr =
                                    (javax.wsdl.extensions.soap.SOAPAddress) elem;
                                portInfo.setAddress(soapAddr.getLocationURI());
                            }
                        });

                        serviceInfo.getPorts().add(portInfo);
                    });
                }

                metadata.getServices().add(serviceInfo);
            });
        }

        // Port Types
        if(definition.getPortTypes() != null) {
            definition.getPortTypes().values().forEach(portType -> {
                javax.wsdl.PortType wsdlPortType = (javax.wsdl.PortType) portType;
                PortTypeInfo portTypeInfo = new PortTypeInfo(wsdlPortType.getQName().getLocalPart());

                if(wsdlPortType.getOperations() != null) {
                    wsdlPortType.getOperations().forEach(op -> {
                        javax.wsdl.Operation wsdlOp = (javax.wsdl.Operation) op;
                        OperationInfo opInfo = new OperationInfo(wsdlOp.getName());

                        if(wsdlOp.getInput() != null && wsdlOp.getInput().getMessage() != null) {
                            opInfo.setInputMessage(wsdlOp.getInput().getMessage().getQName().toString());
                        }

                        if(wsdlOp.getOutput() != null && wsdlOp.getOutput().getMessage() != null) {
                            opInfo.setOutputMessage(wsdlOp.getOutput().getMessage().getQName().toString());
                        }

                        if(wsdlOp.getFaults() != null) {
                            wsdlOp.getFaults().values().forEach(fault -> {
                                javax.wsdl.Fault wsdlFault = (javax.wsdl.Fault) fault;
                                if(wsdlFault.getMessage() != null) {
                                    opInfo.getFaultMessages().add(
                                        wsdlFault.getMessage().getQName().toString()
                                   );
                                }
                            });
                        }

                        portTypeInfo.getOperations().add(opInfo);
                    });
                }

                metadata.getPortTypes().add(portTypeInfo);
            });
        }

        // Messages
        if(definition.getMessages() != null) {
            definition.getMessages().values().forEach(message -> {
                javax.wsdl.Message wsdlMessage = (javax.wsdl.Message) message;
                MessageInfo messageInfo = new MessageInfo(wsdlMessage.getQName().getLocalPart());

                if(wsdlMessage.getParts() != null) {
                    wsdlMessage.getParts().values().forEach(part -> {
                        javax.wsdl.Part wsdlPart = (javax.wsdl.Part) part;
                        PartInfo partInfo = new PartInfo(wsdlPart.getName());

                        if(wsdlPart.getTypeName() != null) {
                            partInfo.setType(wsdlPart.getTypeName().toString());
                        }

                        if(wsdlPart.getElementName() != null) {
                            partInfo.setElement(wsdlPart.getElementName().toString());
                        }

                        messageInfo.getParts().add(partInfo);
                    });
                }

                metadata.getMessages().add(messageInfo);
            });
        }

        return metadata;
    }

    private void performSemanticValidation(Definition definition, ValidationResult result) {
        // Check for empty services
        if(definition.getServices() == null || definition.getServices().isEmpty()) {
            result.getIssues().add(new ValidationIssue("WARNING",
                "No services defined in WSDL"));
        }

        // Check for empty port types
        if(definition.getPortTypes() == null || definition.getPortTypes().isEmpty()) {
            result.getIssues().add(new ValidationIssue("WARNING",
                "No port types defined in WSDL"));
        }

        // Check for orphaned messages
        Set<QName> referencedMessages = new HashSet<>();
        if(definition.getPortTypes() != null) {
            definition.getPortTypes().values().forEach(portType -> {
                javax.wsdl.PortType wsdlPortType = (javax.wsdl.PortType) portType;
                if(wsdlPortType.getOperations() != null) {
                    wsdlPortType.getOperations().forEach(op -> {
                        javax.wsdl.Operation wsdlOp = (javax.wsdl.Operation) op;
                        if(wsdlOp.getInput() != null && wsdlOp.getInput().getMessage() != null) {
                            referencedMessages.add(wsdlOp.getInput().getMessage().getQName());
                        }
                        if(wsdlOp.getOutput() != null && wsdlOp.getOutput().getMessage() != null) {
                            referencedMessages.add(wsdlOp.getOutput().getMessage().getQName());
                        }
                    });
                }
            });
        }

        if(definition.getMessages() != null) {
            definition.getMessages().values().forEach(message -> {
                javax.wsdl.Message wsdlMessage = (javax.wsdl.Message) message;
                if(!referencedMessages.contains(wsdlMessage.getQName())) {
                    result.getIssues().add(new ValidationIssue("WARNING",
                        "Message '" + wsdlMessage.getQName().getLocalPart() +
                        "' is defined but not used by any operation"));
                }
            });
        }
    }

    private void checkBestPractices(Definition definition, String wsdlContent, ValidationResult result) {
        // Check for target namespace
        if(definition.getTargetNamespace() == null || definition.getTargetNamespace().isEmpty()) {
            result.getIssues().add(new ValidationIssue("WARNING",
                "WSDL should define a targetNamespace"));
        }

        // Check for documentation
        if(!wsdlContent.contains("<documentation>") && !wsdlContent.contains("<wsdl:documentation>")) {
            result.getIssues().add(new ValidationIssue("INFO",
                "Consider adding documentation elements to describe services and operations"));
        }

        // Check naming conventions
        if(definition.getServices() != null) {
            definition.getServices().values().forEach(service -> {
                javax.wsdl.Service wsdlService = (javax.wsdl.Service) service;
                String name = wsdlService.getQName().getLocalPart();
                if(!name.endsWith("Service")) {
                    result.getIssues().add(new ValidationIssue("INFO",
                        "Service name '" + name + "' should end with 'Service' by convention"));
                }
            });
        }

        // Check for SOAP 1.2 usage
        if(wsdlContent.contains("soap12:")) {
            result.getIssues().add(new ValidationIssue("INFO",
                "WSDL uses SOAP 1.2. Ensure all clients support this version"));
        }

        // Check for very large WSDL
        if(wsdlContent.length() > 100000) {
            result.getIssues().add(new ValidationIssue("WARNING",
                "WSDL is very large(" + (wsdlContent.length() / 1024) +
                " KB). Consider splitting into multiple files"));
        }
    }
}
