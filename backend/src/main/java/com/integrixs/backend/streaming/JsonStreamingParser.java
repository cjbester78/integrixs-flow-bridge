package com.integrixs.backend.streaming;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.*;
import java.util.function.Consumer;

/**
 * Streaming JSON parser using Jackson Streaming API
 */
@Component
public class JsonStreamingParser implements StreamingParser<JsonNode> {

    private static final Logger logger = LoggerFactory.getLogger(JsonStreamingParser.class);

    private static final Set<String> SUPPORTED_CONTENT_TYPES = Set.of(
        "application/json",
        "application/vnd.api + json",
        "application/hal + json",
        "text/json"
   );

    @Autowired
    private ObjectMapper objectMapper;

    private JsonFactory jsonFactory;

    public JsonStreamingParser() {
        this.jsonFactory = new JsonFactory();
        // Configure factory for streaming
        this.jsonFactory.configure(JsonFactory.Feature.USE_THREAD_LOCAL_FOR_BUFFER_RECYCLING, true);
    }

    @Override
    public void parse(InputStream inputStream,
                     Consumer<JsonNode> elementProcessor,
                     ProgressCallback progressCallback) throws IOException {

        // Wrap input stream to track progress
        CountingInputStream countingStream = new CountingInputStream(inputStream);

        try(JsonParser parser = jsonFactory.createParser(countingStream)) {
            // Move to first token
            JsonToken token = parser.nextToken();

            if(token == JsonToken.START_ARRAY) {
                // Array of objects
                parseArray(parser, elementProcessor, progressCallback, countingStream);
            } else if(token == JsonToken.START_OBJECT) {
                // Single object or object with nested arrays
                parseObject(parser, elementProcessor, progressCallback, countingStream);
            } else {
                throw new IOException("Expected JSON array or object, got: " + token);
            }
        }
    }

    @Override
    public Class<JsonNode> getElementType() {
        return JsonNode.class;
    }

    @Override
    public boolean canHandle(String contentType) {
        return contentType != null && SUPPORTED_CONTENT_TYPES.stream()
            .anyMatch(type -> contentType.toLowerCase().contains(type));
    }

    /**
     * Parse JSON array in streaming fashion
     */
    private void parseArray(JsonParser parser,
                          Consumer<JsonNode> processor,
                          ProgressCallback progressCallback,
                          CountingInputStream countingStream) throws IOException {

        int elementCount = 0;

        while(parser.nextToken() != JsonToken.END_ARRAY) {
            if(parser.currentToken() == JsonToken.START_OBJECT) {
                // Parse individual object
                JsonNode element = objectMapper.readTree(parser);
                processor.accept(element);
                elementCount++;

                // Progress callback
                if(progressCallback != null && elementCount % 100 == 0) {
                    progressCallback.onProgress(countingStream.getCount(), -1);
                }
            }
        }

        logger.debug("Parsed {} array elements", elementCount);
    }

    /**
     * Parse JSON object looking for nested arrays
     */
    private void parseObject(JsonParser parser,
                           Consumer<JsonNode> processor,
                           ProgressCallback progressCallback,
                           CountingInputStream countingStream) throws IOException {

        Stack<String> path = new Stack<>();
        int depth = 1; // We're already in an object

        while(depth > 0) {
            JsonToken token = parser.nextToken();

            if(token == null) {
                break;
            }

            switch(token) {
                case FIELD_NAME:
                    path.push(parser.getCurrentName());
                    break;

                case START_OBJECT:
                    depth++;
                    break;

                case END_OBJECT:
                    depth--;
                    if(!path.isEmpty()) {
                        path.pop();
                    }
                    break;

                case START_ARRAY:
                    // Check if this is a data array we want to stream
                    String fieldName = path.isEmpty() ? "" : path.peek();
                    if(isDataArray(fieldName, path)) {
                        // Stream this array
                        parseArray(parser, processor, progressCallback, countingStream);
                    } else {
                        // Skip this array
                        parser.skipChildren();
                    }
                    break;

                default:
                    // Process individual values if needed
                    break;
            }

            // Progress callback
            if(progressCallback != null && countingStream.getCount() % 10240 == 0) {
                progressCallback.onProgress(countingStream.getCount(), -1);
            }
        }
    }

    /**
     * Stream large JSON file in chunks
     */
    public void streamInChunks(InputStream inputStream,
                             String jsonPath,
                             int chunkSize,
                             Consumer<List<JsonNode>> chunkProcessor) throws IOException {

        List<JsonNode> currentChunk = new ArrayList<>(chunkSize);

        parseJsonPath(inputStream, jsonPath, node -> {
            currentChunk.add(node);

            if(currentChunk.size() >= chunkSize) {
                chunkProcessor.accept(new ArrayList<>(currentChunk));
                currentChunk.clear();
            }
        });

        // Process remaining elements
        if(!currentChunk.isEmpty()) {
            chunkProcessor.accept(currentChunk);
        }
    }

