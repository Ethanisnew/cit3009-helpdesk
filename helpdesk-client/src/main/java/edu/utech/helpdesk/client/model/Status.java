package edu.utech.helpdesk.client.model;

/** Ticket life-cycle statuses required by the project brief. */
public enum Status {
    NEW, ASSIGNED, IN_PROGRESS, PENDING, RESOLVED, CLOSED, REOPENED;

    /** A ticket is "open" until it has been resolved or closed. */
    public boolean isOpen() {
        return this != RESOLVED && this != CLOSED;
    }

    public String label() {
        return name().replace('_', ' ');
    }
}
