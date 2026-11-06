package edu.utech.helpdesk.client.service;

/** Any failure talking to the REST service, already translated into something a user can read. */
public class ApiException extends Exception {

    public enum Kind { CONNECTION, UNAUTHORIZED, NOT_FOUND, CONFLICT, VALIDATION, SERVER }

    private final Kind kind;
    private final int httpStatus;

    public ApiException(Kind kind, int httpStatus, String message) {
        super(message);
        this.kind = kind;
        this.httpStatus = httpStatus;
    }

    public Kind kind() {
        return kind;
    }

    public int httpStatus() {
        return httpStatus;
    }
}
