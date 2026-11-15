package com.university.helpdesk.dto;

import com.university.helpdesk.entity.model.Role;
import com.fasterxml.jackson.annotation.JsonAlias;

public class RegisterRequest {
    private String username;
    private String password;

    @JsonAlias("name")
    private String fullName; // Matches M1's frontend JSON

    private String email;
    private Role role;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}