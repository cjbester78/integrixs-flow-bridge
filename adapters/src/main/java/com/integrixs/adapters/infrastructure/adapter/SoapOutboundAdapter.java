package com.integrixs.adapters.infrastructure.adapter;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.shared.exceptions.AdapterException;

import com.integrixs.adapters.domain.port.OutboundAdapterPort;
import com.integrixs.adapters.config.SoapOutboundAdapterConfig;
import java.util.concurrent.CompletableFuture;
import javax.xml.namespace.QName;
import jakarta.xml.soap.*;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import jakarta.xml.ws.Dispatch;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.soap.SOAPBinding;
import java.io.StringReader;
import java.net.URL;
import java.util.*;
import com.integrixs.adapters.domain.model.*;
/**
 * SOAP Receiver Adapter implementation for SOAP service calls(OUTBOUND).
 * Follows middleware convention: Outbound = sends data TO external systems.
 * Makes SOAP requests to external SOAP services.
 */
public class SoapOutboundAdapter extends AbstractAdapter implements OutboundAdapterPort {
    private static final Logger log = LoggerFactory.getLogger(SoapOutboundAdapter.class);


    private final SoapOutboundAdapterConfig config;
    private SOAPConnectionFactory soapConnectionFactory;
    private MessageFactory messageFactory;
    public SoapOutboundAdapter(SoapOutboundAdapterConfig config) {
        super();
        this.config = config;
    }
    @Override
    protected AdapterOperationResult performInitialization() {
        log.info("Initializing SOAP outbound adapter(outbound) with endpoint: {}", config.getEffectiveEndpoint());

        try {
            validateConfiguration();
            // Initialize SOAP factories
            soapConnectionFactory = SOAPConnectionFactory.newInstance();
            // Create message factory based on SOAP version
            if("1.2".equals(config.getSoapVersion())) {
                messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
            } else {
                messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
            }
        } catch(Exception e) {
            log.error("Error during initialization", e);
            return AdapterOperationResult.failure("Initialization error: " + e.getMessage());
        }

        log.info("SOAP outbound adapter initialized successfully");
        return AdapterOperationResult.success("SOAP outbound adapter initialized");
    }

    @Override
    protected AdapterOperationResult performShutdown() {
        log.info("Destroying SOAP outbound adapter");
        // Cleanup resources if needed
        return AdapterOperationResult.success("SOAP outbound adapter destroyed");
    }

    @Override
    protected AdapterOperationResult performConnectionTest() {
        List<AdapterOperationResult> testResults = new ArrayList<>();

        // Test 1: Endpoint connectivity
        try {
            URL url = new URL(config.getEffectiveEndpoint());
            url.openConnection().connect();
            testResults.add(AdapterOperationResult.success(
                    "Endpoint Connectivity", "Successfully connected to SOAP endpoint"));
        } catch(Exception e) {
            testResults.add(AdapterOperationResult.failure(
                    "Endpoint Connectivity", "Failed to connect to SOAP endpoint: " + e.getMessage()));
        }

        // Test 2: WSDL validation
        if(config.getWsdlUrl() != null && !config.getWsdlUrl().isEmpty()) {
            try {
                URL wsdlUrl = new URL(config.getWsdlUrl());
                // Try to create a service from WSDL
                QName serviceName = new QName(config.getTargetNamespace(), config.getServiceName());
                Service service = Service.create(wsdlUrl, serviceName);
                testResults.add(AdapterOperationResult.success(
                        "WSDL Validation", "Successfully validated WSDL and service"));
            } catch(Exception e) {
                testResults.add(AdapterOperationResult.failure(
                        "WSDL Validation", "Failed to validate WSDL: " + e.getMessage()));
            }
        }

        // Test 3: Authentication test(if configured)
        if(config.getUsername() != null && !config.getUsername().isEmpty()) {
            try {
                // Create a test SOAP message with authentication
                SOAPMessage testMessage = createTestMessage();
                addSecurityHeader(testMessage);
                testResults.add(AdapterOperationResult.success(
                        "Authentication", "Authentication configured for user: " + config.getUsername()));
            } catch(Exception e) {
                testResults.add(AdapterOperationResult.failure(
                        "Authentication", "Failed to configure authentication: " + e.getMessage()));
            }
        }

        return AdapterOperationResult.success(testResults);
    }

