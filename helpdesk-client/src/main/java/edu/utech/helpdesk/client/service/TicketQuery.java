package edu.utech.helpdesk.client.service;

import edu.utech.helpdesk.client.model.Priority;
import edu.utech.helpdesk.client.model.Status;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/** Search criteria for GET /api/tickets. Every field is optional. */
public final class TicketQuery {
    private Long ticketId;
    private Long userId;
    private Long technicianId;
    private String user;
    private String technician;
    private Status status;
    private Priority priority;
    private LocalDate from;
    private LocalDate to;

    public TicketQuery ticketId(Long v) { this.ticketId = v; return this; }
    public TicketQuery userId(Long v) { this.userId = v; return this; }
    public TicketQuery technicianId(Long v) { this.technicianId = v; return this; }
    public TicketQuery user(String v) { this.user = v; return this; }
    public TicketQuery technician(String v) { this.technician = v; return this; }
    public TicketQuery status(Status v) { this.status = v; return this; }
    public TicketQuery priority(Priority v) { this.priority = v; return this; }
    public TicketQuery from(LocalDate v) { this.from = v; return this; }
    public TicketQuery to(LocalDate v) { this.to = v; return this; }

    Map<String, String> toParams() {
        Map<String, String> p = new LinkedHashMap<>();
        put(p, "id", ticketId);
        put(p, "userId", userId);
        put(p, "technicianId", technicianId);
        put(p, "user", user);
        put(p, "technician", technician);
        put(p, "status", status);
        put(p, "priority", priority);
        put(p, "from", from);
        put(p, "to", to);
        return p;
    }

    private static void put(Map<String, String> map, String key, Object value) {
        if (value == null) {
            return;
        }
        String s = value.toString().trim();
        if (!s.isEmpty()) {
            map.put(key, s);
        }
    }
}
