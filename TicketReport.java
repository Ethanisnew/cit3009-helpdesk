package com.university.helpdesk.dto;

import java.time.LocalDateTime;
import java.util.List;

public record TicketReport(String report, LocalDateTime from, LocalDateTime to, int count, List<TicketSummary> tickets) {

}
