package com.integrixs.engine.xml;

import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Converts XML data to CSV format
 */
@Service
public class XmlToCsvConverter {

    /**
     * Configuration for CSV generation
     */
    public static class CsvGenerationConfig {
        private String delimiter = ",";
        private String lineTerminator = "\n";
        private boolean includeHeaders = true;
        private String quoteCharacter = "\"";
        private boolean quoteAllFields = false;
        private Map<String, String> fieldMappings; // XML path to CSV column mapping
        private List<String> columnOrder; // Explicit column ordering
        private boolean trimWhitespace = false;
        private boolean escapeUnicode = false;
        private boolean preserveLineBreaks = false;
        private boolean formatNumbers = false;
        private DecimalFormat decimalFormat;
        private SimpleDateFormat dateFormat;

        // Builder pattern for easy configuration
        public static CsvGenerationConfig builder() {
            return new CsvGenerationConfig();
        }

        public CsvGenerationConfig delimiter(String delimiter) {
            this.delimiter = delimiter;
            return this;
        }

        public CsvGenerationConfig lineTerminator(String lineTerminator) {
            this.lineTerminator = lineTerminator;
            return this;
        }

        public CsvGenerationConfig includeHeaders(boolean includeHeaders) {
            this.includeHeaders = includeHeaders;
            return this;
        }

        public CsvGenerationConfig quoteCharacter(String quoteCharacter) {
            this.quoteCharacter = quoteCharacter;
            return this;
        }

        public CsvGenerationConfig quoteAllFields(boolean quoteAllFields) {
            this.quoteAllFields = quoteAllFields;
            return this;
        }

        public CsvGenerationConfig fieldMappings(Map<String, String> fieldMappings) {
            this.fieldMappings = fieldMappings;
            return this;
        }

        public CsvGenerationConfig columnOrder(List<String> columnOrder) {
            this.columnOrder = columnOrder;
            return this;
        }

        public CsvGenerationConfig trimWhitespace(boolean trimWhitespace) {
            this.trimWhitespace = trimWhitespace;
            return this;
        }

        public CsvGenerationConfig escapeUnicode(boolean escapeUnicode) {
            this.escapeUnicode = escapeUnicode;
            return this;
        }

        public CsvGenerationConfig preserveLineBreaks(boolean preserveLineBreaks) {
            this.preserveLineBreaks = preserveLineBreaks;
            return this;
        }

        public CsvGenerationConfig formatNumbers(boolean formatNumbers) {
            this.formatNumbers = formatNumbers;
            return this;
        }

        public CsvGenerationConfig decimalFormat(DecimalFormat decimalFormat) {
            this.decimalFormat = decimalFormat;
            return this;
        }

        public CsvGenerationConfig dateFormat(SimpleDateFormat dateFormat) {
            this.dateFormat = dateFormat;
            return this;
        }

        // Getters
        public String getDelimiter() { return delimiter; }
        public String getLineTerminator() { return lineTerminator; }
        public boolean isIncludeHeaders() { return includeHeaders; }
        public String getQuoteCharacter() { return quoteCharacter; }
        public boolean isQuoteAllFields() { return quoteAllFields; }
        public Map<String, String> getFieldMappings() { return fieldMappings; }
        public List<String> getColumnOrder() { return columnOrder; }
        public boolean isTrimWhitespace() { return trimWhitespace; }
        public boolean isEscapeUnicode() { return escapeUnicode; }
        public boolean isPreserveLineBreaks() { return preserveLineBreaks; }
        public boolean isFormatNumbers() { return formatNumbers; }
        public DecimalFormat getDecimalFormat() { return decimalFormat; }
        public SimpleDateFormat getDateFormat() { return dateFormat; }
    }

