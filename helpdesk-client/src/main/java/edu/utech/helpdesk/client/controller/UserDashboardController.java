package edu.utech.helpdesk.client.controller;

import edu.utech.helpdesk.client.model.NotificationEvent;
import edu.utech.helpdesk.client.model.NotificationType;
import edu.utech.helpdesk.client.model.Status;
import edu.utech.helpdesk.client.model.TicketDto;
import edu.utech.helpdesk.client.model.TicketInput;
import edu.utech.helpdesk.client.model.UserDto;
import edu.utech.helpdesk.client.notify.NotificationHub;
import edu.utech.helpdesk.client.service.ApiClient;
import edu.utech.helpdesk.client.service.TicketQuery;
import edu.utech.helpdesk.client.ui.AlertUtil;
import edu.utech.helpdesk.client.ui.Filters;
import edu.utech.helpdesk.client.ui.FormValidator;
import edu.utech.helpdesk.client.ui.TicketDetailDialog;
import edu.utech.helpdesk.client.ui.TicketEditDialog;
import edu.utech.helpdesk.client.ui.TicketTable;
import edu.utech.helpdesk.client.util.Async;
import edu.utech.helpdesk.client.util.Disposable;
import edu.utech.helpdesk.client.util.Navigator;
import edu.utech.helpdesk.client.view.UserDashboardView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.TableRow;

import java.util.Locale;
import java.util.Objects;

/** Student / staff dashboard logic: list, create, edit, close, reopen tickets and change password. */
public final class UserDashboardController implements Disposable {

    private final UserDashboardView v;
    private final UserDto user;
    private final ApiClient api = ApiClient.get();

    private final ObservableList<TicketDto> master = FXCollections.observableArrayList();
    private final FilteredList<TicketDto> filtered = new FilteredList<>(master, t -> true);
    private final Runnable unsubscribe;

