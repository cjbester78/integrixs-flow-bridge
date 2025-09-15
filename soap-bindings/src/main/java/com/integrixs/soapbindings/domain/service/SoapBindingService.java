package com.integrixs.soapbindings.domain.service;

import com.integrixs.soapbindings.domain.model.GeneratedBinding;
import com.integrixs.soapbindings.domain.model.SoapBinding;
import com.integrixs.soapbindings.domain.model.WsdlDefinition;

import java.util.List;

/**
 * Domain service interface for SOAP binding operations
 */
public interface SoapBindingService {

    /**
     * Generate SOAP binding classes from WSDL
     * @param wsdl WSDL definition
     * @param packageName Target package name
     * @return Generated binding information
     */
    GeneratedBinding generateBinding(WsdlDefinition wsdl, String packageName);

    /**
     * Create SOAP binding configuration
     * @param binding SOAP binding configuration
     * @return Created binding
     */
    SoapBinding createBinding(SoapBinding binding);

    /**
     * Update SOAP binding configuration
     * @param binding Updated binding
     * @return Updated binding
     */
    SoapBinding updateBinding(SoapBinding binding);

    /**
     * Get SOAP binding by ID
     * @param bindingId Binding ID
     * @return SOAP binding
     */
    SoapBinding getBinding(String bindingId);

    /**
     * Get bindings by WSDL ID
     * @param wsdlId WSDL ID
     * @return List of bindings
     */
    List<SoapBinding> getBindingsByWsdl(String wsdlId);

    /**
     * Get all active bindings
     * @return List of active bindings
     */
    List<SoapBinding> getActiveBindings();

    /**
     * Activate binding
     * @param bindingId Binding ID
     */
    void activateBinding(String bindingId);

    /**
     * Deactivate binding
     * @param bindingId Binding ID
     */
    void deactivateBinding(String bindingId);

    /**
     * Delete binding
     * @param bindingId Binding ID
     */
    void deleteBinding(String bindingId);

    /**
     * Test binding connectivity
     * @param bindingId Binding ID
     * @return true if endpoint is reachable
     */
    boolean testBindingConnectivity(String bindingId);

    /**
     * Compile generated binding classes
     * @param generatedBinding Generated binding info
     * @return true if compilation successful
     */
    boolean compileBinding(GeneratedBinding generatedBinding);

    /**
     * Load compiled binding classes
     * @param generatedBinding Generated binding info
     * @return Class loader with binding classes
     */
    ClassLoader loadBindingClasses(GeneratedBinding generatedBinding);
}
