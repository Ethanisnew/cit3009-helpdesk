package com.helpdesk.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tickets")
public class Ticket {
	//attributes
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_id")
    private Long ticketId;

    @Column(nullable = false, length = 150)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "creation_date", nullable = false, updatable = false)
    private LocalDateTime creationDate;

    @Column(name = "resolution_date")
    private LocalDateTime resolutionDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Priority priority = Priority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.NEW;

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    // Optimistic locking: stops two admins assigning the same ticket at once
    @Version
    private Long version;
    
    //Cardinality
    // Many Tickets -> One User (submitter), FK User_ID
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Many Tickets -> One Technician, FK Tech_ID 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tech_id")
    private Technician technician;

    // Many Tickets -> One Administrator, FK Admin_ID 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private Administrator admin;

    // One Ticket -> Many Comments
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.creationDate = LocalDateTime.now();
    }
    //Constructors
    public Ticket() {}

    public Ticket(String subject, String description, Priority priority, User user) {
        this.subject = subject;
        this.description = description;
        this.priority = priority;
        this.user = user;
    }
    
    //Adding comments
    public void addComment(Comment comment) {
        comments.add(comment);
        comment.setTicket(this);
    }
    //Assign tech
    public void assign(Technician technician, Administrator admin) {
        this.technician = technician;
        this.admin = admin;
        this.status = Status.ASSIGNED;
    }
    //Tech notes, switches status to resoved when adding them
    public void resolve(String notes) {
        this.resolutionNotes = notes;
        this.status = Status.RESOLVED;
        this.resolutionDate = LocalDateTime.now();
    }
    //setters and getters
    public Long getTicketId() { return ticketId; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getCreationDate() { return creationDate; }
    public LocalDateTime getResolutionDate() { return resolutionDate; }
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }
    public Long getVersion() { return version; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Technician getTechnician() { return technician; }
    public Administrator getAdmin() { return admin; }
    public List<Comment> getComments() { return comments; }
}