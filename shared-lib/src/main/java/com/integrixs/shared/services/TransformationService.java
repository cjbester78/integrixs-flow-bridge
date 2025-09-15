package com.integrixs.shared.services;

import java.util.Map;

/**
 * Service for transforming data between different formats
 */
public interface TransformationService {

    /**
     * Transform data from one format to another
     * @param data The data to transform
     * @param sourceFormat The source format(e.g., JSON, XML)
     * @param targetFormat The target format(e.g., JSON, XML)
     * @param transformationConfig Additional transformation configuration
     * @return The transformed data
     */
    String transform(String data, String sourceFormat, String targetFormat, Map<String, Object> transformationConfig);

    /**
     * Apply XSLT transformation to XML data
     * @param xmlData The XML data to transform
     * @param xsltTemplate The XSLT template to apply
     * @return The transformed XML data
     */
    String applyXslt(String xmlData, String xsltTemplate);

    /**
     * Apply field mapping transformation
     * @param data The data to transform
     * @param fieldMappings The field mappings to apply
     * @return The transformed data with mapped fields
     */
    String applyFieldMapping(String data, Map<String, String> fieldMappings);

    /**
     * Convert JSON to XML
     * @param jsonData The JSON data to convert
     * @return The XML representation
     */
    String jsonToXml(String jsonData);

    /**
     * Convert XML to JSON
     * @param xmlData The XML data to convert
     * @return The JSON representation
     */
    String xmlToJson(String xmlData);
}
