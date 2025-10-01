package com.integrixs.backend.marketplace.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
public class AddMemberRequest {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String role;

    // Default constructor
    public AddMemberRequest() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}