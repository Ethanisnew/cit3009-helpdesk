package edu.utech.helpdesk.client.notify;

import edu.utech.helpdesk.client.model.NotificationEvent;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * In-memory bus between the TCP {@link NotificationClient} and the screens.
 * Everything here runs on the JavaFX Application Thread.
 */
public final class NotificationHub {

    private static final int MAX_HISTORY = 200;
    private static final NotificationHub INSTANCE = new NotificationHub();

    public static NotificationHub get() {
        return INSTANCE;
    }

    private final ObservableList<NotificationEvent> history = FXCollections.observableArrayList();
    private final List<Consumer<NotificationEvent>> listeners = new ArrayList<>();
    private final BooleanProperty connected = new SimpleBooleanProperty(false);

    private NotificationHub() {
    }

    /** Newest first. */
    public ObservableList<NotificationEvent> history() {
        return history;
    }

    public BooleanProperty connectedProperty() {
        return connected;
    }

    public void post(NotificationEvent event) {
        history.add(0, event);
        if (history.size() > MAX_HISTORY) {
            history.remove(history.size() - 1);
        }
        for (Consumer<NotificationEvent> l : new ArrayList<>(listeners)) {
            l.accept(event);
        }
    }

    /** @return a Runnable that removes the subscription again */
    public Runnable subscribe(Consumer<NotificationEvent> listener) {
        listeners.add(listener);
        return () -> listeners.remove(listener);
    }

    public void clear() {
        history.clear();
    }
}
