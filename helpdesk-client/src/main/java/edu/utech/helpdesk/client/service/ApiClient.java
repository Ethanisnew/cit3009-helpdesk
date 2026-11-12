package edu.utech.helpdesk.client.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import edu.utech.helpdesk.client.model.CommentDto;
import edu.utech.helpdesk.client.model.ReportSummary;
import edu.utech.helpdesk.client.model.Status;
import edu.utech.helpdesk.client.model.TechnicianDto;
import edu.utech.helpdesk.client.model.TicketDto;
import edu.utech.helpdesk.client.model.TicketInput;
import edu.utech.helpdesk.client.model.UserDto;
import edu.utech.helpdesk.client.model.WorkloadRow;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static edu.utech.helpdesk.client.service.ApiException.Kind;

/**
 * The only class that talks to the Spring Boot REST service (JSON over HTTP).
 * Every method blocks, so call it from a background thread (see {@code Async}).
 *
 * Base URL: -Dhelpdesk.api.url=http://host:port   (default http://localhost:8080)
 */
public final class ApiClient {

    private static final System.Logger LOG = System.getLogger(ApiClient.class.getName());
    private static final ApiClient INSTANCE = new ApiClient();

    private static final TypeReference<UserDto> USER = new TypeReference<>() { };
    private static final TypeReference<List<UserDto>> USERS = new TypeReference<>() { };
    private static final TypeReference<TicketDto> TICKET = new TypeReference<>() { };
    private static final TypeReference<List<TicketDto>> TICKETS = new TypeReference<>() { };
    private static final TypeReference<CommentDto> COMMENT = new TypeReference<>() { };
    private static final TypeReference<List<CommentDto>> COMMENTS = new TypeReference<>() { };
    private static final TypeReference<TechnicianDto> TECH = new TypeReference<>() { };
    private static final TypeReference<List<TechnicianDto>> TECHS = new TypeReference<>() { };
    private static final TypeReference<ReportSummary> SUMMARY = new TypeReference<>() { };
    private static final TypeReference<List<WorkloadRow>> WORKLOAD = new TypeReference<>() { };

    public static ApiClient get() {
        return INSTANCE;
    }

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    private final String baseUrl = System.getProperty("helpdesk.api.url", "http://localhost:8081").replaceAll("/+$", "");

    private ApiClient() {
    }

    public String baseUrl() {
        return baseUrl;
    }

    // ------------------------------------------------------------------ users

    public UserDto login(String username, String password) throws ApiException {
        return call("POST", "/api/users/login", body("username", username, "password", password), USER);
    }

    public UserDto register(String fullName, String username, String email, String password) throws ApiException {
        return call("POST", "/api/users/register",
                body("fullName", fullName, "username", username, "email", email, "password", password), USER);
    }

    /** Lets the server write the "User Logout" audit entry. Best effort. */
    public void logout(Long userId) throws ApiException {
        send("POST", "/api/users/" + userId + "/logout", null);
    }

    public List<UserDto> listUsers() throws ApiException {
        return call("GET", "/api/users", null, USERS);
    }

    public UserDto createUser(Map<String, Object> data) throws ApiException {
        return call("POST", "/api/users", data, USER);
    }

    public UserDto updateUser(Long id, Map<String, Object> data) throws ApiException {
        return call("PUT", "/api/users/" + id, data, USER);
    }

    public void deleteUser(Long id) throws ApiException {
        send("DELETE", "/api/users/" + id, null);
    }

    public void changePassword(Long userId, String currentPassword, String newPassword) throws ApiException {
        send("PUT", "/api/users/" + userId + "/password",
                body("currentPassword", currentPassword, "newPassword", newPassword));
    }

    // ---------------------------------------------------------------- tickets

    public List<TicketDto> searchTickets(TicketQuery query) throws ApiException {
        return call("GET", "/api/tickets" + queryString(query.toParams()), null, TICKETS);
    }

    public TicketDto createTicket(TicketInput in, Long userId) throws ApiException {
        return call("POST", "/api/tickets",
                body("subject", in.subject(), "description", in.description(), "priority", in.priority(), "userId", userId),
                TICKET);
    }

    public TicketDto updateTicket(Long id, TicketInput in) throws ApiException {
        return call("PUT", "/api/tickets/" + id,
                body("subject", in.subject(), "description", in.description(), "priority", in.priority()), TICKET);
    }

    public TicketDto updateStatus(Long id, Status status, Long technicianId) throws ApiException {
        return call("PUT", "/api/tickets/" + id + "/status",
                body("status", status, "technicianId", technicianId), TICKET);
    }

    public TicketDto resolveTicket(Long id, String notes, Long technicianId) throws ApiException {
        return call("PUT", "/api/tickets/" + id + "/resolve",
                body("resolutionNotes", notes, "technicianId", technicianId), TICKET);
    }

    public TicketDto closeTicket(Long id) throws ApiException {
        return call("PUT", "/api/tickets/" + id + "/close", null, TICKET);
    }

    public TicketDto reopenTicket(Long id) throws ApiException {
        return call("PUT", "/api/tickets/" + id + "/reopen", null, TICKET);
    }

