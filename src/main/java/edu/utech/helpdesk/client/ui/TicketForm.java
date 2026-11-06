package edu.utech.helpdesk.client.ui;

import edu.utech.helpdesk.client.model.Priority;
import edu.utech.helpdesk.client.model.TicketDto;
import edu.utech.helpdesk.client.model.TicketInput;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/** Subject / priority / description form, shared by the "New Ticket" tab and the "Edit Ticket" dialog. */
public final class TicketForm extends VBox {

    private static final int MAX_DESCRIPTION = 1000;

    public final TextField subject = new TextField();
    public final ComboBox<Priority> priority = new ComboBox<>();
    public final TextArea description = new TextArea();
    private final Label counter = new Label("0 / " + MAX_DESCRIPTION);

    public TicketForm() {
        super(14);

        subject.setPromptText("Short summary, e.g. \"Wi-Fi drops in Block C library\"");

        priority.getItems().addAll(Priority.values());
        priority.setConverter(new StringConverter<>() {
            @Override
            public String toString(Priority p) {
                return p == null ? "" : p.label();
            }

            @Override
            public Priority fromString(String s) {
                return null;
            }
        });
        priority.setValue(Priority.MEDIUM);

        description.setPromptText("What is happening? Where? Since when? Include any error messages.");
        description.setWrapText(true);
        description.setPrefRowCount(7);
        description.textProperty().addListener((obs, old, text) ->
                counter.setText(text.length() + " / " + MAX_DESCRIPTION));
        counter.getStyleClass().add("muted");

        HBox descriptionFooter = new HBox(counter);
        descriptionFooter.setAlignment(Pos.CENTER_RIGHT);

        subject.setMaxWidth(Double.MAX_VALUE);
        priority.setMaxWidth(Double.MAX_VALUE);
        getChildren().addAll(
                Ui.field("Subject", subject),
                Ui.field("Priority", priority),
                Ui.field("Description", new VBox(4, description, descriptionFooter)));
    }

    public boolean validate() {
        return new FormValidator()
                .required(subject, "Subject")
                .minLength(subject, "Subject", 5)
                .maxLength(subject, "Subject", 120)
                .required(description, "Description")
                .minLength(description, "Description", 10)
                .maxLength(description, "Description", MAX_DESCRIPTION)
                .selected(priority, "Priority")
                .validateOrAlert();
    }

    public TicketInput toInput() {
        return new TicketInput(subject.getText().trim(), description.getText().trim(), priority.getValue());
    }

    public void fill(TicketDto ticket) {
        subject.setText(ticket.subject());
        description.setText(ticket.description());
        priority.setValue(ticket.priority() == null ? Priority.MEDIUM : ticket.priority());
    }

    public void clear() {
        subject.clear();
        description.clear();
        priority.setValue(Priority.MEDIUM);
    }
}
