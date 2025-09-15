package com.integrixs.backend.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StructureValidationResponse {

    private boolean valid;
    private String message;

    @Builder.Default
    private List<Issue> issues = new ArrayList<>();

    private WsdlMetadata wsdlMetadata;
    private JsonSchemaMetadata jsonSchemaMetadata;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Issue {
        private IssueType type;
        private String message;
        private Integer line;
        private Integer column;
        private String path;
    }

    public enum IssueType {
        ERROR, WARNING, INFO
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WsdlMetadata {
        private String targetNamespace;
        private String version;
        private Map<String, String> namespaces = new HashMap<>();
        private List<ServiceInfo> services = new ArrayList<>();
        private List<PortTypeInfo> portTypes = new ArrayList<>();
        private List<MessageInfo> messages = new ArrayList<>();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceInfo {
        private String name;
        private List<PortInfo> ports = new ArrayList<>();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PortInfo {
        private String name;
        private String binding;
        private String address;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PortTypeInfo {
        private String name;
        private List<OperationInfo> operations = new ArrayList<>();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OperationInfo {
        private String name;
        private String inputMessage;
        private String outputMessage;
        private List<String> faultMessages = new ArrayList<>();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageInfo {
        private String name;
        private List<PartInfo> parts = new ArrayList<>();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PartInfo {
        private String name;
        private String type;
        private String element;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JsonSchemaMetadata {
        private String schema;
        private String title;
        private String description;
        private String type;
        private Map<String, Object> properties;
        private List<String> required;
    }
}
