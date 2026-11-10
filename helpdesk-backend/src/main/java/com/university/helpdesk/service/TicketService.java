package com.university.helpdesk.service;

import com.university.helpdesk.entity.model.Administrator;
import com.university.helpdesk.entity.model.Status;
import com.university.helpdesk.entity.model.Technician;
import com.university.helpdesk.entity.model.Ticket;
import com.university.helpdesk.entity.model.User;
import com.university.helpdesk.repository.AdministratorRepository;
import com.university.helpdesk.repository.TechnicianRepository;
import com.university.helpdesk.repository.TicketRepository;
import com.university.helpdesk.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TechnicianRepository technicianRepository;

    @Autowired
    private AdministratorRepository administratorRepository;

    // ... leave the rest of your methods exactly as they are ...

    public Ticket createTicket(String subject, String description, com.university.helpdesk.entity.model.Priority priority, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Ticket ticket = new Ticket(subject, description, priority, user);
        return ticketRepository.save(ticket);
    }

    public List<Ticket> searchTickets(Long userId, Long technicianId, Status status) {
        if (userId != null) {
            return ticketRepository.findByUser_Id(userId);
        } else if (technicianId != null) {
            return ticketRepository.findByTechnician_Id(technicianId);
        } else if (status != null) {
            return ticketRepository.findByStatus(status);
        }
        return ticketRepository.findAll();
    }

    public Ticket updateStatus(Long ticketId, Status status, Long technicianId) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(() -> new RuntimeException("Ticket not found"));
        ticket.setStatus(status);
        if (technicianId != null) {
            // optionally verify technician exists
        }
        return ticketRepository.save(ticket);
    }

    public Ticket resolveTicket(Long ticketId, String resolutionNotes) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(() -> new RuntimeException("Ticket not found"));
        ticket.resolve(resolutionNotes); // uses the method M3 wrote in the Ticket entity!
        return ticketRepository.save(ticket);
    }

    public Ticket updateStatusSimple(Long ticketId, Status newStatus) {
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(() -> new RuntimeException("Ticket not found"));
        ticket.setStatus(newStatus);
        return ticketRepository.save(ticket);
    }

    public Ticket assignTicket(Long ticketId, Long technicianId, Long adminId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
        Technician tech = technicianRepository.findById(technicianId)
                .orElseThrow(() -> new RuntimeException("Technician not found"));
        Administrator admin = administratorRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        ticket.assign(tech, admin); // Uses M3's entity method!
        return ticketRepository.save(ticket);
    }

    public Ticket closeTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
        ticket.setStatus(com.university.helpdesk.entity.model.Status.CLOSED);
        return ticketRepository.save(ticket);
    }

    public Ticket reopenTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
        ticket.setStatus(com.university.helpdesk.entity.model.Status.REOPENED);
        return ticketRepository.save(ticket);
    }
}