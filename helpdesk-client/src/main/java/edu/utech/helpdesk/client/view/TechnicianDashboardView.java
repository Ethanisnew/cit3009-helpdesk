package edu.utech.helpdesk.client.view;

import edu.utech.helpdesk.client.model.Status;
import edu.utech.helpdesk.client.model.TicketDto;
import edu.utech.helpdesk.client.model.UserDto;
import edu.utech.helpdesk.client.ui.Filters;
import edu.utech.helpdesk.client.ui.TicketTable;
import edu.utech.helpdesk.client.ui.TopBar;
import edu.utech.helpdesk.client.ui.Ui;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** Technician dashboard: assigned tickets on the left, work panel for the selected ticket on the right. */
public final class TechnicianDashboardView {

    public final BorderPane root = new BorderPane();
    public final TopBar topBar;

    // stat tiles
    public final Label assignedStat = new Label("0");
    public final Label inProgressStat = new Label("0");
    public final Label pendingStat = new Label("0");
    public final Label resolvedStat = new Label("0");

    // list side
    public final ComboBox<String> statusFilter = Filters.statusBox();
    public final TextField searchField = new TextField();
    public final CheckBox autoRefresh = new CheckBox("Auto-refresh");
    public final Label lastRefreshed = Ui.muted("Not refreshed yet");
    public final Button refreshButton = new Button("Refresh");
    public final TableView<TicketDto> table = TicketTable.create(true, false);

    // work panel
    public final Label panelTitle = Ui.title("Select a ticket");
    public final Label panelMeta = Ui.muted("Choose a ticket from the list to work on it.");
    public final HBox badgeBox = new HBox(8);
    public final TextArea descriptionArea = new TextArea();
    public final ComboBox<Status> statusBox = new ComboBox<>();
    public final Button updateStatusButton = new Button("Update status");
    public final TextArea notesArea = new TextArea();
    public final Button resolveButton = Ui.primary(new Button("Mark as resolved"));
    public final Button commentsButton = new Button("View / add comments");

    public TechnicianDashboardView(UserDto user) {
        topBar = new TopBar(user);
        root.setTop(topBar);

        SplitPane split = new SplitPane(buildListSide(), buildWorkPanel());
        split.setDividerPositions(0.58);
        root.setCenter(split);
    }

    private VBox buildListSide() {
        HBox stats = new HBox(12,
                Ui.statTile("Assigned to me", assignedStat),
                Ui.statTile("In progress", inProgressStat),
                Ui.statTile("Pending", pendingStat),
                Ui.statTile("Resolved", resolvedStat));

        searchField.setPromptText("Search subject or ticket #");
        autoRefresh.setSelected(true);
        HBox filters = new HBox(10, statusFilter, searchField, Ui.spacer(), autoRefresh, refreshButton);
        filters.setAlignment(Pos.CENTER_LEFT);
        Ui.growH(searchField);

        Ui.growV(table);
        VBox box = new VBox(12, Ui.title("My assigned tickets"), stats, filters, table, lastRefreshed);
        box.setPadding(new Insets(20));
        return box;
    }

    private VBox buildWorkPanel() {
        descriptionArea.setEditable(false);
        descriptionArea.setWrapText(true);
        descriptionArea.setPrefRowCount(5);

        statusBox.getItems().addAll(Status.IN_PROGRESS, Status.PENDING);
        statusBox.setValue(Status.IN_PROGRESS);
        statusBox.setMaxWidth(Double.MAX_VALUE);
        HBox statusRow = new HBox(8, statusBox, updateStatusButton);
        Ui.growH(statusBox);

        notesArea.setPromptText("What was wrong and what did you do to fix it? (required to resolve)");
        notesArea.setWrapText(true);
        notesArea.setPrefRowCount(6);

        VBox card = Ui.card(14,
                panelTitle,
                panelMeta,
                badgeBox,
                Ui.field("Description", descriptionArea),
                Ui.field("Set status", statusRow),
                Ui.field("Resolution notes", notesArea),
                resolveButton,
                commentsButton);
        resolveButton.setMaxWidth(Double.MAX_VALUE);
        commentsButton.setMaxWidth(Double.MAX_VALUE);

        VBox box = new VBox(card);
        box.setPadding(new Insets(20));
        Ui.growV(card);
        return box;
    }
}