    /**
     * Convert XML to CSV format
     */
    public String convertToCsv(String xmlContent, CsvGenerationConfig config) throws XmlConversionException {
        try {
            // Parse XML
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlContent)));

            Element rootElement = doc.getDocumentElement();
            List<Map<String, String>> records = extractRecords(rootElement, config);

            if(records.isEmpty()) {
                return "";
            }

            // Generate CSV
            StringWriter csvWriter = new StringWriter();

            // Determine columns
            List<String> columns = determineColumns(records, config);

            // Write headers if requested
            if(config.isIncludeHeaders()) {
                csvWriter.write(generateCsvLine(columns, columns, config));
                csvWriter.write(config.getLineTerminator());
            }

            // Write data rows
            for(Map<String, String> record : records) {
                List<String> values = columns.stream()
                    .map(col -> record.getOrDefault(col, ""))
                    .collect(Collectors.toList());
                csvWriter.write(generateCsvLine(values, columns, config));
                csvWriter.write(config.getLineTerminator());
            }

            return csvWriter.toString();

        } catch(Exception e) {
            throw new XmlConversionException("Failed to convert XML to CSV", e);
        }
    }

    /**
     * Extract records from XML
     */
    private List<Map<String, String>> extractRecords(Element root, CsvGenerationConfig config) {
        List<Map<String, String>> records = new ArrayList<>();

        // Find record elements
        List<Element> recordElements = findRecordElements(root);

        for(Element recordElement : recordElements) {
            Map<String, String> record = extractRecord(recordElement, config);
            if(!record.isEmpty()) {
                records.add(record);
            }
        }

        return records;
    }

    /**
     * Find record elements in XML
     */
    private List<Element> findRecordElements(Element root) {
        List<Element> records = new ArrayList<>();

        // Check if root contains repeating elements
        Map<String, List<Element>> childrenByTag = new HashMap<>();
        NodeList children = root.getChildNodes();

        for(int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if(child.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element) child;
                String tagName = childElement.getTagName();
                childrenByTag.computeIfAbsent(tagName, k -> new ArrayList<>()).add(childElement);
            }
        }

        // Find the repeating element(likely the record element)
        for(Map.Entry<String, List<Element>> entry : childrenByTag.entrySet()) {
            if(entry.getValue().size() > 1) {
                records.addAll(entry.getValue());
                break;
            }
        }

        // If no repeating elements, treat root as single record
        if(records.isEmpty()) {
            records.add(root);
        }

        return records;
    }

    /**
     * Extract a single record from XML element
     */
    private Map<String, String> extractRecord(Element element, CsvGenerationConfig config) {
        Map<String, String> record = new HashMap<>();

        if(config.getFieldMappings() != null && !config.getFieldMappings().isEmpty()) {
            // Use explicit field mappings
            for(Map.Entry<String, String> mapping : config.getFieldMappings().entrySet()) {
                String xmlPath = mapping.getKey();
                String csvColumn = mapping.getValue();
                String value = getValueByPath(element, xmlPath);
                if(value != null) {
                    record.put(csvColumn, value);
                }
            }
        } else {
            // Auto - extract all leaf values
            extractLeafValues(element, "", record);
        }

        return record;
    }

    /**
     * Extract all leaf values from element
     */
    private void extractLeafValues(Element element, String prefix, Map<String, String> values) {
        // Process attributes
        for(int i = 0; i < element.getAttributes().getLength(); i++) {
            Node attr = element.getAttributes().item(i);
            if(!attr.getNodeName().startsWith("xmlns")) {
                String key = prefix + "@" + attr.getNodeName();
                values.put(key, attr.getNodeValue());
            }
        }

        // Process child elements
        NodeList children = element.getChildNodes();
        boolean hasElementChildren = false;

        for(int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if(child.getNodeType() == Node.ELEMENT_NODE) {
                hasElementChildren = true;
                Element childElement = (Element) child;
                String childPrefix = prefix.isEmpty() ?
                    childElement.getTagName() :
                    prefix + "." + childElement.getTagName();

                if(!hasChildElements(childElement)) {
                    // Leaf element
                    values.put(childPrefix, childElement.getTextContent().trim());
                } else {
                    // Recurse
                    extractLeafValues(childElement, childPrefix, values);
                }
            }
        }

        // If no element children and has text content, add text value
        if(!hasElementChildren) {
            String text = element.getTextContent().trim();
            if(!text.isEmpty()) {
                values.put(prefix.isEmpty() ? "_text" : prefix, text);
            }
        }
    }

    /**
     * Get value by XML path
     */
    private String getValueByPath(Element element, String path) {
        String[] parts = path.split("\\.");
        Node current = element;

        for(String part : parts) {
            if(part.startsWith("@")) {
                // Attribute
                if(current.getNodeType() == Node.ELEMENT_NODE) {
                    return((Element) current).getAttribute(part.substring(1));
                }
                return null;
            } else {
                // Element
                if(current.getNodeType() == Node.ELEMENT_NODE) {
                    NodeList children = current.getChildNodes();
                    boolean found = false;
                    for(int i = 0; i < children.getLength(); i++) {
                        Node child = children.item(i);
                        if(child.getNodeType() == Node.ELEMENT_NODE &&
                            child.getNodeName().equals(part)) {
                            current = child;
                            found = true;
                            break;
                        }
                    }
                    if(!found) {
                        return null;
                    }
                }
            }
        }

        return current.getTextContent().trim();
    }

    /**
     * Check if element has child elements
     */
    private boolean hasChildElements(Element element) {
        NodeList children = element.getChildNodes();
        for(int i = 0; i < children.getLength(); i++) {
            if(children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determine column order for CSV
     */
    private List<String> determineColumns(List<Map<String, String>> records, CsvGenerationConfig config) {
        if(config.getColumnOrder() != null && !config.getColumnOrder().isEmpty()) {
            return config.getColumnOrder();
        }

        // Collect all unique columns from records
        Set<String> allColumns = new LinkedHashSet<>();
        for(Map<String, String> record : records) {
            allColumns.addAll(record.keySet());
        }

        return new ArrayList<>(allColumns);
    }

    /**
     * Generate a CSV line
     */
    private String generateCsvLine(List<String> values, List<String> columns, CsvGenerationConfig config) {
        return values.stream()
            .map(value -> formatCsvField(value, config))
            .collect(Collectors.joining(config.getDelimiter()));
    }

    /**
     * Format a CSV field value
     */
    private String formatCsvField(String value, CsvGenerationConfig config) {
        if(value == null) {
            value = "";
        }

        // Handle edge cases
        // 1. Trim whitespace if configured
        if(config.isTrimWhitespace()) {
            value = value.trim();
        }

        // 2. Handle special characters and encoding
        value = handleSpecialCharacters(value, config);

        // 3. Handle numeric formatting
        if(config.isFormatNumbers() && isNumeric(value)) {
            value = formatNumericValue(value, config);
        }

        // 4. Handle date formatting
        if(config.getDateFormat() != null && isDate(value)) {
            value = formatDateValue(value, config);
        }

        boolean needsQuoting = config.isQuoteAllFields() ||
            value.contains(config.getDelimiter()) ||
            value.contains(config.getQuoteCharacter()) ||
            value.contains("\n") ||
            value.contains("\r") ||
            value.startsWith(" ") ||
            value.endsWith(" ") ||
            value.startsWith(" = ") || // Excel formula protection
            value.startsWith(" + ") || // Excel formula protection
            value.startsWith("-") || // Excel formula protection
            value.startsWith("@"); // Excel formula protection

        if(needsQuoting) {
            // Escape quotes by doubling them
            value = value.replace(config.getQuoteCharacter(),
                config.getQuoteCharacter() + config.getQuoteCharacter());
            return config.getQuoteCharacter() + value + config.getQuoteCharacter();
        }

        return value;
    }

    /**
     * Handle special characters in CSV values
     */
    private String handleSpecialCharacters(String value, CsvGenerationConfig config) {
        // Replace null bytes which can cause issues
        value = value.replace("\0", "");

        // Handle Unicode characters if needed
        if(config.isEscapeUnicode()) {
            StringBuilder escaped = new StringBuilder();
            for(char c : value.toCharArray()) {
                if(c > 127) {
                    escaped.append("\\u").append(String.format("%04x", (int) c));
                } else {
                    escaped.append(c);
                }
            }
            value = escaped.toString();
        }

        // Handle line breaks based on configuration
        if(config.isPreserveLineBreaks()) {
            // Keep line breaks but ensure they're properly quoted
        } else {
            // Replace line breaks with spaces
            value = value.replace("\r\n", " ").replace("\n", " ").replace("\r", " ");
        }

        return value;
    }

    /**
     * Check if value is numeric
     */
    private boolean isNumeric(String value) {
        if(value == null || value.isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(value);
            return true;
        } catch(NumberFormatException e) {
            return false;
        }
    }

    /**
     * Format numeric value
     */
    private String formatNumericValue(String value, CsvGenerationConfig config) {
        try {
            double num = Double.parseDouble(value);
            if(config.getDecimalFormat() != null) {
                return config.getDecimalFormat().format(num);
            }
            // Remove unnecessary decimal zeros
            if(num == Math.floor(num) && !Double.isInfinite(num)) {
                return String.valueOf((long) num);
            }
            return value;
        } catch(Exception e) {
            return value;
        }
    }

    /**
     * Check if value is a date
     */
    private boolean isDate(String value) {
        if(value == null || value.isEmpty()) {
            return false;
        }
        // Simple date pattern matching
        return value.matches("\\d {4} - \\d {2} - \\d {2}.*") ||
               value.matches("\\d {2}/\\d {2}/\\d {4}.*");
    }

    /**
     * Format date value
     */
    private String formatDateValue(String value, CsvGenerationConfig config) {
        // This is a simplified implementation
        // In production, you would use proper date parsing and formatting
        return value;
    }
}
