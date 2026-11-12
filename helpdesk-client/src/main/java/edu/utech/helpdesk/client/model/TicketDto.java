package edu.utech.helpdesk.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

/** A ticket as sent by the REST service. Dates are ISO-8601 strings (e.g. 2026-11-02T09:30:00). */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TicketDto(
        Long id,
        String subject,
        String description,
        LocalDateTime creationDate,
        Priority priority,
        Status status,
        Long userId,
        String userName,
        Long technicianId,
        String technicianName,
        String resolutionNotes) {
}