    // OutboundAdapterPort implementation
    @Override
    public AdapterOperationResult send(SendRequest request) {
        try {
            return invokeSoapService(request.getPayload());
        } catch(Exception e) {
            return AdapterOperationResult.failure("SOAP invocation failed: " + e.getMessage());
        }
    }

    protected AdapterOperationResult performSend(Object payload) throws Exception {
        // For SOAP Receiver(outbound), this method sends data TO external SOAP service
        return invokeSoapService(payload);
    }

    private AdapterOperationResult invokeSoapService(Object payload) throws Exception {
        SOAPConnection soapConnection = null;
        try {
            // Create SOAP message
            SOAPMessage soapRequest = createSoapMessage(payload);

            // Add authentication if configured
            if(config.getUsername() != null && !config.getUsername().isEmpty()) {
                addSecurityHeader(soapRequest);
            }
            // Add custom headers if configured
            if(config.getCustomHeaders() != null && !config.getCustomHeaders().isEmpty()) {
                addCustomHeaders(soapRequest, config.getCustomHeaders());
            }
            // Set SOAP action if configured
            if(config.getSoapAction() != null && !config.getSoapAction().isEmpty()) {
                soapRequest.getMimeHeaders().addHeader("SOAPAction", config.getSoapAction());
            }
            // Log outgoing message if configured
            if(config.isLogMessages()) {
                log.debug("Sending SOAP request: {}", soapMessageToString(soapRequest));
            }
            // Send SOAP message
            soapConnection = soapConnectionFactory.createConnection();
            SOAPMessage soapResponse = soapConnection.call(soapRequest, config.getEffectiveEndpoint());
            // Process response
            Map<String, Object> responseData = processSoapResponse(soapResponse);
            // Log response if configured
            if(config.isLogMessages()) {
                log.debug("Received SOAP response: {}", responseData);
            }
            log.info("SOAP outbound adapter successfully invoked service");
            return AdapterOperationResult.success(responseData,
                    "Successfully invoked SOAP service and received response");
        } catch(SOAPException soapEx) {
            if(soapEx.getCause() instanceof SOAPFault) {
                SOAPFault fault = (SOAPFault) soapEx.getCause();
                log.error("SOAP fault received: {}", fault.getFaultString());
                throw new AdapterException("SOAP fault: " + fault.getFaultString(), soapEx);
            } else {
                log.error("SOAP exception: {}", soapEx.getMessage());
                throw new AdapterException("SOAP exception: " + soapEx.getMessage(), soapEx);
            }
        } finally {
            if(soapConnection != null) {
                try {
                    soapConnection.close();
                } catch(Exception e) {
                    log.warn("Error closing SOAP connection", e);
                }
            }
        }
    }

    private SOAPMessage createSoapMessage(Object payload) throws Exception {
        SOAPMessage soapMessage = messageFactory.createMessage();
        SOAPBody soapBody = soapMessage.getSOAPBody();
        if(payload instanceof String) {
            // Parse XML string to SOAP body
            String xmlPayload = (String) payload;
            Source xmlSource = new StreamSource(new StringReader(xmlPayload));
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMResult result = new DOMResult(soapBody);
            transformer.transform(xmlSource, result);
        } else if(payload instanceof Map) {
            // Convert Map to SOAP body elements
            Map<String, Object> dataMap = (Map<String, Object>) payload;
            createSoapBodyFromMap(soapBody, dataMap);
        } else {
            throw new AdapterException("Unsupported payload type: " + payload.getClass().getName());
        }
        soapMessage.saveChanges();
        return soapMessage;
    }

