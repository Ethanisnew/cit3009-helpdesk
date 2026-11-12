package com.university.helpdesk.dto;

import com.university.helpdesk.entity.model.Technician;

public class TechnicianResponse {
    private Long id;
    private String fullName;
    private String username;
    private String email;
    private String specialization;

    public TechnicianResponse(Technician t) {
        this.id = t.getId();
        this.fullName = t.getName();
        this.username = t.getUsername();
        this.email = null; // DB doesn't have email yet
        this.specialization = t.getSpecialization();
    }
    // Add Getters
    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getSpecialization() { return specialization; }
}