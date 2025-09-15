package com.integrixs.backend.api.dto.auth;

import com.integrixs.shared.dto.user.UserDTO;
import lombok.Builder;
import lombok.Data;

/**
 * Login response DTO
 */
@Data
@Builder
public class LoginResponse {
    private String token;
    private String refreshToken;
    private Long expiresIn;
    private UserDTO user;
}
