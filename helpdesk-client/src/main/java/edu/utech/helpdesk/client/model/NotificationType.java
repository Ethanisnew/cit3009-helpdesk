package edu.utech.helpdesk.client.model;

/** Notification kinds broadcast by the TCP Notification Server. */
public enum NotificationType {
    TICKET_ASSIGNED("Ticket assigned"),
    TICKET_RESOLVED("Ticket resolved"),
    COMMENT_ADDED("New comment"),
    URGENT_TICKET_CREATED("Urgent ticket"),
    INFO("Info");

    private final String label;

    NotificationType(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static NotificationType parse(String raw) {
        try {
            return valueOf(raw.trim().toUpperCase());
        } catch (Exception e) {
            return INFO;
        }
    }
}
