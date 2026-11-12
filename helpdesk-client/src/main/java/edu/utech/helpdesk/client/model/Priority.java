package edu.utech.helpdesk.client.model;

public enum Priority {
    LOW, MEDIUM, HIGH, URGENT;

    public String label() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }
}
