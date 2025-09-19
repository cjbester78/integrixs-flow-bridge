package com.integrixs.backend.domain.service;

import com.integrixs.data.model.Role;
import org.springframework.stereotype.Service;

/**
 * Domain service for role management
 * Contains core business logic for role operations
 */
@Service
public class RoleManagementService {

    /**
     * Validate role data
     */
    public void validateRole(Role role) {
        if(role.getName() == null || role.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Role name is required");
        }

        if(role.getName().length() > 50) {
            throw new IllegalArgumentException("Role name cannot exceed 50 characters");
        }

        // Validate role name format - should be uppercase with underscores
        if(!role.getName().matches("^[A - Z][A - Z0-9_]*$")) {
            throw new IllegalArgumentException("Role name must be uppercase letters, numbers, and underscores only, starting with a letter");
        }
    }

    /**
     * Check if role is a system role(cannot be modified)
     */
    public boolean isSystemRole(Role role) {
        // System roles that cannot be modified
        return "ADMINISTRATOR".equals(role.getName()) ||
               "DEVELOPER".equals(role.getName()) ||
               "INTEGRATOR".equals(role.getName()) ||
               "VIEWER".equals(role.getName());
    }

    /**
     * Validate role permissions JSON
     */
    public void validatePermissions(String permissions) {
        if(permissions != null && !permissions.trim().isEmpty()) {
            // Validate that permissions is valid JSON
            try {
                // Basic JSON validation - could be enhanced with actual JSON parser
                if(!permissions.trim().startsWith(" {") || !permissions.trim().endsWith("}")) {
                    throw new IllegalArgumentException("Permissions must be valid JSON");
                }
            } catch(Exception e) {
                throw new IllegalArgumentException("Invalid permissions JSON format", e);
            }
        }
    }

    /**
     * Apply default permissions for new roles
     */
    public String getDefaultPermissions() {
        return " {\n" +
               " \"flows\": {\n" +
               "    \"view\": true,\n" +
               "    \"create\": false,\n" +
               "    \"edit\": false,\n" +
               "    \"delete\": false\n" +
               " },\n" +
               " \"adapters\": {\n" +
               "    \"view\": true,\n" +
               "    \"create\": false,\n" +
               "    \"edit\": false,\n" +
               "    \"delete\": false\n" +
               " },\n" +
               " \"users\": {\n" +
               "    \"view\": false,\n" +
               "    \"create\": false,\n" +
               "    \"edit\": false,\n" +
               "    \"delete\": false\n" +
               " }\n" +
               "}";
    }
}
