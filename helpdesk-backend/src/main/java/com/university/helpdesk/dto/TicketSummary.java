package com.university.helpdesk.dto;

import java.time.LocalDateTime;
import com.university.helpdesk.entity.model.Priority;
import com.university.helpdesk.entity.model.Status;

public record TicketSummary(Long ticketId,String subject, Priority priority, Status status,
		String submitted, String techName, LocalDateTime created, LocalDateTime resolved ) {

}