    /** The server must reject this with 409 if another administrator already assigned the ticket. */
    public TicketDto assignTicket(Long id, Long technicianId, Long adminId) throws ApiException {
        return call("PUT", "/api/tickets/" + id + "/assign",
                body("technicianId", technicianId, "assignedBy", adminId), TICKET);
    }

    public List<CommentDto> getComments(Long ticketId) throws ApiException {
        return call("GET", "/api/tickets/" + ticketId + "/comments", null, COMMENTS);
    }

    public CommentDto addComment(Long ticketId, Long authorId, String content) throws ApiException {
        return call("POST", "/api/tickets/" + ticketId + "/comments",
                body("authorId", authorId, "content", content), COMMENT);
    }

    // ------------------------------------------------------------ technicians

    public List<TechnicianDto> listTechnicians() throws ApiException {
        return call("GET", "/api/technicians", null, TECHS);
    }

    public TechnicianDto createTechnician(Map<String, Object> data) throws ApiException {
        return call("POST", "/api/technicians", data, TECH);
    }

    public TechnicianDto updateTechnician(Long id, Map<String, Object> data) throws ApiException {
        return call("PUT", "/api/technicians/" + id, data, TECH);
    }

    public void deleteTechnician(Long id) throws ApiException {
        send("DELETE", "/api/technicians/" + id, null);
    }

    // ---------------------------------------------------------------- reports

    public ReportSummary reportSummary() throws ApiException {
        return call("GET", "/api/reports/summary", null, SUMMARY);
    }

    public List<WorkloadRow> technicianWorkload() throws ApiException {
        return call("GET", "/api/reports/technician-workload", null, WORKLOAD);
    }

    // --------------------------------------------------------------- plumbing

    private void send(String method, String path, Object body) throws ApiException {
        call(method, path, body, null);
    }

    private <T> T call(String method, String path, Object body, TypeReference<T> type) throws ApiException {
        try {
            HttpRequest.Builder rb = HttpRequest.newBuilder(URI.create(baseUrl + path))
                    .timeout(Duration.ofSeconds(10))
                    .header("Accept", "application/json");
            if (body != null) {
                rb.header("Content-Type", "application/json")
                        .method(method, HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)));
            } else {
                rb.method(method, HttpRequest.BodyPublishers.noBody());
            }
            HttpResponse<String> response = http.send(rb.build(), HttpResponse.BodyHandlers.ofString());
            int code = response.statusCode();
            if (code >= 200 && code < 300) {
                String text = response.body();
                if (type == null || text == null || text.isBlank()) {
                    return null;
                }
                return mapper.readValue(text, type);
            }
            throw toException(code, response.body());
        } catch (ApiException e) {
            LOG.log(System.Logger.Level.WARNING, "{0} {1} failed: {2}", method, path, e.getMessage());
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(Kind.CONNECTION, 0, "The request was interrupted.");
        } catch (JsonProcessingException e) {
            LOG.log(System.Logger.Level.ERROR, "Could not read JSON for " + method + " " + path, e);
            throw new ApiException(Kind.SERVER, 0, "The server sent a response this application could not understand.");
        } catch (IOException e) {
            LOG.log(System.Logger.Level.ERROR, "Cannot reach " + baseUrl, e);
            throw new ApiException(Kind.CONNECTION, 0,
                    "Could not reach the Help Desk server at " + baseUrl + ".\nCheck that the Spring Boot service is running.");
        }
    }

    private ApiException toException(int code, String responseBody) {
        String message = null;
        if (responseBody != null && !responseBody.isBlank()) {
            try {
                JsonNode node = mapper.readTree(responseBody);
                for (String field : new String[] {"message", "error", "detail"}) {
                    if (node.hasNonNull(field) && !node.get(field).asText().isBlank()) {
                        message = node.get(field).asText();
                        break;
                    }
                }
            } catch (IOException ignored) {
                // body was not JSON; fall back to the defaults below
            }
        }
        return switch (code) {
            case 400, 422 -> new ApiException(Kind.VALIDATION, code, message != null ? message : "The server rejected the request.");
            case 401, 403 -> new ApiException(Kind.UNAUTHORIZED, code, message != null ? message : "Invalid username or password.");
            case 404 -> new ApiException(Kind.NOT_FOUND, code, message != null ? message : "The requested record was not found.");
            case 409 -> new ApiException(Kind.CONFLICT, code, message != null ? message : "That record already exists or was changed by someone else.");
            default -> new ApiException(Kind.SERVER, code, message != null ? message : "The server reported an error (HTTP " + code + ").");
        };
    }

    private static String queryString(Map<String, String> params) {
        if (params.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder("?");
        params.forEach((k, v) -> {
            if (sb.length() > 1) {
                sb.append('&');
            }
            sb.append(k).append('=').append(URLEncoder.encode(v, StandardCharsets.UTF_8));
        });
        return sb.toString();
    }

    /** Builds a JSON object from alternating key/value arguments, skipping null values. */
    private static Map<String, Object> body(Object... keyValues) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            if (keyValues[i + 1] != null) {
                map.put((String) keyValues[i], keyValues[i + 1]);
            }
        }
        return map;
    }
}
