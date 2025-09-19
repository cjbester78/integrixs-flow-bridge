package com.integrixs.backend.plugin.validation;

import com.integrixs.backend.plugin.api.AdapterPlugin;
import com.integrixs.backend.plugin.api.PluginInitializationException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PluginValidator {
    
    public ValidationResult validate(AdapterPlugin plugin) {
        List<String> errors = new ArrayList<>();
        
        // Validate plugin metadata
        if (plugin.getMetadata() == null) {
            errors.add("Plugin metadata is required");
        } else {
            if (plugin.getMetadata().getName() == null || plugin.getMetadata().getName().isEmpty()) {
                errors.add("Plugin name is required");
            }
            if (plugin.getMetadata().getVersion() == null || plugin.getMetadata().getVersion().isEmpty()) {
                errors.add("Plugin version is required");
            }
        }
        
        // Validate handlers
        if (plugin.getInboundHandler() == null && plugin.getOutboundHandler() == null) {
            errors.add("Plugin must provide at least one handler (inbound or outbound)");
        }
        
        return errors.isEmpty() ? ValidationResult.success() : ValidationResult.failure(errors);
    }
    
    public static class ValidationResult {
        private final boolean valid;
        private final List<String> errors;
        
        private ValidationResult(boolean valid, List<String> errors) {
            this.valid = valid;
            this.errors = errors;
        }
        
        public static ValidationResult success() {
            return new ValidationResult(true, new ArrayList<>());
        }
        
        public static ValidationResult failure(List<String> errors) {
            return new ValidationResult(false, errors);
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public List<String> getErrors() {
            return errors;
        }
    }
}