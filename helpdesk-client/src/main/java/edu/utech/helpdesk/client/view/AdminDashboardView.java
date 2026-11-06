package edu.utech.helpdesk.client.view;

import edu.utech.helpdesk.client.model.NotificationEvent;
import edu.utech.helpdesk.client.model.TechnicianDto;
import edu.utech.helpdesk.client.model.TicketDto;
import edu.utech.helpdesk.client.model.UserDto;
import edu.utech.helpdesk.client.model.WorkloadRow;
import edu.utech.helpdesk.client.ui.Filters;
import edu.utech.helpdesk.client.ui.TicketTable;
import edu.utech.helpdesk.client.ui.TopBar;
import edu.utech.helpdesk.client.ui.Ui;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** Administrator dashboard: tickets (search + assign), users, technicians, reports and live activity. */
public final class AdminDashboardView {

    public final BorderPane root = new BorderPane();
    public final TopBar topBar;
    public final TabPane tabs = new TabPane();

    // ---- tickets tab
    public final TextField ticketIdField = new TextField();
    public final TextField userField = new TextField();
    public final TextField technicianField = new TextField();
    public final ComboBox<String> statusBox = Filters.statusBox();
    public final ComboBox<String> priorityBox = Filters.priorityBox();
    public final DatePicker fromDate = new DatePicker();
    public final DatePicker toDate = new DatePicker();
    public final Button searchButton = Ui.primary(new Button("Search"));
    public final Button resetButton = new Button("Reset");
    public final TableView<TicketDto> ticketTable = TicketTable.create(true, true);
    public final Button viewTicketButton = new Button("View / comments");
    public final Button assignButton = Ui.primary(new Button("Assign technician"));
    public final Button closeTicketButton = Ui.danger(new Button("Close ticket"));
    public final Label ticketCount = Ui.muted("");

    // ---- users tab
    public final TableView<UserDto> userTable = new TableView<>();
    public final Button addUserButton = Ui.primary(new Button("Add user"));
    public final Button editUserButton = new Button("Edit / change role");
    public final Button deleteUserButton = Ui.danger(new Button("Delete"));
    public final Button refreshUsersButton = new Button("Refresh");

    // ---- technicians tab
    public final TableView<TechnicianDto> techTable = new TableView<>();
    public final Button addTechButton = Ui.primary(new Button("Add technician"));
    public final Button editTechButton = new Button("Edit");
    public final Button deleteTechButton = Ui.danger(new Button("Remove"));
    public final Button refreshTechsButton = new Button("Refresh");

    // ---- reports tab
    public final Label submittedStat = new Label("—");
    public final Label resolvedStat = new Label("—");
    public final Label openStat = new Label("—");
    public final Label avgStat = new Label("—");
    public final TableView<WorkloadRow> workloadTable = new TableView<>();
    public final BarChart<String, Number> workloadChart = new BarChart<>(new CategoryAxis(), new NumberAxis());
    public final Button refreshReportsButton = Ui.primary(new Button("Refresh reports"));
    public final Button exportButton = new Button("Export workload (CSV)");

    // ---- activity tab
    public final ListView<NotificationEvent> activityList = new ListView<>();
    public final Button clearActivityButton = new Button("Clear");

    public AdminDashboardView(UserDto user) {
        topBar = new TopBar(user);
        root.setTop(topBar);

        Tab tickets = new Tab("Tickets", buildTickets());
        Tab users = new Tab("Users", buildUsers());
        Tab technicians = new Tab("Technicians", buildTechnicians());
        Tab reports = new Tab("Reports", buildReports());
        Tab activity = new Tab("Activity", buildActivity());
        tabs.getTabs().addAll(tickets, users, technicians, reports, activity);
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        root.setCenter(tabs);
    }

    // ------------------------------------------------------------------ tabs

    private VBox buildTickets() {
        ticketIdField.setPromptText("e.g. 42");
        userField.setPromptText("Requester name");
        technicianField.setPromptText("Technician name");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        for (int i = 0; i < 4; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(25);
            grid.getColumnConstraints().add(cc);
        }
        HBox buttons = new HBox(8, searchButton, resetButton);
        buttons.setAlignment(Pos.BOTTOM_LEFT);

        grid.add(Ui.field("Ticket ID", ticketIdField), 0, 0);
        grid.add(Ui.field("User", userField), 1, 0);
        grid.add(Ui.field("Technician", technicianField), 2, 0);
        grid.add(Ui.field("Status", statusBox), 3, 0);
        grid.add(Ui.field("Priority", priorityBox), 0, 1);
        grid.add(Ui.field("Created from", fromDate), 1, 1);
        grid.add(Ui.field("Created to", toDate), 2, 1);
        grid.add(buttons, 3, 1);
        for (javafx.scene.control.Control c : new javafx.scene.control.Control[] {
                ticketIdField, userField, technicianField, statusBox, priorityBox, fromDate, toDate}) {
            c.setMaxWidth(Double.MAX_VALUE);
        }

        HBox actions = new HBox(10, viewTicketButton, assignButton, Ui.spacer(), ticketCount, closeTicketButton);
        actions.setAlignment(Pos.CENTER_LEFT);

        Ui.growV(ticketTable);
        VBox page = new VBox(12, Ui.title("All tickets"), Ui.card(0, grid), ticketTable, actions);
        page.setPadding(new Insets(20));
        return page;
    }

