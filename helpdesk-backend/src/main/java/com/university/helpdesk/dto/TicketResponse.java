package com.university.helpdesk.dto;

import com.university.helpdesk.entity.model.Priority;
import com.university.helpdesk.entity.model.Status;
import com.university.helpdesk.entity.model.Ticket;
import java.time.LocalDateTime;

public class TicketResponse {
    private Long id;
    private String subject;
    private String description;
    private LocalDateTime creationDate;
    private Priority priority;
    private Status status;
    private Long userId;
    private String userName;
    private Long technicianId;
    private String technicianName;
    private String resolutionNotes;

    public TicketResponse(Ticket t) {
        this.id = t.getTicketId();
        this.subject = t.getSubject();
        this.description = t.getDescription();
        this.creationDate = t.getCreationDate();
        this.priority = t.getPriority();
        this.status = t.getStatus();
        this.userId = t.getUser().getId();
        this.userName = t.getUser().getName();
        if (t.getTechnician() != null) {
            this.technicianId = t.getTechnician().getId();
            this.technicianName = t.getTechnician().getName();
        }
        this.resolutionNotes = t.getResolutionNotes();
    }

    // Getters
    public Long getId() { return id; }
    public String getSubject() { return subject; }
    public String getDescription() { return description; }
    public LocalDateTime getCreationDate() { return creationDate; }
    public Priority getPriority() { return priority; }
    public Status getStatus() { return status; }
    public Long getUserId() { return userId; }
    public String getUserName() { return userName; }
    public Long getTechnicianId() { return technicianId; }
    public String getTechnicianName() { return technicianName; }
    public String getResolutionNotes() { return resolutionNotes; }
}