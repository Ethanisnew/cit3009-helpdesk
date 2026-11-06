package edu.utech.helpdesk.client.util;

import edu.utech.helpdesk.client.controller.AdminDashboardController;
import edu.utech.helpdesk.client.controller.LoginController;
import edu.utech.helpdesk.client.controller.TechnicianDashboardController;
import edu.utech.helpdesk.client.controller.UserDashboardController;
import edu.utech.helpdesk.client.model.Role;
import edu.utech.helpdesk.client.model.UserDto;
import edu.utech.helpdesk.client.notify.NotificationClient;
import edu.utech.helpdesk.client.notify.NotificationHub;
import edu.utech.helpdesk.client.service.ApiClient;
import edu.utech.helpdesk.client.ui.Theme;
import edu.utech.helpdesk.client.ui.Toast;
import edu.utech.helpdesk.client.view.AdminDashboardView;
import edu.utech.helpdesk.client.view.LoginView;
import edu.utech.helpdesk.client.view.TechnicianDashboardView;
import edu.utech.helpdesk.client.view.UserDashboardView;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Switches the single primary Stage between the Login screen and the three dashboards,
 * and owns the lifetime of the notification connection that belongs to a login.
 */
public final class Navigator {

    private static final System.Logger LOG = System.getLogger(Navigator.class.getName());

    private static Stage stage;
    private static Disposable current;
    private static NotificationClient notifier;
    private static Runnable toastSubscription = () -> { };

    private Navigator() {
    }

    public static void init(Stage primary) {
        stage = primary;
    }

    public static Stage stage() {
        return stage;
    }

    public static void showLogin() {
        disposeCurrent();
        LoginView view = new LoginView();
        new LoginController(view);
        display(view.root, "Sign in", 1000, 640, 900, 600);
    }

    public static void showDashboardFor(UserDto user) {
        disposeCurrent();
        Session.set(user);
        startNotifications(user);
        LOG.log(System.Logger.Level.INFO, "User {0} signed in as {1}", user.username(), user.role());

        Role role = user.role() == null ? Role.USER : user.role();
        switch (role) {
            case ADMIN -> {
                AdminDashboardView view = new AdminDashboardView(user);
                current = new AdminDashboardController(view, user);
                display(view.root, "Administrator", 1240, 760, 1040, 660);
            }
            case TECHNICIAN -> {
                TechnicianDashboardView view = new TechnicianDashboardView(user);
                current = new TechnicianDashboardController(view, user);
                display(view.root, "Technician", 1240, 760, 1040, 660);
            }
            case USER -> {
                UserDashboardView view = new UserDashboardView(user);
                current = new UserDashboardController(view, user);
                display(view.root, "My Tickets", 1120, 740, 980, 640);
            }
        }
    }

    public static void logout() {
        UserDto user = Session.user();
        if (user != null) {
            LOG.log(System.Logger.Level.INFO, "User {0} signed out", user.username());
            Async.run(() -> {
                ApiClient.get().logout(user.id());
                return null;
            }, ok -> { }, err -> LOG.log(System.Logger.Level.WARNING, "Logout call failed: {0}", err.getMessage()));
        }
        stopNotifications();
        Session.clear();
        showLogin();
    }

    public static void shutdown() {
        disposeCurrent();
        stopNotifications();
    }

    private static void display(Parent root, String title, double width, double height, double minWidth, double minHeight) {
        Scene scene = new Scene(root, width, height);
        scene.getStylesheets().add(Theme.css());
        stage.setTitle("University Help Desk — " + title);
        stage.setScene(scene);
        stage.setMinWidth(minWidth);
        stage.setMinHeight(minHeight);
        stage.setWidth(width);
        stage.setHeight(height);
        stage.centerOnScreen();
    }

    private static void disposeCurrent() {
        if (current != null) {
            current.dispose();
            current = null;
        }
    }

    private static void startNotifications(UserDto user) {
        stopNotifications();
        notifier = new NotificationClient();
        notifier.start(user);
        toastSubscription = NotificationHub.get().subscribe(event -> Toast.show(stage, event));
    }

    private static void stopNotifications() {
        if (notifier != null) {
            notifier.stop();
            notifier = null;
        }
        toastSubscription.run();
        toastSubscription = () -> { };
        NotificationHub.get().clear();
    }
}