    public UserDashboardController(UserDashboardView view, UserDto user) {
        this.v = view;
        this.user = user;

        v.topBar.setOnLogout(Navigator::logout);

        SortedList<TicketDto> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(v.table.comparatorProperty());
        v.table.setItems(sorted);
        v.table.setRowFactory(tv -> {
            TableRow<TicketDto> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) {
                    viewSelected();
                }
            });
            return row;
        });
        v.table.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> updateButtons());

        v.statusFilter.valueProperty().addListener((obs, old, val) -> applyFilter());
        v.searchField.textProperty().addListener((obs, old, val) -> applyFilter());

        v.refreshButton.setOnAction(e -> refresh(false));
        v.viewButton.setOnAction(e -> viewSelected());
        v.editButton.setOnAction(e -> editSelected());
        v.closeButton.setOnAction(e -> closeSelected());
        v.reopenButton.setOnAction(e -> reopenSelected());
        v.submitButton.setOnAction(e -> submitTicket());
        v.clearButton.setOnAction(e -> v.ticketForm.clear());
        v.changePasswordButton.setOnAction(e -> changePassword());

        updateButtons();
        unsubscribe = NotificationHub.get().subscribe(this::onNotification);
        refresh(false);
    }

    @Override
    public void dispose() {
        unsubscribe.run();
        v.topBar.dispose();
    }

    // ---------------------------------------------------------------- loading

    private void refresh(boolean quiet) {
        v.refreshButton.setDisable(true);
        Async.run(() -> api.searchTickets(new TicketQuery().userId(user.id())),
                list -> {
                    v.refreshButton.setDisable(false);
                    TicketTable.replaceKeepingSelection(master, v.table, list);
                    updateCount();
                },
                err -> {
                    v.refreshButton.setDisable(false);
                    if (!quiet) {
                        AlertUtil.showError(err);
                    }
                });
    }

    private void onNotification(NotificationEvent event) {
        NotificationType type = event.type();
        if (type == NotificationType.TICKET_ASSIGNED || type == NotificationType.TICKET_RESOLVED
                || type == NotificationType.COMMENT_ADDED) {
            refresh(true);
        }
    }

    private void applyFilter() {
        Status status = Filters.status(v.statusFilter);
        String q = v.searchField.getText() == null ? "" : v.searchField.getText().trim().toLowerCase(Locale.ROOT);
        filtered.setPredicate(t -> (status == null || t.status() == status)
                && (q.isEmpty()
                || Objects.toString(t.subject(), "").toLowerCase(Locale.ROOT).contains(q)
                || String.valueOf(t.id()).equals(q.replace("#", ""))));
        updateCount();
    }

    private void updateCount() {
        v.countLabel.setText(filtered.size() + " of " + master.size() + " tickets");
    }

    private void updateButtons() {
        TicketDto t = v.table.getSelectionModel().getSelectedItem();
        boolean none = t == null;
        Status s = none ? null : t.status();
        v.viewButton.setDisable(none);
        v.editButton.setDisable(none || s == Status.CLOSED);
        v.closeButton.setDisable(none || s == Status.CLOSED);
        v.reopenButton.setDisable(none || !(s == Status.RESOLVED || s == Status.CLOSED));
    }

    // ---------------------------------------------------------------- actions

    private void viewSelected() {
        TicketDto t = v.table.getSelectionModel().getSelectedItem();
        if (t != null) {
            TicketDetailDialog.show(t);
        }
    }

    private void editSelected() {
        TicketDto t = v.table.getSelectionModel().getSelectedItem();
        if (t == null) {
            return;
        }
        TicketEditDialog.show(t).ifPresent((TicketInput input) ->
                Async.run(() -> api.updateTicket(t.id(), input),
                        saved -> refresh(true),
                        err -> AlertUtil.showError(err, "Ticket Not Updated")));
    }

    private void closeSelected() {
        TicketDto t = v.table.getSelectionModel().getSelectedItem();
        if (t == null) {
            return;
        }
        if (AlertUtil.confirm("Close ticket #" + t.id() + "?", "You can reopen it later if the problem comes back.")) {
            Async.run(() -> api.closeTicket(t.id()), saved -> refresh(true),
                    err -> AlertUtil.showError(err, "Ticket Not Closed"));
        }
    }

    private void reopenSelected() {
        TicketDto t = v.table.getSelectionModel().getSelectedItem();
        if (t == null) {
            return;
        }
        if (AlertUtil.confirm("Reopen ticket #" + t.id() + "?", "The ticket goes back to the support team.")) {
            Async.run(() -> api.reopenTicket(t.id()), saved -> refresh(true),
                    err -> AlertUtil.showError(err, "Ticket Not Reopened"));
        }
    }

    private void submitTicket() {
        if (!v.ticketForm.validate()) {
            return;
        }
        TicketInput input = v.ticketForm.toInput();
        v.submitButton.setDisable(true);
        Async.run(() -> api.createTicket(input, user.id()),
                created -> {
                    v.submitButton.setDisable(false);
                    v.ticketForm.clear();
                    String ref = created == null || created.id() == null ? "" : " #" + created.id();
                    AlertUtil.info("Ticket submitted", "Your ticket" + ref + " has been created. You will be notified when it is assigned.");
                    v.tabs.getSelectionModel().select(v.myTicketsTab);
                    refresh(true);
                },
                err -> {
                    v.submitButton.setDisable(false);
                    AlertUtil.showError(err, "Ticket Not Submitted");
                });
    }

    private void changePassword() {
        boolean valid = new FormValidator()
                .required(v.currentPassword, "Current password")
                .required(v.newPassword, "New password")
                .minLength(v.newPassword, "New password", 6)
                .required(v.confirmPassword, "Confirmation")
                .matches(v.newPassword, v.confirmPassword, "The new passwords do not match.")
                .validateOrAlert();
        if (!valid) {
            return;
        }
        String current = v.currentPassword.getText();
        String next = v.newPassword.getText();
        v.changePasswordButton.setDisable(true);
        Async.run(() -> {
                    api.changePassword(user.id(), current, next);
                    return null;
                },
                ok -> {
                    v.changePasswordButton.setDisable(false);
                    v.currentPassword.clear();
                    v.newPassword.clear();
                    v.confirmPassword.clear();
                    AlertUtil.info("Password changed", "Use your new password the next time you sign in.");
                },
                err -> {
                    v.changePasswordButton.setDisable(false);
                    AlertUtil.showError(err, "Password Not Changed");
                });
    }
}
