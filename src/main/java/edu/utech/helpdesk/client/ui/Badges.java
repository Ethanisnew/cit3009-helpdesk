package edu.utech.helpdesk.client.ui;

import javafx.scene.control.Label;

/** Coloured pills for Status and Priority values. Colours live in app.css (.badge-*). */
public final class Badges {

    private Badges() {
    }

    public static Label of(Enum<?> value) {
        Label label = new Label(value == null ? "—" : value.name().replace('_', ' '));
        label.getStyleClass().add("badge");
        if (value != null) {
            label.getStyleClass().add("badge-" + value.name().toLowerCase());
        }
        return label;
    }
}
