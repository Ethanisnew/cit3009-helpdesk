package com.university.helpdesk.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.university.helpdesk.entity.model.Status;
import com.university.helpdesk.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.university.helpdesk.dto.AvgResolution;
import com.university.helpdesk.dto.TechnicianWorkload;
import com.university.helpdesk.dto.TicketReport;
import com.university.helpdesk.dto.TicketSummary;
import com.university.helpdesk.service.ReportService;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

	@Autowired
	private ReportService reportService;

	@Autowired
	private TicketRepository ticketRepository;

	@GetMapping("/tickets-submitted")
	public TicketReport ticketsSubmitted(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
										 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
		return reportService.ticketsSubmitted(from, to);
	}

	@GetMapping("/tickets-resolved")
	public TicketReport ticketsResolved(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
										@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
		return reportService.ticketsResolved(from, to);
	}

	@GetMapping("/open-tickets")
	public List<TicketSummary> openTickets(){
		return reportService.openTickets();
	}

	@GetMapping("/average-resolution-time")
	public AvgResolution averageResolutionTime() {
		return reportService.avgResolutionTime();
	}

	// ===== M1 Frontend Compatible Endpoints =====

	@GetMapping("/summary")
	public Map<String, Object> getSummary() {
		var submitted = reportService.ticketsSubmitted(null, null);
		var resolved = reportService.ticketsResolved(null, null);
		var open = reportService.openTickets();
		var avg = reportService.avgResolutionTime();

		return Map.of(
				"ticketsSubmitted", submitted != null ? submitted.count() : 0,
				"ticketsResolved", resolved != null ? resolved.count() : 0,
				"openTickets", open != null ? open.size() : 0,
				"averageResolutionHours", avg != null ? avg.avgHours() : 0.0
		);
	}

	@GetMapping("/technician-workload")
	public List<Map<String, Object>> getTechnicianWorkload() {
		List<TechnicianWorkload> activeWorkload = reportService.technicianWorkload();

		return activeWorkload.stream().map(tw -> {
			long openCount = tw.activeTickets() != null ? tw.activeTickets() : 0;
			long resolvedCount = ticketRepository.findByTechnician_Id(tw.techId()).stream()
					.filter(t -> t.getStatus() == Status.RESOLVED || t.getStatus() == Status.CLOSED)
					.count();

			return Map.<String, Object>of(
					"technicianId", tw.techId(),
					"technicianName", tw.techName() != null ? tw.techName() : "",
					"openTickets", openCount,
					"resolvedTickets", resolvedCount
			);
		}).collect(Collectors.toList());
	}
}