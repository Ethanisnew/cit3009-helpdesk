package edu.utech.helpdesk.client.controller;

import edu.utech.helpdesk.client.model.NotificationEvent;
import edu.utech.helpdesk.client.model.NotificationType;
import edu.utech.helpdesk.client.model.ReportSummary;
import edu.utech.helpdesk.client.model.Status;
import edu.utech.helpdesk.client.model.TechnicianDto;
import edu.utech.helpdesk.client.model.TicketDto;
import edu.utech.helpdesk.client.model.UserDto;
import edu.utech.helpdesk.client.model.WorkloadRow;
import edu.utech.helpdesk.client.notify.NotificationHub;
import edu.utech.helpdesk.client.service.ApiClient;
import edu.utech.helpdesk.client.service.TicketQuery;
import edu.utech.helpdesk.client.ui.AccountFormDialog;
import edu.utech.helpdesk.client.ui.AlertUtil;
import edu.utech.helpdesk.client.ui.AssignDialog;
import edu.utech.helpdesk.client.ui.Filters;
import edu.utech.helpdesk.client.ui.FormValidator;
import edu.utech.helpdesk.client.ui.TicketDetailDialog;
import edu.utech.helpdesk.client.ui.TicketTable;
import edu.utech.helpdesk.client.ui.Ui;
import edu.utech.helpdesk.client.util.Async;
import edu.utech.helpdesk.client.util.Disposable;
import edu.utech.helpdesk.client.util.Format;
import edu.utech.helpdesk.client.util.Navigator;
import edu.utech.helpdesk.client.view.AdminDashboardView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableRow;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;

/**
 * Administrator dashboard logic: search and assign tickets, manage users and technicians,
 * produce reports and watch live system activity.
 */
public final class AdminDashboardController implements Disposable {

    private final AdminDashboardView v;
    private final UserDto admin;
    private final ApiClient api = ApiClient.get();

    private final ObservableList<TicketDto> tickets = FXCollections.observableArrayList();
    private final ObservableList<UserDto> users = FXCollections.observableArrayList();
    private final ObservableList<TechnicianDto> technicians = FXCollections.observableArrayList();
    private final ObservableList<WorkloadRow> workload = FXCollections.observableArrayList();
    private final Runnable unsubscribe;

    public AdminDashboardController(AdminDashboardView view, UserDto admin) {
        this.v = view;
        this.admin = admin;

        v.topBar.setOnLogout(Navigator::logout);

        Ui.bindSorted(v.ticketTable, tickets);
        Ui.bindSorted(v.userTable, users);
        Ui.bindSorted(v.techTable, technicians);
        v.workloadTable.setItems(workload);
        v.activityList.setItems(NotificationHub.get().history());

        wireTickets();
        wireUsers();
        wireTechnicians();
        wireReports();
        v.clearActivityButton.setOnAction(e -> NotificationHub.get().clear());

        unsubscribe = NotificationHub.get().subscribe(this::onNotification);

        searchTickets(false);
        loadTechnicians(true);
        loadUsers(true);
        loadReports(true);
    }

    @Override
    public void dispose() {
        unsubscribe.run();
        v.topBar.dispose();
    }

    private void onNotification(NotificationEvent event) {
        // keep the ticket list and reports current when something happens elsewhere
        if (event.type() != NotificationType.INFO) {
            searchTickets(true);
        }
    }

    // ================================================================ tickets

