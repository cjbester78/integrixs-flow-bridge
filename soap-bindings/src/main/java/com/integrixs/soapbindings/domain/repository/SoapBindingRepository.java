package com.integrixs.soapbindings.domain.repository;

import com.integrixs.soapbindings.domain.model.SoapBinding;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for SOAP bindings
 */
public interface SoapBindingRepository {

    /**
     * Save SOAP binding
     * @param binding Binding to save
     * @return Saved binding
     */
    SoapBinding save(SoapBinding binding);

    /**
     * Find binding by ID
     * @param bindingId Binding ID
     * @return Binding if found
     */
    Optional<SoapBinding> findById(String bindingId);

    /**
     * Find binding by name
     * @param bindingName Binding name
     * @return Binding if found
     */
    Optional<SoapBinding> findByName(String bindingName);

    /**
     * Find all bindings
     * @return List of all bindings
     */
    List<SoapBinding> findAll();

    /**
     * Find bindings by WSDL ID
     * @param wsdlId WSDL ID
     * @return List of bindings
     */
    List<SoapBinding> findByWsdlId(String wsdlId);

    /**
     * Find active bindings
     * @return List of active bindings
     */
    List<SoapBinding> findByActive(boolean active);

    /**
     * Find bindings by service name
     * @param serviceName Service name
     * @return List of bindings
     */
    List<SoapBinding> findByServiceName(String serviceName);

    /**
     * Update binding
     * @param binding Binding to update
     * @return Updated binding
     */
    SoapBinding update(SoapBinding binding);

    /**
     * Delete binding
     * @param bindingId Binding ID
     */
    void deleteById(String bindingId);

    /**
     * Check if binding exists
     * @param bindingId Binding ID
     * @return true if exists
     */
    boolean existsById(String bindingId);
}