    private void createSoapBodyFromMap(SOAPBody soapBody, Map<String, Object> dataMap) throws Exception {
        String namespace = config.getTargetNamespace();
        String operation = config.getOperationName();
        if(operation == null || operation.isEmpty()) {
            operation = "Request"; // Default operation name
        }
        SOAPElement operationElement = soapBody.addChildElement(operation, "ns", namespace);
        for(Map.Entry<String, Object> entry : dataMap.entrySet()) {
            SOAPElement element = operationElement.addChildElement(entry.getKey());
            element.addTextNode(String.valueOf(entry.getValue()));
        }
    }

    private void addSecurityHeader(SOAPMessage soapMessage) throws Exception {
        SOAPHeader soapHeader = soapMessage.getSOAPHeader();
        if(soapHeader == null) {
            soapHeader = soapMessage.getSOAPPart().getEnvelope().addHeader();
        }
        // Add WS - Security header
        String wsseNamespace = "http://docs.oasis - open.org/wss/2004/01/oasis-200401 - wss - wssecurity - secext-1.0.xsd";
        String wsuNamespace = "http://docs.oasis - open.org/wss/2004/01/oasis-200401 - wss - wssecurity - utility-1.0.xsd";
        SOAPElement securityElement = soapHeader.addChildElement("Security", "wsse", wsseNamespace);
        securityElement.addAttribute(new QName("mustUnderstand"), "1");
        SOAPElement usernameTokenElement = securityElement.addChildElement("UsernameToken", "wsse");
        usernameTokenElement.addAttribute(new QName(wsuNamespace, "Id", "wsu"), "UsernameToken-1");
        SOAPElement usernameElement = usernameTokenElement.addChildElement("Username", "wsse");
        usernameElement.addTextNode(config.getUsername());
        SOAPElement passwordElement = usernameTokenElement.addChildElement("Password", "wsse");
        passwordElement.setAttribute("Type", "http://docs.oasis - open.org/wss/2004/01/oasis-200401 - wss - username - token - profile-1.0#PasswordText");
        passwordElement.addTextNode(config.getPassword());
    }

    private void addCustomHeaders(SOAPMessage soapMessage, String customHeaders) throws Exception {
        SOAPHeader soapHeader = soapMessage.getSOAPHeader();
        if(soapHeader == null) {
            soapHeader = soapMessage.getSOAPPart().getEnvelope().addHeader();
        }
        // Parse custom headers(format: key1 = value1,key2 = value2)
        String[] headerPairs = customHeaders.split(",");
        for(String headerPair : headerPairs) {
            String[] keyValue = headerPair.split(" = ");
            if(keyValue.length == 2) {
                SOAPElement headerElement = soapHeader.addChildElement(keyValue[0].trim());
                headerElement.addTextNode(keyValue[1].trim());
            }
        }
    }

    private Map<String, Object> processSoapResponse(SOAPMessage soapResponse) throws Exception {
        Map<String, Object> responseData = new HashMap<>();
        // Check for SOAP fault
        SOAPBody soapBody = soapResponse.getSOAPBody();
        if(soapBody.hasFault()) {
            SOAPFault fault = soapBody.getFault();
            responseData.put("fault", true);
            responseData.put("faultCode", fault.getFaultCode());
            responseData.put("faultString", fault.getFaultString());
            responseData.put("faultActor", fault.getFaultActor());
            if(fault.getDetail() != null) {
                responseData.put("faultDetail", fault.getDetail().getTextContent());
            }
        } else {
            // Extract response body
            responseData.put("fault", false);
            responseData.put("body", soapMessageToString(soapResponse));
            // Extract specific elements if needed
            Iterator<?> bodyElements = soapBody.getChildElements();
            Map<String, Object> bodyData = new HashMap<>();
            while(bodyElements.hasNext()) {
                Object element = bodyElements.next();
                if(element instanceof SOAPElement) {
                    SOAPElement soapElement = (SOAPElement) element;
                    extractElementData(soapElement, bodyData);
                }
            }
            responseData.put("parsedBody", bodyData);
        }
        responseData.put("timestamp", new Date());
        return responseData;
    }

