package edu.utech.helpdesk.client.ui;

import edu.utech.helpdesk.client.service.ApiException;
import edu.utech.helpdesk.client.util.Navigator;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Region;

/** One place for every JavaFX Alert the application shows. Call on the JavaFX thread. */
public final class AlertUtil {

    private AlertUtil() {
    }

    private static Alert build(Alert.AlertType type, String header, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("University Help Desk");
        alert.setHeaderText(header);
        alert.setContentText(message);
        if (Navigator.stage() != null) {
            alert.initOwner(Navigator.stage());
        }
        alert.getDialogPane().getStylesheets().add(Theme.css());
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        return alert;
    }

    public static void info(String header, String message) {
        build(Alert.AlertType.INFORMATION, header, message).showAndWait();
    }

    public static void warn(String header, String message) {
        build(Alert.AlertType.WARNING, header, message).showAndWait();
    }

    public static void error(String header, String message) {
        build(Alert.AlertType.ERROR, header, message).showAndWait();
    }

    public static boolean confirm(String header, String message) {
        return build(Alert.AlertType.CONFIRMATION, header, message)
                .showAndWait()
                .filter(button -> button == ButtonType.OK)
                .isPresent();
    }

    public static void showError(Throwable t) {
        showError(t, null);
    }

    /**
     * Turns an exception into a readable alert. Connection problems always say
     * "Unable to Connect to Server"; for other REST errors {@code title} (if given) replaces the default heading.
     */
    public static void showError(Throwable t, String title) {
        if (t instanceof ApiException api) {
            String heading = api.kind() == ApiException.Kind.CONNECTION
                    ? "Unable to Connect to Server"
                    : (title != null ? title : defaultTitle(api.kind()));
            error(heading, api.getMessage());
        } else {
            String message = t == null ? "Unknown error." : (t.getMessage() != null ? t.getMessage() : t.getClass().getSimpleName());
            error(title != null ? title : "Unexpected Error", message);
        }
    }

    private static String defaultTitle(ApiException.Kind kind) {
        return switch (kind) {
            case UNAUTHORIZED -> "Invalid Login Credentials";
            case NOT_FOUND -> "Not Found";
            case CONFLICT -> "Duplicate or Conflicting Record";
            case VALIDATION -> "Invalid Request";
            default -> "Server Error";
        };
    }
}
