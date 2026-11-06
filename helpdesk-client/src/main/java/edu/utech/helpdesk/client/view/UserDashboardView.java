package edu.utech.helpdesk.client.view;

import edu.utech.helpdesk.client.model.TicketDto;
import edu.utech.helpdesk.client.model.UserDto;
import edu.utech.helpdesk.client.ui.Filters;
import edu.utech.helpdesk.client.ui.TicketForm;
import edu.utech.helpdesk.client.ui.TicketTable;
import edu.utech.helpdesk.client.ui.TopBar;
import edu.utech.helpdesk.client.ui.Ui;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** Student / staff dashboard: my tickets, submit a ticket, account. */
public final class UserDashboardView {

    public final BorderPane root = new BorderPane();
    public final TopBar topBar;
    public final TabPane tabs = new TabPane();
    public final Tab myTicketsTab = new Tab("My Tickets");
    public final Tab newTicketTab = new Tab("New Ticket");
    public final Tab accountTab = new Tab("Account");

    // my tickets
    public final ComboBox<String> statusFilter = Filters.statusBox();
    public final TextField searchField = new TextField();
    public final Button refreshButton = new Button("Refresh");
    public final TableView<TicketDto> table = TicketTable.create(false, true);
    public final Button viewButton = Ui.primary(new Button("View / Comment"));
    public final Button editButton = new Button("Edit");
    public final Button closeButton = Ui.danger(new Button("Close ticket"));
    public final Button reopenButton = new Button("Reopen");
    public final Label countLabel = Ui.muted("");

    // new ticket
    public final TicketForm ticketForm = new TicketForm();
    public final Button submitButton = Ui.primary(new Button("Submit ticket"));
    public final Button clearButton = new Button("Clear");

    // account
    public final PasswordField currentPassword = new PasswordField();
    public final PasswordField newPassword = new PasswordField();
    public final PasswordField confirmPassword = new PasswordField();
    public final Button changePasswordButton = Ui.primary(new Button("Change password"));

    public UserDashboardView(UserDto user) {
        topBar = new TopBar(user);
        root.setTop(topBar);

        myTicketsTab.setContent(buildMyTickets());
        newTicketTab.setContent(buildNewTicket());
        accountTab.setContent(buildAccount(user));
        tabs.getTabs().addAll(myTicketsTab, newTicketTab, accountTab);
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        root.setCenter(tabs);
    }

    private VBox buildMyTickets() {
        searchField.setPromptText("Search by subject or ticket #");
        searchField.setPrefWidth(260);
        HBox filters = new HBox(10, statusFilter, searchField, Ui.spacer(), countLabel, refreshButton);
        filters.setAlignment(Pos.CENTER_LEFT);

        HBox actions = new HBox(10, viewButton, editButton, reopenButton, Ui.spacer(), closeButton);
        actions.setAlignment(Pos.CENTER_LEFT);

        Ui.growV(table);
        VBox box = new VBox(12, Ui.title("My tickets"), filters, table, actions);
        box.setPadding(new Insets(20));
        return box;
    }

    private VBox buildNewTicket() {
        HBox buttons = new HBox(10, submitButton, clearButton);
        VBox card = Ui.card(16,
                Ui.title("Report a problem"),
                Ui.muted("Describe the issue and a technician will be assigned. You will be notified when its status changes."),
                ticketForm,
                buttons);
        card.setMaxWidth(640);
        VBox page = new VBox(card);
        page.setPadding(new Insets(20));
        return page;
    }

    private VBox buildAccount(UserDto user) {
        GridPane info = new GridPane();
        info.setHgap(18);
        info.setVgap(8);
        row(info, 0, "Name", user.fullName());
        row(info, 1, "Username", user.username());
        row(info, 2, "Email", user.email());
        row(info, 3, "Role", user.role() == null ? "" : user.role().label());

        currentPassword.setPromptText("Current password");
        newPassword.setPromptText("At least 6 characters");
        confirmPassword.setPromptText("Repeat the new password");

        VBox profile = Ui.card(12, Ui.title("Your profile"), info);
        profile.setPrefWidth(340);
        VBox password = Ui.card(12,
                Ui.title("Change password"),
                Ui.field("Current password", currentPassword),
                Ui.field("New password", newPassword),
                Ui.field("Confirm new password", confirmPassword),
                changePasswordButton);
        password.setPrefWidth(340);

        HBox cards = new HBox(20, profile, password);
        cards.setAlignment(Pos.TOP_LEFT);
        VBox page = new VBox(cards);
        page.setPadding(new Insets(20));
        return page;
    }

    private static void row(GridPane grid, int r, String label, String value) {
        Label l = new Label(label);
        l.getStyleClass().add("field-label");
        grid.add(l, 0, r);
        grid.add(new Label(value == null ? "" : value), 1, r);
    }
}
