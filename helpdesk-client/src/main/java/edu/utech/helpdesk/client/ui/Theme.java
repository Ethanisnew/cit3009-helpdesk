package edu.utech.helpdesk.client.ui;

public final class Theme {
    private static String css;

    private Theme() {
    }

    public static String css() {
        if (css == null) {
            css = Theme.class.getResource("/css/app.css").toExternalForm();
        }
        return css;
    }
}
