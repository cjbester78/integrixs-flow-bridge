package com.integrixs.soapbindings.domain.repository;

import com.integrixs.soapbindings.domain.model.WsdlDefinition;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for WSDL definitions
 */
public interface WsdlRepository {

    /**
     * Save WSDL definition
     * @param wsdl WSDL to save
     * @return Saved WSDL
     */
    WsdlDefinition save(WsdlDefinition wsdl);

    /**
     * Find WSDL by ID
     * @param wsdlId WSDL ID
     * @return WSDL if found
     */
    Optional<WsdlDefinition> findById(String wsdlId);

    /**
     * Find WSDL by name
     * @param name WSDL name
     * @return WSDL if found
     */
    Optional<WsdlDefinition> findByName(String name);

    /**
     * Find all WSDLs
     * @return List of all WSDLs
     */
    List<WsdlDefinition> findAll();

    /**
     * Find WSDLs by namespace
     * @param namespace Target namespace
     * @return List of WSDLs
     */
    List<WsdlDefinition> findByNamespace(String namespace);

    /**
     * Update WSDL
     * @param wsdl WSDL to update
     * @return Updated WSDL
     */
    WsdlDefinition update(WsdlDefinition wsdl);

    /**
     * Delete WSDL
     * @param wsdlId WSDL ID
     */
    void deleteById(String wsdlId);

    /**
     * Check if WSDL exists
     * @param wsdlId WSDL ID
     * @return true if exists
     */
    boolean existsById(String wsdlId);

    /**
     * Check if WSDL name exists
     * @param name WSDL name
     * @return true if exists
     */
    boolean existsByName(String name);
}
