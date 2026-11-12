package edu.utech.helpdesk.client.controller;

import edu.utech.helpdesk.client.model.NotificationEvent;
import edu.utech.helpdesk.client.model.NotificationType;
import edu.utech.helpdesk.client.model.Status;
import edu.utech.helpdesk.client.model.TicketDto;
import edu.utech.helpdesk.client.model.UserDto;
import edu.utech.helpdesk.client.notify.NotificationHub;
import edu.utech.helpdesk.client.service.ApiClient;
import edu.utech.helpdesk.client.service.TicketQuery;
import edu.utech.helpdesk.client.ui.AlertUtil;
import edu.utech.helpdesk.client.ui.Badges;
import edu.utech.helpdesk.client.ui.Filters;
import edu.utech.helpdesk.client.ui.FormValidator;
import edu.utech.helpdesk.client.ui.TicketDetailDialog;
import edu.utech.helpdesk.client.util.Async;
import edu.utech.helpdesk.client.util.Disposable;
import edu.utech.helpdesk.client.util.Format;
import edu.utech.helpdesk.client.util.Navigator;
import edu.utech.helpdesk.client.view.TechnicianDashboardView;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.TableRow;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Technician dashboard logic.
 *
 * Multithreading requirement 1: a background scheduler thread re-reads the technician's tickets every
 * {@value #REFRESH_SECONDS} seconds and hands the result to the JavaFX thread with Platform.runLater,
 * so the GUI never freezes while the network call is running.
 */
public final class TechnicianDashboardController implements Disposable {

    private static final System.Logger LOG = System.getLogger(TechnicianDashboardController.class.getName());
    private static final int REFRESH_SECONDS = 15;
    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final TechnicianDashboardView v;
    private final ApiClient api = ApiClient.get();
    private final Long technicianId;

    private final ObservableList<TicketDto> master = FXCollections.observableArrayList();
    private final FilteredList<TicketDto> filtered = new FilteredList<>(master, t -> true);
    private final ScheduledExecutorService scheduler;
    private final Runnable unsubscribe;

    private volatile boolean autoRefreshOn = true;
    private boolean refreshing;

    public TechnicianDashboardController(TechnicianDashboardView view, UserDto user) {
        this.v = view;
        this.technicianId = user.technicianId() != null ? user.technicianId() : user.id();

        v.topBar.setOnLogout(Navigator::logout);

        SortedList<TicketDto> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(v.table.comparatorProperty());
        v.table.setItems(sorted);
        v.table.setRowFactory(tv -> {
            TableRow<TicketDto> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) {
                    showComments();
                }
            });
            return row;
        });
        v.table.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (!refreshing) {
                populate(sel, true);
            }
        });

        v.statusFilter.valueProperty().addListener((obs, old, val) -> applyFilter());
        v.searchField.textProperty().addListener((obs, old, val) -> applyFilter());
        v.autoRefresh.selectedProperty().addListener((obs, old, on) -> {
            autoRefreshOn = on;
            if (on) {
                loadNow(true);
            }
        });

        v.refreshButton.setOnAction(e -> loadNow(false));
        v.updateStatusButton.setOnAction(e -> updateStatus());
        v.resolveButton.setOnAction(e -> resolve());
        v.commentsButton.setOnAction(e -> showComments());

        populate(null, true);

        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "ticket-refresh");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleWithFixedDelay(this::backgroundRefresh, REFRESH_SECONDS, REFRESH_SECONDS, TimeUnit.SECONDS);

        unsubscribe = NotificationHub.get().subscribe(this::onNotification);
        loadNow(false);
    }

    @Override
    public void dispose() {
        scheduler.shutdownNow();
        unsubscribe.run();
        v.topBar.dispose();
    }

    // ---------------------------------------------------------------- loading

    private TicketQuery query() {
        return new TicketQuery().technicianId(technicianId);
    }

    /** Manual / event-driven refresh: network call on a worker thread, result back on the FX thread. */
    private void loadNow(boolean quiet) {
        v.refreshButton.setDisable(true);
        Async.run(() -> api.searchTickets(query()),
                list -> {
                    v.refreshButton.setDisable(false);
                    applyTickets(list);
                },
                err -> {
                    v.refreshButton.setDisable(false);
                    v.lastRefreshed.setText("Refresh failed at " + LocalTime.now().format(CLOCK));
                    if (!quiet) {
                        AlertUtil.showError(err);
                    }
                });
    }

    /** Runs on the scheduler thread, never on the FX thread. */
    private void backgroundRefresh() {
        if (!autoRefreshOn) {
            return;
        }
        try {
            List<TicketDto> list = api.searchTickets(query());
            Platform.runLater(() -> applyTickets(list));
        } catch (Exception e) {
            LOG.log(System.Logger.Level.WARNING, "Background refresh failed: {0}", e.getMessage());
            Platform.runLater(() -> v.lastRefreshed.setText("Auto-refresh failed at "
                    + LocalTime.now().format(CLOCK) + " — will retry"));
        }
    }

    private void onNotification(NotificationEvent event) {
        if (event.type() == NotificationType.TICKET_ASSIGNED || event.type() == NotificationType.COMMENT_ADDED) {
            loadNow(true);
        }
    }

    /** FX thread only. Keeps the selected ticket and whatever the technician has typed in the notes box. */
    private void applyTickets(List<TicketDto> list) {
        TicketDto selected = v.table.getSelectionModel().getSelectedItem();
        Long selectedId = selected == null ? null : selected.id();

        refreshing = true;
        try {
            master.setAll(list);
            if (selectedId != null) {
                for (TicketDto t : v.table.getItems()) {
                    if (selectedId.equals(t.id())) {
                        v.table.getSelectionModel().select(t);
                        break;
                    }
                }
            }
        } finally {
            refreshing = false;
        }

        TicketDto now = v.table.getSelectionModel().getSelectedItem();
        if (now != null) {
            populate(now, false);
        } else if (selectedId != null) {
            populate(null, true);
        }
        updateStats();
        v.lastRefreshed.setText("Updated " + LocalTime.now().format(CLOCK)
                + (autoRefreshOn ? "  ·  refreshes every " + REFRESH_SECONDS + " s" : ""));
    }

    private void applyFilter() {
        Status status = Filters.status(v.statusFilter);
        String q = v.searchField.getText() == null ? "" : v.searchField.getText().trim().toLowerCase(Locale.ROOT);
        filtered.setPredicate(t -> (status == null || t.status() == status)
                && (q.isEmpty()
                || Objects.toString(t.subject(), "").toLowerCase(Locale.ROOT).contains(q)
                || String.valueOf(t.id()).equals(q.replace("#", ""))));
    }

    private void updateStats() {
        v.assignedStat.setText(String.valueOf(master.stream().filter(t -> t.status() == Status.ASSIGNED).count()));
        v.inProgressStat.setText(String.valueOf(master.stream().filter(t -> t.status() == Status.IN_PROGRESS).count()));
        v.pendingStat.setText(String.valueOf(master.stream().filter(t -> t.status() == Status.PENDING).count()));
        v.resolvedStat.setText(String.valueOf(master.stream().filter(t -> t.status() == Status.RESOLVED).count()));
    }

    // ------------------------------------------------------------- work panel

    /** @param resetInputs true when the technician picked a different ticket (clears the notes box) */
    private void populate(TicketDto t, boolean resetInputs) {
        boolean has = t != null;
        v.panelTitle.setText(has ? "#" + t.id() + "  " + t.subject() : "Select a ticket");
        v.panelMeta.setText(has
                ? "Requested by " + Format.orDash(t.userName()) + "  ·  " + Format.dateTime(t.creationDate())
                : "Choose a ticket from the list to work on it.");
        v.badgeBox.getChildren().setAll();
        if (has) {
            v.badgeBox.getChildren().addAll(Badges.of(t.status()), Badges.of(t.priority()));
        }
        v.descriptionArea.setText(has && t.description() != null ? t.description() : "");

        boolean workable = has && t.status() != null && t.status().isOpen();
        v.statusBox.setDisable(!workable);
        v.updateStatusButton.setDisable(!workable);
        v.notesArea.setDisable(!workable);
        v.resolveButton.setDisable(!workable);
        v.commentsButton.setDisable(!has);

        if (resetInputs) {
            v.notesArea.setText(has && t.resolutionNotes() != null ? t.resolutionNotes() : "");
            v.statusBox.setValue(has && t.status() == Status.PENDING ? Status.PENDING : Status.IN_PROGRESS);
        }
    }

    private void updateStatus() {
        TicketDto t = v.table.getSelectionModel().getSelectedItem();
        Status status = v.statusBox.getValue();
        if (t == null || status == null) {
            AlertUtil.warn("Nothing to update", "Select a ticket and a status first.");
            return;
        }
        v.updateStatusButton.setDisable(true);
        Async.run(() -> api.updateStatus(t.id(), status, technicianId),
                saved -> loadNow(true),
                err -> {
                    v.updateStatusButton.setDisable(false);
                    AlertUtil.showError(err, "Status Not Updated");
                });
    }

    private void resolve() {
        TicketDto t = v.table.getSelectionModel().getSelectedItem();
        if (t == null) {
            return;
        }
        boolean valid = new FormValidator()
                .required(v.notesArea, "Resolution notes")
                .minLength(v.notesArea, "Resolution notes", 10)
                .validateOrAlert();
        if (!valid) {
            return;
        }
        String notes = v.notesArea.getText().trim();
        if (!AlertUtil.confirm("Mark ticket #" + t.id() + " as resolved?", "The requester will be notified.")) {
            return;
        }
        v.resolveButton.setDisable(true);
        Async.run(() -> api.resolveTicket(t.id(), notes, technicianId),
                saved -> {
                    AlertUtil.info("Ticket resolved", "Ticket #" + t.id() + " is now marked as resolved.");
                    loadNow(true);
                },
                err -> {
                    v.resolveButton.setDisable(false);
                    AlertUtil.showError(err, "Ticket Not Resolved");
                });
    }

    private void showComments() {
        TicketDto t = v.table.getSelectionModel().getSelectedItem();
        if (t != null) {
            TicketDetailDialog.show(t);
        }
    }
}
