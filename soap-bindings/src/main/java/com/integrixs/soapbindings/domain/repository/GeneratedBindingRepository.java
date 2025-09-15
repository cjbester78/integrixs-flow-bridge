package com.integrixs.soapbindings.domain.repository;

import com.integrixs.soapbindings.domain.model.GeneratedBinding;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for generated bindings
 */
public interface GeneratedBindingRepository {

    /**
     * Save generated binding
     * @param generatedBinding Generated binding to save
     * @return Saved generated binding
     */
    GeneratedBinding save(GeneratedBinding generatedBinding);

    /**
     * Find generated binding by ID
     * @param generationId Generation ID
     * @return Generated binding if found
     */
    Optional<GeneratedBinding> findById(String generationId);

    /**
     * Find generated bindings by WSDL ID
     * @param wsdlId WSDL ID
     * @return List of generated bindings
     */
    List<GeneratedBinding> findByWsdlId(String wsdlId);

    /**
     * Find generated bindings by service name
     * @param serviceName Service name
     * @return List of generated bindings
     */
    List<GeneratedBinding> findByServiceName(String serviceName);

    /**
     * Find latest generated binding for WSDL
     * @param wsdlId WSDL ID
     * @return Latest generated binding
     */
    Optional<GeneratedBinding> findLatestByWsdlId(String wsdlId);

    /**
     * Find successful generated bindings
     * @return List of successful generated bindings
     */
    List<GeneratedBinding> findSuccessful();

    /**
     * Update generated binding
     * @param generatedBinding Generated binding to update
     * @return Updated generated binding
     */
    GeneratedBinding update(GeneratedBinding generatedBinding);

    /**
     * Delete generated binding
     * @param generationId Generation ID
     */
    void deleteById(String generationId);

    /**
     * Delete old generated bindings for WSDL
     * @param wsdlId WSDL ID
     * @param keepCount Number of recent generations to keep
     * @return Number of deleted bindings
     */
    int deleteOldGenerations(String wsdlId, int keepCount);
}
