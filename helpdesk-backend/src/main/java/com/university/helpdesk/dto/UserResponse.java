package com.university.helpdesk.dto;

import com.university.helpdesk.entity.model.User;
import com.university.helpdesk.entity.model.Role;

public class UserResponse {
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private Role role;
    private Long technicianId;

    public UserResponse(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.fullName = user.getName(); // Map backend 'name' to frontend 'fullName'
        this.email = user.getEmail();
        this.role = user.getRole();
        this.technicianId = null;
    }

    // Getters
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public Role getRole() { return role; }
    public Long getTechnicianId() { return technicianId; }
}