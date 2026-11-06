package edu.utech.helpdesk.client.ui;

import edu.utech.helpdesk.client.model.NotificationEvent;
import javafx.animation.PauseTransition;
import javafx.scene.control.Label;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

/** Small non-blocking pop-up in the bottom-right corner for incoming notifications. */
public final class Toast {

    private Toast() {
    }

    public static void show(Stage owner, NotificationEvent event) {
        if (owner == null || !owner.isShowing()) {
            return;
        }
        String heading = event.type().label() + (event.ticketId() != null ? "  ·  #" + event.ticketId() : "");
        Label label = new Label(heading + "\n" + event.message());
        label.getStyleClass().add("toast");
        label.setWrapText(true);
        label.setMaxWidth(340);

        Popup popup = new Popup();
        popup.getContent().add(label);
        popup.getScene().getStylesheets().add(Theme.css());
        popup.show(owner, owner.getX() + owner.getWidth() - 380, owner.getY() + owner.getHeight() - 140);
        popup.setX(owner.getX() + owner.getWidth() - popup.getWidth() - 24);
        popup.setY(owner.getY() + owner.getHeight() - popup.getHeight() - 24);

        PauseTransition delay = new PauseTransition(Duration.seconds(4.5));
        delay.setOnFinished(e -> popup.hide());
        delay.play();
    }
}
