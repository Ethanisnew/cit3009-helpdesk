package edu.utech.helpdesk.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** One technician's row in the workload report. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record WorkloadRow(Long technicianId, String technicianName, long openTickets, long resolvedTickets) {
}
