package com.integrixs.backend.application.service;

import com.integrixs.data.sql.repository.RoleSqlRepository;
import com.integrixs.data.sql.repository.UserSqlRepository;
import com.integrixs.backend.domain.service.RoleManagementService;
import com.integrixs.backend.exception.ConflictException;
import com.integrixs.backend.exception.ResourceNotFoundException;
import com.integrixs.backend.service.AuditTrailService;
import com.integrixs.data.model.Role;
import com.integrixs.shared.dto.RoleDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application service for role management
 * Orchestrates role operations across domain services
 */
@Service
public class RoleManagementApplicationService {

    private static final Logger log = LoggerFactory.getLogger(RoleManagementApplicationService.class);


    private final RoleSqlRepository roleRepository;
    private final UserSqlRepository userRepository;
    private final RoleManagementService roleManagementService;
    private final AuditTrailService auditTrailService;

    public RoleManagementApplicationService(RoleSqlRepository roleRepository,
                                          UserSqlRepository userRepository,
                                          RoleManagementService roleManagementService,
                                          AuditTrailService auditTrailService) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.roleManagementService = roleManagementService;
        this.auditTrailService = auditTrailService;
    }

    /**
     * Get all roles with pagination
     */
    public List<RoleDTO> getAllRoles(int page, int limit) {
        log.debug("Fetching all roles - page: {}, limit: {}", page, limit);

        PageRequest pageRequest = PageRequest.of(page, limit);
        Page<Role> rolePage = roleRepository.findAll(pageRequest);

        return rolePage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get role by ID
     */
    public RoleDTO getRoleById(String id) {
        log.debug("Fetching role by id: {}", id);

        UUID roleId = UUID.fromString(id);
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));

        return convertToDTO(role);
    }

    /**
     * Get role by name
     */
    public RoleDTO getRoleByName(String name) {
        log.debug("Fetching role by name: {}", name);

        Role role = roleRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with name: " + name));

        return convertToDTO(role);
    }

    /**
     * Create new role
     */
    public RoleDTO createRole(RoleDTO roleDTO, String userId) {
        log.info("Creating new role: {}", roleDTO.getName());

        // Check if role name already exists
        if(roleRepository.existsByName(roleDTO.getName())) {
            throw new ConflictException("Role already exists with name: " + roleDTO.getName());
        }

        // Create role entity
        Role role = new Role();
        role.setName(roleDTO.getName().toUpperCase());

        // Set default permissions if not provided
        String permissions = roleDTO.getDescription(); // Using description field for permissions
        if(permissions == null || permissions.trim().isEmpty()) {
            permissions = roleManagementService.getDefaultPermissions();
        }

        // Validate
        roleManagementService.validateRole(role);
        roleManagementService.validatePermissions(permissions);

        role.setPermissions(permissions);

        // Save
        Role savedRole = roleRepository.save(role);

        // Audit trail
        auditTrailService.logCreate(
            "Role",
            savedRole.getId().toString(),
            savedRole
       );

        log.info("Role created successfully: {}", savedRole.getId());

        return convertToDTO(savedRole);
    }

    /**
     * Update existing role
     */
    public RoleDTO updateRole(String id, RoleDTO roleDTO, String userId) {
        log.info("Updating role: {}", id);

        UUID roleId = UUID.fromString(id);
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));

        // Check if it's a system role
        if(roleManagementService.isSystemRole(role)) {
            throw new AccessDeniedException("System roles cannot be modified");
        }

        // Check for name conflicts
        if(!role.getName().equals(roleDTO.getName()) &&
            roleRepository.existsByName(roleDTO.getName())) {
            throw new ConflictException("Role already exists with name: " + roleDTO.getName());
        }

        // Update fields
        role.setName(roleDTO.getName().toUpperCase());

        // Update permissions if provided
        String permissions = roleDTO.getDescription(); // Using description field for permissions
        if(permissions != null && !permissions.trim().isEmpty()) {
            roleManagementService.validatePermissions(permissions);
            role.setPermissions(permissions);
        }

        // Validate
        roleManagementService.validateRole(role);

        // Save
        Role updatedRole = roleRepository.save(role);

        // Audit trail
        auditTrailService.logUpdate(
            "Role",
            updatedRole.getId().toString(),
            role,
            updatedRole
       );

        log.info("Role updated successfully: {}", updatedRole.getId());

        return convertToDTO(updatedRole);
    }

    /**
     * Delete role
     */
    public void deleteRole(String id, String userId) {
        log.info("Deleting role: {}", id);

        UUID roleId = UUID.fromString(id);
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));

        // Check if it's a system role
        if(roleManagementService.isSystemRole(role)) {
            throw new AccessDeniedException("System roles cannot be deleted");
        }

        // Check if role is in use by any users
        long userCount = userRepository.countByRole(role.getName());
        if(userCount > 0) {
            throw new ConflictException(String.format(
                "Cannot delete role '%s'. It is currently assigned to %d user(s).",
                role.getName(), userCount
           ));
        }

        // Delete
        roleRepository.deleteById(roleId);

        // Audit trail
        auditTrailService.logDelete(
            "Role",
            roleId.toString(),
            role
       );

        log.info("Role deleted successfully: {}", roleId);
    }

    /**
     * Get all roles(without pagination)
     */
    public List<RoleDTO> getAllRoles() {
        log.debug("Fetching all roles");

        return roleRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convert Role entity to DTO
     */
    private RoleDTO convertToDTO(Role role) {
        return RoleDTO.builder()
                .id(role.getId().toString())
                .name(role.getName())
                .description(role.getPermissions()) // Using permissions as description
                .createdAt(LocalDateTime.now()) // Role entity doesn't have createdAt
                .build();
    }
}
