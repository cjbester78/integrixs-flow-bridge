package com.integrixs.backend.application.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrixs.backend.api.dto.request.ChangePasswordRequest;
import com.integrixs.backend.api.dto.request.CreateUserRequest;
import com.integrixs.backend.api.dto.request.UpdateUserRequest;
import com.integrixs.backend.api.dto.response.PagedUserResponse;
import com.integrixs.backend.api.dto.response.UserResponse;
import com.integrixs.backend.domain.service.UserManagementService;
import com.integrixs.backend.service.AuditTrailService;
import com.integrixs.data.model.User;
import com.integrixs.data.sql.repository.UserSqlRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

/**
 * Application service for user management
 */
@Service
public class UserManagementApplicationService {

    private static final Logger log = LoggerFactory.getLogger(UserManagementApplicationService.class);


    private final UserSqlRepository userRepository;
    private final UserManagementService userManagementService;
    private final AuditTrailService auditTrailService;
    private final ObjectMapper objectMapper;

    public UserManagementApplicationService(UserSqlRepository userRepository,
                                          UserManagementService userManagementService,
                                          AuditTrailService auditTrailService,
                                          ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.userManagementService = userManagementService;
        this.auditTrailService = auditTrailService;
        this.objectMapper = objectMapper;
    }

    public UserResponse createUser(CreateUserRequest request, String createdBy) {
        log.debug("Creating new user: {}", request.getUsername());

        // Check if username already exists
        if(userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Check if email already exists
        if(userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Create user
        User user = userManagementService.createUser(
            request.getUsername(),
            request.getEmail(),
            request.getFirstName(),
            request.getLastName(),
            request.getPassword(),
            request.getRole()
       );

        user = userRepository.save(user);

        // Audit trail
        auditTrailService.logUserAction(
            user,
            "USER_CREATED",
            "User created: " + user.getUsername(),
            createdBy
       );

        return convertToResponse(user);
    }

    @Cacheable(value = "users", key = "#id")
    public Optional<UserResponse> getUserById(String id) {
        log.debug("Getting user by ID: {}", id);

        return userRepository.findById(UUID.fromString(id))
            .map(this::convertToResponse);
    }

    public Optional<UserResponse> getUserByUsername(String username) {
        log.debug("Getting user by username: {}", username);

        return userRepository.findByUsername(username)
            .map(this::convertToResponse);
    }

    public PagedUserResponse getUsers(int page, int size, String sortBy, String sortDirection) {
        log.debug("Getting users - page: {}, size: {}", page, size);

        Sort.Direction direction = "ASC".equalsIgnoreCase(sortDirection)
            ? Sort.Direction.ASC
            : Sort.Direction.DESC;

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<User> userPage = userRepository.findAll(pageRequest);

        List<UserResponse> users = userPage.getContent().stream()
            .map(this::convertToResponse)
            .toList();

        return PagedUserResponse.builder()
            .content(users)
            .pageNumber(userPage.getNumber())
            .pageSize(userPage.getSize())
            .totalElements(userPage.getTotalElements())
            .totalPages(userPage.getTotalPages())
            .first(userPage.isFirst())
            .last(userPage.isLast())
            .empty(userPage.isEmpty())
            .build();
    }

    @CachePut(value = "users", key = "#id")
    public Optional<UserResponse> updateUser(String id, UpdateUserRequest request, String updatedBy) {
        log.debug("Updating user: {}", id);

        return userRepository.findById(UUID.fromString(id)).map(user -> {
            // Check email uniqueness if changed
            if(request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
                Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
                if(existingUser.isPresent() && !existingUser.get().getId().equals(user.getId())) {
                    throw new IllegalArgumentException("Email already exists");
                }
            }

            // Update user
            userManagementService.updateUserInfo(
                user,
                request.getEmail(),
                request.getFirstName(),
                request.getLastName(),
                request.getRole(),
                request.getStatus()
           );

            // Update permissions if provided
            if(request.getPermissions() != null) {
                try {
                    String permissionsJson = objectMapper.writeValueAsString(request.getPermissions());
                    user.setPermissions(permissionsJson);
                } catch(Exception e) {
                    log.error("Failed to serialize permissions", e);
                }
            }

            user = userRepository.save(user);

            // Audit trail
            auditTrailService.logUserAction(
                user,
                "USER_UPDATED",
                "User information updated",
                updatedBy
           );

            return convertToResponse(user);
        });
    }

    public void changePassword(String id, ChangePasswordRequest request, String changedBy) {
        log.debug("Changing password for user: {}", id);

        User user = userRepository.findById(UUID.fromString(id))
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify current password
        if(!userManagementService.validatePassword(user, request.getCurrentPassword())) {
            throw new AccessDeniedException("Current password is incorrect");
        }

        // Change password
        userManagementService.changePassword(user, request.getNewPassword());
        userRepository.save(user);

        // Audit trail
        auditTrailService.logUserAction(
            user,
            "PASSWORD_CHANGED",
            "User password changed",
            changedBy
       );
    }

    @CacheEvict(value = "users", key = "#id")
    public boolean deleteUser(String id, String deletedBy) {
        log.debug("Deleting user: {}", id);

        Optional<User> userOpt = userRepository.findById(UUID.fromString(id));
        if(userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();

        // Validate deletion
        userManagementService.validateDeletion(user);

        // Delete user
        userRepository.delete(user);

        // Audit trail
        auditTrailService.logUserAction(
            user,
            "USER_DELETED",
            "User deleted: " + user.getUsername(),
            deletedBy
       );

        return true;
    }

    public void activateUser(String id, String activatedBy) {
        log.debug("Activating user: {}", id);

        User user = userRepository.findById(UUID.fromString(id))
            .orElseThrow(() -> new RuntimeException("User not found"));

        userManagementService.activateUser(user);
        userRepository.save(user);

        // Audit trail
        auditTrailService.logUserAction(
            user,
            "USER_ACTIVATED",
            "User activated",
            activatedBy
       );
    }

    public void deactivateUser(String id, String deactivatedBy) {
        log.debug("Deactivating user: {}", id);

        User user = userRepository.findById(UUID.fromString(id))
            .orElseThrow(() -> new RuntimeException("User not found"));

        userManagementService.deactivateUser(user);
        userRepository.save(user);

        // Audit trail
        auditTrailService.logUserAction(
            user,
            "USER_DEACTIVATED",
            "User deactivated",
            deactivatedBy
       );
    }

    private UserResponse convertToResponse(User user) {
        Map<String, Object> permissions = Collections.emptyMap();

        try {
            if(user.getPermissions() != null) {
                permissions = objectMapper.readValue(
                    user.getPermissions(),
                    new TypeReference<Map<String, Object>>() {}
               );
            }
        } catch(Exception e) {
            log.error("Failed to parse user permissions", e);
        }

        return UserResponse.builder()
            .id(user.getId().toString())
            .username(user.getUsername())
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .role(user.getRole())
            .status(user.getStatus())
            .permissions(permissions)
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .lastLoginAt(user.getLastLoginAt())
            .build();
    }
}
