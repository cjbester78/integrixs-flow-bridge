package com.integrixs.soapbindings.infrastructure.repository;

import com.integrixs.soapbindings.domain.model.WsdlDefinition;
import com.integrixs.soapbindings.domain.repository.WsdlRepository;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In - memory implementation of WSDL repository
 */
@Repository
public class InMemoryWsdlRepository implements WsdlRepository {

    private final Map<String, WsdlDefinition> wsdls = new ConcurrentHashMap<>();

    @Override
    public WsdlDefinition save(WsdlDefinition wsdl) {
        wsdls.put(wsdl.getWsdlId(), wsdl);
        return wsdl;
    }

    @Override
    public Optional<WsdlDefinition> findById(String wsdlId) {
        return Optional.ofNullable(wsdls.get(wsdlId));
    }

    @Override
    public Optional<WsdlDefinition> findByName(String name) {
        return wsdls.values().stream()
                .filter(wsdl -> name.equals(wsdl.getName()))
                .findFirst();
    }

    @Override
    public List<WsdlDefinition> findAll() {
        return new ArrayList<>(wsdls.values());
    }

    @Override
    public List<WsdlDefinition> findByNamespace(String namespace) {
        return wsdls.values().stream()
                .filter(wsdl -> namespace.equals(wsdl.getNamespace()))
                .collect(Collectors.toList());
    }

    @Override
    public WsdlDefinition update(WsdlDefinition wsdl) {
        return save(wsdl);
    }

    @Override
    public boolean existsById(String wsdlId) {
        return wsdls.containsKey(wsdlId);
    }

    @Override
    public boolean existsByName(String name) {
        return wsdls.values().stream()
                .anyMatch(wsdl -> name.equals(wsdl.getName()));
    }

    @Override
    public void deleteById(String wsdlId) {
        wsdls.remove(wsdlId);
    }

    public long count() {
        return wsdls.size();
    }
}