    private void wireTickets() {
        v.searchButton.setOnAction(e -> searchTickets(false));
        v.resetButton.setOnAction(e -> {
            v.ticketIdField.clear();
            v.userField.clear();
            v.technicianField.clear();
            Filters.reset(v.statusBox);
            Filters.reset(v.priorityBox);
            v.fromDate.setValue(null);
            v.toDate.setValue(null);
            searchTickets(false);
        });
        v.ticketIdField.setOnAction(e -> searchTickets(false));
        v.userField.setOnAction(e -> searchTickets(false));
        v.technicianField.setOnAction(e -> searchTickets(false));

        v.ticketTable.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> updateTicketButtons());
        v.ticketTable.setRowFactory(tv -> {
            TableRow<TicketDto> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) {
                    viewTicket();
                }
            });
            return row;
        });
        v.viewTicketButton.setOnAction(e -> viewTicket());
        v.assignButton.setOnAction(e -> assignTicket());
        v.closeTicketButton.setOnAction(e -> closeTicket());
        updateTicketButtons();
    }

    private void updateTicketButtons() {
        TicketDto t = v.ticketTable.getSelectionModel().getSelectedItem();
        boolean none = t == null;
        v.viewTicketButton.setDisable(none);
        v.assignButton.setDisable(none || t.status() == Status.CLOSED || t.status() == Status.RESOLVED);
        v.closeTicketButton.setDisable(none || t.status() == Status.CLOSED);
    }

    private void searchTickets(boolean quiet) {
        TicketQuery q = new TicketQuery();

        String idText = text(v.ticketIdField).replace("#", "");
        if (!idText.isEmpty()) {
            try {
                q.ticketId(Long.parseLong(idText));
            } catch (NumberFormatException e) {
                new FormValidator().custom(v.ticketIdField, false, "Ticket ID must be a whole number.").validateOrAlert();
                return;
            }
        }
        LocalDate from = v.fromDate.getValue();
        LocalDate to = v.toDate.getValue();
        if (from != null && to != null && from.isAfter(to)) {
            new FormValidator().custom(v.fromDate, false, "'Created from' must be on or before 'Created to'.").validateOrAlert();
            return;
        }
        q.user(text(v.userField))
                .technician(text(v.technicianField))
                .status(Filters.status(v.statusBox))
                .priority(Filters.priority(v.priorityBox))
                .from(from)
                .to(to);

        v.searchButton.setDisable(true);
        Async.run(() -> api.searchTickets(q),
                list -> {
                    v.searchButton.setDisable(false);
                    TicketTable.replaceKeepingSelection(tickets, v.ticketTable, list);
                    v.ticketCount.setText(list.size() + " ticket" + (list.size() == 1 ? "" : "s"));
                },
                err -> {
                    v.searchButton.setDisable(false);
                    if (!quiet) {
                        AlertUtil.showError(err);
                    }
                });
    }

    private void viewTicket() {
        TicketDto t = v.ticketTable.getSelectionModel().getSelectedItem();
        if (t != null) {
            TicketDetailDialog.show(t);
        }
    }

    /**
     * Concurrent assignment (multithreading requirement 2) is enforced by the server. If another
     * administrator got there first the server answers 409 and we reload so this admin sees the result.
     */
    private void assignTicket() {
        TicketDto t = v.ticketTable.getSelectionModel().getSelectedItem();
        if (t == null) {
            return;
        }
        if (technicians.isEmpty()) {
            AlertUtil.warn("No technicians available", "Add a technician on the Technicians tab before assigning tickets.");
            return;
        }
        AssignDialog.show(t, technicians).ifPresent(tech -> {
            v.assignButton.setDisable(true);
            Async.run(() -> api.assignTicket(t.id(), tech.id(), admin.id()),
                    saved -> {
                        AlertUtil.info("Ticket assigned", "Ticket #" + t.id() + " is now assigned to " + tech.fullName() + ".");
                        searchTickets(true);
                        loadReports(true);
                    },
                    err -> {
                        AlertUtil.showError(err, "Assignment Failed");
                        searchTickets(true);
                    });
        });
    }

    private void closeTicket() {
        TicketDto t = v.ticketTable.getSelectionModel().getSelectedItem();
        if (t == null) {
            return;
        }
        if (AlertUtil.confirm("Close ticket #" + t.id() + "?", "The requester can still reopen it later.")) {
            Async.run(() -> api.closeTicket(t.id()),
                    saved -> {
                        searchTickets(true);
                        loadReports(true);
                    },
                    err -> AlertUtil.showError(err, "Ticket Not Closed"));
        }
    }

    // ================================================================== users

    private void wireUsers() {
        v.userTable.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> updateUserButtons());
        v.refreshUsersButton.setOnAction(e -> loadUsers(false));
        v.addUserButton.setOnAction(e -> addUser());
        v.editUserButton.setOnAction(e -> editUser());
        v.deleteUserButton.setOnAction(e -> deleteUser());
        updateUserButtons();
    }

    private void updateUserButtons() {
        boolean none = v.userTable.getSelectionModel().getSelectedItem() == null;
        v.editUserButton.setDisable(none);
        v.deleteUserButton.setDisable(none);
    }

    private void loadUsers(boolean quiet) {
        Async.run(() -> api.listUsers(),
                list -> users.setAll(list),
                err -> {
                    if (!quiet) {
                        AlertUtil.showError(err);
                    }
                });
    }

    private void addUser() {
        AccountFormDialog.showUser(null).ifPresent(data ->
                Async.run(() -> api.createUser(data),
                        created -> loadUsers(true),
                        err -> AlertUtil.showError(err,
                                err instanceof edu.utech.helpdesk.client.service.ApiException ae
                                        && ae.kind() == edu.utech.helpdesk.client.service.ApiException.Kind.CONFLICT
                                        ? "Duplicate User Account" : "User Not Created")));
    }

    private void editUser() {
        UserDto selected = v.userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        AccountFormDialog.showUser(selected).ifPresent(data ->
                Async.run(() -> api.updateUser(selected.id(), data),
                        saved -> loadUsers(true),
                        err -> AlertUtil.showError(err, "User Not Updated")));
    }

    private void deleteUser() {
        UserDto selected = v.userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        if (selected.id() != null && selected.id().equals(admin.id())) {
            AlertUtil.warn("Cannot delete yourself", "You are signed in as this account.");
            return;
        }
        if (AlertUtil.confirm("Delete user \"" + selected.username() + "\"?", "This cannot be undone.")) {
            Async.run(() -> {
                        api.deleteUser(selected.id());
                        return null;
                    },
                    ok -> loadUsers(true),
                    err -> AlertUtil.showError(err, "User Not Deleted"));
        }
    }

    // ========================================================== technicians

    private void wireTechnicians() {
        v.techTable.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> updateTechButtons());
        v.refreshTechsButton.setOnAction(e -> loadTechnicians(false));
        v.addTechButton.setOnAction(e -> addTechnician());
        v.editTechButton.setOnAction(e -> editTechnician());
        v.deleteTechButton.setOnAction(e -> deleteTechnician());
        updateTechButtons();
    }

    private void updateTechButtons() {
        boolean none = v.techTable.getSelectionModel().getSelectedItem() == null;
        v.editTechButton.setDisable(none);
        v.deleteTechButton.setDisable(none);
    }

    private void loadTechnicians(boolean quiet) {
        Async.run(() -> api.listTechnicians(),
                list -> technicians.setAll(list),
                err -> {
                    if (!quiet) {
                        AlertUtil.showError(err);
                    }
                });
    }

    private void addTechnician() {
        AccountFormDialog.showTechnician(null).ifPresent(data ->
                Async.run(() -> api.createTechnician(data),
                        created -> loadTechnicians(true),
                        err -> AlertUtil.showError(err,
                                err instanceof edu.utech.helpdesk.client.service.ApiException ae
                                        && ae.kind() == edu.utech.helpdesk.client.service.ApiException.Kind.CONFLICT
                                        ? "Duplicate User Account" : "Technician Not Added")));
    }

    private void editTechnician() {
        TechnicianDto selected = v.techTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        AccountFormDialog.showTechnician(selected).ifPresent(data ->
                Async.run(() -> api.updateTechnician(selected.id(), data),
                        saved -> loadTechnicians(true),
                        err -> AlertUtil.showError(err, "Technician Not Updated")));
    }

    private void deleteTechnician() {
        TechnicianDto selected = v.techTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        if (AlertUtil.confirm("Remove technician \"" + selected.fullName() + "\"?",
                "Tickets already assigned to them will need to be reassigned.")) {
            Async.run(() -> {
                        api.deleteTechnician(selected.id());
                        return null;
                    },
                    ok -> loadTechnicians(true),
                    err -> AlertUtil.showError(err, "Technician Not Removed"));
        }
    }

    // ================================================================ reports

    private void wireReports() {
        v.refreshReportsButton.setOnAction(e -> loadReports(false));
        v.exportButton.setOnAction(e -> exportWorkload());
    }

    private void loadReports(boolean quiet) {
        Async.run(() -> api.reportSummary(),
                this::showSummary,
                err -> {
                    if (!quiet) {
                        AlertUtil.showError(err);
                    }
                });
        Async.run(() -> api.technicianWorkload(),
                this::showWorkload,
                err -> {
                    // the summary request already reported a connection problem; stay quiet here
                });
    }

    private void showSummary(ReportSummary s) {
        if (s == null) {
            return;
        }
        v.submittedStat.setText(String.valueOf(s.ticketsSubmitted()));
        v.resolvedStat.setText(String.valueOf(s.ticketsResolved()));
        v.openStat.setText(String.valueOf(s.openTickets()));
        v.avgStat.setText(Format.duration(s.averageResolutionHours()));
    }

    private void showWorkload(List<WorkloadRow> rows) {
        workload.setAll(rows);
        XYChart.Series<String, Number> open = new XYChart.Series<>();
        open.setName("Open");
        XYChart.Series<String, Number> resolved = new XYChart.Series<>();
        resolved.setName("Resolved");
        for (WorkloadRow r : rows) {
            String name = r.technicianName() == null ? "#" + r.technicianId() : r.technicianName();
            open.getData().add(new XYChart.Data<>(name, r.openTickets()));
            resolved.getData().add(new XYChart.Data<>(name, r.resolvedTickets()));
        }
        v.workloadChart.getData().setAll(List.of(open, resolved));
    }

    private void exportWorkload() {
        if (workload.isEmpty()) {
            AlertUtil.info("Nothing to export", "Load the reports first — there is no workload data yet.");
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export technician workload");
        chooser.setInitialFileName("technician-workload.csv");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files", "*.csv"));
        File file = chooser.showSaveDialog(Navigator.stage());
        if (file == null) {
            return;
        }
        StringBuilder csv = new StringBuilder("Technician,Open tickets,Resolved tickets\n");
        for (WorkloadRow r : workload) {
            String name = r.technicianName() == null ? "" : r.technicianName().replace("\"", "\"\"");
            csv.append('"').append(name).append("\",")
                    .append(r.openTickets()).append(',')
                    .append(r.resolvedTickets()).append('\n');
        }
        try {
            Files.writeString(file.toPath(), csv, StandardCharsets.UTF_8);
            AlertUtil.info("Report exported", "Saved to " + file.getAbsolutePath());
        } catch (IOException e) {
            AlertUtil.error("Export failed", "Could not write the file: " + e.getMessage());
        }
    }

    private static String text(javafx.scene.control.TextInputControl field) {
        return field.getText() == null ? "" : field.getText().trim();
    }
}
