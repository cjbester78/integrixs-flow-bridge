package com.integrixs.backend.controller;

import com.integrixs.backend.dto.StreamingUploadResult;
import com.integrixs.backend.service.MessageStructureService;
import com.integrixs.backend.streaming.JsonStreamingParser;
import com.integrixs.backend.streaming.StreamingParser;
import com.integrixs.backend.streaming.XmlStreamingParser;
import com.integrixs.backend.websocket.StreamingProgressWebSocketHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Controller for streaming large file uploads and processing
 */
@RestController
@RequestMapping("/api/streaming")
@Tag(name = "Streaming Upload", description = "Streaming API for large file processing")
public class StreamingUploadController {

    private static final Logger logger = LoggerFactory.getLogger(StreamingUploadController.class);

    private static final long MAX_FILE_SIZE = 500 * 1024 * 1024; // 500MB
    private static final int CHUNK_SIZE = 1000; // Process 1000 elements at a time

    @Autowired
    private XmlStreamingParser xmlStreamingParser;

    @Autowired
    private JsonStreamingParser jsonStreamingParser;

    @Autowired
    private MessageStructureService messageStructureService;

    @Autowired
    private StreamingProgressWebSocketHandler progressWebSocketHandler;

    @Autowired
    private ObjectMapper objectMapper;

    // Track active streaming sessions
    private final Map<String, StreamingSession> activeSessions = new ConcurrentHashMap<>();

