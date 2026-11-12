package com.university.helpdesk.dto;

import com.university.helpdesk.entity.model.Comment;
import java.time.LocalDateTime;

public class CommentResponse {
    private Long id;
    private Long ticketId;
    private Long authorId;
    private String authorName;
    private String content;
    private LocalDateTime createdAt;

    public CommentResponse(Comment c) {
        this.id = c.getCommentId();
        this.ticketId = c.getTicket().getTicketId();
        this.authorId = c.getUser().getId();
        this.authorName = c.getUser().getName();
        this.content = c.getContent();
        this.createdAt = c.getDateCreated();
    }

    // Getters
    public Long getId() { return id; }
    public Long getTicketId() { return ticketId; }
    public Long getAuthorId() { return authorId; }
    public String getAuthorName() { return authorName; }
    public String getContent() { return content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}