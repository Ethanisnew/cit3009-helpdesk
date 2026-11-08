package com.university.helpdesk.dto;

import com.university.helpdesk.entity.model.Priority;

public class CreateTicketRequest {
    private String subject;
    private String description;
    private Priority priority;
    private Long userId;

    // Getters and Setters
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}