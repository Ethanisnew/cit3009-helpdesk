package edu.utech.helpdesk.client.ui;

import edu.utech.helpdesk.client.model.TechnicianDto;
import edu.utech.helpdesk.client.model.TicketDto;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.util.List;
import java.util.Optional;

/** Lets an administrator pick the technician for a ticket. */
public final class AssignDialog {

    private AssignDialog() {
    }

    public static Optional<TechnicianDto> show(TicketDto ticket, List<TechnicianDto> technicians) {
        ComboBox<TechnicianDto> box = new ComboBox<>(FXCollections.observableArrayList(technicians));
        box.setPromptText("Select a technician");
        box.setMaxWidth(Double.MAX_VALUE);
        box.setConverter(new StringConverter<>() {
            @Override
            public String toString(TechnicianDto t) {
                if (t == null) {
                    return "";
                }
                return t.specialization() == null || t.specialization().isBlank()
                        ? t.fullName()
                        : t.fullName() + "  •  " + t.specialization();
            }

            @Override
            public TechnicianDto fromString(String s) {
                return null;
            }
        });
        if (ticket.technicianId() != null) {
            technicians.stream().filter(t -> ticket.technicianId().equals(t.id())).findFirst().ifPresent(box::setValue);
        }

        VBox content = new VBox(10,
                Ui.muted(ticket.subject()),
                Ui.field("Technician", box));
        content.setPrefWidth(380);

        return FormDialog.show("Assign Ticket", "Assign ticket #" + ticket.id(), "Assign", content,
                () -> {
                    if (box.getValue() == null) {
                        AlertUtil.warn("Technician required", "Choose a technician to assign this ticket to.");
                        return false;
                    }
                    return true;
                },
                box::getValue);
    }
}
