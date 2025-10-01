package com.integrixs.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrixs.data.model.CommunicationAdapter;
import com.integrixs.data.model.IntegrationFlow;
import com.integrixs.data.model.SystemLog;
import com.integrixs.data.sql.repository.IntegrationFlowSqlRepository;
import com.integrixs.shared.enums.AdapterType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service that executes communication adapters in the backend module
 * Handles SOAP, HTTP, File, and FTP adapter executions
 */
@Service
public class BackendAdapterExecutor {

    /**
     * Interface for adapter handlers
     */
    public interface AdapterHandler {
        ExecutionResult execute(CommunicationAdapter adapter, Map<String, Object> context);
    }

    /**
     * Result of adapter execution
     */
    public static class ExecutionResult {
        private boolean success;
        private Object data;
        private String error;
        private Map<String, Object> metadata;

        public ExecutionResult(boolean success, Object data) {
            this.success = success;
            this.data = data;
            this.metadata = new HashMap<>();
        }

        public ExecutionResult(boolean success, Object data, String error) {
            this.success = success;
            this.data = data;
            this.error = error;
            this.metadata = new HashMap<>();
        }

        // Getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public Object getData() { return data; }
        public void setData(Object data) { this.data = data; }
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
        public Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    }

    private static final Logger logger = LoggerFactory.getLogger(BackendAdapterExecutor.class);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IntegrationFlowSqlRepository flowRepository;

    @Autowired
    private MessageService messageService;

    /**
     * Execute an adapter with the given message
     */
    public String executeAdapter(CommunicationAdapter adapter, String message, Map<String, Object> context) throws Exception {
        logger.info("Executing adapter: {} ( {})", adapter.getName(), adapter.getType());

        switch(adapter.getType()) {
            case SOAP:
                return executeSoapAdapter(adapter, message, context);

            case REST:
            case HTTP:
                return executeHttpAdapter(adapter, message, context);

            case FILE:
                return executeFileAdapter(adapter, message, context);

            case FTP:
            case SFTP:
                return executeFtpAdapter(adapter, message, context);

            default:
                // Log warning and return message as-is for unknown types
                logger.warn("Adapter type {} not explicitly handled, returning message as-is", adapter.getType());
                return message;
        }
    }

