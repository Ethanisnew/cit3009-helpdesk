package edu.utech.helpdesk.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Returned by the login endpoint and the user endpoints.
 * {@code technicianId} is only set when the account is linked to a Technician entity.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record UserDto(Long id, String username, String fullName, String email, Role role, Long technicianId) {
}
