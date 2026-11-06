package edu.utech.helpdesk.client.ui;

import edu.utech.helpdesk.client.util.Navigator;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * A modal OK/Cancel dialog whose OK button refuses to close the dialog until {@code validate} passes.
 * Used by every form dialog so validation behaves the same everywhere.
 */
public final class FormDialog {

    private FormDialog() {
    }

    public static <T> Optional<T> show(String title, String header, String okText, Node content,
                                       BooleanSupplier validate, Supplier<T> result) {
        Dialog<T> dialog = new Dialog<>();
        dialog.initOwner(Navigator.stage());
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.getDialogPane().getStylesheets().add(Theme.css());
        dialog.getDialogPane().setContent(content);

        ButtonType ok = new ButtonType(okText, ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ok);
        okButton.getStyleClass().add("btn-primary");
        okButton.addEventFilter(ActionEvent.ACTION, e -> {
            if (!validate.getAsBoolean()) {
                e.consume();
            }
        });
        dialog.setResultConverter(button -> button == ok ? result.get() : null);
        return dialog.showAndWait();
    }
}
