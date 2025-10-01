package com.integrixs.shared.dto.user;

/**
 * DTO for UserRegisterResponseDTO.
 * Encapsulates data for transport between layers.
 */
public class UserRegisterResponseDTO {

    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String role;
    private String status;

    // Getters and setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }
     public void setStatus(String status) {
            this.status = status;
        }
}
