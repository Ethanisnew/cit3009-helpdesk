package edu.utech.helpdesk.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CommentDto(Long id, Long ticketId, Long authorId, String authorName, String content, LocalDateTime createdAt) {
}
