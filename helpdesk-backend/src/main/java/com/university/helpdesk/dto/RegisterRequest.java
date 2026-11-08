package com.university.helpdesk.dto;

import com.university.helpdesk.entity.model.Role;

public class RegisterRequest {
    private String username;
    private String password;
    private String name;
    private Role role; // STUDENT_STAFF, TECHNICIAN, or ADMIN

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}