    private String executeSoapAdapter(CommunicationAdapter adapter, String message, Map<String, Object> context) throws Exception {
        logger.info("Executing SOAP adapter: {} (ID: {})", adapter.getName(), adapter.getId());

        // Get correlation ID and flow from context
        String correlationId = (String) context.get("correlationId");
        String flowId = (String) context.get("flowId");
        IntegrationFlow flow = null;
        if(flowId != null && correlationId != null) {
            flow = flowRepository.findById(UUID.fromString(flowId)).orElse(null);
        }

        // Log the incoming payload(what the adapter receives)
        if(correlationId != null) {
            logger.debug("Adapter request payload - correlationId: {}, adapter: {}", correlationId, adapter.getName());
        }

        // Log adapter execution start
        if(flow != null && correlationId != null) {
            try {
                messageService.logProcessingStep(correlationId, flow,
                    "SOAP adapter starting: " + adapter.getName(),
                    "Preparing to send SOAP request",
                    SystemLog.LogLevel.INFO);
            } catch(Exception e) {
                logger.warn("Failed to log processing step: {}", e.getMessage());
            }
        }

        // Log to adapter activity log
        try {
            messageService.logAdapterActivity(adapter,
                "Processing outbound SOAP request",
                "Flow: " + (flow != null ? flow.getName() : "Unknown"),
                SystemLog.LogLevel.INFO,
                correlationId);
        } catch(Exception e) {
            logger.warn("Failed to log adapter activity: {}", e.getMessage());
        }

        // Parse and log configuration
        Map<String, Object> config = parseConfiguration(adapter.getConfiguration());
        logger.info("SOAP adapter configuration keys: {}", config.keySet());
        logger.debug("SOAP adapter full configuration: {}", config);

        // Check for endpoint in multiple possible field names
        // For outbound adapters(sending TO external systems), check targetEndpointUrl
        // For inbound adapters in POLL mode, check serviceEndpointUrl
        String endpoint = (String) config.get("targetEndpointUrl");
        if(endpoint == null) {
            endpoint = (String) config.get("serviceEndpointUrl");
        }
        if(endpoint == null) {
            endpoint = (String) config.get("endpoint");
        }
        logger.info("SOAP endpoint from config: {}", endpoint);

        String soapAction = (String) config.get("soapAction");
        logger.info("SOAP action from config: {}", soapAction);

        if(endpoint == null || endpoint.isEmpty()) {
            logger.error("SOAP endpoint not configured for adapter: {} (ID: {}). Available config keys: {}",
                        adapter.getName(), adapter.getId(), config.keySet());
            if(flow != null && correlationId != null) {
                messageService.logProcessingStep(correlationId, flow,
                    "SOAP adapter error: " + adapter.getName(),
                    "No endpoint configured for SOAP adapter",
                    SystemLog.LogLevel.ERROR);
            }
            throw new IllegalArgumentException("SOAP endpoint not configured");
        }

        // Log endpoint found
        if(flow != null && correlationId != null) {
            messageService.logProcessingStep(correlationId, flow,
                "SOAP endpoint configured",
                "Endpoint: " + endpoint,
                SystemLog.LogLevel.INFO);
        }

        // Check if message is already a SOAP envelope
        logger.info("Received message for SOAP adapter: {}", message);
        String soapRequest;
        if(message.trim().startsWith("<?xml") && message.contains("Envelope")) {
            // Message is already a complete SOAP envelope
            logger.info("Message is already a SOAP envelope, using as - is");
            soapRequest = message;
        } else if(message.trim().startsWith("<") && message.contains(":Envelope")) {
            // Message is a SOAP envelope without XML declaration
            logger.info("Message is a SOAP envelope without declaration, using as - is");
            soapRequest = message;
        } else {
            // Message is just the body content, wrap it
            logger.info("Message is body content only, wrapping in SOAP envelope");
            soapRequest = wrapInSoapEnvelope(message);
        }
        logger.info("Final SOAP request being sent: {}", soapRequest);

        // Make HTTP call
        HttpHeaders headers = new HttpHeaders();

        // Set content type based on SOAP version
        String soapVersion = (String) config.getOrDefault("soapVersion", "1.1");
        logger.info("SOAP version: {}", soapVersion);

        if("1.2".equals(soapVersion)) {
            headers.setContentType(MediaType.valueOf("application/soap+xml"));
            logger.info("Using SOAP 1.2 content type: application/soap+xml");
        } else {
            // Default to SOAP 1.1
            headers.setContentType(MediaType.TEXT_XML);
            logger.info("Using SOAP 1.1 content type: text/xml");
        }

        if(soapAction != null && !soapAction.isEmpty()) {
            headers.add("SOAPAction", soapAction);
            logger.info("Added SOAPAction header: {}", soapAction);
        }

        HttpEntity<String> request = new HttpEntity<>(soapRequest, headers);
        logger.info("Making SOAP call to endpoint: {}", endpoint);
        logger.info("SOAP Request being sent: {}", soapRequest);

        // Log SOAP call details
        if(flow != null && correlationId != null) {
            messageService.logProcessingStep(correlationId, flow,
                "Sending SOAP request",
                "Endpoint: " + endpoint + "\nSOAP Action: " + (soapAction != null ? soapAction : "none"),
                SystemLog.LogLevel.INFO);
        }

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                endpoint,
                HttpMethod.POST,
                request,
                String.class
           );

            logger.info("SOAP call successful. Response status: {}", response.getStatusCode());
            logger.debug("SOAP response body: {}", response.getBody());

