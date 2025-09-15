package com.integrixs.webclient.infrastructure.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Message transformation utility
 */
@Component
public class MessageTransformer {

    private static final Logger logger = LoggerFactory.getLogger(MessageTransformer.class);

    private final ObjectMapper jsonMapper;
    private final XmlMapper xmlMapper;

    public MessageTransformer() {
        this.jsonMapper = new ObjectMapper();
        this.xmlMapper = new XmlMapper();
    }

    /**
     * Transform payload between formats
     * @param payload Source payload
     * @param sourceFormat Source format
     * @param targetFormat Target format
     * @return Transformed payload
     */
    public Object transform(Object payload, String sourceFormat, String targetFormat) {
        logger.debug("Transforming payload from {} to {}", sourceFormat, targetFormat);

        if(sourceFormat == null || targetFormat == null || sourceFormat.equals(targetFormat)) {
            return payload;
        }

        try {
            // Handle common transformations
            if(isJsonFormat(sourceFormat) && isXmlFormat(targetFormat)) {
                return jsonToXml(payload);
            } else if(isXmlFormat(sourceFormat) && isJsonFormat(targetFormat)) {
                return xmlToJson(payload);
            } else {
                logger.warn("Unsupported transformation from {} to {}", sourceFormat, targetFormat);
                return payload;
            }
        } catch(Exception e) {
            logger.error("Error transforming payload: {}", e.getMessage(), e);
            throw new RuntimeException("Transformation failed", e);
        }
    }

    private boolean isJsonFormat(String format) {
        return format != null && format.toLowerCase().contains("json");
    }

    private boolean isXmlFormat(String format) {
        return format != null && format.toLowerCase().contains("xml");
    }

    private String jsonToXml(Object payload) throws Exception {
        Object jsonObject = payload;
        if(payload instanceof String) {
            jsonObject = jsonMapper.readValue((String) payload, Object.class);
        }
        return xmlMapper.writeValueAsString(jsonObject);
    }

    private Object xmlToJson(Object payload) throws Exception {
        if(!(payload instanceof String)) {
            throw new IllegalArgumentException("XML payload must be a string");
        }

        Object xmlObject = xmlMapper.readValue((String) payload, Object.class);
        return jsonMapper.writeValueAsString(xmlObject);
    }
}