    private VBox buildUsers() {
        userTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        userTable.setPlaceholder(new Label("No users to display"));
        userTable.getColumns().add(Ui.numberColumn("ID", (UserDto u) -> u.id(), 60));
        userTable.getColumns().add(Ui.column("Username", UserDto::username, 140));
        userTable.getColumns().add(Ui.column("Full name", UserDto::fullName, 200));
        userTable.getColumns().add(Ui.column("Email", UserDto::email, 220));
        userTable.getColumns().add(Ui.column("Role", (UserDto u) -> u.role() == null ? "" : u.role().label(), 130));

        HBox actions = new HBox(10, addUserButton, editUserButton, Ui.spacer(), refreshUsersButton, deleteUserButton);
        Ui.growV(userTable);
        VBox page = new VBox(12, Ui.title("User accounts"), userTable, actions);
        page.setPadding(new Insets(20));
        return page;
    }

    private VBox buildTechnicians() {
        techTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        techTable.setPlaceholder(new Label("No technicians yet — add one to start assigning tickets"));
        techTable.getColumns().add(Ui.numberColumn("ID", (TechnicianDto t) -> t.id(), 60));
        techTable.getColumns().add(Ui.column("Full name", TechnicianDto::fullName, 200));
        techTable.getColumns().add(Ui.column("Username", TechnicianDto::username, 140));
        techTable.getColumns().add(Ui.column("Email", TechnicianDto::email, 220));
        techTable.getColumns().add(Ui.column("Specialization", TechnicianDto::specialization, 160));

        HBox actions = new HBox(10, addTechButton, editTechButton, Ui.spacer(), refreshTechsButton, deleteTechButton);
        Ui.growV(techTable);
        VBox page = new VBox(12, Ui.title("Technicians"), techTable, actions);
        page.setPadding(new Insets(20));
        return page;
    }

    private VBox buildReports() {
        HBox tiles = new HBox(12,
                Ui.statTile("Tickets submitted", submittedStat),
                Ui.statTile("Tickets resolved", resolvedStat),
                Ui.statTile("Open tickets", openStat),
                Ui.statTile("Average resolution time", avgStat));

        workloadTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        workloadTable.setPlaceholder(new Label("No workload data"));
        workloadTable.getColumns().add(Ui.column("Technician", WorkloadRow::technicianName, 180));
        workloadTable.getColumns().add(Ui.numberColumn("Open", (WorkloadRow r) -> r.openTickets(), 80));
        workloadTable.getColumns().add(Ui.numberColumn("Resolved", (WorkloadRow r) -> r.resolvedTickets(), 90));
        workloadTable.setPrefWidth(400);
        workloadTable.setMinWidth(340);

        workloadChart.setTitle("Technician workload");
        workloadChart.setAnimated(false);
        workloadChart.getYAxis().setLabel("Tickets");
        workloadChart.setMinHeight(280);

        HBox lower = new HBox(16, workloadTable, workloadChart);
        Ui.growH(workloadChart);
        Ui.growV(lower);

        HBox header = new HBox(10, Ui.title("Reports"), Ui.spacer(), exportButton, refreshReportsButton);
        header.setAlignment(Pos.CENTER_LEFT);
        VBox page = new VBox(14, header, tiles, lower);
        page.setPadding(new Insets(20));
        return page;
    }

    private VBox buildActivity() {
        activityList.setPlaceholder(new Label("No activity yet — events from the Notification Server appear here live"));
        activityList.setCellFactory(lv -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(NotificationEvent e, boolean empty) {
                super.updateItem(e, empty);
                if (empty || e == null) {
                    setText(null);
                    return;
                }
                setText(edu.utech.helpdesk.client.util.Format.dateTime(e.receivedAt()) + "   "
                        + e.type().label() + (e.ticketId() != null ? "  #" + e.ticketId() : "")
                        + "   —   " + e.message());
            }
        });
        HBox header = new HBox(10, Ui.title("System activity"), Ui.spacer(), clearActivityButton);
        header.setAlignment(Pos.CENTER_LEFT);
        Ui.growV(activityList);
        VBox page = new VBox(12, header,
                Ui.muted("Live feed of ticket assignments, resolutions, comments and urgent tickets broadcast by the Notification Server."),
                activityList);
        page.setPadding(new Insets(20));
        return page;
    }
}
