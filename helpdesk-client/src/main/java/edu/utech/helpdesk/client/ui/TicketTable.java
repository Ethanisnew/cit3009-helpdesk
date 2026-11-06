package edu.utech.helpdesk.client.ui;

import edu.utech.helpdesk.client.model.TicketDto;
import edu.utech.helpdesk.client.util.Format;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

/** Builds the TableView used by all three dashboards. */
public final class TicketTable {

    private TicketTable() {
    }

    public static TableView<TicketDto> create(boolean showRequester, boolean showTechnician) {
        TableView<TicketDto> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(new Label("No tickets to display"));

        TableColumn<TicketDto, Number> id = Ui.numberColumn("ID", t -> t.id(), 60);
        TableColumn<TicketDto, String> subject = Ui.column("Subject", TicketDto::subject, 260);
        table.getColumns().add(id);
        table.getColumns().add(subject);
        table.getColumns().add(badgeColumn("Priority", TicketDto::priority, 90));
        table.getColumns().add(badgeColumn("Status", TicketDto::status, 110));
        if (showRequester) {
            table.getColumns().add(Ui.column("Requester", TicketDto::userName, 130));
        }
        if (showTechnician) {
            table.getColumns().add(Ui.column("Technician",
                    t -> t.technicianName() == null || t.technicianName().isBlank() ? "Unassigned" : t.technicianName(), 130));
        }
        table.getColumns().add(dateColumn("Created", TicketDto::creationDate, 140));
        return table;
    }

    /** Replaces the rows but keeps the same ticket selected (matched by ID), so refreshes don't lose the user's place. */
    public static void replaceKeepingSelection(ObservableList<TicketDto> source, TableView<TicketDto> table, List<TicketDto> fresh) {
        TicketDto selected = table.getSelectionModel().getSelectedItem();
        Long selectedId = selected == null ? null : selected.id();
        source.setAll(fresh);
        if (selectedId != null) {
            for (TicketDto t : table.getItems()) {
                if (selectedId.equals(t.id())) {
                    table.getSelectionModel().select(t);
                    break;
                }
            }
        }
    }

    private static <E extends Enum<E>> TableColumn<TicketDto, E> badgeColumn(String title, Function<TicketDto, E> getter, double width) {
        TableColumn<TicketDto, E> col = new TableColumn<>(title);
        col.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(getter.apply(cd.getValue())));
        col.setPrefWidth(width);
        col.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(E item, boolean empty) {
                super.updateItem(item, empty);
                setText(null);
                setGraphic(empty || item == null ? null : Badges.of(item));
            }
        });
        return col;
    }

    private static TableColumn<TicketDto, LocalDateTime> dateColumn(String title, Function<TicketDto, LocalDateTime> getter, double width) {
        TableColumn<TicketDto, LocalDateTime> col = new TableColumn<>(title);
        col.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(getter.apply(cd.getValue())));
        col.setPrefWidth(width);
        col.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : Format.dateTime(item));
            }
        });
        return col;
    }
}
