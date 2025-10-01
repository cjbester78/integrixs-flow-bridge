package com.integrixs.backend.streaming;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.xml.namespace.QName;
import javax.xml.stream.*;
import javax.xml.stream.events.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;

/**
 * Streaming XML parser using StAX
 */
@Component
public class XmlStreamingParser implements StreamingParser<Map<String, Object>> {

    private static final Logger logger = LoggerFactory.getLogger(XmlStreamingParser.class);

    private static final Set<String> SUPPORTED_CONTENT_TYPES = Set.of(
        "application/xml",
        "text/xml",
        "application/soap+xml",
        "application/vnd.xml"
   );

    private XMLInputFactory xmlInputFactory;
    private XMLOutputFactory xmlOutputFactory;

    public XmlStreamingParser() {
        this.xmlInputFactory = XMLInputFactory.newInstance();
        this.xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        this.xmlInputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
        this.xmlOutputFactory = XMLOutputFactory.newInstance();
    }

    @Override
    public void parse(InputStream inputStream,
                     Consumer<Map<String, Object>> elementProcessor,
                     ProgressCallback progressCallback) throws IOException {

        // Wrap input stream to track progress
        CountingInputStream countingStream = new CountingInputStream(inputStream);

        try {
            XMLEventReader reader = xmlInputFactory.createXMLEventReader(countingStream, StandardCharsets.UTF_8.name());

            Stack<String> elementPath = new Stack<>();
            Map<String, Object> currentElement = null;
            StringBuilder textContent = new StringBuilder();

            while(reader.hasNext()) {
                XMLEvent event = reader.nextEvent();

                switch(event.getEventType()) {
                    case XMLStreamConstants.START_ELEMENT:
                        StartElement startElement = event.asStartElement();
                        String elementName = startElement.getName().getLocalPart();
                        elementPath.push(elementName);

                        // Start new element
                        Map<String, Object> newElement = new HashMap<>();
                        newElement.put("_name", elementName);
                        newElement.put("_path", String.join("/", elementPath));

                        // Add attributes
                        Iterator<Attribute> attributes = startElement.getAttributes();
                        if(attributes.hasNext()) {
                            Map<String, String> attrs = new HashMap<>();
                            while(attributes.hasNext()) {
                                Attribute attr = attributes.next();
                                attrs.put(attr.getName().getLocalPart(), attr.getValue());
                            }
                            newElement.put("_attributes", attrs);
                        }

                        // Add namespaces
                        Iterator<Namespace> namespaces = startElement.getNamespaces();
                        if(namespaces.hasNext()) {
                            Map<String, String> ns = new HashMap<>();
                            while(namespaces.hasNext()) {
                                Namespace namespace = namespaces.next();
                                ns.put(namespace.getPrefix(), namespace.getNamespaceURI());
                            }
                            newElement.put("_namespaces", ns);
                        }

                        if(currentElement != null) {
                            // Add as child
                            addChild(currentElement, elementName, newElement);
                        }

                        currentElement = newElement;
                        textContent.setLength(0);
                        break;

                    case XMLStreamConstants.CHARACTERS:
                        Characters characters = event.asCharacters();
                        if(!characters.isWhiteSpace()) {
                            textContent.append(characters.getData());
                        }
                        break;

                    case XMLStreamConstants.END_ELEMENT:
                        EndElement endElement = event.asEndElement();
                        elementPath.pop();

                        // Add text content if any
                        if(textContent.length() > 0) {
                            currentElement.put("_text", textContent.toString().trim());
                        }

                        // Process complete element
                        if(shouldProcessElement(currentElement)) {
                            elementProcessor.accept(currentElement);
                        }

                        // Reset for next element
                        currentElement = null;
                        textContent.setLength(0);
                        break;
                }

                // Report progress
                if(progressCallback != null && countingStream.getCount() % 10240 == 0) { // Every 10KB
                    progressCallback.onProgress(countingStream.getCount(), -1);
                }
            }

            reader.close();

        } catch(XMLStreamException e) {
            throw new IOException("Error parsing XML stream", e);
        }
    }