            // Log the response payload only if not handled by IntegrationEndpointService
            // Check if this is a SOAP endpoint flow(where IntegrationEndpointService will log the final response)
            boolean isEndpointFlow = context.get("isEndpointFlow") != null && (boolean) context.get("isEndpointFlow");
            if(correlationId != null && !isEndpointFlow) {
                messageService.logAdapterPayload(correlationId, adapter, "RESPONSE", response.getBody(), "OUTBOUND");
            }

            // Log successful response
            if(flow != null && correlationId != null) {
                messageService.logProcessingStep(correlationId, flow,
                    "SOAP response received",
                    "Status: " + response.getStatusCode() + "\nResponse size: " +
                    (response.getBody() != null ? response.getBody().length() : 0) + " bytes",
                    SystemLog.LogLevel.INFO);
            }

            // Log to adapter activity
            messageService.logAdapterActivity(adapter,
                "SOAP request completed successfully",
                "Endpoint: " + endpoint + "\nStatus: " + response.getStatusCode(),
                SystemLog.LogLevel.INFO,
                correlationId);

            // Extract SOAP body from response
            String extractedBody = extractSoapBody(response.getBody());
            logger.debug("Extracted SOAP body: {}", extractedBody);

            return extractedBody;

        } catch(Exception e) {
            logger.error("Error calling SOAP endpoint: {}. Error: {}", endpoint, e.getMessage(), e);

            // Log error
            if(flow != null && correlationId != null) {
                try {
                    messageService.logProcessingStep(correlationId, flow,
                        "SOAP call failed",
                        "Endpoint: " + endpoint + ", Error: " + e.getMessage(),
                        SystemLog.LogLevel.ERROR);
                } catch(Exception logEx) {
                    logger.warn("Failed to log error step: {}", logEx.getMessage());
                }
            }

            // Log to adapter activity
            try {
                messageService.logAdapterActivity(adapter,
                    "SOAP request failed",
                    "Endpoint: " + endpoint + ", Error: " + e.getMessage(),
                    SystemLog.LogLevel.ERROR,
                    correlationId);
            } catch(Exception logEx) {
                logger.warn("Failed to log adapter error: {}", logEx.getMessage());
            }

            throw new RuntimeException("SOAP call failed: " + e.getMessage(), e);
        }
    }

    private String executeHttpAdapter(CommunicationAdapter adapter, String message, Map<String, Object> context) throws Exception {
        Map<String, Object> config = parseConfiguration(adapter.getConfiguration());
        String endpoint = (String) config.get("endpoint");
        String method = (String) config.getOrDefault("method", "POST");

        if(endpoint == null) {
            throw new IllegalArgumentException("HTTP endpoint not configured");
        }

        // Get correlation ID from context
        String correlationId = (String) context.get("correlationId");

        // Log the incoming payload(what the adapter receives)
        if(correlationId != null) {
            logger.debug("Adapter request payload - correlationId: {}, adapter: {}", correlationId, adapter.getName());
        }

        HttpHeaders headers = new HttpHeaders();

        // Set content type based on configuration
        String contentType = (String) config.getOrDefault("contentType", "application/json");
        headers.setContentType(MediaType.parseMediaType(contentType));

        // Add any configured headers
        Map<String, String> customHeaders = (Map<String, String>) config.get("headers");
        if(customHeaders != null) {
            customHeaders.forEach(headers::add);
        }

        HttpEntity<String> request = new HttpEntity<>(message, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                endpoint,
                HttpMethod.valueOf(method.toUpperCase()),
                request,
                String.class
           );

            // Log the response payload only if not handled by IntegrationEndpointService
            // Check if this is a SOAP endpoint flow(where IntegrationEndpointService will log the final response)
            boolean isEndpointFlow = context.get("isEndpointFlow") != null && (boolean) context.get("isEndpointFlow");
            if(correlationId != null && !isEndpointFlow) {
                messageService.logAdapterPayload(correlationId, adapter, "RESPONSE", response.getBody(), "OUTBOUND");
            }

            return response.getBody();

        } catch(Exception e) {
            logger.error("Error calling HTTP endpoint: {}", endpoint, e);
            throw new RuntimeException("HTTP call failed: " + e.getMessage(), e);
        }
    }

    private String executeFileAdapter(CommunicationAdapter adapter, String message, Map<String, Object> context) throws Exception {
        Map<String, Object> config = parseConfiguration(adapter.getConfiguration());
        String directory = (String) config.get("directory");
        String filePattern = (String) config.getOrDefault("fileNamePattern", "output - {timestamp}.txt");

        if(directory == null) {
            throw new IllegalArgumentException("File directory not configured");
        }

        // Create directory if it doesn't exist
        Path dirPath = Paths.get(directory);
        Files.createDirectories(dirPath);

        // Generate filename
        String filename = filePattern
            .replace(" {timestamp}", String.valueOf(System.currentTimeMillis()))
            .replace(" {uuid}", UUID.randomUUID().toString())
            .replace(" {flowId}", (String) context.get("flowId"));

        // Write file
        Path filePath = dirPath.resolve(filename);
        Files.write(filePath, message.getBytes());

        logger.info("File written to: {}", filePath);

        // Return success response
        return " {\"status\":\"success\",\"file\":\"" + filePath.toString() + "\"}";
    }

    private String executeFtpAdapter(CommunicationAdapter adapter, String message, Map<String, Object> context) throws Exception {
        logger.info("Executing FTP/SFTP adapter: {} [ {}]", adapter.getName(), adapter.getType());

        // Create adapter factory
        com.integrixs.adapters.factory.AdapterFactory factory = new com.integrixs.adapters.factory.DefaultAdapterFactory();

        // Parse configuration JSON
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> configMap;
        try {
            configMap = mapper.readValue(adapter.getConfiguration(), Map.class);
        } catch(Exception e) {
            logger.error("Failed to parse adapter configuration JSON", e);
            throw new RuntimeException("Invalid adapter configuration JSON", e);
        }

        // Prepare configuration based on adapter type and mode
        if(adapter.getMode() == com.integrixs.adapters.domain.model.AdapterConfiguration.AdapterModeEnum.OUTBOUND) {
            // Receiver mode - send data TO FTP server(upload)
            com.integrixs.adapters.config.FtpOutboundAdapterConfig config = new com.integrixs.adapters.config.FtpOutboundAdapterConfig();

            // Set connection details from adapter config
            config.setServerAddress((String) configMap.get("serverAddress"));
            config.setPort((String) configMap.getOrDefault("port", "21"));
            config.setUserName((String) configMap.get("userName"));
            config.setPassword((String) configMap.get("password"));
            config.setTargetDirectory((String) configMap.getOrDefault("targetDirectory", "/"));
            config.setConnectionSecurity((String) configMap.getOrDefault("connectionSecurity", "plain - ftp"));
            config.setEnablePassiveMode(Boolean.parseBoolean(configMap.getOrDefault("enablePassiveMode", "true").toString()));

            // File settings
            config.setTargetFileName((String) configMap.get("targetFileName"));
            config.setOverwriteExistingFile(Boolean.parseBoolean(configMap.getOrDefault("overwriteExistingFile", "false").toString()));
            config.setFileConstructionMode((String) configMap.getOrDefault("fileConstructionMode", "create"));
            config.setFileEncoding((String) configMap.getOrDefault("fileEncoding", "UTF-8"));

            // Create outbound adapter and execute
            com.integrixs.adapters.domain.port.OutboundAdapterPort receiverAdapter = factory.createOutboundAdapter(
                adapter.getType() == com.integrixs.shared.enums.AdapterType.FTP ?
                    com.integrixs.adapters.domain.model.AdapterConfiguration.AdapterTypeEnum.FTP :
                    com.integrixs.adapters.domain.model.AdapterConfiguration.AdapterTypeEnum.SFTP,
                config
           );

            // Create AdapterConfiguration for initialization
            com.integrixs.adapters.domain.model.AdapterConfiguration adapterConfig =
                com.integrixs.adapters.domain.model.AdapterConfiguration.builder()
                    .adapterType(adapter.getType() == com.integrixs.shared.enums.AdapterType.FTP ?
                        com.integrixs.adapters.domain.model.AdapterConfiguration.AdapterTypeEnum.FTP :
                        com.integrixs.adapters.domain.model.AdapterConfiguration.AdapterTypeEnum.SFTP)
                    .adapterMode(com.integrixs.adapters.domain.model.AdapterConfiguration.AdapterModeEnum.OUTBOUND)
                    .connectionProperties(configMap)
                    .build();
            receiverAdapter.initialize(adapterConfig);
            try {
                // Create send request for OutboundAdapterPort
                com.integrixs.adapters.domain.model.SendRequest sendRequest =
                    com.integrixs.adapters.domain.model.SendRequest.builder()
                        .payload(message)
                        .build();
                com.integrixs.adapters.domain.model.AdapterOperationResult result = receiverAdapter.send(sendRequest);
                if(result.isSuccess()) {
                    logger.info("FTP upload successful: {}", result.getMessage());
                    return " {\"status\":\"success\",\"message\":\"File uploaded successfully\"}";
                } else {
                    logger.error("FTP upload failed: {}", result.getMessage());
                    throw new RuntimeException("FTP upload failed: " + result.getMessage());
                }
            } finally {
                receiverAdapter.shutdown();
            }

        } else {
            // Sender mode - receive data FROM FTP server(download/poll)
            com.integrixs.adapters.config.FtpInboundAdapterConfig config = new com.integrixs.adapters.config.FtpInboundAdapterConfig();

            // Set connection details from adapter config
            config.setServerAddress((String) configMap.get("serverAddress"));
            config.setPort((String) configMap.getOrDefault("port", "21"));
            config.setUserName((String) configMap.get("userName"));
            config.setPassword((String) configMap.get("password"));
            config.setSourceDirectory((String) configMap.getOrDefault("sourceDirectory", "/"));
            config.setConnectionSecurity((String) configMap.getOrDefault("connectionSecurity", "plain - ftp"));
            config.setEnablePassiveMode(Boolean.parseBoolean(configMap.getOrDefault("enablePassiveMode", "true").toString()));

            // File pattern settings
            String fileNamePattern = (String) configMap.getOrDefault("fileNamePattern", "*");
            config.setFileName(fileNamePattern); // FtpInboundAdapterConfig uses fileName not fileNamePattern

            // Note: FtpInboundAdapterConfig handles exclusion patterns internally
            // The adapter implementation checks for exclusionFileNamePattern

            config.setFileEncoding((String) configMap.getOrDefault("fileEncoding", "UTF-8"));

            // Processing settings
            config.setProcessingMode((String) configMap.getOrDefault("processingMode", "test"));
            String postProcessing = (String) configMap.getOrDefault("postProcessingCommand", "none");
            // FtpInboundAdapterConfig doesn't have setPostProcessingCommand, it's handled internally

            // Create inbound adapter and execute
            com.integrixs.adapters.domain.port.InboundAdapterPort senderAdapter = factory.createInboundAdapter(
                adapter.getType() == com.integrixs.shared.enums.AdapterType.FTP ?
                    com.integrixs.adapters.domain.model.AdapterConfiguration.AdapterTypeEnum.FTP :
                    com.integrixs.adapters.domain.model.AdapterConfiguration.AdapterTypeEnum.SFTP,
                config
           );

            // Create AdapterConfiguration for initialization
            com.integrixs.adapters.domain.model.AdapterConfiguration senderAdapterConfig =
                com.integrixs.adapters.domain.model.AdapterConfiguration.builder()
                    .adapterType(adapter.getType() == com.integrixs.shared.enums.AdapterType.FTP ?
                        com.integrixs.adapters.domain.model.AdapterConfiguration.AdapterTypeEnum.FTP :
                        com.integrixs.adapters.domain.model.AdapterConfiguration.AdapterTypeEnum.SFTP)
                    .adapterMode(com.integrixs.adapters.domain.model.AdapterConfiguration.AdapterModeEnum.INBOUND)
                    .connectionProperties(configMap)
                    .build();
            senderAdapter.initialize(senderAdapterConfig);
            try {
                // Create fetch request for InboundAdapterPort
                com.integrixs.adapters.domain.model.FetchRequest fetchRequest =
                    com.integrixs.adapters.domain.model.FetchRequest.builder()
                        .build();
                com.integrixs.adapters.domain.model.AdapterOperationResult result = senderAdapter.fetch(fetchRequest);
                if(result.isSuccess()) {
                    // Process the files received
                    Object data = result.getData();
                    if(data instanceof List) {
                        List<?> files = (List<?>) data;
                        logger.info("FTP poll successful, retrieved {} files", files.size());

                        // Convert to JSON response
                        return mapper.writeValueAsString(Map.of(
                            "status", "success",
                            "filesCount", files.size(),
                            "files", files
                       ));
                    } else {
                        return " {\"status\":\"success\",\"message\":\"Operation successful\"}";
                    }
                } else {
                    logger.error("FTP poll failed: {}", result.getMessage());
                    throw new RuntimeException("FTP poll failed: " + result.getMessage());
                }
            } finally {
                senderAdapter.shutdown();
            }
        }
    }

    private String extractSoapBody(String soapResponse) throws Exception {
        // Parse SOAP response and extract body content
        javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
        org.w3c.dom.Document doc = builder.parse(new java.io.ByteArrayInputStream(soapResponse.getBytes()));

        // Find the Body element
        org.w3c.dom.NodeList bodyList = doc.getElementsByTagNameNS("http://schemas.xmlsoap.org/soap/envelope/", "Body");
        if(bodyList.getLength() == 0) {
            // Try SOAP 1.2
            bodyList = doc.getElementsByTagNameNS("http://www.w3.org/2003/05/soap - envelope", "Body");
        }

        if(bodyList.getLength() > 0) {
            // Get first child of Body
            org.w3c.dom.Node body = bodyList.item(0);
            if(body.hasChildNodes()) {
                org.w3c.dom.Node responseNode = body.getFirstChild();
                while(responseNode != null && responseNode.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
                    responseNode = responseNode.getNextSibling();
                }

                if(responseNode != null) {
                    // Convert to string
                    javax.xml.transform.TransformerFactory tf = javax.xml.transform.TransformerFactory.newInstance();
                    javax.xml.transform.Transformer transformer = tf.newTransformer();
                    // Don't output XML declaration
                    transformer.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes");
                    java.io.StringWriter writer = new java.io.StringWriter();
                    transformer.transform(new javax.xml.transform.dom.DOMSource(responseNode),
                                        new javax.xml.transform.stream.StreamResult(writer));
                    return writer.toString();
                }
            }
        }

        // If no body found, return the whole response
        return soapResponse;
    }

    private String wrapInSoapEnvelope(String body) {
        return "<?xml version = \"1.0\" encoding = \"UTF-8\"?>\n" +
               "<soap:Envelope xmlns:soap = \"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
               " <soap:Body>\n" +
               body +
               " </soap:Body>\n" +
               "</soap:Envelope>";
    }

    private Map<String, Object> parseConfiguration(String configJson) {
        if(configJson == null || configJson.isEmpty()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(configJson, Map.class);
        } catch(Exception e) {
            logger.error("Error parsing adapter configuration: {}", e.getMessage());
            return new HashMap<>();
        }
    }
}
