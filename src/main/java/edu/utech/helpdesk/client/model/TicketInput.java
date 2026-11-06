package edu.utech.helpdesk.client.model;

/** What the user types into the ticket form (used for both create and edit). */
public record TicketInput(String subject, String description, Priority priority) {
}
