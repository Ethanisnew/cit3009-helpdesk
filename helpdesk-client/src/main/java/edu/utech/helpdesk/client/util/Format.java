package edu.utech.helpdesk.client.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class Format {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.ENGLISH);

    private Format() {
    }

    public static String dateTime(LocalDateTime t) {
        return t == null ? "—" : DATE_TIME.format(t);
    }

    public static String orDash(String s) {
        return s == null || s.isBlank() ? "—" : s;
    }

    /** Average resolution time, given in hours by the server. */
    public static String duration(double hours) {
        if (hours <= 0) {
            return "—";
        }
        if (hours < 1) {
            return Math.round(hours * 60) + " min";
        }
        if (hours < 48) {
            return String.format(Locale.US, "%.1f h", hours);
        }
        return String.format(Locale.US, "%.1f days", hours / 24);
    }
}
