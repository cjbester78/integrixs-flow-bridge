package com.integrixs.webclient;

import com.integrixs.adapters.domain.model.AdapterOperationResult;
import com.integrixs.adapters.domain.port.OutboundAdapterPort;
import com.integrixs.adapters.factory.AdapterFactoryManager;
import com.integrixs.adapters.config.SoapOutboundAdapterConfig;
import com.integrixs.adapters.domain.model.SendRequest;
import com.integrixs.adapters.domain.model.AdapterConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.util.UUID;

/**
 * Inbound SOAP Endpoint for receiving SOAP requests from external systems.
 * Routes incoming SOAP messages through the adapter framework for processing.
 */
@Endpoint
public class InboundSoapEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(InboundSoapEndpoint.class);
    private static final String NAMESPACE_URI = "http://integrationlab.com/inbound";

    private final AdapterFactoryManager adapterFactory;

    public InboundSoapEndpoint() {
        this.adapterFactory = AdapterFactoryManager.getInstance();
    }

    /**
     * Handle generic inbound SOAP requests
     */
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "InboundSoapRequest")
    @ResponsePayload
    public Source handleInboundSoapRequest(@RequestPayload Source request) {

        logger.info("Received inbound SOAP request");

        try {
            // Create SOAP outbound adapter configuration
            SoapOutboundAdapterConfig config = createSoapConfig();

            // Create and initialize adapter
            OutboundAdapterPort adapter = adapterFactory.createReceiver(AdapterConfiguration.AdapterTypeEnum.SOAP, config);

            // Create AdapterConfiguration for initialization
            AdapterConfiguration adapterConfig = AdapterConfiguration.builder()
                    .adapterId(UUID.randomUUID().toString())
                    .name("SOAP Receiver")
                    .adapterType(AdapterConfiguration.AdapterTypeEnum.SOAP)
                    .adapterMode(AdapterConfiguration.AdapterModeEnum.OUTBOUND)
                    .connectionProperties(new java.util.HashMap<>())
                    .operationProperties(new java.util.HashMap<>())
                    .build();

            adapter.initialize(adapterConfig);

            try {
                // Process the inbound SOAP message
                SendRequest sendRequest = SendRequest.builder()
                        .payload(request)
                        .build();
                AdapterOperationResult result = adapter.send(sendRequest);

                if(result.isSuccess()) {
                    logger.info("Successfully processed inbound SOAP message");
                    return createSuccessResponse("Message processed successfully");
                } else {
                    logger.error("Failed to process inbound SOAP message: {}", result.getMessage());
                    return createErrorResponse("Failed to process message: " + result.getMessage());
                }
            } finally {
                adapter.shutdown();
            }

        } catch(Exception e) {
            logger.error("Error processing inbound SOAP request", e);
            return createErrorResponse("Internal server error: " + e.getMessage());
        }
    }


    /**
     * Create SOAP outbound adapter configuration
     */
    private SoapOutboundAdapterConfig createSoapConfig() {
        SoapOutboundAdapterConfig config = new SoapOutboundAdapterConfig();

        // Set basic SOAP configuration
        config.setServiceEndpointUrl("http://localhost:8080/ws/inbound");
        config.setSoapAction("process");

        return config;
    }

    /**
     * Create success response
     */
    private Source createSuccessResponse(String message) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();

            Element responseElement = document.createElementNS(NAMESPACE_URI, "InboundSoapResponse");
            Element statusElement = document.createElement("status");
            statusElement.setTextContent("SUCCESS");
            Element messageElement = document.createElement("message");
            messageElement.setTextContent(message);

            responseElement.appendChild(statusElement);
            responseElement.appendChild(messageElement);
            document.appendChild(responseElement);

            return new DOMSource(document);
        } catch(Exception e) {
            logger.error("Error creating success response", e);
            return createErrorResponse("Error creating response");
        }
    }

    /**
     * Create error response
     */
    private Source createErrorResponse(String errorMessage) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();

            Element responseElement = document.createElementNS(NAMESPACE_URI, "InboundSoapResponse");
            Element statusElement = document.createElement("status");
            statusElement.setTextContent("ERROR");
            Element messageElement = document.createElement("message");
            messageElement.setTextContent(errorMessage);

            responseElement.appendChild(statusElement);
            responseElement.appendChild(messageElement);
            document.appendChild(responseElement);

            return new DOMSource(document);
        } catch(Exception e) {
            logger.error("Error creating error response", e);
            // Return a simple fallback response
            return null;
        }
    }

}
