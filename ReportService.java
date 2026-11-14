package com.university.helpdesk.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.university.helpdesk.dto.*;
import com.university.helpdesk.entity.model.Status;
import com.university.helpdesk.entity.model.Technician;
import com.university.helpdesk.entity.model.Ticket;
import com.university.helpdesk.repository.TechnicianRepository;
import com.university.helpdesk.repository.TicketRepository;

@Service
public class ReportService {
	//List of finished tickers (Closed and resolved"
	private static final List<Status> Finished = List.of(Status.RESOLVED,Status.CLOSED);
	
	//Tickets a technician is working on
	private static final  List<Status> Active = List.of(Status.ASSIGNED, Status.IN_PROGRESS,Status.PENDING);
	
	@Autowired
	private TicketRepository ticketRepository;
	
	@Autowired 
	private TechnicianRepository technicianRepository;
	
	//tickets submitted
	@Transactional (readOnly = true)
	public TicketReport ticketsSubmitted(LocalDate from, LocalDate to) {
		LocalDateTime start = startOf(from);
		LocalDateTime end = endOf(to);
		 List<TicketSummary> tickets = new ArrayList<>();
		 for(Ticket t : ticketRepository.findByCreationDateBetween(start,end)) {
			 tickets.add(toOpenDto(t));
		 }
		 return new TicketReport("TICKETS_SUBMITTED",start,end, tickets.size(),tickets);
	}
	//tickets resolved
	@Transactional (readOnly = true)
	public TicketReport ticketsResolved(LocalDate from, LocalDate to) {
		LocalDateTime start = startOf(from);
		LocalDateTime end = endOf(to);
		 List<TicketSummary> tickets = new ArrayList<>();
		 for(Ticket t : ticketRepository.findByResolutionDateBetween(start,end)) {
			 tickets.add(toOpenDto(t));
		 }
		 return new TicketReport("TICKETS_RESOLVED",start,end, tickets.size(),tickets);
	}
	//open tickets
	@Transactional (readOnly = true)
	public List<TicketSummary> openTickets(){
		List<TicketSummary> tickets = new ArrayList<>();
		for(Ticket t : ticketRepository.findByStatusNotIn(Finished)) {
			tickets.add(toOpenDto(t));
		}
		return tickets;
	}
	
	//technician workload
	
	@Transactional(readOnly = true)
	public List<TechnicianWorkload> technicianWorkload(){
		Map<Long, TechnicianWorkload> byTech = new LinkedHashMap<>();
			
		//Techs who have tickets
		for(Object[] tech : ticketRepository.technicianWorkload(Active)) {
			Long id = (Long) tech[0];
			byTech.put(id,new TechnicianWorkload(id,(String) tech[1],(Long) tech[2]));
		}
			
		//add techs with no tickets to the report
		for(Technician tech : technicianRepository.findByActiveTrue()) {
			byTech.putIfAbsent(tech.getId(), new TechnicianWorkload(tech.getId(),tech.getName(), 0L));
		}
		return new ArrayList<>(byTech.values());
	}
	
	//Average Resolution time
	@Transactional(readOnly = true)
	public AvgResolution avgResolutionTime() {
		Double hours = ticketRepository.averageResolutionHours();
		double value = (hours == null) ? 0.0 : Math.round(hours * 100.0)/100.0;
		return new AvgResolution(value);
	}
	
	//date helpers
    private LocalDateTime startOf(LocalDate from) {
        return (from == null ? LocalDate.of(2000, 1, 1) : from).atStartOfDay();
    }

    private LocalDateTime endOf(LocalDate to) {
        return (to == null ? LocalDate.now() : to).plusDays(1).atStartOfDay().minusNanos(1);
    }
    
    //dto mapper
    
    private TicketSummary toOpenDto(Ticket t) {
    	return new TicketSummary(
			t.getTicketId(),
			t.getSubject(),
			t.getPriority(),
			t.getStatus(),
			t.getUser().getName(),
			t.getTechnician() != null ? t.getTechnician().getName() : null,
			t.getCreationDate(),
			t.getResolutionDate());
    }
}