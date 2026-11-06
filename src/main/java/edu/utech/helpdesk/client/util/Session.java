package edu.utech.helpdesk.client.util;

import edu.utech.helpdesk.client.model.UserDto;

/** Holds the signed-in user for the lifetime of a login. */
public final class Session {
    private static UserDto user;

    private Session() {
    }

    public static void set(UserDto u) {
        user = u;
    }

    public static UserDto user() {
        return user;
    }

    public static void clear() {
        user = null;
    }
}
