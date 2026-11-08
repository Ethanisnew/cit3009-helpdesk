package com.university.helpdesk.entity.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
public class Comment {
	//attributes
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long commentId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "date_created", nullable = false, updatable = false)
    private LocalDateTime dateCreated;

    // Many Comments -> One Ticket, FK Ticket_ID
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    // Many Comments -> One User (author), FK User_ID
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @PrePersist
    protected void onCreate() {
        this.dateCreated = LocalDateTime.now();
    }
    //constructors
    public Comment() {}

    public Comment(String content, User user) {
        this.content = content;
        this.user = user;
    }
    //GETTERS AND SETTERS
    public Long getCommentId() { return commentId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getDateCreated() { return dateCreated; }
    public Ticket getTicket() { return ticket; }
    public void setTicket(Ticket ticket) { this.ticket = ticket; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}