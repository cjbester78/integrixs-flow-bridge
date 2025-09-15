package com.integrixs.soapbindings.domain.service;

import com.integrixs.soapbindings.domain.model.SoapBinding;

/**
 * Domain service interface for SOAP client operations
 */
public interface SoapClientService {

    /**
     * Create SOAP client for binding
     * @param binding SOAP binding configuration
     * @param serviceClass Service interface class
     * @param <T> Service interface type
     * @return SOAP client instance
     */
    <T> T createClient(SoapBinding binding, Class<T> serviceClass);

    /**
     * Invoke SOAP operation
     * @param client SOAP client
     * @param operationName Operation name
     * @param request Request object
     * @return Response object
     */
    Object invokeOperation(Object client, String operationName, Object request);

    /**
     * Invoke SOAP operation with timeout
     * @param client SOAP client
     * @param operationName Operation name
     * @param request Request object
     * @param timeoutMillis Timeout in milliseconds
     * @return Response object
     */
    Object invokeOperationWithTimeout(Object client, String operationName, Object request, long timeoutMillis);

    /**
     * Set SOAP headers for client
     * @param client SOAP client
     * @param headers SOAP headers
     */
    void setSoapHeaders(Object client, java.util.Map<String, String> headers);

    /**
     * Configure security for client
     * @param client SOAP client
     * @param binding SOAP binding with security config
     */
    void configureSecurity(Object client, SoapBinding binding);

    /**
     * Test SOAP service availability
     * @param binding SOAP binding
     * @return true if service is available
     */
    boolean testServiceAvailability(SoapBinding binding);

    /**
     * Get WSDL from service endpoint
     * @param endpointUrl Service endpoint URL
     * @return WSDL content
     */
    String getWsdlFromEndpoint(String endpointUrl);

    /**
     * Validate SOAP request against WSDL
     * @param binding SOAP binding
     * @param request Request object
     * @return true if valid
     */
    boolean validateRequest(SoapBinding binding, Object request);

    /**
     * Transform object to SOAP message
     * @param object Object to transform
     * @return SOAP message as string
     */
    String objectToSoapMessage(Object object);

    /**
     * Transform SOAP message to object
     * @param soapMessage SOAP message
     * @param targetClass Target class
     * @param <T> Target type
     * @return Transformed object
     */
    <T> T soapMessageToObject(String soapMessage, Class<T> targetClass);
}
