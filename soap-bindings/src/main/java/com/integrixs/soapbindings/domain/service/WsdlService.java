package com.integrixs.soapbindings.domain.service;

import com.integrixs.soapbindings.domain.model.WsdlDefinition;

import java.util.List;

/**
 * Domain service interface for WSDL operations
 */
public interface WsdlService {

    /**
     * Parse WSDL from content
     * @param wsdlContent WSDL content
     * @param location WSDL location/URL
     * @return Parsed WSDL definition
     */
    WsdlDefinition parseWsdl(String wsdlContent, String location);

    /**
     * Load WSDL from URL
     * @param wsdlUrl WSDL URL
     * @return WSDL definition
     */
    WsdlDefinition loadWsdlFromUrl(String wsdlUrl);

    /**
     * Load WSDL from file
     * @param filePath File path
     * @return WSDL definition
     */
    WsdlDefinition loadWsdlFromFile(String filePath);

    /**
     * Validate WSDL definition
     * @param wsdl WSDL definition
     * @return true if valid
     */
    boolean validateWsdl(WsdlDefinition wsdl);

    /**
     * Save WSDL definition
     * @param wsdl WSDL definition
     * @return Saved WSDL
     */
    WsdlDefinition saveWsdl(WsdlDefinition wsdl);

    /**
     * Get WSDL by ID
     * @param wsdlId WSDL ID
     * @return WSDL definition
     */
    WsdlDefinition getWsdl(String wsdlId);

    /**
     * Get all WSDLs
     * @return List of WSDL definitions
     */
    List<WsdlDefinition> getAllWsdls();

    /**
     * Update WSDL
     * @param wsdl Updated WSDL
     * @return Updated WSDL
     */
    WsdlDefinition updateWsdl(WsdlDefinition wsdl);

    /**
     * Delete WSDL
     * @param wsdlId WSDL ID
     */
    void deleteWsdl(String wsdlId);

    /**
     * Extract service names from WSDL
     * @param wsdl WSDL definition
     * @return List of service names
     */
    List<String> extractServiceNames(WsdlDefinition wsdl);

    /**
     * Extract operations from WSDL
     * @param wsdl WSDL definition
     * @param serviceName Service name
     * @return List of operation names
     */
    List<String> extractOperations(WsdlDefinition wsdl, String serviceName);
}
