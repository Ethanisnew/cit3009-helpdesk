package edu.utech.helpdesk.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TechnicianDto(Long id, String fullName, String username, String email, String specialization) {
}
