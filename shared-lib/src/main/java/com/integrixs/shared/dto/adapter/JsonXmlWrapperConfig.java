package com.integrixs.shared.dto.adapter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Configuration for wrapping JSON data in XML structure with namespace support
 */
public class JsonXmlWrapperConfig {

    /**
     * Root element name for the XML document
     * Example: "OrderMessage", "CustomerData", "ProductCatalog"
     */
    private String rootElementName;

    /**
     * XML namespace URI
     * Example: "http://company.com/orders/v1"
     */
    private String namespaceUri;

    /**
     * XML namespace prefix
     * Example: "ord", "cust", "prod"
     */
    private String namespacePrefix;

    /**
     * Whether to include XML declaration
     */
    private boolean includeXmlDeclaration = true;

    /**
     * Custom element names for JSON arrays
     * Key: JSON array field path(e.g., "orders.items")
     * Value: Custom wrapper element name(e.g., "orderItem")
     */
    private Map<String, String> arrayElementNames = new HashMap<>();

    /**
     * Additional namespaces to declare
     * Key: Namespace prefix
     * Value: Namespace URI
     */
    private Map<String, String> additionalNamespaces = new HashMap<>();

    /**
     * Whether to preserve JSON null values as empty XML elements
     */
    private boolean preserveNullValues = false;

    /**
     * Whether to convert JSON property names to XML - friendly names
     * (e.g., "customer_id" to "customerId")
     */
    private boolean convertPropertyNames = true;

    /**
     * Whether to pretty print the XML
     */
    private boolean prettyPrint = true;

    /**
     * Character encoding for XML
     */
    private String encoding = "UTF-8";

    // Default constructor
    public JsonXmlWrapperConfig() {
    }

    // All args constructor
    public JsonXmlWrapperConfig(String rootElementName, String namespaceUri, String namespacePrefix,
                               boolean includeXmlDeclaration, Map<String, String> arrayElementNames,
                               Map<String, String> additionalNamespaces, boolean preserveNullValues,
                               boolean convertPropertyNames, boolean prettyPrint, String encoding) {
        this.rootElementName = rootElementName;
        this.namespaceUri = namespaceUri;
        this.namespacePrefix = namespacePrefix;
        this.includeXmlDeclaration = includeXmlDeclaration;
        this.arrayElementNames = arrayElementNames != null ? arrayElementNames : new HashMap<>();
        this.additionalNamespaces = additionalNamespaces != null ? additionalNamespaces : new HashMap<>();
        this.preserveNullValues = preserveNullValues;
        this.convertPropertyNames = convertPropertyNames;
        this.prettyPrint = prettyPrint;
        this.encoding = encoding;
    }

    // Getters
    public String getRootElementName() {
        return rootElementName;
    }

    public String getNamespaceUri() {
        return namespaceUri;
    }

    public String getNamespacePrefix() {
        return namespacePrefix;
    }

    public boolean isIncludeXmlDeclaration() {
        return includeXmlDeclaration;
    }

    public Map<String, String> getArrayElementNames() {
        return arrayElementNames;
    }

    public Map<String, String> getAdditionalNamespaces() {
        return additionalNamespaces;
    }

    public boolean isPreserveNullValues() {
        return preserveNullValues;
    }

    public boolean isConvertPropertyNames() {
        return convertPropertyNames;
    }

    public boolean isPrettyPrint() {
        return prettyPrint;
    }

    public String getEncoding() {
        return encoding;
    }

    // Setters
    public void setRootElementName(String rootElementName) {
        this.rootElementName = rootElementName;
    }

    public void setNamespaceUri(String namespaceUri) {
        this.namespaceUri = namespaceUri;
    }

    public void setNamespacePrefix(String namespacePrefix) {
        this.namespacePrefix = namespacePrefix;
    }

    public void setIncludeXmlDeclaration(boolean includeXmlDeclaration) {
        this.includeXmlDeclaration = includeXmlDeclaration;
    }

    public void setArrayElementNames(Map<String, String> arrayElementNames) {
        this.arrayElementNames = arrayElementNames;
    }