    @Override
    public Class<Map<String, Object>> getElementType() {
        return(Class<Map<String, Object>>) (Class<?>) Map.class;
    }

    @Override
    public boolean canHandle(String contentType) {
        return contentType != null && SUPPORTED_CONTENT_TYPES.stream()
            .anyMatch(type -> contentType.toLowerCase().contains(type));
    }

    /**
     * Parse XML and extract specific elements by path
     */
    public void parseElements(InputStream inputStream,
                            String targetPath,
                            Consumer<String> xmlProcessor,
                            ProgressCallback progressCallback) throws IOException {

        CountingInputStream countingStream = new CountingInputStream(inputStream);

        try {
            XMLEventReader reader = xmlInputFactory.createXMLEventReader(countingStream, StandardCharsets.UTF_8.name());

            Stack<String> elementPath = new Stack<>();
            boolean capturing = false;
            StringWriter captureWriter = null;
            XMLEventWriter captureEventWriter = null;
            int depth = 0;

            while(reader.hasNext()) {
                XMLEvent event = reader.nextEvent();

                if(event.isStartElement()) {
                    StartElement startElement = event.asStartElement();
                    String elementName = startElement.getName().getLocalPart();
                    elementPath.push(elementName);

                    String currentPath = String.join("/", elementPath);

                    if(!capturing && currentPath.endsWith(targetPath)) {
                        // Start capturing
                        capturing = true;
                        captureWriter = new StringWriter();
                        captureEventWriter = xmlOutputFactory.createXMLEventWriter(captureWriter);
                        depth = 0;
                    }

                    if(capturing) {
                        captureEventWriter.add(event);
                        depth++;
                    }

                } else if(event.isEndElement()) {
                    if(capturing) {
                        captureEventWriter.add(event);
                        depth--;

                        if(depth == 0) {
                            // Complete element captured
                            captureEventWriter.close();
                            xmlProcessor.accept(captureWriter.toString());

                            // Reset
                            capturing = false;
                            captureWriter = null;
                            captureEventWriter = null;
                        }
                    }

                    elementPath.pop();

                } else if(capturing) {
                    // Capture all other events
                    captureEventWriter.add(event);
                }

                // Progress callback
                if(progressCallback != null && countingStream.getCount() % 10240 == 0) {
                    progressCallback.onProgress(countingStream.getCount(), -1);
                }
            }

            reader.close();

        } catch(XMLStreamException e) {
            throw new IOException("Error parsing XML stream", e);
        }
    }

    /**
     * Stream large XML file and split into chunks
     */
    public void streamInChunks(InputStream inputStream,
                             int chunkSize,
                             Consumer<byte[]> chunkProcessor) throws IOException {

        byte[] buffer = new byte[chunkSize];
        int bytesRead;

        while((bytesRead = inputStream.read(buffer)) != -1) {
            if(bytesRead < chunkSize) {
                // Last chunk
                byte[] lastChunk = Arrays.copyOf(buffer, bytesRead);
                chunkProcessor.accept(lastChunk);
            } else {
                chunkProcessor.accept(buffer);
            }
        }
    }

    private void addChild(Map<String, Object> parent, String childName, Map<String, Object> child) {
        Object existing = parent.get(childName);

        if(existing == null) {
            parent.put(childName, child);
        } else if(existing instanceof List) {
            ((List<Map<String, Object>>) existing).add(child);
        } else {
            List<Map<String, Object>> list = new ArrayList<>();
            list.add((Map<String, Object>) existing);
            list.add(child);
            parent.put(childName, list);
        }
    }

    private boolean shouldProcessElement(Map<String, Object> element) {
        // Process elements that have substantial content
        return element != null &&
               (element.size() > 2 || // Has more than just name and path
                element.containsKey("_text") ||
                element.containsKey("_attributes"));
    }

    /**
     * Counting input stream to track progress
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
