package edu.utech.helpdesk.client.ui;

import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.Control;
import javafx.scene.control.TextInputControl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Fluent form validation. Failing controls get the "invalid" style class (red border)
 * which clears itself as soon as the user edits the field.
 *
 * <pre>
 * boolean ok = new FormValidator()
 *         .required(subject, "Subject")
 *         .minLength(subject, "Subject", 5)
 *         .validateOrAlert();
 * </pre>
 */
public final class FormValidator {

    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    private static final String INVALID = "invalid";

    private final List<String> errors = new ArrayList<>();
    private final Set<Control> seen = new HashSet<>();

    public FormValidator required(TextInputControl c, String label) {
        return check(c, !text(c).isBlank(), label + " is required.");
    }

    public FormValidator minLength(TextInputControl c, String label, int min) {
        String t = text(c);
        return check(c, t.isBlank() || t.length() >= min, label + " must be at least " + min + " characters.");
    }

    public FormValidator maxLength(TextInputControl c, String label, int max) {
        return check(c, text(c).length() <= max, label + " must be at most " + max + " characters.");
    }

    public FormValidator email(TextInputControl c, String label) {
        String t = text(c);
        return check(c, t.isBlank() || EMAIL.matcher(t).matches(), label + " must be a valid email address.");
    }

    public FormValidator matches(TextInputControl a, TextInputControl b, String message) {
        String x = a.getText() == null ? "" : a.getText();
        String y = b.getText() == null ? "" : b.getText();
        return check(b, x.equals(y), message);
    }

    public FormValidator selected(ComboBoxBase<?> c, String label) {
        return check(c, c.getValue() != null, "Please choose a " + label.toLowerCase() + ".");
    }

    public FormValidator custom(Control c, boolean ok, String message) {
        return check(c, ok, message);
    }

    public boolean isValid() {
        return errors.isEmpty();
    }

    public String summary() {
        return "• " + String.join("\n• ", errors);
    }

    /** @return true when valid; otherwise shows a warning Alert listing every problem and returns false. */
    public boolean validateOrAlert() {
        if (errors.isEmpty()) {
            return true;
        }
        AlertUtil.warn("Please check the form", summary());
        return false;
    }

    private FormValidator check(Control c, boolean ok, String message) {
        if (seen.add(c)) {
            c.getStyleClass().remove(INVALID);
            installAutoClear(c);
        }
        if (!ok) {
            if (!c.getStyleClass().contains(INVALID)) {
                c.getStyleClass().add(INVALID);
            }
            errors.add(message);
        }
        return this;
    }

    private static void installAutoClear(Control c) {
        if (c.getProperties().putIfAbsent("fv-autoclear", Boolean.TRUE) != null) {
            return;
        }
        if (c instanceof TextInputControl t) {
            t.textProperty().addListener(obs -> c.getStyleClass().remove(INVALID));
        } else if (c instanceof ComboBoxBase<?> cb) {
            cb.valueProperty().addListener(obs -> c.getStyleClass().remove(INVALID));
        }
    }

    private static String text(TextInputControl c) {
        return c.getText() == null ? "" : c.getText().trim();
    }
}
