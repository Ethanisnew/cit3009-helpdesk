package edu.utech.helpdesk.client.ui;

import edu.utech.helpdesk.client.model.TicketDto;
import edu.utech.helpdesk.client.model.TicketInput;

import java.util.Optional;

public final class TicketEditDialog {

    private TicketEditDialog() {
    }

    public static Optional<TicketInput> show(TicketDto ticket) {
        TicketForm form = new TicketForm();
        form.fill(ticket);
        form.setPrefWidth(480);
        return FormDialog.show("Edit Ticket", "Edit ticket #" + ticket.id(), "Save changes",
                form, form::validate, form::toInput);
    }
}
