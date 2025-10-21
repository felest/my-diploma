package org.example.diploma.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegistrationRequest {
    @Size(min = 5, message = "Username must be at least 5 characters")
    private String username;

    @Size(min = 5, message = "Password must be at least 5 characters")
    @Pattern(regexp = "^[^<>*?!:]+$", message = "Password cannot contain <, *, ?, :, ! characters")
    private String password;

    private String role;

    // Геттеры и сеттеры
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
