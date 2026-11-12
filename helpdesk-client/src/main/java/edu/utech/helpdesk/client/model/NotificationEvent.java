package edu.utech.helpdesk.client.model;

import java.time.LocalDateTime;

public record NotificationEvent(NotificationType type, Long ticketId, String message, LocalDateTime receivedAt) {
}
