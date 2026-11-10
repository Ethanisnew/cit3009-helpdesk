package com.university.helpdesk.controller;

import com.university.helpdesk.dto.CreateTicketRequest;
import com.university.helpdesk.dto.TicketResponse;
import com.university.helpdesk.entity.model.Status;
import com.university.helpdesk.entity.model.Ticket;
import com.university.helpdesk.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
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

    @PutMapping("/{id}/status")
    public ResponseEntity<TicketResponse> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Status status = Status.valueOf(body.get("status"));
        Long techId = body.containsKey("technicianId") ? Long.parseLong(body.get("technicianId")) : null;
        Ticket ticket = ticketService.updateStatus(id, status, techId);
        return ResponseEntity.ok(new TicketResponse(ticket));
    }

    @PutMapping("/{id}/resolve")
    public ResponseEntity<TicketResponse> resolveTicket(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Ticket ticket = ticketService.resolveTicket(id, body.get("resolutionNotes"));
        return ResponseEntity.ok(new TicketResponse(ticket));
    }

    @PutMapping("/{id}/assign")
    public ResponseEntity<TicketResponse> assignTicket(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        Long techId = body.get("technicianId");
        Long adminId = body.get("assignedBy");
        Ticket ticket = ticketService.assignTicket(id, techId, adminId);
        return ResponseEntity.ok(new TicketResponse(ticket));
    }

    @PutMapping("/{id}/close")
    public ResponseEntity<TicketResponse> closeTicket(@PathVariable Long id) {
        return ResponseEntity.ok(new TicketResponse(ticketService.updateStatusSimple(id, Status.CLOSED)));
    }

    @PutMapping("/{id}/reopen")
    public ResponseEntity<TicketResponse> reopenTicket(@PathVariable Long id) {
        return ResponseEntity.ok(new TicketResponse(ticketService.updateStatusSimple(id, Status.REOPENED)));
    }

}