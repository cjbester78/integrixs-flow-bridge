package com.integrixs.shared.dto.adapter;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for mapping non - JSON data(JDBC, CSV, etc.) to XML structure
 */
public class XmlMappingConfig {

    private String rootElementName;
    private String rowElementName;
    private String namespace;
    private String namespacePrefix;
    private Map<String, String> fieldToElementMapping = new HashMap<>();
    private Map<String, String> fieldDataTypes = new HashMap<>();
    private boolean includeXmlDeclaration = true;
    private boolean prettyPrint = true;
    private String encoding = "UTF-8";

    // Default constructor
    public XmlMappingConfig() {
    }

    // All args constructor
    public XmlMappingConfig(String rootElementName, String rowElementName, String namespace, String namespacePrefix, Map<String, String> fieldToElementMapping, Map<String, String> fieldDataTypes, boolean includeXmlDeclaration, boolean prettyPrint, String encoding) {
        this.rootElementName = rootElementName;
        this.rowElementName = rowElementName;
        this.namespace = namespace;
        this.namespacePrefix = namespacePrefix;
        this.fieldToElementMapping = fieldToElementMapping != null ? fieldToElementMapping : new HashMap<>();
        this.fieldDataTypes = fieldDataTypes != null ? fieldDataTypes : new HashMap<>();
        this.includeXmlDeclaration = includeXmlDeclaration;
        this.prettyPrint = prettyPrint;
        this.encoding = encoding;
    }

    // Getters
    public String getRootElementName() { return rootElementName; }
    public String getRowElementName() { return rowElementName; }
    public String getNamespace() { return namespace; }
    public String getNamespacePrefix() { return namespacePrefix; }
    public Map<String, String> getFieldToElementMapping() { return fieldToElementMapping; }
    public Map<String, String> getFieldDataTypes() { return fieldDataTypes; }
    public boolean isIncludeXmlDeclaration() { return includeXmlDeclaration; }
    public boolean isPrettyPrint() { return prettyPrint; }
    public String getEncoding() { return encoding; }

    // Setters
    public void setRootElementName(String rootElementName) { this.rootElementName = rootElementName; }
    public void setRowElementName(String rowElementName) { this.rowElementName = rowElementName; }
    public void setNamespace(String namespace) { this.namespace = namespace; }
    public void setNamespacePrefix(String namespacePrefix) { this.namespacePrefix = namespacePrefix; }
    public void setFieldToElementMapping(Map<String, String> fieldToElementMapping) { this.fieldToElementMapping = fieldToElementMapping; }
    public void setFieldDataTypes(Map<String, String> fieldDataTypes) { this.fieldDataTypes = fieldDataTypes; }
    public void setIncludeXmlDeclaration(boolean includeXmlDeclaration) { this.includeXmlDeclaration = includeXmlDeclaration; }
    public void setPrettyPrint(boolean prettyPrint) { this.prettyPrint = prettyPrint; }
    public void setEncoding(String encoding) { this.encoding = encoding; }

    // Builder
    public static XmlMappingConfigBuilder builder() {
        return new XmlMappingConfigBuilder();
    }

    public static class XmlMappingConfigBuilder {
        private String rootElementName;
        private String rowElementName;
        private String namespace;
        private String namespacePrefix;
        private Map<String, String> fieldToElementMapping = new HashMap<>();
        private Map<String, String> fieldDataTypes = new HashMap<>();
        private boolean includeXmlDeclaration = true;
        private boolean prettyPrint = true;
        private String encoding = "UTF-8";

        public XmlMappingConfigBuilder rootElementName(String rootElementName) {
            this.rootElementName = rootElementName;
            return this;
        }

        public XmlMappingConfigBuilder rowElementName(String rowElementName) {
            this.rowElementName = rowElementName;
            return this;
        }

        public XmlMappingConfigBuilder namespace(String namespace) {
            this.namespace = namespace;
            return this;
        }

        public XmlMappingConfigBuilder namespacePrefix(String namespacePrefix) {
            this.namespacePrefix = namespacePrefix;
            return this;
        }

        public XmlMappingConfigBuilder fieldToElementMapping(Map<String, String> fieldToElementMapping) {
            this.fieldToElementMapping = fieldToElementMapping;
            return this;
        }

        public XmlMappingConfigBuilder fieldDataTypes(Map<String, String> fieldDataTypes) {
            this.fieldDataTypes = fieldDataTypes;
            return this;
        }

        public XmlMappingConfigBuilder includeXmlDeclaration(boolean includeXmlDeclaration) {
            this.includeXmlDeclaration = includeXmlDeclaration;
            return this;
        }

        public XmlMappingConfigBuilder prettyPrint(boolean prettyPrint) {
            this.prettyPrint = prettyPrint;
            return this;
        }

        public XmlMappingConfigBuilder encoding(String encoding) {
            this.encoding = encoding;
            return this;
        }

        public XmlMappingConfig build() {
            return new XmlMappingConfig(rootElementName, rowElementName, namespace, namespacePrefix, fieldToElementMapping, fieldDataTypes, includeXmlDeclaration, prettyPrint, encoding);
        }
    }
}