    /**
     * Upload and parse large XML structure
     */
    @PostMapping("/upload/xml")
    @Operation(summary = "Upload large XML file",
               description = "Streams and parses large XML files without loading entire file in memory")
    @ApiResponses( {
        @ApiResponse(responseCode = "200", description = "File processed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid file or format"),
        @ApiResponse(responseCode = "413", description = "File too large"),
        @ApiResponse(responseCode = "500", description = "Processing error")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public ResponseEntity<?> uploadXmlStream(
            @Parameter(description = "XML file to upload")
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Session ID for progress tracking")
            @RequestParam(required = false) String sessionId,
            @Parameter(description = "Target element path to extract(e.g., 'root/items/item')")
            @RequestParam(required = false) String targetPath) {

        try {
            // Validate file
            if(file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "File is empty"));
            }

            if(file.getSize() > MAX_FILE_SIZE) {
                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                    .body(Map.of("error", "File too large",
                                "maxSize", MAX_FILE_SIZE,
                                "actualSize", file.getSize()));
            }

            // Create session
            if(sessionId == null) {
                sessionId = UUID.randomUUID().toString();
            }

            StreamingSession session = new StreamingSession(sessionId, file.getOriginalFilename(), file.getSize());
            activeSessions.put(sessionId, session);

            // Process file in streaming fashion
            List<Map<String, Object>> elements = new ArrayList<>();
            AtomicInteger elementCount = new AtomicInteger(0);
            AtomicLong bytesProcessed = new AtomicLong(0);

            xmlStreamingParser.parse(
                file.getInputStream(),
                element -> {
                    elements.add(element);
                    elementCount.incrementAndGet();

                    // Process in chunks
                    if(elements.size() >= CHUNK_SIZE) {
                        processXmlChunk(elements, session);
                        elements.clear();
                    }
                },
                (bytesRead, totalBytes) -> {
                    bytesProcessed.set(bytesRead);
                    updateProgress(session, bytesRead, file.getSize());
                }
           );

            // Process remaining elements
            if(!elements.isEmpty()) {
                processXmlChunk(elements, session);
            }

            // Complete session
            session.complete();
            updateProgress(session, file.getSize(), file.getSize());

            // Create result
            StreamingUploadResult result = new StreamingUploadResult();
            result.setSessionId(sessionId);
            result.setFileName(file.getOriginalFilename());
            result.setFileSize(file.getSize());
            result.setElementsProcessed(elementCount.get());
            result.setProcessingTimeMs(session.getDuration());
            result.setSuccess(true);
            result.setStructure(session.getExtractedStructure());

            // Clean up
            activeSessions.remove(sessionId);

            return ResponseEntity.ok(result);

        } catch(Exception e) {
            logger.error("Error processing XML stream", e);

            if(sessionId != null) {
                StreamingSession session = activeSessions.get(sessionId);
                if(session != null) {
                    session.fail(e.getMessage());
                    updateProgress(session, 0, 0);
                }
                activeSessions.remove(sessionId);
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Processing failed", "message", e.getMessage()));
        }
    }

    /**
     * Upload and parse large JSON structure
     */
    @PostMapping("/upload/json")
    @Operation(summary = "Upload large JSON file",
               description = "Streams and parses large JSON files without loading entire file in memory")
    @ApiResponses( {
        @ApiResponse(responseCode = "200", description = "File processed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid file or format"),
        @ApiResponse(responseCode = "413", description = "File too large"),
        @ApiResponse(responseCode = "500", description = "Processing error")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public ResponseEntity<?> uploadJsonStream(
            @Parameter(description = "JSON file to upload")
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Session ID for progress tracking")
            @RequestParam(required = false) String sessionId,
            @Parameter(description = "JSON path to data array(e.g., 'data.items')")
            @RequestParam(required = false) String jsonPath) {

        try {
            // Validate file
            if(file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "File is empty"));
            }

            if(file.getSize() > MAX_FILE_SIZE) {
                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                    .body(Map.of("error", "File too large",
                                "maxSize", MAX_FILE_SIZE,
                                "actualSize", file.getSize()));
            }

            // Create session
            if(sessionId == null) {
                sessionId = UUID.randomUUID().toString();
            }

            StreamingSession session = new StreamingSession(sessionId, file.getOriginalFilename(), file.getSize());
            activeSessions.put(sessionId, session);

            // Process file in streaming fashion
            List<JsonNode> elements = new ArrayList<>();
            AtomicInteger elementCount = new AtomicInteger(0);
            AtomicLong bytesProcessed = new AtomicLong(0);

            if(jsonPath != null && !jsonPath.isEmpty()) {
                // Parse specific path
                jsonStreamingParser.parseJsonPath(
                    file.getInputStream(),
                    jsonPath,
                    element -> {
                        elements.add(element);
                        elementCount.incrementAndGet();

                        if(elements.size() >= CHUNK_SIZE) {
                            processJsonChunk(elements, session);
                            elements.clear();
                        }
                    }
               );
            } else {
                // Parse entire file
                jsonStreamingParser.parse(
                    file.getInputStream(),
                    element -> {
                        elements.add(element);
                        elementCount.incrementAndGet();

                        if(elements.size() >= CHUNK_SIZE) {
                            processJsonChunk(elements, session);
                            elements.clear();
                        }
                    },
                    (bytesRead, totalBytes) -> {
                        bytesProcessed.set(bytesRead);
                        updateProgress(session, bytesRead, file.getSize());
                    }
               );
            }

            // Process remaining elements
            if(!elements.isEmpty()) {
                processJsonChunk(elements, session);
            }

            // Complete session
            session.complete();
            updateProgress(session, file.getSize(), file.getSize());

            // Create result
            StreamingUploadResult result = new StreamingUploadResult();
            result.setSessionId(sessionId);
            result.setFileName(file.getOriginalFilename());
            result.setFileSize(file.getSize());
            result.setElementsProcessed(elementCount.get());
            result.setProcessingTimeMs(session.getDuration());
            result.setSuccess(true);
            result.setStructure(session.getExtractedStructure());

            // Clean up
            activeSessions.remove(sessionId);

            return ResponseEntity.ok(result);

        } catch(Exception e) {
            logger.error("Error processing JSON stream", e);

            if(sessionId != null) {
                StreamingSession session = activeSessions.get(sessionId);
                if(session != null) {
                    session.fail(e.getMessage());
                    updateProgress(session, 0, 0);
                }
                activeSessions.remove(sessionId);
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Processing failed", "message", e.getMessage()));
        }
    }

    /**
     * Stream conversion from JSON to XML
     */
    @PostMapping("/convert/json - to - xml")
    @Operation(summary = "Convert JSON to XML",
               description = "Streams conversion of large JSON files to XML format")
    @ApiResponses( {
        @ApiResponse(responseCode = "200", description = "Conversion successful"),
        @ApiResponse(responseCode = "400", description = "Invalid file"),
        @ApiResponse(responseCode = "500", description = "Conversion error")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public StreamingResponseBody convertJsonToXml(
            @Parameter(description = "JSON file to convert")
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Root element name for XML")
            @RequestParam(defaultValue = "root") String rootElement,
            HttpServletResponse response) {

        response.setContentType(MediaType.APPLICATION_XML_VALUE);
        response.setHeader("Content - Disposition",
            "attachment; filename = \"" + getXmlFileName(file.getOriginalFilename()) + "\"");

        return outputStream -> {
            try {
                jsonStreamingParser.jsonToXmlStream(
                    file.getInputStream(),
                    outputStream,
                    rootElement
               );
                outputStream.flush();
            } catch(Exception e) {
                logger.error("Error converting JSON to XML", e);
                throw new IOException("Conversion failed: " + e.getMessage());
            }
        };
    }

    /**
     * Get streaming session status
     */
    @GetMapping("/session/ {sessionId}")
    @Operation(summary = "Get session status",
               description = "Gets the current status of a streaming session")
    @ApiResponses( {
        @ApiResponse(responseCode = "200", description = "Session found"),
        @ApiResponse(responseCode = "404", description = "Session not found")
    })
    public ResponseEntity<?> getSessionStatus(@PathVariable String sessionId) {
        StreamingSession session = activeSessions.get(sessionId);
        if(session != null) {
            return ResponseEntity.ok(session.toStatusMap());
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Cancel streaming session
     */
    @DeleteMapping("/session/ {sessionId}")
    @Operation(summary = "Cancel session",
               description = "Cancels an active streaming session")
    @ApiResponses( {
        @ApiResponse(responseCode = "200", description = "Session cancelled"),
        @ApiResponse(responseCode = "404", description = "Session not found")
    })
    public ResponseEntity<?> cancelSession(@PathVariable String sessionId) {
        StreamingSession session = activeSessions.remove(sessionId);
        if(session != null) {
            session.cancel();
            return ResponseEntity.ok(Map.of("message", "Session cancelled"));
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Process XML chunk
     */
    private void processXmlChunk(List<Map<String, Object>> elements, StreamingSession session) {
        try {
            // Extract structure from first few elements
            if(session.getExtractedStructure() == null && !elements.isEmpty()) {
                Map<String, Object> structure = messageStructureService.inferXmlStructure(elements);
                session.setExtractedStructure(structure);
            }

            // Update element count
            session.incrementProcessedElements(elements.size());

            logger.debug("Processed {} XML elements in session {}",
                elements.size(), session.getSessionId());

        } catch(Exception e) {
            logger.error("Error processing XML chunk", e);
            session.addError(e.getMessage());
        }
    }

    /**
     * Process JSON chunk
     */
    private void processJsonChunk(List<JsonNode> elements, StreamingSession session) {
        try {
            // Extract structure from first few elements
            if(session.getExtractedStructure() == null && !elements.isEmpty()) {
                Map<String, Object> structure = messageStructureService.inferJsonStructure(elements);
                session.setExtractedStructure(structure);
            }

            // Update element count
            session.incrementProcessedElements(elements.size());

            logger.debug("Processed {} JSON elements in session {}",
                elements.size(), session.getSessionId());

        } catch(Exception e) {
            logger.error("Error processing JSON chunk", e);
            session.addError(e.getMessage());
        }
    }

    /**
     * Update progress via WebSocket
     */
    private void updateProgress(StreamingSession session, long bytesRead, long totalBytes) {
        Map<String, Object> progress = new HashMap<>();
        progress.put("sessionId", session.getSessionId());
        progress.put("fileName", session.getFileName());
        progress.put("bytesRead", bytesRead);
        progress.put("totalBytes", totalBytes);
        progress.put("percentComplete", totalBytes > 0 ? (bytesRead * 100.0 / totalBytes) : 0);
        progress.put("elementsProcessed", session.getElementsProcessed());
        progress.put("status", session.getStatus());

        try {
            progressWebSocketHandler.sendProgressUpdate(
                session.getSessionId(),
                objectMapper.writeValueAsString(progress)
           );
        } catch(Exception e) {
            logger.warn("Failed to send progress update", e);
        }
    }

    private String getXmlFileName(String originalName) {
        if(originalName == null) {
            return "converted.xml";
        }
        int lastDot = originalName.lastIndexOf('.');
        if(lastDot > 0) {
            return originalName.substring(0, lastDot) + ".xml";
        }
        return originalName + ".xml";
    }

    /**
     * Internal class to track streaming sessions
     */
    private static class StreamingSession {
        private final String sessionId;
        private final String fileName;
        private final long fileSize;
        private final long startTime;
        private volatile String status = "processing";
        private volatile int elementsProcessed = 0;
        private volatile Map<String, Object> extractedStructure;
        private final List<String> errors = new ArrayList<>();

        public StreamingSession(String sessionId, String fileName, long fileSize) {
            this.sessionId = sessionId;
            this.fileName = fileName;
            this.fileSize = fileSize;
            this.startTime = System.currentTimeMillis();
        }

        public void incrementProcessedElements(int count) {
            this.elementsProcessed += count;
        }

        public void complete() {
            this.status = "completed";
        }

        public void fail(String error) {
            this.status = "failed";
            this.errors.add(error);
        }

        public void cancel() {
            this.status = "cancelled";
        }

        public void addError(String error) {
            this.errors.add(error);
        }

        public long getDuration() {
            return System.currentTimeMillis() - startTime;
        }

        public Map<String, Object> toStatusMap() {
            Map<String, Object> status = new HashMap<>();
            status.put("sessionId", sessionId);
            status.put("fileName", fileName);
            status.put("fileSize", fileSize);
            status.put("status", this.status);
            status.put("elementsProcessed", elementsProcessed);
            status.put("durationMs", getDuration());
            status.put("errors", errors);
            return status;
        }

        // Getters
        public String getSessionId() { return sessionId; }
        public String getFileName() { return fileName; }
        public long getFileSize() { return fileSize; }
        public String getStatus() { return status; }
        public int getElementsProcessed() { return elementsProcessed; }
        public Map<String, Object> getExtractedStructure() { return extractedStructure; }
        public void setExtractedStructure(Map<String, Object> structure) { this.extractedStructure = structure; }
    }
}
