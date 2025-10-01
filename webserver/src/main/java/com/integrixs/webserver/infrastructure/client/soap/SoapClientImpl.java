package com.integrixs.webserver.infrastructure.client.soap;

import com.integrixs.webserver.domain.model.OutboundRequest;
import com.integrixs.webserver.domain.model.OutboundResponse;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.client.core.SoapActionCallback;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * SOAP client implementation
 */
@Component
public class SoapClientImpl {

    private static final Logger logger = LoggerFactory.getLogger(SoapClientImpl.class);

    private final WebServiceTemplate webServiceTemplate;

    public SoapClientImpl() {
        this.webServiceTemplate = new WebServiceTemplate();
    }

    /**
     * Execute SOAP service call
     * @param request Outbound request
     * @return Response from SOAP service
     */
    public OutboundResponse executeSoapCall(OutboundRequest request) {
        logger.info("Executing SOAP call to: {}", request.getTargetUrl());
        long startTime = System.currentTimeMillis();

        try {
            // Get SOAP action from headers
            String soapAction = request.getHeaders().get("SOAPAction");

            // Convert payload to XML Source
            String xmlPayload = (String) request.getPayload();
            Source requestPayload = new StreamSource(new StringReader(xmlPayload));

            // Prepare result writer
            StringWriter responseWriter = new StringWriter();
            StreamResult result = new StreamResult(responseWriter);

            // Execute SOAP call
            boolean success = webServiceTemplate.sendSourceAndReceiveToResult(
                request.getTargetUrl(),
                requestPayload,
                soapAction != null ? new SoapActionCallback(soapAction) : null,
                result
           );

            if(success) {
                return OutboundResponse.success(
                        request.getRequestId(),
                        200,
                        responseWriter.toString()
                   )
                    .withResponseTime(startTime);
            } else {
                return OutboundResponse.failure(
                        request.getRequestId(),
                        500,
                        "SOAP call failed"
                   )
                    .withResponseTime(startTime);
            }

        } catch(Exception e) {
            logger.error("Error executing SOAP call to {}: {}", request.getTargetUrl(), e.getMessage(), e);
            return OutboundResponse.failure(
                    request.getRequestId(),
                    500,
                    "SOAP call error: " + e.getMessage()
               )
                .withResponseTime(startTime);
        }
    }

    /**
     * Create dynamic SOAP client proxy
     * @param serviceClass Service interface class
     * @param serviceUrl SOAP endpoint URL
     * @param serviceQName Service QName
     * @param portQName Port QName
     * @return SOAP service proxy
     */
    public <T> T createSoapClient(Class<T> serviceClass, String serviceUrl,
                                  QName serviceQName, QName portQName) {
        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(serviceClass);
        factory.setAddress(serviceUrl);

        if(serviceQName != null) {
            factory.setServiceName(serviceQName);
        }
        if(portQName != null) {
            factory.setEndpointName(portQName);
        }

        return(T) factory.create();
    }
}
