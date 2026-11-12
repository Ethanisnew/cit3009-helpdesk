package com.university.helpdesk.service;

import com.university.helpdesk.entity.model.*;
import com.university.helpdesk.network.NotificationServer;
import com.university.helpdesk.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TicketService {
    private static final Logger logger = LoggerFactory.getLogger(TicketService.class);

    @Autowired private CommentRepository commentRepository;
    @Autowired private TicketRepository ticketRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private TechnicianRepository technicianRepository;
    @Autowired private AdministratorRepository administratorRepository;
    @Autowired private NotificationServer notificationServer;

    public Ticket createTicket(String subject, String description, Priority priority, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Ticket ticket = new Ticket(subject, description, priority, user);
        logger.info("Creating new ticket: {}", subject);
        return ticketRepository.save(ticket);
    }

    public List<Ticket> searchTickets(Long userId, Long technicianId, Status status) {
        if (userId != null) return ticketRepository.findByUser_Id(userId);
        if (technicianId != null) return ticketRepository.findByTechnician_Id(technicianId);
        if (status != null) return ticketRepository.findByStatus(status);
        return ticketRepository.findAll();
    }

    public Ticket updateStatus(Long ticketId, Status status, Long technicianId) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(() -> new RuntimeException("Ticket not found"));
        ticket.setStatus(status);
        logger.info("Updating status for ticket {} to {}", ticketId, status);
        return ticketRepository.save(ticket);
    }

    public Ticket resolveTicket(Long ticketId, String resolutionNotes) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(() -> new RuntimeException("Ticket not found"));
        ticket.resolve(resolutionNotes);
        logger.info("Ticket {} resolved", ticketId);
        notificationServer.broadcast("TICKET_RESOLVED", ticketId, "Your ticket has been resolved.");
        return ticketRepository.save(ticket);
    }

    public Ticket updateStatusSimple(Long ticketId, Status newStatus) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(() -> new RuntimeException("Ticket not found"));
        ticket.setStatus(newStatus);
        logger.info("Ticket {} status changed to {}", ticketId, newStatus);
        return ticketRepository.save(ticket);
    }

    public Ticket assignTicket(Long ticketId, Long technicianId, Long adminId) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(() -> new RuntimeException("Ticket not found"));
        Technician tech = technicianRepository.findById(technicianId).orElseThrow(() -> new RuntimeException("Technician not found"));
        Administrator admin = administratorRepository.findById(adminId).orElseThrow(() -> new RuntimeException("Admin not found"));

        ticket.assign(tech, admin);
        logger.info("Ticket {} assigned to tech {}", ticketId, technicianId);
        notificationServer.broadcast("TICKET_ASSIGNED", ticketId, "Ticket assigned to " + tech.getName());
        return ticketRepository.save(ticket);
    }

    public List<Comment> getComments(Long ticketId) {
        return commentRepository.findByTicket_TicketIdOrderByDateCreatedAsc(ticketId);
    }

    public Comment addComment(Long ticketId, Long authorId, String content) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(() -> new RuntimeException("Ticket not found"));
        User user = userRepository.findById(authorId).orElseThrow(() -> new RuntimeException("User not found"));

        Comment comment = new Comment(content, user);
        comment.setTicket(ticket);
        logger.info("New comment added to ticket {} by {}", ticketId, user.getName());
        notificationServer.broadcast("COMMENT_ADDED", ticketId, user.getName() + " added a comment.");
        return commentRepository.save(comment);
    }
}