    private void extractElementData(SOAPElement element, Map<String, Object> data) {
        String elementName = element.getLocalName();
        // Check if element has child elements
        Iterator<?> children = element.getChildElements();
        if(children.hasNext()) {
            Map<String, Object> childData = new HashMap<>();
            while(children.hasNext()) {
                Object child = children.next();
                if(child instanceof SOAPElement) {
                    extractElementData((SOAPElement) child, childData);
                }
            }
            data.put(elementName, childData);
        } else {
            // Leaf element - get text content
            data.put(elementName, element.getTextContent());
        }
    }

    private String soapMessageToString(SOAPMessage soapMessage) throws Exception {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        soapMessage.writeTo(baos);
        return baos.toString();
    }

    private SOAPMessage createTestMessage() throws Exception {
        SOAPMessage message = messageFactory.createMessage();
        SOAPBody body = message.getSOAPBody();
        body.addChildElement("TestElement").addTextNode("Test");
        message.saveChanges();
        return message;
    }

    private void validateConfiguration() throws AdapterException {
        if(config.getEffectiveEndpoint() == null || config.getEffectiveEndpoint().trim().isEmpty()) {
            throw new AdapterException("Endpoint URL is required", null);
        }
        if(config.getSoapVersion() == null || config.getSoapVersion().trim().isEmpty()) {
            config.setSoapVersion("1.1"); // Default to SOAP 1.1
        }
    }
    public long getTimeout() {
        // SOAP receivers typically don't poll, they push data
        return config.getTimeout();
    }

    @Override
    public String getConfigurationSummary() {
        return String.format("SOAP Receiver(Outbound): %s, Action: %s, SOAP Version: %s",
                config.getEffectiveEndpoint(),
                config.getSoapAction() != null ? config.getSoapAction() : "Not specified",
                config.getSoapVersion());
    }

    @Override
    public AdapterMetadata getMetadata() {
        return AdapterMetadata.builder()
                .adapterType(AdapterConfiguration.AdapterTypeEnum.SOAP)
                .adapterMode(AdapterConfiguration.AdapterModeEnum.OUTBOUND)
                .description("SOAP Outbound adapter - sends SOAP requests")
                .version("1.0.0")
                .supportsBatch(true)
                .supportsAsync(true)
                .build();
    }


    @Override
    protected AdapterConfiguration.AdapterTypeEnum getAdapterType() {
        return AdapterConfiguration.AdapterTypeEnum.SOAP;
    }

    @Override
    protected AdapterConfiguration.AdapterModeEnum getAdapterMode() {
        return AdapterConfiguration.AdapterModeEnum.OUTBOUND;
    }

    @Override
    protected AdapterOperationResult performStart() {
        return AdapterOperationResult.success("Started");
    }

    @Override
    protected AdapterOperationResult performStop() {
        return AdapterOperationResult.success("Stopped");
    }

    @Override
    public boolean supportsBatchOperations() {
        return false; // SOAP typically doesn't support batch operations
    }

    @Override
    public int getMaxBatchSize() {
        return 1; // SOAP processes one message at a time
    }

    @Override
    public CompletableFuture<AdapterOperationResult> sendAsync(SendRequest request) {
        return CompletableFuture.supplyAsync(() -> send(request));
    }

    @Override
    public AdapterOperationResult sendBatch(List<SendRequest> requests) {
        // SOAP doesn't support batch operations, process sequentially
        List<AdapterOperationResult> results = new ArrayList<>();
        for(SendRequest request : requests) {
            results.add(send(request));
        }

        long successCount = results.stream().filter(AdapterOperationResult::isSuccess).count();
        return successCount == results.size()
            ? AdapterOperationResult.success("All " + successCount + " requests sent successfully")
            : AdapterOperationResult.failure(successCount + " succeeded, " + (results.size() - successCount) + " failed");
    }

    @Override
    public CompletableFuture<AdapterOperationResult> sendBatchAsync(List<SendRequest> requests) {
        return CompletableFuture.supplyAsync(() -> sendBatch(requests));
    }

}
