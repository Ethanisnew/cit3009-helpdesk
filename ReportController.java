package com.university.helpdesk.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.university.helpdesk.dto.TicketReport;
import com.university.helpdesk.service.ReportService;
import com.university.helpdesk.dto.*;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
	
	@Autowired 
	private ReportService reportService;
	
	//GET api/reports/tickets-submitted?from=YYYY-MM=DD&to=YYYY-MM-DD
	@GetMapping("/tickets-submitted")
	public TicketReport ticketsSubmitted(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate to) {
		return reportService.ticketsSubmitted(from, to);
	}
	
	//GET api/reports/tickets-resolved?from=YYYY-MM=DD&to=YYYY-MM-DD
	@GetMapping("/tickets-resolved")
	public TicketReport ticketsResolved(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate to) {
		return reportService.ticketsResolved(from, to);
	}
	//GET api/reports/technician-workload
	@GetMapping("/technician-workload")
	public List<TechnicianWorkload> technicianWorkload(){
		return reportService.technicianWorkload();
	}
	
	//GET api/reports/open-tickets
	@GetMapping("/open-tickets")
	public List<TicketSummary> openTickets(){
		return reportService.openTickets();
	}
	//GET api/reports/average-resolution-time
	@GetMapping("/average-resolution-time")
	public AvgResolution averageResolutionTime() {
		return reportService.avgResolutionTime();
	}
	
}