    /**
     * Parse specific path in JSON
     */
    public void parseJsonPath(InputStream inputStream,
                            String targetPath,
                            Consumer<JsonNode> processor) throws IOException {

        String[] pathParts = targetPath.split("\\.");

        try(JsonParser parser = jsonFactory.createParser(inputStream)) {
            JsonToken token = parser.nextToken();

            if(token == JsonToken.START_OBJECT) {
                navigateToPath(parser, pathParts, 0, processor);
            } else {
                throw new IOException("Expected JSON object at root");
            }
        }
    }

    /**
     * Navigate to specific path in JSON
     */
    private void navigateToPath(JsonParser parser,
                              String[] pathParts,
                              int currentIndex,
                              Consumer<JsonNode> processor) throws IOException {

        if(currentIndex >= pathParts.length) {
            // We've reached the target path
            JsonNode node = objectMapper.readTree(parser);
            processor.accept(node);
            return;
        }

        String targetField = pathParts[currentIndex];

        while(parser.nextToken() != JsonToken.END_OBJECT) {
            if(parser.currentToken() == JsonToken.FIELD_NAME) {
                String fieldName = parser.getCurrentName();

                if(fieldName.equals(targetField)) {
                    JsonToken valueToken = parser.nextToken();

                    if(currentIndex == pathParts.length - 1) {
                        // This is our target
                        if(valueToken == JsonToken.START_ARRAY) {
                            // Parse array elements
                            while(parser.nextToken() != JsonToken.END_ARRAY) {
                                if(parser.currentToken() == JsonToken.START_OBJECT) {
                                    JsonNode element = objectMapper.readTree(parser);
                                    processor.accept(element);
                                }
                            }
                        } else {
                            // Single value
                            JsonNode node = objectMapper.readTree(parser);
                            processor.accept(node);
                        }
                    } else {
                        // Navigate deeper
                        if(valueToken == JsonToken.START_OBJECT) {
                            navigateToPath(parser, pathParts, currentIndex + 1, processor);
                        } else {
                            parser.skipChildren();
                        }
                    }
                } else {
                    parser.skipChildren();
                }
            }
        }
    }

    /**
     * Convert streaming JSON to XML
     */
    public void jsonToXmlStream(InputStream jsonInput,
                              OutputStream xmlOutput,
                              String rootElement) throws IOException {

        try(JsonParser parser = jsonFactory.createParser(jsonInput);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(xmlOutput))) {

            writer.write("<?xml version = \"1.0\" encoding = \"UTF-8\"?>\n");
            writer.write("<" + rootElement + ">\n");

            JsonToken token = parser.nextToken();

            if(token == JsonToken.START_ARRAY) {
                while(parser.nextToken() != JsonToken.END_ARRAY) {
                    if(parser.currentToken() == JsonToken.START_OBJECT) {
                        JsonNode node = objectMapper.readTree(parser);
                        String xml = convertNodeToXml(node, "item", 1);
                        writer.write(xml);
                        writer.flush();
                    }
                }
            }

            writer.write("</" + rootElement + ">");
            writer.flush();
        }
    }

    private String convertNodeToXml(JsonNode node, String elementName, int indent) {
        StringBuilder xml = new StringBuilder();
        String indentStr = " ".repeat(indent);

        xml.append(indentStr).append("<").append(elementName).append(">");

        if(node.isObject()) {
            xml.append("\n");
            node.fields().forEachRemaining(entry -> {
                xml.append(convertNodeToXml(entry.getValue(), entry.getKey(), indent + 1));
            });
            xml.append(indentStr);
        } else if(node.isArray()) {
            xml.append("\n");
            node.forEach(item -> {
                xml.append(convertNodeToXml(item, "item", indent + 1));
            });
            xml.append(indentStr);
        } else {
            xml.append(node.asText());
        }

        xml.append("</").append(elementName).append(">\n");

        return xml.toString();
    }

    private boolean isDataArray(String fieldName, Stack<String> path) {
        // Identify common patterns for data arrays
        Set<String> dataArrayNames = Set.of(
            "data", "items", "results", "records", "rows",
            "entries", "elements", "list", "collection"
       );

        return dataArrayNames.contains(fieldName.toLowerCase());
    }

    /**
     * Counting input stream wrapper
     */
    private static class CountingInputStream extends FilterInputStream {
        private long count = 0;

        public CountingInputStream(InputStream in) {
            super(in);
        }

        @Override
        public int read() throws IOException {
            int result = super.read();
            if(result != -1) {
                count++;
            }
            return result;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int result = super.read(b, off, len);
            if(result != -1) {
                count += result;
            }
            return result;
        }

        public long getCount() {
            return count;
        }
    }
}
