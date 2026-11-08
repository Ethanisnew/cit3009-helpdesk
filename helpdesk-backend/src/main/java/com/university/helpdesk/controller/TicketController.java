package com.university.helpdesk.controller;

import com.university.helpdesk.dto.CreateTicketRequest;
import com.university.helpdesk.dto.TicketResponse;
import com.university.helpdesk.entity.model.Status;
import com.university.helpdesk.entity.model.Ticket;
import com.university.helpdesk.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @PostMapping
    public ResponseEntity<TicketResponse> createTicket(@RequestBody CreateTicketRequest request) {
        Ticket ticket = ticketService.createTicket(
                request.getSubject(),
                request.getDescription(),
                request.getPriority(),
                request.getUserId()
        );
        return ResponseEntity.ok(new TicketResponse(ticket));
    }

    @GetMapping
    public ResponseEntity<List<TicketResponse>> searchTickets(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long technicianId,
            @RequestParam(required = false) Status status) {

        List<Ticket> tickets = ticketService.searchTickets(userId, technicianId, status);
        List<TicketResponse> response = tickets.stream()
                .map(TicketResponse::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}