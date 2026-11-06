package edu.utech.helpdesk.client.ui;

import edu.utech.helpdesk.client.model.Priority;
import edu.utech.helpdesk.client.model.Status;
import javafx.scene.control.ComboBox;

/** Status / priority drop-downs that start on an "All ..." choice. */
public final class Filters {

    private static final String ALL_STATUSES = "All statuses";
    private static final String ALL_PRIORITIES = "All priorities";

    private Filters() {
    }

    public static ComboBox<String> statusBox() {
        ComboBox<String> box = new ComboBox<>();
        box.getItems().add(ALL_STATUSES);
        for (Status s : Status.values()) {
            box.getItems().add(s.name());
        }
        box.setValue(ALL_STATUSES);
        return box;
    }

    public static ComboBox<String> priorityBox() {
        ComboBox<String> box = new ComboBox<>();
        box.getItems().add(ALL_PRIORITIES);
        for (Priority p : Priority.values()) {
            box.getItems().add(p.name());
        }
        box.setValue(ALL_PRIORITIES);
        return box;
    }

    /** @return the chosen status, or null for "All statuses" */
    public static Status status(ComboBox<String> box) {
        String v = box.getValue();
        return v == null || v.equals(ALL_STATUSES) ? null : Status.valueOf(v);
    }

    /** @return the chosen priority, or null for "All priorities" */
    public static Priority priority(ComboBox<String> box) {
        String v = box.getValue();
        return v == null || v.equals(ALL_PRIORITIES) ? null : Priority.valueOf(v);
    }

    public static void reset(ComboBox<String> box) {
        box.getSelectionModel().selectFirst();
    }
}
