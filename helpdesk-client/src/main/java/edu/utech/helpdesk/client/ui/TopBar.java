package edu.utech.helpdesk.client.ui;

import edu.utech.helpdesk.client.model.NotificationEvent;
import edu.utech.helpdesk.client.model.UserDto;
import edu.utech.helpdesk.client.notify.NotificationHub;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.HBox;

import java.time.format.DateTimeFormatter;

/** Header strip shown on every dashboard: title, live-connection dot, notification bell, user and log out. */
public final class TopBar extends HBox {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    private final Button logoutButton = new Button("Log out");
    private final MenuButton bell = new MenuButton("🔔");
    private final Label connection = new Label();
    private final NotificationHub hub = NotificationHub.get();
    private final Runnable unsubscribe;
    private final ChangeListener<Boolean> connectionListener = (obs, old, connected) -> showConnection(connected);
    private int unread;

    public TopBar(UserDto user) {
        super(14);
        getStyleClass().add("top-bar");
        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(10, 20, 10, 20));

        Label title = new Label("University Help Desk");
        title.getStyleClass().add("app-title");

        Label who = new Label(user.fullName() + "  ·  " + (user.role() == null ? "" : user.role().label()));
        who.getStyleClass().add("top-bar-user");

        bell.getStyleClass().add("bell");
        bell.setOnShowing(e -> {
            rebuildBellMenu();
            unread = 0;
            updateBell();
        });
        rebuildBellMenu();

        connection.getStyleClass().add("connection");
        showConnection(hub.connectedProperty().get());
        hub.connectedProperty().addListener(connectionListener);

        unsubscribe = hub.subscribe(this::onNotification);

        logoutButton.getStyleClass().add("btn-ghost");
        getChildren().addAll(title, Ui.spacer(), connection, bell, who, logoutButton);
    }

    public void setOnLogout(Runnable action) {
        logoutButton.setOnAction(e -> action.run());
    }

    public void dispose() {
        unsubscribe.run();
        hub.connectedProperty().removeListener(connectionListener);
    }

    private void onNotification(NotificationEvent event) {
        unread++;
        updateBell();
    }

    private void updateBell() {
        bell.setText(unread > 0 ? "🔔 " + unread : "🔔");
    }

    private void rebuildBellMenu() {
        bell.getItems().clear();
        if (hub.history().isEmpty()) {
            MenuItem none = new MenuItem("No notifications yet");
            none.setDisable(true);
            bell.getItems().add(none);
            return;
        }
        hub.history().stream().limit(8).forEach(e -> bell.getItems().add(new MenuItem(
                TIME.format(e.receivedAt()) + "  " + e.type().label()
                        + (e.ticketId() != null ? " #" + e.ticketId() : "") + " — " + e.message())));
        bell.getItems().add(new SeparatorMenuItem());
        MenuItem clear = new MenuItem("Clear all");
        clear.setOnAction(ev -> hub.clear());
        bell.getItems().add(clear);
    }

    private void showConnection(boolean connected) {
        connection.setText(connected ? "● Live" : "● Offline");
        connection.getStyleClass().removeAll("online", "offline");
        connection.getStyleClass().add(connected ? "online" : "offline");
    }
}