    public void setAdditionalNamespaces(Map<String, String> additionalNamespaces) {
        this.additionalNamespaces = additionalNamespaces;
    }

    public void setPreserveNullValues(boolean preserveNullValues) {
        this.preserveNullValues = preserveNullValues;
    }

    public void setConvertPropertyNames(boolean convertPropertyNames) {
        this.convertPropertyNames = convertPropertyNames;
    }

    public void setPrettyPrint(boolean prettyPrint) {
        this.prettyPrint = prettyPrint;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    // Builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String rootElementName;
        private String namespaceUri;
        private String namespacePrefix;
        private boolean includeXmlDeclaration = true;
        private Map<String, String> arrayElementNames = new HashMap<>();
        private Map<String, String> additionalNamespaces = new HashMap<>();
        private boolean preserveNullValues = false;
        private boolean convertPropertyNames = true;
        private boolean prettyPrint = true;
        private String encoding = "UTF-8";

        public Builder rootElementName(String rootElementName) {
            this.rootElementName = rootElementName;
            return this;
        }

        public Builder namespaceUri(String namespaceUri) {
            this.namespaceUri = namespaceUri;
            return this;
        }

        public Builder namespacePrefix(String namespacePrefix) {
            this.namespacePrefix = namespacePrefix;
            return this;
        }

        public Builder includeXmlDeclaration(boolean includeXmlDeclaration) {
            this.includeXmlDeclaration = includeXmlDeclaration;
            return this;
        }

        public Builder arrayElementNames(Map<String, String> arrayElementNames) {
            this.arrayElementNames = arrayElementNames;
            return this;
        }

        public Builder additionalNamespaces(Map<String, String> additionalNamespaces) {
            this.additionalNamespaces = additionalNamespaces;
            return this;
        }

        public Builder preserveNullValues(boolean preserveNullValues) {
            this.preserveNullValues = preserveNullValues;
            return this;
        }

        public Builder convertPropertyNames(boolean convertPropertyNames) {
            this.convertPropertyNames = convertPropertyNames;
            return this;
        }

        public Builder prettyPrint(boolean prettyPrint) {
            this.prettyPrint = prettyPrint;
            return this;
        }

        public Builder encoding(String encoding) {
            this.encoding = encoding;
            return this;
        }

        public JsonXmlWrapperConfig build() {
            return new JsonXmlWrapperConfig(rootElementName, namespaceUri, namespacePrefix,
                                          includeXmlDeclaration, arrayElementNames, additionalNamespaces,
                                          preserveNullValues, convertPropertyNames, prettyPrint, encoding);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JsonXmlWrapperConfig that = (JsonXmlWrapperConfig) o;
        return includeXmlDeclaration == that.includeXmlDeclaration &&
               preserveNullValues == that.preserveNullValues &&
               convertPropertyNames == that.convertPropertyNames &&
               prettyPrint == that.prettyPrint &&
               Objects.equals(rootElementName, that.rootElementName) &&
               Objects.equals(namespaceUri, that.namespaceUri) &&
               Objects.equals(namespacePrefix, that.namespacePrefix) &&
               Objects.equals(arrayElementNames, that.arrayElementNames) &&
               Objects.equals(additionalNamespaces, that.additionalNamespaces) &&
               Objects.equals(encoding, that.encoding);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rootElementName, namespaceUri, namespacePrefix, includeXmlDeclaration,
                          arrayElementNames, additionalNamespaces, preserveNullValues,
                          convertPropertyNames, prettyPrint, encoding);
    }

    @Override
    public String toString() {
        return "JsonXmlWrapperConfig{" +
                "rootElementName='" + rootElementName + '\'' +
                ", namespaceUri='" + namespaceUri + '\'' +
                ", namespacePrefix='" + namespacePrefix + '\'' +
                ", includeXmlDeclaration=" + includeXmlDeclaration +
                ", arrayElementNames=" + arrayElementNames +
                ", additionalNamespaces=" + additionalNamespaces +
                ", preserveNullValues=" + preserveNullValues +
                ", convertPropertyNames=" + convertPropertyNames +
                ", prettyPrint=" + prettyPrint +
                ", encoding='" + encoding + '\'' +
                '}';
    }
}
