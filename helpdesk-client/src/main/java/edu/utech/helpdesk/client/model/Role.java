package edu.utech.helpdesk.client.model;

import com.fasterxml.jackson.annotation.JsonAlias;

/** The three user roles the system supports. */
public enum Role {
    @JsonAlias({"STUDENT", "STAFF"})
    USER,
    TECHNICIAN,
    ADMIN;

    public String label() {
        return switch (this) {
            case USER -> "Student / Staff";
            case TECHNICIAN -> "Technician";
            case ADMIN -> "Administrator";
        };
    }
}
