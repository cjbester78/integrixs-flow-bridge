package com.integrixs.backend.api.dto.response;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * Response for structure validation
 */
public class StructureValidationResponse {
    private boolean valid;
    private String message;
    private List<Issue> issues = new ArrayList<>();
    private WsdlMetadata wsdlMetadata;
    private String xmlSchemaContent;
    private JsonSchemaMetadata jsonSchemaMetadata;

    // Inner classes
    public static class Issue {
        private IssueType type;
        private String message;
        private String location;
        private Integer line;
        private Integer column;
        private String path;

        public IssueType getType() {
            return type;
        }

        public void setType(IssueType type) {
            this.type = type;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public Integer getLine() {
            return line;
        }

        public void setLine(Integer line) {
            this.line = line;
        }

        public Integer getColumn() {
            return column;
        }

        public void setColumn(Integer column) {
            this.column = column;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        // Builder
        public static IssueBuilder builder() {
            return new IssueBuilder();
        }

        public static class IssueBuilder {
            private IssueType type;
            private String message;
            private String location;
            private Integer line;
            private Integer column;
            private String path;

            public IssueBuilder type(IssueType type) {
                this.type = type;
                return this;
            }

            public IssueBuilder message(String message) {
                this.message = message;
                return this;
            }

            public IssueBuilder location(String location) {
                this.location = location;
                return this;
            }

            public IssueBuilder line(Integer line) {
                this.line = line;
                return this;
            }

            public IssueBuilder column(Integer column) {
                this.column = column;
                return this;
            }

            public IssueBuilder path(String path) {
                this.path = path;
                return this;
            }

            public Issue build() {
                Issue result = new Issue();
                result.setType(this.type);
                result.setMessage(this.message);
                result.setLocation(this.location);
                result.setLine(this.line);
                result.setColumn(this.column);
                result.setPath(this.path);
                return result;
            }
        }
    }

    public static class WsdlMetadata {
        private String targetNamespace;
        private String version;
        private String documentation;
        private List<ServiceInfo> services = new ArrayList<>();
        private List<PortTypeInfo> portTypes = new ArrayList<>();
        private List<MessageInfo> messages = new ArrayList<>();
        private Map<String, String> namespaces = new HashMap<>();

        public Map<String, String> getNamespaces() {
            return namespaces;
        }

        public void setNamespaces(Map<String, String> namespaces) {
            this.namespaces = namespaces;
        }


        public String getTargetNamespace() {
            return targetNamespace;
        }

        public void setTargetNamespace(String targetNamespace) {
            this.targetNamespace = targetNamespace;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getDocumentation() {
            return documentation;
        }

        public void setDocumentation(String documentation) {
            this.documentation = documentation;
        }

        public List<ServiceInfo> getServices() {
            return services;
        }

        public void setServices(List<ServiceInfo> services) {
            this.services = services;
        }

        public List<PortTypeInfo> getPortTypes() {
            return portTypes;
        }

        public void setPortTypes(List<PortTypeInfo> portTypes) {
            this.portTypes = portTypes;
        }

        public List<MessageInfo> getMessages() {
            return messages;
        }

        public void setMessages(List<MessageInfo> messages) {
            this.messages = messages;
        }
    }

    public static class ServiceInfo {
        private String name;
        private List<PortInfo> ports = new ArrayList<>();

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<PortInfo> getPorts() {
            return ports;
        }

        public void setPorts(List<PortInfo> ports) {
            this.ports = ports;
        }
    }

    public static class PortInfo {
        private String name;
        private String binding;
        private String address;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getBinding() {
            return binding;
        }

        public void setBinding(String binding) {
            this.binding = binding;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }
    }

    public static class PortTypeInfo {
        private String name;
        private List<OperationInfo> operations = new ArrayList<>();

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<OperationInfo> getOperations() {
            return operations;
        }

        public void setOperations(List<OperationInfo> operations) {
            this.operations = operations;
        }
    }

    public static class OperationInfo {
        private String name;
        private String inputMessage;
        private String outputMessage;
        private List<String> faultMessages;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getInputMessage() {
            return inputMessage;
        }

        public void setInputMessage(String inputMessage) {
            this.inputMessage = inputMessage;
        }

        public String getOutputMessage() {
            return outputMessage;
        }

        public void setOutputMessage(String outputMessage) {
            this.outputMessage = outputMessage;
        }

        public List<String> getFaultMessages() {
            return faultMessages;
        }

        public void setFaultMessages(List<String> faultMessages) {
            this.faultMessages = faultMessages;
        }
    }

    public static class MessageInfo {
        private String name;
        private List<PartInfo> parts = new ArrayList<>();

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<PartInfo> getParts() {
            return parts;
        }

        public void setParts(List<PartInfo> parts) {
            this.parts = parts;
        }
    }

    public static class PartInfo {
        private String name;
        private String type;
        private String element;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getElement() {
            return element;
        }

        public void setElement(String element) {
            this.element = element;
        }
    }

    public static class JsonSchemaMetadata {
        private String schema;
        private String title;
        private String version;
        private String description;

        public String getSchema() {
            return schema;
        }

        public void setSchema(String schema) {
            this.schema = schema;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    public enum IssueType {
        ERROR,
        WARNING,
        INFO
    }

    // Main class getters and setters
    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Issue> getIssues() {
        return issues;
    }

    public void setIssues(List<Issue> issues) {
        this.issues = issues;
    }

    public WsdlMetadata getWsdlMetadata() {
        return wsdlMetadata;
    }

    public void setWsdlMetadata(WsdlMetadata wsdlMetadata) {
        this.wsdlMetadata = wsdlMetadata;
    }

    public String getXmlSchemaContent() {
        return xmlSchemaContent;
    }

    public void setXmlSchemaContent(String xmlSchemaContent) {
        this.xmlSchemaContent = xmlSchemaContent;
    }

    public JsonSchemaMetadata getJsonSchemaMetadata() {
        return jsonSchemaMetadata;
    }

    public void setJsonSchemaMetadata(JsonSchemaMetadata jsonSchemaMetadata) {
        this.jsonSchemaMetadata = jsonSchemaMetadata;
    }

    // Builder
    public static StructureValidationResponseBuilder builder() {
        return new StructureValidationResponseBuilder();
    }

    public static class StructureValidationResponseBuilder {
        private boolean valid;
        private String message;
        private List<Issue> issues;
        private WsdlMetadata wsdlMetadata;
        private String xmlSchemaContent;
        private JsonSchemaMetadata jsonSchemaMetadata;

        public StructureValidationResponseBuilder valid(boolean valid) {
            this.valid = valid;
            return this;
        }

        public StructureValidationResponseBuilder message(String message) {
            this.message = message;
            return this;
        }

        public StructureValidationResponseBuilder issues(List<Issue> issues) {
            this.issues = issues;
            return this;
        }

        public StructureValidationResponseBuilder wsdlMetadata(WsdlMetadata wsdlMetadata) {
            this.wsdlMetadata = wsdlMetadata;
            return this;
        }

        public StructureValidationResponseBuilder xmlSchemaContent(String xmlSchemaContent) {
            this.xmlSchemaContent = xmlSchemaContent;
            return this;
        }

        public StructureValidationResponseBuilder jsonSchemaMetadata(JsonSchemaMetadata jsonSchemaMetadata) {
            this.jsonSchemaMetadata = jsonSchemaMetadata;
            return this;
        }

        public StructureValidationResponse build() {
            StructureValidationResponse result = new StructureValidationResponse();
            result.setValid(this.valid);
            result.setMessage(this.message);
            result.setIssues(this.issues);
            result.setWsdlMetadata(this.wsdlMetadata);
            result.setXmlSchemaContent(this.xmlSchemaContent);
            result.setJsonSchemaMetadata(this.jsonSchemaMetadata);
            return result;
        }
    }

}
