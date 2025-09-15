package com.integrixs.soapbindings.infrastructure.repository;

import com.integrixs.soapbindings.domain.model.SoapBinding;
import com.integrixs.soapbindings.domain.repository.SoapBindingRepository;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In - memory implementation of SOAP binding repository
 */
@Repository
public class InMemorySoapBindingRepository implements SoapBindingRepository {

    private final Map<String, SoapBinding> bindings = new ConcurrentHashMap<>();

    @Override
    public SoapBinding save(SoapBinding binding) {
        bindings.put(binding.getBindingId(), binding);
        return binding;
    }

    @Override
    public Optional<SoapBinding> findById(String bindingId) {
        return Optional.ofNullable(bindings.get(bindingId));
    }

    @Override
    public Optional<SoapBinding> findByName(String bindingName) {
        return bindings.values().stream()
                .filter(binding -> bindingName.equals(binding.getBindingName()))
                .findFirst();
    }

    @Override
    public List<SoapBinding> findAll() {
        return new ArrayList<>(bindings.values());
    }

    @Override
    public List<SoapBinding> findByWsdlId(String wsdlId) {
        return bindings.values().stream()
                .filter(binding -> wsdlId.equals(binding.getWsdlId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<SoapBinding> findByActive(boolean active) {
        return bindings.values().stream()
                .filter(binding -> binding.isActive() == active)
                .collect(Collectors.toList());
    }

    @Override
    public List<SoapBinding> findByServiceName(String serviceName) {
        return bindings.values().stream()
                .filter(binding -> serviceName.equals(binding.getServiceName()))
                .collect(Collectors.toList());
    }

    @Override
    public SoapBinding update(SoapBinding binding) {
        return save(binding);
    }

    @Override
    public boolean existsById(String bindingId) {
        return bindings.containsKey(bindingId);
    }

    public boolean existsByName(String bindingName) {
        return bindings.values().stream()
                .anyMatch(binding -> bindingName.equals(binding.getBindingName()));
    }

    @Override
    public void deleteById(String bindingId) {
        bindings.remove(bindingId);
    }

    public long count() {
        return bindings.size();
    }
}
