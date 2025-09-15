package com.integrixs.soapbindings.infrastructure.repository;

import com.integrixs.soapbindings.domain.model.GeneratedBinding;
import com.integrixs.soapbindings.domain.repository.GeneratedBindingRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In - memory implementation of generated binding repository
 */
@Repository
public class InMemoryGeneratedBindingRepository implements GeneratedBindingRepository {

    private final Map<String, GeneratedBinding> bindings = new ConcurrentHashMap<>();

    @Override
    public GeneratedBinding save(GeneratedBinding generatedBinding) {
        bindings.put(generatedBinding.getGenerationId(), generatedBinding);
        return generatedBinding;
    }

    @Override
    public Optional<GeneratedBinding> findById(String generationId) {
        return Optional.ofNullable(bindings.get(generationId));
    }

    @Override
    public List<GeneratedBinding> findByWsdlId(String wsdlId) {
        return bindings.values().stream()
                .filter(binding -> wsdlId.equals(binding.getWsdlId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<GeneratedBinding> findByServiceName(String serviceName) {
        return bindings.values().stream()
                .filter(binding -> serviceName.equals(binding.getServiceName()))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<GeneratedBinding> findLatestByWsdlId(String wsdlId) {
        return bindings.values().stream()
                .filter(binding -> wsdlId.equals(binding.getWsdlId()))
                .max(Comparator.comparing(GeneratedBinding::getGenerationTime,
                        Comparator.nullsLast(Comparator.naturalOrder())));
    }

    @Override
    public List<GeneratedBinding> findSuccessful() {
        return bindings.values().stream()
                .filter(GeneratedBinding::isSuccessful)
                .collect(Collectors.toList());
    }

    @Override
    public GeneratedBinding update(GeneratedBinding generatedBinding) {
        return save(generatedBinding);
    }

    @Override
    public void deleteById(String generationId) {
        bindings.remove(generationId);
    }

    @Override
    public int deleteOldGenerations(String wsdlId, int keepCount) {
        List<GeneratedBinding> wsdlBindings = findByWsdlId(wsdlId);

        if(wsdlBindings.size() <= keepCount) {
            return 0;
        }

        // Sort by generation time(newest first)
        wsdlBindings.sort(Comparator.comparing(GeneratedBinding::getGenerationTime,
                Comparator.nullsLast(Comparator.reverseOrder())));

        // Remove oldest bindings
        int deleted = 0;
        for(int i = keepCount; i < wsdlBindings.size(); i++) {
            bindings.remove(wsdlBindings.get(i).getGenerationId());
            deleted++;
        }

        return deleted;
    }

    public List<GeneratedBinding> findAll() {
        return new ArrayList<>(bindings.values());
    }

    public boolean existsById(String bindingId) {
        return bindings.containsKey(bindingId);
    }
}
