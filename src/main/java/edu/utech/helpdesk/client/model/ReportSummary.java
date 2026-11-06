package edu.utech.helpdesk.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Headline numbers for the Reports tab. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ReportSummary(long ticketsSubmitted, long ticketsResolved, long openTickets, double averageResolutionHours) {
}
